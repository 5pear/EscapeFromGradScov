import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Npc implements Interactable {

    private int x, y;
    private int width = 20;
    private int height = 28;
    
    private String name;
    private String[] dialogues; // 여러 줄의 대사 목록
    private int dialogIndex = 0; // 현재 말할 대사 번호

    // 생성자: 위치, 이름, 대사들(가변 인자)
    public Npc(int x, int y, String name, String... dialogues) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.dialogues = dialogues;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public void interact(GameContext context) {
        // 대사가 없으면 아무것도 안 함
        if (dialogues == null || dialogues.length == 0) return;

        // 1. 현재 순서의 대사를 커스텀 대화창으로 출력
        // (이름: 내용) 형식으로 보여줌
        context.showMessage(name + ": " + dialogues[dialogIndex]);

        // 2. 다음 대사로 인덱스 넘기기
        dialogIndex++;
        
        // 3. 대사가 끝까지 갔으면 다시 처음(0번)으로 돌아옴
        if (dialogIndex >= dialogues.length) {
            dialogIndex = 0;
        }
    }

    // 일반 NPC는 파란색으로 그리기
    public void draw(Graphics2D g, int camX, int camY) {
        int screenX = x - camX;
        int screenY = y - camY;

        g.setColor(Color.BLUE); // 파란색
        g.fillRect(screenX, screenY, width, height);
        
        g.setColor(Color.WHITE);
        g.drawString(name, screenX, screenY - 5);
    }
}