package com.zoozve;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class HeartItem {
    public int x, y;
    public boolean collected = false;
    private double bobTimer = 0;

    public HeartItem(int x, int y) { this.x = x; this.y = y; }

    public void update() {
        y += 2;
        bobTimer += 0.1;
        x += (int)(Math.sin(bobTimer) * 0.5);
    }

    public void draw(ShapeRenderer sr) {
        sr.setColor(Color.RED);
        sr.ellipse(x, y, 10, 10);
        sr.ellipse(x + 10, y, 10, 10);
        sr.triangle(x, y + 6, x + 20, y + 6, x + 10, y + 18);
    }
}