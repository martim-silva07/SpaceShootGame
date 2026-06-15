package com.zoozve;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Coin {
    public int x, y;
    public boolean collected = false;

    public Coin(int x, int y) { this.x = x; this.y = y; }

    public void update() { y += 2; }

    public void draw(ShapeRenderer sr) {
        sr.setColor(255/255f, 215/255f, 0f, 100/255f);
        sr.ellipse(x - 2, y - 2, 16, 16);
        sr.setColor(255/255f, 200/255f, 0f, 1f);
        sr.ellipse(x, y, 12, 12);
        sr.setColor(1f, 1f, 0.8f, 0.6f);
        sr.ellipse(x + 3, y + 3, 6, 6);
    }

    public boolean hit(Player p) {
        return new Rectangle(x, y, 12, 12).overlaps(new Rectangle(p.x, p.y, p.width, p.height));
    }
}