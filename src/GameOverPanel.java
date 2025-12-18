import javax.swing.*;
import java.awt.*;

public class GameOverPanel extends JPanel {

    private float alpha = 0f;
    private final long playTimeMillis;
    private Timer fadeTimer;

    private final Font titleFont = new Font("Dialog", Font.BOLD, 80);
    private final Font textFont  = new Font("Dialog", Font.PLAIN, 24);

    public GameOverPanel(GameFrame frame, Dimension size, long playTimeMillis) {
        this.playTimeMillis = playTimeMillis;

        setPreferredSize(size);
        setBackground(Color.BLACK);
        setLayout(null);

        createButtons(frame);
        startFade();
    }

    private void createButtons(GameFrame frame) {
        JButton stats = createButton("통계 보기");
        JButton exit  = createButton("종료");

        int cx = getPreferredSize().width / 2;
        int cy = getPreferredSize().height / 2;

        stats.setBounds(cx - 160, cy + 140, 140, 40);
        exit.setBounds(cx + 20, cy + 140, 140, 40);

        stats.addActionListener(e -> frame.showStats());
        exit.addActionListener(e -> System.exit(0));

        add(stats);
        add(exit);
    }

    private JButton createButton(String t) {
        JButton b = new JButton(t);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Dialog", Font.BOLD, 22));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        return b;
    }

    private void startFade() {
        fadeTimer = new Timer(40, e -> {
            alpha += 0.03f;
            if (alpha >= 1f) {
                alpha = 1f;
                fadeTimer.stop();
            }
            repaint();
        });
        fadeTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setComposite(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
        );
        g2.setColor(Color.WHITE);

        g2.setFont(titleFont);
        drawCentered(g2, "GAME OVER", -120);

        g2.setFont(textFont);
        drawCentered(g2, "이번 플레이 시간 : " + format(playTimeMillis), -20);
    }

    private void drawCentered(Graphics2D g2, String s, int dy) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(s)) / 2;
        int y = getHeight() / 2 + dy;
        g2.drawString(s, x, y);
    }

    private String format(long ms) {
        long sec = ms / 1000;
        return (sec / 60) + "m " + (sec % 60) + "s";
    }
}
