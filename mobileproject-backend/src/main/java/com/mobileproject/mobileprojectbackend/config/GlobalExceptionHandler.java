package com.mobileproject.mobileprojectbackend.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Xử lý ngoại lệ toàn cục (Global Exception Handler) cho tất cả các controller.
 *
 * <p>Kế thừa {@link ResponseEntityExceptionHandler} để xử lý các exception chuẩn của Spring MVC,
 * đồng thời bổ sung handler cho các exception chưa được xử lý.</p>
 *
 * <p>Các loại exception được xử lý:</p>
 * <ul>
 *   <li><b>HttpMessageNotReadableException</b> - Request body JSON sai định dạng → HTTP 400</li>
 *   <li><b>MethodArgumentNotValidException</b> - Validation {@code @Valid} thất bại → HTTP 400,
 *       trả về danh sách lỗi theo từng field</li>
 *   <li><b>Exception (generic)</b> - Các exception chưa xử lý → HTTP 500,
 *       log lỗi đầy đủ nhưng không lộ chi tiết implementation cho client</li>
 * </ul>
 *
 * <p>Response body luôn chứa: {@code timestamp}, {@code status}, {@code error}, {@code message}, {@code path}.</p>
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Xử lý lỗi parse JSON request body (malformed JSON, sai type, ...).
     *
     * @param ex      exception gốc
     * @param headers HTTP headers
     * @param status  HTTP status code
     * @param request web request hiện tại
     * @return ResponseEntity chứa thông tin lỗi chi tiết
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        LOGGER.warn("Request body parse failed at {}: {}", path, ex.getMostSpecificCause().getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", java.time.Instant.now());
        body.put("status", status.value());
        body.put("error", "Invalid JSON");
        body.put("message", "Malformed JSON request: " + ex.getMostSpecificCause().getMessage());
        body.put("path", path);
        return new ResponseEntity<>(body, headers, status);
    }

    /**
     * Xử lý lỗi validation {@code @Valid} trên request body.
     * Trả về danh sách lỗi chi tiết theo từng field trong key {@code fields}.
     *
     * @param ex      exception validation
     * @param headers HTTP headers
     * @param status  HTTP status code
     * @param request web request hiện tại
     * @return ResponseEntity chứa map các lỗi field-level
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        LOGGER.warn("Validation failed at {}: {}", path, ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", java.time.Instant.now());
        body.put("status", status.value());
        body.put("error", "Validation Failed");
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        body.put("fields", fieldErrors);
        body.put("path", path);
        return new ResponseEntity<>(body, headers, status);
    }

    /**
     * Xử lý tất cả các exception chưa được xử lý cụ thể.
     * Log lỗi đầy đủ (stack trace) nhưng chỉ trả message chung chung cho client.
     *
     * @param ex      exception gốc
     * @param request web request hiện tại
     * @return ResponseEntity với HTTP 500 và thông tin lỗi an toàn
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnhandledException(Exception ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        LOGGER.error("Unhandled exception at {}", path, ex);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", java.time.Instant.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage() != null ? ex.getMessage() : "Unexpected server error");
        body.put("path", path);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
