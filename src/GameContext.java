import java.awt.Point;
import javax.swing.JOptionPane;

public class GameContext {

    private GameMap currentMap;
    private final Player player;

    public GameContext(GameMap currentMap, Player player) {
        this.currentMap = currentMap;
        this.player = player;
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }

    public Player getPlayer() {
        return player;
    }

    public void showMessage(String text) {
        JOptionPane.showMessageDialog(null, text);
    }

    public void changeMap(String targetMapId) {
        String fromMapId = currentMap.getMapId();
        currentMap = GameMap.create(targetMapId);

        int pw = player.getWidth();
        int ph = player.getHeight();

        int baseX;
        int baseY;

        if (GameMap.MAP_1F_HALLWAY.equals(targetMapId)) {
            // ✅ 방 -> 복도 : 아래(Down)를 바라보게
            if (GameMap.MAP_ROOM_101.equals(fromMapId)) {
                baseX = 235;
                baseY = 310;
            } else if (GameMap.MAP_ROOM_102.equals(fromMapId)) {
                baseX = 540;
                baseY = 295;
            } else if (GameMap.MAP_ROOM_103.equals(fromMapId)) {
                baseX = 830;
                baseY = 295;
            } else if (GameMap.MAP_ROOM_104.equals(fromMapId)) {
                baseX = 1220;
                baseY = 295;
            } else {
                baseX = currentMap.getWidth() / 2;
                baseY = currentMap.getHeight() / 2;
            }

            player.setFacing(Player.Facing.DOWN); // ✅ 변경(기존 UP -> DOWN)

        } else if (GameMap.MAP_ROOM_101.equals(targetMapId)
                || GameMap.MAP_ROOM_102.equals(targetMapId)
                || GameMap.MAP_ROOM_103.equals(targetMapId)
                || GameMap.MAP_ROOM_104.equals(targetMapId)) {

            // ✅ 복도 -> 방 : 위(Up)를 바라보게
            baseX = 770;
            baseY = 900;

            player.setFacing(Player.Facing.UP); // ✅ 변경(기존 DOWN -> UP)

        } else {
            baseX = currentMap.getWidth() / 2;
            baseY = currentMap.getHeight() / 2;
        }

        Point safe = findSafeSpawnNear(baseX, baseY, pw, ph);
        player.setX(safe.x);
        player.setY(safe.y);
    }

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
