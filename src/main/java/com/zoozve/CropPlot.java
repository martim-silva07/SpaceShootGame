package com.zoozve;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

public class CropPlot {
    public int x, y, width = 85, height = 65;
    public int state = 0;
    private int growthTimer = 0;

    public CropPlot(int x, int y) { this.x = x; this.y = y; }

    public void plant()   { state = 1; growthTimer = 0; }
    public void harvest() { state = 0; }

    public void update() {
        if (state > 0 && state < 3) {
            growthTimer++;
            if (growthTimer > 250) { state++; growthTimer = 0; }
        }
    }

    public void draw(ShapeRenderer sr) {
        sr.setColor(36/255f, 24/255f, 14/255f, 1f);
        sr.rect(x, y, width, height);
        sr.set(ShapeType.Line);
        sr.setColor(90/255f, 70/255f, 50/255f, 1f);
        sr.rect(x, y, width, height);
        sr.set(ShapeType.Filled);

        if (state == 1) {
            sr.setColor(145/255f, 90/255f, 40/255f, 1f);
            sr.ellipse(x + 38, y + 28, 8, 8);
        } else if (state == 2) {
            sr.setColor(50/255f, 180/255f, 70/255f, 1f);
            sr.rect(x + 40, y + 20, 4, 25);
        } else if (state == 3) {
            sr.setColor(0f, 1f, 100/255f, 1f);
            sr.rect(x + 38, y + 12, 8, 35);
            sr.set(ShapeType.Line);
            sr.setColor(0f, 1f, 100/255f, 50/255f);
            sr.rect(x + 32, y + 6, 20, 47);
            sr.set(ShapeType.Filled);
        }
    }

    public boolean isPlayerNear(Player p) {
        return new Rectangle(x - 15, y - 15, width + 30, height + 30)
                .overlaps(new Rectangle(p.x, p.y, p.width, p.height));
    }
}