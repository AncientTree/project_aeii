package com.toyknight.aeii.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.toyknight.aeii.AEIIException;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.record.GameRecord;
import com.toyknight.aeii.entity.GameSave;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author toyknight 9/17/2015.
 */
public class GameToolkit {

    private static final String TAG = "GAME FACTORY";

    public static final int SAVE = 0x1;
    public static final int RECORD = 0x2;

    private static final DateFormat date_format = new SimpleDateFormat("MMddyyyy HH-mm", Locale.getDefault());

    private GameToolkit() {
    }

    public static void save(GameCore game) throws AEIIException {
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

    public static void saveSkirmish(GameCore game, String filename) throws AEIIException {
        try {
            GameSave game_save = new GameSave(new GameCore(game), game.getType());
            FileHandle save_file = FileProvider.getSaveFile("skirmish " + filename);
            Output output = new Output(save_file.write(false));
            output.writeInt(SAVE);
            output.writeString(game_save.toJson().toString());
            output.flush();
            output.close();
        } catch (KryoException ex) {
            throw new AEIIException("Cannot save the game", ex);
        }
    }

    public static GameSave loadGame(FileHandle save_file) {
        try {
            Input input = new Input(save_file.read());
            int type = input.readInt();
            if (type == SAVE) {
                JSONObject json = new JSONObject(input.readString());
                GameSave save = new GameSave(json);
                input.close();
                return save;
            } else {
                return null;
            }
        } catch (JSONException ex) {
            Gdx.app.log(TAG, ex.toString());
            return null;
        }
    }

    public static GameRecord loadRecord(FileHandle record_file) {
        try {
            Input input = new Input(record_file.read());
            int type = input.readInt();
            if (type == RECORD) {
                return new GameRecord(new JSONObject(input.readString()));
            } else {
                return null;
            }
        } catch (JSONException ex) {
            Gdx.app.log(TAG, ex.toString());
            return null;
        }
    }

    public static int getType(FileHandle save_file) {
        try {
            Input input = new Input(save_file.read());
            int type = input.readInt();
            input.close();
            return type;
        } catch (KryoException ex) {
            Gdx.app.log(TAG, ex.toString());
            return -1;
        }
    }

    public static String createFilename(int mode) {
        Date date = new Date(System.currentTimeMillis());
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
