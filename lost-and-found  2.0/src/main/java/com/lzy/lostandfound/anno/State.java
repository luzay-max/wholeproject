package com.lzy.lostandfound.anno;

import com.lzy.lostandfound.validation.StateValidation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented //元注解
@Target({ ElementType.FIELD}) //注解作用的位置
@Retention(RetentionPolicy.RUNTIME)//注解的生命周期
@Constraint(
        validatedBy = {StateValidation.class}//指定提供校验的类
)//注解的验证器

public @interface State {
    //提供校验失败的提示信息
    String message() default "invalid state";
    //用来指定分组
    Class<?>[] groups() default {};
    //负载 获取到注解时，可以附加一些额外信息
    Class<? extends Payload>[] payload() default {};
}
