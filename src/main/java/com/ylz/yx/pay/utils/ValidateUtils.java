package com.ylz.yx.pay.utils;

import com.google.common.base.CaseFormat;
import com.ylz.yx.pay.core.constants.ApiCodeEnum;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static com.google.common.collect.Iterables.getFirst;

/**
 * 业务参数校验
 */
public class ValidateUtils {
    private final static Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public static <T> void validate(T object) {
        /*//执行验证
        Set<ConstraintViolation<T>> constraintViolations = VALIDATOR.validate(object);
        //如果有验证信息，则取出包装成异常返回
        ConstraintViolation<T> constraintViolation = getFirst(constraintViolations, null);
        if (constraintViolation != null) {
            throw new BizException(constraintViolation.getMessage());

        }*/
        Set<ConstraintViolation<Object>> resultSet = VALIDATOR.validate(object);
        if(resultSet == null || resultSet.isEmpty()){
            return ;
        }
        resultSet.stream().forEach(item -> {throw new BizException(item.getMessage());});
    }

    public static <T> void Validate(T object) {
        /*//执行验证
        Set<ConstraintViolation<T>> constraintViolations = VALIDATOR.validate(object);
        //如果有验证信息，这里执行打印内容校验结果
        for (ConstraintViolation<T> constraintViolation : constraintViolations) {
            String validateStrCode = constraintViolation.getMessage();
            String pathError = constraintViolation.getPropertyPath().toString();
            sb.append(pathError + ":" + validateStrCode + ";");
        }*/

        Set<ConstraintViolation<Object>> resultSet = VALIDATOR.validate(object);
        if(resultSet == null || resultSet.isEmpty()){
            return ;
        }
        resultSet.stream().forEach(item -> {
            throw new CustomException(HttpStatus.BAD_REQUEST, item.getMessage());
        });
    }
}


