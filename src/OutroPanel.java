import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class OutroPanel extends JPanel {

    private final List<String> credits = new ArrayList<>();

    private int yOffset;
    private int speed = 1;
    private boolean fast = false;

    private Timer scrollTimer;
    private Timer fadeTimer;

    private float thankYouAlpha = 0f;
    private boolean showThankYou = false;
    private boolean restartTriggered = false;

    private final Font creditFont = new Font("Dialog", Font.BOLD, 28);
    private final Font thankYouFont = new Font("Dialog", Font.BOLD, 48);

    private final int lineGap = 50;

    public OutroPanel(GameFrame frame, Dimension size) {
        setPreferredSize(size);
        setBackground(Color.BLACK);
        setFocusable(true);

        initCredits();
        yOffset = size.height + 50;

        scrollTimer = new Timer(16, e -> {
            yOffset -= fast ? 6 : speed;

            int endY = -(credits.size() * lineGap);
            if (yOffset < endY) {
                scrollTimer.stop();
                startThankYou(frame);
            }
            repaint();
        });
        scrollTimer.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                fast = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                fast = false;
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                fast = true;
            }

            @Override
            public void keyReleased(KeyEvent e) {
                fast = false;
            }
        });
    }

    private void initCredits() {
        credits.add("제작");
        credits.add("사연 있는 남자들");
        credits.add("");
        credits.add("기획");
        credits.add("백승현   고현진   김도원   김지훈");
        credits.add("");
        credits.add("개발");
        credits.add("백승현   고현진   김도원   김지훈");
        credits.add("");
        credits.add("맵 디자인,캐릭터 디자인");
        credits.add("백승현");
        credits.add("");
        credits.add("UI");
        credits.add("김지훈");
        credits.add("");
        credits.add("사운드");
        credits.add("김도원");
        credits.add("");
        credits.add("맵 상호작용");
        credits.add("고현진");
    }

    private void startThankYou(GameFrame frame) {
        if (showThankYou) return;

        showThankYou = true;

        fadeTimer = new Timer(40, e -> {
            thankYouAlpha += 0.02f;

            if (thankYouAlpha >= 1f) {
                thankYouAlpha = 1f;
                fadeTimer.stop();

                if (!restartTriggered) {
                    restartTriggered = true;

                    Timer t = new Timer(3000, ev -> {
                        ((Timer) ev.getSource()).stop();

                        frame.dispose();

                        SwingUtilities.invokeLater(() -> {
                            new GameFrame().setVisible(true);
                        });
                    });
                    t.setRepeats(false);
                    t.start();
                }
            }
            repaint();
        });
        fadeTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        g2.setColor(Color.WHITE);
        g2.setFont(creditFont);

        int x = getWidth() / 2;
        int y = yOffset;

        for (String line : credits) {
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(line, x - fm.stringWidth(line) / 2, y);
            y += lineGap;
        }

        if (showThankYou) {
            g2.setFont(thankYouFont);
            g2.setComposite(
                    AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            thankYouAlpha
                    )
            );

            String msg = "Thank you for playing!";
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(msg)) / 2;
            int ty = getHeight() / 2;

            g2.drawString(msg, tx, ty);
        }
    }
}
