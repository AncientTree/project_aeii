package net.toyknight.aeii.server.utils;

import org.json.JSONObject;

/**
 * @author toyknight 8/17/2016.
 */
public class PacketBuilder {

    public static JSONObject create(int type) {
        JSONObject packet = new JSONObject();
        packet.put("type", type);
        return packet;
    }

    public static JSONObject create(int type, int operation) {
        JSONObject packet = new JSONObject();
        packet.put("operation", operation);
        packet.put("type", type);
        return packet;
    }

}
