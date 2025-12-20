import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GameMap {

    // ✅ 표준 맵 크기(복도 기준)
    private static final int STANDARD_W = 1536;
    private static final int STANDARD_H = 1024;

    // 맵 ID
    public static final String MAP_1F_HALLWAY = "1F_HALLWAY";
    public static final String MAP_ROOM_101   = "ROOM_101";
    public static final String MAP_ROOM_102   = "ROOM_102";
    public static final String MAP_ROOM_103   = "ROOM_103";
    public static final String MAP_ROOM_104   = "ROOM_104";

    private final String mapId;

    private BufferedImage baseImage;
    private BufferedImage maskImage;
    private boolean[][] blocked;

    private final List<Interactable> interactables = new ArrayList<>();

    // 테스트 맵(더미) 크기 = 표준과 동일
    private static final int TEST_W = STANDARD_W;
    private static final int TEST_H = STANDARD_H;

    // ─────────────────────────────
    // 팩토리
    // ─────────────────────────────
    public static GameMap create(String mapId) {
        switch (mapId) {
            case MAP_1F_HALLWAY:
                return new GameMap(mapId, "res/map/1Fhallway.png", "res/map/1Fmove.png");
            case MAP_ROOM_101:
                return new GameMap(mapId, "res/map/101room.png", "res/map/101roommove.png");
            case MAP_ROOM_102:
                return new GameMap(mapId, "res/map/102room.png", "res/map/102roommove.png");
            case MAP_ROOM_103:
                return new GameMap(mapId, "res/map/103room.png", "res/map/103roommove.png");
            case MAP_ROOM_104:
                return new GameMap(mapId, "res/map/104room.png", "res/map/104roommove.png");
            default:
                throw new IllegalArgumentException("Unknown map id: " + mapId);
        }
    }

    private GameMap(String mapId, String basePath, String maskPath) {
        this.mapId = mapId;

        // ✅ 로딩 실패 시 테스트 맵 대체
        loadImagesWithFallback(basePath, maskPath);

        // ✅ (핵심) 배경/마스크를 표준 크기로 강제 통일
        normalizeToStandardSize();

        // ✅ 혹시 남아있을 수 있는 미세 불일치 보정(NEAREST)
        alignMaskToBase();

        // ✅ 충돌 생성(빨강/검정 모드 자동 감지)
        buildCollisionFromMask();

        // ✅ 문 등록
        initInteractables();
    }

    // ─────────────────────────────
    // 로딩 + fallback
    // ─────────────────────────────
    private void loadImagesWithFallback(String basePath, String maskPath) {
        boolean baseOk = true;
        boolean maskOk = true;

        try {
            baseImage = ResourceUtils.loadImage(basePath);
            if (baseImage == null) throw new RuntimeException("baseImage is null");
        } catch (Exception e) {
            baseOk = false;
            System.err.println("[GameMap] Failed to load base image: " + basePath);
            e.printStackTrace();
        }

        try {
            maskImage = ResourceUtils.loadImage(maskPath);
            if (maskImage == null) throw new RuntimeException("maskImage is null");
        } catch (Exception e) {
            maskOk = false;
            System.err.println("[GameMap] Failed to load mask image: " + maskPath);
            e.printStackTrace();
        }

        if (!baseOk) {
            baseImage = createTestBaseImage("TEST MAP (base missing)\nmapId=" + mapId + "\n" + basePath);
        }
        if (!maskOk) {
            maskImage = createTestMaskImage();
        }

        if (!baseOk || !maskOk) {
            System.out.println("[GameMap] Using TEST fallback images for mapId=" + mapId);
        }
    }

    private BufferedImage createTestBaseImage(String label) {
        BufferedImage img = new BufferedImage(TEST_W, TEST_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, TEST_W, TEST_H);

        g.setColor(new Color(60, 60, 60));
        int grid = 64;
        for (int x = 0; x < TEST_W; x += grid) g.drawLine(x, 0, x, TEST_H);
        for (int y = 0; y < TEST_H; y += grid) g.drawLine(0, y, TEST_W, y);

        g.setColor(new Color(220, 80, 80));
        g.setFont(new Font("Dialog", Font.BOLD, 22));
        String[] lines = label.split("\n");
        int ty = 40;
        for (String line : lines) {
            g.drawString(line, 30, ty);
            ty += 28;
        }

        g.dispose();
        return img;
    }

    private BufferedImage createTestMaskImage() {
        BufferedImage img = new BufferedImage(TEST_W, TEST_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setColor(new Color(255, 255, 255, 255));
        g.fillRect(0, 0, TEST_W, TEST_H);

        g.setColor(new Color(0, 0, 0, 255));
        int border = 24;
        g.fillRect(0, 0, TEST_W, border);
        g.fillRect(0, TEST_H - border, TEST_W, border);
        g.fillRect(0, 0, border, TEST_H);
        g.fillRect(TEST_W - border, 0, border, TEST_H);

        g.dispose();
        return img;
    }

    // ─────────────────────────────
    // ✅ (핵심) 표준 크기 강제 통일
    // ─────────────────────────────
    private void normalizeToStandardSize() {
        int bw = baseImage.getWidth();
        int bh = baseImage.getHeight();

        if (bw == STANDARD_W && bh == STANDARD_H) {
            // base가 표준이면 mask는 alignMaskToBase에서 맞춰짐
            return;
        }

        System.out.println("[GameMap] Normalize map size for " + mapId +
                " : " + bw + "x" + bh + " -> " + STANDARD_W + "x" + STANDARD_H);

        // ✅ 배경은 보기 좋게 BILINEAR(부드럽게)
        baseImage = resizeImage(baseImage, STANDARD_W, STANDARD_H, false);

        // ✅ 마스크는 색이 깨지면 안 됨(빨강/검정 판정) → NEAREST
        maskImage = resizeImage(maskImage, STANDARD_W, STANDARD_H, true);
    }

    private BufferedImage resizeImage(BufferedImage src, int tw, int th, boolean nearest) {
        BufferedImage out = new BufferedImage(tw, th, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                nearest ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                        : RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.drawImage(src, 0, 0, tw, th, null);
        g.dispose();
        return out;
    }

    // ─────────────────────────────
    // 마스크 크기 정렬(NEAREST)
    // ─────────────────────────────
    private void alignMaskToBase() {
        int bw = baseImage.getWidth();
        int bh = baseImage.getHeight();
        int mw = maskImage.getWidth();
        int mh = maskImage.getHeight();

        if (bw == mw && bh == mh) return;

        System.out.println("[정보] 마스크 크기(" + mw + "x" + mh +
                ")를 배경 크기(" + bw + "x" + bh + ")에 맞게 리사이즈합니다.");

        BufferedImage resized = new BufferedImage(bw, bh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, bw, bh);

        g.drawImage(maskImage, 0, 0, bw, bh, null);
        g.dispose();

        maskImage = resized;
    }

    // ─────────────────────────────
    // 충돌 생성(빨강/검정 자동)
    // ─────────────────────────────
    private void buildCollisionFromMask() {
        int w = maskImage.getWidth();
        int h = maskImage.getHeight();

        blocked = new boolean[h][w];

        boolean redMode = isRedBlockedMask(maskImage);
        System.out.println("[GameMap] Mask mode for " + mapId + " = "
                + (redMode ? "RED=BLOCKED" : "BLACK=BLOCKED"));

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = maskImage.getRGB(x, y);
                Color c = new Color(argb, true);

                int a = c.getAlpha();
                int r = c.getRed();
                int g = c.getGreen();
                int b = c.getBlue();

                boolean isTransparent = (a < 10);
                boolean isRed = (a > 10 && r >= 240 && g <= 20 && b <= 20);
                boolean isBlack = (r == 0 && g == 0 && b == 0);

                if (redMode) {
                    blocked[y][x] = isTransparent || isRed;
                } else {
                    blocked[y][x] = isTransparent || isBlack;
                }
            }
        }
    }

    private boolean isRedBlockedMask(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();

        int step = 4;
        int redCount = 0;
        int sampleCount = 0;

        for (int y = 0; y < h; y += step) {
            for (int x = 0; x < w; x += step) {
                int argb = img.getRGB(x, y);

                int a = (argb >>> 24) & 0xFF;
                if (a < 10) continue;

                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = (argb) & 0xFF;

                if (r >= 240 && g <= 20 && b <= 20) redCount++;
                sampleCount++;
            }
        }

        return redCount >= Math.max(200, sampleCount / 50);
    }

    // ─────────────────────────────
    // 문 등록
    // ─────────────────────────────
    private void initInteractables() {
        if (MAP_1F_HALLWAY.equals(mapId)) {

            interactables.add(new Door(
                    190, 270, 110, 40,
                    MAP_ROOM_101,
                    "101호 교실로 들어간다.",
                    Player.Facing.UP
            ));

            interactables.add(new Door(
                    490, 250, 120, 45,
                    MAP_ROOM_102,
                    "102호 교실로 들어간다.",
                    Player.Facing.UP
            ));

            interactables.add(new Door(
                    780, 250, 120, 45,
                    MAP_ROOM_103,
                    "103호 교실로 들어간다.",
                    Player.Facing.UP
            ));

            interactables.add(new Door(
                    1170, 250, 120, 45,
                    MAP_ROOM_104,
                    "104호 교실로 들어간다.",
                    Player.Facing.UP
            ));

        } else if (MAP_ROOM_101.equals(mapId)
                || MAP_ROOM_102.equals(mapId)
                || MAP_ROOM_103.equals(mapId)
                || MAP_ROOM_104.equals(mapId)) {

            // 방 → 복도 문(동일)
            interactables.add(new Door(
                    675, 900, 190, 60,
                    MAP_1F_HALLWAY,
                    "복도로 나간다.",
                    Player.Facing.DOWN
            ));
        }
    }

    // ────────── getter ──────────
    public BufferedImage getBaseImage() { return baseImage; }
    public int getWidth() { return baseImage.getWidth(); }
    public int getHeight() { return baseImage.getHeight(); }

    public boolean isBlocked(int x, int y) {
        if (x < 0 || y < 0 || y >= blocked.length || x >= blocked[0].length) return true;
        return blocked[y][x];
    }

    public List<Interactable> getInteractables() { return interactables; }
    public String getMapId() { return mapId; }
}
