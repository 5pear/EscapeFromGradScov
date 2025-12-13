import javax.swing.*;
import java.awt.*;

public class StatsPanel extends JPanel {

    private final Font titleFont = new Font("Dialog", Font.BOLD, 60);
    private final Font textFont  = new Font("Dialog", Font.PLAIN, 26);

    public StatsPanel(GameFrame frame, Dimension size) {
        setPreferredSize(size);
        setBackground(Color.BLACK);
        setLayout(null);

        createButtons(frame);
    }

    private void createButtons(GameFrame frame) {
        JButton close   = createButton("통계 닫기");
        JButton restart = createButton("재시작");
        JButton exit    = createButton("종료");

        int cx = getPreferredSize().width / 2;
        int cy = getPreferredSize().height / 2;

        close.setBounds(cx - 220, cy + 180, 140, 40);
        restart.setBounds(cx - 60, cy + 180, 140, 40);
        exit.setBounds(cx + 100, cy + 180, 140, 40);

        close.addActionListener(e -> frame.setGameOverPanel());

        restart.addActionListener(e -> {
            frame.dispose();
            SwingUtilities.invokeLater(() ->
                    new GameFrame().setVisible(true)
            );
        });

        exit.addActionListener(e -> System.exit(0));

        add(close);
        add(restart);
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.WHITE);

        g2.setFont(titleFont);
        drawCentered(g2, "STATISTICS", -180);

        g2.setFont(textFont);
        GameSession s = GameSession.get();

        drawCentered(g2, "총 플레이 횟수 : " + s.getPlayCount(), -80);
        drawCentered(g2, "게임 오버 횟수 : " + s.getGameOverCount(), -40);
        drawCentered(g2, "누적 플레이 시간 : " + format(s.getTotalPlayTime()), 0);
        drawCentered(g2, "평균 생존 시간 : " + format(s.getAveragePlayTime()), 40);
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
