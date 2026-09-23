package com.xai.contestplugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a command action to execute when a scoring rule triggers.
 * Supports variable substitution and flexible execution types.
 */
public class CommandAction {
    private final CommandExecutorType executorType;
    private final String command;
    private final Map<String, String> variables;
    private final int delayTicks;
    private final double chance;
    private final String condition;
    
    private CommandAction(Builder builder) {
        this.executorType = builder.executorType;
        this.command = builder.command;
        this.variables = builder.variables;
        this.delayTicks = builder.delayTicks;
        this.chance = builder.chance;
        this.condition = builder.condition;
    }
    
    // Getters
    public CommandExecutorType getExecutorType() {
        return executorType;
    }
    
    public String getCommand() {
        return command;
    }
    
    public Map<String, String> getVariables() {
        return variables;
    }
    
    public int getDelayTicks() {
        return delayTicks;
    }
    
    public double getChance() {
        return chance;
    }
    
    public String getCondition() {
        return condition;
    }
    
    /**
     * Replace variables in the command string.
     * Variables are in format {variable_name}.
     * 
     * Available default variables:
     * - {player} - Player name
     * - {player_uuid} - Player UUID
     * - {score} - Current contest score
     * - {points} - Points awarded this action
     * - {total_points} - Total points this contest
     * - {position} - Current leaderboard position
     * - {world} - Player's current world
     * - {x} {y} {z} - Player coordinates
     * - {yaw} {pitch} - Player rotation
     * - {health} - Player health
     * - {hunger} - Player hunger
     * - {level} - Player level
     * - {exp} - Player experience
     * - {gamemode} - Player gamemode
     * - {time} - Current world time
     * - {weather} - Current weather
     * - {biome} - Current biome
     * - {contest} - Contest name
     * - {contest_time_left} - Time remaining in contest
     * - {material} - Block/item material (if applicable)
     * - {entity} - Entity type (if applicable)
     * - {entity_name} - Entity custom name (if applicable)
     * - {damage} - Damage dealt/received (if applicable)
     * - {amount} - Item amount (if applicable)
     * - {chain} - Current chain count (if applicable)
     * - {streak} - Current streak count (if applicable)
     * 
     * @param variables Map of variable names to values
     * @return Command string with variables replaced
     */
    public String replaceVariables(Map<String, String> variables) {
        String result = command;
        
        // Replace built-in variables
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        // Replace custom variables from builder
        for (Map.Entry<String, String> entry : this.variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return result;
    }
    
    /**
     * Builder pattern for creating CommandAction instances.
     */
    public static class Builder {
        private CommandExecutorType executorType = CommandExecutorType.CONSOLE;
        private String command = "";
        private Map<String, String> variables = new HashMap<>();
        private int delayTicks = 0;
        private double chance = 1.0;
        private String condition = null;
        
        public Builder executor(CommandExecutorType type) {
            this.executorType = type;
            return this;
        }
        
        public Builder executor(String typeName) {
            this.executorType = CommandExecutorType.valueOf(typeName.toUpperCase());
            return this;
        }
        
        public Builder command(String command) {
            this.command = command;
            return this;
        }
        
        public Builder variable(String key, String value) {
            this.variables.put(key, value);
            return this;
        }
        
        public Builder variables(Map<String, String> variables) {
            this.variables.putAll(variables);
            return this;
        }
        
        public Builder delay(int ticks) {
            this.delayTicks = ticks;
            return this;
        }
        
        public Builder delaySeconds(int seconds) {
            this.delayTicks = seconds * 20;
            return this;
        }
        
        public Builder chance(double chance) {
            this.chance = chance;
            return this;
        }
        
        public Builder condition(String condition) {
            this.condition = condition;
            return this;
        }
        
        public CommandAction build() {
            if (command == null || command.isEmpty()) {
                throw new IllegalStateException("Command must be specified");
            }
            return new CommandAction(this);
        }
    }
    
    @Override
    public String toString() {
        return "CommandAction{type=" + executorType + ", command='" + command + "'}";
    }
}
