package com.example.game2dgreatwar.gamepanel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Joystick {

    private int outerCircleCenterPositionX;
    private int outerCircleCenterPositionY;
    private int innerCircleCenterPositionX;
    private int innerCircleCenterPositionY;

    private int outerCircleRadius;
    private int innerCircleRadius;

    private final Paint innerCirclePaint;
    private final Paint outerCirclePaint;
    private boolean isPressed = false;
    private double actuatorX;
    private double actuatorY;

    // Relative layout ratios so the joystick scales across screen sizes
    private static final float OUTER_RADIUS_RATIO = 0.14f;
    private static final float INNER_TO_OUTER_RATIO = 0.75f;
    private static final float MARGIN_X_RATIO = 0.04f;
    private static final float MARGIN_Y_RATIO = 0.06f;

    public Joystick(int centerPositionX, int centerPositionY, int outerCircleRadius, int innerCircleRadius) {
        this.outerCircleCenterPositionX = centerPositionX;
        this.outerCircleCenterPositionY = centerPositionY;
        this.innerCircleCenterPositionX = centerPositionX;
        this.innerCircleCenterPositionY = centerPositionY;
        this.outerCircleRadius = outerCircleRadius;
        this.innerCircleRadius = innerCircleRadius;

        outerCirclePaint = new Paint();
        outerCirclePaint.setColor(Color.GRAY);
        outerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        innerCirclePaint = new Paint();
        innerCirclePaint.setColor(Color.BLUE);
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

        outerCircleCenterPositionX = marginX + outerCircleRadius;
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

    public boolean getIsPressed() {
        return isPressed;
    }

    public void setIsPressed(boolean isPressed) {
        this.isPressed = isPressed;
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
