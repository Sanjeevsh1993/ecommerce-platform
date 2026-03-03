package com.ecommerce.gateway.handler;

// STRANGLER FIG - Phase: 5 - Domain: Gateway / Cross-Cutting
// BC References: BC-043 (standard error envelope), BC-044 (do NOT leak internal messages)

import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.util.CorrelationIdUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@Order(-1)   // Must run before DefaultErrorWebExceptionHandler
@RequiredArgsConstructor
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        String correlationId = exchange.getRequest().getHeaders()
                .getFirst(AppConstants.CORRELATION_ID_HEADER);
        String cid = CorrelationIdUtils.resolveOrGenerate(correlationId);

        HttpStatus status;
        String code;
        String message;

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            code = status.name().replace(" ", "_");
            // BC-044: only expose message for 4xx; suppress internal details for 5xx
            message = status.is4xxClientError() ? rse.getReason() : "Request processing failed";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            code = "INTERNAL_SERVER_ERROR";
            // BC-044: never expose internal exception message to the client
            message = "An unexpected error occurred";
            log.error("Unhandled gateway error — correlationId={}", cid, ex);
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("timestamp", Instant.now().toString());
        body.put("correlationId", cid);
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message != null ? message : status.getReasonPhrase());
        body.put("error", error);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            var buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response", e);
            return response.setComplete();
        }
    }
}
