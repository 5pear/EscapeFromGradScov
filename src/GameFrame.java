import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;

public class GameFrame extends JFrame {

    private GamePanel gamePanel; 
    private StartPanel startMenu; 

    public GameFrame() {
        setTitle("EscapeFromGradScov - 1F Test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        gamePanel = new GamePanel();
        
        startMenu = new StartPanel(gamePanel.getPreferredSize());

        setContentPane(startMenu);

        pack();
        setLocationRelativeTo(null);

        startMenu.setFocusable(true);
        startMenu.requestFocusInWindow();
    }
    
    public void startGame() {
        getContentPane().removeAll();
        getContentPane().add(gamePanel);
        
        revalidate();
        repaint();
        
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();
    }
}