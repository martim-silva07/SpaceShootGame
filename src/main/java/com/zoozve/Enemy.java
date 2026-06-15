package com.zoozve;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Enemy {
    public int x, y, type, size, enemyHp;
    public boolean alive = true;
    private int startX;
    private static Texture[] sprites = new Texture[3];

    public static void loadSprites() {
        try {
            sprites[0] = new Texture(Gdx.files.internal("images/enemy0.png"));
            sprites[1] = new Texture(Gdx.files.internal("images/enemy1.png"));
            sprites[2] = new Texture(Gdx.files.internal("images/enemy2.png"));
        } catch (Exception e) {
            System.out.println("Erro ao carregar sprites de inimigos: " + e.getMessage());
        }
    }

    public static void disposeSprites() {
        for (Texture t : sprites) if (t != null) t.dispose();
    }

    public Enemy(int x, int y, int type, int planet) {
        this.startX  = x;
        this.x       = x;
        this.y       = y;
        this.type    = type;
        this.size    = 30 + type * 5;
        this.enemyHp = (planet >= 8) ? 3 : 1;
    }

    public void update(int planet) {
        y += 2 + type;
        x = startX + (int)(50 * Math.sin(y * 0.03));
    }

    public void draw(SpriteBatch batch) {
        if (sprites[type] != null) batch.draw(sprites[type], x, y, size, size);
    }

    public void drawFallback(ShapeRenderer sr) {
        if (sprites[type] == null) {
            sr.setColor(Color.RED);
            sr.ellipse(x, y, size, size);
        }
    }

    public Rectangle getBounds() { return new Rectangle(x, y, size, size); }
    public boolean hit(Bullet b)  { return getBounds().overlaps(b.getBounds()); }
    public boolean hit(Player p)  { return getBounds().overlaps(new Rectangle(p.x, p.y, p.width, p.height)); }
}