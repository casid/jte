package gg.jte.gradle;

import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

public abstract class JteCompilerWorkerAction implements WorkAction<JteCompilerWorkerParameters> {
    @Override
    public void execute() {
        JteCompilerWorkerParameters parameters = getParameters();
        
        try {
            new JteGenerator(
                parameters.getSourceDirectory().getAsFile().get().toPath(),
                parameters.getTargetDirectory().getAsFile().get().toPath(),
                parameters.getContentType().get(),
                parameters.getPackageName().get(),
                parameters.getTrimControlStructures().get(),
                parameters.getHtmlTags().get(),
                parameters.getHtmlCommentsPreserved().get(),
                parameters.getBinaryStaticContent().get(),
                parameters.getTargetResourceDirectory().getAsFile().get().toPath(),
                parameters.getProjectNamespace().get(),
                parameters.getJteExtensions().get()
            ).generate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JTE templates", e);
        }
    }
}
