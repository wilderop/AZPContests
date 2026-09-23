package com.xai.contestplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single scoring rule for a contest.
 * Rules define when and how many points to award based on events and conditions.
 */
public class ScoringRule {
    private final EventType event;
    private final Map<String, Object> conditions;
    private final int basePoints;
    private final String formula;
    private final String message;
    private final String messageSuccess;
    private final String messageFail;
    private final int cooldownSeconds;
    private final boolean chainMultiplier;
    private final int chainTimeoutSeconds;
    private final int streakBonus;
    private final double chance;
    private final int maxTriggers;
    private final boolean negateIfSelfPlaced;
    private final Map<String, Object> metadata;
    
    // Command actions to run when rule triggers
    private final java.util.List<CommandAction> onSuccess;
    private final java.util.List<CommandAction> onFail;
    
    private ScoringRule(Builder builder) {
        this.event = builder.event;
        this.conditions = builder.conditions;
        this.basePoints = builder.basePoints;
        this.formula = builder.formula;
        this.message = builder.message;
        this.messageSuccess = builder.messageSuccess;
        this.messageFail = builder.messageFail;
        this.cooldownSeconds = builder.cooldownSeconds;
        this.chainMultiplier = builder.chainMultiplier;
        this.chainTimeoutSeconds = builder.chainTimeoutSeconds;
        this.streakBonus = builder.streakBonus;
        this.chance = builder.chance;
        this.maxTriggers = builder.maxTriggers;
        this.negateIfSelfPlaced = builder.negateIfSelfPlaced;
        this.metadata = builder.metadata;
        this.onSuccess = builder.onSuccess;
        this.onFail = builder.onFail;
    }
    
    // Getters
    public EventType getEvent() {
        return event;
    }
    
    public Map<String, Object> getConditions() {
        return conditions;
    }
    
    public int getBasePoints() {
        return basePoints;
    }
    
    public String getFormula() {
        return formula;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getMessageSuccess() {
        return messageSuccess;
    }
    
    public String getMessageFail() {
        return messageFail;
    }
    
    public int getCooldownSeconds() {
        return cooldownSeconds;
    }
    
    public boolean isChainMultiplier() {
        return chainMultiplier;
    }
    
    public int getChainTimeoutSeconds() {
        return chainTimeoutSeconds;
    }
    
    public int getStreakBonus() {
        return streakBonus;
    }
    
    public double getChance() {
        return chance;
    }
    
    public int getMaxTriggers() {
        return maxTriggers;
    }
    
    public boolean isNegateIfSelfPlaced() {
        return negateIfSelfPlaced;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public java.util.List<CommandAction> getOnSuccess() {
        return onSuccess;
    }
    
    public java.util.List<CommandAction> getOnFail() {
        return onFail;
    }
    
    /**
     * Builder pattern for creating ScoringRule instances.
     */
    public static class Builder {
        private EventType event;
        private Map<String, Object> conditions = new HashMap<>();
        private int basePoints = 1;
        private String formula = null;
        private String message = null;
        private String messageSuccess = null;
        private String messageFail = null;
        private int cooldownSeconds = 0;
        private boolean chainMultiplier = false;
        private int chainTimeoutSeconds = 5;
        private int streakBonus = 0;
        private double chance = 1.0;
        private int maxTriggers = -1; // -1 = unlimited
        private boolean negateIfSelfPlaced = false;
        private Map<String, Object> metadata = new HashMap<>();
        private java.util.List<CommandAction> onSuccess = new java.util.ArrayList<>();
        private java.util.List<CommandAction> onFail = new java.util.ArrayList<>();
        
        public Builder event(EventType event) {
            this.event = event;
            return this;
        }
        
        public Builder event(String eventName) {
            this.event = EventType.valueOf(eventName.toUpperCase());
            return this;
        }
        
        public Builder conditions(Map<String, Object> conditions) {
            this.conditions = conditions;
            return this;
        }
        
        public Builder condition(String key, Object value) {
            this.conditions.put(key, value);
            return this;
        }
        
        public Builder basePoints(int points) {
            this.basePoints = points;
            return this;
        }
        
        public Builder formula(String formula) {
            this.formula = formula;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder messageSuccess(String message) {
            this.messageSuccess = message;
            return this;
        }
        
        public Builder messageFail(String message) {
            this.messageFail = message;
            return this;
        }
        
        public Builder cooldownSeconds(int seconds) {
            this.cooldownSeconds = seconds;
            return this;
        }
        
        public Builder chainMultiplier(boolean enabled) {
            this.chainMultiplier = enabled;
            return this;
        }
        
        public Builder chainTimeoutSeconds(int seconds) {
            this.chainTimeoutSeconds = seconds;
            return this;
        }
        
        public Builder streakBonus(int bonus) {
            this.streakBonus = bonus;
            return this;
        }
        
        public Builder chance(double chance) {
            this.chance = chance;
            return this;
        }
        
        public Builder maxTriggers(int max) {
            this.maxTriggers = max;
            return this;
        }
        
        public Builder negateIfSelfPlaced(boolean negate) {
            this.negateIfSelfPlaced = negate;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder addOnSuccess(CommandAction action) {
            this.onSuccess.add(action);
            return this;
        }
        
        public Builder addOnFail(CommandAction action) {
            this.onFail.add(action);
            return this;
        }
        
        public ScoringRule build() {
            if (event == null) {
                throw new IllegalStateException("Event type must be specified");
            }
            return new ScoringRule(this);
        }
    }
    
    @Override
    public String toString() {
        return "ScoringRule{event=" + event + ", basePoints=" + basePoints + ", conditions=" + conditions.size() + "}";
    }
}
