package com.xai.contestplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ContestManager {

    final JavaPlugin plugin; // Changed from private to package-private
    private final DatabaseManager db;
    private final Map<String, Contest> contests = new HashMap<>();
    private boolean staggerContests = false;  // Allow contests at different start times
    private String currentContest = null;
    private long startTime;
    private long endTime;
    private Scoreboard scoreboard;
    private Team team;
    private final Map<UUID, Long> startAmounts = new HashMap<>();
    private final Map<UUID, Long> savedScores = new HashMap<>();
    private final Set<UUID> participants = new HashSet<>();
    private final List<Integer> scheduledTasks = new ArrayList<>();
    private long total = 0;
    private long oldTotal = 0;
    private long highScore = 0;
    private final List<UUID> winners = new ArrayList<>();
    
    // Debug mode
    private static boolean debugMode = false;
    
    // Pending leave confirmations (for /contest leave command)
    private final Map<UUID, Long> pendingLeaveConfirmations = new HashMap<>();
    private static final long CONFIRMATION_TIMEOUT = 30000; // 30 seconds
    
    // New event-driven scoring system
    private ScoringEngine scoringEngine;
    private CommandExecutorService commandExecutor;
    private UniversalEventListener universalListener;
    private PlayerAttributionTracker attributionTracker;
    private int autoSaveTaskId = -1;
    private int cleanupTaskId = -1;

    public ContestManager(JavaPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        this.commandExecutor = new CommandExecutorService(plugin);
        this.attributionTracker = new PlayerAttributionTracker(plugin, db);
        this.universalListener = new UniversalEventListener(this, attributionTracker);
        
        // Register universal event listener
        Bukkit.getPluginManager().registerEvents(universalListener, plugin);
        
        // Start auto-save task (every 5 minutes)
        autoSaveTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (currentContest != null && scoringEngine != null) {
                try {
                    attributionTracker.saveToDatabase(currentContest);
                } catch (Exception e) {
                    plugin.getLogger().warning("Auto-save failed: " + e.getMessage());
                }
            }
        }, 6000L, 6000L).getTaskId();
        
        // Start cleanup task (every hour)
        cleanupTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                attributionTracker.cleanupOldData();
            } catch (Exception e) {
                plugin.getLogger().warning("Cleanup failed: " + e.getMessage());
            }
        }, 72000L, 72000L).getTaskId();
    }

    public void loadContests() {
        contests.clear();
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "contests.yml"));
        
        // Load stagger option (default false for backwards compatibility)
        staggerContests = config.getBoolean("stagger_contests", false);
        
        ConfigurationSection contestsSection = config.getConfigurationSection("contests");
        if (contestsSection == null) return;
        for (String key : contestsSection.getKeys(false)) {
            Contest contest = parseContest(key, contestsSection.getConfigurationSection(key));
            if (contest != null) {
                contests.put(key, contest);
            }
        }
    }

    private Contest parseContest(String name, ConfigurationSection section) {
        Contest contest = new Contest();
        contest.setName(name);
        contest.setChance(section.getInt("chance", 0));
        contest.setDescription(section.getString("description"));  // Optional custom description
        
        // Only parse stat_type if present (old system)
        String statTypeStr = section.getString("stat_type");
        String statKeyStr = section.getString("stat_key");
        if (statTypeStr != null) {
            try {
                Statistic statType = Statistic.valueOf(statTypeStr);
                Object statKey = null;
                if (statType.getType() == Statistic.Type.BLOCK || statType.getType() == Statistic.Type.ITEM) {
                    statKey = Material.valueOf(statKeyStr);
                } else if (statType.getType() == Statistic.Type.ENTITY) {
                    statKey = EntityType.valueOf(statKeyStr);
                }
                contest.setStatType(statType);
                contest.setStatKey(statKey);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid stat for contest " + name + ": " + statTypeStr + " " + statKeyStr);
                return null;
            }
        }
        
        contest.setDurationMinutes(section.getInt("duration_minutes", 20));
        contest.setWarningMinutes(section.getInt("warning_minutes", 5));
        contest.setCountdownInterval(section.getInt("countdown_interval_minutes", 1));
        contest.setStartMinute(section.getInt("start_minute", 0));  // Default 0 (top of hour)
        ConfigurationSection areaSection = section.getConfigurationSection("area");
        if (areaSection != null) {
            World world = Bukkit.getWorld(areaSection.getString("world"));
            if (world == null) {
                plugin.getLogger().warning("Invalid world for contest " + name);
                return null;
            }
            contest.setAreaWorld(world);
            contest.setMinX(areaSection.getInt("min_x", -300));
            contest.setMaxX(areaSection.getInt("max_x", 300));
            contest.setMinY(areaSection.getInt("min_y", 61));
            contest.setMaxY(areaSection.getInt("max_y", 256));
            contest.setMinZ(areaSection.getInt("min_z", -300));
            contest.setMaxZ(areaSection.getInt("max_z", 300));
        }
        ConfigurationSection messagesSection = section.getConfigurationSection("messages");
        Map<String, String> messages = new HashMap<>();
        if (messagesSection != null) {
            for (String msgKey : messagesSection.getKeys(false)) {
                messages.put(msgKey, messagesSection.getString(msgKey));
            }
        }
        contest.setMessages(messages);
        ConfigurationSection rewardSection = section.getConfigurationSection("reward_conditions");
        if (rewardSection != null) {
            contest.setMinTotal(rewardSection.getLong("min_total", 0));
            contest.setDoublePrevious(rewardSection.getBoolean("double_previous", false));
            contest.setOnMetCommands(rewardSection.getStringList("on_met_commands"));
            contest.setOnNotMetCommands(rewardSection.getStringList("on_not_met_commands"));
        }
        ConfigurationSection prizesSection = section.getConfigurationSection("prizes");
        if (prizesSection != null) {
            contest.setWinnerPrize(parseItem(prizesSection.getConfigurationSection("winner_item")));
            contest.setWinnerPrizeAmount(prizesSection.getInt("winner_prize_amount", 1));  // Default 1
            String locStr = prizesSection.getString("tie_chest_location");
            if (locStr != null) {
                String[] parts = locStr.split(" ");
                World world = Bukkit.getWorld(parts[0]);
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                contest.setTieChestLocation(new Location(world, x, y, z));
            }
        }
        ConfigurationSection scoreboardSection = section.getConfigurationSection("scoreboard");
        if (scoreboardSection != null) {
            contest.setScoreboardObjective(scoreboardSection.getString("objective"));
            contest.setScoreboardDisplay(scoreboardSection.getString("display_name"));
            contest.setTeamName(scoreboardSection.getString("team_name"));
            contest.setTeamColor(scoreboardSection.getString("team_color"));
        }
        contest.setUpgradeCommand(section.getString("upgrade_command"));  // e.g., "tradepick"
        ConfigurationSection upgradesSection = section.getConfigurationSection("upgrades");
        List<UpgradeTier> upgrades = new ArrayList<>();
        if (upgradesSection != null) {
            for (String tierKey : upgradesSection.getKeys(false)) {
                ConfigurationSection tierSection = upgradesSection.getConfigurationSection(tierKey);
                UpgradeTier tier = new UpgradeTier();
                tier.setName(tierKey);  // Store the tier name (e.g., "tier1", "tier2")
                tier.setCheckLore(tierSection.getString("check_lore"));
                tier.setRequired(tierSection.getInt("required", 10));
                tier.setGive(parseItem(tierSection.getConfigurationSection("give")));
                upgrades.add(tier);
            }
        }
        contest.setUpgrades(upgrades);
        
        // Parse scoring rules (new event-driven system)
        if (section.contains("scoring_rules")) {
            try {
                List<ScoringRule> scoringRules = RuleParser.parseRules(section);
                contest.setScoringRules(scoringRules);
                contest.setContestType("rule_based");
                plugin.getLogger().info("Loaded " + scoringRules.size() + " scoring rules for contest: " + name);
            } catch (Exception e) {
                plugin.getLogger().warning("Error parsing scoring rules for contest " + name + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else if (statTypeStr != null) {
            // Legacy statistic-based contest
            contest.setContestType("statistic");
        }
        
        return contest;
    }

    private ItemStack parseItem(ConfigurationSection section) {
        if (section == null) return null;
        Material material = Material.valueOf(section.getString("material", "STONE"));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        String name = section.getString("name");
        if (name != null) meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> lore = section.getStringList("lore");
        if (!lore.isEmpty()) meta.setLore(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
        ConfigurationSection enchants = section.getConfigurationSection("enchants");
        if (enchants != null) {
            for (String enchantKey : enchants.getKeys(false)) {
                Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(enchantKey.toLowerCase()));
                if (ench != null) {
                    int level = enchants.getInt(enchantKey);
                    meta.addEnchant(ench, level, true);
                }
            }
            // Hide enchantment list from tooltip (but keep "When in Main Hand:" stats)
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        List<Map<?, ?>> attributes = section.getMapList("attributes");
        for (Map<?, ?> attrMap : attributes) {
            try {
                String type = (String) attrMap.get("type");
                double amount = ((Number) attrMap.get("amount")).doubleValue();
                String slotStr = (String) attrMap.get("slot");
                String operation = (String) attrMap.get("operation");
                String id = (String) attrMap.get("id");
                
                // Validate required fields
                if (id == null || id.trim().isEmpty()) {
                    plugin.getLogger().warning("Attribute missing 'id' field in contest config. Skipping attribute.");
                    continue;
                }
                if (type == null) {
                    plugin.getLogger().warning("Attribute missing 'type' field. Skipping.");
                    continue;
                }
                if (slotStr == null) {
                    plugin.getLogger().warning("Attribute missing 'slot' field. Skipping.");
                    continue;
                }
                
                Attribute attribute = Attribute.valueOf(type.toUpperCase());
                EquipmentSlot slot = EquipmentSlot.valueOf(slotStr.toUpperCase());
                AttributeModifier.Operation op = AttributeModifier.Operation.valueOf(operation);
                NamespacedKey key = new NamespacedKey(plugin, id);
                AttributeModifier modifier = new AttributeModifier(key, amount, op, slot.getGroup());
                meta.addAttributeModifier(attribute, modifier);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to parse attribute: " + e.getMessage());
                plugin.getLogger().warning("Attribute data: " + attrMap);
            }
        }
        item.setItemMeta(meta);
        return item;
    }

    public void startScheduler() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (currentContest != null) return;
            LocalDateTime now = LocalDateTime.now();
            int currentMinute = now.getMinute();
            
            if (staggerContests) {
                // Stagger mode: Start contests at their configured start_minute
                for (Contest c : contests.values()) {
                    if (c.getChance() > 0 && c.getStartMinute() == currentMinute) {
                        scheduleWarningAndStart(c.getName());
                        break;  // Only start one contest at a time
                    }
                }
            } else {
                // Original mode: Only at top of hour (minute 0), pick random contest
                if (currentMinute == 0) {
                    int totalChance = contests.values().stream().mapToInt(Contest::getChance).sum();
                    if (totalChance <= 0) return;
                    int roll = ThreadLocalRandom.current().nextInt(totalChance);
                    int cumulative = 0;
                    for (Contest c : contests.values()) {
                        cumulative += c.getChance();
                        if (roll < cumulative) {
                            scheduleWarningAndStart(c.getName());
                            break;
                        }
                    }
                }
            }
        }, 0L, 1200L); // Check every minute
    }

    private void scheduleWarningAndStart(String contestName) {
        Contest contest = contests.get(contestName);
        long warningTicks = contest.getWarningMinutes() * 60L * 20L;
        String msg = contest.getMessages().get("pre_start");
        if (msg == null) {
            msg = "&e{contest_name} contest starting in {warning_minutes} minutes!";
        }
        msg = msg.replace("{contest_name}", contestName).replace("{warning_minutes}", String.valueOf(contest.getWarningMinutes()));
        String finalMsg = ChatColor.translateAlternateColorCodes('&', msg);
        Bukkit.broadcastMessage(finalMsg);
        Bukkit.getScheduler().runTaskLater(plugin, () -> startContest(contestName), warningTicks);
    }

    public void startContest(String contestName) {
        if (currentContest != null) {
            plugin.getLogger().warning("Cannot start " + contestName + " while another contest is running.");
            return;
        }
        Contest contest = contests.get(contestName);
        if (contest == null) {
            plugin.getLogger().warning("No contest named " + contestName);
            return;
        }
        currentContest = contestName;
        startTime = System.currentTimeMillis();
        endTime = startTime + contest.getDurationMinutes() * 60L * 1000L;
        startAmounts.clear();
        savedScores.clear();
        participants.clear();
        scheduledTasks.clear();
        total = 0;
        oldTotal = db.getLastTotal(contestName);
        highScore = 0;
        winners.clear();
        
        // Initialize scoring engine for rule-based contests
        if (contest.isRuleBased()) {
            scoringEngine = new ScoringEngine(plugin, contest, commandExecutor);
            plugin.getLogger().info("Initialized ScoringEngine for rule-based contest: " + contestName);
            
            // Load attribution tracking data
            try {
                attributionTracker.loadFromDatabase(contestName);
                plugin.getLogger().info("Loaded attribution tracking data for: " + contestName);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load attribution data: " + e.getMessage());
            }
        } else {
            scoringEngine = null;
        }
        
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(contest.getScoreboardObjective(), Criteria.DUMMY, ChatColor.translateAlternateColorCodes('&', contest.getScoreboardDisplay()));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        team = scoreboard.registerNewTeam(contest.getTeamName());
        try {
            team.setColor(ChatColor.valueOf(contest.getTeamColor()));
        } catch (IllegalArgumentException e) {
            team.setColor(ChatColor.WHITE);
        }
        String startMsg = contest.getMessages().get("start_broadcast");
        if (startMsg == null) {
            startMsg = "&e{contest_name} contest has started!";
        }
        startMsg = startMsg.replace("{contest_name}", contestName);
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', startMsg));
        String phantomMsg = contest.getMessages().get("phantom_announce");
        if (phantomMsg != null) {
            phantomMsg = phantomMsg.replace("{contest_name}", contestName)
                    .replace("{old_total}", String.valueOf(oldTotal))
                    .replace("{double_threshold}", String.valueOf(oldTotal * 2));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', phantomMsg));
        }
        long durationTicks = contest.getDurationMinutes() * 60L * 20L;
        long intervalTicks = contest.getCountdownInterval() * 60L * 20L;
        for (long i = intervalTicks; i <= durationTicks; i += intervalTicks) {
            final long delay = i;
            int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (currentContest == null) return;  // Contest ended
                long ticksLeft = durationTicks - delay;
                int minutesLeft = (int) (ticksLeft / (60L * 20L));
                sendCountdown(minutesLeft);
                checkPlayerAreas();
            }, delay).getTaskId();
            scheduledTasks.add(taskId);
        }
        // Real-time scoreboard updates every 0.1 seconds
        int updateTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (currentContest == null) return;  // Contest ended
            Contest c = getCurrentContest();
            if (c == null) return;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (participants.contains(player.getUniqueId())) {
                    int score;
                    if (c.isRuleBased()) {
                        if (scoringEngine != null) {
                            // Use scoring engine for rule-based contests
                            score = scoringEngine.getScore(player.getUniqueId());
                        } else {
                            // Rule-based but engine not ready - use saved score or 0
                            score = savedScores.getOrDefault(player.getUniqueId(), 0L).intValue();
                        }
                    } else {
                        // Use statistic for legacy contests
                        long statScore = getPlayerStat(player, c.getStatType(), c.getStatKey()) - startAmounts.get(player.getUniqueId());
                        score = (int) statScore;
                    }
                    scoreboard.getObjective(c.getScoreboardObjective()).getScore(player.getName()).setScore(score);
                }
            }
        }, 2L, 2L).getTaskId();  // Start after 0.1 seconds, repeat every 0.1 seconds (10 times per second)
        scheduledTasks.add(updateTaskId);
        
        // Start periodic timers for rule-based contests
        if (contest.isRuleBased() && scoringEngine != null) {
            scoringEngine.startPeriodicTimers();
        }
        
        // Area check every 2 seconds (40 ticks)
        int areaCheckTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (currentContest == null) return;  // Contest ended
            checkPlayerAreas();
        }, 40L, 40L).getTaskId();  // Start after 2 seconds, repeat every 2 seconds
        scheduledTasks.add(areaCheckTaskId);
        
        // Save contest state every minute (1200 ticks) for crash recovery
        int saveTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (currentContest == null) return;  // Contest ended
            saveContestState();
        }, 1200L, 1200L).getTaskId();  // Every 60 seconds
        scheduledTasks.add(saveTaskId);
        
        // Save initial state immediately
        saveContestState();
        
        int endTaskId = Bukkit.getScheduler().runTaskLater(plugin, this::endContest, durationTicks).getTaskId();
        scheduledTasks.add(endTaskId);
    }

    public void endContest() {
        if (currentContest == null) return;
        
        // Cancel all scheduled tasks
        for (int taskId : scheduledTasks) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        scheduledTasks.clear();
        
        Contest contest = getCurrentContest();
        
        try {
            // Collect all players who participated (active + saved scores)
            Set<UUID> allParticipants = new HashSet<>();
            allParticipants.addAll(participants);
            allParticipants.addAll(savedScores.keySet());
        
        for (UUID uuid : allParticipants) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
            long score = 0;
            if (participants.contains(uuid) && p.isOnline()) {
                // Currently active and online
                Player player = p.getPlayer();
                if (contest.isRuleBased()) {
                    if (scoringEngine != null) {
                        // Get score from scoring engine
                        score = scoringEngine.getScore(uuid);
                    } else {
                        // Rule-based but engine not ready - use saved score
                        score = savedScores.getOrDefault(uuid, 0L);
                    }
                } else {
                    // Legacy statistic-based score
                    score = getPlayerStat(player, contest.getStatType(), contest.getStatKey()) - startAmounts.get(uuid);
                }
                savedScores.put(uuid, score); // Save for database
            } else if (savedScores.containsKey(uuid)) {
                // Offline or left area - use saved score
                score = savedScores.get(uuid);
            }
            total += score;
            if (score > highScore) {
                highScore = score;
                winners.clear();
                winners.add(uuid);
            } else if (score == highScore && highScore > 0) {
                winners.add(uuid);
            }
        }
        
        // Broadcast total (optional message)
        String totalMsg = contest.getMessages().get("total_broadcast");
        if (totalMsg != null) {
            totalMsg = totalMsg.replace("{total}", String.valueOf(total)).replace("{tracked_item}", contest.getStatKey() != null ? contest.getStatKey().toString().toLowerCase() : "items");
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', totalMsg));
        }
        
        if (winners.isEmpty()) {
            String noWinnersMsg = contest.getMessages().get("no_winners");
            if (noWinnersMsg != null) {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', noWinnersMsg));
            }
        } else if (winners.size() == 1) {
            OfflinePlayer winner = Bukkit.getOfflinePlayer(winners.get(0));
            String winMsg = contest.getMessages().get("single_winner");
            if (winMsg != null) {
                winMsg = winMsg.replace("{winner_name}", winner.getName()).replace("{high_score}", String.valueOf(highScore));
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', winMsg));
            }
            
            // Give winner prizes (multiple if configured)
            int amount = contest.getWinnerPrizeAmount();
            for (int i = 0; i < amount; i++) {
                givePrize(winner, contest.getWinnerPrize().clone());
            }
            
            // Save win notification for offline players
            if (!winner.isOnline()) {
                db.addWinNotification(winner.getUniqueId().toString(), contest.getName(), highScore, false);
            }
            
            // Track win in database
            db.incrementWin(contest.getName(), winner.getUniqueId());
        } else {
            String tieMsg = contest.getMessages().get("tie");
            if (tieMsg != null) {
                tieMsg = tieMsg.replace("{winners_count}", String.valueOf(winners.size())).replace("{high_score}", String.valueOf(highScore));
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', tieMsg));
            }
            Location chestLoc = contest.getTieChestLocation();
            if (chestLoc != null && chestLoc.getBlock().getState() instanceof Chest chest) {
                int amount = contest.getWinnerPrizeAmount();
                for (UUID uuid : winners) {
                    // Give multiple prizes if configured
                    for (int i = 0; i < amount; i++) {
                        chest.getInventory().addItem(contest.getWinnerPrize().clone());
                    }
                    
                    // Save win notification for offline players
                    OfflinePlayer offlineWinner = Bukkit.getOfflinePlayer(uuid);
                    if (!offlineWinner.isOnline()) {
                        db.addWinNotification(uuid.toString(), contest.getName(), highScore, true);
                    }
                    
                    // Track win in database
                    db.incrementWin(contest.getName(), uuid);
                }
            }
        }
        
        // Reward conditions check (optional - only if messages exist)
        String conditionCheckMsg = contest.getMessages().get("condition_check");
        if (conditionCheckMsg != null) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', conditionCheckMsg));
        }
        
        boolean met = total >= contest.getMinTotal() && (!contest.isDoublePrevious() || total > oldTotal * 2);
        if (met) {
            String metMsg = contest.getMessages().get("met");
            if (metMsg != null) {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', metMsg
                        .replace("{total}", String.valueOf(total))
                        .replace("{double_threshold}", String.valueOf(oldTotal * 2))));
            }
            for (String cmd : contest.getOnMetCommands()) {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to execute reward command: " + cmd);
                    plugin.getLogger().warning("Error: " + e.getMessage());
                }
            }
        } else {
            String notMetMsg = contest.getMessages().get("not_met");
            if (notMetMsg != null) {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', notMetMsg
                        .replace("{total}", String.valueOf(total))
                        .replace("{double_threshold}", String.valueOf(oldTotal * 2))));
            }
            for (String cmd : contest.getOnNotMetCommands()) {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to execute reward command: " + cmd);
                    plugin.getLogger().warning("Error: " + e.getMessage());
                }
            }
        }
        Timestamp start = Timestamp.valueOf(LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(startTime), java.time.ZoneId.systemDefault()));
        Timestamp end = Timestamp.valueOf(LocalDateTime.now());
        String winnersStr = winners.stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.joining(", "));
        int runId = db.insertPastRun(currentContest, start, end, total, oldTotal, highScore, winnersStr);
        if (runId != -1) {
            for (UUID uuid : participants) {
                long score = savedScores.getOrDefault(uuid, 0L);
                db.insertScore(runId, uuid.toString(), score);
            }
        }
        
        } finally {
            // Save attribution tracking data
            if (currentContest != null) {
                try {
                    attributionTracker.saveToDatabase(currentContest);
                    plugin.getLogger().info("Saved attribution tracking data for: " + currentContest);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to save attribution data: " + e.getMessage());
                }
            }
            
            // Cleanup scoring engine
            if (scoringEngine != null) {
                scoringEngine.clearAllData();
                scoringEngine = null;
            }
            
            // Clear attribution tracker
            attributionTracker.clearAll();
            
            currentContest = null;
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                p.removeScoreboardTag("contest");
            }
            
            // Clear saved contest state from database
            db.clearActiveContest();
            
            // Clear attribution tracking data from database
            if (contest != null) {
                db.clearTrackingData(contest.getName());
            }
        }
    }

    public void join(Player player) {
        if (currentContest == null) {
            player.sendMessage("No contest is currently running.");
            return;
        }
        Contest contest = getCurrentContest();
        if (!isInArea(player, contest)) {
            player.sendMessage(replacePlaceholders(contest.getMessages().get("invalid_area"), player, contest, 0));
            return;
        }
        
        // Check if player is rejoining with a saved score
        if (savedScores.containsKey(player.getUniqueId())) {
            // Rejoining - restore their saved score
            participants.add(player.getUniqueId());
            
            long savedScore = savedScores.get(player.getUniqueId());
            
            // Add to scoring engine if rule-based
            if (contest.isRuleBased() && scoringEngine != null) {
                scoringEngine.addParticipant(player.getUniqueId());
                scoringEngine.setScore(player.getUniqueId(), (int) savedScore);
                startAmounts.put(player.getUniqueId(), 0L);
            } else {
                // For statistic-based, calculate start amount to restore score
                long currentStat = getPlayerStat(player, contest.getStatType(), contest.getStatKey());
                startAmounts.put(player.getUniqueId(), currentStat - savedScore);
            }
            
            player.setScoreboard(scoreboard);
            team.addEntry(player.getName());
            player.addScoreboardTag("contest");
            
            player.sendMessage("§aWelcome back! Your score has been restored: §e" + savedScore);
            return;
        }
        
        // Check if already participating (shouldn't happen with new logic)
        if (participants.contains(player.getUniqueId())) {
            long currentScore;
            if (contest.isRuleBased() && scoringEngine != null) {
                currentScore = scoringEngine.getScore(player.getUniqueId());
            } else {
                currentScore = getPlayerStat(player, contest.getStatType(), contest.getStatKey()) - startAmounts.get(player.getUniqueId());
            }
            player.sendMessage(replacePlaceholders(contest.getMessages().get("already_in"), player, contest, currentScore));
            return;
        }
        
        // New participant
        participants.add(player.getUniqueId());
        
        // Add to scoring engine if rule-based
        if (contest.isRuleBased() && scoringEngine != null) {
            scoringEngine.addParticipant(player.getUniqueId());
            // Process CONTEST_JOIN event
            scoringEngine.processEvent(EventType.CONTEST_JOIN, null, player);
            startAmounts.put(player.getUniqueId(), 0L); // Rule-based contests don't use start amounts
        } else {
            // Statistic-based contests need start amount
            startAmounts.put(player.getUniqueId(), getPlayerStat(player, contest.getStatType(), contest.getStatKey()));
        }
        player.setScoreboard(scoreboard);
        team.addEntry(player.getName());
        player.addScoreboardTag("contest");
        player.sendMessage(replacePlaceholders(contest.getMessages().get("join"), player, contest, 0));
    }

    public void leave(Player player) {
        if (!participants.contains(player.getUniqueId())) {
            player.sendMessage("§cYou are not in a contest.");
            return;
        }
        
        Contest contest = getCurrentContest();
        long score;
        if (contest.isRuleBased() && scoringEngine != null) {
            score = scoringEngine.getScore(player.getUniqueId());
        } else {
            score = getPlayerStat(player, contest.getStatType(), contest.getStatKey()) - startAmounts.get(player.getUniqueId());
        }
        
        // Show confirmation message with clickable yes/no
        player.sendMessage("");
        player.sendMessage("§e⚠ Are you sure you want to leave the contest?");
        player.sendMessage("§7Your current score: §e" + score);
        player.sendMessage("§c§lYour score will be RESET to 0 if you leave!");
        player.sendMessage("");
        
        // Create clickable yes/no buttons using JSON text components
        try {
            // Use Bukkit's built-in Adventure API for 1.21.1
            net.kyori.adventure.text.Component yesButton = net.kyori.adventure.text.Component.text()
                .append(net.kyori.adventure.text.Component.text("[YES - RESET MY SCORE]")
                    .color(net.kyori.adventure.text.format.NamedTextColor.RED)
                    .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD)
                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/contest confirmleave yes"))
                    .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                        net.kyori.adventure.text.Component.text("Click to leave and reset score to 0")
                            .color(net.kyori.adventure.text.format.NamedTextColor.RED)
                    )))
                .build();
            
            net.kyori.adventure.text.Component noButton = net.kyori.adventure.text.Component.text()
                .append(net.kyori.adventure.text.Component.text("[NO - STAY IN CONTEST]")
                    .color(net.kyori.adventure.text.format.NamedTextColor.GREEN)
                    .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD)
                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/contest confirmleave no"))
                    .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                        net.kyori.adventure.text.Component.text("Click to stay in the contest")
                            .color(net.kyori.adventure.text.format.NamedTextColor.GREEN)
                    )))
                .build();
            
            net.kyori.adventure.text.Component message = net.kyori.adventure.text.Component.text()
                .append(yesButton)
                .append(net.kyori.adventure.text.Component.text("  "))
                .append(noButton)
                .build();
            
            player.sendMessage(message);
        } catch (Exception e) {
            // Fallback for older servers without Adventure API
            player.sendMessage("§c[YES] §7or §a[NO]");
            player.sendMessage("§7Type: §e/contest confirmleave yes §7or §e/contest confirmleave no");
        }
        
        player.sendMessage("");
        player.sendMessage("§7This confirmation expires in 30 seconds.");
        player.sendMessage("");
        
        // Track pending confirmation
        pendingLeaveConfirmations.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public void confirmLeave(Player player, boolean confirmed) {
        UUID uuid = player.getUniqueId();
        
        // Check if there's a pending confirmation
        if (!pendingLeaveConfirmations.containsKey(uuid)) {
            player.sendMessage("§cYou don't have a pending leave request.");
            player.sendMessage("§7Use §e/contest leave §7to exit the contest.");
            return;
        }
        
        // Check if confirmation expired
        long requestTime = pendingLeaveConfirmations.get(uuid);
        if (System.currentTimeMillis() - requestTime > CONFIRMATION_TIMEOUT) {
            pendingLeaveConfirmations.remove(uuid);
            player.sendMessage("§cLeave confirmation expired. Use §e/contest leave §cagain if you want to quit.");
            return;
        }
        
        // Remove pending confirmation
        pendingLeaveConfirmations.remove(uuid);
        
        if (!confirmed) {
            player.sendMessage("§aYou chose to stay in the contest. Keep going!");
            return;
        }
        
        // Player confirmed - leave and RESET score
        leaveWithReset(player);
    }
    
    public void leaveWithReset(Player player) {
        if (!participants.contains(player.getUniqueId())) {
            player.sendMessage("§cYou are not in a contest.");
            return;
        }
        
        Contest contest = getCurrentContest();
        
        long score;
        if (contest.isRuleBased()) {
            if (scoringEngine != null) {
                // Get final score before removing
                score = scoringEngine.getScore(player.getUniqueId());
                // Process CONTEST_LEAVE event
                scoringEngine.processEvent(EventType.CONTEST_LEAVE, null, player);
                // Remove from scoring engine and clear all score data
                scoringEngine.removeParticipant(player.getUniqueId());
                scoringEngine.clearScore(player.getUniqueId()); // IMPORTANT: Clear score data
            } else {
                // Rule-based but engine not ready - use saved score or 0
                score = savedScores.getOrDefault(player.getUniqueId(), 0L);
            }
        } else {
            // Legacy statistic-based score
            score = getPlayerStat(player, contest.getStatType(), contest.getStatKey()) - startAmounts.get(player.getUniqueId());
        }
        
        // IMPORTANT: DO NOT save the score - let it reset
        // Remove any saved score so they start fresh if they rejoin
        savedScores.remove(player.getUniqueId());
        
        // Remove from active participants
        participants.remove(player.getUniqueId());
        startAmounts.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        player.removeScoreboardTag("contest");
        
        player.sendMessage("");
        player.sendMessage("§cYou have left the contest.");
        player.sendMessage("§7Your final score was: §e" + score);
        player.sendMessage("§c§lYour score has been RESET.");
        player.sendMessage("§7If you rejoin with §e/contest join§7, you will start at 0 points.");
        player.sendMessage("");
    }

    public void leaveArea(Player player) {
        // Called when player automatically leaves contest area
        if (!participants.contains(player.getUniqueId())) {
            return;
        }
        Contest contest = getCurrentContest();
        
        long score;
        if (contest.isRuleBased()) {
            if (scoringEngine != null) {
                score = scoringEngine.getScore(player.getUniqueId());
            } else {
                // Rule-based but engine not ready - use saved score or 0
                score = savedScores.getOrDefault(player.getUniqueId(), 0L);
            }
        } else {
            score = getPlayerStat(player, contest.getStatType(), contest.getStatKey()) - startAmounts.get(player.getUniqueId());
        }
        
        // Save the score so they can rejoin
        savedScores.put(player.getUniqueId(), score);
        
        // Remove from active participants
        participants.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        player.removeScoreboardTag("contest");
        
        // Use the special "left_area" message with area boundaries
        String message = contest.getMessages().get("left_area");
        if (message != null) {
            player.sendMessage(replacePlaceholders(message, player, contest, score));
        } else {
            // Fallback to regular leave message if left_area not defined
            player.sendMessage(replacePlaceholders(contest.getMessages().get("leave"), player, contest, score));
            player.sendMessage("§7You can rejoin with §e/contest join §7before the contest ends!");
        }
    }

    public void savePlayerScore(Player player) {
        // Called when a player logs off during a contest
        if (currentContest == null) return;
        if (!participants.contains(player.getUniqueId())) return;
        
        Contest contest = getCurrentContest();
        
        long score;
        if (contest.isRuleBased()) {
            if (scoringEngine != null) {
                score = scoringEngine.getScore(player.getUniqueId());
            } else {
                // Rule-based but engine not ready - use saved score or 0
                score = savedScores.getOrDefault(player.getUniqueId(), 0L);
            }
        } else {
            score = getPlayerStat(player, contest.getStatType(), contest.getStatKey()) - startAmounts.get(player.getUniqueId());
        }
        
        // Save the score
        savedScores.put(player.getUniqueId(), score);
        
        // Remove from active participants (they're offline now)
        participants.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        player.removeScoreboardTag("contest");
    }

    private void checkPlayerAreas() {
        Contest contest = getCurrentContest();
        if (contest == null) return;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (participants.contains(player.getUniqueId())) {
                if (!isInArea(player, contest)) {
                    // Player left the area - kick them out with area-specific message
                    leaveArea(player);
                }
            }
        }
    }

    private boolean isInArea(Player player, Contest contest) {
        Location loc = player.getLocation();
        return loc.getWorld().equals(contest.getAreaWorld()) &&
                loc.getBlockX() >= contest.getMinX() && loc.getBlockX() <= contest.getMaxX() &&
                loc.getBlockY() >= contest.getMinY() && loc.getBlockY() <= contest.getMaxY() &&
                loc.getBlockZ() >= contest.getMinZ() && loc.getBlockZ() <= contest.getMaxZ();
    }

    private long getPlayerStat(OfflinePlayer player, Statistic stat, Object key) {
        // Return 0 if stat is null (happens for rule-based contests)
        if (stat == null) {
            return 0;
        }
        if (key == null) {
            return player.getStatistic(stat);
        } else if (key instanceof Material material) {
            return player.getStatistic(stat, material);
        } else if (key instanceof EntityType entityType) {
            return player.getStatistic(stat, entityType);
        }
        return 0;
    }

    private void sendCountdown(int minutes) {
        Contest contest = getCurrentContest();
        if (contest == null) return;  // Contest ended
        
        // Use singular or plural message based on minutes
        String messageKey = (minutes == 1) ? "countdown_singular" : "countdown";
        String msg = replacePlaceholders(contest.getMessages().get(messageKey), null, contest, minutes);
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getScoreboardTags().contains("contest")) {
                p.sendMessage(msg);
                // Send score update with countdown
                if (participants.contains(p.getUniqueId())) {
                    long score;
                    if (contest.isRuleBased() && scoringEngine != null) {
                        score = scoringEngine.getScore(p.getUniqueId());
                    } else {
                        score = getPlayerStat(p, contest.getStatType(), contest.getStatKey()) - startAmounts.get(p.getUniqueId());
                    }
                    String scoreMsg = replacePlaceholders(contest.getMessages().get("current_score"), p, contest, score);
                    p.sendMessage(scoreMsg);
                }
            }
        }
    }

    private String replacePlaceholders(String msg, Player player, Contest contest, long scoreValue) {
        if (msg == null) return "";
        msg = msg.replace("{contest_name}", contest.getName());
        msg = msg.replace("{contest_name_upper}", contest.getName().toUpperCase());
        msg = msg.replace("{player_name}", player != null ? player.getName() : "");
        msg = msg.replace("{current_score}", String.valueOf(scoreValue));
        msg = msg.replace("{final_score}", String.valueOf(scoreValue));
        msg = msg.replace("{total}", String.valueOf(total));
        msg = msg.replace("{old_total}", String.valueOf(oldTotal));
        msg = msg.replace("{double_threshold}", String.valueOf(oldTotal * 2));
        msg = msg.replace("{high_score}", String.valueOf(highScore));
        msg = msg.replace("{winner_name}", winners.isEmpty() ? "N/A" : Bukkit.getOfflinePlayer(winners.get(0)).getName());
        msg = msg.replace("{winners_count}", String.valueOf(winners.size()));
        msg = msg.replace("{minutes_left}", String.valueOf(scoreValue)); // Reused param for minutes
        msg = msg.replace("{warning_minutes}", String.valueOf(contest.getWarningMinutes()));
        msg = msg.replace("{tracked_item}", contest.getStatKey() != null ? contest.getStatKey().toString().toLowerCase() : "items");
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public void givePrize(OfflinePlayer player, ItemStack prize) {
        if (player.isOnline()) {
            Player p = player.getPlayer();
            giveItemSafely(p, prize);
            p.sendMessage("You received your contest prize!");
        } else {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {
                boos.writeObject(prize);
                String serialized = Base64.getEncoder().encodeToString(baos.toByteArray());
                db.addPendingPrize(player.getUniqueId().toString(), serialized);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to serialize prize: " + e.getMessage());
            }
        }
    }
    
    /**
     * Safely give item to player. If inventory is full, drops overflow on ground.
     */
    private void giveItemSafely(Player player, ItemStack item) {
        // addItem returns items that couldn't fit
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
        
        // Drop overflow items at player's feet
        if (!overflow.isEmpty()) {
            Location loc = player.getLocation();
            for (ItemStack leftover : overflow.values()) {
                player.getWorld().dropItemNaturally(loc, leftover);
            }
            player.sendMessage("§eInventory full! Some items dropped at your feet.");
        }
    }

    public void givePendingPrizes(Player player) {
        ResultSet rs = db.getPendingPrizes(player.getUniqueId().toString());
        if (rs == null) return;
        try {
            while (rs.next()) {
                String serialized = rs.getString("item");
                byte[] bytes = Base64.getDecoder().decode(serialized);
                try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes); BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {
                    ItemStack item = (ItemStack) bois.readObject();
                    giveItemSafely(player, item);
                    player.sendMessage("Received pending contest prize!");
                }
            }
            db.removePendingPrizes(player.getUniqueId().toString());
        } catch (SQLException | IOException | ClassNotFoundException e) {
            plugin.getLogger().warning("Failed to give pending prizes: " + e.getMessage());
        }
    }

    public void notifyPendingWins(Player player) {
        DatabaseManager.WinNotification notification = db.getWinNotification(player.getUniqueId().toString());
        if (notification == null) return;
        
        // Show congratulations message
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "════════════════════════════════════");
        if (notification.wasTie) {
            player.sendMessage(ChatColor.GREEN + "✦ CONGRATULATIONS! ✦");
            player.sendMessage(ChatColor.YELLOW + "You TIED in the " + notification.contestName + " contest!");
            player.sendMessage(ChatColor.YELLOW + "Score: " + ChatColor.WHITE + notification.score);
            player.sendMessage(ChatColor.GRAY + "Your prize is waiting in the chest at spawn!");
        } else {
            player.sendMessage(ChatColor.GREEN + "✦ CONGRATULATIONS! ✦");
            player.sendMessage(ChatColor.YELLOW + "You WON the " + notification.contestName + " contest!");
            player.sendMessage(ChatColor.YELLOW + "Score: " + ChatColor.WHITE + notification.score);
            player.sendMessage(ChatColor.GRAY + "Your prize has been added to your inventory!");
        }
        player.sendMessage(ChatColor.GOLD + "════════════════════════════════════");
        player.sendMessage("");
        
        // Remove notification from database
        db.removeWinNotification(player.getUniqueId().toString());
    }

    public void upgradePrize(Player player, String contestName) {
        Contest contest = contests.get(contestName);
        if (contest == null) {
            player.sendMessage("No such contest.");
            return;
        }
        List<UpgradeTier> upgrades = contest.getUpgrades();
        if (upgrades.isEmpty()) {
            player.sendMessage("No upgrades for this contest.");
            return;
        }
        for (UpgradeTier tier : upgrades) {
            String checkLore = tier.getCheckLore();
            int required = tier.getRequired();
            ItemStack give = tier.getGive();
            if (give == null) continue;
            int count = 0;
            List<Integer> slotsToRemove = new ArrayList<>();
            ItemStack[] inv = player.getInventory().getContents();
            for (int i = 0; i < inv.length; i++) {
                ItemStack item = inv[i];
                if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().stream().anyMatch(l -> l.contains(checkLore))) {
                    count += item.getAmount();
                    slotsToRemove.add(i);
                    if (count >= required) break;
                }
            }
            if (count >= required) {
                int toRemove = required;
                for (int slot : slotsToRemove) {
                    ItemStack item = player.getInventory().getItem(slot);
                    int amt = item.getAmount();
                    if (amt <= toRemove) {
                        player.getInventory().setItem(slot, null);
                        toRemove -= amt;
                    } else {
                        item.setAmount(amt - toRemove);
                        toRemove = 0;
                    }
                    if (toRemove == 0) break;
                }
                
                if (contestName.equals("cobble") && tier.getName().equals("tempvoucher")) {
                    DatabaseManager.FirstWinner firstWinner = db.getFirstWinner(contestName, "tempvoucher");
                    
                    if (firstWinner == null) {
                        // This player is the FIRST to trade 30 vouchers!
                        db.setFirstWinner(contestName, "tempvoucher", player.getUniqueId().toString(), player.getName());
                        giveItemSafely(player, give);
                        
                        // Broadcast announcement
                        Bukkit.broadcastMessage("");
                        Bukkit.broadcastMessage("HISTORIC MOMENT!");
                        Bukkit.broadcastMessage(player.getName() + " is the FIRST PLAYER to earn the Cobble Contest Copper Pickaxe!");
                        Bukkit.broadcastMessage("Voucher trading is now LOCKED until the pickaxe is configured.");
                        Bukkit.broadcastMessage("");
                        
                        player.sendMessage("Congratulations! You've made AZP history!");
                        player.sendMessage("Contact admins to name your pickaxe.");
                        
                        plugin.getLogger().info("COBBLE TEMPVOUCHER: First winner " + player.getName() + " - tempvoucher locked");
                        return;
                    } else {
                        // Someone already won - tempvoucher is locked
                        player.sendMessage("");
                        player.sendMessage("Voucher trading is currently locked.");
                        player.sendMessage("The first copper pickaxe is being configured by: " + firstWinner.playerName);
                        player.sendMessage("");
                        player.sendMessage("After the pickaxe is named, use /contest reimburse");
                        player.sendMessage("to trade your vouchers 1:1 for copper pickaxes.");
                        player.sendMessage("");
                        return;
                    }
                }
                
                giveItemSafely(player, give);
                player.sendMessage("Prize upgraded for " + contestName + "!");
                return;
            }
        }
        player.sendMessage("You must have the required prizes for an upgrade in " + contestName + ".");
    }

    public boolean isContestRunning() {
        return currentContest != null;
    }

    public Contest getCurrentContest() {
        return contests.get(currentContest);
    }

    public Map<String, Contest> getContests() {
        return contests;
    }

    public DatabaseManager getDb() {
        return db;
    }
    
    public ScoringEngine getScoringEngine() {
        return scoringEngine;
    }

    public Contest getContestByUpgradeCommand(String command) {
        for (Contest contest : contests.values()) {
            if (command.equalsIgnoreCase(contest.getUpgradeCommand())) {
                return contest;
            }
        }
        return null;
    }
    
    /**
     * Get all contests that have the specified upgrade command.
     * Used for smart upgrade detection.
     */
    public List<Contest> getAllContestsByUpgradeCommand(String command) {
        List<Contest> matches = new ArrayList<>();
        for (Contest contest : contests.values()) {
            if (command.equalsIgnoreCase(contest.getUpgradeCommand())) {
                matches.add(contest);
            }
        }
        return matches;
    }
    
    /**
     * Intelligently detect which contest prize the player has and upgrade it.
     * Checks inventory for matching lore from any contest with this upgrade command.
     */
    public void smartUpgradePrize(Player player, String upgradeCommand) {
        List<Contest> possibleContests = getAllContestsByUpgradeCommand(upgradeCommand);
        
        if (possibleContests.isEmpty()) {
            player.sendMessage("§cNo contests use this upgrade command.");
            return;
        }
        
        // Check each contest to see if player has items from it
        for (Contest contest : possibleContests) {
            if (contest.getUpgrades().isEmpty()) continue;
            
            // Check if player has any items with this contest's lore
            for (UpgradeTier tier : contest.getUpgrades()) {
                String checkLore = tier.getCheckLore();
                if (checkLore == null || checkLore.isEmpty()) continue;
                
                // Scan inventory for items with this lore
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                        for (String loreLine : item.getItemMeta().getLore()) {
                            if (loreLine.contains(checkLore)) {
                                // Found a match! Upgrade this contest's prize
                                upgradePrize(player, contest.getName());
                                return;
                            }
                        }
                    }
                }
            }
        }
        
        // No matching items found in inventory
        player.sendMessage("§cYou don't have any upgradeable contest prizes in your inventory.");
    }

    private void saveContestState() {
        if (currentContest == null) return;
        
        // Save contest metadata
        db.saveActiveContest(currentContest, startTime, endTime, oldTotal);
        
        // Save all participants
        Contest contest = getCurrentContest();
        for (UUID uuid : participants) {
            long savedScore = 0;
            if (startAmounts.containsKey(uuid)) {
                // Try to get current score for online players
                org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if (offlinePlayer.isOnline()) {
                    Player p = offlinePlayer.getPlayer();
                    if (contest.isRuleBased()) {
                        if (scoringEngine != null) {
                            savedScore = scoringEngine.getScore(uuid);
                        } else {
                            // Rule-based contest but scoring engine not ready - use saved score
                            savedScore = savedScores.getOrDefault(uuid, 0L);
                        }
                    } else {
                        savedScore = getPlayerStat(p, contest.getStatType(), contest.getStatKey()) - startAmounts.get(uuid);
                    }
                } else if (savedScores.containsKey(uuid)) {
                    savedScore = savedScores.get(uuid);
                }
                db.saveParticipant(uuid, startAmounts.get(uuid), savedScore);
            }
        }
        
        // Also save offline participants with saved scores
        for (UUID uuid : savedScores.keySet()) {
            if (!participants.contains(uuid)) {
                long startAmount = startAmounts.getOrDefault(uuid, 0L);
                db.saveParticipant(uuid, startAmount, savedScores.get(uuid));
            }
        }
    }

    public void checkAndResumeContest() {
        DatabaseManager.ActiveContestData activeData = db.getActiveContest();
        if (activeData == null) return;  // No active contest to resume
        
        long now = System.currentTimeMillis();
        
        // Check if contest already ended
        if (now >= activeData.endTime) {
            plugin.getLogger().info("Found expired contest '" + activeData.contestName + "' - clearing from database");
            db.clearActiveContest();
            return;
        }
        
        // Resume the contest
        Contest contest = contests.get(activeData.contestName);
        if (contest == null) {
            plugin.getLogger().warning("Contest '" + activeData.contestName + "' no longer exists in config - clearing");
            db.clearActiveContest();
            return;
        }
        
        plugin.getLogger().info("Resuming contest '" + activeData.contestName + "' after server restart...");
        
        // Restore contest state
        currentContest = activeData.contestName;
        startTime = activeData.startTime;
        endTime = activeData.endTime;
        oldTotal = activeData.oldTotal;
        
        // Restore participants
        Map<UUID, DatabaseManager.ParticipantData> participantData = db.getActiveParticipants();
        for (Map.Entry<UUID, DatabaseManager.ParticipantData> entry : participantData.entrySet()) {
            UUID uuid = entry.getKey();
            DatabaseManager.ParticipantData data = entry.getValue();
            
            startAmounts.put(uuid, data.startAmount);
            savedScores.put(uuid, data.savedScore);
            
            // Add online players back to participants
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.isOnline()) {
                participants.add(uuid);
            }
        }
        
        // Recreate scoreboard
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(contest.getScoreboardObjective(), "dummy", contest.getScoreboardDisplay());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        team = scoreboard.registerNewTeam(contest.getTeamName());
        team.setColor(ChatColor.valueOf(contest.getTeamColor()));
        
        // Restore scoreboard for online players
        for (UUID uuid : participants) {
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.isOnline()) {
                Player p = offlinePlayer.getPlayer();
                p.setScoreboard(scoreboard);
                team.addEntry(p.getName());
                p.addScoreboardTag("contest");
                
                // Update scoreboard with saved score
                long score = savedScores.getOrDefault(uuid, 0L);
                scoreboard.getObjective(contest.getScoreboardObjective()).getScore(p.getName()).setScore((int) score);
            }
        }
        
        // Calculate remaining time and reschedule tasks
        long remainingTime = endTime - now;
        long remainingTicks = remainingTime / 50;  // Convert ms to ticks
        
        // Broadcast resume message
        Bukkit.broadcastMessage(ChatColor.GOLD + "[CONTEST RESUMED] " + contest.getName() + " contest continuing after restart!");
        long remainingMinutes = remainingTime / (60 * 1000);
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Time remaining: " + remainingMinutes + " minutes");
        
        // Schedule countdown messages for remaining time
        scheduleRemainingCountdowns(contest, remainingTicks);
        
        // Scoreboard update task
        int updateTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (currentContest == null) return;
            Contest c = getCurrentContest();
            if (c == null) return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (participants.contains(player.getUniqueId())) {
                    long score;
                    if (c.isRuleBased() && scoringEngine != null) {
                        score = scoringEngine.getScore(player.getUniqueId());
                    } else {
                        score = getPlayerStat(player, c.getStatType(), c.getStatKey()) - startAmounts.get(player.getUniqueId());
                    }
                    scoreboard.getObjective(c.getScoreboardObjective()).getScore(player.getName()).setScore((int) score);
                }
            }
        }, 2L, 2L).getTaskId();
        scheduledTasks.add(updateTaskId);
        
        // Area check task
        int areaCheckTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (currentContest == null) return;
            checkPlayerAreas();
        }, 40L, 40L).getTaskId();
        scheduledTasks.add(areaCheckTaskId);
        
        // State saving task
        int saveTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (currentContest == null) return;
            saveContestState();
        }, 1200L, 1200L).getTaskId();
        scheduledTasks.add(saveTaskId);
        
        // Contest end task
        int endTaskId = Bukkit.getScheduler().runTaskLater(plugin, this::endContest, remainingTicks).getTaskId();
        scheduledTasks.add(endTaskId);
        
        plugin.getLogger().info("Contest '" + activeData.contestName + "' successfully resumed!");
    }

    private void scheduleRemainingCountdowns(Contest contest, long remainingTicks) {
        long countdownInterval = contest.getCountdownIntervalMinutes() * 60L * 20L;
        long currentTick = 0;
        
        while (currentTick < remainingTicks) {
            long ticksLeft = remainingTicks - currentTick;
            int minutesLeft = (int) (ticksLeft / (60L * 20L));
            
            if (minutesLeft > 0 && minutesLeft <= contest.getDurationMinutes()) {
                final int finalMinutes = minutesLeft;
                int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (currentContest == null) return;
                    sendCountdown(finalMinutes);
                    checkPlayerAreas();
                }, currentTick).getTaskId();
                scheduledTasks.add(taskId);
            }
            
            currentTick += countdownInterval;
        }
    }

    public void saveContestStateOnShutdown() {
        // Called when server is shutting down - save state but don't end contest
        plugin.getLogger().info("Saving contest state for resumption after restart...");
        saveContestState();
    }
    
    public void shutdown() {
        // Cancel auto-save and cleanup tasks
        if (autoSaveTaskId != -1) {
            Bukkit.getScheduler().cancelTask(autoSaveTaskId);
        }
        if (cleanupTaskId != -1) {
            Bukkit.getScheduler().cancelTask(cleanupTaskId);
        }
        
        // Save any current contest attribution data
        if (currentContest != null) {
            try {
                attributionTracker.saveToDatabase(currentContest);
                plugin.getLogger().info("Saved attribution data on shutdown for: " + currentContest);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save attribution data on shutdown: " + e.getMessage());
            }
        }
    }
    
    public JavaPlugin getPlugin() {
        return plugin;
    }
    
    public static boolean isDebugMode() {
        return debugMode;
    }
    
    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
    }
}
