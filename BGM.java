import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BGMFolderPlayer {

    private static MediaPlayer player;
    private static final List<File> playlist = new ArrayList<>();
    private static int currentIndex = 0;
    private static boolean fxInit = false;

    /** JavaFX 초기화 (한 번만) */
    private static void initFX() {
        if (!fxInit) {
            new JFXPanel();
            fxInit = true;
        }
    }

   
    public static void playFolderLoop(String folderPath) {
        initFX();

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("유효한 폴더가 아닙니다: " + folderPath);
        }

        playlist.clear();
        currentIndex = 0;

        
        for (File f : folder.listFiles()) {
            if (f.getName().toLowerCase().endsWith(".mp3")) {
                playlist.add(f);
            }
        }

        if (playlist.isEmpty()) {
            throw new RuntimeException("폴더에 mp3 파일이 없습니다.");
        }

        playCurrent();
    }

   
    private static void playCurrent() {
        Platform.runLater(() -> {
            try {
                if (player != null) {
                    player.stop();
                    player.dispose();
                }

                File file = playlist.get(currentIndex);
                Media media = new Media(file.toURI().toString());
                player = new MediaPlayer(media);

                player.setVolume(0.5); // 기본 볼륨

                player.setOnEndOfMedia(() -> {
                    currentIndex++;
                    if (currentIndex >= playlist.size()) {
                        currentIndex = 0; // 다시 처음으로
                    }
                    playCurrent();
                });

                player.play();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    
    public static void stop() {
        Platform.runLater(() -> {
            if (player != null) {
                player.stop();
                player.dispose();
                player = null;
            }
        });
    }


    public static void setVolume(double volume) {
        Platform.runLater(() -> {
            if (player != null) {
                player.setVolume(Math.max(0.0, Math.min(1.0, volume)));
            }
        });
    }
}
