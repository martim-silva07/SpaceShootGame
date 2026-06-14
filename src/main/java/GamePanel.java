import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    // Estados do jogo como constantes (sem magic numbers)
    private static final int STATE_INTRO    = -1;
    private static final int STATE_COLONY   =  0;
    private static final int STATE_SPACE    =  1;
    private static final int STATE_GAMEOVER =  2;

    private javax.swing.Timer timer;
    private Player player;
    private List<Bullet> bullets = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Coin> coins = new ArrayList<>();
    private List<Star> stars = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();
    private List<HealShip> healShips = new ArrayList<>();
    private List<HeartItem> hearts = new ArrayList<>();
    private List<WhiteLightning> lightnings = new ArrayList<>();

    private List<CropPlot> plots = new ArrayList<>();
    private int biomass = 0;

    private Random rand = new Random();
    private int coinCount = 10;
    private int score = 0;
    private int hp = 3, maxHp = 3;
    private int speedLevel = 0, fireLevel = 0;
    private boolean shield = false;
    private int shieldTimer = 0;
    private final int SHIELD_MAX_DURATION = 400;

    private boolean shopOpen = false;
    private boolean seedShopOpen = false;
    private CropPlot activePlot = null;

    private int enemiesKilled = 0;
    private int currentPlanetIndex = 0;
    private final int KILLS_PER_LEVEL = 35;
    private boolean waitingForSystemChange = false;
    private boolean clearListsRequested = false;

    private int healShipHp = 10;
    private final int HEAL_SHIP_MAX_HP = 10;

    private int gameState = STATE_INTRO;
    private int introPage = 0;

    private final String[][] INTRO_TEXT = {
            {"ZO0ZVE: LAST FARM OF EARTH", "", "In the year 524522, Earth is gone.", "Not destroyed—harvested.", "An alien swarm known as the Invaders descended,", "stripping Earth of its cities, oceans, and atmosphere..."},
            {"Humanity didn't die... it escaped.", "The last survivors fled to a strange refuge world", "drifting at the edge of known space:", "", "524522 ZOOZVE", "A planet so weird the Invaders didn't name it properly."},
            {"It's small. It's wild. But it breathes.", "And more importantly... it can be cultivated.", "So humanity did what it always does best:", "They started farming again.", "", "THE TWIST: FARMING IS DEFENSE", "Every crop you grow is powered by stolen alien tech."},
            {"But the more you grow, the more you signal your presence.", "And now they're coming back.", "", "\"Grow your last home. Defend your last future.", "Reclaim Earth—one wave at a time.\"", "", "[ PRESS SPACE TO LAND ON ZOOZVE ]"}
    };

    private final String[] PLANETS = {
            "MERCURY", "VENUS", "EARTH", "MARS", "JUPITER", "SATURN", "URANUS", "NEPTUNE",
            "PROXIMA CENTAURI b", "PROXIMA CENTAURI c", "PROXIMA CENTAURI d"
    };
    private final Color[] PLANET_COLORS = {
            new Color(25, 25, 30), new Color(45, 35, 20), new Color(10, 20, 45),
            new Color(50, 15, 10), new Color(45, 30, 25), new Color(40, 40, 30),
            new Color(15, 40, 45), new Color(10, 15, 55), new Color(40, 20, 50),
            new Color(20, 45, 40), new Color(35, 35, 35)
    };

    public GamePanel() {
        setPreferredSize(new Dimension(900, 600));
        setFocusable(true);
        addKeyListener(this);

        // FIX: removido parâmetro speed desnecessário do Star
        for (int i = 0; i < 120; i++) {
            stars.add(new Star(rand.nextInt(900), rand.nextInt(600), rand.nextInt(3) + 1));
        }

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                plots.add(new CropPlot(200 + col * 130, 320 + row * 100));
            }
        }

        Enemy.loadSprites();
        player = new Player(450, 500);
        timer = new javax.swing.Timer(16, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    private void update() {
        if (gameState == STATE_INTRO) {
            for (Star s : stars) s.update();
        }
        else if (gameState == STATE_COLONY) {
            for (Star s : stars) s.update();
            if (seedShopOpen) return;

            player.update(speedLevel);
            for (CropPlot p : plots) p.update();

            if (player.y < 40) {
                gameState = STATE_SPACE;
                player.y = 500;
            }
        }
        else if (gameState == STATE_SPACE) {
            for (Star s : stars) s.update();

            if (shopOpen) return;

            if (clearListsRequested) {
                enemies.clear();
                healShips.clear();
                clearListsRequested = false;
            }

            player.update(speedLevel);

            if (shield) { shieldTimer--; if (shieldTimer <= 0) shield = false; }

            for (Bullet b : bullets) b.update(); bullets.removeIf(b -> !b.active);
            for (Enemy en : enemies) en.update(currentPlanetIndex); enemies.removeIf(en -> !en.alive);

            for (HealShip hs : healShips) {
                hs.update();
                if (hs.y > 650) hs.alive = false;
            }
            healShips.removeIf(hs -> !hs.alive);

            for (HeartItem h : hearts) h.update();
            // FIX: hearts agora também são removidos quando saem do ecrã (sem leak de memória)
            hearts.removeIf(h -> h.collected || h.y > 620);

            // FIX: movimento da coin movido para Coin.update()
            for (Coin c : coins) c.update();
            coins.removeIf(c -> c.collected || c.y > 600);

            for (Particle p : particles) p.update(); particles.removeIf(p -> p.life <= 0);
            for (WhiteLightning wl : lightnings) wl.update(); lightnings.removeIf(wl -> wl.life <= 0);

            if (waitingForSystemChange && currentPlanetIndex == 7) {
                if (rand.nextInt(4) == 0) {
                    lightnings.add(new WhiteLightning(450, 300));
                }
            }

            spawnEnemies();
            spawnHealShip();
            handleCollisions();
        }
    }

    private void spawnEnemies() {
        if (waitingForSystemChange) return;
        int sampleRate = Math.max(12, 40 - (score / 150) - (currentPlanetIndex * 4));
        if (rand.nextInt(sampleRate) == 0) {
            enemies.add(new Enemy(rand.nextInt(740) + 70, -30, rand.nextInt(3), currentPlanetIndex));
        }
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
                    b.active = false; en.enemyHp--;
                    if (en.enemyHp <= 0) {
                        en.alive = false; score += (en.type + 1) * 10;
                        // FIX: partículas agora são spawned ao matar inimigos
                        for (int p = 0; p < 8; p++)
                            particles.add(new Particle(en.x + en.size / 2, en.y + en.size / 2));
                        if (!waitingForSystemChange) {
                            enemiesKilled++;
                            if (enemiesKilled >= KILLS_PER_LEVEL) {
                                waitingForSystemChange = true;
                                clearListsRequested = true;
                            }
                        }
                        coins.add(new Coin(en.x + 10, en.y + 10));
                    }
                }
            }
            if (en.alive && en.hit(player)) {
                en.alive = false;
                if (shield) shield = false; else { hp--; if (hp <= 0) { resetGame(); return; } }
            }
        }

        for (HealShip hs : healShips) {
            for (Bullet b : bullets) {
                if (b.active && hs.alive && b.x > hs.x && b.x < hs.x + hs.width && b.y > hs.y && b.y < hs.y + hs.height) {
                    b.active = false;
                    healShipHp--;
                    if (healShipHp <= 0) {
                        hs.alive = false;
                        hearts.add(new HeartItem(hs.x + (hs.width / 2) - 10, hs.y + (hs.height / 2)));
                    }
                }
            }
        }

        for (HeartItem h : hearts) {
            if (!h.collected && player.x + player.width > h.x && player.x < h.x + 20 && player.y + player.height > h.y && player.y < h.y + 20) {
                h.collected = true;
                if (hp < maxHp) hp++;
            }
        }

        for (Coin c : coins) { if (!c.collected && c.hit(player)) { c.collected = true; coinCount++; } }
    }

    private void resetGame() {
        enemies.clear(); bullets.clear(); coins.clear(); particles.clear(); healShips.clear(); hearts.clear(); lightnings.clear();
        coinCount = 5; score = 0; hp = 3; maxHp = 3; speedLevel = 0; fireLevel = 0; enemiesKilled = 0; currentPlanetIndex = 0;
        waitingForSystemChange = false; shield = false; player.x = 450; player.y = 500;
        seedShopOpen = false; activePlot = null;
        // FIX: vai para ecrã de Game Over em vez de reiniciar imediatamente
        gameState = STATE_GAMEOVER;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (gameState == STATE_INTRO) {
            g2.setColor(new Color(5, 5, 12));
            g2.fillRect(0, 0, 900, 600);
            for (Star s : stars) s.draw(g2);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            int startY = 140;
            for (int i = 0; i < INTRO_TEXT[introPage].length; i++) {
                String line = INTRO_TEXT[introPage][i];
                if (line.contains("ZOOZVE") || line.contains("TITLE")) g2.setColor(Color.YELLOW);
                else if (line.contains("TWIST") || line.contains("INVADERS")) g2.setColor(Color.RED);
                else g2.setColor(Color.LIGHT_GRAY);
                g2.drawString(line, (900 - g2.getFontMetrics().stringWidth(line)) / 2, startY + (i * 35));
            }
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 13)); g2.setColor(Color.GRAY);
            if (introPage < INTRO_TEXT.length - 1) g2.drawString("[ Press SPACE to continue reading ]", 350, 540);
        }
        else if (gameState == STATE_COLONY) {
            g2.setColor(new Color(35, 24, 17));
            g2.fillRect(0, 0, 900, 600);

            g2.setColor(new Color(50, 36, 26));
            for (int i = 0; i < 8; i++) g2.fillOval(i * 140, (i % 2) * 200 + 100, 120, 45);

            for (CropPlot p : plots) p.draw(g2);

            g2.setColor(new Color(0, 255, 140, 40)); g2.fillRect(320, 0, 260, 50);
            g2.setColor(Color.GREEN); g2.drawRect(320, 0, 260, 50);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12)); g2.setColor(Color.GREEN);
            g2.drawString("LAUNCH TO SPACE (FLY HERE)", 355, 30);

            player.draw(g2, shield);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 18)); g2.setColor(Color.WHITE);
            g2.drawString("ZOOZVE COLONY CORE", 25, 40);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.setColor(Color.YELLOW); g2.drawString("COINS: " + coinCount, 25, 70);
            g2.setColor(new Color(0, 255, 120)); g2.drawString("BIOMASS: " + biomass, 25, 95);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12)); g2.setColor(Color.LIGHT_GRAY);
            g2.drawString("Move over a brown plot and press 'F' to manage Agriculture Menu.", 260, 540);
            g2.drawString("Mature plants glow neon green! Press 'F' to harvest Biomass.", 280, 565);

            if (seedShopOpen) drawSeedShop(g2);
        }
        else if (gameState == STATE_SPACE) {
            g2.setColor(PLANET_COLORS[currentPlanetIndex]);
            g2.fillRect(0, 0, 900, 600);

            for (Star s : stars) s.draw(g2);
            drawSpaceAnomaly(g2);

            for (WhiteLightning wl : lightnings) wl.draw(g2);
            for (Coin c : coins) c.draw(g2);

            for (HealShip hs : healShips) {
                hs.draw(g2);
                g2.setColor(Color.RED);
                g2.fillRect(hs.x, hs.y - 10, hs.width, 5);
                g2.setColor(Color.GREEN);
                int barWidth = (int) (hs.width * ((double) healShipHp / HEAL_SHIP_MAX_HP));
                g2.fillRect(hs.x, hs.y - 10, barWidth, 5);
            }

            for (HeartItem h : hearts) h.draw(g2);
            // FIX: partículas agora são desenhadas
            for (Particle p : particles) p.draw(g2);
            // FIX: usa Bullet.draw() em vez de código duplicado
            for (Bullet b : bullets) b.draw(g2);

            for (Enemy e : enemies) e.draw(g2);
            player.draw(g2, shield);

            drawUI(g2);

            if (shopOpen) drawShop(g2);
        }
        // FIX: novo ecrã de Game Over
        else if (gameState == STATE_GAMEOVER) {
            g2.setColor(new Color(5, 5, 12));
            g2.fillRect(0, 0, 900, 600);
            for (Star s : stars) s.draw(g2);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 64));
            g2.setColor(Color.RED);
            String msg = "GAME OVER";
            g2.drawString(msg, (900 - g2.getFontMetrics().stringWidth(msg)) / 2, 260);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            g2.setColor(Color.LIGHT_GRAY);
            String sub = "[ PRESS SPACE TO RESTART ]";
            g2.drawString(sub, (900 - g2.getFontMetrics().stringWidth(sub)) / 2, 330);
        }
    }

    private void drawSpaceAnomaly(Graphics2D g2) {
        int cx = 450, cy = 300;
        int baseSize = 40 + (currentPlanetIndex * 12);
        boolean isSystemTransition = waitingForSystemChange && currentPlanetIndex == 7;

        for (int r = baseSize + 60; r > baseSize; r -= 4) {
            int alpha = (int) (110 * Math.exp(-(r - baseSize) * 0.04));
            if (isSystemTransition) {
                g2.setColor(new Color(240, 245, 255, alpha));
            } else {
                g2.setColor(new Color(130, 50, 250, alpha));
            }
            g2.fillOval(cx - r / 2, cy - r / 2, r, r);
        }

        if (isSystemTransition) {
            g2.setColor(Color.WHITE);
        } else {
            g2.setColor(new Color(5, 5, 10));
        }
        g2.fillOval(cx - baseSize / 2, cy - baseSize / 2, baseSize, baseSize);

        int px = player.x + 20;
        int py = player.y + 20;
        double dist = Math.sqrt(Math.pow(px - cx, 2) + Math.pow(py - cy, 2));
        if (dist <= (baseSize / 2 + 40)) {
            g2.setFont(new Font("Courier New", Font.BOLD, 14));
            g2.setColor(new Color(0, 255, 150));

            String qMsg;
            if (waitingForSystemChange) {
                qMsg = (currentPlanetIndex == 7) ? "[ PRESS 'Q' TO JUMP SYSTEM (TO CENTAURI) ]" : "[ PRESS 'Q' TO WARP TO NEXT PLANET ]";
            } else {
                qMsg = "[ PRESS 'Q' TO RETURN TO CORE BASE ]";
            }
            g2.drawString(qMsg, (900 - g2.getFontMetrics().stringWidth(qMsg)) / 2, 385);
        }

        if (waitingForSystemChange) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.setColor(Color.ORANGE);
            String msg = (currentPlanetIndex == 7) ? "SOLAR SYSTEM CLEARED: CENTAURI LINK READY!" : "PLANET CLEARED: ORBITAL PATH OPEN";
            g2.drawString(msg, (900 - g2.getFontMetrics().stringWidth(msg)) / 2, 95);
        }
    }

    private void drawUI(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 220)); g2.fillRect(0, 0, 900, 55);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(Color.RED); g2.drawString("HP: ", 20, 32);
        for (int i = 0; i < maxHp; i++) {
            g2.setColor(i < hp ? Color.RED : Color.DARK_GRAY);
            g2.fillRect(55 + (i * 22), 20, 14, 14);
        }
        g2.setColor(Color.WHITE); g2.drawString("SCORE: " + score, 160, 32);
        g2.setColor(Color.YELLOW); g2.drawString("COINS: " + coinCount, 290, 32);

        g2.setColor(Color.CYAN);
        String pName = PLANETS[currentPlanetIndex];
        int pNameW = g2.getFontMetrics().stringWidth(pName);
        g2.drawString(pName, 450 - (pNameW / 2), 24);

        int pBarW = 120;
        int pBarX = 450 - (pBarW / 2);
        int pBarY = 32;
        g2.setColor(new Color(40, 40, 50));
        g2.fillRect(pBarX, pBarY, pBarW, 6);

        int pFillW = (int) (pBarW * Math.min(1.0, (double) enemiesKilled / KILLS_PER_LEVEL));
        g2.setColor(Color.CYAN);
        g2.fillRect(pBarX, pBarY, pFillW, 6);
        g2.setColor(new Color(0, 255, 255, 100));
        g2.drawRect(pBarX, pBarY, pBarW, 6);

        if (shield) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(new Color(255, 50, 100));
            g2.drawString("SHIELD", 580, 25);
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(580, 30, 65, 8);
            int barWidth = (int) (65.0 * shieldTimer / SHIELD_MAX_DURATION);
            g2.setColor(new Color(255, 50, 100));
            g2.fillRect(580, 30, barWidth, 8);
        }

        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(Color.MAGENTA); g2.drawString("SPD: Lvl " + speedLevel, 660, 32);
        g2.drawString("GUN: Lvl " + fireLevel, 770, 32);
    }

    private void drawSeedShop(Graphics2D g2) {
        GradientPaint panelGrad = new GradientPaint(100, 80, new Color(10, 28, 18, 240), 100, 520, new Color(5, 16, 10, 245));
        g2.setPaint(panelGrad);
        g2.fillRect(100, 80, 700, 440);

        g2.setStroke(new BasicStroke(3));
        g2.setColor(new Color(0, 255, 130, 200));
        g2.drawRect(100, 80, 700, 440);
        g2.setStroke(new BasicStroke(1));

        g2.setFont(new Font("Courier New", Font.BOLD, 36));
        g2.setColor(new Color(0, 255, 140));
        String title = "== BIO-SEED LAB ==";
        g2.drawString(title, (900 - g2.getFontMetrics().stringWidth(title)) / 2, 135);

        g2.setFont(new Font("Lucida Console", Font.PLAIN, 16));
        g2.setColor(Color.YELLOW);
        String coinsMsg = "AVAILABLE FUNDS: " + coinCount + " CASH";
        g2.drawString(coinsMsg, (900 - g2.getFontMetrics().stringWidth(coinsMsg)) / 2, 175);

        g2.setColor(new Color(0, 255, 130, 60)); g2.drawLine(140, 195, 760, 195);

        int startY = 230; int spacing = 75;
        drawSeedItem(g2, startY, "1", "TERRA CARROT SEEDS", "Standard earth crop. Yields basic biomass.", "2 COINS", new Color(0, 255, 150), true);
        drawSeedItem(g2, startY + spacing, "2", "COSMIC BIO-REAGENT", "[ LOCKED - FUTURE UPGRADE ]", "5 COINS", Color.GRAY, false);
        drawSeedItem(g2, startY + (spacing * 2), "3", "QUANTUM BLUEBERRIES SEEDS", "[ LOCKED - FUTURE UPGRADE ]", "10 COINS", Color.GRAY, false);

        g2.setColor(new Color(0, 255, 130, 80)); g2.drawLine(140, 460, 760, 460);
        g2.setFont(new Font("Courier New", Font.ITALIC | Font.BOLD, 15));
        g2.setColor(Color.LIGHT_GRAY);
        String exitMsg = "[ PRESS 'F' OR 'E' TO EXIT SEED LAB ]";
        g2.drawString(exitMsg, (900 - g2.getFontMetrics().stringWidth(exitMsg)) / 2, 495);
    }

    private void drawSeedItem(Graphics2D g2, int y, String key, String name, String desc, String cost, Color themeColor, boolean available) {
        g2.setColor(new Color(255, 255, 255, 10)); g2.fillRoundRect(140, y, 620, 58, 8, 8);
        g2.setColor(themeColor); g2.drawRect(155, y + 14, 30, 30);
        g2.setFont(new Font("Courier New", Font.BOLD, 18)); g2.drawString(key, 165, y + 35);

        g2.setFont(new Font("Lucida Console", Font.BOLD, 14)); g2.setColor(available ? Color.WHITE : Color.GRAY);
        g2.drawString(name, 205, y + 26);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12)); g2.setColor(Color.LIGHT_GRAY);
        g2.drawString(desc, 205, y + 44);

        g2.setFont(new Font("Courier New", Font.BOLD, 15)); g2.setColor(available ? Color.YELLOW : Color.DARK_GRAY);
        g2.drawString("COST: " + cost, 595, y + 35);
    }

    private void drawShop(Graphics2D g2) {
        GradientPaint panelGrad = new GradientPaint(100, 80, new Color(10, 15, 30, 240), 100, 520, new Color(5, 8, 16, 245));
        g2.setPaint(panelGrad);
        g2.fillRect(100, 80, 700, 440);

        g2.setColor(new Color(255, 255, 255, 6));
        for (int y = 85; y < 515; y += 3) { g2.drawLine(105, y, 795, y); }

        g2.setStroke(new BasicStroke(3));
        g2.setColor(new Color(0, 220, 255, 200));
        g2.drawRect(100, 80, 700, 440);
        g2.setStroke(new BasicStroke(1));
        g2.setColor(new Color(0, 100, 255, 150));
        g2.drawRect(104, 84, 692, 432);

        g2.setFont(new Font("Courier New", Font.BOLD, 38));
        g2.setColor(new Color(0, 255, 200));
        String title = "== METROPOLIS MARKET ==";
        g2.drawString(title, (900 - g2.getFontMetrics().stringWidth(title)) / 2, 135);

        g2.setFont(new Font("Lucida Console", Font.PLAIN, 16));
        g2.setColor(Color.YELLOW);
        String coinsMsg = "CREDITS SYNCED: " + coinCount + " CASH";
        g2.drawString(coinsMsg, 450 - (g2.getFontMetrics().stringWidth(coinsMsg) / 2), 175);

        g2.setColor(new Color(0, 220, 255, 80)); g2.drawLine(140, 195, 760, 195);

        int startY = 230; int spacing = 75;
        drawShopItem(g2, startY, "1", "HYPER ENGINE UPGRADE", speedLevel, 5, "10 COINS", new Color(0, 255, 150), true);
        drawShopItem(g2, startY + spacing, "2", "PLASMA DISRUPTOR GUN", fireLevel, 3, "15 COINS", new Color(255, 100, 0), true);
        String shieldStatus = shield ? "[ACTIVE]" : "READY TO DEPLOY";
        drawShopItem(g2, startY + (spacing * 2), "3", "ENERGY DEFLECTOR SHIELD (" + shieldStatus + ")", 0, 0, "5 COINS", new Color(255, 50, 100), false);

        g2.setColor(new Color(0, 220, 255, 80)); g2.drawLine(140, 460, 760, 460);
        g2.setFont(new Font("Courier New", Font.ITALIC | Font.BOLD, 15));
        g2.setColor(new Color(255, 60, 60));
        String exitMsg = "[ PRESS 'E' TO BREAK LINK AND RETURN TO COMBAT ]";
        g2.drawString(exitMsg, (900 - g2.getFontMetrics().stringWidth(exitMsg)) / 2, 495);
    }

    private void drawShopItem(Graphics2D g2, int y, String key, String name, int currentLvl, int maxLvl, String cost, Color themeColor, boolean drawBars) {
        g2.setColor(new Color(255, 255, 255, 12)); g2.fillRoundRect(140, y, 620, 58, 8, 8);
        g2.setColor(new Color(0, 220, 255, 40)); g2.drawRoundRect(140, y, 620, 58, 8, 8);
        g2.setColor(themeColor); g2.setStroke(new BasicStroke(2)); g2.drawRect(155, y + 14, 30, 30);
        g2.setStroke(new BasicStroke(1)); g2.setFont(new Font("Courier New", Font.BOLD, 18)); g2.drawString(key, 165, y + 35);
        g2.setFont(new Font("Lucida Console", Font.BOLD, 14)); g2.setColor(Color.WHITE); g2.drawString(name, 205, y + 26);

        if (drawBars) {
            for (int i = 0; i < maxLvl; i++) {
                if (i < currentLvl) { g2.setColor(themeColor); g2.fillRect(205 + (i * 24), y + 36, 18, 10); }
                else {
                    g2.setColor(new Color(60, 60, 70)); g2.fillRect(205 + (i * 24), y + 36, 18, 10);
                    g2.setColor(new Color(100, 100, 110, 100)); g2.drawRect(205 + (i * 24), y + 36, 18, 10);
                }
            }
        } else {
            g2.setFont(new Font("Lucida Console", Font.PLAIN, 12)); g2.setColor(Color.LIGHT_GRAY); g2.drawString("Provides full immunity to next enemy impacts", 205, y + 44);
        }
        g2.setFont(new Font("Courier New", Font.BOLD, 15));
        if (drawBars && currentLvl >= maxLvl) { g2.setColor(Color.DARK_GRAY); g2.drawString("[MAXED]", 640, y + 35); }
        else { g2.setColor(Color.YELLOW); g2.drawString("COST: " + cost, 595, y + 35); }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        // FIX: novo handler para o ecrã de Game Over
        if (gameState == STATE_GAMEOVER) {
            if (k == KeyEvent.VK_SPACE) gameState = STATE_COLONY;
            return;
        }

        if (gameState == STATE_INTRO) {
            if (k == KeyEvent.VK_SPACE) {
                if (introPage < INTRO_TEXT.length - 1) introPage++;
                else { gameState = STATE_COLONY; player.x = 450; player.y = 500; }
            }
            return;
        }

        if (k == KeyEvent.VK_E) {
            if (gameState == STATE_SPACE) shopOpen = !shopOpen;
            if (gameState == STATE_COLONY && seedShopOpen) { seedShopOpen = false; activePlot = null; }
            return;
        }

        if (gameState == STATE_COLONY && seedShopOpen) {
            if (k == KeyEvent.VK_F) { seedShopOpen = false; activePlot = null; return; }
            if (k == KeyEvent.VK_1 || k == KeyEvent.VK_NUMPAD1) {
                if (coinCount >= 2 && activePlot != null && activePlot.state == 0) {
                    coinCount -= 2;
                    activePlot.plant();
                    seedShopOpen = false;
                    activePlot = null;
                }
            }
            return;
        }

        if (gameState == STATE_SPACE && k == KeyEvent.VK_Q) {
            int px = player.x + 20;
            int py = player.y + 20;
            double dist = Math.sqrt(Math.pow(px - 450, 2) + Math.pow(py - 300, 2));
            int baseSize = 40 + (currentPlanetIndex * 12);
            if (dist <= (baseSize / 2 + 40)) {
                if (waitingForSystemChange) {
                    enemiesKilled = 0;
                    waitingForSystemChange = false;
                    if (currentPlanetIndex < PLANETS.length - 1) {
                        currentPlanetIndex++;
                    }
                    player.x = 450;
                    player.y = 500;
                    enemies.clear();
                    bullets.clear();
                    healShips.clear();
                } else {
                    gameState = STATE_COLONY;
                    player.x = 450;
                    player.y = 500;
                    shopOpen = false;
                }
                return;
            }
        }

        if (shopOpen && gameState == STATE_SPACE) {
            if (k == KeyEvent.VK_1 || k == KeyEvent.VK_NUMPAD1) { if (coinCount >= 10 && speedLevel < 5) { coinCount -= 10; speedLevel++; } }
            if (k == KeyEvent.VK_2 || k == KeyEvent.VK_NUMPAD2) { if (coinCount >= 15 && fireLevel < 3) { coinCount -= 15; fireLevel++; } }
            if (k == KeyEvent.VK_3 || k == KeyEvent.VK_NUMPAD3) { if (coinCount >= 5 && !shield) { coinCount -= 5; shield = true; shieldTimer = SHIELD_MAX_DURATION; } }
            return;
        }

        if (gameState == STATE_COLONY && k == KeyEvent.VK_F) {
            for (CropPlot p : plots) {
                if (p.isPlayerNear(player)) {
                    if (p.state == 0) {
                        activePlot = p;
                        seedShopOpen = true;
                    } else if (p.state == 3) {
                        p.harvest();
                        biomass += 10;
                        score += 15;
                    }
                }
            }
            return;
        }

        if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT)  player.left  = true;
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) player.right = true;
        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP)    player.up    = true;
        if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN)  player.down  = true;

        if (gameState == STATE_SPACE && k == KeyEvent.VK_SPACE) {
            int shots = 1 + fireLevel;
            for (int i = 0; i < shots; i++) {
                bullets.add(new Bullet(player.x + 20 + ((i * 14) - ((shots - 1) * 7)), player.y));
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT)  player.left  = false;
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) player.right = false;
        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP)    player.up    = false;
        if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN)  player.down  = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}