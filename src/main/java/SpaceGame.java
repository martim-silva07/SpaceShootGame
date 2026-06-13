import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class SpaceGame extends JFrame {

    public SpaceGame() {
        setTitle("Space Journey");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        add(new GamePanel());

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpaceGame::new);
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {

    private javax.swing.Timer timer;

    private Player player;

    private List<Bullet> bullets = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Coin> coins = new ArrayList<>();

    private Random rand = new Random();

    private int coinCount = 0;

    private int speedLevel = 0;
    private int fireLevel = 0;

    private boolean shield = false;
    private int shieldTimer = 0;

    private boolean shopOpen = false;

    private double blackHoleT = 0;

    public GamePanel() {

        setPreferredSize(new Dimension(900, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        player = new Player(450, 500);

        timer = new javax.swing.Timer(16, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!shopOpen) update();
        repaint();
    }

    private void update() {

        blackHoleT += 0.02;

        player.update(speedLevel);

        if (shield) {
            shieldTimer--;
            if (shieldTimer <= 0) shield = false;
        }

        for (Bullet b : bullets) b.update();
        bullets.removeIf(b -> !b.active);

        for (Enemy en : enemies) en.update();
        enemies.removeIf(en -> !en.alive);

        for (Coin c : coins) c.update();

        spawnEnemies();
        handleCollisions();
    }

    private void spawnEnemies() {
        if (rand.nextInt(40) == 0) {
            int type = rand.nextInt(3);
            enemies.add(new Enemy(rand.nextInt(860), -20, type));
        }
    }

    private void handleCollisions() {

        for (Enemy en : enemies) {

            for (Bullet b : bullets) {
                if (b.active && en.hit(b)) {
                    b.active = false;
                    en.alive = false;

                    coins.add(new Coin(en.x, en.y));
                }
            }

            if (en.hit(player)) {
                if (shield) {
                    en.alive = false;
                } else {
                    resetGame();
                    return;
                }
            }
        }

        for (Coin c : coins) {
            if (!c.collected && c.hit(player)) {
                c.collected = true;
                coinCount++;
            }
        }

        coins.removeIf(c -> c.collected);
    }

    private void resetGame() {
        enemies.clear();
        bullets.clear();
        coins.clear();

        coinCount = 0;
        speedLevel = 0;
        fireLevel = 0;

        shield = false;
        shieldTimer = 0;

        player.x = 450;
        player.y = 500;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        drawBlackHole(g2);

        player.draw(g2, shield);

        for (Bullet b : bullets) b.draw(g2);
        for (Enemy e : enemies) e.draw(g2);
        for (Coin c : coins) c.draw(g2);

        drawUI(g2);

        if (shopOpen) drawShop(g2);
    }

    private void drawBlackHole(Graphics2D g2) {

        int cx = 450;
        int cy = 300;

        for (int r = 220; r > 10; r -= 5) {

            double intensity = Math.exp(-r * 0.02);

            int alpha = (int)(120 * intensity);

            Color c = new Color(
                    (int)(255 * intensity),
                    (int)(140 * intensity),
                    0,
                    Math.min(255, alpha)
            );

            g2.setColor(c);

            g2.fillOval(cx - r / 2, cy - r / 2, r, r);
        }

        g2.setColor(Color.BLACK);
        g2.fillOval(cx - 20, cy - 20, 40, 40);
    }

    private void drawUI(Graphics2D g2) {

        g2.setColor(Color.WHITE);

        g2.drawString("Coins: " + coinCount, 10, 20);
        g2.drawString("Speed: " + speedLevel, 10, 40);
        g2.drawString("Fire: " + fireLevel, 10, 60);
        g2.drawString("Shield: " + shield, 10, 80);
        g2.drawString("E = shop", 10, 100);
    }

    private void drawShop(Graphics2D g2) {

        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, 900, 600);

        g2.setColor(Color.WHITE);

        g2.drawString("SHOP", 430, 200);

        g2.drawString("1 - Speed boost (5 coins)", 350, 240);
        g2.drawString("2 - Double fire (7 coins)", 350, 270);
        g2.drawString("3 - Shield (10 coins)", 350, 300);

        g2.drawString("Press E to close", 350, 340);
    }

    @Override
    public void keyPressed(KeyEvent e) {

        int k = e.getKeyCode();

        if (k == KeyEvent.VK_E) {
            shopOpen = !shopOpen;
            return;
        }

        if (shopOpen) {

            if (k == KeyEvent.VK_1 && coinCount >= 5) {
                coinCount -= 5;
                speedLevel++;
            }

            if (k == KeyEvent.VK_2 && coinCount >= 7) {
                coinCount -= 7;
                fireLevel++;
            }

            if (k == KeyEvent.VK_3 && coinCount >= 10) {
                coinCount -= 10;
                shield = true;
                shieldTimer = 600;
            }

            return;
        }

        if (k == KeyEvent.VK_A) player.left = true;
        if (k == KeyEvent.VK_D) player.right = true;
        if (k == KeyEvent.VK_W) player.up = true;
        if (k == KeyEvent.VK_S) player.down = true;

        if (k == KeyEvent.VK_SPACE) {

            int shots = 1 + fireLevel;

            for (int i = 0; i < shots; i++) {
                int offset = i * 6 - (shots * 3);
                bullets.add(new Bullet(player.x + 12 + offset, player.y));
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        int k = e.getKeyCode();

        if (k == KeyEvent.VK_A) player.left = false;
        if (k == KeyEvent.VK_D) player.right = false;
        if (k == KeyEvent.VK_W) player.up = false;
        if (k == KeyEvent.VK_S) player.down = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}

class Player {

    int x, y;

    boolean left, right, up, down;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update(int speedLevel) {

        int speed = 4 + speedLevel;

        if (left) x -= speed;
        if (right) x += speed;
        if (up) y -= speed;
        if (down) y += speed;

        x = Math.max(0, Math.min(880, x));
        y = Math.max(0, Math.min(560, y));
    }

    public void draw(Graphics2D g, boolean shield) {

        int[] px = {x, x - 10, x + 10};
        int[] py = {y - 15, y + 10, y + 10};

        g.setColor(Color.CYAN);
        g.fillPolygon(px, py, 3);

        if (shield) {
            g.setColor(new Color(0, 200, 255, 80));
            g.fillOval(x - 20, y - 20, 40, 40);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x - 10, y - 15, 20, 25);
    }
}

class Bullet {

    int x, y;
    boolean active = true;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        y -= 10;
        if (y < 0) active = false;
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.YELLOW);
        g.fillRect(x, y, 4, 10);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 4, 10);
    }
}

class Enemy {

    int x, y;
    int type;
    boolean alive = true;

    public Enemy(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void update() {
        y += 2 + type * 2;
        x += Math.sin(y * 0.02) * 2;
    }

    public void draw(Graphics2D g) {

        if (type == 0) g.setColor(Color.RED);
        if (type == 1) g.setColor(Color.MAGENTA);
        if (type == 2) g.setColor(Color.ORANGE);

        g.fillOval(x, y, 20 + type * 5, 20 + type * 5);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 25, 25);
    }

    public boolean hit(Bullet b) {
        return getBounds().intersects(b.getBounds());
    }

    public boolean hit(Player p) {
        return getBounds().intersects(p.getBounds());
    }
}

class Coin {

    int x, y;
    boolean collected = false;

    public Coin(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        y += 1;
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.YELLOW);
        g.fillOval(x, y, 8, 8);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 8, 8);
    }

    public boolean hit(Player p) {
        return getBounds().intersects(p.getBounds());
    }
}