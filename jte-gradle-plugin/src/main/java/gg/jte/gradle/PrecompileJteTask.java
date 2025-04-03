package gg.jte.gradle;

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;

public class PrecompileJteTask extends JteTaskBase {
    @Inject
    public PrecompileJteTask(JteExtension extension, WorkerExecutor workerExecutor) {
        super(extension, JteStage.PRECOMPILE, workerExecutor);
    }

    @InputFiles
    @CompileClasspath
    public FileCollection getCompilePath() {
        return extension.getCompilePath();
    }

    @Input
    @Optional
    public String getHtmlPolicyClass() {
        return extension.getHtmlPolicyClass().getOrNull();
    }

    @Input
    @Optional
    public String[] getCompileArgs() {
        return extension.getCompileArgs().getOrNull();
    }

    @Input
    @Optional
    public String[] getKotlinCompileArgs() {
        return extension.getKotlinCompileArgs().getOrNull();
    }

    @TaskAction
    public void execute() {
        WorkQueue workQueue = workerExecutor.classLoaderIsolation(spec ->
                spec.getClasspath().from(getCompilePath())
        );

        workQueue.submit(PrecompileJteWorker.class, params -> {
            configureWorkerParams(params);
            params.getHtmlPolicyClass().set(getHtmlPolicyClass());
            params.getCompileArgs().set(getCompileArgs());
            params.getKotlinCompileArgs().set(getKotlinCompileArgs());
            params.getCompilerClasspath().from(getCompilePath());
        });
    }
}
