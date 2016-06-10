package com.toyknight.aeii.network.entity;

import com.toyknight.aeii.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author by toyknight 6/10/2016.
 */
public class MapSnapshot implements Serializable {

    private final int capacity;
    private final String filename;
    private final String author;

    public MapSnapshot(int capacity, String filename, String author) {
        this.capacity = capacity;
        this.filename = filename;
        this.author = author;
    }

    public MapSnapshot(JSONObject json) throws JSONException {
        this(json.getInt("capacity"), json.getString("filename"), json.getString("author"));
    }

    public int getCapacity() {
        return capacity;
    }

    public String getFilename() {
        return filename;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return String.format("(%d) %s [%s]", capacity, filename, author);
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("capacity", capacity);
        json.put("filename", filename);
        json.put("author", author);
        return json;
    }
}
