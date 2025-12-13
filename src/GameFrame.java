import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {

    private GamePanel gamePanel;
    private StartPanel startMenu;

    private long gameStartTime;
    private long lastPlayTime;

    public GameFrame() {
        setTitle("EscapeFromGradScov - 1F Test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new GamePanel();
        startMenu = new StartPanel(gamePanel.getPreferredSize());

        setContentPane(startMenu);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        startMenu.requestFocusInWindow();
    }

    public void startGame() {
        getContentPane().removeAll();
        getContentPane().add(gamePanel);

        revalidate();
        repaint();

        gamePanel.requestFocusInWindow();

        gameStartTime = System.currentTimeMillis();
        GameSession.get().onGameStart();
    }

    public void showGameOver() {
        lastPlayTime = System.currentTimeMillis() - gameStartTime;
        GameSession.get().onGameOver(lastPlayTime);

        setGameOverPanel();
    }

    public void setGameOverPanel() {
        getContentPane().removeAll();
        GameOverPanel panel =
                new GameOverPanel(this, gamePanel.getPreferredSize(), lastPlayTime);
        getContentPane().add(panel);

        revalidate();
        repaint();
        panel.requestFocusInWindow();
    }

    public void showStats() {
        getContentPane().removeAll();
        StatsPanel panel =
                new StatsPanel(this, gamePanel.getPreferredSize());
        getContentPane().add(panel);

        revalidate();
        repaint();
        panel.requestFocusInWindow();
    }
    
    public void showOutro() {
        getContentPane().removeAll();

        OutroPanel outro =
                new OutroPanel(this, gamePanel.getPreferredSize());

        getContentPane().add(outro);
        revalidate();
        repaint();

        outro.requestFocusInWindow();
    }
}
