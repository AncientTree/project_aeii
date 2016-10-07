package net.toyknight.aeii.server.managers;

import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.network.entity.LeaderboardRecord;
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

    public boolean changeMapAuthor(int map_id, String author) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("UPDATE maps SET author = ? WHERE map_id = ?");
        statement.setString(1, author);
        statement.setInt(2, map_id);
        return statement.executeUpdate() > 0;
    }

    public boolean changeMapFilename(int map_id, String filename) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("UPDATE maps SET filename = ? WHERE map_id = ?");
        statement.setString(1, filename);
        statement.setInt(2, map_id);
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

    public ObjectSet<String> getAuthors(String prefix, boolean symmetric) throws SQLException {
        String prefix_modifier = prefix == null ? "" : " WHERE author LIKE ?";
        String sql = (symmetric ?
                "SELECT DISTINCT author FROM (SELECT * FROM maps WHERE symmetric = 1) AS symmetric_maps" :
                "SELECT DISTINCT author FROM maps")
                + prefix_modifier;
        PreparedStatement statement = getConnection().prepareStatement(sql);
        if (prefix != null) {
            statement.setString(1, prefix + "%");
        }
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

    public void submitRecord(
            String username, String address, String campaign_code, int stage_number, int turns, int actions)
            throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(
                "INSERT INTO leaderboard (username, address, campaign_code, stage_number, turns, actions, timestamp) VALUES (?, ?, ?, ?, ?, ?, now())");
        statement.setString(1, username);
        statement.setString(2, address);
        statement.setString(3, campaign_code);
        statement.setInt(4, stage_number);
        statement.setInt(5, turns);
        statement.setInt(6, actions);
        statement.executeUpdate();
    }

    public LeaderboardRecord getBestRecord(String campaign_code, int stage_number) throws SQLException {
        PreparedStatement turns_statement = getConnection().prepareStatement(
                "SELECT * FROM leaderboard WHERE campaign_code = ? AND stage_number = ? ORDER BY turns ASC, timestamp ASC LIMIT 1");
        turns_statement.setString(1, campaign_code);
        turns_statement.setInt(2, stage_number);
        ResultSet turns_result = turns_statement.executeQuery();
        String best_turns_username = "none";
        int best_turns = -1;
        if (turns_result.first() && turns_result.getInt("turns") > 0) {
            best_turns_username = turns_result.getString("username");
            best_turns = turns_result.getInt("turns");
        }
        turns_result.close();
        PreparedStatement actions_statement = getConnection().prepareStatement(
                "SELECT * FROM leaderboard WHERE campaign_code = ? AND stage_number = ? ORDER BY actions ASC, timestamp ASC LIMIT 1");
        actions_statement.setString(1, campaign_code);
        actions_statement.setInt(2, stage_number);
        ResultSet actions_result = actions_statement.executeQuery();
        String best_actions_username = "none";
        int best_actions = -1;
        if (actions_result.first() && actions_result.getInt("actions") > 0) {
            best_actions_username = actions_result.getString("username");
            best_actions = actions_result.getInt("actions");
        }
        return new LeaderboardRecord(best_turns, best_turns_username, best_actions, best_actions_username);
    }

}
