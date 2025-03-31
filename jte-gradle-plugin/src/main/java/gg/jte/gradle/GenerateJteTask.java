package gg.jte.gradle;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.nio.file.Path;

public abstract class GenerateJteTask extends JteTaskBase {
    private final WorkerExecutor workerExecutor;

    @Inject
    public GenerateJteTask(JteExtension extension, WorkerExecutor workerExecutor) {
        super(extension, JteStage.GENERATE);
        this.workerExecutor = workerExecutor;
        getOutputs().cacheIf(task -> true); // Enable caching based on outputs
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

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getClasspath();

    @TaskAction
    public void execute() {
        // Use worker API with classloader isolation to avoid compiler symbol conflicts
        WorkQueue workQueue = workerExecutor.classLoaderIsolation(spec -> {
            // Include both application and compiler classpath in isolation
            spec.getClasspath().from(getClasspath());
            spec.getClasspath().from(extension.getCompilePath());
        });

        workQueue.submit(GenerateJteWorker.class, params -> {
            params.getSourceDirectory().fileValue(getSourceDirectory().toFile());
            params.getTargetDirectory().fileValue(getTargetDirectory().toFile());
            params.getContentType().value(getContentType());
            params.getPackageName().value(getPackageName());
            params.getTrimControlStructures().value(getTrimControlStructures());
            params.getHtmlTags().value(getHtmlTags());
            params.getHtmlCommentsPreserved().value(getHtmlCommentsPreserved());
            params.getBinaryStaticContent().value(getBinaryStaticContent());
            params.getTargetResourceDirectory().fileValue(getTargetResourceDirectory().toFile());
            params.getProjectNamespace().value(extension.getProjectNamespace());
            params.getCompilerClasspath().from(extension.getCompilePath());
            extension.getJteExtensions().get().forEach(e -> 
                params.getJteExtensions().put(e.getClassName().get(), e.getProperties().get())
            );
        });
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
        params.getProjectNamespace().value(extension.getProjectNamespace());
        extension.getJteExtensions().get().forEach(e -> params.getJteExtensions().put(e.getClassName().get(), e.getProperties().get()));
    }

}
