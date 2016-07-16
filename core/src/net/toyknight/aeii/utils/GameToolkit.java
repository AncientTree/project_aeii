package net.toyknight.aeii.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.*;
import net.toyknight.aeii.record.GameRecord;
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

    private static final DateFormat date_format = new SimpleDateFormat("MMddyyyy-HHmmss", Locale.getDefault());

    private GameToolkit() {
    }

    public static GameCore createCampaignGame(StageController stage) throws AEIIException {
        FileHandle map_file = FileProvider.getAssetsFile("map/campaign/" + stage.getMapName());
        Map map = MapFactory.createMap(map_file);
        Rule rule = stage.getRule();

        GameCore game = new GameCore(map, rule, stage.getStartGold(), GameCore.CAMPAIGN);
        for (int team = 0; team < 4; team++) {
            if (game.getMap().hasTeamAccess(team)) {
                game.getPlayer(team).setAlliance(team);
                if (team == stage.getPlayerTeam()) {
                    game.getPlayer(team).setType(Player.LOCAL);
                } else {
                    game.getPlayer(team).setType(Player.ROBOT);
                }
            }
        }
        return game;
    }

    public static void saveSkirmish(GameCore game) throws AEIIException {
        try {
            FileHandle save_file = FileProvider.getSaveFile("skirmish " + createFilename(SAVE));
            GameSave game_save = new GameSave(new GameCore(game), game.getType());
            Output output = new Output(save_file.write(false));
            output.writeInt(SAVE);
            output.writeString(game_save.toJson().toString());
            output.flush();
            output.close();
        } catch (KryoException ex) {
            throw new AEIIException("Cannot save the game", ex);
        }
    }

    public static void saveCampaign(GameCore game, String code, int stage, ObjectMap<String, Integer> attributes)
            throws AEIIException {
        try {
            FileHandle save_file = FileProvider.getSaveFile("campaign " + createFilename(SAVE));
            GameSave game_save = new GameSave(new GameCore(game), game.getType());
            game_save.putString("_code", code);
            game_save.putInteger("_stage", stage);
            for (String key : attributes.keys()) {
                game_save.putInteger(key, attributes.get(key));
            }

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
