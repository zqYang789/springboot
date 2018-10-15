package com.sunsan.framework.config;


import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.sunsan.framework.model.ApiException;
import com.sunsan.framework.model.ErrCode;
import com.sunsan.framework.model.Resp;
/**
 * 全局异常捕捉
 *
 * @author yangzm
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
   

 
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Resp> globalApiException(HttpServletRequest request, ApiException exception) {
        exception.printStackTrace();
        Resp resp = new Resp();
        resp.setCode(exception.getCode());
        resp.setMsg(exception.getMessage());
        logger.error("Exception", exception);
        return new ResponseEntity<Resp>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Resp> globalException(HttpServletRequest request, Exception exception) {
        exception.printStackTrace();
        Resp resp = new Resp();
        resp.setCode(ErrCode.unknown.getCode());
        resp.setMsg(exception.getMessage());
        return new ResponseEntity<Resp>(resp, HttpStatus.BAD_REQUEST);
    }

}
