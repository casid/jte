package org.jusecase.jte.benchmark;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jusecase.jte.ContentType;
import org.jusecase.jte.TemplateEngine;
import org.jusecase.jte.output.StringOutput;
import org.jusecase.jte.output.StringOutputPool;
import org.jusecase.jte.resolve.DirectoryCodeResolver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleWebServer implements HttpHandler {
    public static void main(String[] args) throws IOException {
        new SimpleWebServer().start();
    }

    private final DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Path.of("jte", "src", "test", "resources", "benchmark"));
    private final TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
    private final StringOutputPool stringOutputPool = new StringOutputPool();
    private final AtomicInteger visits = new AtomicInteger();

    private void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new SimpleWebServer());
        server.start();

        System.out.println("Server started.");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        StringOutput output = stringOutputPool.get();
        templateEngine.render("welcome.jte", new WelcomePage(visits.incrementAndGet()), output);

        byte[] bytes = output.toString().getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = new BufferedOutputStream(exchange.getResponseBody())) {
            os.write(bytes);
        }
    }
}
