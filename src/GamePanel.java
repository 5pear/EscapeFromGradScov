import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements KeyListener, ActionListener {

    private GameMap currentMap;
    private Player player;

    private final boolean[] keys = new boolean[256];
    private final Timer timer;

    // 패널(표시 영역) 크기 = 첫 맵(1F 복도) 이미지 크기
    private final int viewWidth;
    private final int viewHeight;

    public GamePanel() {
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // 1. 초기 맵 로드
        currentMap = GameMap.create(GameMap.MAP_1F_HALLWAY);

        // 2. 패널 크기 = 첫 맵 크기
        viewWidth = currentMap.getWidth();
        viewHeight = currentMap.getHeight();
        setPreferredSize(new Dimension(viewWidth, viewHeight));

        // 3. 플레이어 생성
        player = new Player(0, 0);

        // 4. 초기 스폰 위치
        Point spawn = findSafeSpawnNear(
                currentMap,
                currentMap.getWidth() / 2,
                currentMap.getHeight() / 2
        );

        player.setX(spawn.x);
        player.setY(spawn.y);

        // 5. 게임 루프
        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        addKeyListener(this);
        requestFocusInWindow();
    }

    // ===============================
    //       렌더링(카메라 포함)
    // ===============================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 배경 검정
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, viewWidth, viewHeight);

        int mapW = currentMap.getWidth();
        int mapH = currentMap.getHeight();

        // 플레이어 중심을 카메라 중심으로 설정
        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;

        int camX = (int) Math.floor(playerCenterX - viewWidth / 2.0);
        int camY = (int) Math.floor(playerCenterY - viewHeight / 2.0);

        // 맵에서 실제로 그릴 부분 (맵 내부 영역만)
        int srcX1 = Math.max(0, camX);
        int srcY1 = Math.max(0, camY);
        int srcX2 = Math.min(mapW, camX + viewWidth);
        int srcY2 = Math.min(mapH, camY + viewHeight);

        // 화면에 그릴 목적지 영역
        int dstX1 = srcX1 - camX;    // 맵이 왼쪽/위로 벗어났을 경우 보정
        int dstY1 = srcY1 - camY;
        int dstX2 = dstX1 + (srcX2 - srcX1);
        int dstY2 = dstY1 + (srcY2 - srcY1);

        // drawImage 한 번으로 화면에 그림 (빠름!)
        g2.drawImage(
            currentMap.getBaseImage(),
            dstX1, dstY1, dstX2, dstY2,
            srcX1, srcY1, srcX2, srcY2,
            null
        );
        
     // 물체npc상호작용~
        if (currentMap != null) {
            for (Interactable obj : currentMap.getInteractables()) {
                // obj가 Npc 클래스의 인스턴스인지 확인
                if (obj instanceof Npc) {
                    Npc npc = (Npc) obj;
                    // Npc 클래스에 만들어둔 draw 메서드 호출
                    npc.draw(g2, camX, camY);
                }
            }
        }

        // 플레이어 렌더링 (카메라 보정)
        int drawPX = (int) Math.round(player.getX() - camX);
        int drawPY = (int) Math.round(player.getY() - camY);

        g2.setColor(Color.RED);
        g2.fillRect(drawPX, drawPY, player.getWidth(), player.getHeight());
    }



    // ===============================
    //           게임 로직
    // ===============================
    @Override
    public void actionPerformed(ActionEvent e) {
        updatePlayer();
        repaint();
    }

    private void updatePlayer() {
        double speed = player.getSpeed();
        double x = player.getX();
        double y = player.getY();

        double nx = x;
        double ny = y;

        if (isKeyDown(KeyEvent.VK_UP))    { ny -= speed; player.setFacing(Player.Facing.UP); }
        if (isKeyDown(KeyEvent.VK_DOWN))  { ny += speed; player.setFacing(Player.Facing.DOWN); }
        if (isKeyDown(KeyEvent.VK_LEFT))  { nx -= speed; player.setFacing(Player.Facing.LEFT); }
        if (isKeyDown(KeyEvent.VK_RIGHT)) { nx += speed; player.setFacing(Player.Facing.RIGHT); }

        if (isAreaFree((int) nx, (int) ny)) {
            player.setX(nx);
            player.setY(ny);
        } else if (isAreaFree((int) x, (int) ny)) {
            player.setY(ny);
        } else if (isAreaFree((int) nx, (int) y)) {
            player.setX(nx);
        }
    }

    private boolean isAreaFree(int x, int y) {
        int w = player.getWidth();
        int h = player.getHeight();

        for (int xx = x; xx < x + w; xx++) {
            for (int yy = y; yy < y + h; yy++) {
                if (currentMap.isBlocked(xx, yy)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isKeyDown(int keyCode) {
        return (keyCode >= 0 && keyCode < keys.length && keys[keyCode]);
    }



    // ===============================
    //        상호작용 관련
    // ===============================
    private Rectangle getInteractionBox() {
        Rectangle pb = player.getBounds();
        int range = 45;

        return switch (player.getFacing()) {
            case UP    -> new Rectangle(pb.x, pb.y - range, pb.width, range);
            case DOWN  -> new Rectangle(pb.x, pb.y + pb.height, pb.width, range);
            case LEFT  -> new Rectangle(pb.x - range, pb.y, range, pb.height);
            case RIGHT -> new Rectangle(pb.x + pb.width, pb.y, range, pb.height);
        };
    }

    private void attemptInteraction() {
        Rectangle interactBox = getInteractionBox();

        Interactable target = null;
        int bestDist = Integer.MAX_VALUE;

        Rectangle pb = player.getBounds();
        int px = pb.x + pb.width / 2;
        int py = pb.y + pb.height / 2;

        for (Interactable it : currentMap.getInteractables()) {
            Rectangle b = it.getBounds();
            if (interactBox.intersects(b)) {
                int cx = b.x + b.width / 2;
                int cy = b.y + b.height / 2;
                int dist = (px - cx)*(px - cx) + (py - cy)*(py - cy);

                if (dist < bestDist) {
                    bestDist = dist;
                    target = it;
                }
            }
        }

        if (target instanceof Door door) {
            if (door.getRequiredFacing() != null &&
                door.getRequiredFacing() != player.getFacing())
                return;
        }

        if (target != null) {
            GameContext ctx = new GameContext(currentMap, player);
            target.interact(ctx);
            currentMap = ctx.getCurrentMap();
        }
    }



    // ===============================
    //       스폰 위치 계산
    // ===============================
    private Point findSafeSpawnNear(GameMap map, int cx, int cy) {
        int pw = player.getWidth();
        int ph = player.getHeight();

        int w = map.getWidth();
        int h = map.getHeight();

        cx = Math.max(0, Math.min(cx, w - pw));
        cy = Math.max(0, Math.min(cy, h - ph));

        for (int r = 0; r <= 300; r++) {
            if (isAreaFree(cx, cy + r)) return new Point(cx, cy + r);
            if (isAreaFree(cx, cy - r)) return new Point(cx, cy - r);
        }

        return new Point(cx, cy);
    }



    // ===============================
    //        키 입력 처리
    // ===============================
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length)
            keys[code] = true;

        if (code == KeyEvent.VK_A)
            attemptInteraction();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length)
            keys[code] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    
    
    
    
    
}
