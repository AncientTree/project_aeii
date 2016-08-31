package net.toyknight.aeii.server.managers;

import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.network.entity.MapSnapshot;

import java.sql.*;

/**
 * @author toyknight 8/30/2016.
 */
public class DatabaseManager {

    private Connection connection;

    public void connect(String host, String name, String username, String password)
            throws SQLException, ClassNotFoundException {
        connection = DriverManager.getConnection(String.format(
                "jdbc:mysql://%s/%s?user=%s&password=%s&useUnicode=true&characterEncoding=UTF-8",
                host, name, username, password));
    }

    private Connection getConnection() {
        return connection;
    }

    public int addMap(int capacity, String filename, String author, boolean symmetric) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(
                "INSERT INTO maps (capacity, filename, author, symmetric) VALUES (?, ?, ?, ?)");
        statement.setInt(1, capacity);
        statement.setString(2, filename);
        statement.setString(3, author);
        statement.setInt(4, symmetric ? 1 : 0);
        statement.executeUpdate();

        statement = getConnection().prepareStatement("SELECT map_id FROM maps WHERE filename = ? AND author = ?");
        statement.setString(1, filename);
        statement.setString(2, author);
        ResultSet result = statement.executeQuery();
        return result.next() ? result.getInt("map_id") : -1;
    }

    public boolean removeMap(int map_id) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("DELETE FROM maps WHERE map_id = ?");
        statement.setInt(1, map_id);
        return statement.executeUpdate() > 0;
    }

    public ObjectSet<MapSnapshot> getMapSnapshots(String author, boolean symmetric) throws SQLException {
        String sql = symmetric ?
                "SELECT * FROM maps WHERE author = ? AND symmetric = 1" :
                "SELECT * FROM maps WHERE author = ?";
        PreparedStatement statement = getConnection().prepareStatement(sql);
        statement.setString(1, author);
        ResultSet result = statement.executeQuery();
        ObjectSet<MapSnapshot> snapshots = new ObjectSet<MapSnapshot>();
        while (result.next()) {
            snapshots.add(new MapSnapshot(
                    result.getInt("map_id"),
                    result.getInt("capacity"),
                    result.getString("filename"),
                    result.getString("author")));
        }
        return snapshots;
    }

    public ObjectSet<String> getAuthors(boolean symmetric) throws SQLException {
        String sql = symmetric ?
                "SELECT DISTINCT author FROM (SELECT * FROM maps WHERE symmetric = 1) as symmetric_maps" :
                "SELECT DISTINCT author FROM maps";
        PreparedStatement statement = getConnection().prepareStatement(sql);
        ResultSet result = statement.executeQuery();
        ObjectSet<String> authors = new ObjectSet<String>();
        while (result.next()) {
            authors.add(result.getString("author"));
        }
        return authors;
    }

    public boolean isMapExisting(String filename, String author) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(
                "SELECT count(map_id) FROM maps WHERE filename = ? AND author = ?");
        statement.setString(1, filename);
        statement.setString(2, author);
        ResultSet result = statement.executeQuery();
        return result.next() && result.getInt(1) == 1;
    }

}
