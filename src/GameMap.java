import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO; // ✅ 추가

public class GameMap {
    
    // 화면 해상도 (표준 크기)
    public static final int STANDARD_W = 1536; 
    public static final int STANDARD_H = 1024;  

    // 맵 ID
    public static final String MAP_1F_HALLWAY = "1F_HALLWAY";
    public static final String MAP_ROOM_101   = "ROOM_101";
    public static final String MAP_ROOM_102   = "ROOM_102";
    public static final String MAP_ROOM_103   = "ROOM_103";
    public static final String MAP_ROOM_104   = "ROOM_104";

    private final String mapId;

    private BufferedImage baseImage;   // 배경
    private BufferedImage maskImage;   // 이동 가능 마스크
    private boolean[][] blocked;       // true = 이동 불가

    private final List<Interactable> interactables = new ArrayList<>();

    // 테스트 맵(더미) 크기
    private static final int TEST_W = STANDARD_W;
    private static final int TEST_H = STANDARD_H;

    // ─────────────────────────────
    // 팩토리 메서드
    // ─────────────────────────────
    public static GameMap create(String mapId) {
        switch (mapId) {
            case MAP_1F_HALLWAY:
                return new GameMap(mapId, "/map/1Fhallway.png", "/map/1Fmove.png");
            case MAP_ROOM_101:
                return new GameMap(mapId, "/map/101room.png", "/map/101roommove.png");
            case MAP_ROOM_102:
                return new GameMap(mapId, "/map/102room.png", "/map/102roommove.png");
            case MAP_ROOM_103:
                return new GameMap(mapId, "/map/103room.png", "/map/103roommove.png");
            case MAP_ROOM_104:
                return new GameMap(mapId, "/map/104room.png", "/map/104roommove.png");
            default:
                throw new IllegalArgumentException("Unknown map id: " + mapId);
        }
    }

    // ─────────────────────────────
    // 생성자
    // ─────────────────────────────
    private GameMap(String mapId, String basePath, String maskPath) {
        this.mapId = mapId;

        // ✅ 로딩 실패 시 테스트 맵 대체
        loadImagesWithFallback(basePath, maskPath);

        // ✅ 표준 해상도로 통일 (배경/마스크 둘 다)
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
            // ✅  ResourceUtils 대신 표준 ImageIO 사용
            baseImage = ImageIO.read(getClass().getResource(basePath));
            if (baseImage == null) throw new RuntimeException("baseImage is null");
        } catch (Exception e) {
            baseOk = false;
            System.err.println("[GameMap] 배경 이미지 로딩 실패: " + basePath);
            // e.printStackTrace();
        }

        try {
            
            maskImage = ImageIO.read(getClass().getResource(maskPath));
            if (maskImage == null) throw new RuntimeException("maskImage is null");
        } catch (Exception e) {
            maskOk = false;
            System.err.println("[GameMap] 마스크 이미지 로딩 실패: " + maskPath);
            // e.printStackTrace();
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
    // ✅ 표준 크기 강제 통일
    // ─────────────────────────────
    private void normalizeToStandardSize() {
        int bw = baseImage.getWidth();
        int bh = baseImage.getHeight();

        if (bw == STANDARD_W && bh == STANDARD_H) {
            return;
        }

        System.out.println("[GameMap] Normalize map size for " + mapId +
                " : " + bw + "x" + bh + " -> " + STANDARD_W + "x" + STANDARD_H);

        baseImage = resizeImage(baseImage, STANDARD_W, STANDARD_H, false);
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

        // System.out.println("[정보] 마스크 크기를 배경 크기에 맞게 리사이즈합니다.");

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
            
            interactables.add(new PasswordDoor(
            		640, 700,   // x, y
                    150, 60,    // width, height
                    "OUTRO",    // 이동할 맵 ID (엔딩 화면용 맵이 있다면 그것을 입력)
                    "1588",     // ★ 비밀번호
                    "비밀번호를 입력하라." // 힌트 메시지
                ));
          
        } 
        
        // 2. 101호 (수학과 교수 방)
        else if (MAP_ROOM_101.equals(mapId)) {
            // 복도로 나가는 문 (공통)
            addHallwayDoor();

            // [NPC] 수학과 교수         
            Npc mathProf = new Npc(
                    400, 200,
                    "수학과 교수",
                    new String[] {
                        "수학과 교수: 공사 소음 때문에 집중이 안 되는군.",
                        "수학과 교수: 그래도… 들어왔으면 규칙은 지켜.",
                        "수학과 교수: 문제 하나. 값만 구해 와"
                    },
                    "E ={((-√144+((9×9)-2×(13×13)))^2+1)^0}+{0×((999-101×97)+((1+2+50)-(25×51))}의 정답은?", 
                    "1",
                    "아예 멍청이는 아니군",
                    "자네는 지능이 참 낮군",
                    "첫번째 자리수는 1이야"
            );
            // ✅ 이미지를 설정합니다.
            mathProf.setCustomImage("/character/professor1.png");
            // 이미지 크기  조절
            mathProf.setSize(200, 200);
            // ✅ 리스트에 추가합니다.
            interactables.add(mathProf);
            
        }
        
        
        
        // 4. 102호 (화학과 조교 방)
        else if (MAP_ROOM_102.equals(mapId)) {
            addHallwayDoor();

            // [NPC] 화학과 조교
            Npc chemAsst = new Npc(
                    400, 200,
                    "화학과 조교",
                    new String[] { "화학과 조교: 텅 빈 컴퓨터실인데… 화학식만 남아있네. 딱 하나만 맞혀"                   		
                    },
                    "CuSO₄ · □H₂O 여기서 **□**에 들어갈 숫자는 뭐지?", 
                    "5",
                    "훌륭하군.",
                    "공부 좀 더 하게.",
                    "두번째 자리수는 5야"
            );
            // ✅ 이미지 설정
            chemAsst.setCustomImage("/character/professor2.png");
            chemAsst.setSize(200, 200);
            // ✅ 리스트 추가
            interactables.add(chemAsst);
        }
        
     // 3. 103호 (영어 교수 방 )
        else if (MAP_ROOM_103.equals(mapId)) {
            addHallwayDoor();

            // [NPC] 영어 교수 
            Npc engProf = new Npc(
                    700, 150,
                    "영문과 교수",
                    new String[] { "영어 교수: Hello there.",
                    		"영어 교수: Do you speak English?" 
                    },
                    "사과는 영어로?", 
                    "apple",
                    "Great job!",
                    "No, try again.",
                    "Eight"
            );
            // ✅ 이미지 설정
            engProf.setCustomImage("/character/professor3.png");
            engProf.setSize(200, 200);
            // ✅ 리스트 추가
            interactables.add(engProf);
        }
        
        // 5. 104호 (???)
        else if (MAP_ROOM_104.equals(mapId)) {
            addHallwayDoor();
            
         // [NPC] 대학원생
            Npc student = new Npc(
                    650, 750,
                    "???",
                    new String[] { "???: 너도 갇혔구나.이곳에서는 한발자국도 못움직여" ,
                    		"???:내가 마지막 자리 비번을 알고 있다",
                    		"???:그냥은 못주지 "
                    		
                    },
                    "컴공과 대학원생:CPU와 주기억장치의 속도차이를 보완하기 위한 기억장치는??", 
                    "캐시메모리",
                    "정답이야",
                    "아직 탈출할 준비가 덜 됬군",
                    "1바이트"
            );
         // ✅ 이미지 설정
            student.setCustomImage("/character/student.png");
            student.setSize(200, 200);
            // ✅ 리스트 추가
            interactables.add(student);
        }
    }

    // 모든 방에서 '복도로 나가는 문' 위치가 같다면 이 메서드를 활용
    private void addHallwayDoor() {
        interactables.add(new Door(
                675, 900, 190, 60,
                MAP_1F_HALLWAY,
                "복도로 나간다.",
                Player.Facing.DOWN
        ));
    }
       
    

    // ─────────────────────────────
    // getter
    // ─────────────────────────────
    public BufferedImage getBaseImage() { return baseImage; }
    public int getWidth() { return baseImage.getWidth(); }
    public int getHeight() { return baseImage.getHeight(); }

    public boolean isBlocked(int x, int y) {
        if (x < 0 || y < 0 || y >= blocked.length || x >= blocked[0].length) return true;
        return blocked[y][x];
    }

    public List<Interactable> getInteractables() {
        return interactables;
    }

    public String getMapId() {
        return mapId;
    }

    public void addInteractable(Interactable it) {
        interactables.add(it);
    }
}
