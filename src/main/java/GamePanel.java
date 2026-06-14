import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
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
    private final int SHIELD_MAX_DURATION = 400; // Duração máxima original para cálculo da barra
    private boolean shopOpen = false;
    private int enemiesKilled = 0;
    private int currentPlanetIndex = 0;
    private final int KILLS_PER_LEVEL = 35;
    private boolean waitingForSystemChange = false;
    private boolean clearListsRequested = false;

    private int gameState = -1;
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

        for (int i = 0; i < 120; i++) {
            stars.add(new Star(rand.nextInt(900), rand.nextInt(600), 0, rand.nextInt(3) + 1));
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
        if (gameState == -1) {
            for (Star s : stars) s.update();
        }
        else if (gameState == 0) {
            for (Star s : stars) s.update();
            player.update(speedLevel);
            for (CropPlot p : plots) p.update();

            if (player.y < 40) {
                gameState = 1;
                player.y = 500;
            }
        }
        else if (gameState == 1) {
            for (Star s : stars) s.update();

            if (shopOpen) return;

            if (clearListsRequested) { enemies.clear(); healShips.clear(); clearListsRequested = false; }

            player.update(speedLevel);

            if (shield) { shieldTimer--; if (shieldTimer <= 0) shield = false; }

            for (Bullet b : bullets) b.update(); bullets.removeIf(b -> !b.active);
            for (Enemy en : enemies) en.update(currentPlanetIndex); enemies.removeIf(en -> !en.alive);
            for (HealShip hs : healShips) hs.update(); healShips.removeIf(hs -> !hs.alive);
            for (HeartItem h : hearts) h.update(); hearts.removeIf(h -> h.collected);

            for (Coin c : coins) {
                c.y += 2;
                c.update();
            }
            coins.removeIf(c -> c.collected || c.y > 600);

            for (Particle p : particles) p.update(); particles.removeIf(p -> p.life <= 0);
            for (WhiteLightning wl : lightnings) wl.update(); lightnings.removeIf(wl -> wl.life <= 0);

            if (waitingForSystemChange) {
                if (rand.nextInt(4) == 0) {
                    lightnings.add(new WhiteLightning(450, 300));
                }
                if (enemies.isEmpty()) {
                    enemiesKilled = 0;
                    waitingForSystemChange = false;
                    if (currentPlanetIndex < PLANETS.length - 1) {
                        currentPlanetIndex++;
                    }
                }
            }
            spawnEnemies();
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

    private void handleCollisions() {
        for (Enemy en : enemies) {
            for (Bullet b : bullets) {
                if (b.active && en.hit(b)) {
                    b.active = false; en.enemyHp--;
                    if (en.enemyHp <= 0) {
                        en.alive = false; score += (en.type + 1) * 10;
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
        for (Coin c : coins) { if (!c.collected && c.hit(player)) { c.collected = true; coinCount++; } }
    }

    private void resetGame() {
        enemies.clear(); bullets.clear(); coins.clear(); particles.clear(); healShips.clear(); hearts.clear(); lightnings.clear();
        coinCount = 5; score = 0; hp = 3; maxHp = 3; speedLevel = 0; fireLevel = 0; enemiesKilled = 0; currentPlanetIndex = 0;
        waitingForSystemChange = false; shield = false; gameState = 0; player.x = 450; player.y = 500;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (gameState == -1) {
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
        else if (gameState == 0) {
            g2.setColor(new Color(35, 24, 17));
            g2.fillRect(0, 0, 900, 600);

            g2.setColor(new Color(50, 36, 26));
            for(int i=0; i<8; i++) g2.fillOval(i*140, (i%2)*200 + 100, 120, 45);

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
            g2.drawString("Move over a brown plot and press 'F' to Plant (Costs 2 Coins)", 270, 540);
            g2.drawString("Mature plants glow neon green! Press 'F' to harvest Biomass.", 280, 565);
        }
        else if (gameState == 1) {
            g2.setColor(PLANET_COLORS[currentPlanetIndex]);
            g2.fillRect(0, 0, 900, 600);

            for (Star s : stars) s.draw(g2);
            drawSpaceAnomaly(g2);

            for (WhiteLightning wl : lightnings) wl.draw(g2);
            for (Coin c : coins) c.draw(g2);

            for (Bullet b : bullets) {
                g2.setColor(new Color(255, 60, 0, 150));
                g2.fillRect(b.x - 1, b.y + 4, 7, 12);
                g2.setColor(Color.YELLOW);
                g2.fillRect(b.x, b.y, 5, 12);
            }

            for (Enemy e : enemies) e.draw(g2);
            player.draw(g2, shield);

            drawUI(g2);

            if (shopOpen) drawShop(g2);
        }
    }

    private void drawSpaceAnomaly(Graphics2D g2) {
        int cx = 450, cy = 300;
        int baseSize = 40 + (currentPlanetIndex * 12);
        for (int r = baseSize + 60; r > baseSize; r -= 4) {
            int alpha = (int) (110 * Math.exp(-(r - baseSize) * 0.04));
            g2.setColor(new Color(130, 50, 250, alpha));
            g2.fillOval(cx - r / 2, cy - r / 2, r, r);
        }
        g2.setColor(new Color(5, 5, 10));
        g2.fillOval(cx - baseSize / 2, cy - baseSize / 2, baseSize, baseSize);

        if (waitingForSystemChange) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.setColor(Color.ORANGE);
            String msg = "HYPERDRIVE ENGAGED: CLEAR REMAINING HOSTILES!";
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
        g2.setColor(Color.CYAN); g2.drawString(PLANETS[currentPlanetIndex], 450, 32);

        // ELEMENTO NOVO: Barra de Progresso Temporal do Escudo no HUD Superior esquerdo
        if (shield) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(new Color(255, 50, 100));
            g2.drawString("SHIELD", 580, 25);

            // Fundo escuro da barra
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(580, 30, 65, 8);

            // Preenchimento proporcional ao tempo restante do temporizador
            int barWidth = (int) (65.0 * shieldTimer / SHIELD_MAX_DURATION);
            g2.setColor(new Color(255, 50, 100));
            g2.fillRect(580, 30, barWidth, 8);
        }

        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(Color.MAGENTA); g2.drawString("SPD: Lvl " + speedLevel, 660, 32);
        g2.drawString("GUN: Lvl " + fireLevel, 770, 32);
    }

    private void drawShop(Graphics2D g2) {
        GradientPaint panelGrad = new GradientPaint(100, 80, new Color(10, 15, 30, 240), 100, 520, new Color(5, 8, 16, 245));
        g2.setPaint(panelGrad);
        g2.fillRect(100, 80, 700, 440);

        g2.setColor(new Color(255, 255, 255, 6));
        for (int y = 85; y < 515; y += 3) {
            g2.drawLine(105, y, 795, y);
        }

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
        String coinsMsg = "CREDITS SYNCED: " + coinCount + " CAS";
        g2.drawString(coinsMsg, (900 - g2.getFontMetrics().stringWidth(coinsMsg)) / 2, 175);

        g2.setColor(new Color(0, 220, 255, 80));
        g2.drawLine(140, 195, 760, 195);

        int startY = 230;
        int spacing = 75;

        drawShopItem(g2, startY, "1", "HYPER ENGINE UPGRADE", speedLevel, 5, "10 COINS", new Color(0, 255, 150), true);
        drawShopItem(g2, startY + spacing, "2", "PLASMA DISRUPTOR GUN", fireLevel, 3, "15 COINS", new Color(255, 100, 0), true);

        String shieldStatus = shield ? "[ACTIVE]" : "READY TO DEPLOY";
        drawShopItem(g2, startY + (spacing * 2), "3", "ENERGY DEFLECTOR SHIELD (" + shieldStatus + ")", 0, 0, "5 COINS", new Color(255, 50, 100), false);

        g2.setColor(new Color(0, 220, 255, 80));
        g2.drawLine(140, 460, 760, 460);

        g2.setFont(new Font("Courier New", Font.ITALIC | Font.BOLD, 15));
        g2.setColor(new Color(255, 60, 60));
        String exitMsg = "[ PRESS 'E' TO BREAK LINK AND RETURN TO COMBAT ]";
        g2.drawString(exitMsg, (900 - g2.getFontMetrics().stringWidth(exitMsg)) / 2, 495);
    }

    private void drawShopItem(Graphics2D g2, int y, String key, String name, int currentLvl, int maxLvl, String cost, Color themeColor, boolean drawBars) {
        g2.setColor(new Color(255, 255, 255, 12));
        g2.fillRoundRect(140, y, 620, 58, 8, 8);
        g2.setColor(new Color(0, 220, 255, 40));
        g2.drawRoundRect(140, y, 620, 58, 8, 8);

        g2.setColor(themeColor);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(155, y + 14, 30, 30);
        g2.setStroke(new BasicStroke(1));
        g2.setFont(new Font("Courier New", Font.BOLD, 18));
        g2.drawString(key, 165, y + 35);

        g2.setFont(new Font("Lucida Console", Font.BOLD, 14));
        g2.setColor(Color.WHITE);
        g2.drawString(name, 205, y + 26);

        if (drawBars) {
            for (int i = 0; i < maxLvl; i++) {
                if (i < currentLvl) {
                    g2.setColor(themeColor);
                    g2.fillRect(205 + (i * 24), y + 36, 18, 10);
                } else {
                    g2.setColor(new Color(60, 60, 70));
                    g2.fillRect(205 + (i * 24), y + 36, 18, 10);
                    g2.setColor(new Color(100, 100, 110, 100));
                    g2.drawRect(205 + (i * 24), y + 36, 18, 10);
                }
            }
        } else {
            g2.setFont(new Font("Lucida Console", Font.PLAIN, 12));
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString("Provides full immunity to next enemy impacts", 205, y + 44);
        }

        g2.setFont(new Font("Courier New", Font.BOLD, 15));
        if (drawBars && currentLvl >= maxLvl) {
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("[MAXED]", 640, y + 35);
        } else {
            g2.setColor(Color.YELLOW);
            g2.drawString("COST: " + cost, 595, y + 35);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        if (gameState == -1) {
            if (k == KeyEvent.VK_SPACE) {
                if (introPage < INTRO_TEXT.length - 1) introPage++;
                else { gameState = 0; player.x = 450; player.y = 500; }
            }
            return;
        }

        if (k == KeyEvent.VK_E) {
            if (gameState == 1) {
                shopOpen = !shopOpen;
            }
            return;
        }

        if (shopOpen && gameState == 1) {
            if (k == KeyEvent.VK_1 || k == KeyEvent.VK_NUMPAD1) {
                if (coinCount >= 10 && speedLevel < 5) {
                    coinCount -= 10;
                    speedLevel++;
                }
            }
            if (k == KeyEvent.VK_2 || k == KeyEvent.VK_NUMPAD2) {
                if (coinCount >= 15 && fireLevel < 3) {
                    coinCount -= 15;
                    fireLevel++;
                }
            }
            if (k == KeyEvent.VK_3 || k == KeyEvent.VK_NUMPAD3) {
                if (coinCount >= 5 && !shield) {
                    coinCount -= 5;
                    shield = true;
                    shieldTimer = SHIELD_MAX_DURATION; // Reinicia o escudo com o temporizador cheio
                }
            }
            return;
        }

        if (gameState == 0 && k == KeyEvent.VK_F) {
            for (CropPlot p : plots) {
                if (p.isPlayerNear(player)) {
                    if (p.state == 0 && coinCount >= 2) { coinCount -= 2; p.plant(); }
                    else if (p.state == 3) { p.harvest(); biomass += 10; score += 15; }
                }
            }
        }

        if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) player.left = true;
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) player.right = true;
        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) player.up = true;
        if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) player.down = true;

        if (gameState == 1 && k == KeyEvent.VK_SPACE) {
            int shots = 1 + fireLevel;
            for (int i = 0; i < shots; i++) {
                bullets.add(new Bullet(player.x + 20 + ((i * 14) - ((shots - 1) * 7)), player.y));
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) player.left = false;
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) player.right = false;
        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) player.up = false;
        if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) player.down = false;
    }
    @Override public void keyTyped(KeyEvent e) {}
}