package gg.jte.resolve;

import gg.jte.CodeResolver;
import gg.jte.compiler.IoUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectoryCodeResolver implements CodeResolver {
    private final Path root;
    private final ConcurrentMap<String, Long> modificationTimes = new ConcurrentHashMap<>();

    public DirectoryCodeResolver(Path root) {
        this.root = root;
    }

    @Override
    public String resolve(String name) {
        try {
            Path file = root.resolve(name);
            byte[] bytes = Files.readAllBytes(file);
            modificationTimes.put(name, getLastModified(file));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean exists(String name) {
        Path file = root.resolve(name);
        return Files.exists(file);
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
        try (Stream<Path> stream = Files.walk(root, FileVisitOption.FOLLOW_LINKS)) {
            return stream
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> root.relativize(p).toString().replace('\\', '/'))
                    .filter(IoUtils::isTemplateFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to resolve all templates in " + root, e);
        }
    }

    public Path getRoot() {
        return root;
    }
}
