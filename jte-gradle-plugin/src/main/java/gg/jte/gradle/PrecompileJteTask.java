package gg.jte.gradle;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.nio.file.Path;

public abstract class PrecompileJteTask extends JteTaskBase {
    private final ConfigurableFileCollection compilePath;

    @Inject
    public PrecompileJteTask(WorkerExecutor workerExecutor, ObjectFactory objectFactory) {
        super(JteStage.PRECOMPILE, objectFactory);
        this.workerExecutor = workerExecutor;
        this.compilePath = objectFactory.fileCollection();
        getOutputs().cacheIf(task -> true); // Enable caching based on outputs
    }

    @Input
    @Optional
    public abstract Property<String> getHtmlPolicyClass();

    @Input
    @Optional
    public abstract Property<String[]> getCompileArgs();

    @Input
    @Optional
    public abstract Property<String[]> getKotlinCompileArgs();

    @InputFiles
    @CompileClasspath
    public ConfigurableFileCollection getCompilePath() {
        return compilePath;
    }

    public void setCompilePath(FileCollection compilePath) {
        getCompilePath().from(compilePath);
    }

    private final WorkerExecutor workerExecutor;

    @TaskAction
    public void execute() {
        getLogger().info("{} execute", getClass().getName());
        WorkQueue workQueue = workerExecutor.classLoaderIsolation(spec ->
                spec.getClasspath().from(getCompilePath())
        );

        workQueue.submit(PrecompileJteWorker.class, params -> {
            params.getSourceDirectory().set(getSourceDirectory().get().toFile());
            params.getTargetDirectory().set(getTargetDirectory().get().toFile());
            params.getContentType().set(getContentType());
            params.getPackageName().set(getPackageName());
            params.getTrimControlStructures().set(getTrimControlStructures());
            params.getHtmlTags().set(toListProvider(getHtmlTags()));
            params.getHtmlPolicyClass().set(getHtmlPolicyClass());
            params.getHtmlCommentsPreserved().set(getHtmlCommentsPreserved());
            params.getBinaryStaticContent().set(getBinaryStaticContent());
            params.getCompileArgs().set(toListProvider(getCompileArgs()));
            params.getKotlinCompileArgs().set(toListProvider(getKotlinCompileArgs()));
            params.getTargetResourceDirectory().set(getTargetResourceDirectory().flatMap(path -> objectFactory.directoryProperty().fileValue(path.toFile())));
            params.getCompilePath().from(getCompilePath());
        });
    }

    @Override
    protected void wireExtension(JteExtension extension, Provider<Path> defaultTargetDirectory) {
        super.wireExtension(extension, defaultTargetDirectory);
        getCompileArgs().set(extension.getCompileArgs());
        getKotlinCompileArgs().set(extension.getKotlinCompileArgs());
        getHtmlPolicyClass().set(extension.getHtmlPolicyClass());
        getCompilePath().from(extension.getCompilePath());
        wiring = false;
    }
}
