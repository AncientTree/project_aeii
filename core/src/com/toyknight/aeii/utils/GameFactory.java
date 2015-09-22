package com.toyknight.aeii.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.GameRecord;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.serializable.GameSave;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;

/**
 * @author toyknight 9/17/2015.
 */
public class GameFactory {

    private static final String TAG = "GAME FACTORY";

    public static final int SAVE = 0x1;
    public static final int RECORD = 0x2;

    private GameFactory() {
    }

    public static void save(GameCore game) throws IOException {
        String filename = createFilename(SAVE);
        switch (game.getType()) {
            case GameCore.SKIRMISH:
                saveSkirmish(game, filename);
                break;
            case GameCore.CAMPAIGN:
                break;
            default:
                //do nothing
        }
    }

    public static void saveSkirmish(GameCore game, String filename) throws IOException {
        GameSave game_save = new GameSave(game, game.getType());
        FileHandle save_file = FileProvider.getSaveFile("skirmish " + filename);
        ObjectOutputStream oos = new ObjectOutputStream(save_file.write(false));
        oos.writeInt(SAVE);
        oos.writeObject(game_save);
        oos.flush();
        oos.close();
    }

    public static GameSave loadGame(FileHandle save_file) {
        try {
            ObjectInputStream ois = new ObjectInputStream(save_file.read());
            int type = ois.readInt();
            if (type == SAVE) {
                GameSave save = (GameSave) ois.readObject();
                ois.close();
                return save;
            } else {
                return null;
            }
        } catch (IOException ex) {
            Gdx.app.log(TAG, ex.toString());
            return null;
        } catch (ClassNotFoundException ex) {
            Gdx.app.log(TAG, ex.toString());
            return null;
        }
    }

    public static GameRecord loadRecord(FileHandle record_file) {
        try {
            ObjectInputStream ois = new ObjectInputStream(record_file.read());
            int type = ois.readInt();
            if (type == RECORD) {
                GameCore game = (GameCore) ois.readObject();
                Queue<GameEvent> events = (Queue) ois.readObject();
                return new GameRecord(game, events);
            } else {
                return null;
            }
        } catch (IOException ex) {
            Gdx.app.log(TAG, ex.toString());
            return null;
        } catch (ClassNotFoundException ex) {
            Gdx.app.log(TAG, ex.toString());
            return null;
        }
    }

    public static int getType(FileHandle save_file) {
        try {
            ObjectInputStream ois = new ObjectInputStream(save_file.read());
            int type = ois.readInt();
            ois.close();
            return type;
        } catch (IOException ex) {
            Gdx.app.log(TAG, ex.toString());
            return -1;
        }
    }

    public static String createFilename(int mode) {
        Date date = new Date(System.currentTimeMillis());
        DateFormat date_format = new SimpleDateFormat("MMddyyyy HH-mm");
        switch (mode) {
            case SAVE:
                return date_format.format(date) + ".sav";
            case RECORD:
                return date_format.format(date) + ".rec";
            default:
                return date_format.format(date);
        }
    }

}
