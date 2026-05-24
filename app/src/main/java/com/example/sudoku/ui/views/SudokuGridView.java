package com.example.sudoku.ui.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import com.example.sudoku.R;
import com.example.sudoku.domain.models.Cell;
import com.example.sudoku.domain.models.Grid;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════
 * SUDOKU GRID VIEW — Clean Minimal Design
 * ═══════════════════════════════════════════════════════════════════
 * 
 * Beautiful, minimal Sudoku grid inspired by Dustland's "The Clean One".
 * Adapted to work with domain Grid model.
 */
public class SudokuGridView extends View {
    
    // ═══════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════════
    
    private static final int GRID_SIZE = 9;
    private static final int BOX_SIZE = 3;
    
    // ═══════════════════════════════════════════════════════════════
    // GRID DATA
    // ═══════════════════════════════════════════════════════════════
    
    private Grid grid;
    
    // ═══════════════════════════════════════════════════════════════
    // SELECTION & HIGHLIGHTING
    // ═══════════════════════════════════════════════════════════════
    
    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean highlightSameNumbers = true;
    private boolean highlightConflicts = true;
    
    // ═══════════════════════════════════════════════════════════════
    // PAINT OBJECTS (Reusable)
    // ═══════════════════════════════════════════════════════════════
    
    private Paint gridBackgroundPaint;
    private Paint thinLinePaint;
    private Paint thickLinePaint;
    private Paint selectedCellPaint;
    private Paint selectedRowColPaint;
    private Paint sameNumberPaint;
    private Paint conflictPaint;
    private Paint lockedCellPaint;
    private Paint textPaint;
    private Paint textLockedPaint;
    private Paint textPlayerPaint;
    private Paint textErrorPaint;
    private Paint pencilTextPaint;
    private Paint coordinatePaint;
    private Paint selectedOutlinePaint;
    private Paint markerPaint;
    
    // ═══════════════════════════════════════════════════════════════
    // DIMENSIONS
    // ═══════════════════════════════════════════════════════════════
    
    private float cellSize;
    private float gridLeft, gridTop;
    private RectF gridBounds = new RectF();
    
    // ═══════════════════════════════════════════════════════════════
    // ANIMATION
    // ═══════════════════════════════════════════════════════════════
    
    private ValueAnimator selectionAnimator;
    private float selectionAnimValue = 1.0f;
    
    // ═══════════════════════════════════════════════════════════════
    // THEME COLORS (Dynamic)
    // ═══════════════════════════════════════════════════════════════
    
    private int colorGridBackground;
    private int colorGridLineThin;
    private int colorGridLineThick;
    private int colorCellSelected;
    private int colorCellSelectedLight;
    private int colorCellSameNumber;
    private int colorCellConflict;
    private int colorCellLocked;
    private int colorTextPrimary;
    private int colorTextLocked;
    private int colorTextPlayer;
    private int colorTextPencil;
    private int colorTextError;
    
    // ═══════════════════════════════════════════════════════════════
    // LISTENER
    // ═══════════════════════════════════════════════════════════════
    
    private OnCellSelectedListener listener;
    
    public interface OnCellSelectedListener {
        void onCellSelected(int row, int col);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public SudokuGridView(Context context) {
        super(context);
        init(context);
    }
    
    public SudokuGridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public SudokuGridView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════
     * INITIALIZATION
     * ═══════════════════════════════════════════════════════════════
     */
    private void init(Context context) {
        // Load colors
        loadColors(context);

        // Initialize paints
        initPaints();

        // Enable software layer for BlurMaskFilter glow effects
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        // Enable drawing
        setWillNotDraw(false);
    }
    
    /**
     * Load theme colors from resources.
     */
    private void loadColors(Context context) {
        colorGridBackground = context.getColor(R.color.grid_background);
        colorGridLineThin = context.getColor(R.color.grid_line_thin);
        colorGridLineThick = context.getColor(R.color.grid_line_thick);
        colorCellSelected = context.getColor(R.color.cell_selected);
        colorCellSelectedLight = context.getColor(R.color.cell_selected_light);
        colorCellSameNumber = context.getColor(R.color.cell_same_number);
        colorCellConflict = context.getColor(R.color.cell_conflict);
        colorCellLocked = context.getColor(R.color.cell_locked);
        colorTextPrimary = context.getColor(R.color.text_primary);
        colorTextLocked = context.getColor(R.color.text_locked);
        colorTextPlayer = context.getColor(R.color.text_player);
        colorTextPencil = context.getColor(R.color.text_pencil);
        colorTextError = context.getColor(R.color.text_error);
    }
    
    /**
     * Initialize all Paint objects.
     */
    private void initPaints() {
        // Grid background
        gridBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridBackgroundPaint.setColor(colorGridBackground);
        gridBackgroundPaint.setStyle(Paint.Style.FILL);
        
        // Grid lines
        thinLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thinLinePaint.setColor(colorGridLineThin);
        thinLinePaint.setStrokeWidth(dpToPx(1));
        thinLinePaint.setStyle(Paint.Style.STROKE);
        
        thickLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thickLinePaint.setColor(colorGridLineThick);
        thickLinePaint.setStrokeWidth(dpToPx(3));
        thickLinePaint.setStyle(Paint.Style.STROKE);
        
        // Cell backgrounds
        selectedCellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedCellPaint.setColor(colorCellSelected);
        selectedCellPaint.setStyle(Paint.Style.FILL);
        
        selectedRowColPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedRowColPaint.setColor(colorCellSelectedLight);
        selectedRowColPaint.setStyle(Paint.Style.FILL);
        
        sameNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sameNumberPaint.setColor(colorCellSameNumber);
        sameNumberPaint.setStyle(Paint.Style.FILL);
        
        conflictPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        conflictPaint.setColor(colorCellConflict);
        conflictPaint.setStyle(Paint.Style.FILL);
        
        lockedCellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lockedCellPaint.setColor(colorCellLocked);
        lockedCellPaint.setStyle(Paint.Style.FILL);
        
        // Text paints
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(colorTextPrimary);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        
        textLockedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textLockedPaint.setColor(colorTextLocked);
        textLockedPaint.setTextAlign(Paint.Align.CENTER);
        textLockedPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        
        textPlayerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPlayerPaint.setColor(colorTextPlayer);
        textPlayerPaint.setTextAlign(Paint.Align.CENTER);
        textPlayerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        
        textErrorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textErrorPaint.setColor(colorTextError);
        textErrorPaint.setTextAlign(Paint.Align.CENTER);
        textErrorPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        
        pencilTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pencilTextPaint.setColor(colorTextPencil);
        pencilTextPaint.setTextAlign(Paint.Align.CENTER);
        pencilTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        coordinatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        coordinatePaint.setColor(colorTextPencil);
        coordinatePaint.setTextAlign(Paint.Align.CENTER);
        coordinatePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        selectedOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedOutlinePaint.setColor(colorGridLineThick);
        selectedOutlinePaint.setStrokeWidth(dpToPx(2));
        selectedOutlinePaint.setStyle(Paint.Style.STROKE);

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(colorGridLineThick);
        markerPaint.setStrokeWidth(dpToPx(2));
        markerPaint.setStyle(Paint.Style.STROKE);
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════
     * MEASUREMENT
     * ═══════════════════════════════════════════════════════════════
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Make view square (edge-to-edge like Dustland)
        int size = Math.min(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        );
        
        setMeasuredDimension(size, size);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Calculate cell size
        float labelGutter = dpToPx(20);
        float gridSize = Math.min(w - labelGutter, h - labelGutter);
        cellSize = gridSize / GRID_SIZE;
        
        gridLeft = labelGutter;
        gridTop = labelGutter;
        
        gridBounds.set(gridLeft, gridTop, gridLeft + gridSize, gridTop + gridSize);
        
        // Update text sizes
        textPaint.setTextSize(cellSize * 0.55f);
        textLockedPaint.setTextSize(cellSize * 0.55f);
        textPlayerPaint.setTextSize(cellSize * 0.55f);
        textErrorPaint.setTextSize(cellSize * 0.55f);
        pencilTextPaint.setTextSize(cellSize * 0.22f);
        coordinatePaint.setTextSize(Math.max(dpToPx(9), cellSize * 0.22f));
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════
     * DRAWING
     * ═══════════════════════════════════════════════════════════════
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (grid == null) return;

        // Draw coordinate labels
        drawCoordinates(canvas);
        
        // Draw grid background
        canvas.drawRect(gridBounds, gridBackgroundPaint);

        // Draw cell backgrounds (highlights)
        drawCellBackgrounds(canvas);
        
        // Draw grid lines
        drawGridLines(canvas);
        
        // Draw numbers and pencil marks
        drawNumbers(canvas);
    }
    
    /**
     * Draw cell background colors (selection, highlights).
     */
    private void drawCellBackgrounds(Canvas canvas) {
        int selectedBoxR = (selectedRow / 3) * 3;
        int selectedBoxC = (selectedCol / 3) * 3;

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Cell cell = grid.getCell(r, c);
                if (cell == null) continue;
                
                float left = gridLeft + c * cellSize;
                float top = gridTop + r * cellSize;
                float right = left + cellSize;
                float bottom = top + cellSize;
                
                // Locked cells (given clues) - light background
                if (cell.isLocked()) {
                    canvas.drawRect(left, top, right, bottom, lockedCellPaint);
                }
                
                // Selected row/column/box highlight
                if (selectedRow >= 0 && selectedCol >= 0) {
                    if (r == selectedRow || c == selectedCol || (r >= selectedBoxR && r < selectedBoxR + 3 && c >= selectedBoxC && c < selectedBoxC + 3)) {
                        canvas.drawRect(left, top, right, bottom, selectedRowColPaint);
                    }
                }
                
                // Same number highlight
                if (highlightSameNumbers && selectedRow >= 0 && selectedCol >= 0) {
                    Cell selectedCell = grid.getCell(selectedRow, selectedCol);
                    if (selectedCell != null && selectedCell.getValue() > 0) {
                        if (cell.getValue() == selectedCell.getValue()) {
                            drawHatch(canvas, left, top, right, bottom);
                        }
                    }
                }

                if (cell.isHintHighlighted()) {
                    drawHatch(canvas, left, top, right, bottom);
                }

                if (highlightConflicts && cell.isConflict()) {
                    canvas.drawRect(left, top, right, bottom, conflictPaint);
                    drawConflictMarker(canvas, left, top, right, bottom);
                }
                
                // Selected cell (with animation and glow)
                if (r == selectedRow && c == selectedCol) {
                    // Animate selection with subtle pulse
                    int alpha = (int)(selectionAnimValue * 255);
                    selectedCellPaint.setAlpha(alpha);
                    canvas.drawRect(left, top, right, bottom, selectedCellPaint);
                    selectedCellPaint.setAlpha(255);
                    float inset = dpToPx(3);
                    canvas.drawRect(left + inset, top + inset, right - inset, bottom - inset, selectedOutlinePaint);
                }
            }
        }
    }

    private void drawCoordinates(Canvas canvas) {
        Paint.FontMetrics fm = coordinatePaint.getFontMetrics();
        float textOffset = -((fm.descent + fm.ascent) / 2);
        for (int i = 0; i < GRID_SIZE; i++) {
            float center = i * cellSize + cellSize / 2f;
            canvas.drawText(String.valueOf((char) ('A' + i)), gridLeft + center, gridTop / 2f + textOffset, coordinatePaint);
            canvas.drawText(String.valueOf(i + 1), gridLeft / 2f, gridTop + center + textOffset, coordinatePaint);
        }
    }

    private void drawHatch(Canvas canvas, float left, float top, float right, float bottom) {
        canvas.drawRect(left, top, right, bottom, sameNumberPaint);
    }

    private void drawConflictMarker(Canvas canvas, float left, float top, float right, float bottom) {
        float dotRadius = cellSize * 0.06f;
        Paint conflictDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        conflictDotPaint.setStyle(Paint.Style.FILL);
        conflictDotPaint.setColor(0xFFE5484D);
        canvas.drawCircle(right - dotRadius - dpToPx(3), top + dotRadius + dpToPx(3), dotRadius, conflictDotPaint);
    }
    
    /**
     * Draw grid lines (thin and thick).
     */
    private void drawGridLines(Canvas canvas) {
        for (int i = 0; i <= GRID_SIZE; i++) {
            float offset = i * cellSize;
            
            // Choose paint based on line position
            Paint paint = (i % BOX_SIZE == 0) ? thickLinePaint : thinLinePaint;
            
            // Vertical line
            canvas.drawLine(
                gridLeft + offset, gridTop,
                gridLeft + offset, gridTop + GRID_SIZE * cellSize,
                paint
            );
            
            // Horizontal line
            canvas.drawLine(
                gridLeft, gridTop + offset,
                gridLeft + GRID_SIZE * cellSize, gridTop + offset,
                paint
            );
        }
    }
    
    /**
     * Draw numbers and pencil marks.
     */
    private void drawNumbers(Canvas canvas) {
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Cell cell = grid.getCell(r, c);
                if (cell == null) continue;
                
                float centerX = gridLeft + c * cellSize + cellSize / 2f;
                float centerY = gridTop + r * cellSize + cellSize / 2f;
                
                int value = cell.getValue();
                
                if (value > 0) {
                    // Draw number
                    String text = String.valueOf(value);
                    
                    // Choose paint based on cell type
                    Paint paint;
                    if (cell.isLocked()) {
                        paint = textLockedPaint;
                    } else if (cell.isConflict()) {
                        paint = textErrorPaint;
                    } else if (cell.isCorrect()) {
                        paint = textPlayerPaint;
                    } else {
                        paint = textErrorPaint;
                    }
                    
                    // Center text vertically
                    float textY = centerY - ((paint.descent() + paint.ascent()) / 2);
                    
                    canvas.drawText(text, centerX, textY, paint);
                    
                } else {
                    List<Integer> marks = cell.getNotes();
                    if (!marks.isEmpty()) {
                        // Draw pencil marks (3x3 grid of small numbers)
                        drawPencilMarks(canvas, r, c, marks);
                    }
                }
            }
        }
    }
    
    /**
     * Draw pencil marks in 3x3 mini-grid.
     */
    private void drawPencilMarks(Canvas canvas, int row, int col, List<Integer> marks) {
        float cellLeft = gridLeft + col * cellSize;
        float cellTop = gridTop + row * cellSize;
        float miniCellSize = cellSize / 3f;
        
        for (int mark : marks) {
            if (mark < 1 || mark > 9) continue;
            
            // Position in 3x3 grid
            int miniRow = (mark - 1) / 3;
            int miniCol = (mark - 1) % 3;
            
            float x = cellLeft + miniCol * miniCellSize + miniCellSize / 2f;
            float y = cellTop + miniRow * miniCellSize + miniCellSize / 2f;
            float textY = y - ((pencilTextPaint.descent() + pencilTextPaint.ascent()) / 2);
            
            canvas.drawText(String.valueOf(mark), x, textY, pencilTextPaint);
        }
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════
     * TOUCH HANDLING
     * ═══════════════════════════════════════════════════════════════
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            
            // Check if touch is within grid
            if (gridBounds.contains(x, y)) {
                // Calculate cell coordinates
                int col = (int)((x - gridLeft) / cellSize);
                int row = (int)((y - gridTop) / cellSize);
                
                // Validate bounds
                if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
                    selectCell(row, col);
                    performClick();
                    return true;
                }
            }
        }
        
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        if (selectedRow >= 0 && selectedCol >= 0) {
            Cell cell = grid != null ? grid.getCell(selectedRow, selectedCol) : null;
            String value = cell != null && cell.getValue() > 0
                    ? String.valueOf(cell.getValue())
                    : "empty";
            announceForAccessibility("Selected row " + (selectedRow + 1)
                    + ", column " + (selectedCol + 1) + ", " + value);
        }
        return true;
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════
     * PUBLIC API
     * ═══════════════════════════════════════════════════════════════
     */
    
    /**
     * Set the grid object.
     */
    public void setGrid(Grid grid) {
        this.grid = grid;
        postInvalidateOnAnimation();
    }
    
    /**
     * Select a cell.
     */
    public void selectCell(int row, int col) {
        if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
            selectedRow = row;
            selectedCol = col;
            
            // Animate selection
            animateSelection();
            
            // Notify listener
            if (listener != null) {
                listener.onCellSelected(row, col);
            }
            
            postInvalidateOnAnimation();
        }
    }
    
    /**
     * Clear selection.
     */
    public void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
        // No animation when clearing usually
        postInvalidateOnAnimation();
    }
    
    /**
     * Set listener for cell selection.
     */
    public void setListener(OnCellSelectedListener listener) {
        this.listener = listener;
    }

    public void setHighlightSameNumbers(boolean enabled) {
        this.highlightSameNumbers = enabled;
        postInvalidateOnAnimation();
    }

    public void setHighlightConflicts(boolean enabled) {
        this.highlightConflicts = enabled;
        postInvalidateOnAnimation();
    }
    
    /**
     * Update theme colors dynamically.
     */
    public void updateTheme(int accentColor, int backgroundColor, int textColor, int gridLineColor) {
        colorGridLineThick = getResources().getColor(R.color.grid_line_thick);
        colorGridLineThin = getResources().getColor(R.color.grid_line_thin);
        colorCellSelected = getResources().getColor(R.color.cell_selected);
        colorCellSelectedLight = getResources().getColor(R.color.cell_highlighted);
        colorCellSameNumber = getResources().getColor(R.color.cell_same_number);
        colorCellConflict = getResources().getColor(R.color.cell_conflict);
        colorCellLocked = getResources().getColor(R.color.cell_locked);
        colorGridBackground = getResources().getColor(R.color.bg_grid);
        colorTextLocked = getResources().getColor(R.color.text_locked);
        colorTextPrimary = getResources().getColor(R.color.text_primary);
        colorTextPlayer = getResources().getColor(R.color.text_player);
        colorTextError = getResources().getColor(R.color.text_error);
        colorTextPencil = getResources().getColor(R.color.text_pencil);

        // Update paints
        initPaints();
        postInvalidateOnAnimation();
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════
     * ANIMATION
     * ═══════════════════════════════════════════════════════════════
     */
    
    private void animateSelection() {
        if (selectionAnimator != null) {
            selectionAnimator.cancel();
        }
        
        // Subtle pulse animation
        selectionAnimator = ValueAnimator.ofFloat(0.7f, 1.0f);
        selectionAnimator.setDuration(200);
        selectionAnimator.addUpdateListener(animation -> {
            selectionAnimValue = (float) animation.getAnimatedValue();
            postInvalidateOnAnimation();
        });
        selectionAnimator.start();
    }

    /**
     * Shake animation for wrong input feedback.
     */
    public void shakeSelectedCell() {
        ObjectAnimator shake = ObjectAnimator.ofFloat(this, View.TRANSLATION_X,
                0f, 10f, -10f, 7f, -7f, 3f, -3f, 0f);
        shake.setDuration(380);
        shake.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (selectionAnimator != null) {
            selectionAnimator.cancel();
            selectionAnimator.removeAllUpdateListeners();
            selectionAnimator = null;
        }
        listener = null;
        super.onDetachedFromWindow();
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════
     * UTILITIES
     * ═══════════════════════════════════════════════════════════════
     */
    
    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
