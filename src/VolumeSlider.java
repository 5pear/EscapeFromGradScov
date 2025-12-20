import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class VolumeSlider extends JComponent {

    private BufferedImage trackL, trackC, trackR;
    private BufferedImage barL, barC, barR;

    private boolean useImages = false;

    private int volume = 50;
    private final List<ChangeListener> listeners = new ArrayList<>();

    public VolumeSlider() {
        // ✅ 이미지가 없으면 크래시 대신 fallback로 동작
        try {
            trackL = ResourceUtils.loadImage("res/track_left.png");
            trackC = ResourceUtils.loadImage("res/track_center.png");
            trackR = ResourceUtils.loadImage("res/track_right.png");

            barL = ResourceUtils.loadImage("res/bar_left.png");
            barC = ResourceUtils.loadImage("res/bar_center.png");
            barR = ResourceUtils.loadImage("res/bar_right.png");

            useImages = (trackL != null && trackC != null && trackR != null
                    && barL != null && barC != null && barR != null);
        } catch (Exception e) {
            useImages = false;
        }

        int h = useImages ? trackC.getHeight() : 18;
        setPreferredSize(new Dimension(180, h));
        setMaximumSize(new Dimension(180, h));

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { updateVolumeByX(e.getX()); }
            @Override public void mouseDragged(MouseEvent e) { updateVolumeByX(e.getX()); }
        };

        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    private void updateVolumeByX(int x) {
        int w = getWidth();
        if (w <= 0) return;

        int newVolume;
        if (useImages) {
            int capW = trackL.getWidth();
            int centerStart = capW;
            int centerEnd = w - capW;

            x = Math.max(0, Math.min(w, x));

            if (x <= centerStart) newVolume = 0;
            else if (x >= centerEnd) newVolume = 100;
            else newVolume = (x - centerStart) * 100 / (centerEnd - centerStart);
        } else {
            x = Math.max(0, Math.min(w, x));
            newVolume = x * 100 / w;
        }

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

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    private void fireStateChanged() {
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener l : listeners) l.stateChanged(evt);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (useImages) {
            int capW = trackL.getWidth();

            g2.drawImage(trackL, 0, 0, null);
            g2.drawImage(trackC, capW, 0, w - capW * 2, h, null);
            g2.drawImage(trackR, w - capW, 0, null);

            int centerWidth = (w - capW * 2) * volume / 100;

            g2.drawImage(barL, 0, 0, null);
            if (centerWidth > 0) {
                g2.drawImage(barC, capW, 0, centerWidth, h, null);
            }
            g2.drawImage(barR, capW + centerWidth, 0, null);

        } else {
            // ✅ fallback: 심플 바
            g2.setColor(new Color(60, 60, 60));
            g2.fillRoundRect(0, 0, w, h, 8, 8);

            int fillW = (int) Math.round(w * (volume / 100.0));
            g2.setColor(new Color(220, 220, 220));
            g2.fillRoundRect(0, 0, fillW, h, 8, 8);

            g2.setColor(Color.BLACK);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
        }

        g2.dispose();
    }
}
