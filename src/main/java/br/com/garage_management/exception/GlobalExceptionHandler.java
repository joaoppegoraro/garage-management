package br.com.garage_management.exception;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Objects;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = { BusinessException.class })
    protected ResponseEntity<Object> handleConflict(BusinessException ex, WebRequest request) {
        HttpHeaders responseHeaders = new HttpHeaders();
        return ResponseEntity.status(ex.getHttpStatusCode()).headers(responseHeaders).body(ex.getOnlyBody());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleJsonParseError(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();

        BusinessException ex = BusinessException.builder()
                .httpStatusCode(HttpStatus.BAD_REQUEST)
                .message("Erro ao processar JSON de entrada")
                .description(cause instanceof InvalidFormatException invalid
                        ? "Valor invalido para o campo: " + invalid.getPath().stream()
                        .map(Reference::getFieldName)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse("campo desconhecido")
                        + ", " + "valor recebido: " + invalid.getValue()
                        : Optional.ofNullable(e.getMessage()).orElse(e.toString()))
                .build();

        HttpHeaders responseHeaders = new HttpHeaders();

        return ResponseEntity.status(ex.getHttpStatusCode())
                .headers(responseHeaders)
                .body(ex.getOnlyBody());
    }
}
