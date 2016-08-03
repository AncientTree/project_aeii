package net.toyknight.aeii;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import net.toyknight.aeii.utils.FileProvider;

import java.util.Random;

/**
 * @author Zeo 4/7/15.
 */
public class AudioManager {
    private static final String AUDIO_PATH = "audio/";
    private static final String MUSIC_PATH = "music/";
    private static final String SE_PATH = "se/";
    private static final String MAIN_THEME = "main_theme.mp3";
    private static final String[] BGM_LIST = new String[]{"bg_good.mp3", "bg_bad.mp3"};

    public static float musicVolume = 1.0f;
    public static float seVolume = 0.8f;

    private static Music music;

    /**
     * Play a music file by given a the file name(with extension).
     *
     * @param fileName the music file name(with extension)
     */
    public static void playBGM(String fileName, boolean loop) {
        if (musicVolume > 0f) {
            String pathToFile = AUDIO_PATH + MUSIC_PATH + fileName;

            stopCurrentBGM();

            music = Gdx.audio.newMusic(FileProvider.getAssetsFile(pathToFile));
            music.setVolume(musicVolume);
            music.setLooping(loop);

            music.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music music) {
                    if (!music.isLooping()) {
                        stopCurrentBGM();
                    }
                }
            });

            music.play();
        }
    }

    /**
     * Play main theme music.
     */

    public static void loopMainTheme() {
        playBGM(MAIN_THEME, true);
    }

    /**
     * Stop and dispose current music.
     */
    public static void stopCurrentBGM() {
        if (music != null) {
            if (music.isPlaying()) {
                music.stop();
            }
            music.dispose();
        }
    }

    /**
     * Set volume of music.
     *
     * @param volume the volume of music
     */
    public static void setMusicVolume(float volume) {
        musicVolume = volume;
        if (music == null) {
            if (volume > 0f) {
                loopMainTheme();
            }
        } else {
            if (volume > 0f) {
                music.setVolume(musicVolume);
            } else {
                stopCurrentBGM();
                music = null;
            }
        }
    }

    /**
     * Set volume of sound effects.
     *
     * @param volume the volume of sound effects
     */
    public static void setSEVolume(float volume) {
        seVolume = volume;
    }

    /**
     * Play music randomly. All audio files are in "asset/music/" directory in project.
     */
    public static void playRandomBGM() {
        if (musicVolume > 0f) {
            stopCurrentBGM();

            String bgmPath = getRandomBgmPath();
            music = Gdx.audio.newMusic(FileProvider.getAssetsFile(bgmPath));
            music.setVolume(musicVolume);
            music.play();

            music.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music music) {
                    playRandomBGM();
                }
            });
        }
    }

    /**
     * Start playing music randomly with setting the starting one.
     *
     * @param startBGM the starting music file name(with extension)
     */
    public static void playRandomBGM(String startBGM) {
        if (musicVolume > 0f) {
            String pathToFile = AUDIO_PATH + MUSIC_PATH + startBGM;

            stopCurrentBGM();

            music = Gdx.audio.newMusic(FileProvider.getAssetsFile(pathToFile));
            music.setVolume(musicVolume);

            music.play();

            music.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music music) {
                    playRandomBGM();
                }
            });
        }
    }

    /**
     * Play a sound file by given a the file name(with extension).
     *
     * @param fileName the sound file name(with extension)
     * @return the ID of sound
     */
    public static long playSE(String fileName) {
        String pathToFile = AUDIO_PATH + SE_PATH + fileName;

        Sound se = Gdx.audio.newSound(FileProvider.getAssetsFile(pathToFile));

        return se.play(seVolume);
    }

    /**
     * Mute music.
     */
    public static void muteBGM() {
        setMusicVolume(0f);
    }

    /**
     * Mute sound.
     */
    public static void muteSE() {
        seVolume = 0.0f;
    }

    /**
     * Get a random bgm path.
     *
     * @return the bgm path
     */
    private static String getRandomBgmPath() {
        Random random = new Random();
        return AUDIO_PATH + MUSIC_PATH + BGM_LIST[random.nextInt(BGM_LIST.length)];
    }

}
