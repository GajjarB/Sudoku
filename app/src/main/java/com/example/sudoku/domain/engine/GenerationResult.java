package com.example.sudoku.domain.engine;

/**
 * Discriminated result for an asynchronous puzzle generation request.
 *
 * Pre-Java 21 we model this with a tagged class plus static factory methods;
 * after Kotlin/Java 21 migration this becomes a sealed interface.
 */
public final class GenerationResult {

    public enum Status { SUCCESS, TIMEOUT, CANCELLED, FAILED }

    private final Status status;
    private final SudokuGenerator.Puzzle puzzle;
    private final String message;

    private GenerationResult(Status status, SudokuGenerator.Puzzle puzzle, String message) {
        this.status = status;
        this.puzzle = puzzle;
        this.message = message;
    }

    public static GenerationResult success(SudokuGenerator.Puzzle puzzle) {
        return new GenerationResult(Status.SUCCESS, puzzle, null);
    }

    public static GenerationResult timeout(String message) {
        return new GenerationResult(Status.TIMEOUT, null, message);
    }

    public static GenerationResult cancelled() {
        return new GenerationResult(Status.CANCELLED, null, "Generation cancelled");
    }

    public static GenerationResult failed(String message) {
        return new GenerationResult(Status.FAILED, null, message);
    }

    public Status status() { return status; }
    public SudokuGenerator.Puzzle puzzle() { return puzzle; }
    public String message() { return message; }

    public boolean isSuccess() { return status == Status.SUCCESS; }
}
