package com.xai.contestplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Evaluates conditions to determine if a scoring rule should trigger.
 * This is the brain of the event-driven scoring system.
 */
public class ConditionEvaluator {
    
    private final Map<Location, UUID> placedBlocks = new HashMap<>();
    private final JavaPlugin plugin;
    
    public ConditionEvaluator(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Evaluate all conditions for a scoring rule.
     * Returns true if ALL conditions are met.
     */
    public boolean evaluate(Map<String, Object> conditions, Event event, Player player, Contest contest) {
        if (conditions == null || conditions.isEmpty()) {
            if (ContestManager.isDebugMode()) {
                plugin.getLogger().info("[DEBUG] No conditions to evaluate - returning true");
            }
            return true; // No conditions = always true
        }
        
        if (ContestManager.isDebugMode()) {
            plugin.getLogger().info("[DEBUG] Evaluating " + conditions.size() + " conditions");
        }
        
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String conditionKey = entry.getKey().toLowerCase();
            Object conditionValue = entry.getValue();
            
            if (ContestManager.isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Checking condition: " + conditionKey + " = " + conditionValue);
            }
            
            boolean result = evaluateCondition(conditionKey, conditionValue, event, player, contest);
            
            if (ContestManager.isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Condition " + conditionKey + " result: " + result);
            }
            
            if (!result) {
                if (ContestManager.isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Condition FAILED: " + conditionKey);
                }
                return false; // Any failed condition = rule doesn't trigger
            }
        }
        
        if (ContestManager.isDebugMode()) {
            plugin.getLogger().info("[DEBUG] All conditions passed!");
        }
        return true; // All conditions passed
    }
    
    /**
     * Evaluate a single condition.
     */
    private boolean evaluateCondition(String key, Object value, Event event, Player player, Contest contest) {
        switch (key) {
            // Material conditions
            case "material":
                return checkMaterial(value, event);
            case "materials":
                return checkMaterials(value, event);
            case "not_material":
                return !checkMaterial(value, event);
            
            // Location conditions
            case "min_y":
                return checkMinY((Integer) value, event, player);
            case "max_y":
                return checkMaxY((Integer) value, event, player);
            case "exact_y":
                return checkExactY((Integer) value, event, player);
            case "in_area":
                return checkInArea((Boolean) value, player, contest);
            case "world":
                return checkWorld((String) value, player);
            case "worlds":
                return checkWorlds(value, player);
            case "biome":
                return checkBiome((String) value, player);
            case "biomes":
                return checkBiomes(value, player);
            
            // Time/Weather conditions
            case "time":
                return checkTime((String) value, player);
            case "weather":
                return checkWeather((String) value, player);
            
            // Entity conditions
            case "entity_type":
                return checkEntityType((String) value, event);
            case "entity_types":
                return checkEntityTypes(value, event);
            case "is_boss":
                return checkIsBoss((Boolean) value, event);
            case "is_baby":
                return checkIsBaby((Boolean) value, event);
            case "is_adult":
                return !checkIsBaby(true, event);
            
            // Player conditions
            case "player_gamemode":
                return checkPlayerGamemode((String) value, player);
            case "player_health_above":
                return player.getHealth() > ((Number) value).doubleValue();
            case "player_health_below":
                return player.getHealth() < ((Number) value).doubleValue();
            case "player_level_above":
                return player.getLevel() > ((Number) value).intValue();
            case "player_level_below":
                return player.getLevel() < ((Number) value).intValue();
            case "player_has_item":
                return checkPlayerHasItem((String) value, player);
            case "player_sneaking":
                return player.isSneaking() == (Boolean) value;
            case "player_sprinting":
                return player.isSprinting() == (Boolean) value;
            case "player_swimming":
                return player.isSwimming() == (Boolean) value;
            case "player_flying":
                return player.isFlying() == (Boolean) value;
            case "player_on_fire":
                return (player.getFireTicks() > 0) == (Boolean) value;
            case "player_in_water":
                return player.isInWater() == (Boolean) value;
            case "player_on_ground":
                return player.isOnGround() == (Boolean) value;
            
            // Tool conditions
            case "require_tool_type":
                return checkToolType((String) value, player);
            case "require_tool_material":
                return checkToolMaterial((String) value, player);
            case "require_enchant":
                return checkRequireEnchant((String) value, player);
            
            // Special conditions
            case "only_if_not_self_placed":
                return checkNotSelfPlaced((Boolean) value, event, player);
            case "chance":
                return Math.random() < ((Number) value).doubleValue();
            
            // Damage conditions
            case "damage_dealt":
                return checkDamageDealt((String) value, event);
            case "damage_type":
                return checkDamageType((String) value, event);
            
            // Structure/Growth conditions
            case "structure_type":
            case "tree_type":
            case "crop_type":
            case "growth_stage":
            case "sapling_type":
                return checkStructureCondition(key, value, event);
            
            // Breeding/Taming conditions
            case "animal_type":
            case "baby_type":
            case "breeding_item":
            case "tamed_entity_type":
                return checkBreedingCondition(key, value, event);
            
            // Villager conditions
            case "villager_profession":
            case "villager_level":
            case "trade_item_in":
            case "trade_item_out":
                return checkVillagerCondition(key, value, event);
            
            // Potion/Brewing conditions
            case "potion_type":
            case "potion_effects":
            case "brewing_ingredient":
                return checkPotionCondition(key, value, event);
            
            // Projectile conditions
            case "projectile_type":
            case "hit_type":
            case "hit_entity_type":
            case "distance_traveled":
                return checkProjectileCondition(key, value, event);
            
            // Advancement conditions
            case "advancement_key":
                return checkAdvancementCondition(value, event);
            
            // Explosion/Combat conditions
            case "explosion_type":
            case "attacker_type":
            case "weapon_type":
                return checkCombatCondition(key, value, event);
            
            // Sheep conditions
            case "sheep_color":
            case "dye_color":
                return checkSheepCondition(key, value, event);
            
            // Furnace conditions
            case "smelted_item":
            case "smelted_result":
            case "fuel_type":
                return checkFurnaceCondition(key, value, event);
            
            // Block change conditions
            case "spread_block_type":
            case "formed_block_type":
            case "faded_block_type":
            case "flowing_block_type":
            case "redstone_power":
                return checkBlockChangeCondition(key, value, event);
            
            // Piston conditions
            case "piston_direction":
            case "blocks_moved":
            case "sticky_piston":
                return checkPistonCondition(key, value, event);
            
            // Portal conditions
            case "portal_type":
            case "portal_size":
                return checkPortalCondition(key, value, event);
            
            // Weather conditions
            case "new_weather":
            case "old_weather":
            case "lightning_distance":
                return checkWeatherCondition(key, value, event, player);
            
            // Inventory conditions
            case "clicked_slot":
            case "click_type":
            case "inventory_type":
                return checkInventoryCondition(key, value, event);
            
            // Fishing conditions
            case "caught_item":
            case "fish_state":
            case "hook_entity":
                return checkFishingCondition(key, value, event);
            
            // Consumption conditions
            case "food_item":
            case "hunger_restored":
                return checkConsumptionCondition(key, value, event);
            
            // Bucket conditions
            case "bucket_type":
            case "liquid_source":
                return checkBucketCondition(key, value, event);
            
            // Transform conditions
            case "transform_from":
            case "transform_to":
                return checkTransformCondition(key, value, event);
            
            // Bed conditions
            case "bed_color":
                return checkBedCondition(value, event);
            
            // Logical operators
            case "not":
                return false; // Handled at higher level
            case "and":
                return evaluateAnd((List<?>) value, event, player, contest);
            case "or":
                return evaluateOr((List<?>) value, event, player, contest);
            
            default:
                // Unknown condition - log warning but don't fail
                return true;
        }
    }
    
    // ==================== MATERIAL CHECKS ====================
    
    private boolean checkMaterial(Object value, Event event) {
        String materialName = value.toString().toUpperCase();
        Material material = Material.valueOf(materialName);
        
        if (event instanceof BlockBreakEvent) {
            return ((BlockBreakEvent) event).getBlock().getType() == material;
        } else if (event instanceof BlockPlaceEvent) {
            return ((BlockPlaceEvent) event).getBlock().getType() == material;
        } else if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent pie = (PlayerInteractEvent) event;
            if (pie.getClickedBlock() != null) {
                return pie.getClickedBlock().getType() == material;
            }
        }
        return false;
    }
    
    private boolean checkMaterials(Object value, Event event) {
        List<String> materials;
        if (value instanceof String) {
            materials = Arrays.asList(((String) value).split(","));
        } else {
            materials = (List<String>) value;
        }
        
        for (String mat : materials) {
            if (checkMaterial(mat.trim(), event)) {
                return true;
            }
        }
        return false;
    }
    
    // ==================== LOCATION CHECKS ====================
    
    private boolean checkMinY(int minY, Event event, Player player) {
        int y = getEventY(event, player);
        return y >= minY;
    }
    
    private boolean checkMaxY(int maxY, Event event, Player player) {
        int y = getEventY(event, player);
        return y <= maxY;
    }
    
    private boolean checkExactY(int exactY, Event event, Player player) {
        int y = getEventY(event, player);
        return y == exactY;
    }
    
    private int getEventY(Event event, Player player) {
        if (event instanceof BlockBreakEvent) {
            return ((BlockBreakEvent) event).getBlock().getY();
        } else if (event instanceof BlockPlaceEvent) {
            return ((BlockPlaceEvent) event).getBlock().getY();
        } else if (event instanceof EntityEvent) {
            return ((EntityEvent) event).getEntity().getLocation().getBlockY();
        }
        return player.getLocation().getBlockY();
    }
    
    private boolean checkInArea(boolean shouldBeInArea, Player player, Contest contest) {
        if (!shouldBeInArea) return true; // Don't care about area
        
        Location loc = player.getLocation();
        return loc.getWorld().equals(contest.getAreaWorld()) &&
               loc.getBlockX() >= contest.getMinX() && loc.getBlockX() <= contest.getMaxX() &&
               loc.getBlockY() >= contest.getMinY() && loc.getBlockY() <= contest.getMaxY() &&
               loc.getBlockZ() >= contest.getMinZ() && loc.getBlockZ() <= contest.getMaxZ();
    }
    
    private boolean checkWorld(String worldName, Player player) {
        return player.getWorld().getName().equals(worldName);
    }
    
    private boolean checkWorlds(Object value, Player player) {
        List<String> worlds = (List<String>) value;
        return worlds.contains(player.getWorld().getName());
    }
    
    private boolean checkBiome(String biomeName, Player player) {
        return player.getLocation().getBlock().getBiome().name().equals(biomeName.toUpperCase());
    }
    
    private boolean checkBiomes(Object value, Player player) {
        List<String> biomes = (List<String>) value;
        String currentBiome = player.getLocation().getBlock().getBiome().name();
        return biomes.stream().anyMatch(b -> b.equalsIgnoreCase(currentBiome));
    }
    
    // ==================== TIME/WEATHER CHECKS ====================
    
    private boolean checkTime(String timeValue, Player player) {
        long time = player.getWorld().getTime();
        switch (timeValue.toUpperCase()) {
            case "DAY":
                return time >= 0 && time < 12000;
            case "NIGHT":
                return time >= 12000 && time < 24000;
            case "DAWN":
                return time >= 23000 || time < 1000;
            case "DUSK":
                return time >= 11000 && time < 13000;
            default:
                return false;
        }
    }
    
    private boolean checkWeather(String weatherValue, Player player) {
        switch (weatherValue.toUpperCase()) {
            case "CLEAR":
                return !player.getWorld().hasStorm();
            case "RAIN":
                return player.getWorld().hasStorm() && !player.getWorld().isThundering();
            case "THUNDER":
                return player.getWorld().isThundering();
            default:
                return false;
        }
    }
    
    // ==================== ENTITY CHECKS ====================
    
    private boolean checkEntityType(String entityType, Event event) {
        Entity entity = getEntityFromEvent(event);
        if (entity == null) return false;
        return entity.getType().name().equals(entityType.toUpperCase());
    }
    
    private boolean checkEntityTypes(Object value, Event event) {
        List<String> types = (List<String>) value;
        Entity entity = getEntityFromEvent(event);
        if (entity == null) return false;
        return types.stream().anyMatch(t -> t.equalsIgnoreCase(entity.getType().name()));
    }
    
    private boolean checkIsBoss(boolean shouldBeBoss, Event event) {
        Entity entity = getEntityFromEvent(event);
        if (entity == null) return false;
        
        // Check if entity is a boss type
        return (entity.getType() == EntityType.ENDER_DRAGON ||
                entity.getType() == EntityType.WITHER ||
                entity.getType() == EntityType.ELDER_GUARDIAN ||
                entity.getType() == EntityType.WARDEN) == shouldBeBoss;
    }
    
    private boolean checkIsBaby(boolean shouldBeBaby, Event event) {
        Entity entity = getEntityFromEvent(event);
        if (entity == null) return false;
        
        if (entity instanceof Ageable) {
            return ((Ageable) entity).isAdult() != shouldBeBaby;
        }
        return false;
    }
    
    private Entity getEntityFromEvent(Event event) {
        if (event instanceof EntityDeathEvent) {
            return ((EntityDeathEvent) event).getEntity();
        } else if (event instanceof EntityDamageByEntityEvent) {
            return ((EntityDamageByEntityEvent) event).getEntity();
        } else if (event instanceof EntityTameEvent) {
            return ((EntityTameEvent) event).getEntity();
        } else if (event instanceof EntityBreedEvent) {
            return ((EntityBreedEvent) event).getEntity();
        }
        return null;
    }
    
    // ==================== PLAYER CHECKS ====================
    
    private boolean checkPlayerGamemode(String gamemode, Player player) {
        return player.getGameMode().name().equals(gamemode.toUpperCase());
    }
    
    private boolean checkPlayerHasItem(String materialName, Player player) {
        Material material = Material.valueOf(materialName.toUpperCase());
        return player.getInventory().contains(material);
    }
    
    // ==================== TOOL CHECKS ====================
    
    private boolean checkToolType(String toolType, Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) return false;
        
        String materialName = hand.getType().name();
        switch (toolType.toUpperCase()) {
            case "PICKAXE":
                return materialName.endsWith("_PICKAXE");
            case "AXE":
                return materialName.endsWith("_AXE");
            case "SHOVEL":
                return materialName.endsWith("_SHOVEL");
            case "HOE":
                return materialName.endsWith("_HOE");
            case "SWORD":
                return materialName.endsWith("_SWORD");
            case "NONE":
                return true;
            default:
                return false;
        }
    }
    
    private boolean checkToolMaterial(String toolMaterial, Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) return false;
        
        String materialName = hand.getType().name();
        return materialName.startsWith(toolMaterial.toUpperCase());
    }
    
    private boolean checkRequireEnchant(String enchantName, Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) return false;
        
        return hand.getEnchantments().keySet().stream()
                .anyMatch(e -> e.getKey().getKey().equalsIgnoreCase(enchantName));
    }
    
    // ==================== SPECIAL CHECKS ====================
    
    private boolean checkNotSelfPlaced(boolean enabled, Event event, Player player) {
        if (!enabled) return true;
        
        if (event instanceof BlockBreakEvent) {
            Location loc = ((BlockBreakEvent) event).getBlock().getLocation();
            UUID placer = placedBlocks.get(loc);
            return placer == null || !placer.equals(player.getUniqueId());
        }
        return true;
    }
    
    public void trackBlockPlace(Location location, UUID player) {
        placedBlocks.put(location, player);
    }
    
    public void clearBlockTracking() {
        placedBlocks.clear();
    }
    
    // ==================== DAMAGE CHECKS ====================
    
    private boolean checkDamageDealt(String condition, Event event) {
        if (!(event instanceof EntityDamageEvent)) return false;
        
        double damage = ((EntityDamageEvent) event).getDamage();
        return evaluateComparison(condition, damage);
    }
    
    private boolean checkDamageType(String damageType, Event event) {
        if (!(event instanceof EntityDamageEvent)) return false;
        
        EntityDamageEvent.DamageCause cause = ((EntityDamageEvent) event).getCause();
        switch (damageType.toUpperCase()) {
            case "PROJECTILE":
                return cause == EntityDamageEvent.DamageCause.PROJECTILE;
            case "MELEE":
                return cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK;
            case "FIRE":
                return cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK;
            case "FALL":
                return cause == EntityDamageEvent.DamageCause.FALL;
            case "EXPLOSION":
                return cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || 
                       cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION;
            default:
                return false;
        }
    }
    
    // ==================== LOGICAL OPERATORS ====================
    
    private boolean evaluateAnd(List<?> conditions, Event event, Player player, Contest contest) {
        for (Object condObj : conditions) {
            Map<String, Object> condMap = (Map<String, Object>) condObj;
            if (!evaluate(condMap, event, player, contest)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean evaluateOr(List<?> conditions, Event event, Player player, Contest contest) {
        for (Object condObj : conditions) {
            Map<String, Object> condMap = (Map<String, Object>) condObj;
            if (evaluate(condMap, event, player, contest)) {
                return true;
            }
        }
        return false;
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Evaluate comparison operators like ">10", "<5", ">=20"
     */
    private boolean evaluateComparison(String condition, double value) {
        condition = condition.trim();
        
        if (condition.startsWith(">=")) {
            return value >= Double.parseDouble(condition.substring(2));
        } else if (condition.startsWith("<=")) {
            return value <= Double.parseDouble(condition.substring(2));
        } else if (condition.startsWith(">")) {
            return value > Double.parseDouble(condition.substring(1));
        } else if (condition.startsWith("<")) {
            return value < Double.parseDouble(condition.substring(1));
        } else if (condition.startsWith("==")) {
            return value == Double.parseDouble(condition.substring(2));
        } else {
            return value == Double.parseDouble(condition);
        }
    }
    
    // ==================== NEW CONDITION CHECKS ====================
    
    private boolean checkStructureCondition(String key, Object value, Event event) {
        try {
            if (event instanceof org.bukkit.event.world.StructureGrowEvent) {
                org.bukkit.event.world.StructureGrowEvent structEvent = (org.bukkit.event.world.StructureGrowEvent) event;
                String speciesName = structEvent.getSpecies().name();
                
                if (key.equals("tree_type") || key.equals("structure_type") || key.equals("sapling_type")) {
                    // Handle list of tree types
                    if (value instanceof List) {
                        List<?> treeTypes = (List<?>) value;
                        for (Object treeType : treeTypes) {
                            String strValue = treeType.toString().toUpperCase();
                            if (matchesTreeType(speciesName, strValue)) {
                                return true; // Match found
                            }
                        }
                        return false; // No match in list
                    } else {
                        // Handle single tree type
                        String strValue = value.toString().toUpperCase();
                        return matchesTreeType(speciesName, strValue);
                    }
                }
            } else if (event instanceof org.bukkit.event.block.BlockGrowEvent) {
                org.bukkit.event.block.BlockGrowEvent growEvent = (org.bukkit.event.block.BlockGrowEvent) event;
                if (key.equals("crop_type")) {
                    return growEvent.getBlock().getType().name().contains(value.toString().toUpperCase());
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    /**
     * Check if tree species matches the requested type, handling common aliases.
     * Bukkit uses "REDWOOD" for spruce, "TREE" for oak, etc.
     */
    private boolean matchesTreeType(String speciesName, String requestedType) {
        // Direct match
        if (speciesName.equals(requestedType) || speciesName.contains(requestedType)) {
            return true;
        }
        
        // Handle common aliases
        switch (requestedType) {
            case "SPRUCE":
                return speciesName.contains("REDWOOD"); // REDWOOD or TALL_REDWOOD
            case "OAK":
                return speciesName.equals("TREE") || speciesName.equals("BIG_TREE");
            case "BIRCH":
                return speciesName.equals("BIRCH") || speciesName.equals("TALL_BIRCH");
            case "JUNGLE":
                return speciesName.contains("JUNGLE"); // JUNGLE, SMALL_JUNGLE, JUNGLE_BUSH
            case "ACACIA":
                return speciesName.equals("ACACIA");
            case "DARK_OAK":
                return speciesName.equals("DARK_OAK");
            case "MANGROVE":
                return speciesName.equals("MANGROVE");
            case "CHERRY":
                return speciesName.equals("CHERRY");
            case "AZALEA":
                return speciesName.equals("AZALEA");
            case "MUSHROOM":
                return speciesName.contains("MUSHROOM"); // BROWN_MUSHROOM, RED_MUSHROOM
            default:
                return false;
        }
    }
    
    private boolean checkBreedingCondition(String key, Object value, Event event) {
        try {
            if (event instanceof EntityBreedEvent) {
                EntityBreedEvent breedEvent = (EntityBreedEvent) event;
                String strValue = value.toString().toUpperCase();
                
                switch (key) {
                    case "animal_type":
                    case "baby_type":
                        return breedEvent.getEntity().getType().name().equals(strValue);
                    case "breeding_item":
                        if (breedEvent.getBredWith() != null) {
                            return breedEvent.getBredWith().getType().name().equals(strValue);
                        }
                        return false;
                }
            } else if (event instanceof EntityTameEvent && key.equals("tamed_entity_type")) {
                EntityTameEvent tameEvent = (EntityTameEvent) event;
                return tameEvent.getEntity().getType().name().equals(value.toString().toUpperCase());
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkVillagerCondition(String key, Object value, Event event) {
        try {
            if (event instanceof org.bukkit.event.entity.VillagerCareerChangeEvent) {
                org.bukkit.event.entity.VillagerCareerChangeEvent villEvent = (org.bukkit.event.entity.VillagerCareerChangeEvent) event;
                if (key.equals("villager_profession")) {
                    return villEvent.getProfession().name().equals(value.toString().toUpperCase());
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkPotionCondition(String key, Object value, Event event) {
        try {
            if (event instanceof PlayerItemConsumeEvent) {
                PlayerItemConsumeEvent consumeEvent = (PlayerItemConsumeEvent) event;
                ItemStack item = consumeEvent.getItem();
                
                if (item.getType().name().contains("POTION") && item.hasItemMeta()) {
                    org.bukkit.inventory.meta.PotionMeta meta = (org.bukkit.inventory.meta.PotionMeta) item.getItemMeta();
                    if (key.equals("potion_type") && meta.getBasePotionData() != null) {
                        return meta.getBasePotionData().getType().name().equals(value.toString().toUpperCase());
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkProjectileCondition(String key, Object value, Event event) {
        try {
            if (event instanceof org.bukkit.event.entity.ProjectileHitEvent) {
                org.bukkit.event.entity.ProjectileHitEvent projEvent = (org.bukkit.event.entity.ProjectileHitEvent) event;
                
                switch (key) {
                    case "projectile_type":
                        return projEvent.getEntity().getType().name().equals(value.toString().toUpperCase());
                    case "hit_type":
                        if (value.toString().equalsIgnoreCase("BLOCK")) {
                            return projEvent.getHitBlock() != null;
                        } else if (value.toString().equalsIgnoreCase("ENTITY")) {
                            return projEvent.getHitEntity() != null;
                        }
                        break;
                    case "hit_entity_type":
                        if (projEvent.getHitEntity() != null) {
                            return projEvent.getHitEntity().getType().name().equals(value.toString().toUpperCase());
                        }
                        return false;
                }
            } else if (event instanceof org.bukkit.event.entity.ProjectileLaunchEvent && key.equals("projectile_type")) {
                org.bukkit.event.entity.ProjectileLaunchEvent launchEvent = (org.bukkit.event.entity.ProjectileLaunchEvent) event;
                return launchEvent.getEntity().getType().name().equals(value.toString().toUpperCase());
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkAdvancementCondition(Object value, Event event) {
        try {
            if (event instanceof org.bukkit.event.player.PlayerAdvancementDoneEvent) {
                org.bukkit.event.player.PlayerAdvancementDoneEvent advEvent = (org.bukkit.event.player.PlayerAdvancementDoneEvent) event;
                return advEvent.getAdvancement().getKey().toString().contains(value.toString());
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkCombatCondition(String key, Object value, Event event) {
        try {
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
                String strValue = value.toString().toUpperCase();
                
                switch (key) {
                    case "attacker_type":
                        return damageEvent.getDamager().getType().name().equals(strValue);
                    case "weapon_type":
                        if (damageEvent.getDamager() instanceof Player) {
                            Player attacker = (Player) damageEvent.getDamager();
                            ItemStack weapon = attacker.getInventory().getItemInMainHand();
                            return weapon != null && weapon.getType().name().contains(strValue);
                        }
                        return false;
                }
            } else if (event instanceof EntityExplodeEvent && key.equals("explosion_type")) {
                EntityExplodeEvent explEvent = (EntityExplodeEvent) event;
                return explEvent.getEntity().getType().name().equals(value.toString().toUpperCase());
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkSheepCondition(String key, Object value, Event event) {
        try {
            if (event instanceof org.bukkit.event.entity.SheepDyeWoolEvent) {
                org.bukkit.event.entity.SheepDyeWoolEvent dyeEvent = (org.bukkit.event.entity.SheepDyeWoolEvent) event;
                String strValue = value.toString().toUpperCase();
                
                if (key.equals("sheep_color")) {
                    return dyeEvent.getEntity().getColor().name().equals(strValue);
                } else if (key.equals("dye_color")) {
                    return dyeEvent.getColor().name().equals(strValue);
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkFurnaceCondition(String key, Object value, Event event) {
        try {
            if (event instanceof org.bukkit.event.inventory.FurnaceSmeltEvent) {
                org.bukkit.event.inventory.FurnaceSmeltEvent smeltEvent = (org.bukkit.event.inventory.FurnaceSmeltEvent) event;
                String strValue = value.toString().toUpperCase();
                
                if (key.equals("smelted_item")) {
                    return smeltEvent.getSource().getType().name().equals(strValue);
                } else if (key.equals("smelted_result")) {
                    return smeltEvent.getResult().getType().name().equals(strValue);
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkBlockChangeCondition(String key, Object value, Event event) {
        try {
            String strValue = value.toString().toUpperCase();
            
            if (event instanceof org.bukkit.event.block.BlockSpreadEvent && key.equals("spread_block_type")) {
                org.bukkit.event.block.BlockSpreadEvent spreadEvent = (org.bukkit.event.block.BlockSpreadEvent) event;
                return spreadEvent.getSource().getType().name().equals(strValue);
            } else if (event instanceof org.bukkit.event.block.BlockFormEvent && key.equals("formed_block_type")) {
                org.bukkit.event.block.BlockFormEvent formEvent = (org.bukkit.event.block.BlockFormEvent) event;
                return formEvent.getNewState().getType().name().equals(strValue);
            } else if (event instanceof org.bukkit.event.block.BlockFadeEvent && key.equals("faded_block_type")) {
                org.bukkit.event.block.BlockFadeEvent fadeEvent = (org.bukkit.event.block.BlockFadeEvent) event;
                return fadeEvent.getBlock().getType().name().equals(strValue);
            } else if (event instanceof org.bukkit.event.block.BlockFromToEvent && key.equals("flowing_block_type")) {
                org.bukkit.event.block.BlockFromToEvent flowEvent = (org.bukkit.event.block.BlockFromToEvent) event;
                return flowEvent.getBlock().getType().name().equals(strValue);
            } else if (event instanceof org.bukkit.event.block.BlockRedstoneEvent && key.equals("redstone_power")) {
                org.bukkit.event.block.BlockRedstoneEvent redstoneEvent = (org.bukkit.event.block.BlockRedstoneEvent) event;
                return evaluateComparison(value.toString(), redstoneEvent.getNewCurrent());
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkPistonCondition(String key, Object value, Event event) {
        try {
            if (event instanceof org.bukkit.event.block.BlockPistonExtendEvent) {
                org.bukkit.event.block.BlockPistonExtendEvent pistonEvent = (org.bukkit.event.block.BlockPistonExtendEvent) event;
                
                if (key.equals("piston_direction")) {
                    return pistonEvent.getDirection().name().equals(value.toString().toUpperCase());
                } else if (key.equals("blocks_moved")) {
                    return evaluateComparison(value.toString(), pistonEvent.getBlocks().size());
                } else if (key.equals("sticky_piston")) {
                    return pistonEvent.isSticky() == (Boolean) value;
                }
            } else if (event instanceof org.bukkit.event.block.BlockPistonRetractEvent) {
                org.bukkit.event.block.BlockPistonRetractEvent pistonEvent = (org.bukkit.event.block.BlockPistonRetractEvent) event;
                
                if (key.equals("piston_direction")) {
                    return pistonEvent.getDirection().name().equals(value.toString().toUpperCase());
                } else if (key.equals("sticky_piston")) {
                    return pistonEvent.isSticky() == (Boolean) value;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkPortalCondition(String key, Object value, Event event) {
        try {
            if (event instanceof org.bukkit.event.world.PortalCreateEvent) {
                org.bukkit.event.world.PortalCreateEvent portalEvent = (org.bukkit.event.world.PortalCreateEvent) event;
                
                if (key.equals("portal_type")) {
                    return portalEvent.getReason().name().contains(value.toString().toUpperCase());
                } else if (key.equals("portal_size")) {
                    return evaluateComparison(value.toString(), portalEvent.getBlocks().size());
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkWeatherCondition(String key, Object value, Event event, Player player) {
        try {
            if (event instanceof org.bukkit.event.weather.WeatherChangeEvent) {
                org.bukkit.event.weather.WeatherChangeEvent weatherEvent = (org.bukkit.event.weather.WeatherChangeEvent) event;
                
                if (key.equals("new_weather")) {
                    if (value.toString().equalsIgnoreCase("RAIN")) {
                        return weatherEvent.toWeatherState();
                    } else if (value.toString().equalsIgnoreCase("CLEAR")) {
                        return !weatherEvent.toWeatherState();
                    }
                }
            } else if (event instanceof org.bukkit.event.weather.LightningStrikeEvent && key.equals("lightning_distance")) {
                org.bukkit.event.weather.LightningStrikeEvent lightningEvent = (org.bukkit.event.weather.LightningStrikeEvent) event;
                double distance = player.getLocation().distance(lightningEvent.getLightning().getLocation());
                return evaluateComparison(value.toString(), distance);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkInventoryCondition(String key, Object value, Event event) {
        try {
            if (event instanceof org.bukkit.event.inventory.InventoryClickEvent) {
                org.bukkit.event.inventory.InventoryClickEvent clickEvent = (org.bukkit.event.inventory.InventoryClickEvent) event;
                
                switch (key) {
                    case "clicked_slot":
                        return evaluateComparison(value.toString(), clickEvent.getSlot());
                    case "click_type":
                        return clickEvent.getClick().name().equals(value.toString().toUpperCase());
                    case "inventory_type":
                        return clickEvent.getInventory().getType().name().equals(value.toString().toUpperCase());
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkFishingCondition(String key, Object value, Event event) {
        try {
            if (event instanceof PlayerFishEvent) {
                PlayerFishEvent fishEvent = (PlayerFishEvent) event;
                
                switch (key) {
                    case "fish_state":
                        return fishEvent.getState().name().equals(value.toString().toUpperCase());
                    case "caught_item":
                        if (fishEvent.getCaught() instanceof org.bukkit.entity.Item) {
                            org.bukkit.entity.Item item = (org.bukkit.entity.Item) fishEvent.getCaught();
                            return item.getItemStack().getType().name().equals(value.toString().toUpperCase());
                        }
                        return false;
                    case "hook_entity":
                        if (fishEvent.getCaught() != null) {
                            return fishEvent.getCaught().getType().name().equals(value.toString().toUpperCase());
                        }
                        return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkConsumptionCondition(String key, Object value, Event event) {
        try {
            if (event instanceof PlayerItemConsumeEvent) {
                PlayerItemConsumeEvent consumeEvent = (PlayerItemConsumeEvent) event;
                
                if (key.equals("food_item")) {
                    return consumeEvent.getItem().getType().name().equals(value.toString().toUpperCase());
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkBucketCondition(String key, Object value, Event event) {
        try {
            if (event instanceof PlayerBucketFillEvent) {
                PlayerBucketFillEvent bucketEvent = (PlayerBucketFillEvent) event;
                
                if (key.equals("bucket_type")) {
                    return bucketEvent.getBucket().name().contains(value.toString().toUpperCase());
                } else if (key.equals("liquid_source")) {
                    org.bukkit.block.data.BlockData blockData = bucketEvent.getBlock().getBlockData();
                    if (blockData instanceof org.bukkit.block.data.Levelled) {
                        org.bukkit.block.data.Levelled levelled = (org.bukkit.block.data.Levelled) blockData;
                        return (levelled.getLevel() == 0) == (Boolean) value;
                    }
                }
            } else if (event instanceof PlayerBucketEmptyEvent && key.equals("bucket_type")) {
                PlayerBucketEmptyEvent bucketEvent = (PlayerBucketEmptyEvent) event;
                return bucketEvent.getBucket().name().contains(value.toString().toUpperCase());
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkTransformCondition(String key, Object value, Event event) {
        try {
            if (event instanceof EntityTransformEvent) {
                EntityTransformEvent transformEvent = (EntityTransformEvent) event;
                String strValue = value.toString().toUpperCase();
                
                if (key.equals("transform_from")) {
                    return transformEvent.getEntity().getType().name().equals(strValue);
                } else if (key.equals("transform_to")) {
                    return transformEvent.getTransformedEntity().getType().name().equals(strValue);
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkBedCondition(Object value, Event event) {
        try {
            if (event instanceof PlayerBedEnterEvent) {
                PlayerBedEnterEvent bedEvent = (PlayerBedEnterEvent) event;
                org.bukkit.block.data.BlockData blockData = bedEvent.getBed().getBlockData();
                
                if (blockData instanceof org.bukkit.block.data.type.Bed) {
                    org.bukkit.block.data.type.Bed bed = (org.bukkit.block.data.type.Bed) blockData;
                    return bedEvent.getBed().getType().name().contains(value.toString().toUpperCase());
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}

