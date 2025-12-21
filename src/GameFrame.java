import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {

    private GamePanel gamePanel;
    private StartPanel startMenu;

    private long gameStartTime;
    private long lastPlayTime;

    public GameFrame() {
        setTitle("Escape From GradScov");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new GamePanel(this);
        
        startMenu = new StartPanel(this);

        setContentPane(startMenu);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // ✅ Main에서 setVisible(true) 호출하므로 여기서는 호출하지 않음
        // setVisible(true);

        // StartPanel이 addNotify()에서 focus/intro 시작을 처리하므로 여기서 강제 포커스는 필수 아님
        // startMenu.requestFocusInWindow();
    }

    public void startGame() {
        getContentPane().removeAll();
        
        gamePanel = new GamePanel(this);
        getContentPane().add(gamePanel);

        pack();
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

        pack();
        revalidate();
        repaint();

        panel.requestFocusInWindow();
    }

    public void showStats() {
        getContentPane().removeAll();

        StatsPanel panel =
                new StatsPanel(this, gamePanel.getPreferredSize());

        getContentPane().add(panel);

        pack();
        revalidate();
        repaint();

        panel.requestFocusInWindow();
    }

 

    public void showOutro() {
        getContentPane().removeAll();

    
        OutroPanel outro = new OutroPanel(this); 

        getContentPane().add(outro);
        pack();
        revalidate();
        repaint();
        outro.requestFocusInWindow();
    }
}
