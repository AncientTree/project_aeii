package net.toyknight.aeii.campaign;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.campaign.aei.AEICampaign;
import net.toyknight.aeii.campaign.aeii.AEIICampaign;
import net.toyknight.aeii.campaign.challenge.ChallengeCampaign;
import net.toyknight.aeii.campaign.tutorial.TutorialCampaign;
import net.toyknight.aeii.entity.*;
import net.toyknight.aeii.manager.GameEvent;
import net.toyknight.aeii.manager.GameManager;
import net.toyknight.aeii.utils.UnitToolkit;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

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

        CampaignController campaign_aeii = new AEIICampaign();
        campaign_aeii.initialize();
        campaigns.put(campaign_aeii.getCode(), campaign_aeii);

        CampaignController campaign_aei = new AEICampaign();
        campaign_aei.initialize();
        campaigns.put(campaign_aei.getCode(), campaign_aei);
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

    public void loadCampaign(GameSave save) {
        String campaign_code = save.getString("_code", "");
        int stage_number = save.getInteger("_stage", 0);
        setCurrentCampaign(campaign_code);
        setCurrentStage(stage_number);

        ObjectMap<String, Integer> attributes = new ObjectMap<String, Integer>();
        Iterator<String> keys = save.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!key.startsWith("_")) {
                attributes.put(key, save.getInteger(key, 0));
            }
        }
        getCurrentCampaign().setAttributes(attributes);
    }

    public void onGameStart() {
        if (getContext().getGame().getType() == GameCore.CAMPAIGN) {
            getCurrentCampaign().getCurrentStage().onGameStart();
        }
    }

    public void onUnitMoved(Unit unit, int x, int y) {
        if (getContext().getGame().getType() == GameCore.CAMPAIGN) {
            getCurrentCampaign().getCurrentStage().onUnitMoved(unit, x, y);
        }
    }

    public void onUnitStandby(int x, int y) {
        if (getContext().getGame().getType() == GameCore.CAMPAIGN) {
            Unit unit = getContext().getGame().getMap().getUnit(x, y);
            if (unit != null) {
                getCurrentCampaign().getCurrentStage().onUnitStandby(unit);
            }
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

        public void reinforce(int team, Reinforcement... reinforcements) {
            reinforce(team, -1, -1, reinforcements);
        }

        public void reinforce(int team, int from_x, int from_y, Reinforcement... reinforcements) {
            JSONArray json_reinforcements = new JSONArray();
            for (Reinforcement reinforcement : reinforcements) {
                JSONObject json_reinforcement = new JSONObject();
                json_reinforcement.put("index", reinforcement.getIndex());
                json_reinforcement.put("head", reinforcement.getHead());
                json_reinforcement.put("x", reinforcement.getMapX());
                json_reinforcement.put("y", reinforcement.getMapY());
                json_reinforcements.put(json_reinforcement);
            }
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(
                    GameEvent.CAMPAIGN_REINFORCE, team, from_x, from_y, json_reinforcements);
        }

        public int count_unit(int team) {
            return getContext().getGame().getMap().getUnits(team).size;
        }

        public int count_castle(int team) {
            return getContext().getGame().getMap().getCastlePositions(team).size;
        }

        public int count_village(int team) {
            return getContext().getGame().getMap().getVillagePositions(team).size;
        }

        public void clear() {
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(GameEvent.CAMPAIGN_CLEAR);
        }

        public void fail() {
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(GameEvent.CAMPAIGN_FAIL);
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

        public void gold(int team, int gold) {
            getContext().getGame().getPlayer(team).setGold(gold);
        }

        public void alliance(int team, int alliance) {
            getContext().getGame().getPlayer(team).setAlliance(alliance);
        }

        public void destroy_team(int team) {
            getContext().getGame().destroyTeam(team);
        }

        public void restore(int team) {
            getContext().getGame().getPlayer(team).setType(Player.ROBOT);
            getContext().getGame().setTeamDestroyed(team, false);
        }

        public void attack(int x, int y, int damage) {
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(GameEvent.CAMPAIGN_ATTACK, x, y, damage);
        }

        public void destroy_unit(int x, int y) {
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(GameEvent.UNIT_DESTROY, x, y, -1);
        }

        public void destroy_tile(int x, int y) {
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(GameEvent.TILE_DESTROY, x, y, -1);
        }

        public void destroy_tile(int x, int y, int destroyed_index) {
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(
                    GameEvent.CAMPAIGN_TILE_DESTROY, x, y, destroyed_index);
        }

        public void focus(int x, int y) {
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(GameEvent.CAMPAIGN_FOCUS, x, y);
        }

        public void head(int team, int index) {
            getContext().getGame().getCommander(team).setHead(index);
        }

        public void head(int x, int y, int index) {
            Unit unit = getContext().getGame().getMap().getUnit(x, y);
            if (unit != null) {
                unit.setHead(index);
            }
        }

        public void steal_crystal(int map_x, int map_y, int target_x, int target_y) {
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(
                    GameEvent.CAMPAIGN_CRYSTAL_STEAL, map_x, map_y, target_x, target_y);
        }

        public void create(int index, int team, int map_x, int map_y) {
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(
                    GameEvent.CAMPAIGN_CREATE_UNIT, index, team, map_x, map_y);
        }

        public void move(int unit_x, int unit_y, int target_x, int target_y) {
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(
                    GameEvent.CAMPAIGN_MOVE_UNIT, unit_x, unit_y, target_x, target_y);
        }

        public void remove_unit(int unit_x, int unit_y) {
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(
                    GameEvent.CAMPAIGN_REMOVE_UNIT, unit_x, unit_y);
        }

        public void remove_tomb(int map_x, int map_y) {
            if (getContext().getGame().getMap().isTomb(map_x, map_y)) {
                getContext().getGame().getMap().removeTomb(map_x, map_y);
            }
        }

        public void team(int unit_x, int unit_y, int team) {
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(
                    GameEvent.CAMPAIGN_CHANGE_TEAM, unit_x, unit_y, team);
        }

        public Unit crystal(int team) {
            for (Unit unit : getContext().getGame().getMap().getUnits(team)) {
                if (unit.isCrystal()) {
                    return unit;
                }
            }
            return null;
        }

        public void fly_over(int index, int team, int start_x, int start_y, int target_x, int target_y) {
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(
                    GameEvent.CAMPAIGN_FLY_OVER, index, team, start_x, start_y, target_x, target_y);
        }

        public void carry(int carrier_x, int carrier_y, int target_index, int target_team, int dest_x, int dest_y) {
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(
                    GameEvent.CAMPAIGN_CARRY_UNIT, carrier_x, carrier_y, target_index, target_team, dest_x, dest_y);
        }

        public void show_objectives() {
            getContext().getGameManager().getGameEventExecutor().submitGameEvent(GameEvent.CAMPAIGN_SHOW_OBJECTIVES);
        }

        public void havens_fury(int team, int target_x, int target_y, int damage) {
            Unit target;
            if ((target = getContext().getGame().getMap().getUnit(target_x, target_y)) == null) {
                ObjectSet<Unit> units = getContext().getGame().getMap().getUnits(team);
                int max_price = Integer.MIN_VALUE;
                int max_hp = Integer.MIN_VALUE;
                for (Unit unit : units) {
                    if (!unit.isCommander()) {
                        if (unit.getCurrentHp() > max_hp
                                || (unit.getCurrentHp() == max_hp && unit.getPrice() > max_price)) {
                            target = unit;
                            max_price = unit.getPrice();
                            max_hp = unit.getCurrentHp();
                        }
                    }
                }
                if (target == null && units.size > 0) {
                    target = units.first();
                }
                if (target != null) {
                    getContext().getGameManager().getGameEventExecutor().submitGameEvent(
                            GameEvent.CAMPAIGN_HAVENS_FURY, target.getX(), target.getY(), damage);
                }
            } else {
                getContext().getGameManager().getGameEventExecutor().submitGameEvent(
                        GameEvent.CAMPAIGN_HAVENS_FURY, target_x, target_y, damage);
            }
            if (target != null) {
                if (damage != 0) {
                    hp_change(target.getX(), target.getY(), -damage);
                }
                if (target.getCurrentHp() - damage <= 0) {
                    destroy_unit(target_x, target_y);
                }
            }
        }

        public void hp_change(int x, int y, int change) {
            Unit target = getContext().getGame().getMap().getUnit(x, y);
            if (target != null) {
                JSONArray hp_changes = new JSONArray();
                JSONObject hp_change = new JSONObject();
                change = UnitToolkit.validateHpChange(target, change);
                hp_change.put("x", x);
                hp_change.put("y", y);
                hp_change.put("change", change);
                hp_changes.put(hp_change);
                getContext().getGameManager().getGameEventExecutor().submitGameEvent(GameEvent.HP_CHANGE, hp_changes);
            }
        }

        public void code(int x, int y, String code) {
            getContext().getGame().getMap().getUnit(x, y).setUnitCode(code);
        }

        public void set_static(int x, int y, boolean is_static) {
            Unit unit = getContext().getGame().getMap().getUnit(x, y);
            if (unit != null) {
                unit.setStatic(is_static);
            }
        }

        public Unit get_unit(int map_x, int map_y) {
            return getContext().getGame().getMap().getUnit(map_x, map_y);
        }

        public int get_gold(int team) {
            return getContext().getGame().getPlayer(team).getGold();
        }

    }

}
