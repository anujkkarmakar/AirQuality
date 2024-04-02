package com.example.airquality;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CircularMeterView extends View {
    private static final int MAX_VALUE = 5;
    private static final float NEEDLE_LENGTH = 0.8f; // Length of the needle (0 to 1)

    private Paint circlePaint;
    private Paint needlePaint;
    private Paint textPaint;
    private float currentValue = 0; // Initial value

    public CircularMeterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        circlePaint = new Paint();
        circlePaint.setColor(Color.LTGRAY);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(20);

        needlePaint = new Paint();
        needlePaint.setColor(Color.RED);
        needlePaint.setStyle(Paint.Style.STROKE);
        needlePaint.setStrokeWidth(10);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setValue(float value) {
        // Set the value (between 1 and 10)
        currentValue = Math.max(1, Math.min(value, MAX_VALUE));
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        float radius = Math.min(centerX, centerY) - 30;

        // Draw the circular meter
        canvas.drawCircle(centerX, centerY, radius, circlePaint);

        String[] numericLabels = {"1", "2", "3", "4", "5"};


        // Draw labels (1 to 10)
        for (int i = 1; i <= MAX_VALUE; i++) {
            float angle = (float) Math.toRadians(180 - (i - 1) * 45); // Equally spaced angles
            float x = centerX + radius * 0.9f * (float) Math.cos(angle);
            float y = centerY - radius * 0.9f * (float) Math.sin(angle);
            canvas.drawText(numericLabels[i - 1], x, y, textPaint);
        }

        // Calculate needle position
//        float angle = (1 - (currentValue / MAX_VALUE)) * 180f; // Angle in degrees
        float angle = (float) (180 - (currentValue - 1) * 45);
        float startX = centerX;
        float startY = centerY;
        float endX = centerX + radius * NEEDLE_LENGTH * (float) Math.cos(Math.toRadians(angle));
        float endY = centerY - radius * NEEDLE_LENGTH * (float) Math.sin(Math.toRadians(angle));

        // Draw the needle
        canvas.drawLine(startX, startY, endX, endY, needlePaint);
    }
}
