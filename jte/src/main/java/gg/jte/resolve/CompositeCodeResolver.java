package gg.jte.resolve;


import gg.jte.CodeResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Resolves template code from multiple other resolvers
 */
public class CompositeCodeResolver implements CodeResolver {

    private final List<CodeResolver> codeResolvers;

    public CompositeCodeResolver(List<CodeResolver> codeResolvers) {
        this.codeResolvers = codeResolvers;
    }

    @Override
    public String resolve(String name) {
        var content = "";
        for (CodeResolver codeResolver : this.codeResolvers) {
            try {
                String resolve = codeResolver.resolve(name);
                if(Objects.nonNull(resolve)){
                    content = resolve;
                }
            } catch (Exception ex) {
                // ignore
            }
        }
        if(!content.isEmpty())
            return content;
        throw new UncheckedIOException(new IOException("Could not find template " + name));
    }

    @Override
    public long getLastModified(String name) {
        long lastModified = 0;
        for (CodeResolver codeResolver : this.codeResolvers) {
            try {
                lastModified = codeResolver.getLastModified(name);
                if(lastModified > 0)
                    return lastModified;
            } catch (Exception ex) {
                lastModified = 0;
            }
        }
        return lastModified;
    }

    @Override
    public List<String> resolveAllTemplateNames() {
        List<String> allTemplateNames = new ArrayList<>();
        for (CodeResolver codeResolver : this.codeResolvers) {
            try {
                allTemplateNames.addAll(codeResolver.resolveAllTemplateNames());
            } catch (Exception ex) {
                // ignore
            }
        }
        return allTemplateNames;
    }
}
