package net.toyknight.aeii.entity;

import net.toyknight.aeii.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 4/3/2015.
 */
public class Tomb extends Position implements Serializable {

    private int remains = 1;

    public Tomb(JSONObject json) throws JSONException {
        this(json.getInt("x"), json.getInt("y"));
        setRemains(json.getInt("remains"));
    }

    public Tomb(Tomb tomb) {
        this(tomb.x, tomb.y);
    }

    public Tomb(int x, int y) {
        super(x, y);
    }

    public void update() {
        if (remains >= 0) {
            remains--;
        }
    }

    public int getRemains() {
        return remains;
    }

    public void setRemains(int remains) {
        this.remains = remains;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("x", x);
        json.put("y", y);
        json.put("remains", getRemains());
        return json;
    }

}
