import java.awt.Point;

public class GameContext {

    private GameMap currentMap;
    private final Player player;
    private final DialogueUI dialogueUI;
    
    //  GamePanel을 제어하기 위해 저장
    private final GamePanel gamePanel; 

    //  생성자에서 GamePanel 받기
    public GameContext(GameMap currentMap, Player player, DialogueUI dialogueUI, GamePanel gamePanel) {
        this.currentMap = currentMap;
        this.player = player;
        this.dialogueUI = dialogueUI;
        this.gamePanel = gamePanel; 
    }
    // [교수] 게임 오버 발동
    public void triggerGameOver() {
        gamePanel.switchToGameOver();
    }

    // [교수] 현재 맵에 NPC 추가
    public void addInteractable(Interactable it) {
        currentMap.addInteractable(it);
    }

    public GameMap getCurrentMap() { return currentMap; }
    public Player getPlayer() { return player; }
    public DialogueUI getDialogueUI() { return dialogueUI; }

    public void showMessage(String text) {
        if (dialogueUI != null) dialogueUI.showMessage(text);
        else System.out.println(text);
    }

    public void changeMap(String targetMapId) {
        
    
        if ("OUTRO".equals(targetMapId)) {
            gamePanel.switchToOutro(); 
            return; 
        }

        // 일반 맵 이동 로직
        currentMap = GameMap.create(targetMapId);

        int pw = player.getWidth();
        int ph = player.getHeight();
        int baseX, baseY;

        if (GameMap.MAP_1F_HALLWAY.equals(targetMapId)) {
            baseX = 235; baseY = 310;
            player.setFacing(Player.Facing.UP);
        } else if (GameMap.MAP_ROOM_101.equals(targetMapId)) {
            baseX = 770; baseY = 900;
            player.setFacing(Player.Facing.DOWN);
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
            if (isAreaFree(cx, cy + r, pw, ph)) return new Point(cx, cy + r);
            if (isAreaFree(cx, cy - r, pw, ph)) return new Point(cx, cy - r);
            if (isAreaFree(cx - r, cy, pw, ph)) return new Point(cx - r, cy);
            if (isAreaFree(cx + r, cy, pw, ph)) return new Point(cx + r, cy);
        }
        return new Point(cx, cy);
    }

    private boolean isAreaFree(int x, int y, int w, int h) {
        for (int xx = x; xx < x + w; xx++) {
            for (int yy = y; yy < y + h; yy++) {
                if (currentMap.isBlocked(xx, yy)) return false;
            }
        }
        return true;
    }
}