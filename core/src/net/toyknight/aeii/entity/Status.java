package net.toyknight.aeii.entity;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 4/3/2015.
 */
public class Status implements Serializable {

    public static final int POISONED = 0;
    public static final int SLOWED = 1;
    public static final int INSPIRED = 2;
    public static final int BLINDED = 3;

    private final int type;
    private int remaining_turn;

    public Status(JSONObject json) throws JSONException {
        this.type = json.getInt("type");
        this.remaining_turn = json.getInt("remaining_turn");
    }

    public Status(Status status) {
        this(status.getType(), status.getRemainingTurn());
    }

    public Status(int type) {
        this(type, 0);
    }

    public Status(int type, int turn) {
        this.type = type;
        setRemainingTurn(turn);
    }

    public void update() {
        this.remaining_turn--;
    }

    public int getType() {
        return type;
    }

    public int getRemainingTurn() {
        return remaining_turn;
    }

    public void setRemainingTurn(int turn) {
        this.remaining_turn = turn;
    }

    @Override
    public boolean equals(Object status) {
        return status instanceof Status && ((Status) status).getType() == this.type;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.type;
        return hash;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", getType());
        json.put("remaining_turn", getRemainingTurn());
        return json;
    }

    public static boolean isBuff(Status status) {
        return status != null && status.getType() == INSPIRED;
    }

    public static boolean isDebuff(Status status) {
        if (status == null) {
            return false;
        } else {
            int type = status.getType();
            return type == POISONED || type == SLOWED || type == BLINDED;
        }
    }

    public static Array<Integer> getAllStatus() {
        Array<Integer> status = new Array<Integer>();
        for (int i = 0; i < 4; i++) {
            status.add(i);
        }
        return status;
    }

}
