package com.zoozve;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Bullet {
    public int x, y;
    public boolean active = true;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        y -= 11;
        if (y < 40) active = false;
    }

    public void draw(ShapeRenderer sr) {
        sr.setColor(1f, 60/255f, 0f, 150/255f);
        sr.rect(x - 1, y + 4, 7, 12);
        sr.setColor(Color.YELLOW);
        sr.rect(x, y, 5, 12);
    }

    public Rectangle getBounds() { return new Rectangle(x, y, 5, 12); }
}