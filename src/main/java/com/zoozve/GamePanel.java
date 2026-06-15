package com.zoozve;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends ApplicationAdapter implements InputProcessor {

    private static final int STATE_INTRO    = -1;
    private static final int STATE_COLONY   =  0;
    private static final int STATE_SPACE    =  1;
    private static final int STATE_GAMEOVER =  2;

    private SpriteBatch batch;
    private ShapeRenderer sr;
    private BitmapFont fontSm, fontMd, fontLg, fontXl;
    private GlyphLayout layout;
    private OrthographicCamera camera;

    private static final Color[] PLANET_COLORS = {
            new Color(25/255f, 25/255f, 30/255f, 1), new Color(45/255f, 35/255f, 20/255f, 1),
            new Color(10/255f, 20/255f, 45/255f, 1), new Color(50/255f, 15/255f, 10/255f, 1),
            new Color(45/255f, 30/255f, 25/255f, 1), new Color(40/255f, 40/255f, 30/255f, 1),
            new Color(15/255f, 40/255f, 45/255f, 1), new Color(10/255f, 15/255f, 55/255f, 1),
            new Color(40/255f, 20/255f, 50/255f, 1), new Color(20/255f, 45/255f, 40/255f, 1),
            new Color(35/255f, 35/255f, 35/255f, 1)
    };

    private Player player;
    private List<Bullet>         bullets    = new ArrayList<>();
    private List<Enemy>          enemies    = new ArrayList<>();
    private List<Coin>           coins      = new ArrayList<>();
    private List<Star>           stars      = new ArrayList<>();
    private List<Particle>       particles  = new ArrayList<>();
    private List<HealShip>       healShips  = new ArrayList<>();
    private List<HeartItem>      hearts     = new ArrayList<>();
    private List<WhiteLightning> lightnings = new ArrayList<>();
    private List<CropPlot>       plots      = new ArrayList<>();

    private int biomass = 0;
    private final Random rand = new Random();
    private int coinCount = 10, score = 0, hp = 3, maxHp = 3;
    private int speedLevel = 0, fireLevel = 0;
    private boolean shield = false;
    private int shieldTimer = 0;
    private final int SHIELD_MAX_DURATION = 400;
    private boolean shopOpen = false, seedShopOpen = false;
    private CropPlot activePlot = null;
    private int enemiesKilled = 0, currentPlanetIndex = 0;
    private final int KILLS_PER_LEVEL = 35;
    private boolean waitingForSystemChange = false, clearListsRequested = false;
    private int healShipHp = 10;
    private final int HEAL_SHIP_MAX_HP = 10;
    private int gameState = STATE_INTRO, introPage = 0;

    private final String[][] INTRO_TEXT = {
            {"ZOOZVE: LAST FARM OF EARTH","","In the year 524522, Earth is gone.","Not destroyed-harvested.","An alien swarm known as the Invaders descended,","stripping Earth of its cities, oceans, and atmosphere..."},
            {"Humanity didn't die... it escaped.","The last survivors fled to a strange refuge world","drifting at the edge of known space.","","524522 ZOOZVE","A planet so weird the Invaders didn't name it properly."},
            {"It's small. It's wild. But it breathes.","And more importantly... it can be cultivated.","So humanity did what it always does best:","They started farming again.","","THE TWIST: FARMING IS DEFENSE","Every crop you grow is powered by stolen alien tech."},
            {"But the more you grow, the more you signal your presence.","And now they're coming back.","","Grow your last home. Defend your last future.","Reclaim Earth - one wave at a time.","","[ PRESS SPACE TO LAND ON ZOOZVE ]"}
    };

    private final String[] PLANETS = {
            "MERCURY","VENUS","EARTH","MARS","JUPITER","SATURN","URANUS","NEPTUNE",
            "PROXIMA CENTAURI B","PROXIMA CENTAURI C","PROXIMA CENTAURI D"
    };

    // =========================================================
    // CREATE
    // =========================================================
    @Override
    public void create() {
        batch  = new SpriteBatch();
        sr     = new ShapeRenderer();
        sr.setAutoShapeType(true);
        layout = new GlyphLayout();

        camera = new OrthographicCamera();
        camera.setToOrtho(true, 900, 600);

        fontSm = new BitmapFont(true); fontSm.getData().setScale(0.85f);
        fontMd = new BitmapFont(true); fontMd.getData().setScale(1.1f);
        fontLg = new BitmapFont(true); fontLg.getData().setScale(1.8f);
        fontXl = new BitmapFont(true); fontXl.getData().setScale(3.2f);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Gdx.input.setInputProcessor(this);

        for (int i = 0; i < 120; i++)
            stars.add(new Star(rand.nextInt(900), rand.nextInt(600), rand.nextInt(3) + 1));

        for (int row = 0; row < 2; row++)
            for (int col = 0; col < 4; col++)
                plots.add(new CropPlot(200 + col * 130, 320 + row * 100));

        Enemy.loadSprites();
        HealShip.loadSprite();
        player = new Player(450, 500);
    }

    // =========================================================
    // UPDATE
    // =========================================================
    private void update() {
        if (gameState == STATE_INTRO) {
            for (Star s : stars) s.update();
        }
        else if (gameState == STATE_COLONY) {
            for (Star s : stars) s.update();
            if (seedShopOpen) return;
            player.update(speedLevel);
            for (CropPlot p : plots) p.update();
            if (player.y < 40) { gameState = STATE_SPACE; player.y = 500; }
        }
        else if (gameState == STATE_SPACE) {
            for (Star s : stars) s.update();
            if (shopOpen) return;

            if (clearListsRequested) { enemies.clear(); healShips.clear(); clearListsRequested = false; }

            player.update(speedLevel);
            if (shield) { shieldTimer--; if (shieldTimer <= 0) shield = false; }

            for (Bullet b : bullets) b.update();
            bullets.removeIf(b -> !b.active);

            for (Enemy en : enemies) en.update(currentPlanetIndex);
            enemies.removeIf(en -> !en.alive);

            for (HealShip hs : healShips) { hs.update(); if (hs.y > 650) hs.alive = false; }
            healShips.removeIf(hs -> !hs.alive);

            for (HeartItem h : hearts) h.update();
            hearts.removeIf(h -> h.collected || h.y > 620);

            for (Coin c : coins) c.update();
            coins.removeIf(c -> c.collected || c.y > 600);

            for (Particle p : particles) p.update();
            particles.removeIf(p -> p.life <= 0);

            for (WhiteLightning wl : lightnings) wl.update();
            lightnings.removeIf(wl -> wl.life <= 0);

            if (waitingForSystemChange && currentPlanetIndex == 7 && rand.nextInt(4) == 0)
                lightnings.add(new WhiteLightning(450, 300));

            spawnEnemies();
            spawnHealShip();
            handleCollisions();
        }
    }

    private void spawnEnemies() {
        if (waitingForSystemChange) return;
        int rate = Math.max(12, 40 - (score / 150) - (currentPlanetIndex * 4));
        if (rand.nextInt(rate) == 0)
            enemies.add(new Enemy(rand.nextInt(740) + 70, -30, rand.nextInt(3), currentPlanetIndex));
    }

    private void spawnHealShip() {
        if (waitingForSystemChange || !healShips.isEmpty()) return;
        if (rand.nextInt(800) == 0) {
            healShips.add(new HealShip(rand.nextInt(740) + 70, -50));
            healShipHp = HEAL_SHIP_MAX_HP;
        }
    }

    private void handleCollisions() {
        for (Enemy en : enemies) {
            for (Bullet b : bullets) {
                if (b.active && en.hit(b)) {
                    b.active = false;
                    en.enemyHp--;
                    if (en.enemyHp <= 0) {
                        en.alive = false;
                        score += (en.type + 1) * 10;
                        for (int p = 0; p < 8; p++)
                            particles.add(new Particle(en.x + en.size / 2, en.y + en.size / 2));
                        if (!waitingForSystemChange) {
                            enemiesKilled++;
                            if (enemiesKilled >= KILLS_PER_LEVEL) { waitingForSystemChange = true; clearListsRequested = true; }
                        }
                        coins.add(new Coin(en.x + 10, en.y + 10));
                    }
                }
            }
            if (en.alive && en.hit(player)) {
                en.alive = false;
                if (shield) shield = false;
                else { hp--; if (hp <= 0) { resetGame(); return; } }
            }
        }

        for (HealShip hs : healShips) {
            for (Bullet b : bullets) {
                if (b.active && hs.alive && b.x > hs.x && b.x < hs.x + hs.width && b.y > hs.y && b.y < hs.y + hs.height) {
                    b.active = false;
                    healShipHp--;
                    if (healShipHp <= 0) {
                        hs.alive = false;
                        hearts.add(new HeartItem(hs.x + hs.width / 2 - 10, hs.y + hs.height / 2));
                    }
                }
            }
        }

        for (HeartItem h : hearts)
            if (!h.collected && player.x + player.width > h.x && player.x < h.x + 20 && player.y + player.height > h.y && player.y < h.y + 20)
            { h.collected = true; if (hp < maxHp) hp++; }

        for (Coin c : coins)
            if (!c.collected && c.hit(player)) { c.collected = true; coinCount++; }
    }

    private void resetGame() {
        enemies.clear(); bullets.clear(); coins.clear(); particles.clear();
        healShips.clear(); hearts.clear(); lightnings.clear();
        coinCount = 5; score = 0; hp = 3; maxHp = 3;
        speedLevel = 0; fireLevel = 0; enemiesKilled = 0; currentPlanetIndex = 0;
        waitingForSystemChange = false; shield = false;
        player.x = 450; player.y = 500;
        seedShopOpen = false; activePlot = null;
        gameState = STATE_GAMEOVER;
    }

    // =========================================================
    // RENDER
    // =========================================================
    @Override
    public void render() {
        update();
        camera.update();
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        switch (gameState) {
            case STATE_INTRO    -> renderIntro();
            case STATE_COLONY   -> renderColony();
            case STATE_SPACE    -> renderSpace();
            case STATE_GAMEOVER -> renderGameOver();
        }
    }

    private void renderIntro() {
        sr.begin(ShapeType.Filled);
        sr.setColor(5/255f, 5/255f, 12/255f, 1f);
        sr.rect(0, 0, 900, 600);
        for (Star s : stars) s.draw(sr);
        sr.end();

        batch.begin();
        int startY = 140;
        for (int i = 0; i < INTRO_TEXT[introPage].length; i++) {
            String line = INTRO_TEXT[introPage][i];
            if (line.contains("ZOOZVE"))              fontMd.setColor(Color.YELLOW);
            else if (line.contains("TWIST") || line.contains("INVADERS")) fontMd.setColor(Color.RED);
            else                                       fontMd.setColor(Color.LIGHT_GRAY);
            fontMd.draw(batch, line, 70, startY + i * 35);
        }
        if (introPage < INTRO_TEXT.length - 1) {
            fontSm.setColor(Color.GRAY);
            fontSm.draw(batch, "[ Press SPACE to continue ]", 330, 555);
        }
        batch.end();
    }

    private void renderColony() {
        sr.begin(ShapeType.Filled);
        sr.setColor(35/255f, 24/255f, 17/255f, 1f);
        sr.rect(0, 0, 900, 600);
        sr.setColor(50/255f, 36/255f, 26/255f, 1f);
        for (int i = 0; i < 8; i++) sr.ellipse(i * 140, (i % 2) * 200 + 100, 120, 45);
        for (CropPlot p : plots) p.draw(sr);
        sr.setColor(0, 1f, 140/255f, 40/255f);
        sr.rect(320, 0, 260, 50);
        sr.end();

        sr.begin(ShapeType.Line);
        sr.setColor(Color.GREEN);
        sr.rect(320, 0, 260, 50);
        if (shield) { sr.setColor(Color.BLUE); sr.ellipse(player.x - 5, player.y - 5, player.width + 10, player.height + 10); }
        sr.end();

        batch.begin();
        Matrix4 oldMatrixColony = batch.getTransformMatrix().cpy();
        Matrix4 flipMatrixPlayer = oldMatrixColony.cpy();
        float pCx = player.x + player.width / 2f;
        float pCy = player.y + player.height / 2f;
        flipMatrixPlayer.translate(pCx, pCy, 0);
        flipMatrixPlayer.scale(1, -1, 1);
        flipMatrixPlayer.translate(-pCx, -pCy, 0);
        batch.setTransformMatrix(flipMatrixPlayer);
        player.draw(batch);
        batch.setTransformMatrix(oldMatrixColony);

        fontLg.setColor(Color.WHITE);
        fontLg.draw(batch, "ZOOZVE COLONY CORE", 25, 40);
        fontMd.setColor(Color.YELLOW);
        fontMd.draw(batch, "COINS: " + coinCount, 25, 70);
        fontMd.setColor(0, 1f, 120/255f, 1f);
        fontMd.draw(batch, "BIOMASS: " + biomass, 25, 95);
        fontSm.setColor(Color.GREEN);
        fontSm.draw(batch, "LAUNCH TO SPACE (FLY HERE)", 360, 30);
        fontSm.setColor(Color.LIGHT_GRAY);
        fontSm.draw(batch, "Move over a plot and press F to plant  |  Neon green = ready to harvest (F)", 150, 565);
        batch.end();

        if (seedShopOpen) renderSeedShop();
    }

    private void renderSpace() {
        sr.begin(ShapeType.Filled);
        sr.setColor(PLANET_COLORS[currentPlanetIndex]);
        sr.rect(0, 0, 900, 600);
        for (Star s : stars) s.draw(sr);
        drawSpaceAnomalyShapes();
        for (WhiteLightning wl : lightnings) wl.draw(sr);
        for (Coin c : coins) c.draw(sr);
        for (HeartItem h : hearts) h.draw(sr);
        for (Particle p : particles) p.draw(sr);
        for (Bullet b : bullets) b.draw(sr);
        for (Enemy en : enemies) en.drawFallback(sr);
        for (HealShip hs : healShips) hs.drawFallback(sr);
        if (shield) {
            sr.set(ShapeType.Line);
            sr.setColor(Color.BLUE);
            sr.ellipse(player.x - 5, player.y - 5, player.width + 10, player.height + 10);
            sr.set(ShapeType.Filled);
        }
        sr.end();

        batch.begin();
        Matrix4 oldMatrixSpace = batch.getTransformMatrix().cpy();

        for (HealShip hs : healShips) {
            Matrix4 flipMatrix = oldMatrixSpace.cpy();
            float cx = hs.x + hs.width / 2f;
            float cy = hs.y + hs.height / 2f;
            flipMatrix.translate(cx, cy, 0);
            flipMatrix.scale(1, -1, 1);
            flipMatrix.translate(-cx, -cy, 0);
            batch.setTransformMatrix(flipMatrix);
            hs.draw(batch);
        }

        for (Enemy en : enemies) {
            Matrix4 flipMatrix = oldMatrixSpace.cpy();
            float cx = en.x + en.size / 2f;
            float cy = en.y + en.size / 2f;
            flipMatrix.translate(cx, cy, 0);
            flipMatrix.scale(1, -1, 1);
            flipMatrix.translate(-cx, -cy, 0);
            batch.setTransformMatrix(flipMatrix);
            en.draw(batch);
        }

        Matrix4 flipMatrixPlayerSpace = oldMatrixSpace.cpy();
        float pCxSpace = player.x + player.width / 2f;
        float pCySpace = player.y + player.height / 2f;
        flipMatrixPlayerSpace.translate(pCxSpace, pCySpace, 0);
        flipMatrixPlayerSpace.scale(1, -1, 1);
        flipMatrixPlayerSpace.translate(-pCxSpace, -pCySpace, 0);
        batch.setTransformMatrix(flipMatrixPlayerSpace);
        player.draw(batch);

        batch.setTransformMatrix(oldMatrixSpace);
        batch.end();

        sr.begin(ShapeType.Filled);
        for (HealShip hs : healShips) {
            sr.setColor(Color.RED);   sr.rect(hs.x, hs.y - 10, hs.width, 5);
            sr.setColor(Color.GREEN); sr.rect(hs.x, hs.y - 10, (int)(hs.width * ((float)healShipHp / HEAL_SHIP_MAX_HP)), 5);
        }
        sr.setColor(0, 0, 0, 220/255f); sr.rect(0, 0, 900, 55);
        for (int i = 0; i < maxHp; i++) {
            sr.setColor(i < hp ? Color.RED : Color.DARK_GRAY);
            sr.rect(55 + i * 22, 20, 14, 14);
        }
        int pBarX = 390;
        sr.setColor(40/255f, 40/255f, 50/255f, 1f); sr.rect(pBarX, 32, 120, 6);
        sr.setColor(Color.CYAN);
        sr.rect(pBarX, 32, (int)(120 * Math.min(1.0, (double)enemiesKilled / KILLS_PER_LEVEL)), 6);
        if (shield) {
            sr.setColor(Color.DARK_GRAY); sr.rect(580, 30, 65, 8);
            sr.setColor(1f, 50/255f, 100/255f, 1f);
            sr.rect(580, 30, (int)(65.0 * shieldTimer / SHIELD_MAX_DURATION), 8);
        }
        sr.end();

        batch.begin();
        fontMd.setColor(Color.RED);    fontMd.draw(batch, "HP:", 20, 32);
        fontMd.setColor(Color.WHITE);  fontMd.draw(batch, "SCORE: " + score, 160, 32);
        fontMd.setColor(Color.YELLOW); fontMd.draw(batch, "COINS: " + coinCount, 290, 32);
        fontMd.setColor(Color.CYAN);
        String pName = PLANETS[currentPlanetIndex];
        layout.setText(fontMd, pName);
        fontMd.draw(batch, pName, 450 - layout.width / 2f, 24);
        if (shield) { fontSm.setColor(1f, 50/255f, 100/255f, 1f); fontSm.draw(batch, "SHIELD", 580, 25); }
        fontMd.setColor(Color.MAGENTA);
        fontMd.draw(batch, "SPD: Lvl " + speedLevel, 660, 32);
        fontMd.draw(batch, "GUN: Lvl " + fireLevel,  775, 32);
        drawSpaceAnomalyText();
        batch.end();

        if (shopOpen) renderShop();
    }

    private void renderGameOver() {
        sr.begin(ShapeType.Filled);
        sr.setColor(5/255f, 5/255f, 12/255f, 1f); sr.rect(0, 0, 900, 600);
        for (Star s : stars) s.draw(sr);
        sr.end();

        batch.begin();
        fontXl.setColor(Color.RED);
        String msg = "GAME OVER";
        layout.setText(fontXl, msg);
        fontXl.draw(batch, msg, (900 - layout.width) / 2f, 245);
        fontLg.setColor(Color.LIGHT_GRAY);
        String sub = "[ PRESS R TO RESTART ]";
        layout.setText(fontLg, sub);
        fontLg.draw(batch, sub, (900 - layout.width) / 2f, 330);
        batch.end();
    }

    private void drawSpaceAnomalyShapes() {
        int cx = 450, cy = 300;
        int base = 40 + currentPlanetIndex * 12;
        boolean isTransition = waitingForSystemChange && currentPlanetIndex == 7;
        for (int r = base + 60; r > base; r -= 4) {
            float alpha = (float)(110 * Math.exp(-(r - base) * 0.04)) / 255f;
            if (isTransition) sr.setColor(240/255f, 245/255f, 1f, alpha);
            else              sr.setColor(130/255f, 50/255f, 250/255f, alpha);
            sr.ellipse(cx - r / 2f, cy - r / 2f, r, r);
        }
        sr.setColor(isTransition ? Color.WHITE : new Color(5/255f, 5/255f, 10/255f, 1f));
        sr.ellipse(cx - base / 2f, cy - base / 2f, base, base);
    }

    private void drawSpaceAnomalyText() {
        int cx = 450, cy = 300, base = 40 + currentPlanetIndex * 12;
        double dist = Math.sqrt(Math.pow(player.x + 20 - cx, 2) + Math.pow(player.y + 20 - cy, 2));
        if (dist <= base / 2.0 + 40) {
            fontMd.setColor(0, 1f, 150/255f, 1f);
            String msg;
            if (waitingForSystemChange)
                msg = (currentPlanetIndex == 7) ? "[ Q - JUMP TO CENTAURI SYSTEM ]" : "[ Q - WARP TO NEXT PLANET ]";
            else
                msg = "[ Q - RETURN TO COLONY ]";
            layout.setText(fontMd, msg);
            fontMd.draw(batch, msg, (900 - layout.width) / 2f, 385);
        }
        if (waitingForSystemChange) {
            fontLg.setColor(Color.ORANGE);
            String msg = (currentPlanetIndex == 7) ? "SOLAR SYSTEM CLEARED!" : "PLANET CLEARED!";
            layout.setText(fontLg, msg);
            fontLg.draw(batch, msg, (900 - layout.width) / 2f, 95);
        }
    }

    private void renderSeedShop() {
        sr.begin(ShapeType.Filled);
        sr.setColor(10/255f, 28/255f, 18/255f, 240/255f); sr.rect(100, 80, 700, 440);
        sr.setColor(0, 1f, 130/255f, 60/255f);
        sr.rect(140, 195, 620, 1); sr.rect(140, 460, 620, 1);
        sr.end();
        sr.begin(ShapeType.Line);
        sr.setColor(0, 1f, 130/255f, 200/255f); sr.rect(100, 80, 700, 440);
        sr.end();

        batch.begin();
        fontXl.setColor(0, 1f, 140/255f, 1f);
        String title = "== BIO-SEED LAB ==";
        layout.setText(fontXl, title); fontXl.draw(batch, title, (900 - layout.width) / 2f, 135);
        fontMd.setColor(Color.YELLOW);
        String funds = "AVAILABLE FUNDS: " + coinCount + " CASH";
        layout.setText(fontMd, funds); fontMd.draw(batch, funds, (900 - layout.width) / 2f, 175);
        drawSeedRow(230, "1", "TERRA CARROT SEEDS",      "Standard earth crop. Yields basic biomass.", "2 COINS", true);
        drawSeedRow(305, "2", "COSMIC BIO-REAGENT",      "[ LOCKED - FUTURE UPGRADE ]",                "5 COINS", false);
        drawSeedRow(380, "3", "QUANTUM SUNFLOWER SEEDS", "[ LOCKED - FUTURE UPGRADE ]",                "10 COINS",false);
        fontMd.setColor(Color.LIGHT_GRAY);
        String exit = "[ PRESS F OR E TO EXIT ]";
        layout.setText(fontMd, exit); fontMd.draw(batch, exit, (900 - layout.width) / 2f, 495);
        batch.end();
    }

    private void drawSeedRow(int y, String key, String name, String desc, String cost, boolean available) {
        fontMd.setColor(available ? Color.WHITE : Color.GRAY);
        fontMd.draw(batch, key + "  " + name, 205, y + 26);
        fontSm.setColor(Color.LIGHT_GRAY); fontSm.draw(batch, desc, 205, y + 44);
        fontMd.setColor(available ? Color.YELLOW : Color.DARK_GRAY);
        fontMd.draw(batch, "COST: " + cost, 595, y + 35);
    }

    private void renderShop() {
        sr.begin(ShapeType.Filled);
        sr.setColor(10/255f, 15/255f, 30/255f, 240/255f); sr.rect(100, 80, 700, 440);
        sr.setColor(1f, 1f, 1f, 6/255f);
        for (int y = 85; y < 515; y += 3) sr.rect(105, y, 690, 1);
        for (int y : new int[]{230, 305, 380}) { sr.setColor(1f, 1f, 1f, 12/255f); sr.rect(140, y, 620, 58); }
        for (int i = 0; i < 5; i++) {
            sr.setColor(i < speedLevel ? new Color(0, 1f, 150/255f, 1f) : new Color(60/255f, 60/255f, 70/255f, 1f));
            sr.rect(205 + i * 24, 266, 18, 10);
        }
        for (int i = 0; i < 3; i++) {
            sr.setColor(i < fireLevel ? new Color(1f, 100/255f, 0, 1f) : new Color(60/255f, 60/255f, 70/255f, 1f));
            sr.rect(205 + i * 24, 341, 18, 10);
        }
        sr.setColor(0, 220/255f, 1f, 80/255f);
        sr.rect(140, 195, 620, 1); sr.rect(140, 460, 620, 1);
        sr.end();

        sr.begin(ShapeType.Line);
        sr.setColor(0, 220/255f, 1f, 200/255f); sr.rect(100, 80, 700, 440);
        sr.setColor(0, 1f, 150/255f, 1f);    sr.rect(155, 244, 30, 30);
        sr.setColor(1f, 100/255f, 0, 1f);    sr.rect(155, 319, 30, 30);
        sr.setColor(1f, 50/255f, 100/255f, 1f); sr.rect(155, 394, 30, 30);
        sr.end();

        batch.begin();
        fontXl.setColor(0, 1f, 200/255f, 1f);
        String title = "== METROPOLIS MARKET ==";
        layout.setText(fontXl, title); fontXl.draw(batch, title, (900 - layout.width) / 2f, 135);
        fontMd.setColor(Color.YELLOW);
        String credits = "CREDITS SYNCED: " + coinCount + " CASH";
        layout.setText(fontMd, credits); fontMd.draw(batch, credits, (900 - layout.width) / 2f, 175);

        fontMd.setColor(Color.WHITE); fontMd.draw(batch, "1  HYPER ENGINE UPGRADE", 205, 256);
        if (speedLevel >= 5) { fontMd.setColor(Color.DARK_GRAY); fontMd.draw(batch, "[MAXED]", 640, 265); }
        else { fontMd.setColor(Color.YELLOW); fontMd.draw(batch, "COST: 10 COINS", 595, 265); }

        fontMd.setColor(Color.WHITE); fontMd.draw(batch, "2  PLASMA DISRUPTOR GUN", 205, 331);
        if (fireLevel >= 3) { fontMd.setColor(Color.DARK_GRAY); fontMd.draw(batch, "[MAXED]", 640, 340); }
        else { fontMd.setColor(Color.YELLOW); fontMd.draw(batch, "COST: 15 COINS", 595, 340); }

        String shieldStatus = shield ? "[ACTIVE]" : "READY TO DEPLOY";
        fontMd.setColor(Color.WHITE); fontMd.draw(batch, "3  ENERGY DEFLECTOR SHIELD (" + shieldStatus + ")", 205, 406);
        fontSm.setColor(Color.LIGHT_GRAY); fontSm.draw(batch, "Provides full immunity to next enemy impacts", 205, 424);
        fontMd.setColor(shield ? Color.DARK_GRAY : Color.YELLOW); fontMd.draw(batch, "COST: 5 COINS", 595, 415);

        fontMd.setColor(1f, 60/255f, 60/255f, 1f);
        String exit = "[ PRESS E TO RETURN TO COMBAT ]";
        layout.setText(fontMd, exit); fontMd.draw(batch, exit, (900 - layout.width) / 2f, 495);
        batch.end();
    }

    // =========================================================
    // INPUT
    // =========================================================
    @Override
    public boolean keyDown(int k) {
        // ✨ NOVO: Lógica da tecla "P" para Fullscreen / Windowed
        if (k == Keys.P) {
            if (Gdx.graphics.isFullscreen()) {
                // Volta para o modo janela padrão
                Gdx.graphics.setWindowedMode(900, 600);
            } else {
                // Obtém a resolução nativa do monitor atual e ativa o Fullscreen
                DisplayMode currentMode = Gdx.graphics.getDisplayMode();
                Gdx.graphics.setFullscreenMode(currentMode);
            }
            return true;
        }

        if (gameState == STATE_GAMEOVER) {
            if (k == Keys.R) { resetGame(); gameState = STATE_COLONY; }
            return true;
        }
        if (gameState == STATE_INTRO) {
            if (k == Keys.SPACE) {
                if (introPage < INTRO_TEXT.length - 1) introPage++;
                else { gameState = STATE_COLONY; player.x = 450; player.y = 500; }
            }
            return true;
        }
        if (k == Keys.E) {
            if (gameState == STATE_SPACE)  shopOpen = !shopOpen;
            if (gameState == STATE_COLONY && seedShopOpen) { seedShopOpen = false; activePlot = null; }
            return true;
        }
        if (gameState == STATE_COLONY && seedShopOpen) {
            if (k == Keys.F) { seedShopOpen = false; activePlot = null; return true; }
            if (k == Keys.NUM_1 || k == Keys.NUMPAD_1) {
                if (coinCount >= 2 && activePlot != null && activePlot.state == 0)
                { coinCount -= 2; activePlot.plant(); seedShopOpen = false; activePlot = null; }
            }
            return true;
        }
        if (gameState == STATE_SPACE && k == Keys.Q) {
            int base = 40 + currentPlanetIndex * 12;
            double dist = Math.sqrt(Math.pow(player.x + 20 - 450, 2) + Math.pow(player.y + 20 - 300, 2));
            if (dist <= base / 2.0 + 40) {
                if (waitingForSystemChange) {
                    enemiesKilled = 0; waitingForSystemChange = false;
                    if (currentPlanetIndex < PLANETS.length - 1) currentPlanetIndex++;
                    player.x = 450; player.y = 500;
                    enemies.clear(); bullets.clear(); healShips.clear();
                } else {
                    gameState = STATE_COLONY; player.x = 450; player.y = 500; shopOpen = false;
                }
            }
            return true;
        }
        if (shopOpen && gameState == STATE_SPACE) {
            if ((k == Keys.NUM_1 || k == Keys.NUMPAD_1) && coinCount >= 10 && speedLevel < 5) { coinCount -= 10; speedLevel++; }
            if ((k == Keys.NUM_2 || k == Keys.NUMPAD_2) && coinCount >= 15 && fireLevel < 3)  { coinCount -= 15; fireLevel++; }
            if ((k == Keys.NUM_3 || k == Keys.NUMPAD_3) && coinCount >= 5  && !shield) { coinCount -= 5; shield = true; shieldTimer = SHIELD_MAX_DURATION; }
            return true;
        }
        if (gameState == STATE_COLONY && k == Keys.F) {
            for (CropPlot p : plots) {
                if (p.isPlayerNear(player)) {
                    if (p.state == 0) { activePlot = p; seedShopOpen = true; }
                    else if (p.state == 3) { p.harvest(); biomass += 10; score += 15; }
                }
            }
            return true;
        }
        if (gameState == STATE_SPACE && k == Keys.SPACE) {
            int shots = 1 + fireLevel;
            for (int i = 0; i < shots; i++)
                bullets.add(new Bullet(player.x + 20 + i * 14 - (shots - 1) * 7, player.y));
            return true;
        }
        if (k == Keys.A || k == Keys.LEFT)  player.left  = true;
        if (k == Keys.D || k == Keys.RIGHT) player.right = true;
        if (k == Keys.W || k == Keys.UP)    player.up    = true;
        if (k == Keys.S || k == Keys.DOWN)  player.down  = true;
        return false;
    }

    @Override
    public boolean keyUp(int k) {
        if (k == Keys.A || k == Keys.LEFT)  player.left  = false;
        if (k == Keys.D || k == Keys.RIGHT) player.right = false;
        if (k == Keys.W || k == Keys.UP)    player.up    = false;
        if (k == Keys.S || k == Keys.DOWN)  player.down  = false;
        return false;
    }

    @Override public boolean keyTyped(char c)                        { return false; }
    @Override public boolean touchDown(int x,int y,int p,int b)     { return false; }
    @Override public boolean touchUp(int x,int y,int p,int b)       { return false; }
    @Override public boolean touchCancelled(int x,int y,int p,int b){ return false; }
    @Override public boolean touchDragged(int x,int y,int p)        { return false; }
    @Override public boolean mouseMoved(int x,int y)                { return false; }
    @Override public boolean scrolled(float x,float y)              { return false; }

    // ✨ ALTERADO: Quando a janela muda de tamanho (mudar para fullscreen), garante que o jogo continua a usar a resolução virtual correta.
    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(true, 900, 600);
    }

    // =========================================================
    // DISPOSE
    // =========================================================
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (sr    != null) sr.dispose();
        if (fontSm != null) fontSm.dispose();
        if (fontMd != null) fontMd.dispose();
        if (fontLg != null) fontLg.dispose();
        if (fontXl != null) fontXl.dispose();
        if (player != null) player.dispose();
        Enemy.disposeSprites();
        HealShip.disposeSprite();
    }
}