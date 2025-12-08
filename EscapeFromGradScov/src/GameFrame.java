import javax.swing.JFrame;

public class GameFrame extends JFrame {

    public GameFrame() {
        setTitle("EscapeFromGradScov - 1F Test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel panel = new GamePanel();
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
    }
}
