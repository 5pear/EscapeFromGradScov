import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.function.Consumer;

public class DialogueUI {

    private boolean visible = false;
    private String currentText = "";
    
    // 입력 모드 관련
    private boolean isInputMode = false;
    private StringBuilder inputBuffer = new StringBuilder();
    private Consumer<String> onInputComplete;

    // [추가] 순차 대화 관련 변수
    private String[] dialogueQueue; // 대화 내용 배열
    private int queueIndex = 0;     // 현재 몇 번째 대화인지
    private Runnable onSequenceFinish; // 대화가 다 끝난 후 실행할 행동(퀴즈 등)

    private final int height = 150;
    private final int margin = 20;

    // 1. (수정됨) 대화 시퀀스 시작 메서드
    public void showSequence(String[] texts, Runnable onFinish) {
        this.dialogueQueue = texts;
        this.onSequenceFinish = onFinish;
        this.queueIndex = 0;
        this.visible = true;
        this.isInputMode = false;
        
        // 첫 번째 대화 보여주기
        if (texts != null && texts.length > 0) {
            this.currentText = texts[0];
        } else {
            // 대화가 없으면 바로 종료 동작 실행
            finishSequence();
        }
    }

    // 2. 단일 메시지 (기존 호환용)
    public void showMessage(String text) {
        showSequence(new String[]{text}, null);
    }

    // 3. 입력창 띄우기 (기존과 동일)
    public void showInput(String question, Consumer<String> callback) {
        this.currentText = question;
        this.onInputComplete = callback;
        this.visible = true;
        this.isInputMode = true;
        this.inputBuffer.setLength(0);
        this.dialogueQueue = null; // 대화 모드 해제
    }

    // [핵심] 다음 대화로 넘기기 (Space/Enter 누를 때 호출)
    public void next() {
        // 입력 모드일 때는 다음으로 넘기지 않음
        if (isInputMode) return;

        // 대화 큐가 있다면 다음 대화 표시
        if (dialogueQueue != null) {
            queueIndex++;
            if (queueIndex < dialogueQueue.length) {
                currentText = dialogueQueue[queueIndex];
            } else {
                // 대화 끝
                finishSequence();
            }
        } else {
            hide();
        }
    }

    private void finishSequence() {
        // 후속 작업(퀴즈)이 있다면 실행
        if (onSequenceFinish != null) {
            onSequenceFinish.run();
        } else {
            hide(); // 할 일 없으면 창 닫기
        }
    }

    public void hide() {
        this.visible = false;
        this.isInputMode = false;
        this.dialogueQueue = null;
    }

    // ... handleInput, isVisible, isInputMode 등 getter는 기존 유지 ...
    public boolean isVisible() { return visible; }
    public boolean isInputMode() { return isInputMode; }

    public void handleInput(char keyChar, int keyCode) {
        if (!isInputMode) return;
        if (keyCode == 8) { 
            if (inputBuffer.length() > 0) inputBuffer.deleteCharAt(inputBuffer.length() - 1);
        } else if (keyCode == 10) { 
            if (onInputComplete != null) onInputComplete.accept(inputBuffer.toString());
        } else {
            if (Character.isDefined(keyChar) && keyCode != 10 && keyCode != 8) inputBuffer.append(keyChar);
        }
    }

    public void draw(Graphics2D g2) {
        if (!visible) return;
        
        int viewW = (g2.getClipBounds() != null) ? g2.getClipBounds().width : 800;
        int viewH = (g2.getClipBounds() != null) ? g2.getClipBounds().height : 600;
        int x = margin;
        int y = viewH - height - margin;
        int w = viewW - (margin * 2);

        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRoundRect(x, y, w, height, 20, 20);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, w, height, 20, 20);

        g2.setFont(new Font("나눔고딕", Font.BOLD, 24));
        g2.drawString(currentText, x + 30, y + 50);

        if (isInputMode) {
            g2.setColor(Color.YELLOW);
            g2.drawString("> " + inputBuffer.toString() + "_", x + 30, y + 100);
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString("정답 입력 후 ENTER", x + w - 200, y + height - 20);
        } else {
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            // 대화가 더 남았으면 Next, 아니면 Close 표시
            if (dialogueQueue != null && queueIndex < dialogueQueue.length - 1) {
                g2.drawString("SPACE to Next ▶", x + w - 150, y + height - 20);
            } else {
                g2.drawString("SPACE to Close", x + w - 150, y + height - 20);
            }
        }
    }
}