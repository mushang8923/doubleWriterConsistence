package com.amos.doublewriterconsistence.util;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: ValidatorResultUtil
 * @Package: com.amos.common.util
 * @author: amos
 * @Description:
 * @date: 2019/7/10 0010 下午 17:22
 * @Version: V1.0
 */
public class ValidatorResultUtil {

    public static String getMessage(BindingResult result) {
        List<FieldError> fieldErrorList = null;
        String errorMsg = null;
        if (result.hasErrors()) {
            fieldErrorList = result.getFieldErrors();
            errorMsg = fieldErrorList.stream().map(fieldError -> fieldError.getDefaultMessage()).collect(Collectors.joining("；")).replace("\"", "");
        }
        return errorMsg;
    }
}
