import java.awt.*;

class Particle {
    public double x, y;
    public int life;

    public Particle(double x, double y, Color c) {
        this.x = x;
        this.y = y;
        this.life = 20;
    }

    public void update() {
        life--;
    }

    public void draw(Graphics2D g) {}
}

class WhiteLightning {
    public int life = 5;

    public WhiteLightning(int x, int y) {}

    public void update() {
        life--;
    }

    public void draw(Graphics2D g) {}
}

class HealShip {
    public int x, y;
    public int hp = 5;
    public boolean alive = true;

    public HealShip(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {}

    public void draw(Graphics2D g) {}

    public boolean hit(Bullet b) {
        return false;
    }
}

class HeartItem {
    public int x, y;
    public boolean collected = false;

    public HeartItem(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {}

    public void draw(Graphics2D g) {}

    public boolean hit(Player p) {
        return false;
    }
}