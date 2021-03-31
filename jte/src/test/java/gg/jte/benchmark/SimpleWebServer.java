package gg.jte.benchmark;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.Utf8ByteOutput;
import gg.jte.resolve.DirectoryCodeResolver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleWebServer implements HttpHandler {
    public static void main(String[] args) throws IOException {
        new SimpleWebServer().start();
    }

    private final DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Paths.get("jte", "src", "test", "resources", "benchmark"));
    private final TemplateEngine templateEngine = createTemplateEngine();
    private final AtomicInteger visits = new AtomicInteger();

    private TemplateEngine createTemplateEngine() {
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        templateEngine.setBinaryStaticContent(true);
        return templateEngine;
    }

    private void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", this);
        server.start();

        System.out.println("Server started.");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Utf8ByteOutput output = new Utf8ByteOutput();
        templateEngine.render("welcome.jte", new WelcomePage(visits.incrementAndGet()), output);

        exchange.sendResponseHeaders(200, output.getContentLength());
        try (OutputStream os = new BufferedOutputStream(exchange.getResponseBody())) {
            output.writeTo(os);
        }
    }
}
