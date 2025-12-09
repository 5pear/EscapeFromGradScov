import java.awt.Rectangle;

public class Door implements Interactable {

    private final Rectangle bounds;
    private final String targetMapId;
    private final String message;
    private final Player.Facing requiredFacing;

    public Door(int x, int y, int width, int height,
                String targetMapId, String message,
                Player.Facing requiredFacing) {
        this.bounds = new Rectangle(x, y, width, height);
        this.targetMapId = targetMapId;
        this.message = message;
        this.requiredFacing = requiredFacing;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    public Player.Facing getRequiredFacing() {
        return requiredFacing;
    }

    @Override
    public void interact(GameContext context) {
        if (message != null && !message.isEmpty()) {
            context.showMessage(message);
        }
        context.changeMap(targetMapId);
    }
}
