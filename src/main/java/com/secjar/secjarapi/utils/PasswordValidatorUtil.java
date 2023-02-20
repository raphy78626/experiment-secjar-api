package com.secjar.secjarapi.utils;

import org.passay.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class PasswordValidatorUtil {

    private final PasswordValidator validator;

    public PasswordValidatorUtil() {
        validator = new PasswordValidator(Arrays.asList(
                new LengthRule(8, 30),
                new UppercaseCharacterRule(1),
                new LowercaseCharacterRule(1),
                new DigitCharacterRule(1),
                new SpecialCharacterRule(1),
                new WhitespaceRule()));
    }

    public boolean validate(String password) {
        return validator.validate(new PasswordData(password)).isValid();
    }
}
