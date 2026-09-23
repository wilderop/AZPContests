package com.xai.contestplugin;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses scoring rules from YAML configuration into ScoringRule objects.
 */
public class RuleParser {
    
    /**
     * Parse a list of scoring rules from a configuration section.
     */
    public static List<ScoringRule> parseRules(ConfigurationSection rulesSection) {
        List<ScoringRule> rules = new ArrayList<>();
        
        if (rulesSection == null) {
            return rules;
        }
        
        List<Map<?, ?>> rulesList = (List<Map<?, ?>>) rulesSection.getList("scoring_rules");
        if (rulesList == null || rulesList.isEmpty()) {
            return rules;
        }
        
        for (Map<?, ?> ruleMap : rulesList) {
            try {
                ScoringRule rule = parseRule(ruleMap);
                rules.add(rule);
            } catch (Exception e) {
                System.err.println("Error parsing scoring rule: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return rules;
    }
    
    /**
     * Parse a single scoring rule from a map.
     */
    private static ScoringRule parseRule(Map<?, ?> ruleMap) {
        ScoringRule.Builder builder = new ScoringRule.Builder();
        
        // Event type (required)
        String eventStr = (String) ruleMap.get("event");
        if (eventStr == null) {
            throw new IllegalArgumentException("Scoring rule missing required 'event' field");
        }
        builder.event(eventStr);
        
        // Conditions (optional)
        Object conditionsObj = ruleMap.get("conditions");
        if (conditionsObj instanceof Map) {
            Map<String, Object> conditions = new HashMap<>();
            Map<?, ?> condMap = (Map<?, ?>) conditionsObj;
            for (Map.Entry<?, ?> entry : condMap.entrySet()) {
                conditions.put(entry.getKey().toString(), entry.getValue());
            }
            builder.conditions(conditions);
        }
        
        // Base points (optional, default 1)
        if (ruleMap.containsKey("points")) {
            builder.basePoints(((Number) ruleMap.get("points")).intValue());
        }
        
        // Formula (optional)
        if (ruleMap.containsKey("formula")) {
            builder.formula((String) ruleMap.get("formula"));
        }
        
        // Messages (optional)
        if (ruleMap.containsKey("message")) {
            builder.message((String) ruleMap.get("message"));
        }
        if (ruleMap.containsKey("message_success")) {
            builder.messageSuccess((String) ruleMap.get("message_success"));
        }
        if (ruleMap.containsKey("message_fail")) {
            builder.messageFail((String) ruleMap.get("message_fail"));
        }
        
        // Cooldown (optional)
        if (ruleMap.containsKey("cooldown_seconds")) {
            builder.cooldownSeconds(((Number) ruleMap.get("cooldown_seconds")).intValue());
        }
        
        // Chain multiplier (optional)
        if (ruleMap.containsKey("chain_multiplier")) {
            builder.chainMultiplier((Boolean) ruleMap.get("chain_multiplier"));
        }
        
        // Chain timeout (optional)
        if (ruleMap.containsKey("chain_timeout_seconds")) {
            builder.chainTimeoutSeconds(((Number) ruleMap.get("chain_timeout_seconds")).intValue());
        }
        
        // Streak bonus (optional)
        if (ruleMap.containsKey("streak_bonus")) {
            builder.streakBonus(((Number) ruleMap.get("streak_bonus")).intValue());
        }
        
        // Chance (optional)
        if (ruleMap.containsKey("chance")) {
            builder.chance(((Number) ruleMap.get("chance")).doubleValue());
        }
        
        // Max triggers (optional)
        if (ruleMap.containsKey("max_triggers")) {
            builder.maxTriggers(((Number) ruleMap.get("max_triggers")).intValue());
        }
        
        // Negate if self placed (optional)
        if (ruleMap.containsKey("only_if_not_self_placed")) {
            builder.negateIfSelfPlaced((Boolean) ruleMap.get("only_if_not_self_placed"));
        }
        
        // Command actions on success (optional)
        if (ruleMap.containsKey("on_success")) {
            List<?> commandsList = (List<?>) ruleMap.get("on_success");
            for (Object cmdObj : commandsList) {
                if (cmdObj instanceof Map) {
                    CommandAction action = parseCommandAction((Map<?, ?>) cmdObj);
                    builder.addOnSuccess(action);
                }
            }
        }
        
        // Command actions on fail (optional)
        if (ruleMap.containsKey("on_fail")) {
            List<?> commandsList = (List<?>) ruleMap.get("on_fail");
            for (Object cmdObj : commandsList) {
                if (cmdObj instanceof Map) {
                    CommandAction action = parseCommandAction((Map<?, ?>) cmdObj);
                    builder.addOnFail(action);
                }
            }
        }
        
        return builder.build();
    }
    
    /**
     * Parse a command action from a map.
     */
    private static CommandAction parseCommandAction(Map<?, ?> cmdMap) {
        CommandAction.Builder builder = new CommandAction.Builder();
        
        // Executor type (required)
        String executorStr = (String) cmdMap.get("executor");
        if (executorStr == null) {
            throw new IllegalArgumentException("Command action missing required 'executor' field");
        }
        builder.executor(executorStr);
        
        // Command (required)
        String command = (String) cmdMap.get("command");
        if (command == null) {
            throw new IllegalArgumentException("Command action missing required 'command' field");
        }
        builder.command(command);
        
        // Variables (optional)
        if (cmdMap.containsKey("variables")) {
            Object varsObj = cmdMap.get("variables");
            if (varsObj instanceof Map) {
                Map<?, ?> varsMap = (Map<?, ?>) varsObj;
                for (Map.Entry<?, ?> entry : varsMap.entrySet()) {
                    builder.variable(entry.getKey().toString(), entry.getValue().toString());
                }
            }
        }
        
        // Delay (optional)
        if (cmdMap.containsKey("delay_ticks")) {
            builder.delay(((Number) cmdMap.get("delay_ticks")).intValue());
        }
        if (cmdMap.containsKey("delay_seconds")) {
            builder.delaySeconds(((Number) cmdMap.get("delay_seconds")).intValue());
        }
        
        // Chance (optional)
        if (cmdMap.containsKey("chance")) {
            builder.chance(((Number) cmdMap.get("chance")).doubleValue());
        }
        
        // Condition (optional)
        if (cmdMap.containsKey("condition")) {
            builder.condition((String) cmdMap.get("condition"));
        }
        
        return builder.build();
    }
}
