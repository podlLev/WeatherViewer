package com.weatherviewer.dto.helper;

import jakarta.validation.*;

import static org.assertj.core.api.Assertions.assertThat;

public final class ValidatorTestFactory {

    private ValidatorTestFactory() {}

    @SafeVarargs
    public static Validator skipValidator(Class<? extends ConstraintValidator<?, ?>>... toSkip) {
        try (ValidatorFactory factory = Validation.byDefaultProvider()
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

    public static void assertFieldHasViolation(Validator validator, Object dto, String fieldName) {
        assertThat(validator.validate(dto))
                .anyMatch(v -> v.getPropertyPath().toString().equals(fieldName));
    }

    public static void assertNoViolations(Validator validator, Object dto) {
        assertThat(validator.validate(dto)).isEmpty();
    }

}
