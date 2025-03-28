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
import java.util.stream.Collectors;

@CacheableTask
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

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getClasspath();

    @TaskAction
    public void execute() {
        WorkQueue workQueue = workerExecutor.classLoaderIsolation(spec -> {
            spec.getClasspath().from(getClasspath());
            spec.getClasspath().from(getProject().getConfigurations().getByName("jteCompilerClasspath"));
        });

        workQueue.submit(JteCompilerWorkerAction.class, parameters -> {
            parameters.getSourceDirectory().fileValue(getSourceDirectory().toFile());
            parameters.getTargetDirectory().fileValue(getTargetDirectory().toFile());
            parameters.getContentType().set(getContentType());
            parameters.getPackageName().set(getPackageName());
            parameters.getTrimControlStructures().set(getTrimControlStructures());
            parameters.getHtmlTags().set(getHtmlTags());
            parameters.getHtmlCommentsPreserved().set(getHtmlCommentsPreserved());
            parameters.getBinaryStaticContent().set(getBinaryStaticContent());
            parameters.getTargetResourceDirectory().fileValue(getTargetResourceDirectory().toFile());
            parameters.getProjectNamespace().set(extension.getProjectNamespace());
            parameters.getJteExtensions().set(getProject().provider(() -> 
                extension.getJteExtensions().get().stream()
                    .collect(Collectors.toMap(
                        e -> e.getClassName().get(),
                        e -> e.getProperties().get()
                    ))
            ));
        });
        
        workQueue.await();
    }

}
