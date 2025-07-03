package org.pixelito.input;

public class Mouse {

    private static double x, y;
    private static double lastX, lastY;
    private static double deltaX, deltaY;

    private static boolean firstUpdate = true;

    public static void setPosition(double newX, double newY) {
        if (firstUpdate) {
            lastX = newX;
            lastY = newY;
            firstUpdate = false;
        }

        deltaX = newX - lastX;
        deltaY = newY - lastY;

        lastX = newX;
        lastY = newY;

        x = newX;
        y = newY;
    }

    public static double getDeltaX() {
        return deltaX;
    }

    public static double getDeltaY() {
        return deltaY;
    }

    public static void resetDeltas() {
        deltaX = 0;
        deltaY = 0;
    }

    public static double getX() {
        return x;
    }

    public static double getY() {
        return y;
    }
}