package org.jusecase.jte.resolve;

import org.jusecase.jte.CodeResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirectoryCodeResolver implements CodeResolver {
    private final Path root;

    public DirectoryCodeResolver(Path root) {
        this.root = root;
    }

    @Override
    public String resolve(String name) {
        try {
            return Files.readString(root.resolve(name));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
