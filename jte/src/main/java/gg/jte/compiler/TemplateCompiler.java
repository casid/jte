package gg.jte.compiler;

import gg.jte.CodeResolver;
import gg.jte.TemplateConfig;
import gg.jte.TemplateException;
import gg.jte.TemplateNotFoundException;
import gg.jte.compiler.java.JavaClassCompiler;
import gg.jte.compiler.java.JavaCodeGenerator;
import gg.jte.output.FileOutput;
import gg.jte.runtime.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TemplateCompiler extends TemplateLoader {

    public static final boolean DEBUG = false;

    private final TemplateConfig config;
    private final CodeResolver codeResolver;
    private final ClassLoader parentClassLoader;

    private final ConcurrentHashMap<String, LinkedHashSet<TemplateDependency>> templateDependencies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<ParamInfo>> paramOrder = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ClassInfo> templateByClassName = new ConcurrentHashMap<>();

    private List<String> classPath;

    public TemplateCompiler(TemplateConfig config, CodeResolver codeResolver, Path classDirectory, ClassLoader parentClassLoader) {
        super(classDirectory, config.packageName);
        this.config = config;
        this.codeResolver = codeResolver;
        this.parentClassLoader = parentClassLoader;
    }

    @Override
    public Template load(String name) {
        precompile(Collections.singletonList(name));
        return super.load(name);
    }

    @Override
    public Template hotReload(String name) {
        LinkedHashSet<ClassDefinition> classDefinitions = generate(Collections.singletonList(name), true);
        classDefinitions.removeIf(c -> !c.isChanged());

        if (!classDefinitions.isEmpty()) {
            precompileClasses(classDefinitions);
        }

        return super.load(name);
    }

    @Override
    protected ClassInfo getClassInfo(ClassLoader classLoader, String className) {
        return templateByClassName.get(className);
    }

    @Override
    protected ClassLoader getClassLoader() {
        return createClassLoader(parentClassLoader);
    }

    @Override
    public void cleanAll() {
        IoUtils.deleteDirectoryContent(classDirectory.resolve(config.packageName.replace('.', '/')));
    }

    @Override
    public List<String> generateAll() {
        LinkedHashSet<ClassDefinition> classDefinitions = generate(codeResolver.resolveAllTemplateNames(), false);
        return classDefinitions.stream().map(ClassDefinition::getSourceFileName).collect(Collectors.toList());
    }

    @Override
    public List<String> precompileAll() {
        return precompile(codeResolver.resolveAllTemplateNames());
    }
    
    /**
     * Generate configuration files that can be read by Graal native-image.
     * See <a href="https://www.graalvm.org/reference-manual/native-image/BuildConfiguration/">GraalVM reference manual</a>.
     * @param classDefinitions details of generated classes
     */
    private void generateNativeResources(LinkedHashSet<ClassDefinition> classDefinitions) {
        if (!config.generateNativeImageResources) {
            return;
        }

        if (config.resourceDirectory == null) {
            return;
        }

        if (classDefinitions.isEmpty()) {
            return;
        }

        String namespace = config.projectNamespace != null ? config.projectNamespace : packageName;
        Path nativeImageResourceRoot = config.resourceDirectory.resolve("META-INF/native-image/jte-generated/" + namespace);
        try (FileOutput properties = new FileOutput(nativeImageResourceRoot.resolve("native-image.properties"))) {
            properties.writeContent("Args = -H:ReflectionConfigurationResources=${.}/reflection-config.json -H:ResourceConfigurationResources=${.}/resource-config.json\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // avoid adding a json dependency to the project by just writing text
        try (FileOutput reflection = new FileOutput(nativeImageResourceRoot.resolve("reflection-config.json"))) {
            boolean first = true;

            reflection.writeContent("[\n");
            for (ClassDefinition classDefinition : classDefinitions) {
                if (!first) {
                    reflection.writeContent(",\n");
                }

                reflection.writeContent("{\n  \"name\":\"");
                reflection.writeContent(classDefinition.getName());
                reflection.writeContent("\",\n  \"allDeclaredMethods\":true,\n  \"allDeclaredFields\":true\n}");

                first = false;
            }
            reflection.writeContent("\n]\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        try (FileOutput resource = new FileOutput(nativeImageResourceRoot.resolve("resource-config.json"))) {
            resource.writeContent("{\"resources\": {\"includes\": [{\"pattern\": \".*Generated\\\\.bin$\"}]}}\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<String> precompile(List<String> names) {
        LinkedHashSet<ClassDefinition> classDefinitions = generate(names, false);
        return precompileClasses(classDefinitions);
    }

    private List<String> precompileClasses(LinkedHashSet<ClassDefinition> classDefinitions) {
        List<String> classPath = getClassPath();

        Set<String> extensions = new HashSet<>();

        String[] files = new String[classDefinitions.size()];
        int i = 0;
        for (ClassDefinition classDefinition : classDefinitions) {
            files[i++] = classDirectory.resolve(classDefinition.getSourceFileName()).toFile().getAbsolutePath();
            extensions.add(classDefinition.getExtension());
        }

        List<String> javaCompilerClassPath = new ArrayList<>(classPath);
        javaCompilerClassPath.add(classDirectory.toAbsolutePath().toString());

        if (extensions.size() == 1) {
            ClassCompiler compiler = createCompiler(extensions.iterator().next());
            compiler.compile(files, javaCompilerClassPath, config, classDirectory, templateByClassName);
        } else if (extensions.size() > 1) {
            // As there is currently only support for java and kotlin as expression language, this is the java / kotlin case.
            // We first need to compile all kotlin classes while passing generate .java files to the kotlin compiler.
            // Then, we invoke the Java compiler with the compiled kotlin classes on the classpath.
            // https://discuss.kotlinlang.org/t/compiling-mixed-java-and-kotlin-files-on-the-command-line/1553/4

            ClassCompiler kotlinCompiler = createCompiler("kt");
            kotlinCompiler.compile(files, classPath, config, classDirectory, templateByClassName);

            String[] javaFiles = Arrays.stream(files).filter(f -> f.endsWith(".java")).toArray(String[]::new);

            ClassCompiler javaCompiler = createCompiler("java");
            javaCompiler.compile(javaFiles, javaCompilerClassPath, config, classDirectory, templateByClassName);
        }

        return classDefinitions.stream().map(ClassDefinition::getSourceFileName).collect(Collectors.toList());
    }

    private List<String> getClassPath() {
        if (classPath == null) {
            classPath = calculateClassPath();
        }

        return classPath;
    }

    private List<String> calculateClassPath() {
        if (config.classPath != null) {
            return config.classPath;
        } else {
            List<String> classPath = new ArrayList<>();
            ClassUtils.resolveClasspathFromClassLoader(parentClassLoader, classPath::add);
            return classPath;
        }
    }

    ClassCompiler createCompiler(String extension) {
        if ("kt".equals(extension)) {
            try {
                Class<?> compilerClass = Class.forName("gg.jte.compiler.kotlin.KotlinClassCompiler");
                return (ClassCompiler)compilerClass.getConstructor().newInstance();
            } catch (Exception e) {
                throw new TemplateException("Failed to create kotlin compiler. To compile .kte files, you need to add gg.jte:jte-kotlin to your project.", e);
            }
        } else {
            return new JavaClassCompiler();
        }
    }

    private LinkedHashSet<ClassDefinition> generate(List<String> names, boolean trackChanges) {
        LinkedHashSet<ClassDefinition> classDefinitions = new LinkedHashSet<>();
        for (String name : names) {
            LinkedHashSet<TemplateDependency> dependencies = initTemplateDependencies(name);

            ClassInfo templateInfo = generateTemplateCall(name, classDefinitions, dependencies, null);

            templateDependencies.put(name, dependencies);
            templateByClassName.put(templateInfo.name, templateInfo);
        }

        Path resourceDirectory = config.resourceDirectory == null ? classDirectory : config.resourceDirectory;
        for (ClassDefinition classDefinition : classDefinitions) {
            Path sourceFile = classDirectory.resolve(classDefinition.getSourceFileName());
            if (trackChanges) {
                String sourceFileContent = IoUtils.toString(sourceFile);
                classDefinition.setChanged(!classDefinition.getCode().equals(sourceFileContent));
            }

            try (FileOutput fileOutput = new FileOutput(sourceFile)) {
                fileOutput.writeContent(classDefinition.getCode());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            List<byte[]> textParts = classDefinition.getBinaryTextParts();
            if (!textParts.isEmpty()) {
                try {
                    Files.createDirectories(resourceDirectory.resolve(classDefinition.getBinaryTextPartsFileName()).getParent());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                try (OutputStream os = Files.newOutputStream(resourceDirectory.resolve(classDefinition.getBinaryTextPartsFileName()), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
                    for (byte[] textPart : textParts) {
                        os.write(textPart);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }

        generateNativeResources(classDefinitions);

        return classDefinitions;
    }

    private LinkedHashSet<TemplateDependency> initTemplateDependencies(String name) {
        LinkedHashSet<TemplateDependency> templateDependencies = new LinkedHashSet<>();
        templateDependencies.add(new TemplateDependency(name, codeResolver.getLastModified(name)));
        return templateDependencies;
    }

    public ClassInfo generateTemplateCall(String simpleName, String extension, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<TemplateDependency> templateDependencies, DebugInfo debugInfo) {
        String name = resolveTemplateName(simpleName, extension);
        try {
            return generateTemplateCall(name, classDefinitions, templateDependencies, debugInfo);
        } catch (TemplateNotFoundException e) {
            String alternativeName = resolveTemplateName(simpleName, "jte".equals(extension) ? "kte" : "jte");

            if (codeResolver.exists(alternativeName)) {
                return generateTemplateCall(alternativeName, classDefinitions, templateDependencies, debugInfo);
            } else {
                throw e;
            }
        }
    }

    private String resolveTemplateName(String simpleName, String extension) {
        return simpleName.replace('.', '/') + "." + extension;
    }

    public ClassInfo generateTemplateCall(String name, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<TemplateDependency> templateDependencies, DebugInfo debugInfo) {
        templateDependencies.add(new TemplateDependency(name, codeResolver.getLastModified(name)));
        ClassInfo classInfo = new ClassInfo(name, config.packageName);

        ClassDefinition classDefinition = new ClassDefinition(classInfo.fullName, classInfo);
        if (classDefinitions.contains(classDefinition)) {
            return classInfo;
        }

        String code = resolveCode(name, debugInfo);

        classDefinitions.add(classDefinition);

        CodeGenerator codeGenerator = createCodeGenerator(name, classInfo, classDefinitions, templateDependencies);
        new TemplateParser(code, TemplateType.Template, codeGenerator, config, codeResolver).parse();

        classDefinition.setCode(codeGenerator.getCode(), codeGenerator.getBinaryTextParts());
        templateByClassName.put(classDefinition.getName(), classInfo);

        if (DEBUG) {
            System.out.println(classDefinition.getCode());
        }

        return classInfo;
    }

    private CodeGenerator createCodeGenerator(String templateName, ClassInfo classInfo, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<TemplateDependency> templateDependencies) {
        if ("kte".equals(classInfo.extension)) {
            try {
                Class<?> compilerClass = Class.forName("gg.jte.compiler.kotlin.KotlinCodeGenerator");
                return (CodeGenerator)compilerClass.getConstructor(TemplateCompiler.class, TemplateConfig.class, ConcurrentHashMap.class, ClassInfo.class, LinkedHashSet.class, LinkedHashSet.class).newInstance(this, this.config, paramOrder, classInfo, classDefinitions, templateDependencies);
            } catch (Exception e) {
                throw new TemplateException("Failed to create kotlin generator. To handle .kte files, you need to add gg.jte:jte-kotlin to your project.", e);
            }
        } else {
            return new JavaCodeGenerator(templateName, this, this.config, paramOrder, classInfo, classDefinitions, templateDependencies);
        }
    }

    private String resolveCode(String name, DebugInfo debugInfo) {
        String code = codeResolver.resolve(name);
        if (code == null) {
            String message = name + " not found";
            if (debugInfo != null) {
                message += ", referenced at " + debugInfo.name + ":" + debugInfo.line;
            }
            throw new TemplateNotFoundException(message);
        }
        return code;
    }

    @Override
    public boolean hasChanged(String name) {
        LinkedHashSet<TemplateDependency> dependencies = this.templateDependencies.get(name);
        if (dependencies == null) {
            return false;
        }

        for (TemplateDependency dependency : dependencies) {
            if (codeResolver.getLastModified(dependency.getName()) > dependency.getLastModifiedTimestamp()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<String> getTemplatesUsing(String name) {
        TemplateDependency dependency = new TemplateDependency(name, 0);

        List<String> result = new ArrayList<>();
        for (Map.Entry<String, LinkedHashSet<TemplateDependency>> dependencies : templateDependencies.entrySet()) {
            if (dependencies.getValue().contains(dependency)) {
                result.add(dependencies.getKey());
            }
        }

        return result;
    }
}
