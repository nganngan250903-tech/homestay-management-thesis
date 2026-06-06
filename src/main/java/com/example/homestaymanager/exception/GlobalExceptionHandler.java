package com.example.homestaymanager.exception;
import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<?> handleUnauthorizedException(UnauthorizedException ex){
        return ApiResponse.of(ApiStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ApiResponse<?> handleBadRequestException(BadRequestException ex){
        return ApiResponse.of(ApiStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handleRuntimeException(RuntimeException ex){
        if (isTechnicalException(ex)) {
            log.error("Technical runtime exception", ex);
            return ApiResponse.of(ApiStatus.INTERNAL_ERROR, ApiMessage.ERROR, null);
        }
        return ApiResponse.of(ApiStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(DataAccessException.class)
    public ApiResponse<?> handleDataAccessException(DataAccessException ex){
        log.error("Database exception", ex);
        return ApiResponse.of(ApiStatus.INTERNAL_ERROR, ApiMessage.ERROR, null);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception ex){
        log.error("Unhandled exception", ex);
        return ApiResponse.of(ApiStatus.INTERNAL_ERROR, ApiMessage.ERROR, null);
    }

    private static boolean isTechnicalException(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof DataAccessException) {
                return true;
            }
            String className = current.getClass().getName().toLowerCase();
            if (className.contains("hibernate")
                    || className.contains("jdbc")
                    || className.contains("sql")
                    || className.contains("transaction")) {
                return true;
            }
            String message = current.getMessage();
            if (message != null && isTechnicalMessage(message)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean isTechnicalMessage(String message) {
        String normalized = message.toLowerCase();
        return normalized.contains("could not execute statement")
                || normalized.contains("deadlock")
                || normalized.contains("sql [")
                || normalized.contains("constraint")
                || normalized.contains("duplicate entry")
                || normalized.contains("jdbc")
                || normalized.contains("hibernate")
                || normalized.contains("transaction");
    }
}
