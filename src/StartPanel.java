import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Dimension;

public class StartPanel extends JPanel implements KeyListener {
	
	private JLabel pressEnterLabel;
	private Timer blinkTimer;
	private boolean isEnter = false;
	
	private final int SLOW_BLINK_RATE = 500;
	private final int FAST_BLINK_RATE = 50;
	private final int BLINK_COUNT = 10;
	private int blinkCounter = 0;
	
    public StartPanel(Dimension size) {
        setPreferredSize(size);
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());
        
        JPanel textGroupPanel = new JPanel();
        textGroupPanel.setLayout(new BoxLayout(textGroupPanel, BoxLayout.Y_AXIS));
        textGroupPanel.setBackground(Color.BLACK);
        
        JLabel titleLabel = new JLabel("EscapeFromGradScov");
        titleLabel.setFont(new Font(null, Font.BOLD, 100));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        pressEnterLabel = new JLabel("[press enter to start]");
        pressEnterLabel.setFont(new Font(null, Font.PLAIN, 30));
        pressEnterLabel.setForeground(Color.WHITE);
        pressEnterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        textGroupPanel.add(Box.createVerticalStrut(size.height / 3));
        textGroupPanel.add(titleLabel);
        textGroupPanel.add(Box.createVerticalStrut(100));
        textGroupPanel.add(pressEnterLabel);
        add(textGroupPanel, BorderLayout.CENTER);
        
        JPanel topPanel = new JPanel(new BorderLayout());
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

        startSlowBlinking();
        
        addKeyListener(this);
        setFocusable(true);
    }
    
    private void startSlowBlinking() {
        if (blinkTimer != null) {
            blinkTimer.stop();
        }
        ActionListener blinkAction = e -> {
            boolean isVisible = pressEnterLabel.isVisible();
            pressEnterLabel.setVisible(!isVisible);
        };

        blinkTimer = new Timer(SLOW_BLINK_RATE, blinkAction);
        blinkTimer.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && !isEnter) {
            
            isEnter = true;
            blinkTimer.stop();
            
            startFastBlinkTransition();
        }
    }
    
    private void startFastBlinkTransition() {
        Timer fastBlinkTimer = new Timer(FAST_BLINK_RATE, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pressEnterLabel.setVisible(!pressEnterLabel.isVisible());
                blinkCounter++;

                if (blinkCounter >= BLINK_COUNT) {
                    ((Timer)e.getSource()).stop();
                    pressEnterLabel.setVisible(false);

                    startDelayAndGame();
                }
            }
        });
        fastBlinkTimer.start();
    }
    
    private void startDelayAndGame() {
        int delay = 500;
        
        Timer delayTimer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer)e.getSource()).stop();
                
                JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(StartPanel.this);
                if (parentFrame instanceof GameFrame) {
                    ((GameFrame) parentFrame).startGame();
                }
            }
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }
    
    private void openSettingsDialog() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parent == null) return;

        SettingsDialog dialog = new SettingsDialog(parent);
        dialog.setVisible(true);
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
}
