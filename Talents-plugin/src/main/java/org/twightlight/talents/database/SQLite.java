package org.twightlight.talents.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

public class SQLite {
    private Connection connection;
    private final Plugin plugin;
    private static final Gson GSON = new Gson();

    public SQLite(Plugin plugin) {
        this.plugin = plugin;
        connect();
    }

    public void connect() {
        Bukkit.getLogger().info("Connecting to your database...");
        this.connection = getConnection();
        Bukkit.getLogger().info("Connected successfully to your database!");
        Bukkit.getLogger().info("Creating tables...");

        try (Statement statement = this.getConnection().createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS talents (" +
                            "player TEXT PRIMARY KEY, " +
                            "talentsData TEXT DEFAULT '{}', " +
                            "soulstones INTEGER DEFAULT 0" +
                            ");"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS skills (" +
                            "player TEXT PRIMARY KEY, " +
                            "skills TEXT DEFAULT '{}', " +
                            "magicalspirits INTEGER DEFAULT 0, " +
                            "selecting TEXT DEFAULT ''" +
                            ");"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS runes (" +
                            "player TEXT PRIMARY KEY, " +
                            "storage TEXT DEFAULT '{}', " +
                            "n_selecting TEXT DEFAULT '{}', " +
                            "s_selecting TEXT DEFAULT '{}'" +
                            ");"
            );

            Bukkit.getLogger().info("Tables created successfully!");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Connection getConnection() {
        File dataFolder = this.plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File dataFile = new File(dataFolder, "talents.db");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Could not create database file", e);
            }
        }

        try {
            if (this.connection != null && !this.connection.isClosed()) {
                return this.connection;
            }
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dataFile.getPath());
            return this.connection;
        } catch (ClassNotFoundException | SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean createPlayerData(OfflinePlayer p) {
        String sqlCheck = "SELECT soulstones FROM talents WHERE player = ?";
        String sqlInsert = "INSERT INTO talents (player, talentsData, soulstones) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {

            psCheck.setString(1, p.getUniqueId().toString());
            try (ResultSet rs = psCheck.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                        psInsert.setString(1, p.getUniqueId().toString());
                        psInsert.setString(2, "{}");
                        psInsert.setInt(3, 0);
                        psInsert.executeUpdate();
                        return true;
                    }
                } else {
                    return false;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create player data for " + p.getName(), ex);
        }
    }

    public boolean createPlayerSkillsData(OfflinePlayer p) {
        String sqlCheck = "SELECT selecting FROM skills WHERE player = ?";
        String sqlInsert = "INSERT INTO skills (player, skills, magicalspirits, selecting) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {

            psCheck.setString(1, p.getUniqueId().toString());
            try (ResultSet rs = psCheck.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                        psInsert.setString(1, p.getUniqueId().toString());
                        psInsert.setString(2, "{}");
                        psInsert.setInt(3, 0);
                        psInsert.setString(4, "");
                        psInsert.executeUpdate();
                        return true;
                    }
                } else {
                    return false;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create player skills data for " + p.getName(), ex);
        }
    }

    public boolean createPlayerRuneData(OfflinePlayer p) {
        String sqlCheck = "SELECT storage FROM runes WHERE player = ?";
        String sqlInsert = "INSERT INTO runes (player, storage, n_selecting, s_selecting) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {

            psCheck.setString(1, p.getUniqueId().toString());
            try (ResultSet rs = psCheck.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                        psInsert.setString(1, p.getUniqueId().toString());
                        psInsert.setString(2, "{}");
                        psInsert.setString(3, "{}");
                        psInsert.setString(4, "{}");
                        psInsert.executeUpdate();
                        return true;
                    }
                } else {
                    return false;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create player runes data for " + p.getName(), ex);
        }
    }

    /**
     * Generic getter for values from the database.
     *
     * @param player    offline player
     * @param table     table name
     * @param column    column name
     * @param typeToken Guava TypeToken describing desired type
     * @param fallback  fallback value when missing/null
     * @param <T>       generic type
     * @return value cast to T or fallback
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(OfflinePlayer player, String table, String column, TypeToken<T> typeToken, T fallback) {
        String sql = "SELECT " + column + " FROM " + table + " WHERE player = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, player.getUniqueId().toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return fallback;
                }

                // Handle primitive-like values quickly
                Class<?> raw = typeToken.getRawType();
                if (raw == Integer.class || raw == int.class) {
                    return (T) Integer.valueOf(rs.getInt(column));
                }
                if (raw == String.class) {
                    String s = rs.getString(column);
                    return s == null ? fallback : (T) s;
                }

                String dataString = rs.getString(column);
                if (dataString == null) {
                    return fallback;
                }

                try {
                    Type type = typeToken.getType();
                    return (T) GSON.fromJson(dataString, type);
                } catch (JsonSyntaxException ex) {
                    Bukkit.getLogger().warning("Invalid JSON in database for " + column + ": " + dataString);
                    return fallback;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> T getData(OfflinePlayer player, String column, TypeToken<T> typeToken, T fallback) {
        return getData(player, "talents", column, typeToken, fallback);
    }


    public List<String> getActivatingSkills(Player p) {
        String selecting = this.getData(p, "skills", "selecting", new TypeToken<String>() {}, "");
        String[] parts = selecting.split(";", -1);
        String first = parts.length > 0 ? parts[0] : "";
        String second = parts.length > 1 ? parts[1] : "";
        String third = parts.length > 2 ? parts[2] : "";
        String fourth = parts.length > 3 ? parts[3] : "";

        return new ArrayList<>(Arrays.asList(first, second, third, fourth));
    }

    public boolean setSoulStones(Player p, int i) {
        return update(p, i, "soulstones");
    }

    public int getSoulStones(Player p) {
        Integer i = this.getData(p, "soulstones", new TypeToken<Integer>() {}, 0);
        return i == null ? 0 : i;
    }

    public boolean setMagicalSpirits(Player p, int i) {
        return update(p, i, "skills", "soulstones");
    }

    public int getMagicalSpirits(Player p) {
        Integer i = this.getData(p, "skills", "magicalspirots", new TypeToken<Integer>() {}, 0);
        return i == null ? 0 : i;
    }

    public Map<String, Integer> getTalentsMap(Player p) {
        return this.getData(p, "talentsData", new TypeToken<Map<String, Integer>>() {}, new HashMap<>());
    }

    public Map<String, Integer> getSkillsMap(Player p) {
        return this.getData(p, "skills", "skills", new TypeToken<Map<String, Integer>>() {}, new HashMap<>());
    }

    public Map<String, Integer> getRunesStorage(Player p) {
        return this.getData(p, "runes", "storage", new TypeToken<Map<String, Integer>>() {}, new HashMap<>());
    }

    public Map<String, List<String>> getSelectingRunes(Player p, String prefix) {
        return this.getData(p, "runes", prefix + "_selecting", new TypeToken<Map<String, List<String>>>() {}, new HashMap<>());
    }


    public <T> boolean update(OfflinePlayer player, T data, String table, String column) {
        String sql = "UPDATE " + table + " SET " + column + " = ? WHERE player = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (data instanceof Integer || data instanceof Double || data instanceof Boolean || data instanceof String) {
                ps.setObject(1, data);
            } else {
                ps.setString(1, GSON.toJson(data));
            }
            ps.setString(2, player.getUniqueId().toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } catch (NullPointerException | IllegalArgumentException ex) {
            throw ex;
        }
    }

    public <T> boolean update(OfflinePlayer player, T data, String column) {
        return update(player, data, "talents", column);
    }

}