package gg.jte.compiler;

import gg.jte.*;
import gg.jte.compiler.java.JavaClassCompiler;
import gg.jte.compiler.java.JavaCodeGenerator;
import gg.jte.runtime.*;
import gg.jte.output.FileOutput;

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

    private final ConcurrentHashMap<String, LinkedHashSet<String>> templateDependencies = new ConcurrentHashMap<>();
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
        LinkedHashSet<ClassDefinition> classDefinitions = generate(codeResolver.resolveAllTemplateNames());
        return classDefinitions.stream().map(ClassDefinition::getSourceFileName).collect(Collectors.toList());
    }

    @Override
    public List<String> precompileAll() {
        return precompile(codeResolver.resolveAllTemplateNames());
    }

    public List<String> precompile(List<String> names) {
        LinkedHashSet<ClassDefinition> classDefinitions = generate(names);

        List<String> classPath = getClassPath();

        Set<String> extensions = new HashSet<>();

        String[] files = new String[classDefinitions.size()];
        int i = 0;
        for (ClassDefinition classDefinition : classDefinitions) {
            files[i++] = classDirectory.resolve(classDefinition.getSourceFileName()).toFile().getAbsolutePath();
            extensions.add(classDefinition.getExtension());
        }

        if (extensions.size() == 1) {
            ClassCompiler compiler = createCompiler(extensions.iterator().next());
            compiler.compile(files, classPath, config, classDirectory, templateByClassName);
        } else if (extensions.size() > 1) {
            // As there is currently only support for java and kotlin as expression language, this is the java / kotlin case.
            // We first need to compile all kotlin classes while passing generate .java files to the kotlin compiler.
            // Then, we invoke the Java compiler with the compiled kotlin classes on the classpath.
            // https://discuss.kotlinlang.org/t/compiling-mixed-java-and-kotlin-files-on-the-command-line/1553/4

            ClassCompiler kotlinCompiler = createCompiler("kt");
            kotlinCompiler.compile(files, classPath, config, classDirectory, templateByClassName);

            String[] javaFiles = Arrays.stream(files).filter(f -> f.endsWith(".java")).toArray(String[]::new);
            ClassCompiler javaCompiler = createCompiler("java");
            List<String> javaCompilerClassPath = new ArrayList<>(classPath);
            javaCompilerClassPath.add(classDirectory.toAbsolutePath().toString());

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

    private LinkedHashSet<ClassDefinition> generate(List<String> names) {
        LinkedHashSet<ClassDefinition> classDefinitions = new LinkedHashSet<>();
        for (String name : names) {
            switch (getTemplateType(name)) {
                case Template:
                    generateTemplate(name, classDefinitions);
                    break;
                case Tag:
                    generateTemplateFromTag(name, classDefinitions);
                    break;
                case Layout:
                    generateTemplateFromLayout(name, classDefinitions);
                    break;
            }
        }

        for (ClassDefinition classDefinition : classDefinitions) {
            try (FileOutput fileOutput = new FileOutput(classDirectory.resolve(classDefinition.getSourceFileName()))) {
                fileOutput.writeContent(classDefinition.getCode());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            List<byte[]> textParts = classDefinition.getBinaryTextParts();
            if (!textParts.isEmpty()) {
                try (OutputStream os = Files.newOutputStream(classDirectory.resolve(classDefinition.getBinaryTextPartsFileName()), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
                    for (byte[] textPart : textParts) {
                        os.write(textPart);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        return classDefinitions;
    }

    private void generateTemplate(String name, LinkedHashSet<ClassDefinition> classDefinitions) {
        String code = resolveCode(name, null);

        LinkedHashSet<String> templateDependencies = new LinkedHashSet<>();

        ClassInfo templateInfo = new ClassInfo(name, config.packageName);

        CodeGenerator codeGenerator = createCodeGenerator(templateInfo, classDefinitions, templateDependencies);
        new TemplateParser(code, TemplateType.Template, codeGenerator, config).parse();

        this.templateDependencies.put(name, templateDependencies);

        ClassDefinition templateDefinition = new ClassDefinition(templateInfo.fullName, templateInfo);
        templateDefinition.setCode(codeGenerator.getCode(), codeGenerator.getBinaryTextParts());
        classDefinitions.add(templateDefinition);

        templateByClassName.put(templateDefinition.getName(), templateInfo);

        if (DEBUG) {
            System.out.println(templateDefinition.getCode());
        }
    }

    private void generateTemplateFromTag(String name, LinkedHashSet<ClassDefinition> classDefinitions) {
        LinkedHashSet<String> templateDependencies = new LinkedHashSet<>();

        ClassInfo templateInfo = generateTagOrLayout(TemplateType.Tag, name, classDefinitions, templateDependencies, null);

        this.templateDependencies.put(name, templateDependencies);

        templateByClassName.put(templateInfo.name, templateInfo);
    }

    private void generateTemplateFromLayout(String name, LinkedHashSet<ClassDefinition> classDefinitions) {
        LinkedHashSet<String> templateDependencies = new LinkedHashSet<>();

        ClassInfo templateInfo = generateTagOrLayout(TemplateType.Layout, name, classDefinitions, templateDependencies, null);

        this.templateDependencies.put(name, templateDependencies);

        templateByClassName.put(templateInfo.name, templateInfo);
    }

    public ClassInfo generateTagOrLayout(TemplateType type, String simpleName, String extension, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<String> templateDependencies, DebugInfo debugInfo) {
        String name = resolveTagOrLayoutName(type, simpleName, extension);
        try {
            return generateTagOrLayout(type, name, classDefinitions, templateDependencies, debugInfo);
        } catch (TemplateNotFoundException e) {
            String alternativeName = resolveTagOrLayoutName(type, simpleName, "jte".equals(extension) ? "kte" : "jte");

            if (codeResolver.exists(alternativeName)) {
                return generateTagOrLayout(type, alternativeName, classDefinitions, templateDependencies, debugInfo);
            } else {
                throw e;
            }
        }
    }

    private String resolveTagOrLayoutName(TemplateType type, String simpleName, String extension) {
        String directory = type == TemplateType.Layout ? Constants.LAYOUT_DIRECTORY : Constants.TAG_DIRECTORY;
        return directory + simpleName.replace('.', '/') + "." + extension;
    }

    public ClassInfo generateTagOrLayout(TemplateType type, String name, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<String> templateDependencies, DebugInfo debugInfo) {
        templateDependencies.add(name);
        ClassInfo classInfo = new ClassInfo(name, config.packageName);

        ClassDefinition classDefinition = new ClassDefinition(classInfo.fullName, classInfo);
        if (classDefinitions.contains(classDefinition)) {
            return classInfo;
        }

        String code = resolveCode(name, debugInfo);

        classDefinitions.add(classDefinition);

        CodeGenerator codeGenerator = createCodeGenerator(classInfo, classDefinitions, templateDependencies);
        new TemplateParser(code, type, codeGenerator, config).parse();

        classDefinition.setCode(codeGenerator.getCode(), codeGenerator.getBinaryTextParts());
        templateByClassName.put(classDefinition.getName(), classInfo);

        if (DEBUG) {
            System.out.println(classDefinition.getCode());
        }

        return classInfo;
    }

    private CodeGenerator createCodeGenerator(ClassInfo classInfo, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<String> templateDependencies) {
        if ("kte".equals(classInfo.extension)) {
            try {
                Class<?> compilerClass = Class.forName("gg.jte.compiler.kotlin.KotlinCodeGenerator");
                return (CodeGenerator)compilerClass.getConstructor(TemplateCompiler.class, TemplateConfig.class, ConcurrentHashMap.class, ClassInfo.class, LinkedHashSet.class, LinkedHashSet.class).newInstance(this, this.config, paramOrder, classInfo, classDefinitions, templateDependencies);
            } catch (Exception e) {
                throw new TemplateException("Failed to create kotlin generator. To handle .kte files, you need to add gg.jte:jte-kotlin to your project.", e);
            }
        } else {
            return new JavaCodeGenerator(this, this.config, paramOrder, classInfo, classDefinitions, templateDependencies);
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
        if (codeResolver.hasChanged(name)) {
            return true;
        }

        LinkedHashSet<String> dependencies = templateDependencies.get(name);
        if (dependencies == null) {
            return false;
        }

        for (String dependency : dependencies) {
            if (codeResolver.hasChanged(dependency)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<String> getTemplatesUsing(String name) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, LinkedHashSet<String>> dependencies : templateDependencies.entrySet()) {
            if (dependencies.getValue().contains(name)) {
                result.add(dependencies.getKey());
            }
        }

        return result;
    }
}
