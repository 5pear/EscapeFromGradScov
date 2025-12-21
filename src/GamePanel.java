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
import java.util.Arrays;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements KeyListener, ActionListener {
    
    private GameFrame parentFrame;
    private GameMap currentMap;
    private Player player;

    private final DialogueUI dialogueUI = new DialogueUI(); 
    private final boolean[] keys = new boolean[256];
    private final Timer timer;

    private final int viewWidth;
    private final int viewHeight;

    public GamePanel(GameFrame parentFrame) {
        this.parentFrame = parentFrame; 
        
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        currentMap = GameMap.create(GameMap.MAP_1F_HALLWAY);

        // ✅ [수정 1] 화면 크기를 1280x720으로 고정해야 카메라가 움직입니다!
        // (이전 코드: viewWidth = currentMap.getWidth(); -> 이렇게 하면 화면이 맵만큼 커져서 안 움직임)
        viewWidth = 1280; 
        viewHeight = 720;
        
        setPreferredSize(new Dimension(viewWidth, viewHeight));

        player = new Player(0, 0);
        player.setRenderScale(0.5);

        Point spawn = findSafeSpawnNear(
                currentMap,
                currentMap.getWidth() / 2,
                currentMap.getHeight() / 2
        );

        player.setX(spawn.x);
        player.setY(spawn.y);

        timer = new Timer(16, this);
        timer.start();
    }

    public void switchToOutro() {
        if (parentFrame != null) {
            parentFrame.showOutro();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        addKeyListener(this);
        requestFocusInWindow();
    }

    private void resetKeyStates() {
        Arrays.fill(keys, false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 배경 검은색 채우기
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, viewWidth, viewHeight);

        int mapW = currentMap.getWidth();
        int mapH = currentMap.getHeight();

        // 카메라 위치 계산
        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;

        int camX = (int) Math.floor(playerCenterX - viewWidth / 2.0);
        int camY = (int) Math.floor(playerCenterY - viewHeight / 2.0);

        // 맵 범위 밖으로 카메라가 나가지 않도록 고정 (Clamping)
        // (맵이 화면보다 작을 때는 0으로 고정)
        if (mapW > viewWidth) {
            camX = Math.max(0, Math.min(camX, mapW - viewWidth));
        } else {
            camX = 0; 
        }

        if (mapH > viewHeight) {
            camY = Math.max(0, Math.min(camY, mapH - viewHeight));
        } else {
            camY = 0;
        }

        // 화면에 보일 부분만 잘라서 그리기 (최적화)
        int srcX1 = Math.max(0, camX);
        int srcY1 = Math.max(0, camY);
        int srcX2 = Math.min(mapW, camX + viewWidth);
        int srcY2 = Math.min(mapH, camY + viewHeight);

        int dstX1 = srcX1 - camX;
        int dstY1 = srcY1 - camY;
        int dstX2 = dstX1 + (srcX2 - srcX1);
        int dstY2 = dstY1 + (srcY2 - srcY1);

        g2.drawImage(currentMap.getBaseImage(), dstX1, dstY1, dstX2, dstY2, srcX1, srcY1, srcX2, srcY2, null);
        
        if (currentMap != null) {
            for (Interactable obj : currentMap.getInteractables()) {
                if (obj instanceof Npc) {
                    ((Npc) obj).draw(g2, camX, camY);
                }
                else if (obj instanceof PasswordDoor) {
                    ((PasswordDoor) obj).draw(g2, camX, camY);
                }
            }
        }

        int drawPX = (int) Math.round(player.getX() - camX);
        int drawPY = (int) Math.round(player.getY() - camY);

        // ✅ [수정 2] 빨간 박스(fillRect) 대신 캐릭터 이미지(player.draw) 그리기
        // g2.setColor(Color.RED);
        // g2.fillRect(drawPX, drawPY, player.getWidth(), player.getHeight());
        player.draw(g2, drawPX, drawPY);
        
        dialogueUI.draw(g2); 
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!dialogueUI.isVisible()) {
            updatePlayer();
        }
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
            resetKeyStates();

            // GameContext 생성
            GameContext ctx = new GameContext(currentMap, player, dialogueUI, this);
            target.interact(ctx);
            
            // 맵 업데이트
            currentMap = ctx.getCurrentMap();

            requestFocusInWindow();
        }
    }

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

    public void switchToGameOver() {
        if (parentFrame != null) {
            parentFrame.showGameOver();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (dialogueUI.isVisible()) {
            if (dialogueUI.isInputMode()) {
                if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_BACK_SPACE) {
                    dialogueUI.handleInput(e.getKeyChar(), code);
                }
            } else {
                if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) dialogueUI.next();
            }
            return;
        }
        if (code >= 0 && code < keys.length) keys[code] = true;
        if (code == KeyEvent.VK_A) attemptInteraction();
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        if (dialogueUI.isVisible() && dialogueUI.isInputMode()) {
            char c = e.getKeyChar();
            if (c != KeyEvent.VK_ENTER && c != KeyEvent.VK_BACK_SPACE) dialogueUI.handleInput(c, -1);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length)
            keys[code] = false;
    }
    
    public DialogueUI getDialogueUI() { return dialogueUI; }
}