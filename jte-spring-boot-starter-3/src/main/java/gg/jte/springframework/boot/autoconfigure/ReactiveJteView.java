package gg.jte.springframework.boot.autoconfigure;

import gg.jte.*;
import gg.jte.output.*;
import org.springframework.core.io.buffer.*;
import org.springframework.http.*;
import org.springframework.lang.*;
import org.springframework.web.reactive.result.view.*;
import org.springframework.web.server.*;
import reactor.core.publisher.*;

import java.io.*;
import java.util.*;

public class ReactiveJteView extends AbstractUrlBasedView {
    private final TemplateEngine templateEngine;

    public ReactiveJteView(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public boolean checkResourceExists(Locale locale) {
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

                    DataBuffer dataBuffer = exchange.getResponse().bufferFactory().allocateBuffer();
                    try {
                        output.writeTo(dataBuffer.asOutputStream());
                        return dataBuffer;
                    } catch (IOException ex){
                        DataBufferUtils.release(dataBuffer);
                        String message = "Could not load jte template for URL [" + getUrl() + "]";
                        throw new IllegalStateException(message, ex);
                    }catch (Throwable ex) {
                        DataBufferUtils.release(dataBuffer);
                        throw ex;
                    }}).doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release));

    }

}
