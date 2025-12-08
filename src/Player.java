import java.awt.Rectangle;

public class Player {

    private double x;
    private double y;
    private final int width = 20;
    private final int height = 28;
    private final double speed = 4.0;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public double getSpeed() { return speed; }
}
