import javax.sound.sampled.*;
import java.io.File;
import java.net.URL;
import java.util.*;

public class SoundManager {

    
    private static final SoundManager instance = new SoundManager();
    public static SoundManager get() { return instance; }

    
    private float masterVolume = 0.5f;
    private float bgmVolume = 0.5f;
    private float sfxVolume = 0.5f;

    
    private final Map<String, List<ClipInfo>> clipMap = new HashMap<>();

    
    private String currentBgmPath = null;

   
    private int sfxPlayingCount = 0;

    private SoundManager() {}

    private static class ClipInfo {
        Clip clip;
        boolean bgm;

        ClipInfo(Clip clip, boolean bgm) {
            this.clip = clip;
            this.bgm = bgm;
        }
    }

    
    public void play(String path, boolean bgm, boolean loop) {
        try {
            Clip clip = openClip(path);

            ClipInfo info = new ClipInfo(clip, bgm);
            clipMap.computeIfAbsent(path, k -> new ArrayList<>()).add(info);

            applyVolume(info);

            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            clip.start(); 
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public void stop(String path) {
        List<ClipInfo> list = clipMap.get(path);
        if (list == null) return;

        for (ClipInfo info : list) {
            safeStopClose(info.clip);
        }
        list.clear();

        
        if (Objects.equals(currentBgmPath, path)) {
            currentBgmPath = null;
        }
    }

   
   
    public void startBgmLoop(String bgmPath) {
        
        if (currentBgmPath != null && !currentBgmPath.equals(bgmPath)) {
            stop(currentBgmPath);
        }

        currentBgmPath = bgmPath;
   
        stop(bgmPath);
        play(bgmPath, true, true);
    }

  
    public void pauseBgm() {
        if (currentBgmPath == null) return;
        List<ClipInfo> list = clipMap.get(currentBgmPath);
        if (list == null) return;

        for (ClipInfo info : list) {
            if (info.bgm && info.clip != null && info.clip.isRunning()) {
                info.clip.stop(); 
            }
        }
    }

    
    public void resumeBgm() {
        if (currentBgmPath == null) return;
        List<ClipInfo> list = clipMap.get(currentBgmPath);
        if (list == null) return;

        for (ClipInfo info : list) {
            if (info.bgm && info.clip != null && !info.clip.isRunning()) {
                info.clip.start();
            }
        }
    }

 
    public void stopBgm() {
        if (currentBgmPath == null) return;
        stop(currentBgmPath);
        currentBgmPath = null;
    }

  
    public void playSfxOncePauseBgm(String sfxPath) {
        try {
            Clip clip = openClip(sfxPath);

            ClipInfo info = new ClipInfo(clip, false);
            clipMap.computeIfAbsent(sfxPath, k -> new ArrayList<>()).add(info);

            applyVolume(info);

    
            sfxPlayingCount++;
            pauseBgm();

 
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    safeStopClose(clip);
                    removeClipInfo(sfxPath, clip);

                    sfxPlayingCount = Math.max(0, sfxPlayingCount - 1);
                    if (sfxPlayingCount == 0) {
                        resumeBgm();
                    }
                }
            });

            clip.start();

        } catch (Exception e) {
            e.printStackTrace();
            sfxPlayingCount = Math.max(0, sfxPlayingCount - 1);
            if (sfxPlayingCount == 0) resumeBgm();
        }
    }


    private void applyVolume(ClipInfo info) {
        if (info.clip == null) return;
        if (!info.clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;

        float volume = masterVolume * (info.bgm ? bgmVolume : sfxVolume);
        FloatControl gain = (FloatControl) info.clip.getControl(FloatControl.Type.MASTER_GAIN);

        float min = gain.getMinimum();
        float max = gain.getMaximum();

        float dB = (volume <= 0f)
                ? min
                : Math.max(min, Math.min(max, (float) (20.0 * Math.log10(volume))));

        gain.setValue(dB);
    }

    private void updateAllClips() {
        for (List<ClipInfo> list : clipMap.values()) {
            for (ClipInfo info : list) {
                applyVolume(info);
            }
        }
    }

    public void setMasterVolume(float v) {
        masterVolume = clamp(v);
        updateAllClips();
    }

    public void setBgmVolume(float v) {
        bgmVolume = clamp(v);
        updateAllClips();
    }

    public void setSfxVolume(float v) {
        sfxVolume = clamp(v);
        updateAllClips();
    }

    public float getMasterVolume() { return masterVolume; }
    public float getBgmVolume() { return bgmVolume; }
    public float getSfxVolume() { return sfxVolume; }

    private float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private Clip openClip(String path) throws Exception {
        URL resourceUrl = ResourceUtils.getResourceUrl(path);
        AudioInputStream ais = (resourceUrl != null)
                ? AudioSystem.getAudioInputStream(resourceUrl)
                : AudioSystem.getAudioInputStream(new File(path));

        Clip clip = AudioSystem.getClip();
        clip.open(ais);
        return clip;
    }

    private void safeStopClose(Clip clip) {
        try {
            if (clip != null) {
                clip.stop();
                clip.close();
            }
        } catch (Exception ignored) {}
    }

    private void removeClipInfo(String path, Clip clip) {
        List<ClipInfo> list = clipMap.get(path);
        if (list == null) return;

        list.removeIf(info -> info.clip == clip);
        if (list.isEmpty()) {
            clipMap.remove(path);
        }
    }
}
 