package net.toyknight.aeii;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight on 2/25/2016.
 */
public interface Serializable {

    JSONObject toJson() throws JSONException;

}
