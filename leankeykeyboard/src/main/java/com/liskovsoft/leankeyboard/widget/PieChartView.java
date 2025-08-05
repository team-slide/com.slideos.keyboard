package com.liskovsoft.leankeyboard.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PieChartView extends View {
    
    private Paint mPaint;
    private RectF mRectF;
    private float mUsedPercentage;
    private int mUsedColor;
    private int mFreeColor;
    
    public PieChartView(Context context) {
        super(context);
        init();
    }
    
    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        
        mRectF = new RectF();
        
        // Default colors
        mUsedColor = Color.parseColor("#FF6B6B"); // Red for used space
        mFreeColor = Color.parseColor("#4ECDC4"); // Teal for free space
        
        mUsedPercentage = 0.0f;
    }
    
    public void setStorageData(long usedBytes, long totalBytes) {
        if (totalBytes > 0) {
            mUsedPercentage = (float) usedBytes / totalBytes;
        } else {
            mUsedPercentage = 0.0f;
        }
        invalidate();
    }
    
    public void setColors(int usedColor, int freeColor) {
        mUsedColor = usedColor;
        mFreeColor = freeColor;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        if (width <= 0 || height <= 0) {
            return;
        }
        
        // Calculate the pie chart bounds with some padding
        int padding = 4;
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 2 - padding;
        
        mRectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        
        // Draw the pie chart
        float startAngle = -90; // Start from top
        
        // Draw used space
        if (mUsedPercentage > 0) {
            mPaint.setColor(mUsedColor);
            float sweepAngle = mUsedPercentage * 360;
            canvas.drawArc(mRectF, startAngle, sweepAngle, true, mPaint);
            startAngle += sweepAngle;
        }
        
        // Draw free space
        if (mUsedPercentage < 1.0f) {
            mPaint.setColor(mFreeColor);
            float sweepAngle = (1.0f - mUsedPercentage) * 360;
            canvas.drawArc(mRectF, startAngle, sweepAngle, true, mPaint);
        }
        
        // Draw center circle to create a donut effect
        mPaint.setColor(Color.BLACK);
        int innerRadius = radius * 3 / 4;
        canvas.drawCircle(centerX, centerY, innerRadius, mPaint);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredSize = 80; // Default size in dp
        
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        int width;
        int height;
        
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredSize, widthSize);
        } else {
            width = desiredSize;
        }
        
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredSize, heightSize);
        } else {
            height = desiredSize;
        }
        
        setMeasuredDimension(width, height);
    }
} 