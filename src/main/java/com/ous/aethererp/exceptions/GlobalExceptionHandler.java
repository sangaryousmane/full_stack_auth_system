package com.ous.aethererp.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;



@RestControllerAdvice
public class GlobalExceptionHandler {


    private ResponseEntity<Object> buildResponse(
            HttpStatus status,
            String message
    ){

        Map<String,Object> body = new HashMap<>();

        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);


        return new ResponseEntity<>(body,status);
    }



    //400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleBadRequest(
            IllegalArgumentException ex
    ){

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }



    //404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(
            ResourceNotFoundException ex
    ){

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }




    //409
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntime(
            RuntimeException ex
    ){

        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
    }


    // 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(Exception ex){
        return buildResponse(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );
    }


    //500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(
            Exception ex
    ){

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error"
        );
    }

    @ExceptionHandler(InvalidOTPException.class)
    public ResponseEntity<?> handleInvalidOTP(
            InvalidOTPException ex
    ) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request"
        );
    }

}