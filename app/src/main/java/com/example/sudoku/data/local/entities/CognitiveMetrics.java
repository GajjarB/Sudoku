package com.example.sudoku.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Daily cognitive performance metrics.
 * Computed by BehavioralAnalyzer once per day.
 */
@Entity(tableName = "cognitive_metrics")
public class CognitiveMetrics {
    
    @PrimaryKey
    @NonNull
    public String dateKey;          // "2025-02-16"
    
    // Speed metrics
    public double avgDecisionTimeMs;
    public double fastestDecisionMs;
    public double slowestDecisionMs;
    
    // Accuracy metrics
    public double errorRate;        // % wrong placements
    public double correctionRate;   // % self-corrections
    public double hintDependency;   // % games using hints
    
    // Cognitive load
    public double hesitationScore;  // Variance in timing
    public double focusScore;       // Consistency
    public double fatigueIndex;     // Performance drop over session
    
    // Learning
    public double improvementRate;  // Week-over-week
    public double plateauScore;     // 0-1, stagnation detector
    public int challengeReadiness;  // 0-100, ready for harder?
    
    // Session data
    public int avgSessionLength;
    public String peakPerformanceTime;
    
    public CognitiveMetrics(@NonNull String dateKey) {
        this.dateKey = dateKey;
        this.avgDecisionTimeMs = 5000;
        this.fastestDecisionMs = 1000;
        this.slowestDecisionMs = 30000;
        this.errorRate = 0.3;
        this.correctionRate = 0.2;
        this.hintDependency = 0.5;
        this.hesitationScore = 0.5;
        this.focusScore = 0.5;
        this.fatigueIndex = 0.2;
        this.improvementRate = 0;
        this.plateauScore = 0;
        this.challengeReadiness = 30;
        this.avgSessionLength = 600; // 10 minutes
        this.peakPerformanceTime = "afternoon";
    }
}
