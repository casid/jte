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
    private final WorkerExecutor workerExecutor;

    @Inject
    public GenerateJteTask(JteExtension extension, WorkerExecutor workerExecutor) {
        super(extension, JteStage.GENERATE);
        this.workerExecutor = workerExecutor;
        getOutputs().cacheIf(task -> true); // Enable caching based on outputs
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
        var builds = extension.getBuilds();
        if (builds.isEmpty()) {
            executeBuild(extension);
        } else {
            builds.forEach(this::executeBuild);
        }
    }

    private void executeBuild(JteBuildSpec buildSpec) {
        WorkQueue workQueue = workerExecutor.classLoaderIsolation(spec -> {
            spec.getClasspath().from(getClasspath());
            spec.getClasspath().from(buildSpec.getCompilePath());
        });

        workQueue.submit(GenerateJteWorker.class, buildSpec::toParams);
    }
}
