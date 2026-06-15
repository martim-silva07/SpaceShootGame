package com.zoozve;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.Random;

public class Particle {
    public float x, y, dx, dy;
    public int life;
    private final Color color;
    private static final Random rand = new Random();

    public Particle(int x, int y) {
        this.x = x; this.y = y;
        this.dx = (float)(rand.nextDouble() - 0.5) * 4;
        this.dy = (float)(rand.nextDouble() - 0.5) * 4;
        this.life  = rand.nextInt(20) + 10;
        this.color = new Color(1f, rand.nextInt(150) / 255f, 0f, 1f);
    }

    public void update() { x += dx; y += dy; life--; }

    public void draw(ShapeRenderer sr) {
        sr.setColor(color);
        sr.rect(x, y, 3, 3);
    }
}