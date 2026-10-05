#!/bin/bash
cd app
../gradlew testDebugUnitTest --tests "com.example.sudoku.domain.engine.GameValidatorTest"
