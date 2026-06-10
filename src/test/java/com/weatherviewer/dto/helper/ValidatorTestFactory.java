package com.weatherviewer.dto.helper;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

public final class ValidatorTestFactory {

    private ValidatorTestFactory() {}

    @SafeVarargs
    public static Validator skipValidator(Class<? extends ConstraintValidator<?, ?>>... toSkip) {
        try (var factory = Validation.byDefaultProvider()
                .configure()
                .constraintValidatorFactory(new ConstraintValidatorFactory() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
                        for (var skipped : toSkip) {
                            if (key == skipped) {
                                return (T) (ConstraintValidator<?, Object>) (value, context) -> true;
                            }
                        }
                        try {
                            return key.getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void releaseInstance(ConstraintValidator<?, ?> instance) {}
                })
                .buildValidatorFactory()) {
            return factory.getValidator();
        }
    }
}
