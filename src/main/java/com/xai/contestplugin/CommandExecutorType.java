package com.xai.contestplugin;

/**
 * Defines how a command should be executed.
 */
public enum CommandExecutorType {
    CONSOLE("Execute as console", "give {player} diamond 1"),
    PLAYER("Execute as the player", "say I got points!"),
    OP("Execute as player with temporary OP", "gamemode creative"),
    BROADCAST("Broadcast message to all players", "{player} just earned points!"),
    PLAYER_MESSAGE("Send message to the player only", "You earned {points} points!"),
    TITLE("Send title to the player", "Title~Subtitle~10~70~20"),
    SUBTITLE("Send subtitle to the player", "You earned points!"),
    ACTIONBAR("Send action bar message to the player", "Points: {score}"),
    SOUND("Play sound to the player", "ENTITY_PLAYER_LEVELUP~1.0~1.0"),
    PARTICLE("Spawn particle effect at player", "FIREWORKS_SPARK~10~0.5"),
    GIVE_ITEM("Give item to the player", "DIAMOND_SWORD~1~&bLegendary Sword"),
    TAKE_ITEM("Take item from the player", "DIRT~64"),
    TELEPORT("Teleport the player", "world~0~100~0"),
    EFFECT("Apply potion effect to the player", "SPEED~60~1"),
    CLEAR_EFFECTS("Clear all potion effects from player", ""),
    HEAL("Heal the player", "20"),
    FEED("Feed the player", "20"),
    DAMAGE("Damage the player", "5"),
    SET_HEALTH("Set player health", "10"),
    SET_HUNGER("Set player hunger", "10"),
    SET_LEVEL("Set player level", "30"),
    GIVE_EXP("Give player experience", "100"),
    TAKE_EXP("Take player experience", "50"),
    LAUNCH("Launch player in the air", "5"),
    STRIKE_LIGHTNING("Strike lightning at player", "false"),
    CREATE_EXPLOSION("Create explosion at player", "2~false~false"),
    SET_FIRE("Set player on fire", "5"),
    SET_GAMEMODE("Set player gamemode", "SURVIVAL/CREATIVE/ADVENTURE/SPECTATOR"),
    SEND_TO_SERVER("Send player to another server (BungeeCord)", "lobby"),
    KICK("Kick the player", "You've been kicked!"),
    CLOSE_INVENTORY("Close player's inventory", ""),
    UPDATE_SCOREBOARD("Update player's scoreboard", "custom_tag~value"),
    SET_METADATA("Set custom metadata on player", "key~value"),
    REMOVE_METADATA("Remove metadata from player", "key"),
    GRANT_PERMISSION("Grant temporary permission", "custom.permission"),
    REVOKE_PERMISSION("Revoke temporary permission", "custom.permission"),
    RUN_FUNCTION("Run a Minecraft function", "namespace:path/to/function"),
    WEBHOOK("Send webhook to external URL (if supported)", "https://discord.com/webhook..."),
    EXECUTE_SERIES("Execute multiple commands in sequence", "command1|command2|command3"),
    CONDITIONAL("Execute command only if condition met", "if:{condition}~then:{command}"),
    DELAYED("Execute command after delay in ticks", "100~command"),
    RANDOM("Execute random command from list", "command1|command2|command3"),
    CHANCE("Execute command with % chance", "0.5~command");
    
    private final String description;
    private final String exampleValue;
    
    CommandExecutorType(String description, String exampleValue) {
        this.description = description;
        this.exampleValue = exampleValue;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getExampleValue() {
        return exampleValue;
    }
    
    /**
     * Check if this executor type modifies player state.
     */
    public boolean isPlayerModifying() {
        return this == HEAL || this == FEED || this == DAMAGE || this == SET_HEALTH ||
               this == SET_HUNGER || this == SET_LEVEL || this == GIVE_EXP || this == TAKE_EXP ||
               this == LAUNCH || this == SET_FIRE || this == SET_GAMEMODE || this == EFFECT ||
               this == CLEAR_EFFECTS;
    }
    
    /**
     * Check if this executor type gives items/rewards.
     */
    public boolean isRewardType() {
        return this == GIVE_ITEM || this == GIVE_EXP;
    }
    
    /**
     * Check if this executor type is a punishment.
     */
    public boolean isPunishmentType() {
        return this == TAKE_ITEM || this == TAKE_EXP || this == DAMAGE || this == KICK ||
               this == SET_FIRE || this == STRIKE_LIGHTNING;
    }
}
