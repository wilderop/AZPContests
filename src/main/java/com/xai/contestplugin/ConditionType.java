package com.xai.contestplugin;

/**
 * All supported condition types for scoring rules.
 * Conditions are evaluated to determine if points should be awarded.
 */
public enum ConditionType {
    // ==================== MATERIAL CONDITIONS ====================
    MATERIAL("Block/item must be this material", "STONE"),
    MATERIALS("Block/item must be one of these materials", "STONE,DIRT,COBBLESTONE"),
    MATERIAL_TAG("Block/item must match this tag", "minecraft:logs"),
    NOT_MATERIAL("Block/item must NOT be this material", "BEDROCK"),
    
    // ==================== LOCATION CONDITIONS ====================
    MIN_Y("Y-coordinate must be >= this value", "30"),
    MAX_Y("Y-coordinate must be <= this value", "60"),
    EXACT_Y("Y-coordinate must be exactly this value", "64"),
    IN_AREA("Must be inside contest area", "true"),
    WORLD("Must be in this world", "world"),
    WORLDS("Must be in one of these worlds", "world,world_nether"),
    BIOME("Must be in this biome", "PLAINS"),
    BIOMES("Must be in one of these biomes", "PLAINS,FOREST"),
    DISTANCE_FROM_SPAWN("Distance from world spawn", "<1000"),
    
    // ==================== TIME/WEATHER CONDITIONS ====================
    TIME("Time of day must match", "DAY/NIGHT/DAWN/DUSK"),
    WEATHER("Weather must match", "CLEAR/RAIN/THUNDER"),
    MOON_PHASE("Moon phase must match", "FULL/NEW/HALF"),
    MINECRAFT_DAY("Minecraft day number", ">100"),
    
    // ==================== ENTITY CONDITIONS ====================
    ENTITY_TYPE("Entity must be this type", "ZOMBIE"),
    ENTITY_TYPES("Entity must be one of these types", "ZOMBIE,SKELETON,CREEPER"),
    ENTITY_NAME("Entity name must match", "Bob"),
    ENTITY_NAME_CONTAINS("Entity name must contain this text", "Boss"),
    ENTITY_HEALTH_ABOVE("Entity health must be >", "50"),
    ENTITY_HEALTH_BELOW("Entity health must be <", "10"),
    ENTITY_HAS_AI("Entity must have AI enabled", "true"),
    ENTITY_AGE("Entity age must match", ">100"),
    IS_BABY("Entity must be a baby", "true"),
    IS_ADULT("Entity must be an adult", "true"),
    IS_BOSS("Entity must be a boss mob", "true"),
    
    // ==================== PLAYER CONDITIONS ====================
    PLAYER_GAMEMODE("Player gamemode must be", "SURVIVAL/CREATIVE/ADVENTURE/SPECTATOR"),
    PLAYER_HEALTH_ABOVE("Player health must be >", "15"),
    PLAYER_HEALTH_BELOW("Player health must be <", "5"),
    PLAYER_HUNGER_ABOVE("Player hunger must be >", "15"),
    PLAYER_HUNGER_BELOW("Player hunger must be <", "5"),
    PLAYER_LEVEL_ABOVE("Player level must be >", "30"),
    PLAYER_LEVEL_BELOW("Player level must be <", "10"),
    PLAYER_EXPERIENCE_ABOVE("Player experience must be >", "1000"),
    PLAYER_HAS_PERMISSION("Player must have this permission", "example.permission"),
    PLAYER_HAS_ITEM("Player must have this item in inventory", "DIAMOND_SWORD"),
    PLAYER_HAS_ITEMS("Player must have all of these items", "DIAMOND_SWORD,DIAMOND_PICKAXE"),
    PLAYER_HAS_ENCHANT("Player must have item with this enchant", "FORTUNE"),
    PLAYER_WEARING_HELMET("Player must be wearing helmet", "DIAMOND_HELMET"),
    PLAYER_WEARING_CHESTPLATE("Player must be wearing chestplate", "DIAMOND_CHESTPLATE"),
    PLAYER_WEARING_LEGGINGS("Player must be wearing leggings", "DIAMOND_LEGGINGS"),
    PLAYER_WEARING_BOOTS("Player must be wearing boots", "DIAMOND_BOOTS"),
    PLAYER_RIDING_ENTITY("Player must be riding this entity type", "HORSE"),
    PLAYER_SNEAKING("Player must be sneaking", "true"),
    PLAYER_SPRINTING("Player must be sprinting", "true"),
    PLAYER_SWIMMING("Player must be swimming", "true"),
    PLAYER_FLYING("Player must be flying", "true"),
    PLAYER_ON_FIRE("Player must be on fire", "true"),
    PLAYER_IN_VEHICLE("Player must be in a vehicle", "true"),
    PLAYER_ON_GROUND("Player must be on ground", "true"),
    PLAYER_IN_WATER("Player must be in water", "true"),
    PLAYER_IN_LAVA("Player must be in lava", "true"),
    
    // ==================== ITEM CONDITIONS ====================
    ITEM_MATERIAL("Item must be this material", "DIAMOND"),
    ITEM_MATERIALS("Item must be one of these materials", "DIAMOND,EMERALD"),
    ITEM_NAME("Item name must match", "Legendary Sword"),
    ITEM_NAME_CONTAINS("Item name must contain", "Legendary"),
    ITEM_LORE_CONTAINS("Item lore must contain", "Powerful"),
    ITEM_ENCHANT("Item must have this enchantment", "SHARPNESS"),
    ITEM_ENCHANTS("Item must have all these enchantments", "SHARPNESS,LOOTING"),
    ITEM_ENCHANT_LEVEL("Enchantment level must be >=", "SHARPNESS:5"),
    ITEM_DURABILITY_ABOVE("Item durability must be >", "100"),
    ITEM_DURABILITY_BELOW("Item durability must be <", "10"),
    ITEM_AMOUNT_ABOVE("Item stack amount must be >", "32"),
    ITEM_AMOUNT_BELOW("Item stack amount must be <", "16"),
    
    // ==================== TOOL/WEAPON CONDITIONS ====================
    REQUIRE_TOOL_TYPE("Requires specific tool type", "PICKAXE/AXE/SHOVEL/HOE/SWORD"),
    REQUIRE_TOOL_MATERIAL("Requires specific tool material", "DIAMOND/NETHERITE"),
    REQUIRE_ENCHANT("Requires item to have enchantment", "FORTUNE"),
    REQUIRE_ENCHANT_LEVEL("Requires enchant at minimum level", "FORTUNE:3"),
    
    // ==================== SPECIAL CONDITIONS ====================
    ONLY_IF_NOT_SELF_PLACED("Only score if player didn't place this block", "true"),
    FRIENDLY_FIRE("Allow scoring on teammates", "false"),
    CHANCE("Probability of scoring (0.0-1.0)", "0.5"),
    COOLDOWN_SECONDS("Seconds before this rule can trigger again", "10"),
    REQUIRE_LINE_OF_SIGHT("Requires clear line of sight to target", "true"),
    BLOCK_LIGHT_LEVEL("Block light level must match", ">10"),
    SKY_LIGHT_LEVEL("Sky light level must match", ">10"),
    
    // ==================== CHAIN/COMBO CONDITIONS ====================
    CHAIN_MULTIPLIER("Multiply points by chain count", "true"),
    CHAIN_TIMEOUT_SECONDS("Seconds before chain resets", "5"),
    STREAK_BONUS("Bonus points per streak count", "10"),
    STREAK_MATERIAL("Must match same material for streak", "true"),
    
    // ==================== ADVANCED CONDITIONS ====================
    FORMULA("Custom formula for point calculation", "base * (y / 100)"),
    DAMAGE_DEALT("Damage dealt must be >", "10"),
    DAMAGE_TYPE("Damage type must be", "PROJECTILE/MELEE/MAGIC/FIRE"),
    DEATH_CAUSE("Death cause must be", "FALL/FIRE/DROWNING"),
    TRADE_TYPE("Trade type must be", "BUY/SELL"),
    FISH_TYPE("Fish caught must be", "COD/SALMON/TROPICAL_FISH"),
    
    // ==================== STRUCTURE/GROWTH CONDITIONS ====================
    STRUCTURE_TYPE("Structure type must be", "TREE/MUSHROOM/BAMBOO"),
    TREE_TYPE("Tree species must be", "OAK/BIRCH/SPRUCE/JUNGLE"),
    CROP_TYPE("Crop type must be", "WHEAT/CARROT/POTATO"),
    GROWTH_STAGE("Crop growth stage must be", "0-7"),
    SAPLING_TYPE("Sapling type must be", "OAK_SAPLING/BIRCH_SAPLING"),
    
    // ==================== BREEDING/TAMING CONDITIONS ====================
    ANIMAL_TYPE("Animal type must be", "COW/SHEEP/CHICKEN"),
    BABY_TYPE("Baby entity type must be", "COW/SHEEP"),
    PARENT_TYPES("Parent entity types", "COW,COW"),
    BREEDING_ITEM("Item used for breeding", "WHEAT"),
    TAMED_ENTITY_TYPE("Tamed entity type", "WOLF/CAT/HORSE"),
    
    // ==================== VILLAGER CONDITIONS ====================
    VILLAGER_PROFESSION("Villager profession", "FARMER/LIBRARIAN/CLERIC"),
    VILLAGER_LEVEL("Villager trade level", "1-5"),
    TRADE_ITEM_IN("Item traded to villager", "EMERALD"),
    TRADE_ITEM_OUT("Item received from villager", "ENCHANTED_BOOK"),
    EMERALD_COST("Emerald cost of trade", ">10"),
    TRADE_LEVEL("Trade tier level", "1-5"),
    
    // ==================== POTION/BREWING CONDITIONS ====================
    POTION_TYPE("Potion type", "SPEED/STRENGTH/HEALING"),
    POTION_EFFECTS("Potion must have effects", "SPEED,JUMP_BOOST"),
    POTION_DURATION("Potion duration in ticks", ">1200"),
    POTION_AMPLIFIER("Potion effect level", ">1"),
    BREWING_INGREDIENT("Ingredient used", "BLAZE_POWDER"),
    
    // ==================== PROJECTILE CONDITIONS ====================
    PROJECTILE_TYPE("Projectile type", "ARROW/SNOWBALL/EGG"),
    HIT_TYPE("What projectile hit", "BLOCK/ENTITY/PLAYER"),
    HIT_ENTITY_TYPE("Entity type hit by projectile", "ZOMBIE"),
    DISTANCE_TRAVELED("Distance projectile traveled", ">50"),
    PROJECTILE_VELOCITY("Projectile velocity", ">2.0"),
    
    // ==================== ADVANCEMENT CONDITIONS ====================
    ADVANCEMENT_KEY("Advancement identifier", "minecraft:story/mine_diamond"),
    ADVANCEMENT_CATEGORY("Advancement category", "STORY/NETHER/END"),
    ADVANCEMENT_TYPE("Advancement type", "TASK/GOAL/CHALLENGE"),
    
    // ==================== EXPLOSION/COMBAT CONDITIONS ====================
    EXPLOSION_TYPE("Explosion source", "CREEPER/TNT/WITHER"),
    EXPLOSION_RADIUS("Explosion radius", ">3.0"),
    DAMAGE_CAUSE("Damage cause", "FALL/FIRE/ENTITY_ATTACK"),
    ATTACKER_TYPE("Attacker entity type", "ZOMBIE/PLAYER"),
    WEAPON_TYPE("Weapon used", "SWORD/AXE/BOW"),
    
    // ==================== SHEEP CONDITIONS ====================
    SHEEP_COLOR("Sheep wool color", "WHITE/RED/BLUE"),
    DYE_COLOR("Dye color used", "RED/BLUE/GREEN"),
    WOOL_REGROWN("Wool regrown after shearing", "true"),
    
    // ==================== FURNACE CONDITIONS ====================
    SMELTED_ITEM("Item being smelted", "IRON_ORE"),
    SMELTED_RESULT("Result of smelting", "IRON_INGOT"),
    FUEL_TYPE("Fuel type used", "COAL/LAVA_BUCKET"),
    COOKING_TIME("Ticks to cook", ">100"),
    
    // ==================== BLOCK CHANGE CONDITIONS ====================
    SPREAD_BLOCK_TYPE("Block type spreading", "GRASS_BLOCK/MYCELIUM"),
    FORMED_BLOCK_TYPE("Block type formed", "ICE/SNOW/CONCRETE"),
    FADED_BLOCK_TYPE("Block type fading", "ICE/SNOW"),
    FLOWING_BLOCK_TYPE("Liquid type flowing", "WATER/LAVA"),
    REDSTONE_POWER("Redstone power level", ">5"),
    
    // ==================== PISTON CONDITIONS ====================
    PISTON_DIRECTION("Piston facing direction", "NORTH/SOUTH/EAST/WEST"),
    BLOCKS_MOVED("Number of blocks moved", ">1"),
    STICKY_PISTON("Must be sticky piston", "true"),
    
    // ==================== PORTAL CONDITIONS ====================
    PORTAL_TYPE("Portal type", "NETHER/END"),
    PORTAL_SIZE("Portal block count", ">10"),
    PORTAL_CREATOR("Portal created by player", "true"),
    
    // ==================== WEATHER/WORLD CONDITIONS ====================
    NEW_WEATHER("Weather changing to", "CLEAR/RAIN/THUNDER"),
    OLD_WEATHER("Weather changing from", "CLEAR/RAIN/THUNDER"),
    LIGHTNING_DISTANCE("Distance from lightning", "<10"),
    
    // ==================== INVENTORY CONDITIONS ====================
    CLICKED_SLOT("Inventory slot clicked", "0-40"),
    CLICK_TYPE("Type of click", "LEFT/RIGHT/SHIFT_LEFT"),
    INVENTORY_TYPE("Inventory type", "CHEST/CRAFTING/FURNACE"),
    CURSOR_ITEM("Item on cursor", "DIAMOND"),
    SLOT_ITEM("Item in slot", "IRON_INGOT"),
    
    // ==================== HANGING ENTITY CONDITIONS ====================
    HANGING_TYPE("Hanging entity type", "PAINTING/ITEM_FRAME"),
    PAINTING_ART("Painting artwork", "KEBAB/AZTEC/BOMB"),
    ITEM_FRAME_ROTATION("Item frame rotation", "0-7"),
    ITEM_FRAME_ITEM("Item in frame", "DIAMOND"),
    
    // ==================== ARMOR STAND CONDITIONS ====================
    ARMOR_STAND_ARMS("Armor stand has arms", "true"),
    ARMOR_STAND_BASE_PLATE("Armor stand has base plate", "true"),
    ARMOR_STAND_SMALL("Armor stand is small", "true"),
    ARMOR_STAND_MARKER("Armor stand is marker", "true"),
    
    // ==================== EXPERIENCE CONDITIONS ====================
    EXP_AMOUNT("Experience amount", ">10"),
    LEVEL_GAINED("Levels gained", ">1"),
    TOTAL_EXP("Total experience", ">1000"),
    EXP_SOURCE("Experience source", "SMELTING/BREEDING/MINING"),
    
    // ==================== BED CONDITIONS ====================
    BED_COLOR("Bed color", "RED/BLUE/WHITE"),
    RESPAWN_POINT_SET("Respawn point set", "true"),
    TIME_SKIP("Time skip occurred", "true"),
    
    // ==================== BUCKET CONDITIONS ====================
    BUCKET_TYPE("Bucket type", "WATER/LAVA/MILK"),
    LIQUID_SOURCE("Liquid is source block", "true"),
    
    // ==================== TRANSFORM CONDITIONS ====================
    TRANSFORM_FROM("Entity transforming from", "ZOMBIE"),
    TRANSFORM_TO("Entity transforming to", "DROWNED"),
    TRANSFORM_CAUSE("Transform cause", "DROWNING/LIGHTNING/CONVERSION"),
    
    // ==================== FISHING CONDITIONS ====================
    CAUGHT_ITEM("Item caught while fishing", "ENCHANTED_BOOK"),
    FISH_STATE("Fishing state", "CAUGHT_FISH/CAUGHT_ENTITY/FAILED_ATTEMPT"),
    HOOK_ENTITY("Entity hooked", "COD/SALMON"),
    
    // ==================== CONSUMPTION CONDITIONS ====================
    FOOD_ITEM("Food item consumed", "BREAD/APPLE"),
    HUNGER_RESTORED("Hunger restored", ">6"),
    SATURATION_RESTORED("Saturation restored", ">3"),
    FOOD_LEVEL_AFTER("Food level after eating", ">15"),
    
    // ==================== ENTITY INTERACTION CONDITIONS ====================
    INTERACTION_TYPE("Type of interaction", "FEED/HEAL/NAME"),
    HELD_ITEM("Item held during interaction", "WHEAT/NAME_TAG"),
    
    // ==================== REGION/PLOT CONDITIONS ====================
    IN_REGION("Must be in this WorldGuard region", "spawn"),
    IN_PLOT("Must be in a plot (PlotSquared)", "true"),
    OWNS_PLOT("Must own the plot", "true"),
    
    // ==================== TEAM CONDITIONS ====================
    SAME_TEAM("Target must be on same team", "true"),
    DIFFERENT_TEAM("Target must be on different team", "true"),
    TEAM_NAME("Player must be on this team", "red"),
    
    // ==================== NEGATION ====================
    NOT("Negates the condition", "true"),
    
    // ==================== LOGICAL OPERATORS ====================
    AND("All sub-conditions must be true", "condition1,condition2"),
    OR("Any sub-condition must be true", "condition1,condition2"),
    
    // ==================== METADATA CONDITIONS ====================
    HAS_METADATA("Player/entity must have this metadata key", "custom_tag"),
    METADATA_VALUE("Metadata value must match", "custom_tag:value");
    
    private final String description;
    private final String example;
    
    ConditionType(String description, String example) {
        this.description = description;
        this.example = example;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getExample() {
        return example;
    }
}
