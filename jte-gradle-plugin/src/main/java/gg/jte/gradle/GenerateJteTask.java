package gg.jte.gradle;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.nio.file.Path;

public abstract class GenerateJteTask extends JteTaskBase {

    @Inject
    public GenerateJteTask(JteExtension extension, WorkerExecutor workerExecutor) {
        super(extension, JteStage.GENERATE, workerExecutor);
    }

    @Override
    public Path getTargetDirectory() {
        if (!extension.getStage().isPresent()) {
            extension.getStage().set(JteStage.GENERATE);
        }
        return super.getTargetDirectory();
    }

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getClasspath();

    @TaskAction
    public void execute() {
        WorkQueue workQueue = workerExecutor.classLoaderIsolation(spec -> {
            spec.getClasspath().from(getClasspath());
            spec.getClasspath().from(extension.getCompilePath());
        });

        workQueue.submit(GenerateJteWorker.class, params -> {
            configureWorkerParams(params);
            params.getProjectNamespace().set(extension.getProjectNamespace());
            params.getCompilerClasspath().from(extension.getCompilePath());
            extension.getJteExtensions().get().forEach(e ->
                    params.getJteExtensions().put(e.getClassName().get(), e.getProperties().get())
            );
        });
    }
}
