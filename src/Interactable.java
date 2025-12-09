import java.awt.Rectangle;

public interface Interactable {
    Rectangle getBounds();
    void interact(GameContext context);
}
