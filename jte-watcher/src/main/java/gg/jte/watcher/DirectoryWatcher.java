package gg.jte.watcher;

import com.sun.nio.file.SensitivityWatchEventModifier;
import gg.jte.TemplateEngine;
import gg.jte.compiler.IoUtils;
import gg.jte.resolve.DirectoryCodeResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.List;
import java.util.function.Consumer;

public class DirectoryWatcher {
    private final TemplateEngine templateEngine;
    private final Path root;

    private Thread reloadThread;

    public DirectoryWatcher(TemplateEngine templateEngine, DirectoryCodeResolver codeResolver) {
        this(templateEngine, codeResolver.getRoot());
    }

    public DirectoryWatcher(TemplateEngine templateEngine, Path root) {
        this.templateEngine = templateEngine;
        this.root = root;
    }

    public void start(Consumer<List<String>> onTemplatesChanged) {
        reloadThread = new Thread(() -> startBlocking(onTemplatesChanged));
        reloadThread.setName("jte-reloader");
        reloadThread.setDaemon(true);
        reloadThread.start();
    }

    public void stop() {
        reloadThread.interrupt();
        reloadThread = null;
    }

    public void startBlocking(Consumer<List<String>> onTemplatesChanged) {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

            Files.walk(root).filter(Files::isDirectory).forEach(p -> {
                try {
                    p.register(watchService, new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_MODIFY}, SensitivityWatchEventModifier.HIGH);
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to register watch service for hot reload!", e);
                }
            });

            WatchKey watchKey;
            while ((watchKey = watchService.take()) != null) {
                try {
                    List<WatchEvent<?>> events = watchKey.pollEvents();
                    for (WatchEvent<?> event : events) {
                        String eventContext = event.context().toString();
                        if (!IoUtils.isTemplateFile(eventContext)) {
                            continue;
                        }

                        Path file = root.relativize((Path) watchKey.watchable()).resolve(eventContext);

                        Path absoluteFile = root.resolve(file);
                        if (absoluteFile.toFile().length() <= 0) {
                            continue;
                        }

                        String name = file.toString().replace('\\', '/');

                        List<String> changedTemplates = templateEngine.getTemplatesUsing(name);
                        if (onTemplatesChanged != null) {
                            onTemplatesChanged.accept(changedTemplates);
                        }
                    }
                } finally {
                    watchKey.reset();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to watch page content", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
