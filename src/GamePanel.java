import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private static final int PLAYER_W = 20;
    private static final int PLAYER_H = 28;

    private final GameMap map;
    private final Player player;
    private final Timer timer;
    private final boolean[] keys = new boolean[256];

    public GamePanel() {

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        map = new GameMap(
                "res/map/1Fhallway.png",
                "res/map/1Fmove.png"
        );

        Point spawn = findSafeSpawnNear(
                map,
                map.getWidth() / 2,
                map.getHeight() / 2
        );
        player = new Player(spawn.x, spawn.y);

        setPreferredSize(new Dimension(map.getWidth(), map.getHeight()));

        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        addKeyListener(this);
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(map.getBaseImage(), 0, 0, null);

        g.setColor(Color.RED);
        g.fillRect((int)player.getX(), (int)player.getY(), PLAYER_W, PLAYER_H);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        double dx = 0, dy = 0;

        if (keys[KeyEvent.VK_LEFT]) dx -= player.getSpeed();
        if (keys[KeyEvent.VK_RIGHT]) dx += player.getSpeed();
        if (keys[KeyEvent.VK_UP]) dy -= player.getSpeed();
        if (keys[KeyEvent.VK_DOWN]) dy += player.getSpeed();

        movePlayer(dx, dy);
        repaint();
    }

    private void movePlayer(double dx, double dy) {

        if (dx != 0) {
            double nx = player.getX() + dx;
            if (!collidesAt(nx, player.getY())) {
                player.setX(nx);
            }
        }

        if (dy != 0) {
            double ny = player.getY() + dy;
            if (!collidesAt(player.getX(), ny)) {
                player.setY(ny);
            }
        }
    }

    private boolean collidesAt(double x, double y) {

        for (int xx = (int)x; xx < x + PLAYER_W; xx++) {
            for (int yy = (int)y; yy < y + PLAYER_H; yy++) {
                if (map.isBlocked(xx, yy)) return true;
            }
        }
        return false;
    }

    private Point findSafeSpawnNear(GameMap map, int cx, int cy) {

        int w = map.getWidth();
        int h = map.getHeight();

        cx = Math.max(0, Math.min(cx, w - PLAYER_W));
        cy = Math.max(0, Math.min(cy, h - PLAYER_H));

        for (int r = 0; r <= 500; r++) {

            for (int dx = -r; dx <= r; dx++) {
                if (isAreaFree(map, cx + dx, cy - r)) return new Point(cx + dx, cy - r);
                if (isAreaFree(map, cx + dx, cy + r)) return new Point(cx + dx, cy + r);
            }
            for (int dy = -r; dy <= r; dy++) {
                if (isAreaFree(map, cx - r, cy + dy)) return new Point(cx - r, cy + dy);
                if (isAreaFree(map, cx + r, cy + dy)) return new Point(cx + r, cy + dy);
            }
        }

        return new Point(cx, cy);
    }

    private boolean isAreaFree(GameMap map, int x, int y) {

        for (int xx = x; xx < x + PLAYER_W; xx++) {
            for (int yy = y; yy < y + PLAYER_H; yy++) {
                if (map.isBlocked(xx, yy)) return false;
            }
        }
        return true;
    }

    @Override public void keyPressed(KeyEvent e) { keys[e.getKeyCode()] = true; }
    @Override public void keyReleased(KeyEvent e) { keys[e.getKeyCode()] = false; }
    @Override public void keyTyped(KeyEvent e) {}
}
