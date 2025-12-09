import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JOptionPane; // 입력창을 위해 필요

public class Npc implements Interactable {

    private String name;
    private int x, y;
    private int width, height;

    // 퀴즈 
    private String question;   // 질문
    private String answer;     // 정답
    private String correctMsg; // 맞았을 때 대사
    private String wrongMsg;   // 틀렸을 때 대사

    // 생성자: 위치, 이름, 질문, 정답, 성공메시지, 실패메시지
    public Npc(int x, int y, String name, String question, String answer, String correctMsg, String wrongMsg) {
        this.x = x;
        this.y = y;
        this.width = 20;
        this.height = 28;
        this.name = name;
        
        this.question = question;
        this.answer = answer;
        this.correctMsg = correctMsg;
        this.wrongMsg = wrongMsg;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // 입력창 띄우기 및 정답 확인
    @Override
    public void interact(GameContext context) {
        // 1. 입력창 띄우기
        String input = JOptionPane.showInputDialog(null, name + ": " + question);

        // 2. 취소 버튼을 눌렀거나 내용을 입력하지 않은 경우 무시
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        // 3. 정답 비교 (공백 제거 후 확인)
        if (input.trim().equals(answer)) {
            // 정답인 경우
            context.showMessage(name + ": " + correctMsg);
        } else {
            // 오답인 경우
            context.showMessage(name + ": " + wrongMsg);
        }
    }

    // 그리기 기능
    public void draw(Graphics2D g, int camX, int camY) {
        int screenX = x - camX;
        int screenY = y - camY;

        g.setColor(Color.BLUE);
        g.fillRect(screenX, screenY, width, height);
        
        g.setColor(Color.WHITE);
        g.drawString(name, screenX, screenY - 5);
    }
}