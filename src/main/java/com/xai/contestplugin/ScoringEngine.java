package com.xai.contestplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core scoring engine that manages all player scores, chains, combos, and cooldowns.
 * This is the heart of the event-driven scoring system.
 */
public class ScoringEngine {
    
    private final JavaPlugin plugin;
    private final Contest contest;
    private final ConditionEvaluator conditionEvaluator;
    private final FormulaEvaluator formulaEvaluator;
    private final CommandExecutorService commandExecutor;
    
    // Score tracking
    private final Map<UUID, Integer> playerScores = new ConcurrentHashMap<>();
    
    // Chain tracking (consecutive triggers)
    private final Map<UUID, Map<ScoringRule, Integer>> chainCounts = new ConcurrentHashMap<>();
    private final Map<UUID, Map<ScoringRule, Long>> chainTimestamps = new ConcurrentHashMap<>();
    
    // Streak tracking (same material/entity)
    private final Map<UUID, Map<ScoringRule, Integer>> streakCounts = new ConcurrentHashMap<>();
    private final Map<UUID, Map<ScoringRule, String>> streakMaterials = new ConcurrentHashMap<>();
    
    // Cooldown tracking
    private final Map<UUID, Map<ScoringRule, Long>> cooldownTimestamps = new ConcurrentHashMap<>();
    
    // Trigger count tracking (for max_triggers)
    private final Map<UUID, Map<ScoringRule, Integer>> triggerCounts = new ConcurrentHashMap<>();
    
    // Active participants
    private final Set<UUID> participants = new HashSet<>();
    
    public ScoringEngine(JavaPlugin plugin, Contest contest, CommandExecutorService commandExecutor) {
        this.plugin = plugin;
        this.contest = contest;
        this.conditionEvaluator = new ConditionEvaluator(plugin);
        this.formulaEvaluator = new FormulaEvaluator();
        this.commandExecutor = commandExecutor;
    }
    
    /**
     * Add a player to the contest.
     */
    public void addParticipant(UUID playerId) {
        participants.add(playerId);
        playerScores.putIfAbsent(playerId, 0);
    }
    
    /**
     * Remove a player from the contest.
     */
    public void removeParticipant(UUID playerId) {
        participants.remove(playerId);
    }
    
    /**
     * Clear a player's score completely (for score reset on voluntary leave).
     */
    public void clearScore(UUID playerId) {
        playerScores.remove(playerId);
        cooldownTimestamps.remove(playerId);
        triggerCounts.remove(playerId);
    }
    
    /**
     * Check if player is participating.
     */
    public boolean isParticipant(UUID playerId) {
        return participants.contains(playerId);
    }
    
    /**
     * Process an event and award points if rules match.
     */
    public void processEvent(EventType eventType, Event event, Player player) {
        if (ContestManager.isDebugMode()) {
            plugin.getLogger().info("processEvent called: " + eventType + " for player " + player.getName());
        }
        
        if (!isParticipant(player.getUniqueId())) {
            if (ContestManager.isDebugMode()) {
                plugin.getLogger().warning("Player " + player.getName() + " is not a participant!");
            }
            return;
        }
        
        List<ScoringRule> rules = contest.getScoringRules();
        if (rules == null || rules.isEmpty()) {
            if (ContestManager.isDebugMode()) {
                plugin.getLogger().warning("No scoring rules found!");
            }
            return;
        }
        
        if (ContestManager.isDebugMode()) {
            plugin.getLogger().info("Checking " + rules.size() + " rules for event: " + eventType);
        }
        
        // Check each rule
        for (ScoringRule rule : rules) {
            if (rule.getEvent() != eventType) {
                continue; // Wrong event type
            }
            
            if (ContestManager.isDebugMode()) {
                plugin.getLogger().info("Rule matched event type: " + eventType);
            }
            
            // Check cooldown
            if (isOnCooldown(player.getUniqueId(), rule)) {
                if (ContestManager.isDebugMode()) {
                    plugin.getLogger().info("Player on cooldown for this rule");
                }
                continue;
            }
            
            // Check max triggers
            if (hasReachedMaxTriggers(player.getUniqueId(), rule)) {
                if (ContestManager.isDebugMode()) {
                    plugin.getLogger().info("Player reached max triggers for this rule");
                }
                continue;
            }
            
            // Check probability
            if (rule.getChance() < 1.0 && Math.random() >= rule.getChance()) {
                if (ContestManager.isDebugMode()) {
                    plugin.getLogger().info("Failed probability check");
                }
                continue;
            }
            
            // Evaluate conditions
            if (ContestManager.isDebugMode()) {
                plugin.getLogger().info("Evaluating conditions for rule...");
            }
            if (!conditionEvaluator.evaluate(rule.getConditions(), event, player, contest)) {
                if (ContestManager.isDebugMode()) {
                    plugin.getLogger().warning("Conditions failed!");
                }
                // Conditions failed - execute onFail commands if any
                if (rule.getOnFail() != null && !rule.getOnFail().isEmpty()) {
                    executeCommands(rule.getOnFail(), player, rule, 0);
                }
                continue;
            }
            
            if (ContestManager.isDebugMode()) {
                plugin.getLogger().info("All checks passed! Awarding points...");
            }
            // All checks passed - award points!
            awardPoints(player, rule, event);
        }
    }
    
    /**
     * Award points to a player for a rule trigger.
     */
    private void awardPoints(Player player, ScoringRule rule, Event event) {
        UUID playerId = player.getUniqueId();
        
        // Calculate base points
        int basePoints = rule.getBasePoints();
        
        // Get current stats for formula/messages
        int currentScore = playerScores.getOrDefault(playerId, 0);
        int chain = getChainCount(playerId, rule);
        int streak = getStreakCount(playerId, rule);
        
        // Apply chain multiplier
        if (rule.isChainMultiplier() && chain > 0) {
            basePoints = basePoints * (chain + 1);
        }
        
        // Apply streak bonus
        if (rule.getStreakBonus() > 0 && streak > 0) {
            basePoints += rule.getStreakBonus() * streak;
        }
        
        // Apply formula if present
        int finalPoints = basePoints;
        if (rule.getFormula() != null && !rule.getFormula().isEmpty()) {
            Map<String, String> variables = formulaEvaluator.buildVariables(
                player, event, basePoints, chain, streak, currentScore
            );
            finalPoints = formulaEvaluator.evaluate(rule.getFormula(), variables);
        }
        
        // Update score
        int newScore = currentScore + finalPoints;
        playerScores.put(playerId, newScore);
        
        // Update chain
        updateChain(playerId, rule);
        
        // Update streak
        updateStreak(playerId, rule, event);
        
        // Set cooldown
        if (rule.getCooldownSeconds() > 0) {
            setCooldown(playerId, rule);
        }
        
        // Increment trigger count
        incrementTriggerCount(playerId, rule);
        
        // Send message
        if (rule.getMessage() != null || rule.getMessageSuccess() != null) {
            String message = rule.getMessageSuccess() != null ? rule.getMessageSuccess() : rule.getMessage();
            if (message != null) {
                Map<String, String> vars = buildMessageVariables(player, finalPoints, newScore, chain, streak, event);
                String formatted = replaceVariables(message, vars);
                player.sendMessage(formatted);
            }
        }
        
        // Execute success commands
        if (rule.getOnSuccess() != null && !rule.getOnSuccess().isEmpty()) {
            executeCommands(rule.getOnSuccess(), player, rule, finalPoints);
        }
        
        // Log scoring event
        plugin.getLogger().fine(String.format(
            "Player %s scored %d points (rule: %s, total: %d)",
            player.getName(), finalPoints, rule.getEvent(), newScore
        ));
    }
    
    /**
     * Execute a list of command actions.
     */
    private void executeCommands(List<CommandAction> commands, Player player, ScoringRule rule, int points) {
        int currentScore = playerScores.getOrDefault(player.getUniqueId(), 0);
        int chain = getChainCount(player.getUniqueId(), rule);
        int streak = getStreakCount(player.getUniqueId(), rule);
        
        Map<String, String> variables = buildMessageVariables(player, points, currentScore, chain, streak, null);
        
        for (CommandAction action : commands) {
            commandExecutor.execute(action, player, variables);
        }
    }
    
    /**
     * Build variables for messages and commands.
     */
    private Map<String, String> buildMessageVariables(Player player, int points, int score, 
                                                       int chain, int streak, Event event) {
        Map<String, String> vars = new HashMap<>();
        
        // Player info
        vars.put("player", player.getName());
        vars.put("player_uuid", player.getUniqueId().toString());
        vars.put("player_displayname", player.getDisplayName());
        
        // Score info
        vars.put("points", String.valueOf(points));
        vars.put("score", String.valueOf(score));
        vars.put("chain", String.valueOf(chain));
        vars.put("streak", String.valueOf(streak));
        
        // Player stats
        vars.put("health", String.valueOf((int) player.getHealth()));
        vars.put("max_health", String.valueOf((int) player.getMaxHealth()));
        vars.put("hunger", String.valueOf(player.getFoodLevel()));
        vars.put("level", String.valueOf(player.getLevel()));
        vars.put("exp", String.valueOf(player.getTotalExperience()));
        vars.put("gamemode", player.getGameMode().name());
        
        // Location
        vars.put("world", player.getWorld().getName());
        vars.put("x", String.valueOf(player.getLocation().getBlockX()));
        vars.put("y", String.valueOf(player.getLocation().getBlockY()));
        vars.put("z", String.valueOf(player.getLocation().getBlockZ()));
        vars.put("biome", player.getLocation().getBlock().getBiome().name());
        
        // Contest info
        vars.put("contest", contest.getName());
        
        // Time info
        long time = player.getWorld().getTime();
        String timeOfDay = (time >= 0 && time < 12000) ? "Day" : "Night";
        vars.put("time", String.valueOf(time));
        vars.put("time_formatted", timeOfDay);
        
        // Weather
        String weather = "Clear";
        if (player.getWorld().hasStorm()) {
            weather = player.getWorld().isThundering() ? "Thunder" : "Rain";
        }
        vars.put("weather", weather);
        
        // Event-specific variables would be added here based on event type
        // (material, entity, damage, etc.)
        
        return vars;
    }
    
    /**
     * Replace variables in a string.
     */
    private String replaceVariables(String text, Map<String, String> variables) {
        String result = text;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        // Apply color codes
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', result);
    }
    
    // ==================== CHAIN MANAGEMENT ====================
    
    private int getChainCount(UUID playerId, ScoringRule rule) {
        return chainCounts.getOrDefault(playerId, new HashMap<>()).getOrDefault(rule, 0);
    }
    
    private void updateChain(UUID playerId, ScoringRule rule) {
        // Check if chain has timed out
        long currentTime = System.currentTimeMillis();
        Map<ScoringRule, Long> playerChainTimes = chainTimestamps.computeIfAbsent(playerId, k -> new HashMap<>());
        Long lastTime = playerChainTimes.get(rule);
        
        if (lastTime != null) {
            int timeoutMs = rule.getChainTimeoutSeconds() * 1000;
            if (currentTime - lastTime > timeoutMs) {
                // Chain timed out - reset
                chainCounts.computeIfAbsent(playerId, k -> new HashMap<>()).put(rule, 1);
                playerChainTimes.put(rule, currentTime);
                return;
            }
        }
        
        // Increment chain
        Map<ScoringRule, Integer> playerChains = chainCounts.computeIfAbsent(playerId, k -> new HashMap<>());
        int newChain = playerChains.getOrDefault(rule, 0) + 1;
        playerChains.put(rule, newChain);
        playerChainTimes.put(rule, currentTime);
    }
    
    private void resetChain(UUID playerId, ScoringRule rule) {
        chainCounts.computeIfAbsent(playerId, k -> new HashMap<>()).put(rule, 0);
    }
    
    // ==================== STREAK MANAGEMENT ====================
    
    private int getStreakCount(UUID playerId, ScoringRule rule) {
        return streakCounts.getOrDefault(playerId, new HashMap<>()).getOrDefault(rule, 0);
    }
    
    private void updateStreak(UUID playerId, ScoringRule rule, Event event) {
        // Extract material/entity from event for streak tracking
        String currentMaterial = extractMaterialOrEntity(event);
        
        if (currentMaterial == null) {
            return; // Can't track streak without material/entity
        }
        
        Map<ScoringRule, String> playerStreakMats = streakMaterials.computeIfAbsent(playerId, k -> new HashMap<>());
        String lastMaterial = playerStreakMats.get(rule);
        
        if (lastMaterial == null || lastMaterial.equals(currentMaterial)) {
            // Continue or start streak
            Map<ScoringRule, Integer> playerStreaks = streakCounts.computeIfAbsent(playerId, k -> new HashMap<>());
            int newStreak = playerStreaks.getOrDefault(rule, 0) + 1;
            playerStreaks.put(rule, newStreak);
            playerStreakMats.put(rule, currentMaterial);
        } else {
            // Different material - reset streak
            streakCounts.computeIfAbsent(playerId, k -> new HashMap<>()).put(rule, 1);
            playerStreakMats.put(rule, currentMaterial);
        }
    }
    
    private String extractMaterialOrEntity(Event event) {
        if (event instanceof org.bukkit.event.block.BlockEvent) {
            return ((org.bukkit.event.block.BlockEvent) event).getBlock().getType().name();
        } else if (event instanceof org.bukkit.event.entity.EntityEvent) {
            return ((org.bukkit.event.entity.EntityEvent) event).getEntity().getType().name();
        }
        return null;
    }
    
    // ==================== COOLDOWN MANAGEMENT ====================
    
    private boolean isOnCooldown(UUID playerId, ScoringRule rule) {
        if (rule.getCooldownSeconds() <= 0) {
            return false;
        }
        
        Map<ScoringRule, Long> playerCooldowns = cooldownTimestamps.get(playerId);
        if (playerCooldowns == null) {
            return false;
        }
        
        Long lastTrigger = playerCooldowns.get(rule);
        if (lastTrigger == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        int cooldownMs = rule.getCooldownSeconds() * 1000;
        return (currentTime - lastTrigger) < cooldownMs;
    }
    
    private void setCooldown(UUID playerId, ScoringRule rule) {
        cooldownTimestamps.computeIfAbsent(playerId, k -> new HashMap<>())
                         .put(rule, System.currentTimeMillis());
    }
    
    // ==================== TRIGGER COUNT MANAGEMENT ====================
    
    private boolean hasReachedMaxTriggers(UUID playerId, ScoringRule rule) {
        if (rule.getMaxTriggers() < 0) {
            return false; // No limit
        }
        
        Map<ScoringRule, Integer> playerTriggers = triggerCounts.get(playerId);
        if (playerTriggers == null) {
            return false;
        }
        
        Integer count = playerTriggers.get(rule);
        return count != null && count >= rule.getMaxTriggers();
    }
    
    private void incrementTriggerCount(UUID playerId, ScoringRule rule) {
        triggerCounts.computeIfAbsent(playerId, k -> new HashMap<>())
                    .merge(rule, 1, Integer::sum);
    }
    
    // ==================== SCORE GETTERS ====================
    
    public int getScore(UUID playerId) {
        return playerScores.getOrDefault(playerId, 0);
    }
    
    public void setScore(UUID playerId, int score) {
        playerScores.put(playerId, score);
    }
    
    public Map<UUID, Integer> getAllScores() {
        return new HashMap<>(playerScores);
    }
    
    public Set<UUID> getParticipants() {
        return new HashSet<>(participants);
    }
    
    // ==================== CLEANUP ====================
    
    public void clearAllData() {
        playerScores.clear();
        chainCounts.clear();
        chainTimestamps.clear();
        streakCounts.clear();
        streakMaterials.clear();
        cooldownTimestamps.clear();
        triggerCounts.clear();
        participants.clear();
        conditionEvaluator.clearBlockTracking();
    }
    
    // ==================== BLOCK TRACKING (for self-placed checks) ====================
    
    public void trackBlockPlace(org.bukkit.Location location, UUID playerId) {
        conditionEvaluator.trackBlockPlace(location, playerId);
    }
    
    // ==================== PERIODIC EVENTS ====================
    
    /**
     * Start periodic event timers (EVERY_SECOND, EVERY_MINUTE, PERIODIC).
     */
    public void startPeriodicTimers() {
        List<ScoringRule> rules = contest.getScoringRules();
        if (rules == null) return;
        
        for (ScoringRule rule : rules) {
            if (rule.getEvent().isTimeBasedEvent()) {
                startPeriodicTimer(rule);
            }
        }
    }
    
    private void startPeriodicTimer(ScoringRule rule) {
        long intervalTicks;
        
        switch (rule.getEvent()) {
            case EVERY_SECOND:
                intervalTicks = 20L; // 1 second
                break;
            case EVERY_MINUTE:
                intervalTicks = 1200L; // 60 seconds
                break;
            case PERIODIC:
                intervalTicks = rule.getCooldownSeconds() * 20L;
                break;
            default:
                return;
        }
        
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID playerId : new HashSet<>(participants)) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    processEvent(rule.getEvent(), null, player);
                }
            }
        }, intervalTicks, intervalTicks);
    }
}
