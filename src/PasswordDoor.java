import java.awt.Graphics2D;
import java.awt.Rectangle;

public class PasswordDoor implements Interactable {

    private int x, y;
    private int width, height;
    
    private String targetMapId;
    private String password;
    private String hintMsg;
    
    // 틀린 횟수 카운트
    private int failCount = 0; 

    public PasswordDoor(int x, int y, int width, int height, String targetMapId, String password, String hintMsg) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.targetMapId = targetMapId;
        this.password = password;
        this.hintMsg = hintMsg;
    }

    @Override
    public void interact(GameContext ctx) {
        ctx.getDialogueUI().showInput(hintMsg, (input) -> {
            
            // 1. 정답인 경우
            if (input.trim().equals(password)) {
                ctx.getDialogueUI().showMessage("잠금장치가 해제되었습니다.");
                ctx.changeMap(targetMapId);
            } 
            // 2. 오답인 경우
            else {
                failCount++; // 실패 횟수 증가
                
                // 3번 틀렸을 때 (게임 오버 이벤트 발동)
                if (failCount >= 3) {
                    triggerTrapEvent(ctx);
                } else {
                    // 아직 기회가 남음
                    int left = 3 - failCount;
                    ctx.getDialogueUI().showMessage("비밀번호가 틀렸습니다.\n(남은 기회: " + left + "번)");
                }
            }
        });
    }

    // 3번 틀렸을 때 발생하는 이벤트
    private void triggerTrapEvent(GameContext ctx) {
        // 1. 플레이어 위치 가져오기
        double px = ctx.getPlayer().getX();
        double py = ctx.getPlayer().getY();

        // 2. 플레이어 바로 옆(오른쪽)에 교수님 NPC 소환 (화면에 보이게)
        // (실제 대화는 여기서 하지 않고 아래 시퀀스에서 처리)
        Npc trapNpc = new Npc(
            (int)px , (int)py-150, 
            "교수",
            new String[]{ "..." }, // (더미 대사)
            null, null, null, null, null
        );
        trapNpc.setCustomImage("/character/professor.png");
        // 2. ✅ [핵심] 크기를 키웁니다 (너비 100, 높이 150)
    
        trapNpc.setSize(200, 200);
        
        ctx.addInteractable(trapNpc);

        // 3. 강제 대화 시퀀스 시작
       
        ctx.getDialogueUI().showSequence(new String[]{
            "!!! 경고음이 울린다 !!!",
            "???: 자네!!! 여기서 뭐 하는 건가!"
        }, () -> {
            // 경고 메시지가 끝나면 교수님의 진짜 무서운 대사 출력
            runGameOverSequence(ctx);
        });
    }

    // 교수님 대화 후 게임오버 처리용 메서드
    private void runGameOverSequence(GameContext ctx) {
        String[] scolding = {
                "교수: 이곳을 빠져나가려고 했구나...",
                "교수: 그럼 죽어."
        };

        // 대화 출력 후 -> 게임 오버 화면으로 전환
        ctx.getDialogueUI().showSequence(scolding, () -> {
            ctx.triggerGameOver(); 
        });
    }

    public void draw(Graphics2D g2, int camX, int camY) {
        // 투명 문이므로 그리지 않음
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}