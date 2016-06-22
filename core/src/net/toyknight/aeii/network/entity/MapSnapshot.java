package net.toyknight.aeii.network.entity;

import net.toyknight.aeii.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author by toyknight 6/10/2016.
 */
public class MapSnapshot implements Serializable {

    private final int capacity;
    private final String filename;
    private final String author;

    private boolean directory = false;

    public MapSnapshot(int capacity, String filename, String author) {
        this.capacity = capacity;
        this.filename = filename;
        this.author = author;
    }

    public MapSnapshot(JSONObject json) throws JSONException {
        this(json.getInt("capacity"), json.getString("filename"), json.getString("author"));
        setDirectory(json.getBoolean("directory"));
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public boolean isDirectory() {
        return directory;
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
        if (isDirectory()) {
            return "/" + getAuthor();
        } else {
            return String.format("(%d) %s [%s]", getCapacity(), getFilename(), getAuthor());
        }
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("capacity", capacity);
        json.put("filename", filename);
        json.put("author", author);
        json.put("directory", directory);
        return json;
    }
}
