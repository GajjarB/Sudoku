package com.example.sudoku.domain.coach;

/**
 * Strategy that prunes candidates rather than placing a value.
 */
public interface EliminationDetector {
    CandidateElimination.StrategyHit detect(CandidateGrid candidates);
}
