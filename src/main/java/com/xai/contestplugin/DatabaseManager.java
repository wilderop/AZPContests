package com.xai.contestplugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {

    private Connection connection;
    private final JavaPlugin plugin;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                boolean created = dataFolder.mkdir();
                if (!created) {
                    plugin.getLogger().severe("Failed to create plugin data folder!");
                    plugin.getLogger().severe("Check file permissions on: " + dataFolder.getAbsolutePath());
                    return;
                }
            }
            File dbFile = new File(dataFolder, "data.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());
            createTables();
            plugin.getLogger().info("Database initialized successfully");
        } catch (SQLException e) {
            plugin.getLogger().severe("DB init failed: " + e.getMessage());
            plugin.getLogger().severe("Check file permissions on plugin folder!");
            connection = null;
        }
    }
    
    public boolean isConnected() {
        return connection != null;
    }

    private void createTables() throws SQLException {
        String pastRuns = "CREATE TABLE IF NOT EXISTS past_runs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "contest_name TEXT," +
                "start_time TIMESTAMP," +
                "end_time TIMESTAMP," +
                "total LONG," +
                "old_total LONG," +
                "high_score LONG," +
                "winners TEXT" +
                ");";
        String scores = "CREATE TABLE IF NOT EXISTS scores (" +
                "run_id INTEGER," +
                "player_uuid TEXT," +
                "score LONG," +
                "FOREIGN KEY(run_id) REFERENCES past_runs(id)" +
                ");";
        String pendingPrizes = "CREATE TABLE IF NOT EXISTS pending_prizes (" +
                "player_uuid TEXT," +
                "item TEXT" +
                ");";
        String activeContest = "CREATE TABLE IF NOT EXISTS active_contest (" +
                "id INTEGER PRIMARY KEY CHECK (id = 1)," +
                "contest_name TEXT NOT NULL," +
                "start_time BIGINT NOT NULL," +
                "end_time BIGINT NOT NULL," +
                "old_total BIGINT NOT NULL," +
                "last_saved BIGINT NOT NULL" +
                ");";
        String activeParticipants = "CREATE TABLE IF NOT EXISTS active_participants (" +
                "player_uuid TEXT PRIMARY KEY," +
                "start_amount BIGINT NOT NULL," +
                "saved_score BIGINT NOT NULL" +
                ");";
        String pendingWinNotifications = "CREATE TABLE IF NOT EXISTS pending_win_notifications (" +
                "player_uuid TEXT PRIMARY KEY," +
                "contest_name TEXT NOT NULL," +
                "score BIGINT NOT NULL," +
                "was_tie BOOLEAN NOT NULL" +
                ");";
        String firstWinnerEvents = "CREATE TABLE IF NOT EXISTS first_winner_events (" +
                "contest_name TEXT NOT NULL," +
                "tier_name TEXT NOT NULL," +
                "player_uuid TEXT NOT NULL," +
                "player_name TEXT NOT NULL," +
                "timestamp BIGINT NOT NULL," +
                "PRIMARY KEY (contest_name, tier_name)" +
                ");";
        String pendingVoucherTrades = "CREATE TABLE IF NOT EXISTS pending_voucher_trades (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_uuid TEXT NOT NULL," +
                "player_name TEXT NOT NULL," +
                "contest_name TEXT NOT NULL," +
                "tier_name TEXT NOT NULL," +
                "timestamp BIGINT NOT NULL" +
                ");";
        String trackingData = "CREATE TABLE IF NOT EXISTS tracking_data (" +
                "contest_name TEXT NOT NULL," +
                "tracking_type TEXT NOT NULL," +
                "key_data TEXT NOT NULL," +
                "player_uuid TEXT NOT NULL," +
                "timestamp BIGINT NOT NULL," +
                "metadata TEXT," +
                "PRIMARY KEY (contest_name, tracking_type, key_data)" +
                ");";
        String contestWins = "CREATE TABLE IF NOT EXISTS contest_wins (" +
                "contest_name TEXT NOT NULL," +
                "player_uuid TEXT NOT NULL," +
                "wins INTEGER DEFAULT 0," +
                "PRIMARY KEY (contest_name, player_uuid)" +
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(pastRuns);
            stmt.execute(scores);
            stmt.execute(pendingPrizes);
            stmt.execute(activeContest);
            stmt.execute(activeParticipants);
            stmt.execute(pendingWinNotifications);
            stmt.execute(firstWinnerEvents);
            stmt.execute(pendingVoucherTrades);
            stmt.execute(trackingData);
            stmt.execute(contestWins);
        }
    }

    public int insertPastRun(String contestName, Timestamp start, Timestamp end, long total, long oldTotal, long highScore, String winners) {
        String sql = "INSERT INTO past_runs (contest_name, start_time, end_time, total, old_total, high_score, winners) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, contestName);
            pstmt.setTimestamp(2, start);
            pstmt.setTimestamp(3, end);
            pstmt.setLong(4, total);
            pstmt.setLong(5, oldTotal);
            pstmt.setLong(6, highScore);
            pstmt.setString(7, winners);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Insert past run failed: " + e.getMessage());
        }
        return -1;
    }

    public void insertScore(int runId, String uuid, long score) {
        String sql = "INSERT INTO scores (run_id, player_uuid, score) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, runId);
            pstmt.setString(2, uuid);
            pstmt.setLong(3, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Insert score failed: " + e.getMessage());
        }
    }

    public long getLastTotal(String contestName) {
        String sql = "SELECT total FROM past_runs WHERE contest_name = ? ORDER BY end_time DESC LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contestName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getLong("total");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Get last total failed: " + e.getMessage());
        }
        return 0;
    }

    public ResultSet getLeaderboards(String contestName, int limit) {
        String sql = "SELECT player_uuid, MAX(score) as max_score FROM scores " +
                "INNER JOIN past_runs ON scores.run_id = past_runs.id " +
                "WHERE past_runs.contest_name = ? GROUP BY player_uuid ORDER BY max_score DESC LIMIT ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, contestName);
            pstmt.setInt(2, limit);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            plugin.getLogger().warning("Get leaderboards failed: " + e.getMessage());
            return null;
        }
    }

    public ResultSet getHistory(String contestName, int limit) {
        String sql = "SELECT * FROM past_runs WHERE contest_name = ? ORDER BY end_time DESC LIMIT ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, contestName);
            pstmt.setInt(2, limit);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            plugin.getLogger().warning("Get history failed: " + e.getMessage());
            return null;
        }
    }

    public void addPendingPrize(String uuid, String itemSerialized) {
        String sql = "INSERT INTO pending_prizes (player_uuid, item) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, itemSerialized);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Add pending prize failed: " + e.getMessage());
        }
    }

    public ResultSet getPendingPrizes(String uuid) {
        String sql = "SELECT item FROM pending_prizes WHERE player_uuid = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, uuid);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            plugin.getLogger().warning("Get pending prizes failed: " + e.getMessage());
            return null;
        }
    }

    public void removePendingPrizes(String uuid) {
        String sql = "DELETE FROM pending_prizes WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Remove pending prizes failed: " + e.getMessage());
        }
    }

    // ===== ACTIVE CONTEST PERSISTENCE =====

    public void saveActiveContest(String contestName, long startTime, long endTime, long oldTotal) {
        String sql = "INSERT OR REPLACE INTO active_contest (id, contest_name, start_time, end_time, old_total, last_saved) VALUES (1, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contestName);
            pstmt.setLong(2, startTime);
            pstmt.setLong(3, endTime);
            pstmt.setLong(4, oldTotal);
            pstmt.setLong(5, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Save active contest failed: " + e.getMessage());
        }
    }

    public void saveParticipant(UUID uuid, long startAmount, long savedScore) {
        String sql = "INSERT OR REPLACE INTO active_participants (player_uuid, start_amount, saved_score) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setLong(2, startAmount);
            pstmt.setLong(3, savedScore);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Save participant failed: " + e.getMessage());
        }
    }

    public void removeParticipant(UUID uuid) {
        String sql = "DELETE FROM active_participants WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Remove participant failed: " + e.getMessage());
        }
    }

    public ActiveContestData getActiveContest() {
        if (connection == null) {
            plugin.getLogger().warning("Database not connected - cannot get active contest");
            return null;
        }
        String sql = "SELECT * FROM active_contest WHERE id = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                ActiveContestData data = new ActiveContestData();
                data.contestName = rs.getString("contest_name");
                data.startTime = rs.getLong("start_time");
                data.endTime = rs.getLong("end_time");
                data.oldTotal = rs.getLong("old_total");
                data.lastSaved = rs.getLong("last_saved");
                return data;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Get active contest failed: " + e.getMessage());
        }
        return null;
    }

    public Map<UUID, ParticipantData> getActiveParticipants() {
        Map<UUID, ParticipantData> participants = new HashMap<>();
        if (connection == null) {
            plugin.getLogger().warning("Database not connected - cannot get active participants");
            return participants;
        }
        String sql = "SELECT * FROM active_participants";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                ParticipantData data = new ParticipantData();
                data.startAmount = rs.getLong("start_amount");
                data.savedScore = rs.getLong("saved_score");
                participants.put(uuid, data);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Get active participants failed: " + e.getMessage());
        }
        return participants;
    }

    public void clearActiveContest() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM active_contest");
            stmt.execute("DELETE FROM active_participants");
        } catch (SQLException e) {
            plugin.getLogger().warning("Clear active contest failed: " + e.getMessage());
        }
    }

    // ===== WIN NOTIFICATIONS =====

    public void addWinNotification(String uuid, String contestName, long score, boolean wasTie) {
        String sql = "INSERT OR REPLACE INTO pending_win_notifications (player_uuid, contest_name, score, was_tie) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, contestName);
            pstmt.setLong(3, score);
            pstmt.setBoolean(4, wasTie);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Add win notification failed: " + e.getMessage());
        }
    }

    public WinNotification getWinNotification(String uuid) {
        String sql = "SELECT * FROM pending_win_notifications WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    WinNotification notification = new WinNotification();
                    notification.contestName = rs.getString("contest_name");
                    notification.score = rs.getLong("score");
                    notification.wasTie = rs.getBoolean("was_tie");
                    return notification;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Get win notification failed: " + e.getMessage());
        }
        return null;
    }

    public void removeWinNotification(String uuid) {
        String sql = "DELETE FROM pending_win_notifications WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Remove win notification failed: " + e.getMessage());
        }
    }

    // ===== VOUCHER SYSTEM =====

    public FirstWinner getFirstWinner(String contestName, String tierName) {
        String sql = "SELECT * FROM first_winner_events WHERE contest_name = ? AND tier_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contestName);
            pstmt.setString(2, tierName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    FirstWinner winner = new FirstWinner();
                    winner.playerUuid = rs.getString("player_uuid");
                    winner.playerName = rs.getString("player_name");
                    winner.timestamp = rs.getLong("timestamp");
                    return winner;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Get first winner failed: " + e.getMessage());
        }
        return null;
    }

    public void setFirstWinner(String contestName, String tierName, String playerUuid, String playerName) {
        String sql = "INSERT OR REPLACE INTO first_winner_events (contest_name, tier_name, player_uuid, player_name, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contestName);
            pstmt.setString(2, tierName);
            pstmt.setString(3, playerUuid);
            pstmt.setString(4, playerName);
            pstmt.setLong(5, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Set first winner failed: " + e.getMessage());
        }
    }

    public void addPendingVoucherTrade(String playerUuid, String playerName, String contestName, String tierName) {
        String sql = "INSERT INTO pending_voucher_trades (player_uuid, player_name, contest_name, tier_name, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid);
            pstmt.setString(2, playerName);
            pstmt.setString(3, contestName);
            pstmt.setString(4, tierName);
            pstmt.setLong(5, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Add pending voucher trade failed: " + e.getMessage());
        }
    }

    public List<PendingVoucherTrade> getPendingVoucherTrades(String contestName, String tierName) {
        List<PendingVoucherTrade> trades = new ArrayList<>();
        String sql = "SELECT * FROM pending_voucher_trades WHERE contest_name = ? AND tier_name = ? ORDER BY timestamp ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contestName);
            pstmt.setString(2, tierName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PendingVoucherTrade trade = new PendingVoucherTrade();
                    trade.playerUuid = rs.getString("player_uuid");
                    trade.playerName = rs.getString("player_name");
                    trade.timestamp = rs.getLong("timestamp");
                    trades.add(trade);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Get pending voucher trades failed: " + e.getMessage());
        }
        return trades;
    }

    public void clearPendingVoucherTrades(String contestName, String tierName) {
        String sql = "DELETE FROM pending_voucher_trades WHERE contest_name = ? AND tier_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contestName);
            pstmt.setString(2, tierName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Clear pending voucher trades failed: " + e.getMessage());
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().warning("DB close failed: " + e.getMessage());
            }
        }
    }

    // Data classes for active contest state
    public static class ActiveContestData {
        public String contestName;
        public long startTime;
        public long endTime;
        public long oldTotal;
        public long lastSaved;
    }

    public static class ParticipantData {
        public long startAmount;
        public long savedScore;
    }

    public static class WinNotification {
        public String contestName;
        public long score;
        public boolean wasTie;
    }

    public static class FirstWinner {
        public String playerUuid;
        public String playerName;
        public long timestamp;
    }

    public static class PendingVoucherTrade {
        public String playerUuid;
        public String playerName;
        public long timestamp;
    }
    
    // ==================== TRACKING DATA PERSISTENCE ====================
    
    public void saveTrackingData(String contestName, String trackingType, Map<?, PlayerAttributionTracker.AttributionData> data) {
        if (!isConnected()) return;
        
        String deleteSql = "DELETE FROM tracking_data WHERE contest_name = ? AND tracking_type = ?";
        String insertSql = "INSERT INTO tracking_data (contest_name, tracking_type, key_data, player_uuid, timestamp, metadata) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql);
             PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            
            deleteStmt.setString(1, contestName);
            deleteStmt.setString(2, trackingType);
            deleteStmt.executeUpdate();
            
            for (Map.Entry<?, PlayerAttributionTracker.AttributionData> entry : data.entrySet()) {
                insertStmt.setString(1, contestName);
                insertStmt.setString(2, trackingType);
                insertStmt.setString(3, entry.getKey().toString());
                insertStmt.setString(4, entry.getValue().playerUUID.toString());
                insertStmt.setLong(5, entry.getValue().timestamp);
                insertStmt.setString(6, null);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save tracking data: " + e.getMessage());
        }
    }
    
    public void saveVillagerData(String contestName, Map<UUID, PlayerAttributionTracker.VillagerData> data) {
        if (!isConnected()) return;
        
        String deleteSql = "DELETE FROM tracking_data WHERE contest_name = ? AND tracking_type = 'villager_professions'";
        String insertSql = "INSERT INTO tracking_data (contest_name, tracking_type, key_data, player_uuid, timestamp, metadata) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql);
             PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            
            deleteStmt.setString(1, contestName);
            deleteStmt.executeUpdate();
            
            for (Map.Entry<UUID, PlayerAttributionTracker.VillagerData> entry : data.entrySet()) {
                insertStmt.setString(1, contestName);
                insertStmt.setString(2, "villager_professions");
                insertStmt.setString(3, entry.getKey().toString());
                insertStmt.setString(4, entry.getValue().playerUUID.toString());
                insertStmt.setLong(5, entry.getValue().timestamp);
                insertStmt.setString(6, entry.getValue().profession);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save villager data: " + e.getMessage());
        }
    }
    
    public Map<org.bukkit.Location, PlayerAttributionTracker.AttributionData> loadLocationTrackingData(String contestName, String trackingType) {
        Map<org.bukkit.Location, PlayerAttributionTracker.AttributionData> result = new HashMap<>();
        if (!isConnected()) return result;
        
        String sql = "SELECT key_data, player_uuid, timestamp FROM tracking_data WHERE contest_name = ? AND tracking_type = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contestName);
            pstmt.setString(2, trackingType);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                try {
                    String[] parts = rs.getString("key_data").split(",");
                    org.bukkit.World world = org.bukkit.Bukkit.getWorld(parts[0]);
                    if (world != null) {
                        org.bukkit.Location loc = new org.bukkit.Location(
                            world,
                            Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2]),
                            Double.parseDouble(parts[3])
                        );
                        UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                        long timestamp = rs.getLong("timestamp");
                        result.put(loc, new PlayerAttributionTracker.AttributionData(playerUUID, timestamp));
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to parse location tracking data: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load location tracking data: " + e.getMessage());
        }
        
        return result;
    }
    
    public Map<UUID, PlayerAttributionTracker.AttributionData> loadUUIDTrackingData(String contestName, String trackingType) {
        Map<UUID, PlayerAttributionTracker.AttributionData> result = new HashMap<>();
        if (!isConnected()) return result;
        
        String sql = "SELECT key_data, player_uuid, timestamp FROM tracking_data WHERE contest_name = ? AND tracking_type = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contestName);
            pstmt.setString(2, trackingType);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                try {
                    UUID keyUUID = UUID.fromString(rs.getString("key_data"));
                    UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                    long timestamp = rs.getLong("timestamp");
                    result.put(keyUUID, new PlayerAttributionTracker.AttributionData(playerUUID, timestamp));
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to parse UUID tracking data: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load UUID tracking data: " + e.getMessage());
        }
        
        return result;
    }
    
    public Map<UUID, PlayerAttributionTracker.VillagerData> loadVillagerData(String contestName) {
        Map<UUID, PlayerAttributionTracker.VillagerData> result = new HashMap<>();
        if (!isConnected()) return result;
        
        String sql = "SELECT key_data, player_uuid, timestamp, metadata FROM tracking_data WHERE contest_name = ? AND tracking_type = 'villager_professions'";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contestName);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                try {
                    UUID villagerUUID = UUID.fromString(rs.getString("key_data"));
                    UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                    long timestamp = rs.getLong("timestamp");
                    String profession = rs.getString("metadata");
                    result.put(villagerUUID, new PlayerAttributionTracker.VillagerData(profession, playerUUID, timestamp));
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to parse villager data: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load villager data: " + e.getMessage());
        }
        
        return result;
    }
    
    public void clearTrackingData(String contestName) {
        if (!isConnected()) return;
        
        String sql = "DELETE FROM tracking_data WHERE contest_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contestName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to clear tracking data: " + e.getMessage());
        }
    }
    
    /**
     * Increment win count for a player in a specific contest
     */
    public void incrementWin(String contestName, UUID playerUUID) {
        if (!isConnected()) return;
        
        String sql = "INSERT INTO contest_wins (contest_name, player_uuid, wins) VALUES (?, ?, 1) " +
                     "ON CONFLICT(contest_name, player_uuid) DO UPDATE SET wins = wins + 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contestName);
            pstmt.setString(2, playerUUID.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to increment win for " + playerUUID + ": " + e.getMessage());
        }
    }
    
    /**
     * Get top winners for a contest
     * Returns map of UUID -> win count, ordered by wins descending
     */
    public List<Map.Entry<UUID, Integer>> getTopWinners(String contestName, int limit) {
        List<Map.Entry<UUID, Integer>> results = new ArrayList<>();
        if (!isConnected()) return results;
        
        String sql = "SELECT player_uuid, wins FROM contest_wins WHERE contest_name = ? ORDER BY wins DESC LIMIT ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contestName);
            pstmt.setInt(2, limit);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                int wins = rs.getInt("wins");
                results.add(new java.util.AbstractMap.SimpleEntry<>(uuid, wins));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get top winners: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Get win count for a specific player in a contest
     */
    public int getWinCount(String contestName, UUID playerUUID) {
        if (!isConnected()) return 0;
        
        String sql = "SELECT wins FROM contest_wins WHERE contest_name = ? AND player_uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contestName);
            pstmt.setString(2, playerUUID.toString());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("wins");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get win count: " + e.getMessage());
        }
        
        return 0;
    }
}
