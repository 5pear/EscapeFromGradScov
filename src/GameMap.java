import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class GameMap {

    // 맵 ID
    public static final String MAP_1F_HALLWAY = "1F_HALLWAY";
    public static final String MAP_ROOM_101   = "ROOM_101";

    private final String mapId;

    private BufferedImage baseImage;   // 배경
    private BufferedImage maskImage;   // 이동 가능 마스크
    private boolean[][] blocked;       // true = 이동 불가

    private final List<Interactable> interactables = new ArrayList<>();

    // ─────────────────────────────
    // 팩토리 메서드
    // ─────────────────────────────
    public static GameMap create(String mapId) {
        switch (mapId) {
            case MAP_1F_HALLWAY:
                return new GameMap(
                        mapId,
                        "res/map/1Fhallway.png",
                        "res/map/1Fmove.png"
                );
            case MAP_ROOM_101:
                return new GameMap(
                        mapId,
                        "res/map/101room.png",
                        "res/map/101roommove.png"
                );
            default:
                throw new IllegalArgumentException("Unknown map id: " + mapId);
        }
    }

    // ─────────────────────────────
    // 생성자
    // ─────────────────────────────
    private GameMap(String mapId, String basePath, String maskPath) {
        this.mapId = mapId;
        loadImages(basePath, maskPath);
        alignMaskToBase();
        buildCollisionFromMask();
        initInteractables();
    }

    private void loadImages(String basePath, String maskPath) {
        try {
            baseImage = ImageIO.read(new File(basePath));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load base image: " + basePath, e);
        }

        try {
            maskImage = ImageIO.read(new File(maskPath));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load mask image: " + maskPath, e);
        }
    }

    // 배경 크기에 마스크 크기를 맞춰 스케일링
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

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, bw, bh);

        g.drawImage(maskImage, 0, 0, bw, bh, null);
        g.dispose();

        maskImage = resized;
    }

    // 마스크 → blocked 배열 생성
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

                blocked[y][x] = isBlack; // 검정/투명 = 이동 불가
            }
        }
    }

    // 맵별 문 상호작용 등록
    private void initInteractables() {
        if (MAP_1F_HALLWAY.equals(mapId)) {
            // 복도 → 101호 문
            // 좌표: (190,270) ~ (300,310)
            interactables.add(new Door(
                    190, 270,
                    110, 40,
                    MAP_ROOM_101,
                    "101호 교실로 들어간다.",
                    Player.Facing.UP
            ));
            
            // [탈출] 비밀번호 탈출구
          
            interactables.add(new PasswordDoor(
                640, 450,   // x, y
                150, 60,    // width, height
                "OUTRO",    // 이동할 맵 ID (엔딩 화면용 맵이 있다면 그것을 입력)
                "1234",     // ★ 비밀번호
                "비밀번호를 입력하라." // 힌트 메시지
            ));

        } else if (MAP_ROOM_101.equals(mapId)) {
            // 101호 → 복도 문
            // 좌표: (675,910) ~ (865,940) 기준으로 약간 여유 있게
            interactables.add(new Door(
                    675, 900,
                    190, 60,
                    MAP_1F_HALLWAY,
                    "복도로 나간다.",
                    Player.Facing.DOWN
            ));
            
         // NPC~
            interactables.add(new Npc(
                    400, 300,
                    "수학과 교수",
                    // [1] 퀴즈 전 잡담 (여러 줄 가능)
                    new String[] {
                        "수학과 교수: 공사 소음 때문에 집중이 안 되는군.",
                        "수학과 교수: 그래도… 들어왔으면 규칙은 지켜.",
                        "수학과 교수: 문제 하나. 값만 구해 와"
                    },
                    // [2] 퀴즈 질문
                    "E ={ ( ( (127 - 3×41)^2 + (19×23 - 5^3) - √144 + (48/6)×(7-2) + ( (31-17)×(9+8) - (22-5×4) ) + ( (9×9×9) - 2×(13×13) ) )^2 + 1 )^0 }+{ 0 × (( (9999 - 101×97) × (73 - 8×9) )+ ( (17^4 - 16^4) / (17 - 16) )+ ( (3×5×7×11×13) - (2^10) ) + ( (1+2+3+…+50) - (25×51) ) )}의 정답은?", 
                    "1",
                    "아예 멍청이는 아니군",
                    "자네는 지능이 참 낮군",
                    "힌트"
            ));
            
            

        }
    }
    
 

    // ────────── getter / 유틸 ──────────
    public BufferedImage getBaseImage() {
        return baseImage;
    }

    public int getWidth() {
        return baseImage.getWidth();
    }

    public int getHeight() {
        return baseImage.getHeight();
    }

    public boolean isBlocked(int x, int y) {
        if (x < 0 || y < 0 || y >= blocked.length || x >= blocked[0].length) {
            return true;
        }
        return blocked[y][x];
    }

    public List<Interactable> getInteractables() {
        return interactables;
    }

    public String getMapId() {
        return mapId;
    }
 // 실행 중에 NPC나 물체를 맵에 추가하는 메서드
    public void addInteractable(Interactable it) {
        interactables.add(it);
    }
}

