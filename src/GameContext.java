import java.awt.Point;
import java.util.function.Consumer;

import javax.swing.JOptionPane;

public class GameContext {

    private GameMap currentMap;
    private final Player player;
    private final GamePanel panel; // [커스텀메세지] 패널 참조

    public GameContext(GameMap currentMap, Player player, GamePanel panel) {
        this.currentMap = currentMap;
        this.player = player;
        this.panel = panel;
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }

    public Player getPlayer() {
        return player;
    }
    //[커스텀메세지]그냥 커스텀메세지 호출
    public void showMessage(String text) {        
    	panel.showDialog(text, null);
    }
    // [커스텀메세지] 상호작용 메시지 (문 이동 등)
    public void showMessage(String text, Runnable nextAction) {
        panel.showDialog(text, nextAction);
    }
    // [QNPC]
    public void showQuestion(String question, String[] options, Consumer<Integer> onAnswer) {
        panel.showQuestion(question, options, onAnswer);
    }

    // 문/상호작용에서 맵 교체 시 호출
    public void changeMap(String targetMapId) {
        currentMap = GameMap.create(targetMapId);

        int pw = player.getWidth();
        int ph = player.getHeight();

        int baseX;
        int baseY;

        if (GameMap.MAP_1F_HALLWAY.equals(targetMapId)) {
            // 101호에서 복도로 나올 때 → 복도 101호 문 앞
            baseX = 235;
            baseY = 310;
            player.setFacing(Player.Facing.UP);

        } else if (GameMap.MAP_ROOM_101.equals(targetMapId)) {
            // 복도에서 101호로 들어갈 때 → 101호 문 앞
            baseX = 770;
            baseY = 900;
            player.setFacing(Player.Facing.DOWN);

        } else {
            // 기타맵이 생기면 중앙
            baseX = currentMap.getWidth() / 2;
            baseY = currentMap.getHeight() / 2;
        }

        Point safe = findSafeSpawnNear(baseX, baseY, pw, ph);
        player.setX(safe.x);
        player.setY(safe.y);
        // [커스텀메세지] 패널에게 "맵이 바뀌었으니 화면도 바꿔라"라고 알림
        panel.setMap(currentMap);
    
    }

    // 기준 좌표 근처에서 이동 가능한 지점 탐색
    private Point findSafeSpawnNear(int cx, int cy, int pw, int ph) {
        int w = currentMap.getWidth();
        int h = currentMap.getHeight();

        cx = Math.max(0, Math.min(cx, w - pw));
        cy = Math.max(0, Math.min(cy, h - ph));

        for (int r = 0; r <= 200; r++) {
            int yDown = cy + r;
            if (isAreaFree(cx, yDown, pw, ph)) return new Point(cx, yDown);

            int yUp = cy - r;
            if (isAreaFree(cx, yUp, pw, ph)) return new Point(cx, yUp);

            int xLeft = cx - r;
            if (isAreaFree(xLeft, cy, pw, ph)) return new Point(xLeft, cy);

            int xRight = cx + r;
            if (isAreaFree(xRight, cy, pw, ph)) return new Point(xRight, cy);
        }

        return new Point(cx, cy);
    }

    private boolean isAreaFree(int x, int y, int w, int h) {
        for (int xx = x; xx < x + w; xx++) {
            for (int yy = y; yy < y + h; yy++) {
                if (currentMap.isBlocked(xx, yy)) {
                    return false;
                }
            }
        }
        return true;
    }
}
