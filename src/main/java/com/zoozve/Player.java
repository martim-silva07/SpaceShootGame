package com.zoozve;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player {
    public int x, y, width = 44, height = 40;
    public boolean left, right, up, down;
    private Texture sprite;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        try {
            sprite = new Texture(Gdx.files.internal("images/player.png"));
        } catch (Exception e) {
            System.out.println("Erro ao carregar player.png: " + e.getMessage());
        }
    }

    public void update(int speedLevel) {
        int speed = 5 + speedLevel;
        int dx = 0, dy = 0;
        if (left)  dx -= speed;
        if (right) dx += speed;
        if (up)    dy -= speed;
        if (down)  dy += speed;

        // Fix velocidade diagonal
        if (dx != 0 && dy != 0) {
            dx = (int)(dx * 0.707f);
            dy = (int)(dy * 0.707f);
        }
        x = Math.max(0, Math.min(850, x + dx));
        y = Math.max(10, Math.min(530, y + dy));
    }

    public void draw(SpriteBatch batch) {
        if (sprite != null) batch.draw(sprite, x, y, width, height);
    }

    public void dispose() {
        if (sprite != null) sprite.dispose();
    }
}