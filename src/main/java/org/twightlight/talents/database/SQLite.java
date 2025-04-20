package org.twightlight.talents.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.Utility;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SQLite {
    private Connection connection;
    private Plugin plugin;
    private static List<String> intersections_2 = Arrays.asList("IHE", "IMD", "MLS", "AP", "CD");


    public SQLite(Plugin plugin) {
        this.plugin = plugin;
        connect();
    }

    public void connect() {
        System.out.println("Connecting to your database...");
        this.connection = getConnection();
        System.out.println("Connected successfully to your database!");
        System.out.println("Creating tables...");
        try {
            Statement statement = getConnection().createStatement();
            statement.executeUpdate(" CREATE TABLE IF NOT EXISTS talents ( player TEXT PRIMARY KEY, melee TEXT DEFAULT '{}', ranged TEXT DEFAULT '{}', protective TEXT DEFAULT '{}', supportive TEXT DEFAULT '{}', miscellaneous TEXT DEFAULT '{}', special TEXT DEFAULT '{}', soulstones INTEGER DEFAULT 0); ");
            statement.close();
            System.out.println("Tables created successfully!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        File dataFile = new File(plugin.getDataFolder().getPath(), "talents.db");
        if (!dataFile.exists())
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        try {
            if (this.connection != null && !this.connection.isClosed())
                return this.connection;
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dataFile);
            return this.connection;
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean createPlayerData(OfflinePlayer p) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = getConnection();

            ps = connection.prepareStatement("SELECT 1 FROM talents WHERE player = ?");
            ps.setString(1, p.getUniqueId().toString());
            rs = ps.executeQuery();

            if (rs.next()) {
                return false;
            }

            ps = connection.prepareStatement(
                    "INSERT INTO talents (player, melee, ranged, protective, supportive, miscellaneous, special, soulstones) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, p.getUniqueId().toString());
            ps.setString(2, "{}");
            ps.setString(3, "{}");
            ps.setString(4, "{}");
            ps.setString(5, "{}");
            ps.setString(6, "{}");
            ps.setString(7, "{}");
            ps.setInt(8, 0);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create player data for " + p.getName(), e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public <T> T getData(OfflinePlayer player, String column, TypeToken<T> typeToken, T fallback) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT " + column + " FROM talents WHERE player = ?")) {

            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                if (typeToken.getRawType() == Integer.class || typeToken.getRawType() == int.class) {
                    int value = rs.getInt(column);
                    return (T) Integer.valueOf(value);
                }
                else {
                    String dataString = rs.getString(column);
                    if (dataString != null) {
                        try {
                            return new Gson().fromJson(dataString, typeToken.getType());
                        } catch (JsonSyntaxException e) {
                            Bukkit.getLogger().warning("Invalid JSON in database for " + column + ": " + dataString);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return fallback;
    }

    public int getSoulStones(Player p) {
        return getData(p, "soulstones", new TypeToken<Integer>() {}, 0);
    }

    public boolean setSoulStones(Player p, int i) {
        return pull(p, i, "soulstones");
    }

    public int getTalentLevel(Player p, TalentsCategory category, String id) {
        return getData(p, category.getColumn(), new TypeToken<Map<String, Integer>>() {}, null).get(id);
    }

    public boolean setLevelTalent(int amount, Talent<?> talent, String talentid, Player p) {
        int costListSize = talent.getCostList().size();
        Map<String, Integer> map = getData(p, talent.getCategory().getColumn(), new TypeToken<Map<String, Integer>>() {}, null);
        if (map.containsKey(talentid)) {
            if (amount <= costListSize && amount >= 0) {
                map.put(talentid, amount);
            } else {
                return false;
            }
        } else {
            return false;
        }
        pull(p, map, talent.getCategory().getColumn());
        return true;
    }

    public boolean upgradeTalents(int amount, Talent<?> talent, String talentid, Player p) {
        int costListSize = talent.getCostList().size();
        Map<String, Integer> map = getData(p, talent.getCategory().getColumn(), new TypeToken<Map<String, Integer>>() {}, null);
        if (map.containsKey(talentid)) {
            int c_level = map.get(talentid);
            if (c_level + amount <= costListSize && c_level + amount >= 0) {
                map.put(talentid, c_level + amount);
            } else {
                return false;
            }
        } else {
            return false;
        }
        pull(p, map, talent.getCategory().getColumn());
        return true;
    }

    public int resetTalents(Player p) {
        float totalRefund = 0;
        TalentsCategory[] cats = TalentsCategory.values();
        for (TalentsCategory cat : cats) {
            Map<String, Integer> map = getData(p, cat.getColumn(), new TypeToken<Map<String, Integer>>() {}, null);
            Set<String> talents = map.keySet();
            for (String talent : talents) {
                int level = map.get(talent);
                if (level > 0) {
                    Talent<?> talentInstance = Talents.getInstance().getTalentsManagerService().Talents.get(cat.getColumn()).get(talent);
                    float refund = Utility.totalCost(talentInstance.getCostList(), 0, level-1);
                    setLevelTalent(0, talentInstance, talent, p);
                    if (intersections_2.contains(talent)) {
                        refund /= 2;
                    }
                    totalRefund += refund;
                }
            }
        }

        return (int) totalRefund;
    }

    public <T> boolean pull(OfflinePlayer player, T data, String column) {
        try {
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("UPDATE talents SET " + column + "=? WHERE player=?");
            Gson gson = new Gson();
            if (data instanceof Integer || data instanceof Double || data instanceof Boolean) {
                ps.setObject(1, data);
            } else {
                ps.setString(1, gson.toJson(data));
            }
            ps.setString(2, player.getUniqueId().toString());
            ps.executeUpdate();
            ps.close();
            c.close();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            throw new NullPointerException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public boolean addColumnIfNotExists(String columnName, String columnType, String defaultValue) {

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            if (!columnExists(columnName)) {

                String statement = "ALTER TABLE hlootchest ADD COLUMN " + columnName + " " + columnType +
                        (defaultValue != null ? " DEFAULT " + defaultValue : "") + ";";
                stmt.executeUpdate(statement);
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            return false;
        }
    }

    private boolean columnExists(String columnName) throws SQLException {
        Connection conn = getConnection();
        String query = "PRAGMA table_info(hlootchest)";
        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
