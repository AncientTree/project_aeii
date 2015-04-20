package com.toyknight.aeii;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.toyknight.aeii.utils.FileProvider;
import com.toyknight.aeii.utils.Platform;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Random;

/**
 * Created by Zeo on 4/7/15.
 */
public class AudioManager {
    private static final String AUDIO_PATH = "audio/";
    private static final String MUSIC_PATH = "music/";
    private static final String SE_PATH = "se/";
    private static final String MAIN_THEME = "main_theme.ogg";

    public static float musicVolume = 1.0f;
    public static float seVolume = 0.8f;

    private static Music music;
    private static Sound se;

    /**
     * Play a music file by given a the file name(with extension).
     *
     * @param fileName the music file name(with extension)
     */
    public static void playBGM(String fileName) {
        String pathToFile = AUDIO_PATH + MUSIC_PATH + fileName;

        stopMusic();

        music = Gdx.audio.newMusic(FileProvider.getAssetsFile(pathToFile));
        music.setVolume(musicVolume);

        music.play();

        // Dispose after playing.
        music.setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                music.dispose();
            }
        });
    }

    /**
     * Play main theme music.
     */
    public static void playMainTheme() {
        playBGM(MAIN_THEME);
    }

    /**
     * Stop and dispose current music.
     */
    public static void stopCurrentBGM() {
        if (music != null) {
            music.stop();
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
        if (music != null) {
            music.setVolume(musicVolume);
        }
    }

    /**
     * Play music randomly. All audio files are in "asset/music/" directory in project.
     */
    public static void playRandomBGM() {
        String path = AUDIO_PATH + MUSIC_PATH;
        FileHandle[] musicFiles = FileProvider.getAssetsFile(path).list(filterAudioExtensions());
        int start = 0;
        int end = musicFiles.length - 1;

        stopMusic();

        int randomIndex = getRandomInt(start, end);
        music = Gdx.audio.newMusic(musicFiles[randomIndex]);
        music.setVolume(musicVolume);
        music.play();

        music.setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                playRandomBGM();
            }
        });
    }

    /**
     * Start playing music randomly with setting the starting one.
     *
     * @param startBGM the starting music file name(with extension)
     */
    public static void playRandomBGM(String startBGM) {
        String pathToFile = AUDIO_PATH + MUSIC_PATH + startBGM;

        stopMusic();

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

    /**
     * Play a sound file by given a the file name(with extension).
     *
     * @param fileName the sound file name(with extension)
     * @return the ID of sound
     */
    public static long playSE(String fileName) {
        String pathToFile = AUDIO_PATH + SE_PATH + fileName;

        se = Gdx.audio.newSound(FileProvider.getAssetsFile(pathToFile));

        return se.play(seVolume);
    }

    /**
     * Mute music.
     */
    public static void muteBGM() {
        musicVolume = 0.0f;

        music.setVolume(musicVolume);
    }

    /**
     * Mute sound.
     */
    public static void muteSE() {
        seVolume = 0.0f;
    }

    /**
     * Filter audio file extensions(.wav, .mp3, .ogg).
     *
     * @return the {@link FilenameFilter} object
     */
    private static FilenameFilter filterAudioExtensions() {
        return new FilenameFilter() {
            private boolean isWAV(String name) {
                return name.toLowerCase().endsWith(".wav");
            }

            private boolean isMP3(String name) {
                return name.toLowerCase().endsWith(".mp3");
            }

            private boolean isOGG(String name) {
                return name.toLowerCase().endsWith(".ogg");
            }

            @Override
            public boolean accept(File dir, String name) {
                return (isWAV(name) || isMP3(name) || isOGG(name));
            }
        };
    }

    /**
     * Stop current music without disposing.
     */
    private static void stopMusic() {
        if (music != null) {
            music.stop();
        }
    }

    /**
     * Get a random integer by given range.
     *
     * @param start minimum number
     * @param end   maximum number
     * @return the random integer
     */
    private static int getRandomInt(int start, int end) {
        Random random = new Random();
        return random.nextInt(end - start + 1) + start;
    }
}
