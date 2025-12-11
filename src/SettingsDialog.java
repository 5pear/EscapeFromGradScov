import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog {

    public SettingsDialog(Frame owner) {
        super(owner, "게임 설정", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("게임 설정");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font(null, Font.BOLD, 20));

        JLabel desc = new JLabel("여기에 사운드, 조작키 등의 옵션을 배치.");
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(Box.createVerticalStrut(20));
        center.add(title);
        center.add(Box.createVerticalStrut(10));
        center.add(desc);

        add(center, BorderLayout.CENTER);

        JButton closeButton = new JButton("닫기");
        closeButton.addActionListener(e -> dispose());

        JPanel bottom = new JPanel();
        bottom.add(closeButton);
        add(bottom, BorderLayout.SOUTH);
    }
}