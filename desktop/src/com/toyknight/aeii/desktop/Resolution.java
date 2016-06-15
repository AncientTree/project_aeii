package com.toyknight.aeii.desktop;

/**
 * @author toyknight 6/14/2016.
 */
public class Resolution {

    private final int width;

    private final int height;

    public Resolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean equals(int width, int height) {
        return this.width == width && this.height == height;
    }

    @Override
    public String toString() {
        return String.format("%d x %d", getWidth(), getHeight());
    }

}
