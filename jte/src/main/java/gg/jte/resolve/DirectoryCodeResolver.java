package gg.jte.resolve;

import gg.jte.CodeResolver;
import gg.jte.compiler.IoUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Resolves template code within a given root directory.
 */
public class DirectoryCodeResolver implements CodeResolver {
    private final Path root;

    public DirectoryCodeResolver(Path root) {
        this.root = root;
    }

    @Override
    public String resolve(String name) {
        try {
            Path file = root.resolve(name);
            byte[] bytes = Files.readAllBytes(file);
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
    public long getLastModified(String name) {
        return getLastModified(root.resolve(name));
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
