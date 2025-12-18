public class GameSession {

    private static final GameSession instance = new GameSession();

    private long totalPlayTime;
    private long lastPlayTime;
    private int playCount;
    private int gameOverCount;

    private GameSession() {}

    public static GameSession get() {
        return instance;
    }

    public void onGameStart() {
        playCount++;
    }

    public void onGameOver(long playTime) {
        lastPlayTime = playTime;
        totalPlayTime += playTime;
        gameOverCount++;
    }

    public long getTotalPlayTime() {
        return totalPlayTime;
    }

    public long getLastPlayTime() {
        return lastPlayTime;
    }

    public int getPlayCount() {
        return playCount;
    }

    public int getGameOverCount() {
        return gameOverCount;
    }

    public long getAveragePlayTime() {
        if (gameOverCount == 0) return 0;
        return totalPlayTime / gameOverCount;
    }
}
