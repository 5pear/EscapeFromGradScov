import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GameMap {

    private BufferedImage baseImage;   // 실제 배경 (1Fhallway)
    private BufferedImage maskImage;   // 이동 가능 영역 마스크 (1Fmove)
    private boolean[][] blocked;       // true = 이동 불가

    public GameMap(String basePath, String maskPath) {
        loadImagesOrFallback(basePath, maskPath);
        alignMaskSizeToBase();   // 🔥 여기서 크기 불일치 자동 조정
        buildCollisionFromMask();
    }

    // 이미지 로드 (없으면 테스트용 맵 생성)
    private void loadImagesOrFallback(String basePath, String maskPath) {

        try {
            baseImage = ImageIO.read(new File(basePath));
        } catch (Exception e) {
            System.err.println("[경고] 배경 로드 실패: " + basePath);
        }

        try {
            maskImage = ImageIO.read(new File(maskPath));
        } catch (Exception e) {
            System.err.println("[경고] 마스크 로드 실패: " + maskPath);
        }

        // 둘 중 하나라도 null이면 테스트용 맵 생성
        if (baseImage == null || maskImage == null) {
            createFallbackMap();
        }
    }

    // base/마스크 중 하나라도 없을 때 테스트용 맵 생성
    private void createFallbackMap() {
        int w = 800, h = 400;

        baseImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        maskImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = baseImage.createGraphics();
        g.setColor(new Color(40, 40, 40));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(90, 90, 90));
        g.fillRect(0, h / 2, w, h / 2);
        g.dispose();

        Graphics2D gm = maskImage.createGraphics();
        gm.setColor(Color.BLACK);
        gm.fillRect(0, 0, w, h);
        gm.setColor(Color.WHITE);
        gm.fillRect(0, h / 2, w, h / 2);
        gm.dispose();

        System.out.println("[정보] 테스트용 맵을 사용합니다 (이미지 로드 실패).");
    }

    // 🔥 여기서 크기 불일치 해결
    private void alignMaskSizeToBase() {
        if (baseImage == null || maskImage == null) return;

        int bw = baseImage.getWidth();
        int bh = baseImage.getHeight();
        int mw = maskImage.getWidth();
        int mh = maskImage.getHeight();

        if (bw == mw && bh == mh) {
            // 이미 크기가 같으면 그대로 사용
            return;
        }

        System.out.println("[정보] 마스크 크기(" + mw + "x" + mh +
                           ")를 배경 크기(" + bw + "x" + bh + ")에 맞게 리사이즈합니다.");

        // 배경 크기에 맞춘 새 마스크 이미지 생성
        BufferedImage resized = new BufferedImage(bw, bh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();

        // 기본은 전부 이동 불가(검정)
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, bw, bh);

        // 전체를 배경 크기에 맞춰 스케일링해서 그리기
        g.drawImage(maskImage, 0, 0, bw, bh, null);
        g.dispose();

        maskImage = resized;
    }

    /**
     * maskImage 기준으로 충돌맵 생성
     *  - 순수 검정(0,0,0) 또는 alpha 거의 0 → 벽/공백
     *  - 그 외 픽셀 → 이동 가능
     */
    private void buildCollisionFromMask() {
        int w = maskImage.getWidth();
        int h = maskImage.getHeight();
        blocked = new boolean[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = maskImage.getRGB(x, y);
                Color c = new Color(argb, true);

                int a = c.getAlpha();
                int r = c.getRed();
                int g = c.getGreen();
                int b = c.getBlue();

                boolean isBlack = (a < 10) || (r == 0 && g == 0 && b == 0);

                blocked[y][x] = isBlack;  // 검정/투명 = 이동 불가
            }
        }
    }

    public BufferedImage getBaseImage() { return baseImage; }
    public int getWidth() { return baseImage.getWidth(); }
    public int getHeight() { return baseImage.getHeight(); }

    public boolean isBlocked(int x, int y) {
        if (x < 0 || y < 0 || y >= blocked.length || x >= blocked[0].length)
            return true;
        return blocked[y][x];
    }
}
