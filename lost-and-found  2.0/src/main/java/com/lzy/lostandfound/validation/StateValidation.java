package com.lzy.lostandfound.validation;

import com.lzy.lostandfound.anno.State;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StateValidation implements ConstraintValidator<State, String> { //<给哪个注解提供校验，校验的类型>
    /**
     *
     * @param value 将来要校验的对象
     * @param constraintValidatorContext
     * @return 返回False表示校验失败，返回True表示校验成功
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        //提供校验规则
       if(value ==null)return false;
       if(value.equals("ADMIN")||value.equals("STUDENT")||value.equals("TEACHER"))return true;
        return false;

    }
}
