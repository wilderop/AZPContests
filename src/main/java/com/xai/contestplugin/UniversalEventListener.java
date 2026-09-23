package com.xai.contestplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.*;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.UUID;

public class UniversalEventListener implements Listener {
    
    private final ContestManager contestManager;
    private final PlayerAttributionTracker attributionTracker;
    
    public UniversalEventListener(ContestManager contestManager, PlayerAttributionTracker attributionTracker) {
        this.contestManager = contestManager;
        this.attributionTracker = attributionTracker;
    }
    
    private ScoringEngine getEngine() {
        return contestManager.getScoringEngine();
    }
    
    private boolean isActive() {
        return contestManager.isContestRunning();
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isActive()) return;
        try {
            Player player = event.getPlayer();
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getBlock().getLocation();
                engine.trackBlockPlace(loc, player.getUniqueId());
                
                if (event.getBlock().getType().name().contains("SAPLING") || 
                    event.getBlock().getType() == Material.MANGROVE_PROPAGULE) {
                    attributionTracker.trackSaplingPlacement(loc, player.getUniqueId());
                    if (ContestManager.isDebugMode()) {
                        contestManager.getPlugin().getLogger().info("Sapling placed by " + player.getName() + " at " + loc);
                    }
                }
                
                engine.processEvent(EventType.BLOCK_PLACE, event, player);
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in BLOCK_PLACE: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isActive()) return;
        try {
            Player player = event.getPlayer();
            ScoringEngine engine = getEngine();
            if (engine != null) {
                engine.processEvent(EventType.BLOCK_BREAK, event, player);
                attributionTracker.removeSapling(event.getBlock().getLocation());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in BLOCK_BREAK: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        if (!isActive()) return;
        try {
            Player player = event.getPlayer();
            if (player != null && getEngine() != null) {
                getEngine().processEvent(EventType.BLOCK_FERTILIZE, event, player);
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in BLOCK_FERTILIZE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (!isActive()) return;
        try {
            Player player = event.getPlayer();
            if (player != null && getEngine() != null) {
                getEngine().processEvent(EventType.BLOCK_IGNITE, event, player);
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in BLOCK_IGNITE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getBlock().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 50 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.BLOCK_BURN, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in BLOCK_BURN: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getBlock().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 50 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.BLOCK_SPREAD, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in BLOCK_SPREAD: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getBlock().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 50 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.BLOCK_FROM_TO, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in BLOCK_FROM_TO: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getBlock().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 50 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.BLOCK_FORM, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in BLOCK_FORM: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getBlock().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 50 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.BLOCK_FADE, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in BLOCK_FADE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getBlock().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 20 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.BLOCK_REDSTONE, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in BLOCK_REDSTONE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getBlock().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 30 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.LEAVES_DECAY, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in LEAVES_DECAY: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (!isActive()) return;
        
        if (ContestManager.isDebugMode()) {
            contestManager.getPlugin().getLogger().info("STRUCTURE_GROW event fired!");
        }
        
        try {
            Location saplingLoc = event.getLocation();
            UUID planter = attributionTracker.getSaplingPlanter(saplingLoc);
            
            if (ContestManager.isDebugMode()) {
                contestManager.getPlugin().getLogger().info("Sapling location: " + saplingLoc);
                contestManager.getPlugin().getLogger().info("Planter UUID: " + planter);
            }
            
            if (planter != null) {
                Player player = org.bukkit.Bukkit.getPlayer(planter);
                
                if (ContestManager.isDebugMode()) {
                    contestManager.getPlugin().getLogger().info("Player found: " + (player != null ? player.getName() : "null"));
                }
                
                if (player != null && getEngine() != null) {
                    getEngine().processEvent(EventType.STRUCTURE_GROW, event, player);
                    if (ContestManager.isDebugMode()) {
                        contestManager.getPlugin().getLogger().info("Event processed for: " + player.getName());
                    }
                }
                attributionTracker.removeSapling(saplingLoc);
            } else {
                if (ContestManager.isDebugMode()) {
                    contestManager.getPlugin().getLogger().warning("No planter found for sapling at: " + saplingLoc);
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in STRUCTURE_GROW: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCropGrow(BlockGrowEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getBlock().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 30 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.CROP_GROWTH, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in CROP_GROWTH: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getBlock().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 20 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.PISTON_EXTEND, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PISTON_EXTEND: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getBlock().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 20 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.PISTON_RETRACT, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PISTON_RETRACT: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isActive()) return;
        try {
            Player killer = event.getEntity().getKiller();
            if (killer != null && getEngine() != null) {
                getEngine().processEvent(EventType.ENTITY_KILL, event, killer);
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in ENTITY_KILL: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                if (event.getDamager() instanceof Player) {
                    engine.processEvent(EventType.ENTITY_DAMAGE, event, (Player) event.getDamager());
                    engine.processEvent(EventType.ENTITY_DAMAGE_BY_ENTITY, event, (Player) event.getDamager());
                }
                if (event.getEntity() instanceof Player) {
                    engine.processEvent(EventType.ENTITY_DAMAGE_RECEIVED, event, (Player) event.getEntity());
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in ENTITY_DAMAGE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityTame(EntityTameEvent event) {
        if (!isActive()) return;
        try {
            if (event.getOwner() instanceof Player) {
                Player player = (Player) event.getOwner();
                if (getEngine() != null) {
                    getEngine().processEvent(EventType.ENTITY_TAME, event, player);
                    attributionTracker.trackTaming(event.getEntity().getUniqueId(), player.getUniqueId());
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in ENTITY_TAME: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent event) {
        if (!isActive()) return;
        try {
            if (event.getBreeder() instanceof Player) {
                Player player = (Player) event.getBreeder();
                if (getEngine() != null) {
                    getEngine().processEvent(EventType.ENTITY_BREED, event, player);
                    if (event.getEntity() != null) {
                        attributionTracker.trackBreeding(event.getEntity().getUniqueId(), player.getUniqueId());
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in ENTITY_BREED: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        if (!isActive()) return;
        try {
            Player player = event.getPlayer();
            if (getEngine() != null) {
                getEngine().processEvent(EventType.ENTITY_SHEAR, event, player);
                getEngine().processEvent(EventType.PLAYER_SHEAR_ENTITY, event, player);
                if (event.getEntity() instanceof Sheep) {
                    attributionTracker.trackSheepShearing(event.getEntity().getUniqueId(), player.getUniqueId());
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_SHEAR_ENTITY: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSheepDye(SheepDyeWoolEvent event) {
        if (!isActive()) return;
        try {
            if (event.getPlayer() != null && getEngine() != null) {
                getEngine().processEvent(EventType.SHEEP_DYE, event, event.getPlayer());
                attributionTracker.trackSheepDyeing(event.getEntity().getUniqueId(), event.getPlayer().getUniqueId());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in SHEEP_DYE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSheepRegrowWool(SheepRegrowWoolEvent event) {
        if (!isActive()) return;
        try {
            UUID shearer = attributionTracker.getSheepShearer(event.getEntity().getUniqueId());
            if (shearer != null) {
                Player player = org.bukkit.Bukkit.getPlayer(shearer);
                if (player != null && getEngine() != null) {
                    getEngine().processEvent(EventType.SHEEP_REGROW_WOOL, event, player);
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in SHEEP_REGROW_WOOL: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getEntity().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 10 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.VILLAGER_ACQUIRE_TRADE, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in VILLAGER_ACQUIRE_TRADE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVillagerCareerChange(VillagerCareerChangeEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getEntity().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 10 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.VILLAGER_CAREER_CHANGE, event, player);
                        attributionTracker.trackVillagerProfession(event.getEntity().getUniqueId(), event.getProfession().name(), player.getUniqueId());
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in VILLAGER_CAREER_CHANGE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityResurrect(EntityResurrectEvent event) {
        if (!isActive()) return;
        try {
            if (event.getEntity() instanceof Player && getEngine() != null) {
                getEngine().processEvent(EventType.ENTITY_RESURRECT, event, (Player) event.getEntity());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in ENTITY_RESURRECT: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityTransform(EntityTransformEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getEntity().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 30 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.ENTITY_TRANSFORM, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in ENTITY_TRANSFORM: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null && event.getEntity() instanceof Enderman) {
                Location loc = event.getBlock().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 30 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.ENTITY_CHANGE_BLOCK, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in ENTITY_CHANGE_BLOCK: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 50 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.ENTITY_EXPLODE, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in ENTITY_EXPLODE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!isActive()) return;
        try {
            Player player = event.getEntity();
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_DEATH, event, player);
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_DEATH: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_RESPAWN, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_RESPAWN: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerFish(PlayerFishEvent event) {
        if (!isActive()) return;
        try {
            Player player = event.getPlayer();
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_FISH, event, player);
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_FISH: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!isActive()) return;
        try {
            if (event.getWhoClicked() instanceof Player && getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_CRAFT, event, (Player) event.getWhoClicked());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_CRAFT: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_SMELT, event, event.getPlayer());
                getEngine().processEvent(EventType.FURNACE_EXTRACT, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in FURNACE_EXTRACT: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_ENCHANT, event, event.getEnchanter());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_ENCHANT: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PREPARE_ITEM_ENCHANT, event, event.getEnchanter());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PREPARE_ITEM_ENCHANT: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBrewEvent(BrewEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getBlock().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 10 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.PLAYER_BREW, event, player);
                        engine.processEvent(EventType.BREW, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in BREW: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_CONSUME, event, event.getPlayer());
                getEngine().processEvent(EventType.PLAYER_ITEM_CONSUME, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_ITEM_CONSUME: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_BUCKET_FILL, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_BUCKET_FILL: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_BUCKET_EMPTY, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_BUCKET_EMPTY: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_LEVEL_UP, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_LEVEL_UP: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_EXPERIENCE_CHANGE, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_EXPERIENCE_CHANGE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTrade(TradeSelectEvent event) {
        if (!isActive()) return;
        try {
            if (event.getWhoClicked() instanceof Player && getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_TRADE, event, (Player) event.getWhoClicked());
                getEngine().processEvent(EventType.VILLAGER_TRADE, event, (Player) event.getWhoClicked());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_TRADE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_PORTAL, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_PORTAL: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_TELEPORT, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_TELEPORT: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_DROP_ITEM, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_DROP_ITEM: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_PICKUP_ITEM, event, event.getPlayer());
                getEngine().processEvent(EventType.INVENTORY_PICKUP_ITEM, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_PICKUP_ITEM: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_PICKUP_ARROW, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_PICKUP_ARROW: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRiptide(PlayerRiptideEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_RIPTIDE, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_RIPTIDE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.ADVANCEMENT_DONE, event, event.getPlayer());
                getEngine().processEvent(EventType.PLAYER_ADVANCE_ACHIEVEMENT, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_ADVANCE_ACHIEVEMENT: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_BED_ENTER, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_BED_ENTER: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_BED_LEAVE, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_BED_LEAVE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.PLAYER_INTERACT_ENTITY, event, event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PLAYER_INTERACT_ENTITY: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!isActive()) return;
        try {
            if (event.getEntity().getShooter() instanceof Player && getEngine() != null) {
                getEngine().processEvent(EventType.PROJECTILE_HIT, event, (Player) event.getEntity().getShooter());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PROJECTILE_HIT: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!isActive()) return;
        try {
            if (event.getEntity().getShooter() instanceof Player && getEngine() != null) {
                getEngine().processEvent(EventType.PROJECTILE_LAUNCH, event, (Player) event.getEntity().getShooter());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PROJECTILE_LAUNCH: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isActive()) return;
        try {
            if (event.getWhoClicked() instanceof Player && getEngine() != null) {
                getEngine().processEvent(EventType.INVENTORY_CLICK, event, (Player) event.getWhoClicked());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in INVENTORY_CLICK: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!isActive()) return;
        try {
            if (event.getPlayer() instanceof Player && getEngine() != null) {
                getEngine().processEvent(EventType.INVENTORY_CLOSE, event, (Player) event.getPlayer());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in INVENTORY_CLOSE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!isActive()) return;
        try {
            if (event.getWhoClicked() instanceof Player && getEngine() != null) {
                getEngine().processEvent(EventType.INVENTORY_DRAG, event, (Player) event.getWhoClicked());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in INVENTORY_DRAG: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getInventory().getLocation();
                if (loc != null) {
                    for (Player player : loc.getWorld().getPlayers()) {
                        if (player.getLocation().distance(loc) < 10 && engine.isParticipant(player.getUniqueId())) {
                            engine.processEvent(EventType.PREPARE_ANVIL, event, player);
                        }
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PREPARE_ANVIL: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getBlock().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 10 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.FURNACE_SMELT, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in FURNACE_SMELT: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onLightningStrike(LightningStrikeEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                Location loc = event.getLightning().getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 100 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.LIGHTNING_STRIKE, event, player);
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in LIGHTNING_STRIKE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null) {
                for (Player player : event.getWorld().getPlayers()) {
                    engine.processEvent(EventType.WEATHER_CHANGE, event, player);
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in WEATHER_CHANGE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPortalCreate(PortalCreateEvent event) {
        if (!isActive()) return;
        try {
            ScoringEngine engine = getEngine();
            if (engine != null && event.getBlocks().size() > 0) {
                Location loc = event.getBlocks().get(0).getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) < 20 && engine.isParticipant(player.getUniqueId())) {
                        engine.processEvent(EventType.PORTAL_CREATE, event, player);
                        attributionTracker.trackPortalCreation(loc, player.getUniqueId());
                    }
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in PORTAL_CREATE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (!isActive()) return;
        try {
            if (event.getPlayer() != null && getEngine() != null) {
                getEngine().processEvent(EventType.HANGING_PLACE, event, event.getPlayer());
                attributionTracker.trackHangingPlacement(event.getEntity().getUniqueId(), event.getPlayer().getUniqueId());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in HANGING_PLACE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (!isActive()) return;
        try {
            if (event instanceof HangingBreakByEntityEvent) {
                HangingBreakByEntityEvent breakEvent = (HangingBreakByEntityEvent) event;
                if (breakEvent.getRemover() instanceof Player && getEngine() != null) {
                    getEngine().processEvent(EventType.HANGING_BREAK, event, (Player) breakEvent.getRemover());
                }
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in HANGING_BREAK: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (!isActive()) return;
        try {
            if (getEngine() != null) {
                getEngine().processEvent(EventType.ARMOR_STAND_MANIPULATE, event, event.getPlayer());
                attributionTracker.trackArmorStandManipulation(event.getRightClicked().getUniqueId(), event.getPlayer().getUniqueId());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in ARMOR_STAND_MANIPULATE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!isActive()) return;
        try {
            if (event.getEntered() instanceof Player && getEngine() != null) {
                getEngine().processEvent(EventType.VEHICLE_ENTER, event, (Player) event.getEntered());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in VEHICLE_ENTER: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onVehicleExit(VehicleExitEvent event) {
        if (!isActive()) return;
        try {
            if (event.getExited() instanceof Player && getEngine() != null) {
                getEngine().processEvent(EventType.VEHICLE_EXIT, event, (Player) event.getExited());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in VEHICLE_EXIT: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (!isActive()) return;
        try {
            if (event.getAttacker() instanceof Player && getEngine() != null) {
                getEngine().processEvent(EventType.VEHICLE_DAMAGE, event, (Player) event.getAttacker());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in VEHICLE_DAMAGE: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (!isActive()) return;
        try {
            if (event.getAttacker() instanceof Player && getEngine() != null) {
                getEngine().processEvent(EventType.VEHICLE_DESTROY, event, (Player) event.getAttacker());
            }
        } catch (Exception e) {
            contestManager.getPlugin().getLogger().warning("Error in VEHICLE_DESTROY: " + e.getMessage());
        }
    }
}
