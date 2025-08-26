package gg.jte.gradle;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.List;

public abstract class GenerateJteTask extends JteTaskBase {
    private final WorkerExecutor workerExecutor;

    @Inject
    public GenerateJteTask(WorkerExecutor workerExecutor, ObjectFactory objectFactory) {
        super(JteStage.GENERATE, objectFactory);
        this.workerExecutor = workerExecutor;
        getOutputs().cacheIf(task -> true); // Enable caching based on outputs
        getProjectNamespace().convention(getProject().getGroup() + "/" + getProject().getName());
    }

    @Input
    public abstract Property<String> getProjectNamespace();

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getClasspath();

    @TaskAction
    public void execute() {
        // Use worker API with classloader isolation to avoid compiler symbol conflicts
        WorkQueue workQueue = workerExecutor.classLoaderIsolation(spec -> {
            // Include both application and compiler classpath in isolation
            spec.getClasspath().from(getClasspath());
        });

        workQueue.submit(GenerateJteWorker.class, params -> {
            params.getSourceDirectory().set(getSourceDirectory().get().toFile());
            params.getTargetDirectory().set(getTargetDirectory().get().toFile());
            params.getContentType().set(getContentType());
            params.getPackageName().set(getPackageName());
            params.getTrimControlStructures().set(getTrimControlStructures());
            params.getHtmlTags().set(toListProvider(getHtmlTags()));
            params.getHtmlCommentsPreserved().set(getHtmlCommentsPreserved());
            params.getBinaryStaticContent().set(getBinaryStaticContent());
            params.getTargetResourceDirectory().set(getTargetResourceDirectory().flatMap(path -> objectFactory.directoryProperty().fileValue(path.toFile())));
            params.getProjectNamespace().set(getProjectNamespace());
            params.getCompilerClasspath().from(getClasspath());
            getJteExtensions().get().forEach(e ->
                    params.getJteExtensions().put(e.getClassName().get(), e.getProperties().get())
            );
        });
    }

    @Override
    protected void wireExtension(JteExtension extension, Provider<Path> defaultTargetDirectory) {
        super.wireExtension(extension, defaultTargetDirectory);
        getProjectNamespace().set(extension.getProjectNamespace());
    }
}
