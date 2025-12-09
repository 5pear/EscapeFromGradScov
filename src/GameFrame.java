import javax.swing.JFrame;

public class GameFrame extends JFrame {

    public GameFrame() {
        setTitle("Escape From GradScov");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel panel = new GamePanel();
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
    }
}
