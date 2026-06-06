package com.example.homestaymanager.exception;
import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        return ApiResponse.of(ApiStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception ex){
        log.error("Unhandled exception", ex);
        String message = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : ApiMessage.ERROR;
        return ApiResponse.of(ApiStatus.INTERNAL_ERROR, message, null);
    }
}
