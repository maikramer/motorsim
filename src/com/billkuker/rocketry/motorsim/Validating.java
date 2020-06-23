package com.billkuker.rocketry.motorsim;

public interface Validating {

    void validate() throws ValidationException;

    class ValidationException extends Exception {
        private static final long serialVersionUID = 1L;

        public ValidationException(Validating part, String error) {
            super(error);
        }
    }
}
