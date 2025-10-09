package gg.jte.gradle;

import gg.jte.TemplateEngine;
import gg.jte.html.HtmlPolicy;
import gg.jte.resolve.DirectoryCodeResolver;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.workers.WorkAction;

import java.io.File;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class PrecompileJteWorker implements WorkAction<PrecompileJteParams> {
    private static final Logger logger = Logging.getLogger(PrecompileJteWorker.class);

    @Override
    public void execute() {
        // Prevent Kotlin compiler to leak file handles
        System.setProperty("kotlin.environment.keepalive", "false");

        long start = System.nanoTime();
        PrecompileJteParams params = getParameters();

        // Load compiler in isolated classloader
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader compilerClassLoader = Utils.createCompilerClassLoader(params.getCompilePath())) {
            Thread.currentThread().setContextClassLoader(compilerClassLoader);

            Path sourceDirectory = Utils.toPathOrNull(params.getSourceDirectory());
            Path targetDirectory = Utils.toPathOrNull(params.getTargetDirectory());

            logger.info("Precompiling jte templates found in {}", sourceDirectory);

            TemplateEngine templateEngine = TemplateEngine.create(
                    new DirectoryCodeResolver(sourceDirectory),
                    targetDirectory,
                    params.getContentType().get(),
                    null,
                    params.getPackageName().get());

            templateEngine.setTrimControlStructures(Boolean.TRUE.equals(params.getTrimControlStructures().getOrNull()));
            templateEngine.setHtmlTags(Utils.toStringArrayOrNull(params.getHtmlTags()));

            String htmlPolicyClass = params.getHtmlPolicyClass().getOrNull();

            if (htmlPolicyClass != null) {
                templateEngine.setHtmlPolicy(createHtmlPolicy(htmlPolicyClass, params.getCompilePath()));
            }

            templateEngine.setHtmlCommentsPreserved(Boolean.TRUE.equals(params.getHtmlCommentsPreserved().getOrNull()));
            templateEngine.setBinaryStaticContent(Boolean.TRUE.equals(params.getBinaryStaticContent().getOrNull()));
            templateEngine.setCompileArgs(Utils.toStringArrayOrNull(params.getCompileArgs()));
            templateEngine.setKotlinCompileArgs(Utils.toStringArrayOrNull(params.getKotlinCompileArgs()));

            Path targetResourceDir = Utils.toPathOrNull(params.getTargetResourceDirectory());
            templateEngine.setTargetResourceDirectory(targetResourceDir);

            int amount;
            try {
                templateEngine.cleanAll();
                List<String> compilePathFiles = params.getCompilePath().getFiles().stream()
                        .map(File::getAbsolutePath)
                        .collect(Collectors.toList());
                amount = templateEngine.precompileAll(compilePathFiles).size();
            } catch (Exception e) {
                logger.error("Failed to precompile templates.", e);
                throw e;
            }

            long end = System.nanoTime();
            long duration = TimeUnit.NANOSECONDS.toSeconds(end - start);
            logger.info("Successfully precompiled {} jte file{} in {}s to {}", amount, amount == 1 ? "" : "s", duration, targetDirectory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute template precompilation", e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    private HtmlPolicy createHtmlPolicy(String htmlPolicyClass, ConfigurableFileCollection compilePath) {
        try (URLClassLoader compilerClassLoader = Utils.createCompilerClassLoader(compilePath)) {
            Class<?> clazz = compilerClassLoader.loadClass(htmlPolicyClass);
            return (HtmlPolicy) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate custom HtmlPolicy " + htmlPolicyClass, e);
        }
    }

}
