// Npc.java

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Npc implements Interactable {

    private int x, y;
    private int width = 40;  // 기본 크기
    private int height = 60;
    
    private String name;
    private String[] conversations;
    
    private String quizQuestion;
    private String answer;
    private String successMsg;
    private String failMsg;
    private String hintText;

    private boolean isSolved = false;
    private BufferedImage customImage; 
    private Rectangle interactionBounds;

    public Npc(int x, int y, String name, String[] chats, 
               String quiz, String ans, String sMsg, String fMsg, String hint) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.conversations = chats;
        this.quizQuestion = quiz;
        this.answer = ans;
        this.successMsg = sMsg;
        this.failMsg = fMsg;
        this.hintText = hint;
    }

    public void setCustomImage(String path) {
        try {
            // 경로 앞에 슬래시가 없으면 자동으로 붙여줌 (실수 방지)
            if (!path.startsWith("/")) path = "/" + path;
            customImage = ImageIO.read(getClass().getResource(path));
        } catch (Exception e) {
            System.err.println("NPC 이미지 로딩 실패: " + path);
            customImage = null;
        }
    }

    // ✅  크기를 강제로 바꾸는 메서드
    public void setSize(int w, int h) {
        this.width = w;
        this.height = h;
    }

    public void setInteractionBounds(Rectangle bounds) {
        this.interactionBounds = bounds;
    }

    @Override
    public void interact(GameContext ctx) {
        if (isSolved) {
            ctx.getDialogueUI().showMessage(name + ": 더 볼일 없네.");
            return;
        }

        ctx.getDialogueUI().showSequence(conversations, () -> {
            if (answer != null) {
                ctx.getDialogueUI().showInput(name + ": " + quizQuestion, (userInput) -> {
                    if (userInput.trim().equals(answer)) {
                        isSolved = true; 
                        ctx.getDialogueUI().showMessage(successMsg);
                        
                        if ("OUTRO".equals(hintText)) {
                            ctx.changeMap("OUTRO");
                        } else if (hintText != null) {
                            ctx.getDialogueUI().showMessage("[힌트] " + hintText);
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

        if (customImage != null) {
            // 설정된 width, height에 맞춰서 이미지를 늘려서 그림
            g2.drawImage(customImage, screenX, screenY, width, height, null);
        } else {
            g2.setColor(Color.BLUE);
            g2.fillRect(screenX, screenY, width, height);
            g2.setColor(Color.WHITE);
            g2.drawString(name, screenX, screenY - 5);
        }
    }

    @Override
    public Rectangle getBounds() {
        if (interactionBounds != null) {
            return interactionBounds;
        }
        return new Rectangle(x, y, width, height);
    }
}
