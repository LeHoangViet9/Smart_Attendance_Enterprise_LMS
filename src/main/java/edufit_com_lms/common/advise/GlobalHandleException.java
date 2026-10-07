package edufit_com_lms.common.advise;

import edufit_com_lms.common.exception.AppException;
import edufit_com_lms.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestControllerAdvice
public class GlobalHandleException {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException exception) {
        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message(exception.getMessage())
                .status(exception.getStatus())
                .build();

        return ResponseEntity
                .status(exception.getStatus())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult().getAllErrors().forEach(error -> {
            String errorMessage = error.getDefaultMessage();
            String fieldName;

            if (error instanceof FieldError fieldError) {
                fieldName = fieldError.getField();
            } else {
                fieldName = error.getObjectName();
            }

            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Dá»¯ liá»‡u Ä‘áº§u vÃ o khÃ´ng há»£p lá»‡! Vui lÃ²ng kiá»ƒm tra láº¡i.")
                .data(null)
                .error(errors)
                .status(HttpStatus.BAD_REQUEST)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception exception) {
        log.error("Unhandled exception: ", exception);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message("Há»‡ thá»‘ng Ä‘Ã£ xáº£y ra sá»± cá»‘ bÃ­ áº©n. Vui lÃ²ng liÃªn há»‡ Admin!")
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("JSON parse error: ", ex);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message("Äá»‹nh dáº¡ng dá»¯ liá»‡u JSON gá»­i lÃªn khÃ´ng há»£p lá»‡ hoáº·c sai kiá»ƒu dá»¯ liá»‡u!")
                .status(HttpStatus.BAD_REQUEST)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
