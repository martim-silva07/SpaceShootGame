package com.zoozve;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Star {
    public float x, y;
    int size;

    public Star(int x, int y, int size) { this.x = x; this.y = y; this.size = size; }

    public void update() { }

    public void draw(ShapeRenderer sr) {
        sr.setColor(1f, 1f, 1f, 0.78f);
        sr.ellipse(x, y, size, size);
    }
}