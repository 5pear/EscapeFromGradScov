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
        
        javax.swing.Timer t = new javax.swing.Timer(3000, e -> showOutro());
        t.setRepeats(false);
        t.start();
    }
    
    public void showStartMenu() {
        getContentPane().removeAll();
        startMenu = new StartPanel(gamePanel.getPreferredSize());
        setContentPane(startMenu);

        revalidate();
        repaint();

        startMenu.setFocusable(true);
        startMenu.requestFocusInWindow();
    }
    
    public void showOutro() {
        getContentPane().removeAll();

        Dimension size = gamePanel.getPreferredSize();
        OutroPanel outroPanel = new OutroPanel(this, size);

        getContentPane().add(outroPanel);

        revalidate();
        repaint();

        outroPanel.setFocusable(true);
        outroPanel.requestFocusInWindow();
    }
}