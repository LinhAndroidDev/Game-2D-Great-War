package com.example.game2dgreatwar.gamepanel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

public class Joystick {

    private int outerCircleCenterPositionX;
    private int outerCircleCenterPositionY;
    private int innerCircleCenterPositionX;
    private int innerCircleCenterPositionY;

    private int outerCircleRadius;
    private int innerCircleRadius;

    private final Paint innerCirclePaint;
    private final Paint outerCirclePaint;
    private static final int NO_POINTER = -1;

    private final boolean anchorRight;
    private boolean isPressed = false;
    private int pointerId = NO_POINTER;
    private double actuatorX;
    private double actuatorY;

    // Relative layout ratios so the joystick scales across screen sizes
    private static final float OUTER_RADIUS_RATIO = 0.14f;
    private static final float INNER_TO_OUTER_RATIO = 0.75f;
    private static final float MARGIN_X_RATIO = 0.04f;
    private static final float MARGIN_Y_RATIO = 0.06f;

    /**
     * @param anchorRight true to place the joystick in the bottom-right corner instead of bottom-left
     * @param innerColor color of the movable knob, to tell joysticks apart
     */
    public Joystick(boolean anchorRight, int innerColor) {
        this.anchorRight = anchorRight;
        this.outerCircleRadius = 1;
        this.innerCircleRadius = 1;

        outerCirclePaint = new Paint();
        outerCirclePaint.setColor(Color.GRAY);
        outerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        innerCirclePaint = new Paint();
        innerCirclePaint.setColor(innerColor);
        innerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    /**
     * Places and scales the joystick based on the actual surface size so it stays
     * visible and proportional on both small and large screens.
     */
    public void layoutForScreen(int screenWidth, int screenHeight) {
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }

        int shortSide = Math.min(screenWidth, screenHeight);
        outerCircleRadius = Math.max(40, Math.round(shortSide * OUTER_RADIUS_RATIO));
        innerCircleRadius = Math.max(24, Math.round(outerCircleRadius * INNER_TO_OUTER_RATIO));

        int marginX = Math.round(screenWidth * MARGIN_X_RATIO);
        int marginY = Math.round(screenHeight * MARGIN_Y_RATIO);

        outerCircleCenterPositionX = anchorRight
                ? screenWidth - marginX - outerCircleRadius
                : marginX + outerCircleRadius;
        outerCircleCenterPositionY = screenHeight - marginY - outerCircleRadius;
        innerCircleCenterPositionX = outerCircleCenterPositionX;
        innerCircleCenterPositionY = outerCircleCenterPositionY;
        resetActuator();
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle(
                outerCircleCenterPositionX,
                outerCircleCenterPositionY,
                outerCircleRadius,
                outerCirclePaint
        );

        canvas.drawCircle(
                innerCircleCenterPositionX,
                innerCircleCenterPositionY,
                innerCircleRadius,
                innerCirclePaint
        );
    }

    public void update() {
        updateInnerCirclePosition();
    }

    private void updateInnerCirclePosition() {
        innerCircleCenterPositionX = (int) (outerCircleCenterPositionX + actuatorX * outerCircleRadius);
        innerCircleCenterPositionY = (int) (outerCircleCenterPositionY + actuatorY * outerCircleRadius);
    }

    public void setActuator(double touchPositionX, double touchPositionY) {
        double deltaX = touchPositionX - outerCircleCenterPositionX;
        double deltaY = touchPositionY - outerCircleCenterPositionY;
        double deltaDistance = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));

        if (deltaDistance < outerCircleRadius) {
            actuatorX = deltaX / outerCircleRadius;
            actuatorY = deltaY / outerCircleRadius;
        } else {
            actuatorX = deltaX / deltaDistance;
            actuatorY = deltaY / deltaDistance;
        }
    }

    public boolean isPressed(double touchPositionX, double touchPositionY) {
        double joystickCenterToTouchDistance = Math.sqrt(
                Math.pow(outerCircleCenterPositionX - touchPositionX, 2) +
                        Math.pow(outerCircleCenterPositionY - touchPositionY, 2)
        );
        return joystickCenterToTouchDistance < outerCircleRadius;
    }

    /**
     * Starts controlling this joystick with the given finger if it touched down on it.
     * @return true if the touch was taken by this joystick
     */
    public boolean tryPress(int touchPointerId, double touchPositionX, double touchPositionY) {
        if (isPressed || !isPressed(touchPositionX, touchPositionY)) {
            return false;
        }
        isPressed = true;
        pointerId = touchPointerId;
        setActuator(touchPositionX, touchPositionY);
        return true;
    }

    /** Follows the finger that is controlling this joystick, if it is part of the move event. */
    public void handleMove(MotionEvent event) {
        if (!isPressed) {
            return;
        }
        int pointerIndex = event.findPointerIndex(pointerId);
        if (pointerIndex >= 0) {
            setActuator(event.getX(pointerIndex), event.getY(pointerIndex));
        }
    }

    /** Releases the joystick if the lifted finger is the one controlling it. */
    public void release(int touchPointerId) {
        if (isPressed && pointerId == touchPointerId) {
            releaseAll();
        }
    }

    public void releaseAll() {
        isPressed = false;
        pointerId = NO_POINTER;
        resetActuator();
    }

    /** How far the knob is pushed, from 0 (center) to 1 (edge). */
    public double getMagnitude() {
        return Math.min(1.0, Math.sqrt(actuatorX * actuatorX + actuatorY * actuatorY));
    }

    public boolean getIsPressed() {
        return isPressed;
    }

    public double getActuatorX() {
        return actuatorX;
    }

    public double getActuatorY() {
        return actuatorY;
    }

    public void resetActuator() {
        actuatorX = 0;
        actuatorY = 0;
    }
}
