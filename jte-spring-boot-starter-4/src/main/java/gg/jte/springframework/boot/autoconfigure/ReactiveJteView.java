package gg.jte.springframework.boot.autoconfigure;

import gg.jte.TemplateEngine;
import gg.jte.output.Utf8ByteOutput;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.result.view.AbstractUrlBasedView;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class ReactiveJteView extends AbstractUrlBasedView {

    private final TemplateEngine templateEngine;

    public ReactiveJteView(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public boolean checkResourceExists(@NonNull Locale locale) {
        return templateEngine.hasTemplate(this.getUrl());
    }

    @Override
    @NonNull
    protected Mono<Void> renderInternal(@NonNull Map<String, Object> renderAttributes, MediaType contentType, ServerWebExchange exchange) {
        return exchange.getResponse().writeWith(Mono
                .fromCallable(() -> {
                    String url = this.getUrl();
                    Utf8ByteOutput output = new Utf8ByteOutput();
                    templateEngine.render(url, renderAttributes, output);

                    DataBuffer dataBuffer = exchange.getResponse().bufferFactory().allocateBuffer(DefaultDataBufferFactory.DEFAULT_INITIAL_CAPACITY);
                    try {
                        output.writeTo(dataBuffer.asOutputStream());
                        return dataBuffer;
                    } catch (IOException ex) {
                        DataBufferUtils.release(dataBuffer);
                        String message = "Could not load jte template for URL [" + getUrl() + "]";
                        throw new IllegalStateException(message, ex);
                    } catch (Throwable ex) {
                        DataBufferUtils.release(dataBuffer);
                        throw ex;
                    }
                }).doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release));
    }
}
