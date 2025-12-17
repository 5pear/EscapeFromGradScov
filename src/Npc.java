import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Npc implements Interactable {

    private int x, y;
    private int width = 40, height = 40;
    
    private String name;
    private String[] conversations; 
    private String quizQuestion;    
    private String answer;
    private String successMsg;
    private String failMsg;
    
    // [힌트] 보상 아이템 이름 (null이면 보상 없음)
    private String rewardItem; 
    
    private boolean isSolved = false; 

    //  생성자에 rewardItem 추가
    public Npc(int x, int y, String name, String[] conversations, String quizQuestion, String answer, String successMsg, String failMsg, String rewardItem) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.conversations = conversations;
        this.quizQuestion = quizQuestion;
        this.answer = answer;
        this.successMsg = successMsg;
        this.failMsg = failMsg;
        this.rewardItem = rewardItem; // 받아온 아이템 이름 저장
    }

    @Override
    public void interact(GameContext ctx) {
        if (isSolved) {
            ctx.getDialogueUI().showMessage(name + ": 더 할말이 남았나?");
            return;
        }

        ctx.getDialogueUI().showSequence(conversations, () -> {
            if (answer != null) {
                ctx.getDialogueUI().showInput(name + ": " + quizQuestion, (userInput) -> {
                    if (userInput.trim().equals(answer)) {
                        isSolved = true; 
                        
                        // 1. 성공 메시지 출력
                        ctx.getDialogueUI().showMessage(successMsg);
                        
                        // [힌트] 2. 보상 아이템이 있다면 지급!
                        if (rewardItem != null) {
                            ctx.getPlayer().addItem(rewardItem);
                            // 아이템 획득 알림 메시지 추가로 띄우기
                            ctx.getDialogueUI().showMessage("[힌트] " + rewardItem  );
                        }
                        
                    } else {
                        ctx.getDialogueUI().showMessage(failMsg);
                    }
                });
            }
        });
    }

    public void draw(Graphics2D g2, int camX, int camY) {
        int screenX = x - camX;
        int screenY = y - camY;
        //해결시 색상변경(임시)나중에 이미지로 교체
        if (isSolved) g2.setColor(Color.GRAY);
        else g2.setColor(Color.BLUE);
        
        g2.fillRect(screenX, screenY, width, height);
        g2.setColor(Color.WHITE);
        g2.drawString(name, screenX, screenY - 5);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}