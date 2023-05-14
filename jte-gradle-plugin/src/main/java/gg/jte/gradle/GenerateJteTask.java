package gg.jte.gradle;

import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public abstract class GenerateJteTask extends JteTaskBase {

    private final WorkerExecutor workerExecutor;

    @Inject
    public GenerateJteTask(JteExtension extension, WorkerExecutor workerExecutor) {
        super(extension, JteStage.GENERATE);
        this.workerExecutor = workerExecutor;
    }

    @Override
    public Path getTargetDirectory()
    {
        if (!extension.getStage().isPresent())
        {
            extension.getStage().set(JteStage.GENERATE);
        }
        return super.getTargetDirectory();
    }

    @Input
    public boolean getGenerateNativeImageResources() {
        return extension.getGenerateNativeImageResources().getOrElse(false);
    }

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getClasspath();

    public void setGenerateNativeImageResources(boolean value) {
        extension.getGenerateNativeImageResources().set(value);
        setterCalled();
    }

    @TaskAction
    public void execute() {
        // use worker api so the classpath can be modified
        WorkQueue workQueue = workerExecutor.classLoaderIsolation(spec -> spec.getClasspath().from(getClasspath()));

        workQueue.submit(GenerateJteWorker.class, this::buildParams);
        workQueue.await();
    }

    private void buildParams(GenerateJteParams params) {
        params.getSourceDirectory().fileValue(getSourceDirectory().toFile());
        params.getTargetDirectory().fileValue(getTargetDirectory().toFile());
        params.getContentType().value(getContentType());
        params.getPackageName().value(getPackageName());
        params.getTrimControlStructures().value(getTrimControlStructures());
        params.getHtmlTags().value(getHtmlTags());
        params.getHtmlCommentsPreserved().value(getHtmlCommentsPreserved());
        params.getBinaryStaticContent().value(getBinaryStaticContent());
        params.getTargetResourceDirectory().fileValue(getTargetResourceDirectory().toFile());
        params.getGenerateNativeImageResources().value(getGenerateNativeImageResources());
        params.getProjectNamespace().value(extension.getProjectNamespace());
        extension.getJteExtensions().get().forEach(e -> params.getJteExtensions().put(e.getClassName().get(), e.getProperties().get()));
    }

}
