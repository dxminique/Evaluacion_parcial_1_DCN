package com.pedidos360.productos.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(RecursoNoEncontradoException.class)
    ResponseEntity<Map<String, String>> notFound(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).orElse("Solicitud inválida");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
