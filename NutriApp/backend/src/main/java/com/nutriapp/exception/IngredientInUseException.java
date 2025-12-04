package com.nutriapp.exception;

public class IngredientInUseException extends RuntimeException {
    public IngredientInUseException(String message) {
        super(message);
    }
}
