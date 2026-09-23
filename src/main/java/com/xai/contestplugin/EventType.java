package com.xai.contestplugin;

/**
 * All supported event types for contest scoring.
 * Each event type maps to one or more Bukkit events.
 */
public enum EventType {
    // ==================== BLOCK EVENTS ====================
    BLOCK_PLACE("Player places a block"),
    BLOCK_BREAK("Player breaks a block"),
    BLOCK_INTERACT("Player interacts with a block (right-click)"),
    BLOCK_FERTILIZE("Player uses bone meal on a block"),
    BLOCK_IGNITE("Player ignites a block"),
    BLOCK_BURN("Block burns from fire/lava"),
    BLOCK_SPREAD("Block spreads (grass, mycelium)"),
    BLOCK_FROM_TO("Liquid flows or block teleports"),
    BLOCK_FORM("Block forms naturally (ice, snow, concrete)"),
    BLOCK_FADE("Block fades (ice melts, crops die)"),
    BLOCK_REDSTONE("Redstone power state changes"),
    LEAVES_DECAY("Leaves decay naturally"),
    STRUCTURE_GROW("Structure grows (trees, mushrooms, bamboo)"),
    CROP_GROWTH("Crop grows to next stage"),
    PISTON_EXTEND("Piston extends"),
    PISTON_RETRACT("Piston retracts"),
    
    // ==================== ENTITY EVENTS ====================
    ENTITY_KILL("Player kills an entity"),
    ENTITY_DAMAGE("Player damages an entity"),
    ENTITY_DAMAGE_RECEIVED("Player receives damage from entity"),
    ENTITY_TAME("Player tames an entity"),
    ENTITY_BREED("Player breeds two entities"),
    ENTITY_SHEAR("Player shears a sheep"),
    ENTITY_COMBUST("Player sets entity on fire"),
    ENTITY_TARGET("Entity targets the player"),
    ENTITY_INTERACT("Player interacts with an entity"),
    ENTITY_DAMAGE_BY_ENTITY("Entity damages another entity"),
    ENTITY_RESURRECT("Player uses totem of undying"),
    ENTITY_TRANSFORM("Entity transforms (zombie to drowned, etc)"),
    ENTITY_CHANGE_BLOCK("Entity picks up or places block (enderman)"),
    ENTITY_EXPLODE("Entity explodes (creeper, TNT, wither)"),
    SHEEP_DYE("Player dyes a sheep"),
    SHEEP_REGROW_WOOL("Sheep regrows wool after shearing"),
    VILLAGER_TRADE("Player trades with villager"),
    VILLAGER_ACQUIRE_TRADE("Villager gains new trade option"),
    VILLAGER_CAREER_CHANGE("Villager changes profession"),
    
    // ==================== PLAYER EVENTS ====================
    PLAYER_DEATH("Player dies"),
    PLAYER_RESPAWN("Player respawns"),
    PLAYER_JOIN("Player joins the server"),
    PLAYER_QUIT("Player leaves the server"),
    PLAYER_FISH("Player catches something while fishing"),
    PLAYER_FISH_SUCCESS("Player successfully catches a fish"),
    PLAYER_FISH_FAIL("Player's fishing attempt fails"),
    PLAYER_CRAFT("Player crafts an item"),
    PLAYER_SMELT("Player smelts an item in furnace"),
    PLAYER_ENCHANT("Player enchants an item"),
    PLAYER_BREW("Player brews a potion"),
    PLAYER_CONSUME("Player eats/drinks an item"),
    PLAYER_BUCKET_FILL("Player fills a bucket"),
    PLAYER_BUCKET_EMPTY("Player empties a bucket"),
    PLAYER_LEVEL_UP("Player gains a level"),
    PLAYER_EXPERIENCE_CHANGE("Player gains/loses experience"),
    PLAYER_TRADE("Player trades with villager"),
    PLAYER_PORTAL("Player uses a portal"),
    PLAYER_TELEPORT("Player teleports"),
    PLAYER_INTERACT("Player interacts (right-click)"),
    PLAYER_DROP_ITEM("Player drops an item"),
    PLAYER_PICKUP_ITEM("Player picks up an item"),
    PLAYER_ATTACK("Player attacks (any entity)"),
    PLAYER_SNEAK("Player starts sneaking"),
    PLAYER_SPRINT("Player starts sprinting"),
    PLAYER_JUMP("Player jumps"),
    PLAYER_CHAT("Player sends a chat message"),
    PLAYER_COMMAND("Player executes a command"),
    PLAYER_BED_ENTER("Player enters bed"),
    PLAYER_BED_LEAVE("Player leaves bed"),
    PLAYER_PICKUP_ARROW("Player picks up arrow"),
    PLAYER_RIPTIDE("Player uses riptide trident"),
    PLAYER_ADVANCE_ACHIEVEMENT("Player earns advancement"),
    PLAYER_ITEM_CONSUME("Player consumes food or potion"),
    PLAYER_INTERACT_ENTITY("Player interacts with entity"),
    PLAYER_SHEAR_ENTITY("Player shears entity"),
    
    // ==================== COMBAT EVENTS ====================
    PVP_KILL("Player kills another player"),
    PVP_DAMAGE("Player damages another player"),
    PVP_DEATH("Player dies to another player"),
    MOB_KILL("Player kills a mob"),
    BOSS_KILL("Player kills a boss mob"),
    
    // ==================== INVENTORY EVENTS ====================
    INVENTORY_CLICK("Player clicks in inventory"),
    INVENTORY_PICKUP("Player picks up item to cursor"),
    INVENTORY_OPEN("Player opens an inventory"),
    INVENTORY_CLOSE("Player closes an inventory"),
    INVENTORY_DRAG("Player drags items in inventory"),
    INVENTORY_PICKUP_ITEM("Player picks up item from ground"),
    PREPARE_ANVIL("Player prepares anvil operation"),
    PREPARE_ITEM_ENCHANT("Player prepares enchantment"),
    FURNACE_SMELT("Item finishes smelting in furnace"),
    FURNACE_EXTRACT("Player extracts item from furnace"),
    BREW("Potion finishes brewing"),
    
    // ==================== WORLD EVENTS ====================
    LIGHTNING_STRIKE("Lightning strikes near player"),
    EXPLOSION("Explosion occurs near player"),
    WEATHER_CHANGE("Weather changes"),
    PORTAL_CREATE("Portal is created"),
    
    // ==================== PROJECTILE EVENTS ====================
    PROJECTILE_HIT("Projectile hits target"),
    PROJECTILE_LAUNCH("Projectile is launched"),
    
    // ==================== HANGING ENTITY EVENTS ====================
    HANGING_PLACE("Player places painting or item frame"),
    HANGING_BREAK("Player breaks painting or item frame"),
    
    // ==================== ARMOR STAND EVENTS ====================
    ARMOR_STAND_MANIPULATE("Player manipulates armor stand"),
    
    // ==================== ADVANCEMENT EVENTS ====================
    ADVANCEMENT_DONE("Player completes an advancement"),
    
    // ==================== VEHICLE EVENTS ====================
    VEHICLE_ENTER("Player enters a vehicle"),
    VEHICLE_EXIT("Player exits a vehicle"),
    VEHICLE_DAMAGE("Player damages a vehicle"),
    VEHICLE_DESTROY("Player destroys a vehicle"),
    
    // ==================== SPECIAL CONTEST EVENTS ====================
    CONTEST_JOIN("Player joins the contest"),
    CONTEST_LEAVE("Player leaves the contest"),
    LEAVE_AREA("Player leaves contest area"),
    RETURN_AREA("Player returns to contest area"),
    PERIODIC("Triggers every X seconds (for passive scoring)"),
    
    // ==================== TIME-BASED EVENTS ====================
    EVERY_SECOND("Triggers every second player is in contest"),
    EVERY_MINUTE("Triggers every minute player is in contest"),
    
    // ==================== PLACEHOLDER FOR FUTURE EXPANSION ====================
    CUSTOM("Custom event defined by plugin developers");
    
    private final String description;
    
    EventType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this event type involves blocks.
     */
    public boolean isBlockEvent() {
        return this.name().startsWith("BLOCK_");
    }
    
    /**
     * Check if this event type involves entities.
     */
    public boolean isEntityEvent() {
        return this.name().startsWith("ENTITY_") || this.name().startsWith("MOB_") || this.name().startsWith("BOSS_");
    }
    
    /**
     * Check if this event type involves combat.
     */
    public boolean isCombatEvent() {
        return this.name().startsWith("PVP_") || this == ENTITY_KILL || this == ENTITY_DAMAGE;
    }
    
    /**
     * Check if this event type is time-based.
     */
    public boolean isTimeBasedEvent() {
        return this == PERIODIC || this == EVERY_SECOND || this == EVERY_MINUTE;
    }
}
