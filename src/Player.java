import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Player {

    public enum Facing {
        UP, DOWN, LEFT, RIGHT
    }

    private double x;
    private double y;

    private final int width = 20;
    private final int height = 28;

    private final double speed = 9.0;

    private Facing facing = Facing.DOWN;
    
    // [아이템] 아이템을 담을 가방 (문자열로 아이템 이름 저장)
    private List<String> inventory = new ArrayList<>();

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
    }
    // [힌트] 아이템 획득 메서드
    public void addItem(String itemName) {
        inventory.add(itemName);
    }

    // [힌트] 아이템 보유 확인 메서드 
    public boolean hasItem(String itemName) {
        return inventory.contains(itemName);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public double getSpeed() { return speed; }

    public Facing getFacing() { return facing; }

    public void setFacing(Facing facing) {
        if (facing != null) {
            this.facing = facing;
        }
    }
}
