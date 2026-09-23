package com.xai.contestplugin;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks player attribution for events that don't have a direct player reference.
 * Used for events like structure growth, breeding, entity transformations, etc.
 */
public class PlayerAttributionTracker {
    private final JavaPlugin plugin;
    private final DatabaseManager db;
    
    // Track who planted saplings (location -> player UUID + timestamp)
    private final Map<Location, AttributionData> plantedSaplings = new ConcurrentHashMap<>();
    
    // Track who bred animals (baby entity UUID -> breeder UUID + timestamp)
    private final Map<UUID, AttributionData> bredAnimals = new ConcurrentHashMap<>();
    
    // Track who tamed entities (entity UUID -> tamer UUID + timestamp)
    private final Map<UUID, AttributionData> tamedEntities = new ConcurrentHashMap<>();
    
    // Track who dyed sheep (sheep UUID -> dyer UUID + timestamp)
    private final Map<UUID, AttributionData> dyedSheep = new ConcurrentHashMap<>();
    
    // Track who sheared sheep (sheep UUID -> shearer UUID + timestamp)
    private final Map<UUID, AttributionData> shearedSheep = new ConcurrentHashMap<>();
    
    // Track villager creators (villager UUID -> creator UUID + timestamp)
    private final Map<UUID, AttributionData> villagerCreators = new ConcurrentHashMap<>();
    
    // Track villager professions (villager UUID -> profession + timestamp)
    private final Map<UUID, VillagerData> villagerProfessions = new ConcurrentHashMap<>();
    
    // Track portal creators (portal location -> creator UUID + timestamp)
    private final Map<Location, AttributionData> portalCreators = new ConcurrentHashMap<>();
    
    // Track hanging entity placers (hanging UUID -> placer UUID + timestamp)
    private final Map<UUID, AttributionData> hangingPlacers = new ConcurrentHashMap<>();
    
    // Track armor stand manipulators (armor stand UUID -> manipulator UUID + timestamp)
    private final Map<UUID, AttributionData> armorStandManipulators = new ConcurrentHashMap<>();
    
    // Track enderman block changes (enderman UUID -> player UUID + timestamp)
    private final Map<UUID, AttributionData> endermanTracking = new ConcurrentHashMap<>();
    
    public PlayerAttributionTracker(JavaPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }
    
    // ==================== SAPLING TRACKING ====================
    
    public void trackSaplingPlacement(Location location, UUID playerUUID) {
        plantedSaplings.put(location.clone(), new AttributionData(playerUUID, System.currentTimeMillis()));
    }
    
    public UUID getSaplingPlanter(Location location) {
        AttributionData data = plantedSaplings.get(location);
        return data != null ? data.playerUUID : null;
    }
    
    public void removeSapling(Location location) {
        plantedSaplings.remove(location);
    }
    
    // ==================== BREEDING TRACKING ====================
    
    public void trackBreeding(UUID babyUUID, UUID breederUUID) {
        bredAnimals.put(babyUUID, new AttributionData(breederUUID, System.currentTimeMillis()));
    }
    
    public UUID getBreeder(UUID babyUUID) {
        AttributionData data = bredAnimals.get(babyUUID);
        return data != null ? data.playerUUID : null;
    }
    
    public void removeBreeding(UUID babyUUID) {
        bredAnimals.remove(babyUUID);
    }
    
    // ==================== TAMING TRACKING ====================
    
    public void trackTaming(UUID entityUUID, UUID tamerUUID) {
        tamedEntities.put(entityUUID, new AttributionData(tamerUUID, System.currentTimeMillis()));
    }
    
    public UUID getTamer(UUID entityUUID) {
        AttributionData data = tamedEntities.get(entityUUID);
        return data != null ? data.playerUUID : null;
    }
    
    public void removeTaming(UUID entityUUID) {
        tamedEntities.remove(entityUUID);
    }
    
    // ==================== SHEEP DYEING TRACKING ====================
    
    public void trackSheepDyeing(UUID sheepUUID, UUID dyerUUID) {
        dyedSheep.put(sheepUUID, new AttributionData(dyerUUID, System.currentTimeMillis()));
    }
    
    public UUID getSheepDyer(UUID sheepUUID) {
        AttributionData data = dyedSheep.get(sheepUUID);
        return data != null ? data.playerUUID : null;
    }
    
    public void removeSheepDyeing(UUID sheepUUID) {
        dyedSheep.remove(sheepUUID);
    }
    
    // ==================== SHEEP SHEARING TRACKING ====================
    
    public void trackSheepShearing(UUID sheepUUID, UUID shearerUUID) {
        shearedSheep.put(sheepUUID, new AttributionData(shearerUUID, System.currentTimeMillis()));
    }
    
    public UUID getSheepShearer(UUID sheepUUID) {
        AttributionData data = shearedSheep.get(sheepUUID);
        return data != null ? data.playerUUID : null;
    }
    
    public void removeSheepShearing(UUID sheepUUID) {
        shearedSheep.remove(sheepUUID);
    }
    
    // ==================== VILLAGER TRACKING ====================
    
    public void trackVillagerCreation(UUID villagerUUID, UUID creatorUUID) {
        villagerCreators.put(villagerUUID, new AttributionData(creatorUUID, System.currentTimeMillis()));
    }
    
    public UUID getVillagerCreator(UUID villagerUUID) {
        AttributionData data = villagerCreators.get(villagerUUID);
        return data != null ? data.playerUUID : null;
    }
    
    public void trackVillagerProfession(UUID villagerUUID, String profession, UUID playerUUID) {
        villagerProfessions.put(villagerUUID, new VillagerData(profession, playerUUID, System.currentTimeMillis()));
    }
    
    public VillagerData getVillagerData(UUID villagerUUID) {
        return villagerProfessions.get(villagerUUID);
    }
    
    public void removeVillager(UUID villagerUUID) {
        villagerCreators.remove(villagerUUID);
        villagerProfessions.remove(villagerUUID);
    }
    
    // ==================== PORTAL TRACKING ====================
    
    public void trackPortalCreation(Location location, UUID creatorUUID) {
        portalCreators.put(location.clone(), new AttributionData(creatorUUID, System.currentTimeMillis()));
    }
    
    public UUID getPortalCreator(Location location) {
        AttributionData data = portalCreators.get(location);
        return data != null ? data.playerUUID : null;
    }
    
    public void removePortal(Location location) {
        portalCreators.remove(location);
    }
    
    // ==================== HANGING ENTITY TRACKING ====================
    
    public void trackHangingPlacement(UUID hangingUUID, UUID placerUUID) {
        hangingPlacers.put(hangingUUID, new AttributionData(placerUUID, System.currentTimeMillis()));
    }
    
    public UUID getHangingPlacer(UUID hangingUUID) {
        AttributionData data = hangingPlacers.get(hangingUUID);
        return data != null ? data.playerUUID : null;
    }
    
    public void removeHanging(UUID hangingUUID) {
        hangingPlacers.remove(hangingUUID);
    }
    
    // ==================== ARMOR STAND TRACKING ====================
    
    public void trackArmorStandManipulation(UUID armorStandUUID, UUID manipulatorUUID) {
        armorStandManipulators.put(armorStandUUID, new AttributionData(manipulatorUUID, System.currentTimeMillis()));
    }
    
    public UUID getArmorStandManipulator(UUID armorStandUUID) {
        AttributionData data = armorStandManipulators.get(armorStandUUID);
        return data != null ? data.playerUUID : null;
    }
    
    public void removeArmorStand(UUID armorStandUUID) {
        armorStandManipulators.remove(armorStandUUID);
    }
    
    // ==================== ENDERMAN TRACKING ====================
    
    public void trackEnderman(UUID endermanUUID, UUID playerUUID) {
        endermanTracking.put(endermanUUID, new AttributionData(playerUUID, System.currentTimeMillis()));
    }
    
    public UUID getEndermanPlayer(UUID endermanUUID) {
        AttributionData data = endermanTracking.get(endermanUUID);
        return data != null ? data.playerUUID : null;
    }
    
    public void removeEnderman(UUID endermanUUID) {
        endermanTracking.remove(endermanUUID);
    }
    
    // ==================== CLEANUP & PERSISTENCE ====================
    
    /**
     * Clean up old tracking data (older than 7 days)
     */
    public void cleanupOldData() {
        long cutoffTime = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000); // 7 days
        
        plantedSaplings.entrySet().removeIf(entry -> entry.getValue().timestamp < cutoffTime);
        bredAnimals.entrySet().removeIf(entry -> entry.getValue().timestamp < cutoffTime);
        tamedEntities.entrySet().removeIf(entry -> entry.getValue().timestamp < cutoffTime);
        dyedSheep.entrySet().removeIf(entry -> entry.getValue().timestamp < cutoffTime);
        shearedSheep.entrySet().removeIf(entry -> entry.getValue().timestamp < cutoffTime);
        villagerCreators.entrySet().removeIf(entry -> entry.getValue().timestamp < cutoffTime);
        villagerProfessions.entrySet().removeIf(entry -> entry.getValue().timestamp < cutoffTime);
        portalCreators.entrySet().removeIf(entry -> entry.getValue().timestamp < cutoffTime);
        hangingPlacers.entrySet().removeIf(entry -> entry.getValue().timestamp < cutoffTime);
        armorStandManipulators.entrySet().removeIf(entry -> entry.getValue().timestamp < cutoffTime);
        endermanTracking.entrySet().removeIf(entry -> entry.getValue().timestamp < cutoffTime);
    }
    
    /**
     * Save all tracking data to database
     */
    public void saveToDatabase(String contestName) {
        try {
            db.saveTrackingData(contestName, "saplings", plantedSaplings);
            db.saveTrackingData(contestName, "breeding", bredAnimals);
            db.saveTrackingData(contestName, "taming", tamedEntities);
            db.saveTrackingData(contestName, "sheep_dye", dyedSheep);
            db.saveTrackingData(contestName, "sheep_shear", shearedSheep);
            db.saveTrackingData(contestName, "villagers", villagerCreators);
            db.saveVillagerData(contestName, villagerProfessions);
            db.saveTrackingData(contestName, "portals", portalCreators);
            db.saveTrackingData(contestName, "hanging", hangingPlacers);
            db.saveTrackingData(contestName, "armor_stands", armorStandManipulators);
            db.saveTrackingData(contestName, "enderman", endermanTracking);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save tracking data: " + e.getMessage());
        }
    }
    
    /**
     * Load all tracking data from database
     */
    public void loadFromDatabase(String contestName) {
        try {
            plantedSaplings.clear();
            bredAnimals.clear();
            tamedEntities.clear();
            dyedSheep.clear();
            shearedSheep.clear();
            villagerCreators.clear();
            villagerProfessions.clear();
            portalCreators.clear();
            hangingPlacers.clear();
            armorStandManipulators.clear();
            endermanTracking.clear();
            
            plantedSaplings.putAll(db.loadLocationTrackingData(contestName, "saplings"));
            bredAnimals.putAll(db.loadUUIDTrackingData(contestName, "breeding"));
            tamedEntities.putAll(db.loadUUIDTrackingData(contestName, "taming"));
            dyedSheep.putAll(db.loadUUIDTrackingData(contestName, "sheep_dye"));
            shearedSheep.putAll(db.loadUUIDTrackingData(contestName, "sheep_shear"));
            villagerCreators.putAll(db.loadUUIDTrackingData(contestName, "villagers"));
            villagerProfessions.putAll(db.loadVillagerData(contestName));
            portalCreators.putAll(db.loadLocationTrackingData(contestName, "portals"));
            hangingPlacers.putAll(db.loadUUIDTrackingData(contestName, "hanging"));
            armorStandManipulators.putAll(db.loadUUIDTrackingData(contestName, "armor_stands"));
            endermanTracking.putAll(db.loadUUIDTrackingData(contestName, "enderman"));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load tracking data: " + e.getMessage());
        }
    }
    
    /**
     * Clear all tracking data
     */
    public void clearAll() {
        plantedSaplings.clear();
        bredAnimals.clear();
        tamedEntities.clear();
        dyedSheep.clear();
        shearedSheep.clear();
        villagerCreators.clear();
        villagerProfessions.clear();
        portalCreators.clear();
        hangingPlacers.clear();
        armorStandManipulators.clear();
        endermanTracking.clear();
    }
    
    // ==================== DATA CLASSES ====================
    
    public static class AttributionData {
        public final UUID playerUUID;
        public final long timestamp;
        
        public AttributionData(UUID playerUUID, long timestamp) {
            this.playerUUID = playerUUID;
            this.timestamp = timestamp;
        }
    }
    
    public static class VillagerData {
        public final String profession;
        public final UUID playerUUID;
        public final long timestamp;
        
        public VillagerData(String profession, UUID playerUUID, long timestamp) {
            this.profession = profession;
            this.playerUUID = playerUUID;
            this.timestamp = timestamp;
        }
    }
}
