package org.jusecase.jte.benchmark;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jusecase.jte.TemplateEngine;
import org.jusecase.jte.output.Utf8ArrayOutput;
import org.jusecase.jte.resolve.DirectoryCodeResolver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleWebServer implements HttpHandler {
    public static void main(String[] args) throws IOException {
        new SimpleWebServer().start();
    }

    private final DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Path.of("src", "test", "resources", "benchmark"));
    private final TemplateEngine templateEngine = TemplateEngine.create(codeResolver);
    private final AtomicInteger visits = new AtomicInteger();

    private void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new SimpleWebServer());
        server.start();

        System.out.println("Server started.");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Utf8ArrayOutput output = new Utf8ArrayOutput();
        templateEngine.render("welcome.jte", new WelcomePage(visits.incrementAndGet()), output);

        exchange.sendResponseHeaders(200, output.getContentLength());
        try (OutputStream os = new BufferedOutputStream(exchange.getResponseBody())) {
            output.writeTo(os);
        }
    }
}
