package com.zoozve;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class HealShip {
    public int x, y;
    public boolean alive = true;
    public int width = 50, height = 50;
    private double waveTimer = 0;
    private static Texture sprite;

    public static void loadSprite() {
        try {
            sprite = new Texture(Gdx.files.internal("images/heal_ship.png"));
        } catch (Exception e) {
            System.out.println("Erro ao carregar heal_ship.png: " + e.getMessage());
        }
    }

    public static void disposeSprite() {
        if (sprite != null) sprite.dispose();
    }

    public HealShip(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        y += 2;
        waveTimer += 0.05;
        x += (int)(Math.sin(waveTimer) * 1.2);
    }

    public void draw(SpriteBatch batch) {
        if (sprite != null) batch.draw(sprite, x, y, width, height);
    }

    public void drawFallback(ShapeRenderer sr) {
        if (sprite == null) {
            sr.setColor(Color.GREEN);
            sr.rect(x, y, width, height);
        }
    }
}