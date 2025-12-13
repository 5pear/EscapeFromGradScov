import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog {

    private static final int DEFAULT_VOLUME = 50;

    private int tempMaster;
    private int tempBgm;
    private int tempSfx;

    public SettingsDialog(Frame owner) {
        super(owner, "게임 설정", true);
        setSize(480, 340);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        tempMaster = Math.round(SoundManager.get().getMasterVolume() * 100);
        tempBgm    = Math.round(SoundManager.get().getBgmVolume() * 100);
        tempSfx    = Math.round(SoundManager.get().getSfxVolume() * 100);

        JLabel title = new JLabel("게임 설정", SwingConstants.CENTER);
        title.setFont(new Font(null, Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        VolumeRow masterRow = new VolumeRow("마스터", tempMaster);
        VolumeRow bgmRow    = new VolumeRow("배경음", tempBgm);
        VolumeRow sfxRow    = new VolumeRow("효과음", tempSfx);

        center.add(masterRow);
        center.add(Box.createVerticalStrut(12));
        center.add(bgmRow);
        center.add(Box.createVerticalStrut(12));
        center.add(sfxRow);

        add(center, BorderLayout.CENTER);

        JButton resetBtn = new JButton("기본값 복원");
        JButton applyBtn = new JButton("적용");
        JButton closeBtn = new JButton("닫기");

        resetBtn.addActionListener(e -> {
            masterRow.setVolume(DEFAULT_VOLUME);
            bgmRow.setVolume(DEFAULT_VOLUME);
            sfxRow.setVolume(DEFAULT_VOLUME);
        });

        applyBtn.addActionListener(e -> {
            SoundManager.get().setMasterVolume(masterRow.getVolume() / 100f);
            SoundManager.get().setBgmVolume(bgmRow.getVolume() / 100f);
            SoundManager.get().setSfxVolume(sfxRow.getVolume() / 100f);
            dispose();
        });

        closeBtn.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(resetBtn);
        bottom.add(applyBtn);
        bottom.add(closeBtn);

        add(bottom, BorderLayout.SOUTH);
    }

    private static class VolumeRow extends JPanel {

        private final VolumeSlider slider;
        private final JLabel valueLabel;
        private int currentVolume;

        VolumeRow(String labelText, int initialVolume) {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            slider = new VolumeSlider();
            int h = slider.getPreferredSize().height;

            JLabel label = new JLabel(labelText);
            label.setPreferredSize(new Dimension(50, h));

            valueLabel = new JLabel();
            valueLabel.setPreferredSize(new Dimension(30, h));
            valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            slider.setVolume(initialVolume);
            currentVolume = initialVolume;
            valueLabel.setText(initialVolume + "%");

            slider.addChangeListener(e -> {
                currentVolume = slider.getVolume();
                valueLabel.setText(currentVolume + "%");
            });

            JButton left = createArrow("◀", h, () ->
                    slider.setVolume(slider.getVolume() - 5)
            );

            JButton right = createArrow("▶", h, () ->
                    slider.setVolume(slider.getVolume() + 5)
            );

            JButton muteBtn = new JButton("🔇");
            muteBtn.setPreferredSize(new Dimension(h, h));
            muteBtn.setFocusPainted(false);

            final int[] last = { currentVolume };
            final boolean[] muted = { false };

            muteBtn.addActionListener(e -> {
                if (!muted[0]) {
                    last[0] = slider.getVolume();
                    slider.setVolume(0);
                    muted[0] = true;
                } else {
                    slider.setVolume(last[0]);
                    muted[0] = false;
                }
            });

            add(label);
            add(Box.createHorizontalStrut(8));
            add(slider);
            add(Box.createHorizontalStrut(6));
            add(left);
            add(Box.createHorizontalStrut(2));
            add(valueLabel);
            add(Box.createHorizontalStrut(2));
            add(right);
            add(Box.createHorizontalStrut(6));
            add(muteBtn);
        }

        int getVolume() {
            return currentVolume;
        }

        void setVolume(int v) {
            slider.setVolume(v);
            currentVolume = v;
            valueLabel.setText(v + "%");
        }

        private static JButton createArrow(String text, int size, Runnable action) {
            JButton btn = new JButton(text);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(size, size));
            btn.addActionListener(e -> action.run());
            return btn;
        }
    }
}
