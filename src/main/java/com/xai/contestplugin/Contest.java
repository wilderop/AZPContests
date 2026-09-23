package com.xai.contestplugin;

import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Contest {
    private String name;
    private int chance;
    private String description;  // Custom description for "WHAT TO DO" section
    
    // Legacy statistic-based scoring (backward compatibility)
    private Statistic statType;
    private Object statKey; // Material or EntityType
    
    // New event-driven scoring system
    private List<ScoringRule> scoringRules;
    private String contestType; // "statistic" or "rule_based"
    
    private int durationMinutes;
    private int warningMinutes;
    private int countdownInterval;
    private World areaWorld;
    private int minX, maxX, minY, maxY, minZ, maxZ;
    private Map<String, String> messages;
    private long minTotal;
    private boolean doublePrevious;
    private List<String> onMetCommands;
    private List<String> onNotMetCommands;
    private ItemStack winnerPrize;
    private int winnerPrizeAmount;  // Number of winner prizes to give (default 1)
    private Location tieChestLocation;
    private String scoreboardObjective;
    private String scoreboardDisplay;
    private String teamName;
    private String teamColor;
    private String upgradeCommand;  // Custom command for upgrading (e.g., "tradepick")
    private List<UpgradeTier> upgrades;
    private int startMinute;  // Minute within hour to start (0-59), default 0

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getChance() {
        return chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Statistic getStatType() {
        return statType;
    }

    public void setStatType(Statistic statType) {
        this.statType = statType;
    }

    public Object getStatKey() {
        return statKey;
    }

    public void setStatKey(Object statKey) {
        this.statKey = statKey;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getWarningMinutes() {
        return warningMinutes;
    }

    public void setWarningMinutes(int warningMinutes) {
        this.warningMinutes = warningMinutes;
    }

    public int getCountdownInterval() {
        return countdownInterval;
    }

    public void setCountdownInterval(int countdownInterval) {
        this.countdownInterval = countdownInterval;
    }

    public int getCountdownIntervalMinutes() {
        return countdownInterval;  // Already in minutes
    }

    public World getAreaWorld() {
        return areaWorld;
    }

    public void setAreaWorld(World areaWorld) {
        this.areaWorld = areaWorld;
    }

    public int getMinX() {
        return minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public int getMinZ() {
        return minZ;
    }

    public void setMinZ(int minZ) {
        this.minZ = minZ;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public void setMaxZ(int maxZ) {
        this.maxZ = maxZ;
    }

    public Map<String, String> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, String> messages) {
        this.messages = messages;
    }

    public long getMinTotal() {
        return minTotal;
    }

    public void setMinTotal(long minTotal) {
        this.minTotal = minTotal;
    }

    public boolean isDoublePrevious() {
        return doublePrevious;
    }

    public void setDoublePrevious(boolean doublePrevious) {
        this.doublePrevious = doublePrevious;
    }

    public List<String> getOnMetCommands() {
        return onMetCommands;
    }

    public void setOnMetCommands(List<String> onMetCommands) {
        this.onMetCommands = onMetCommands;
    }

    public List<String> getOnNotMetCommands() {
        return onNotMetCommands;
    }

    public void setOnNotMetCommands(List<String> onNotMetCommands) {
        this.onNotMetCommands = onNotMetCommands;
    }

    public ItemStack getWinnerPrize() {
        return winnerPrize;
    }

    public void setWinnerPrize(ItemStack winnerPrize) {
        this.winnerPrize = winnerPrize;
    }

    public int getWinnerPrizeAmount() {
        return winnerPrizeAmount;
    }

    public void setWinnerPrizeAmount(int winnerPrizeAmount) {
        this.winnerPrizeAmount = winnerPrizeAmount;
    }

    public Location getTieChestLocation() {
        return tieChestLocation;
    }

    public void setTieChestLocation(Location tieChestLocation) {
        this.tieChestLocation = tieChestLocation;
    }

    public String getScoreboardObjective() {
        return scoreboardObjective;
    }

    public void setScoreboardObjective(String scoreboardObjective) {
        this.scoreboardObjective = scoreboardObjective;
    }

    public String getScoreboardDisplay() {
        return scoreboardDisplay;
    }

    public void setScoreboardDisplay(String scoreboardDisplay) {
        this.scoreboardDisplay = scoreboardDisplay;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(String teamColor) {
        this.teamColor = teamColor;
    }

    public String getUpgradeCommand() {
        return upgradeCommand;
    }

    public void setUpgradeCommand(String upgradeCommand) {
        this.upgradeCommand = upgradeCommand;
    }

    public List<UpgradeTier> getUpgrades() {
        return upgrades;
    }

    public void setUpgrades(List<UpgradeTier> upgrades) {
        this.upgrades = upgrades;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }
    
    // New event-driven scoring system
    
    public List<ScoringRule> getScoringRules() {
        return scoringRules;
    }
    
    public void setScoringRules(List<ScoringRule> scoringRules) {
        this.scoringRules = scoringRules;
    }
    
    public String getContestType() {
        return contestType;
    }
    
    public void setContestType(String contestType) {
        this.contestType = contestType;
    }
    
    /**
     * Check if this contest uses the new rule-based scoring system.
     */
    public boolean isRuleBased() {
        return "rule_based".equals(contestType) || (scoringRules != null && !scoringRules.isEmpty());
    }
    
    /**
     * Check if this contest uses the legacy statistic-based scoring.
     */
    public boolean isStatisticBased() {
        return statType != null && !isRuleBased();
    }
}
