import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class VolumeSlider extends JComponent {

    private BufferedImage trackL, trackC, trackR;
    private BufferedImage barL, barC, barR;

    private int volume = 50;
    private final List<ChangeListener> listeners = new ArrayList<>();

    public VolumeSlider() {
        try {
            trackL = ImageIO.read(new File("res/track_left.png"));
            trackC = ImageIO.read(new File("res/track_center.png"));
            trackR = ImageIO.read(new File("res/track_right.png"));

            barL = ImageIO.read(new File("res/bar_left.png"));
            barC = ImageIO.read(new File("res/bar_center.png"));
            barR = ImageIO.read(new File("res/bar_right.png"));
        } catch (Exception e) {
            throw new RuntimeException("VolumeSlider 이미지 로딩 실패", e);
        }

        int h = trackC.getHeight();
        setPreferredSize(new Dimension(180, h));
        setMaximumSize(new Dimension(180, h));

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                updateVolumeByRightCap(e.getX());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateVolumeByRightCap(e.getX());
            }
        };

        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    private void fireStateChanged() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(e);
        }
    }

    private void updateVolumeByRightCap(int mouseX) {
        int w = getWidth();
        int capW = barL.getWidth();

        int centerStart = capW;
        int centerEnd = w - capW;

        int barEndX = Math.max(centerStart,
                       Math.min(mouseX, centerEnd));

        int newVolume = (barEndX - centerStart) * 100
                      / (centerEnd - centerStart);

        if (newVolume != volume) {
            volume = newVolume;
            fireStateChanged();
            repaint();
        }
    }

    public void setVolume(int volume) {
        int v = Math.max(0, Math.min(100, volume));
        if (this.volume != v) {
            this.volume = v;
            fireStateChanged();
            repaint();
        }
    }

    public int getVolume() {
        return volume;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = trackC.getHeight();
        int capW = barL.getWidth();

        g.drawImage(trackL, 0, 0, null);
        g.drawImage(trackC, capW, 0, w - capW * 2, h, null);
        g.drawImage(trackR, w - capW, 0, null);

        int centerWidth = (w - capW * 2) * volume / 100;

        g.drawImage(barL, 0, 0, null);
        if (centerWidth > 0) {
            g.drawImage(barC, capW, 0, centerWidth, h, null);
        }
        g.drawImage(barR, capW + centerWidth, 0, null);
    }
}

