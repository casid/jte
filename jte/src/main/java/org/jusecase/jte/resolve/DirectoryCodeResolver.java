package org.jusecase.jte.resolve;

import com.sun.nio.file.SensitivityWatchEventModifier;
import org.jusecase.jte.CodeResolver;
import org.jusecase.jte.TemplateEngine;
import org.jusecase.jte.internal.Constants;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectoryCodeResolver implements CodeResolver {
    private final Path root;
    private final ConcurrentMap<String, Long> modificationTimes = new ConcurrentHashMap<>();
    private Thread reloadThread;

    public DirectoryCodeResolver(Path root) {
        this.root = root;
    }

    @Override
    public String resolve(String name) {
        try {
            Path file = root.resolve(name);
            modificationTimes.put(name, getLastModified(file));
            return Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean hasChanged(String name) {
        Long lastResolveTime = modificationTimes.get(name);
        if (lastResolveTime == null) {
            return true;
        }

        long lastModified = getLastModified(root.resolve(name));

        return lastModified != lastResolveTime;
    }

    private long getLastModified(Path file) {
        return file.toFile().lastModified();
    }

    @Override
    public List<String> resolveAllTemplateNames() {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> root.relativize(p).toString().replace('\\', '/'))
                    .filter(s -> !s.startsWith(Constants.TAG_DIRECTORY) && !s.startsWith(Constants.LAYOUT_DIRECTORY))
                    .filter(s -> s.endsWith(".jte"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to resolve all templates in " + root, e);
        }
    }

    public void startTemplateFilesListener(TemplateEngine templateEngine, Consumer<List<String>> onTemplatesChanged) {
        reloadThread = new Thread(() -> startTemplateFilesListenerBlocking(templateEngine, onTemplatesChanged));
        reloadThread.setName("jte-reloader");
        reloadThread.setDaemon(true);
        reloadThread.start();
    }

    public void stopTemplateFilesListener() {
        reloadThread.interrupt();
        reloadThread = null;
    }

    public void startTemplateFilesListenerBlocking(TemplateEngine templateEngine, Consumer<List<String>> onTemplatesChanged) {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

            Files.walk(root).filter(p -> Files.isDirectory(p)).forEach(p -> {
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
                        if (!eventContext.endsWith(".jte")) {
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
