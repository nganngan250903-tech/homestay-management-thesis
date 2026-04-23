package com.example.homestaymanager.exception;
import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handleRuntimeException(RuntimeException ex){
        return ApiResponse.of(ApiStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception ex){
        return ApiResponse.of(ApiStatus.INTERNAL_ERROR, ApiMessage.ERROR, null);
    }
}
