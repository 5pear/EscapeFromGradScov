import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements KeyListener, ActionListener {

    private GameMap currentMap;
    private Player player;

    private final boolean[] keys = new boolean[256];
    private final Timer timer;

    // 패널(표시 영역) 크기 = 첫 맵(1F 복도) 이미지 크기
    private final int viewWidth;
    private final int viewHeight;
    
    // [커스텀메세지] 대화창 관련 변수
    private boolean isDialogVisible = false; // 대화창이 켜져 있는가?
    private String dialogText = "";          // 현재 보여줄 대사
    
    // [커스텀메세지]폰트 설정 (맑은 고딕, 굵게, 20크기)
    private final Font font = new Font("Malgun Gothic", Font.BOLD, 20);
    // [커스텀메세지] 대화창이 닫히면 실행할 행동을 저장하는 변수
    private Runnable onDialogClose;
    // [QNPC] 질문/선택지 관련 변수
    private boolean isQuestionMode = false;      // 질문 모드인가?
    private String[] currentOptions;             // 선택지들 {"1번", "2번", "3번"}
    private int selectedOptionIndex = 0;         // 현재 선택한 번호
    private Consumer<Integer> onAnswer;          // 정답 선택 시 실행할 행동

 

    public GamePanel() {
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // 1. 초기 맵 로드
        currentMap = GameMap.create(GameMap.MAP_1F_HALLWAY);

        // 2. 패널 크기 = 첫 맵 크기
        viewWidth = currentMap.getWidth();
        viewHeight = currentMap.getHeight();
        setPreferredSize(new Dimension(viewWidth, viewHeight));

        // 3. 플레이어 생성
        player = new Player(0, 0);

        // 4. 초기 스폰 위치
        Point spawn = findSafeSpawnNear(
                currentMap,
                currentMap.getWidth() / 2,
                currentMap.getHeight() / 2
        );

        player.setX(spawn.x);
        player.setY(spawn.y);

        // 5. 게임 루프
        timer = new Timer(16, this);
        timer.start();
    }
    
    // [커스텀메세지] 외부에서 대화창을 띄우는 메서드
    public void showDialog(String text, Runnable onClose) {
        this.dialogText = text;
        this.onDialogClose = onClose; // 할 일을 기억해둠(문넘기 버그 방지)
        this.isDialogVisible = true;
        // 대화창이 떴을 때 키 입력 상태 초기화 (계속 걷는 버그 방지)
        for(int i=0; i<keys.length; i++) keys[i] = false;
    }

    // [커스텀메세지] 대화창 닫기
    public void closeDialog() {
        this.isDialogVisible = false;
        
        // 닫힐 때, 기억해둔 행동이 있다면 실행!
        if (this.onDialogClose != null) {
            this.onDialogClose.run();
            this.onDialogClose = null; // 실행 후 비워둠
        }
    }
    // [QNPC] 질문창 띄우기
    public void showQuestion(String question, String[] options, Consumer<Integer> onAnswer) {
        this.dialogText = question;      // 질문 텍스트는 기존 변수 재활용
        this.currentOptions = options;
        this.onAnswer = onAnswer;
        this.selectedOptionIndex = 0;    // 0번부터 선택 시작
        this.isQuestionMode = true;      // 질문 모드 ON
        
        // 키 입력 초기화
        for(int i=0; i<keys.length; i++) keys[i] = false;
    }

    // [QNPC] 맵 변경 (외부 호출용)
    public void setMap(GameMap map) {
        this.currentMap = map;
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        addKeyListener(this);
        requestFocusInWindow();
    }

    // ===============================
    //       렌더링(카메라 포함)
    // ===============================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 배경 검정
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, viewWidth, viewHeight);

        int mapW = currentMap.getWidth();
        int mapH = currentMap.getHeight();

        // 플레이어 중심을 카메라 중심으로 설정
        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;

        int camX = (int) Math.floor(playerCenterX - viewWidth / 2.0);
        int camY = (int) Math.floor(playerCenterY - viewHeight / 2.0);

        // 맵에서 실제로 그릴 부분 (맵 내부 영역만)
        int srcX1 = Math.max(0, camX);
        int srcY1 = Math.max(0, camY);
        int srcX2 = Math.min(mapW, camX + viewWidth);
        int srcY2 = Math.min(mapH, camY + viewHeight);

        // 화면에 그릴 목적지 영역
        int dstX1 = srcX1 - camX;    // 맵이 왼쪽/위로 벗어났을 경우 보정
        int dstY1 = srcY1 - camY;
        int dstX2 = dstX1 + (srcX2 - srcX1);
        int dstY2 = dstY1 + (srcY2 - srcY1);

        // drawImage 한 번으로 화면에 그림 (빠름!)
        g2.drawImage(
            currentMap.getBaseImage(),
            dstX1, dstY1, dstX2, dstY2,
            srcX1, srcY1, srcX2, srcY2,
            null
        );
        
     
        //[QNPC/NPC]
        if (currentMap != null) {
            for (Interactable obj : currentMap.getInteractables()) {
                if (obj instanceof Npc) {
                    ((Npc) obj).draw(g2, camX, camY);
                }
                // [추가] QuizNpc
                else if (obj instanceof QuizNpc) {
                    ((QuizNpc) obj).draw(g2, camX, camY);
                }
            }
        }

        // 플레이어 렌더링 (카메라 보정)
        int drawPX = (int) Math.round(player.getX() - camX);
        int drawPY = (int) Math.round(player.getY() - camY);

        g2.setColor(Color.RED);
        g2.fillRect(drawPX, drawPY, player.getWidth(), player.getHeight());
        
     // [커스텀메세지] 대화창 그리기 (맨 마지막에 그려야 맨 위에 뜸)       
        if (isDialogVisible || isQuestionMode) {
            // 공통 박스 그리기
            int boxHeight = 150;
            int boxY = viewHeight - boxHeight - 20;

            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRoundRect(20, boxY, viewWidth - 40, boxHeight, 20, 20);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(20, boxY, viewWidth - 40, boxHeight, 20, 20);
            
            g2.setFont(font);

            // [A] 질문(퀴즈) 모드일 때 그리기
            if (isQuestionMode) {
                // 질문 내용
                g2.drawString("Q. " + dialogText, 50, boxY + 40);

                // 선택지 그리기
                g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 18));
                for (int i = 0; i < currentOptions.length; i++) {
                    int optY = boxY + 80 + (i * 25);
                    
                    if (i == selectedOptionIndex) {
                        g2.setColor(Color.YELLOW); // 선택된 건 노란색
                        g2.drawString("▶ " + currentOptions[i], 50, optY);
                    } else {
                        g2.setColor(Color.WHITE);
                        g2.drawString("   " + currentOptions[i], 50, optY);
                    }
                }
            } 
            // [B] 일반 대화 모드일 때 그리기
            else {
                g2.drawString(dialogText, 50, boxY + 60);
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                g2.drawString("PRESS SPACE TO CLOSE", viewWidth - 200, viewHeight - 40);
            }
        }
    }
    



    // ===============================
    //           게임 로직
    // ===============================
    //[이동버그수정]
    @Override
    public void actionPerformed(ActionEvent e) {
    	if (!isDialogVisible) {
            updatePlayer();
        }
        repaint();
    }

    private void updatePlayer() {
        double speed = player.getSpeed();
        double x = player.getX();
        double y = player.getY();

        double nx = x;
        double ny = y;

        if (isKeyDown(KeyEvent.VK_UP))    { ny -= speed; player.setFacing(Player.Facing.UP); }
        if (isKeyDown(KeyEvent.VK_DOWN))  { ny += speed; player.setFacing(Player.Facing.DOWN); }
        if (isKeyDown(KeyEvent.VK_LEFT))  { nx -= speed; player.setFacing(Player.Facing.LEFT); }
        if (isKeyDown(KeyEvent.VK_RIGHT)) { nx += speed; player.setFacing(Player.Facing.RIGHT); }

        if (isAreaFree((int) nx, (int) ny)) {
            player.setX(nx);
            player.setY(ny);
        } else if (isAreaFree((int) x, (int) ny)) {
            player.setY(ny);
        } else if (isAreaFree((int) nx, (int) y)) {
            player.setX(nx);
        }
    }

    private boolean isAreaFree(int x, int y) {
        int w = player.getWidth();
        int h = player.getHeight();

        for (int xx = x; xx < x + w; xx++) {
            for (int yy = y; yy < y + h; yy++) {
                if (currentMap.isBlocked(xx, yy)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isKeyDown(int keyCode) {
        return (keyCode >= 0 && keyCode < keys.length && keys[keyCode]);
    }

    // [커스템메세지] 외부(GameContext)에서 맵을 강제로 바꿀 때 사용
    public void setMap(GameMap map) {
        this.currentMap = map;
    }

    // ===============================
    //        상호작용 관련
    // ===============================
    private Rectangle getInteractionBox() {
        Rectangle pb = player.getBounds();
        int range = 45;

        return switch (player.getFacing()) {
            case UP    -> new Rectangle(pb.x, pb.y - range, pb.width, range);
            case DOWN  -> new Rectangle(pb.x, pb.y + pb.height, pb.width, range);
            case LEFT  -> new Rectangle(pb.x - range, pb.y, range, pb.height);
            case RIGHT -> new Rectangle(pb.x + pb.width, pb.y, range, pb.height);
        };
    }

    private void attemptInteraction() {
        Rectangle interactBox = getInteractionBox();

        Interactable target = null;
        int bestDist = Integer.MAX_VALUE;

        Rectangle pb = player.getBounds();
        int px = pb.x + pb.width / 2;
        int py = pb.y + pb.height / 2;

        for (Interactable it : currentMap.getInteractables()) {
            Rectangle b = it.getBounds();
            if (interactBox.intersects(b)) {
                int cx = b.x + b.width / 2;
                int cy = b.y + b.height / 2;
                int dist = (px - cx)*(px - cx) + (py - cy)*(py - cy);

                if (dist < bestDist) {
                    bestDist = dist;
                    target = it;
                }
            }
        }

        if (target instanceof Door door) {
            if (door.getRequiredFacing() != null &&
                door.getRequiredFacing() != player.getFacing())
                return;
        }

        if (target != null) {
            GameContext ctx = new GameContext(currentMap, player,this);
            target.interact(ctx);         
        }
    }



    // ===============================
    //       스폰 위치 계산
    // ===============================
    private Point findSafeSpawnNear(GameMap map, int cx, int cy) {
        int pw = player.getWidth();
        int ph = player.getHeight();

        int w = map.getWidth();
        int h = map.getHeight();

        cx = Math.max(0, Math.min(cx, w - pw));
        cy = Math.max(0, Math.min(cy, h - ph));

        for (int r = 0; r <= 300; r++) {
            if (isAreaFree(cx, cy + r)) return new Point(cx, cy + r);
            if (isAreaFree(cx, cy - r)) return new Point(cx, cy - r);
        }

        return new Point(cx, cy);
    }



    // ===============================
    //        키 입력 처리
    // ===============================
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        // [QNPC] 질문 모드일 때 키 처리 (방향키로 선택)
        if (isQuestionMode) {
            if (code == KeyEvent.VK_UP) {
                selectedOptionIndex--;
                if (selectedOptionIndex < 0) selectedOptionIndex = currentOptions.length - 1;
            }
            else if (code == KeyEvent.VK_DOWN) {
                selectedOptionIndex++;
                if (selectedOptionIndex >= currentOptions.length) selectedOptionIndex = 0;
            }
            else if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
                isQuestionMode = false; // 질문창 닫기
                if (onAnswer != null) {
                    onAnswer.accept(selectedOptionIndex); // 선택한 번호를 알려줌
                }
            }
            return; // 다른 키 입력 무시
        }
        
        
        
        
        // [커스텀메세지] 대화창이 켜져있을 때 처리
        if (isDialogVisible) {
            if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
                closeDialog(); // 스페이스바 누르면 대화창 닫기
            }
            return; // 이동 키 입력 등을 무시하고 여기서 끝냄
        }
        if (code >= 0 && code < keys.length)
            keys[code] = true;

        if (code == KeyEvent.VK_A)
            attemptInteraction();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length)
            keys[code] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    
    
    
    
    
}
