package com.zjun.widget.rotate;

import static android.view.View.MeasureSpec.UNSPECIFIED;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.zjun.widget.R;

/**
 * Rotates first view in this layout by specified angle.
 * <p>
 * This layout is supposed to have only one view. Behaviour of the views after the first one
 * is not defined.
 * <p>
 * XML attributes
 * See com.github.rongi.rotate_layout.R.styleable#RotateLayout RotateLayout Attributes,
 * This class from <a href="https://github.com/rongi/rotate-layout">
 */
public class RotateLayout extends ViewGroup {

    private int angle;

    private final Matrix rotateMatrix = new Matrix();

    private final Rect viewRectRotated = new Rect();

    private final RectF tempRectF1 = new RectF();
    private final RectF tempRectF2 = new RectF();

    private final float[] viewTouchPoint = new float[2];
    private final float[] childTouchPoint = new float[2];

    private boolean angleChanged = true;

    public RotateLayout(Context context) {
        this(context, null);
    }

    public RotateLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RotateLayout);
        angle = a.getInt(R.styleable.RotateLayout_angle, 0);
        a.recycle();

        setWillNotDraw(false);
    }

    /**
     * Returns current angle of this layout
     */
    public int getAngle() {
        return angle;
    }

    /**
     * Sets current angle of this layout.
     */
    public void setAngle(int angle) {
        if (this.angle != angle) {
            this.angle = angle;
            angleChanged = true;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Returns this layout's child or null if there is no any
     */
    public View getView() {
        if (getChildCount() > 0) {
            return getChildAt(0);
        } else {
            return null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final View child = getView();
        if (child != null) {
            if (Math.abs(angle % 180) == 90) {
                //noinspection SuspiciousNameCombination
                measureChild(child, heightMeasureSpec, widthMeasureSpec);
                setMeasuredDimension(
                        resolveSize(child.getMeasuredHeight(), widthMeasureSpec),
                        resolveSize(child.getMeasuredWidth(), heightMeasureSpec));
            } else if (Math.abs(angle % 180) == 0) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                setMeasuredDimension(
                        resolveSize(child.getMeasuredWidth(), widthMeasureSpec),
                        resolveSize(child.getMeasuredHeight(), heightMeasureSpec));
            } else {
                int childWithMeasureSpec = MeasureSpec.makeMeasureSpec(0, UNSPECIFIED);
                int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, UNSPECIFIED);
                measureChild(child, childWithMeasureSpec, childHeightMeasureSpec);

                int measuredWidth = (int) Math.ceil(child.getMeasuredWidth() * Math.abs(Math.cos(angle_c())) + child.getMeasuredHeight() * Math.abs(Math.sin(angle_c())));
                int measuredHeight = (int) Math.ceil(child.getMeasuredWidth() * Math.abs(Math.sin(angle_c())) + child.getMeasuredHeight() * Math.abs(Math.cos(angle_c())));

                setMeasuredDimension(
                        resolveSize(measuredWidth, widthMeasureSpec),
                        resolveSize(measuredHeight, heightMeasureSpec));
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int layoutWidth = r - l;
        int layoutHeight = b - t;

        if (angleChanged || changed) {
            final RectF layoutRect = tempRectF1;
            layoutRect.set(0, 0, layoutWidth, layoutHeight);
            final RectF layoutRectRotated = tempRectF2;
            rotateMatrix.setRotate(angle, layoutRect.centerX(), layoutRect.centerY());
            rotateMatrix.mapRect(layoutRectRotated, layoutRect);
            layoutRectRotated.round(viewRectRotated);
            angleChanged = false;
        }

        final View child = getView();
        if (child != null) {
            int childLeft = (layoutWidth - child.getMeasuredWidth()) / 2;
            int childTop = (layoutHeight - child.getMeasuredHeight()) / 2;
            int childRight = childLeft + child.getMeasuredWidth();
            int childBottom = childTop + child.getMeasuredHeight();
            child.layout(childLeft, childTop, childRight, childBottom);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.rotate(-angle, getWidth() / 2f, getHeight() / 2f);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        invalidate();
        return super.invalidateChildInParent(location, dirty);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        viewTouchPoint[0] = event.getX();
        viewTouchPoint[1] = event.getY();

        rotateMatrix.mapPoints(childTouchPoint, viewTouchPoint);

        event.setLocation(childTouchPoint[0], childTouchPoint[1]);
        boolean result = super.dispatchTouchEvent(event);
        event.setLocation(viewTouchPoint[0], viewTouchPoint[1]);

        return result;
    }

    /**
     * Circle angle, from 0 to TAU
     */
    private Double angle_c() {
        // True circle constant, not that petty imposter known as "PI"
        double TAU = 2 * Math.PI;
        return TAU * angle / 360;
    }

}