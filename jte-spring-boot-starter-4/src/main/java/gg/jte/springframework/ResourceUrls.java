package gg.jte.springframework;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.ResourceUrlProviderExposingInterceptor;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

/**
 * Resolves static resource URLs through Spring MVC's configured resource chain.
 * A new instance is exposed as {@code resources} for each servlet view render.
 */
public final class ResourceUrls {

    private final @Nullable ResourceUrlProvider provider;
    private final String prefix;

    public ResourceUrls(HttpServletRequest request) {
        Object attribute = request.getAttribute(ResourceUrlProviderExposingInterceptor.RESOURCE_URL_PROVIDER_ATTR);
        this.provider = attribute instanceof ResourceUrlProvider resourceUrlProvider ? resourceUrlProvider : null;
        String servletPath = ServletRequestPathUtils.getServletPathPrefix(request);
        this.prefix = request.getContextPath() + (servletPath != null
                ? UriUtils.encodePath(servletPath, StandardCharsets.UTF_8) : "");
    }

    /**
     * Resolves a path beginning with {@code /}, relative to the MVC resource
     * mappings or already including the current context and servlet prefix.
     * Resolved URLs include that prefix and retain their query and fragment.
     * Relative, external and unresolved URLs are returned unchanged.
     *
     * @param url the resource URL
     * @return the public resource URL, or the original URL if unresolved
     */
    public String url(String url) {
        if (provider == null || !url.startsWith("/") || url.startsWith("//")) {
            return url;
        }

        int end = url.length();
        int query = url.indexOf('?');
        if (query >= 0) {
            end = query;
        }
        int fragment = url.indexOf('#');
        if (fragment >= 0) {
            end = Math.min(end, fragment);
        }

        String path = url.substring(0, end);
        if (!prefix.isEmpty() && (path.equals(prefix) || path.startsWith(prefix + "/"))) {
            path = path.substring(prefix.length());
        }
        String resolved = provider.getForLookupPath(path);
        return resolved != null ? prefix + resolved + url.substring(end) : url;
    }
}
