package com.amos.doublewriterconsistence.exception;

import com.amos.doublewriterconsistence.bean.Result;
import com.amos.doublewriterconsistence.type.ResultEnum;
import com.amos.doublewriterconsistence.util.ValidatorResultUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.Set;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: GlobalExceptionHandler
 * @Package: com.amos.common.exception
 * @author: amos
 * @Description:
 * @date: 2019/7/10 0010 下午 17:19
 * @Version: V1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    /**
     * 400 - Bad Request 参数类型错误
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        Result result = new Result();
        result.setCode(ResultEnum.error());
        result.setMsg("parameter type error :" + e.getMessage());
        return result;
    }

    /**
     * 400 - Bad Request 缺少请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        Result result = new Result();
        result.setCode(ResultEnum.error());
        result.setMsg("required parameter is not present");
        return result;
    }

    /**
     * 400 - Bad Request 参数解析失败
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        Result result = new Result();
        result.setCode(ResultEnum.error());
        result.setMsg("could not read json");
        return result;
    }

    /**
     * 400 - Bad Request 参数验证失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String errorMsg = ValidatorResultUtil.getMessage(bindingResult);
        Result result = new Result();
        result.setCode(ResultEnum.error());
        result.setMsg(errorMsg);
        return result;
    }

    /**
     * 400 - Bad Request 参数绑定失败
     */
    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        String errorMsg = ValidatorResultUtil.getMessage(bindingResult);
        Result result = new Result();
        result.setCode(ResultEnum.error());
        result.setMsg(errorMsg);
        return result;
    }

    /**
     * 400 - Bad Request 参数验证失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleServiceException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        ConstraintViolation<?> violation = violations.iterator().next();
        String errorMsg = violation.getMessage();
        Result result = new Result();
        result.setCode(ResultEnum.error());
        result.setMsg("parameter:" + errorMsg);
        return result;
    }

    /**
     * 400 - Bad Request 参数验证失败
     */
    @ExceptionHandler(ValidationException.class)
    public Result handleValidationException(ValidationException e) {
        Result result = new Result();
        result.setCode(ResultEnum.error());
        result.setMsg("validation exception");
        return result;
    }

    /**
     * 405 - Method Not Allowed 不支持当前请求方法
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        Result result = new Result();
        result.setCode(ResultEnum.error());
        result.setMsg("request method not supported");
        return result;
    }

    /**
     * 415 - Unsupported Media Type 不支持当前媒体类型
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result handleHttpMediaTypeNotSupportedException(Exception e) {
        Result result = new Result();
        result.setCode(ResultEnum.error());
        result.setMsg("content type not supported");
        return result;
    }

    /**
     * 操作数据库出现异常:名称重复，外键关联等
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result handleException(DataIntegrityViolationException e) {
        Result result = new Result();
        result.setCode(ResultEnum.error());
        result.setMsg("operating database exception");
        return result;
    }

    /**
     * 500 - Internal Server Error 通用异常
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        Result result = new Result();
        result.setCode(ResultEnum.error());
        if (e instanceof NullPointerException) {
            result.setMsg(String.valueOf(e));
        } else {
            result.setMsg(e.getMessage());
        }
        return result;
    }

    /**
     * 应用异常
     */
    @ExceptionHandler(RabbitMQException.class)
    public Result handleException(RabbitMQException e) {
        Result result = new Result();
        result.setCode(e.getCode());
        result.setMsg(e.getMessage());
        return result;
    }

    /**
     * 断言异常
     */
    @ExceptionHandler(AssertionError.class)
    public Result handleException(AssertionError e) {
        Result result = new Result();
        result.setCode(ResultEnum.error());
        result.setMsg(e.getMessage());
        return result;
    }

}
