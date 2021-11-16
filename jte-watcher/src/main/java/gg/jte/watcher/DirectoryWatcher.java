package gg.jte.watcher;

import gg.jte.TemplateEngine;
import gg.jte.compiler.IoUtils;
import gg.jte.resolve.DirectoryCodeResolver;
import io.methvin.watcher.DirectoryChangeEvent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class DirectoryWatcher {
    private final TemplateEngine templateEngine;
    private final Path root;

    private io.methvin.watcher.DirectoryWatcher watcher;

    public DirectoryWatcher(TemplateEngine templateEngine, DirectoryCodeResolver codeResolver) {
        this(templateEngine, codeResolver.getRoot());
    }

    public DirectoryWatcher(TemplateEngine templateEngine, Path root) {
        this.templateEngine = templateEngine;
        this.root = root;
    }

    public void start(Consumer<List<String>> onTemplatesChanged) {
        watcher = createWatcher(onTemplatesChanged);
        watcher.watchAsync();
    }

    public void stop() {
        try {
            watcher.close();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to close directory watcher at " + root, e);
        }
    }

    public void startBlocking(Consumer<List<String>> onTemplatesChanged) {
        watcher = createWatcher(onTemplatesChanged);
        watcher.watch();
    }

    private io.methvin.watcher.DirectoryWatcher createWatcher(Consumer<List<String>> onTemplatesChanged) {
        try {
            return io.methvin.watcher.DirectoryWatcher.builder()
                    .path(root)
                    .listener(event -> {
                        if (!isRelevantEventType(event)) {
                            return;
                        }

                        Path absolutePath = event.path();
                        Path relativePath = root.relativize(absolutePath);

                        if (absolutePath.toFile().length() <= 0) {
                            return;
                        }

                        String name = relativePath.toString().replace('\\', '/');
                        if (!IoUtils.isTemplateFile(name)) {
                            return;
                        }

                        List<String> changedTemplates = templateEngine.getTemplatesUsing(name);
                        if (onTemplatesChanged != null) {
                            onTemplatesChanged.accept(changedTemplates);
                        }
                    })
                    .build();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to initialize watcher for directory " + root, e);
        }
    }

    private boolean isRelevantEventType(DirectoryChangeEvent event) {
        return event.eventType() == DirectoryChangeEvent.EventType.MODIFY || event.eventType() == DirectoryChangeEvent.EventType.CREATE;
    }
}
