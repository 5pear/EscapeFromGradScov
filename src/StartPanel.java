import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class StartPanel extends JPanel implements KeyListener {

    private GameFrame parentFrame;
    private JLabel pressEnterLabel;
    private Timer blinkTimer;
    private Timer fastBlinkTimer;
    private Timer introTimer;

    private boolean isEnter = false;
    private boolean introFinished = false;

    private final int SLOW_BLINK_RATE = 500;
    private final int FAST_BLINK_RATE = 50;
    private final int BLINK_COUNT = 10;
    private int blinkCounter = 0;

    private BufferedImage introImage;
    private int introY;

    private int introStartY;
    private int introEndY;

    private JPanel textGroupPanel;
    private JPanel topPanel;

    // 
    public StartPanel(GameFrame parentFrame) {
        this.parentFrame = parentFrame;
        
        // 크기는 GameFrame 크기를 따라갑니다.
        setPreferredSize(new Dimension(1280, 720)); 
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        loadIntroImage();
        buildUi(); // buildUi에 size 전달 제거
        setUiVisible(false);

        addKeyListener(this);
        setFocusable(true);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
        SwingUtilities.invokeLater(this::startIntroScroll);
    }

    private void loadIntroImage() {
        try {
            
            introImage = ImageIO.read(getClass().getResource("/intro.png"));
        } catch (Exception e) {
            System.err.println("인트로 이미지를 찾을 수 없습니다: /intro.png");
            introImage = null;
            introFinished = true;
        }
    }

    private void buildUi() {
        textGroupPanel = new JPanel();
        textGroupPanel.setOpaque(false);
        textGroupPanel.setLayout(new BoxLayout(textGroupPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("EscapeFromGradScov");
        titleLabel.setFont(new Font(null, Font.BOLD, 100));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        pressEnterLabel = new JLabel("[press enter to start]");
        pressEnterLabel.setFont(new Font(null, Font.PLAIN, 30));
        pressEnterLabel.setForeground(Color.WHITE);
        pressEnterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 여백 조정
        textGroupPanel.add(Box.createVerticalStrut(200)); 
        textGroupPanel.add(titleLabel);
        textGroupPanel.add(Box.createVerticalStrut(80));
        textGroupPanel.add(pressEnterLabel);

        add(textGroupPanel, BorderLayout.CENTER);

        topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JButton settingsButton = new JButton("⚙");
        settingsButton.setFont(new Font(null, Font.PLAIN, 40));
        settingsButton.setForeground(Color.WHITE);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setFocusPainted(false);
        settingsButton.setFocusable(false);
        settingsButton.addActionListener(e -> openSettingsDialog());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(settingsButton);

        topPanel.add(rightPanel, BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);
    }

    private void setUiVisible(boolean visible) {
        textGroupPanel.setVisible(visible);
        topPanel.setVisible(visible);
    }

    private void startIntroScroll() {
        // ✅ [수정 3] SoundManager가 없어도 에러 안 나도록 예외처리
        try {
            SoundManager.get().play("/sound/introsound.wav", true, true);
        } catch (Exception e) {
            // 사운드 파일이 없거나 매니저가 없으면 무시하고 넘어감
        }

        if (introImage == null) {
            introFinished = true;
            setUiVisible(true);
            startSlowBlinking();
            return;
        }

        int panelW = 1280; // 고정 해상도 사용
        int panelH = 720;

        float scale = (float) panelW / introImage.getWidth();
        int scaledH = Math.round(introImage.getHeight() * scale);

        BufferedImage scaled = new BufferedImage(panelW, scaledH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.drawImage(introImage, 0, 0, panelW, scaledH, null);
        g2.dispose();
        introImage = scaled;

        introStartY = panelH - introImage.getHeight(); 
        introEndY = 0;                                 
        introY = introStartY;

        introTimer = new Timer(16, e -> {
            introY += 2;

            if (introY >= introEndY) {
                introY = introEndY;
                introTimer.stop();
                introFinished = true;
                setUiVisible(true);
                startSlowBlinking();
            }
            repaint();
        });
        introTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (introImage != null) {
            g.drawImage(introImage, 0, introY, null);
        }
    }

    private void startSlowBlinking() {
        blinkTimer = new Timer(SLOW_BLINK_RATE, e ->
                pressEnterLabel.setVisible(!pressEnterLabel.isVisible())
        );
        blinkTimer.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!introFinished && e.getKeyCode() == KeyEvent.VK_ENTER) {
            skipIntro();
            return;
        }

        if (introFinished && e.getKeyCode() == KeyEvent.VK_ENTER && !isEnter) {
            isEnter = true;
            
            try {
                SoundManager.get().play("/sound/start.wav", false, false);
                SoundManager.get().stop("/sound/introsound.wav");
            } catch (Exception ex) {}

            if (blinkTimer != null) blinkTimer.stop();
            startFastBlinkTransition();
        }
    }

    private void skipIntro() {
        if (introTimer != null) introTimer.stop();

        introY = introEndY;
        introFinished = true;

        setUiVisible(true);
        startSlowBlinking();
        repaint();
    }

    private void startFastBlinkTransition() {
        fastBlinkTimer = new Timer(FAST_BLINK_RATE, e -> {
            pressEnterLabel.setVisible(!pressEnterLabel.isVisible());
            blinkCounter++;
            if (blinkCounter >= BLINK_COUNT) {
                fastBlinkTimer.stop();
                startDelayAndGame();
            }
        });
        fastBlinkTimer.start();
    }

    private void startDelayAndGame() {
        new Timer(500, e -> {
            ((Timer) e.getSource()).stop();
            parentFrame.startGame(); // GameFrame 바로 호출
        }).start();
    }

    private void openSettingsDialog() {
        if (!introFinished) return;
        // SettingsDialog가 있으면 띄우고, 없으면 에러 방지
        try {
            new SettingsDialog(parentFrame).setVisible(true);
        } catch (Exception e) {
            System.out.println("설정 창(SettingsDialog) 파일을 찾을 수 없습니다.");
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}