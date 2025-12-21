import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO; // ✅ 추가됨

public class Player {

    public enum Facing { UP, DOWN, LEFT, RIGHT }

    private double x;
    private double y;

    private final int width = 20;
    private final int height = 28;

    private final double speed = 9.0;
    private Facing facing = Facing.DOWN;

    // ✅ [경로 확인] res 폴더가 소스 폴더이므로 "/character/player.png"가 맞습니다.
    private static final String SPRITE_SHEET_PATH = "/character/player.png";

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
            // ✅ [수정 1] ResourceUtils 대신 표준 ImageIO 사용 (가장 안전함)
            sheet = ImageIO.read(Player.class.getResource(SPRITE_SHEET_PATH));
            
            if (sheet == null) throw new RuntimeException("sheet is null");
        } catch (Exception e) {
            System.err.println("[Player] 이미지 로딩 실패!");
            System.err.println("경로 확인 필요: " + SPRITE_SHEET_PATH);
            // e.printStackTrace(); // 에러 메시지가 너무 길면 주석 처리
            return;
        }

        // 디버깅용 출력 (나중에 지워도 됨)
        // System.out.println("[Player] sheet loaded: " + sheet.getWidth() + "x" + sheet.getHeight());

        try {
            // ✅ 좌표는 보내주신 코드 그대로 유지했습니다.
            spriteDown = crop(sheet, 55, 390, 270, 735);
            spriteUp    = crop(sheet, 350, 390, 565, 730);
            spriteLeft = crop(sheet, 630, 40, 825, 375);
            spriteRight = flipHorizontally(spriteLeft);

            System.out.println("[Player] Sprites loaded successfully.");
        } catch (Exception e) {
            System.err.println("[Player] 이미지 자르기(Crop) 실패. 좌표가 이미지 크기를 벗어났을 수 있습니다.");
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
            // 이미지가 없으면 빨간 박스로라도 표시
            g2.setColor(java.awt.Color.RED);
            g2.fillRect(drawX, drawY, width, height);
            return;
        }

        int rw = (int) Math.round(img.getWidth() * renderScale);
        int rh = (int) Math.round(img.getHeight() * renderScale);

        int renderX = drawX - (rw - width) / 2;
        int renderY = drawY - (rh - height);

        g2.drawImage(img, renderX, renderY, rw, rh, null);
    }

    // ✅ [수정 2] NPC가 아이템을 줄 때 필요한 메서드 추가 (없으면 에러 남)
    public void addItem(String item) {
        System.out.println("[Player] 아이템 획득: " + item);
        // 나중에 인벤토리 리스트에 추가하는 코드를 여기에 작성하면 됩니다.
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
        if (facing != null) {
            this.facing = facing;
        }
    }
}