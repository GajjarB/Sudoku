package com.example.sudoku.domain.engine;

public class GenerationFailedException extends RuntimeException {
    public GenerationFailedException(String message) {
        super(message);
    }
}
