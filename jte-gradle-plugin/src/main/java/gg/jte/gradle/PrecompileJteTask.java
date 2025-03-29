package gg.jte.gradle;

import gg.jte.html.HtmlPolicy;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class PrecompileJteTask extends JteTaskBase {
    @Inject
    public PrecompileJteTask(JteExtension extension, WorkerExecutor workerExecutor) {
        super(extension, JteStage.PRECOMPILE);
        this.workerExecutor = workerExecutor;
        getOutputs().cacheIf(task -> true); // Enable caching based on outputs
    }

    @InputFiles
    @CompileClasspath
    public FileCollection getCompilePath() {
        return extension.getCompilePath();
    }

    public void setCompilePath(FileCollection compilePath) {
        extension.getCompilePath().from(compilePath);
        setterCalled();
    }

    @Input
    @Optional
    public String getHtmlPolicyClass() {
        return extension.getHtmlPolicyClass().getOrNull();
    }

    public void setHtmlPolicyClass(String htmlPolicyClass) {
        extension.getHtmlPolicyClass().set(htmlPolicyClass);
        setterCalled();
    }

    @Input
    @Optional
    public String[] getCompileArgs() {
        return extension.getCompileArgs().getOrNull();
    }

    public void setCompileArgs(String[] compileArgs) {
        extension.getCompileArgs().set(compileArgs);
        setterCalled();
    }

    @Input
    @Optional
    public String[] getKotlinCompileArgs() {
        return extension.getKotlinCompileArgs().getOrNull();
    }

    public void setKotlinCompileArgs(String[] compileArgs) {
        extension.getKotlinCompileArgs().set(compileArgs);
        setterCalled();
    }

    private final WorkerExecutor workerExecutor;

    @TaskAction
    public void execute() {
        WorkQueue workQueue = workerExecutor.classLoaderIsolation(spec ->
                spec.getClasspath().from(getCompilePath())
        );

        workQueue.submit(PrecompileJteWorker.class, params -> {
            params.getSourceDirectory().set(getSourceDirectory().toFile());
            params.getTargetDirectory().set(getTargetDirectory().toFile());
            params.getContentType().set(getContentType());
            params.getPackageName().set(getPackageName());
            params.getTrimControlStructures().set(getTrimControlStructures());
            params.getHtmlTags().set(getHtmlTags());
            params.getHtmlPolicyClass().set(getHtmlPolicyClass());
            params.getHtmlCommentsPreserved().set(getHtmlCommentsPreserved());
            params.getBinaryStaticContent().set(getBinaryStaticContent());
            params.getCompileArgs().set(getCompileArgs());
            params.getKotlinCompileArgs().set(getKotlinCompileArgs());
            params.getTargetResourceDirectory().set(getTargetResourceDirectory().toFile());
            params.getCompilePath().from(getCompilePath());
        });
    }

    private HtmlPolicy createHtmlPolicy(String htmlPolicyClass) {
        try {
            URLClassLoader projectClassLoader = createProjectClassLoader();
            Class<?> clazz = projectClassLoader.loadClass(htmlPolicyClass);
            return (HtmlPolicy) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate custom HtmlPolicy " + htmlPolicyClass, e);
        }
    }

    private URLClassLoader createProjectClassLoader() throws IOException {
        List<File> files = new ArrayList<>(getCompilePath().getFiles());

        URL[] runtimeUrls = new URL[files.size()];
        for (int i = 0; i < files.size(); i++) {
            File element = files.get(i);
            runtimeUrls[i] = element.toURI().toURL();
        }
        return new URLClassLoader(runtimeUrls, Thread.currentThread().getContextClassLoader());
    }
}
