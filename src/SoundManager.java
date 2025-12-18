import javax.sound.sampled.*;
import java.io.File;
import java.util.*;

public class SoundManager {

    private static final SoundManager instance = new SoundManager();
    public static SoundManager get() {
        return instance;
    }

    private float masterVolume = 0.5f;
    private float bgmVolume = 0.5f;
    private float sfxVolume = 0.5f;

    private final Map<String, List<ClipInfo>> clipMap = new HashMap<>();

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
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(path));
            Clip clip = AudioSystem.getClip();
            clip.open(ais);

            ClipInfo info = new ClipInfo(clip, bgm);
            clipMap.computeIfAbsent(path, k -> new ArrayList<>()).add(info);

            applyVolume(info);

            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(String path) {
        List<ClipInfo> list = clipMap.get(path);
        if (list == null) return;

        for (ClipInfo info : list) {
            info.clip.stop();
            info.clip.close();
        }
        list.clear();
    }

    private void applyVolume(ClipInfo info) {
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
}
