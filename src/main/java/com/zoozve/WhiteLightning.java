package com.zoozve;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.Random;

public class WhiteLightning {
    public int x, y, life;
    private double angle, radius;
    private int size;
    private final Color color;
    private static final Random rand = new Random();

    public WhiteLightning(int centerX, int centerY) {
        this.x      = centerX;
        this.y      = centerY;
        this.radius = rand.nextInt(150) + 100;
        this.angle  = rand.nextDouble() * Math.PI * 2;
        this.life   = rand.nextInt(30) + 20;
        this.size   = rand.nextInt(5) + 4;
        int r = rand.nextInt(3);
        if      (r == 0) this.color = Color.WHITE;
        else if (r == 1) this.color = new Color(130/255f, 230/255f, 1f, 1f);
        else             this.color = new Color(200/255f, 240/255f, 1f, 1f);
    }

    public void update() {
        life--;
        angle  += 0.08;
        radius -= 3.5;
        if (radius < 5) radius = 5;
    }

    public void draw(ShapeRenderer sr) {
        int drawX = (int)(x + Math.cos(angle) * radius);
        int drawY = (int)(y + Math.sin(angle) * radius);
        float alpha = Math.max(0, Math.min(1f, life * 10 / 255f));
        sr.setColor(color.r, color.g, color.b, alpha);
        sr.ellipse(drawX - size / 2f, drawY - size / 2f, size, size);
    }
}