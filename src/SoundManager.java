import javax.sound.sampled.*;

public class SoundManager {
    private static int musicVolume = 50;
    private static int soundVolume = 50;
    private static Clip currentMusic;

    public static void playSound(String filename) {
        Clip clip = ResourceManager.getSound(filename);
        if (clip != null) {
            setVolume(clip, soundVolume);
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public static void playMusic(String filename) {
        stopMusic();
        currentMusic = ResourceManager.getSound(filename);
        if (currentMusic != null) {
            setVolume(currentMusic, musicVolume);
            currentMusic.loop(Clip.LOOP_CONTINUOUSLY);
            currentMusic.start();
        }
    }

    public static void stopMusic() {
        if (currentMusic != null && currentMusic.isRunning()) {
            currentMusic.stop();
        }
    }

    public static void setMusicVolume(int volume) {
        musicVolume = volume;
        if (currentMusic != null) {
            setVolume(currentMusic, volume);
        }
    }

    public static void setSoundVolume(int volume) {
        soundVolume = volume;
    }

    public static int getMusicVolume() { return musicVolume; }
    public static int getSoundVolume() { return soundVolume; }

    private static void setVolume(Clip clip, int volume) {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(volume / 100.0) * 20.0);
            gainControl.setValue(dB);
        }
    }
}