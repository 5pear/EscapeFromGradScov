import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Player {

    public enum Facing { UP, DOWN, LEFT, RIGHT }

    private double x;
    private double y;

    private final int width = 20;
    private final int height = 28;

    private final double speed = 9.0;
    private Facing facing = Facing.DOWN;

    private static final String SPRITE_SHEET_PATH = "res/character/player.png";

    private static BufferedImage sheet;
    private static BufferedImage spriteDown;
    private static BufferedImage spriteUp;
    private static BufferedImage spriteLeft;
    private static BufferedImage spriteRight;

    private double renderScale = 0.5;

    static {
        loadSprites();
    }

    private static void loadSprites() {
        try {
            File f = new File(SPRITE_SHEET_PATH);
            System.out.println("[Player] Try file path: " + f.getAbsolutePath() + " exists=" + f.exists());
            sheet = ImageIO.read(f);
        } catch (IOException e) {
            System.err.println("[Player] Sprite sheet load failed: " + SPRITE_SHEET_PATH);
            e.printStackTrace();
            return;
        }

        System.out.println("[Player] sheet=" + sheet.getWidth() + "x" + sheet.getHeight()
                + " hasAlpha=" + sheet.getColorModel().hasAlpha());

        try {
            spriteDown = crop(sheet, 55, 390, 270, 735);
            spriteUp   = crop(sheet, 350, 390, 565, 730);
            spriteLeft = crop(sheet, 630, 40, 825, 375);
            spriteRight = flipHorizontally(spriteLeft);

            System.out.println("[Player] Sprite crop success.");
        } catch (Exception e) {
            System.err.println("[Player] Crop failed. (좌표/이미지 크기 확인 필요)");
            e.printStackTrace();
        }
    }

    private static BufferedImage crop(BufferedImage src, int x1, int y1, int x2, int y2) {
        int w = x2 - x1;
        int h = y2 - y1;
        return src.getSubimage(x1, y1, w, h);
    }

    private static BufferedImage flipHorizontally(BufferedImage src) {
        if (src == null) return null;

        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = out.createGraphics();
        AffineTransform at = new AffineTransform();
        at.scale(-1, 1);
        at.translate(-w, 0);
        g2.drawImage(src, at, null);
        g2.dispose();

        return out;
    }

    public BufferedImage getCurrentSprite() {
        switch (facing) {
            case UP: return spriteUp;
            case DOWN: return spriteDown;
            case LEFT: return spriteLeft;
            case RIGHT: return spriteRight;
            default: return spriteDown;
        }
    }

    public void draw(Graphics2D g2, int drawX, int drawY) {
        BufferedImage img = getCurrentSprite();
        if (img == null) {
            g2.fillRect(drawX, drawY, width, height);
            return;
        }

        int rw = (int) Math.round(img.getWidth() * renderScale);
        int rh = (int) Math.round(img.getHeight() * renderScale);

        int renderX = drawX - (rw - width) / 2;
        int renderY = drawY - (rh - height);

        g2.drawImage(img, renderX, renderY, rw, rh, null);
    }

    public void setRenderScale(double scale) {
        if (scale > 0) this.renderScale = scale;
    }

    public double getRenderScale() {
        return renderScale;
    }

    public Player(double x, double y) { this.x = x; this.y = y; }

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
        if (facing != null) this.facing = facing;
    }
}
