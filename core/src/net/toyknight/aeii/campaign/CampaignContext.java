package net.toyknight.aeii.campaign;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.campaign.challenge.ChallengeCampaign;
import net.toyknight.aeii.campaign.tutorial.TutorialCampaign;
import net.toyknight.aeii.entity.GameCore;
import net.toyknight.aeii.entity.Position;
import net.toyknight.aeii.entity.Tile;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.manager.GameEvent;
import net.toyknight.aeii.manager.GameManager;
import net.toyknight.aeii.utils.Language;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author toyknight 6/24/2016.
 */
public class CampaignContext {

    private final GameContext context;

    private CampaignController current_campaign;

    private ObjectMap<String, CampaignController> campaigns;

    public CampaignContext(GameContext context) {
        this.context = context;
        this.campaigns = new ObjectMap<String, CampaignController>();

        CampaignController campaign_tutorial = new TutorialCampaign();
        campaign_tutorial.initialize();
        campaigns.put(campaign_tutorial.getCode(), campaign_tutorial);

        CampaignController campaign_challenge = new ChallengeCampaign();
        campaign_challenge.initialize();
        campaigns.put(campaign_challenge.getCode(), campaign_challenge);
    }

    public GameContext getContext() {
        return context;
    }

    public GameManager getManager() {
        return getContext().getGameManager();
    }

    public ObjectMap.Keys<String> getCampaignCodes() {
        return campaigns.keys();
    }

    public CampaignController getCampaign(String campaign_code) {
        return campaigns.get(campaign_code);
    }

    public void setCurrentCampaign(String campaign_code) {
        if (campaigns.containsKey(campaign_code)) {
            current_campaign = getCampaign(campaign_code);
        }
    }

    public CampaignController getCurrentCampaign() {
        return current_campaign;
    }

    public void setCurrentStage(int stage) {
        if (getCurrentCampaign().setCurrentStage(stage)) {
            getCurrentCampaign().getCurrentStage().setCleared(false);
            getCurrentCampaign().getCurrentStage().setContext(new StageContext());
        }
    }

    public void onGameStart() {
        if (getContext().getGame().getType() == GameCore.CAMPAIGN) {
            getCurrentCampaign().getCurrentStage().onGameStart();
        }
    }

    public void onUnitMoved(Unit unit, int x, int y) {
        if (getContext().getGame().getType() == GameCore.CAMPAIGN) {
            getCurrentCampaign().getCurrentStage().onUnitMoved(new Unit(unit), x, y);
        }
    }

    public void onUnitAttacked(Unit attacker, Unit defender) {
        if (getContext().getGame().getType() == GameCore.CAMPAIGN) {
            getCurrentCampaign().getCurrentStage().onUnitAttacked(attacker, defender);
        }
    }

    public void onUnitDestroyed(Unit unit) {
        if (getContext().getGame().getType() == GameCore.CAMPAIGN) {
            getCurrentCampaign().getCurrentStage().onUnitDestroyed(unit);
        }
    }

    public void onTileRepaired(int x, int y) {
        if (getContext().getGame().getType() == GameCore.CAMPAIGN) {
            getCurrentCampaign().getCurrentStage().onTileRepaired(x, y);
        }
    }

    public void onTileOccupied(int x, int y, int team) {
        if (getContext().getGame().getType() == GameCore.CAMPAIGN) {
            getCurrentCampaign().getCurrentStage().onTileOccupied(x, y, team);
        }
    }

    public void onTurnStart(int turn) {
        if (getContext().getGame().getType() == GameCore.CAMPAIGN) {
            getCurrentCampaign().getCurrentStage().onTurnStart(turn);
        }
    }

    public void onTurnEnd(int turn) {
        if (getContext().getGame().getType() == GameCore.CAMPAIGN) {
            getCurrentCampaign().getCurrentStage().onTurnEnd(turn);
        }
    }

    public class StageContext {

        public void message(Message... messages) {
            JSONArray message_list = new JSONArray();
            for (Message message : messages) {
                JSONObject message_json = new JSONObject();
                message_json.put("portrait", message.getPortrait());
                message_json.put("message", message.getMessage());
                message_list.put(message_json);
            }
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(GameEvent.CAMPAIGN_MESSAGE, message_list);
        }

        public void reinforce(int team, Array<Integer> indexes, Array<Position> positions) {
            JSONArray index_array = new JSONArray();
            for (Integer index : indexes) {
                index_array.put(index);
            }
            JSONArray position_array = new JSONArray();
            for (Position position : positions) {
                JSONObject json = new JSONObject();
                json.put("x", position.x);
                json.put("y", position.y);
                position_array.put(json);
            }
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(
                    GameEvent.REINFORCE, team, index_array, position_array);
        }

        public int count(int team) {
            return getContext().getGame().getMap().getUnits(team).size;
        }

        public void clear() {
            getCurrentCampaign().getCurrentStage().setCleared(true);
            getContext().getGame().setGameOver(true);
            getContext().getGameManager().getAnimationDispatcher().submitMessageAnimation(Language.getText("LB_STAGE_CLEAR"), 1.0f);
        }

        public void fail() {
            getContext().getGame().setGameOver(true);
            getContext().getGameManager().getAnimationDispatcher().submitMessageAnimation(Language.getText("LB_STAGE_FAIL"), 1.0f);
        }

        public Position position(int x, int y) {
            return getContext().getGame().getMap().getPosition(x, y);
        }

        public int get(String name) {
            return getCurrentCampaign().getAttribute(name);
        }

        public void set(String name, int value) {
            getCurrentCampaign().setAttribute(name, value);
        }

        public void level(int unit_x, int unit_y, int experience) {
            Unit unit = getManager().getGame().getMap().getUnit(unit_x, unit_y);
            if (unit != null) {
                unit.gainExperience(experience);
            }
        }

        public Tile tile(int map_x, int map_y) {
            return getContext().getGame().getMap().getTile(map_x, map_y);
        }

    }

}
