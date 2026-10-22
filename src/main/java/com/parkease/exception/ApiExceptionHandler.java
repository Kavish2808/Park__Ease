package com.parkease.exception;
import java.time.Instant; import java.util.*; import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import org.springframework.http.converter.HttpMessageNotReadableException; import org.springframework.security.core.AuthenticationException; import org.springframework.web.bind.MissingServletRequestParameterException; import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(ApiException.class) ResponseEntity<Map<String,Object>> api(ApiException e){return error(e.getStatus(),e.getMessage());}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException e){String m=e.getBindingResult().getFieldErrors().stream().findFirst().map(x->x.getField()+" "+x.getDefaultMessage()).orElse("Validation failed");return error(HttpStatus.BAD_REQUEST,m);}
 @ExceptionHandler(AuthenticationException.class) ResponseEntity<Map<String,Object>> auth(AuthenticationException e){return error(HttpStatus.UNAUTHORIZED,"Invalid email or password");}
 @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class,MissingServletRequestParameterException.class}) ResponseEntity<Map<String,Object>> badRequest(Exception e){return error(HttpStatus.BAD_REQUEST,"Invalid request");}
 @ExceptionHandler(Exception.class) ResponseEntity<Map<String,Object>> generic(Exception e){return error(HttpStatus.INTERNAL_SERVER_ERROR,"Unexpected server error");}
 private ResponseEntity<Map<String,Object>> error(HttpStatus s,String m){return ResponseEntity.status(s).body(Map.of("timestamp", Instant.now().toString(),"status",s.value(),"message",m));}
}
