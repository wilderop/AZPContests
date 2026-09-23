package com.xai.contestplugin;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Executes command actions with full variable substitution and flexible execution modes.
 * Supports 45+ command executor types.
 */
public class CommandExecutorService {
    
    private final JavaPlugin plugin;
    
    public CommandExecutorService(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Execute a command action.
     */
    public void execute(CommandAction action, Player player, Map<String, String> variables) {
        // Replace variables in command
        String command = action.replaceVariables(variables);
        
        // Check condition if present
        if (action.getCondition() != null && !evaluateCondition(action.getCondition(), variables)) {
            return;
        }
        
        // Check chance
        if (action.getChance() < 1.0 && Math.random() >= action.getChance()) {
            return;
        }
        
        // Execute with delay if specified
        if (action.getDelayTicks() > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                executeCommand(action.getExecutorType(), command, player);
            }, action.getDelayTicks());
        } else {
            executeCommand(action.getExecutorType(), command, player);
        }
    }
    
    /**
     * Execute a command based on executor type.
     */
    private void executeCommand(CommandExecutorType type, String command, Player player) {
        try {
            switch (type) {
                case CONSOLE:
                    executeConsole(command, player);
                    break;
                case PLAYER:
                    executePlayer(command, player);
                    break;
                case OP:
                    executeAsOp(command, player);
                    break;
                case BROADCAST:
                    executeBroadcast(command);
                    break;
                case PLAYER_MESSAGE:
                    executePlayerMessage(command, player);
                    break;
                case TITLE:
                    executeTitle(command, player);
                    break;
                case SUBTITLE:
                    executeSubtitle(command, player);
                    break;
                case ACTIONBAR:
                    executeActionbar(command, player);
                    break;
                case SOUND:
                    executeSound(command, player);
                    break;
                case PARTICLE:
                    executeParticle(command, player);
                    break;
                case GIVE_ITEM:
                    executeGiveItem(command, player);
                    break;
                case TAKE_ITEM:
                    executeTakeItem(command, player);
                    break;
                case TELEPORT:
                    executeTeleport(command, player);
                    break;
                case EFFECT:
                    executeEffect(command, player);
                    break;
                case CLEAR_EFFECTS:
                    executeClearEffects(player);
                    break;
                case HEAL:
                    executeHeal(command, player);
                    break;
                case FEED:
                    executeFeed(command, player);
                    break;
                case DAMAGE:
                    executeDamage(command, player);
                    break;
                case SET_HEALTH:
                    executeSetHealth(command, player);
                    break;
                case SET_HUNGER:
                    executeSetHunger(command, player);
                    break;
                case SET_LEVEL:
                    executeSetLevel(command, player);
                    break;
                case GIVE_EXP:
                    executeGiveExp(command, player);
                    break;
                case TAKE_EXP:
                    executeTakeExp(command, player);
                    break;
                case LAUNCH:
                    executeLaunch(command, player);
                    break;
                case STRIKE_LIGHTNING:
                    executeStrikeLightning(command, player);
                    break;
                case CREATE_EXPLOSION:
                    executeExplosion(command, player);
                    break;
                case SET_FIRE:
                    executeSetFire(command, player);
                    break;
                case SET_GAMEMODE:
                    executeSetGamemode(command, player);
                    break;
                case CLOSE_INVENTORY:
                    executeCloseInventory(player);
                    break;
                case SET_METADATA:
                    executeSetMetadata(command, player);
                    break;
                case REMOVE_METADATA:
                    executeRemoveMetadata(command, player);
                    break;
                case EXECUTE_SERIES:
                    executeCommandSeries(command, player);
                    break;
                case RANDOM:
                    executeRandomCommand(command, player);
                    break;
                case CONDITIONAL:
                    executeConditionalCommand(command, player);
                    break;
                case DELAYED:
                    executeDelayedCommand(command, player);
                    break;
                case CHANCE:
                    executeChanceCommand(command, player);
                    break;
                default:
                    plugin.getLogger().warning("Unknown command executor type: " + type);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error executing command '" + command + "': " + e.getMessage());
        }
    }
    
    // ==================== BASIC COMMANDS ====================
    
    private void executeConsole(String command, Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
    
    private void executePlayer(String command, Player player) {
        player.performCommand(command);
    }
    
    private void executeAsOp(String command, Player player) {
        boolean wasOp = player.isOp();
        try {
            player.setOp(true);
            player.performCommand(command);
        } finally {
            player.setOp(wasOp);
        }
    }
    
    private void executeBroadcast(String command) {
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', command));
    }
    
    private void executePlayerMessage(String command, Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', command));
    }
    
    // ==================== VISUAL EFFECTS ====================
    
    private void executeTitle(String command, Player player) {
        // Format: Title~Subtitle~FadeIn~Stay~FadeOut
        String[] parts = command.split("~");
        if (parts.length < 2) return;
        
        String title = ChatColor.translateAlternateColorCodes('&', parts[0]);
        String subtitle = ChatColor.translateAlternateColorCodes('&', parts[1]);
        int fadeIn = parts.length > 2 ? Integer.parseInt(parts[2]) : 10;
        int stay = parts.length > 3 ? Integer.parseInt(parts[3]) : 70;
        int fadeOut = parts.length > 4 ? Integer.parseInt(parts[4]) : 20;
        
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
    
    private void executeSubtitle(String command, Player player) {
        player.sendTitle("", ChatColor.translateAlternateColorCodes('&', command), 10, 70, 20);
    }
    
    private void executeActionbar(String command, Player player) {
        player.sendActionBar(ChatColor.translateAlternateColorCodes('&', command));
    }
    
    private void executeSound(String command, Player player) {
        // Format: SOUND_NAME~Volume~Pitch
        String[] parts = command.split("~");
        if (parts.length < 1) return;
        
        try {
            Sound sound = Sound.valueOf(parts[0]);
            float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
            
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + parts[0]);
        }
    }
    
    private void executeParticle(String command, Player player) {
        // Format: PARTICLE_TYPE~Count~Spread
        String[] parts = command.split("~");
        if (parts.length < 1) return;
        
        try {
            Particle particle = Particle.valueOf(parts[0]);
            int count = parts.length > 1 ? Integer.parseInt(parts[1]) : 10;
            double spread = parts.length > 2 ? Double.parseDouble(parts[2]) : 0.5;
            
            Location loc = player.getLocation().add(0, 1, 0);
            player.getWorld().spawnParticle(particle, loc, count, spread, spread, spread);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle: " + parts[0]);
        }
    }
    
    // ==================== ITEM MANAGEMENT ====================
    
    private void executeGiveItem(String command, Player player) {
        // Format: MATERIAL~Amount~DisplayName~Lore1|Lore2
        String[] parts = command.split("~");
        if (parts.length < 1) return;
        
        try {
            Material material = Material.valueOf(parts[0]);
            int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
            
            ItemStack item = new ItemStack(material, amount);
            
            // Apply name and lore if present
            if (parts.length > 2) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String name = ChatColor.translateAlternateColorCodes('&', parts[2]);
                    meta.setDisplayName(name);
                    
                    if (parts.length > 3) {
                        List<String> lore = Arrays.stream(parts[3].split("\\|"))
                            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                            .toList();
                        meta.setLore(lore);
                    }
                    
                    item.setItemMeta(meta);
                }
            }
            
            giveItemSafely(player, item);
            
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material: " + parts[0]);
        }
    }
    
    private void giveItemSafely(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
        
        if (!overflow.isEmpty()) {
            Location loc = player.getLocation();
            for (ItemStack leftover : overflow.values()) {
                player.getWorld().dropItemNaturally(loc, leftover);
            }
            player.sendMessage(ChatColor.YELLOW + "Some items dropped at your feet.");
        }
    }
    
    private void executeTakeItem(String command, Player player) {
        // Format: MATERIAL~Amount
        String[] parts = command.split("~");
        if (parts.length < 1) return;
        
        try {
            Material material = Material.valueOf(parts[0]);
            int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
            
            ItemStack item = new ItemStack(material, amount);
            player.getInventory().removeItem(item);
            
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material: " + parts[0]);
        }
    }
    
    // ==================== PLAYER STATE ====================
    
    private void executeHeal(String command, Player player) {
        if (command.equalsIgnoreCase("FULL")) {
            player.setHealth(player.getMaxHealth());
        } else {
            double amount = Double.parseDouble(command);
            player.setHealth(Math.min(player.getHealth() + amount, player.getMaxHealth()));
        }
    }
    
    private void executeFeed(String command, Player player) {
        if (command.equalsIgnoreCase("FULL")) {
            player.setFoodLevel(20);
            player.setSaturation(20);
        } else {
            int amount = Integer.parseInt(command);
            player.setFoodLevel(Math.min(player.getFoodLevel() + amount, 20));
        }
    }
    
    private void executeDamage(String command, Player player) {
        double amount = Double.parseDouble(command);
        player.damage(amount);
    }
    
    private void executeSetHealth(String command, Player player) {
        double health = Double.parseDouble(command);
        player.setHealth(Math.min(health, player.getMaxHealth()));
    }
    
    private void executeSetHunger(String command, Player player) {
        int hunger = Integer.parseInt(command);
        player.setFoodLevel(Math.min(hunger, 20));
    }
    
    private void executeSetLevel(String command, Player player) {
        int level = Integer.parseInt(command);
        player.setLevel(level);
    }
    
    private void executeGiveExp(String command, Player player) {
        int exp = Integer.parseInt(command);
        player.giveExp(exp);
    }
    
    private void executeTakeExp(String command, Player player) {
        int exp = Integer.parseInt(command);
        int current = player.getTotalExperience();
        player.setTotalExperience(Math.max(0, current - exp));
    }
    
    private void executeEffect(String command, Player player) {
        // Format: EFFECT_TYPE~Duration~Amplifier
        String[] parts = command.split("~");
        if (parts.length < 2) return;
        
        try {
            PotionEffectType effectType = PotionEffectType.getByName(parts[0]);
            if (effectType == null) return;
            
            int duration = Integer.parseInt(parts[1]) * 20; // Convert to ticks
            int amplifier = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            
            PotionEffect effect = new PotionEffect(effectType, duration, amplifier);
            player.addPotionEffect(effect);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid potion effect: " + parts[0]);
        }
    }
    
    private void executeClearEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
    
    // ==================== ADVANCED COMMANDS ====================
    
    private void executeTeleport(String command, Player player) {
        // Format: World~X~Y~Z or X~Y~Z
        String[] parts = command.split("~");
        
        try {
            if (parts.length == 4) {
                // World specified
                World world = Bukkit.getWorld(parts[0]);
                if (world == null) return;
                
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                
                player.teleport(new Location(world, x, y, z));
            } else if (parts.length == 3) {
                // Current world
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                double z = Double.parseDouble(parts[2]);
                
                player.teleport(new Location(player.getWorld(), x, y, z));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid teleport coordinates: " + command);
        }
    }
    
    private void executeLaunch(String command, Player player) {
        double power = Double.parseDouble(command);
        Vector velocity = new Vector(0, power * 0.5, 0);
        player.setVelocity(velocity);
    }
    
    private void executeStrikeLightning(String command, Player player) {
        boolean damage = Boolean.parseBoolean(command);
        
        if (damage) {
            player.getWorld().strikeLightning(player.getLocation());
        } else {
            player.getWorld().strikeLightningEffect(player.getLocation());
        }
    }
    
    private void executeExplosion(String command, Player player) {
        // Format: Power~SetFire~BreakBlocks
        String[] parts = command.split("~");
        
        float power = parts.length > 0 ? Float.parseFloat(parts[0]) : 2.0f;
        boolean setFire = parts.length > 1 ? Boolean.parseBoolean(parts[1]) : false;
        boolean breakBlocks = parts.length > 2 ? Boolean.parseBoolean(parts[2]) : false;
        
        player.getWorld().createExplosion(player.getLocation(), power, setFire, breakBlocks);
    }
    
    private void executeSetFire(String command, Player player) {
        int seconds = Integer.parseInt(command);
        player.setFireTicks(seconds * 20);
    }
    
    private void executeSetGamemode(String command, Player player) {
        try {
            GameMode gamemode = GameMode.valueOf(command.toUpperCase());
            player.setGameMode(gamemode);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid gamemode: " + command);
        }
    }
    
    private void executeCloseInventory(Player player) {
        player.closeInventory();
    }
    
    // ==================== METADATA ====================
    
    private void executeSetMetadata(String command, Player player) {
        // Format: Key~Value
        String[] parts = command.split("~");
        if (parts.length < 2) return;
        
        player.setMetadata(parts[0], new org.bukkit.metadata.FixedMetadataValue(plugin, parts[1]));
    }
    
    private void executeRemoveMetadata(String command, Player player) {
        player.removeMetadata(command, plugin);
    }
    
    // ==================== SPECIAL EXECUTORS ====================
    
    private void executeCommandSeries(String command, Player player) {
        // Format: Command1|Command2|Command3
        String[] commands = command.split("\\|");
        
        for (String cmd : commands) {
            String[] cmdParts = cmd.split(":", 2);
            if (cmdParts.length < 2) continue;
            
            try {
                CommandExecutorType type = CommandExecutorType.valueOf(cmdParts[0].toUpperCase());
                executeCommand(type, cmdParts[1], player);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid command type in series: " + cmdParts[0]);
            }
        }
    }
    
    private void executeRandomCommand(String command, Player player) {
        // Format: Command1|Command2|Command3
        String[] commands = command.split("\\|");
        
        if (commands.length == 0) return;
        
        String randomCmd = commands[new Random().nextInt(commands.length)];
        String[] cmdParts = randomCmd.split(":", 2);
        
        if (cmdParts.length < 2) return;
        
        try {
            CommandExecutorType type = CommandExecutorType.valueOf(cmdParts[0].toUpperCase());
            executeCommand(type, cmdParts[1], player);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid command type in random: " + cmdParts[0]);
        }
    }
    
    private void executeConditionalCommand(String command, Player player) {
        // Format: if:{condition}~then:{command}
        if (!command.startsWith("if:")) return;
        
        String[] parts = command.substring(3).split("~then:");
        if (parts.length < 2) return;
        
        String condition = parts[0];
        String thenCommand = parts[1];
        
        // Simple condition evaluation (supports basic comparisons)
        Map<String, String> vars = new HashMap<>();
        if (evaluateSimpleCondition(condition, vars)) {
            String[] cmdParts = thenCommand.split(":", 2);
            if (cmdParts.length == 2) {
                try {
                    CommandExecutorType type = CommandExecutorType.valueOf(cmdParts[0].toUpperCase());
                    executeCommand(type, cmdParts[1], player);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid command type in conditional: " + cmdParts[0]);
                }
            }
        }
    }
    
    private void executeDelayedCommand(String command, Player player) {
        // Format: Ticks~CommandType:Command
        String[] parts = command.split("~", 2);
        if (parts.length < 2) return;
        
        int ticks = Integer.parseInt(parts[0]);
        String[] cmdParts = parts[1].split(":", 2);
        
        if (cmdParts.length < 2) return;
        
        try {
            CommandExecutorType type = CommandExecutorType.valueOf(cmdParts[0].toUpperCase());
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                executeCommand(type, cmdParts[1], player);
            }, ticks);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid command type in delayed: " + cmdParts[0]);
        }
    }
    
    private void executeChanceCommand(String command, Player player) {
        // Format: Chance~CommandType:Command
        String[] parts = command.split("~", 2);
        if (parts.length < 2) return;
        
        double chance = Double.parseDouble(parts[0]);
        
        if (Math.random() < chance) {
            String[] cmdParts = parts[1].split(":", 2);
            if (cmdParts.length == 2) {
                try {
                    CommandExecutorType type = CommandExecutorType.valueOf(cmdParts[0].toUpperCase());
                    executeCommand(type, cmdParts[1], player);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid command type in chance: " + cmdParts[0]);
                }
            }
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Evaluate a simple condition for conditional commands.
     */
    private boolean evaluateCondition(String condition, Map<String, String> variables) {
        // Replace variables
        String expr = condition;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            expr = expr.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return evaluateSimpleCondition(expr, variables);
    }
    
    /**
     * Evaluate simple comparison expressions.
     */
    private boolean evaluateSimpleCondition(String expr, Map<String, String> variables) {
        try {
            if (expr.contains(">=")) {
                String[] parts = expr.split(">=");
                return Double.parseDouble(parts[0].trim()) >= Double.parseDouble(parts[1].trim());
            } else if (expr.contains("<=")) {
                String[] parts = expr.split("<=");
                return Double.parseDouble(parts[0].trim()) <= Double.parseDouble(parts[1].trim());
            } else if (expr.contains(">")) {
                String[] parts = expr.split(">");
                return Double.parseDouble(parts[0].trim()) > Double.parseDouble(parts[1].trim());
            } else if (expr.contains("<")) {
                String[] parts = expr.split("<");
                return Double.parseDouble(parts[0].trim()) < Double.parseDouble(parts[1].trim());
            } else if (expr.contains("==")) {
                String[] parts = expr.split("==");
                return parts[0].trim().equals(parts[1].trim());
            } else if (expr.contains("!=")) {
                String[] parts = expr.split("!=");
                return !parts[0].trim().equals(parts[1].trim());
            } else if (expr.contains("%")) {
                // Modulo operation for checking divisibility
                String[] parts = expr.split("%");
                if (parts.length == 2 && parts[1].contains("==")) {
                    String[] modParts = parts[1].split("==");
                    int dividend = Integer.parseInt(parts[0].trim());
                    int divisor = Integer.parseInt(modParts[0].trim());
                    int expected = Integer.parseInt(modParts[1].trim());
                    return (dividend % divisor) == expected;
                }
            }
        } catch (Exception e) {
            // If evaluation fails, return false
            return false;
        }
        
        return false;
    }
}
