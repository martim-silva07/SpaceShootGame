import java.awt.*;
import java.util.Random;

public class WhiteLightning {
    public int x, y;
    public int life;
    private double angle;
    private double radius;
    private int size;
    private Color color;
    private Random rand = new Random();

    public WhiteLightning(int centerX, int centerY) {
        // Define o centro (o buraco branco)
        this.x = centerX;
        this.y = centerY;
        // Começa num raio afastado e vai fechando em órbita
        this.radius = rand.nextInt(150) + 100;
        this.angle = rand.nextDouble() * Math.PI * 2;
        this.life = rand.nextInt(30) + 20;
        this.size = rand.nextInt(5) + 4;

        // Cores de luzes estelares (branco, ciano e azul claro brilhante)
        int r = rand.nextInt(3);
        if (r == 0) this.color = Color.WHITE;
        else if (r == 1) this.color = new Color(130, 230, 255);
        else this.color = new Color(200, 240, 255);
    }

    public void update() {
        life--;
        // Faz a luz rodar em redor do centro
        angle += 0.08;
        // Faz a luz ser sugada gradualmente para o centro
        radius -= 3.5;
        if (radius < 5) radius = 5;
    }

    public void draw(Graphics2D g) {
        // Calcula a posição da partícula com base na órbita circular
        int drawX = (int) (x + Math.cos(angle) * radius);
        int drawY = (int) (y + Math.sin(angle) * radius);

        // Desenha a luz com um efeito de transparência (fade out)
        int alpha = Math.max(0, Math.min(255, life * 10));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));

        // Desenha círculos de luz brilhante
        g.fillOval(drawX - size/2, drawY - size/2, size, size);
    }
}