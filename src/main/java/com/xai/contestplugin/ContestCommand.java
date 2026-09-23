package com.xai.contestplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ContestCommand implements CommandExecutor {

    private final ContestManager manager;

    public ContestCommand(ContestManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            boolean isAdmin = sender.isOp() || sender.hasPermission("azpcontests.admin");
            
            sender.sendMessage("§6§lAZP Contest Commands");
            sender.sendMessage("§e/contest join §7- Join the current contest");
            sender.sendMessage("§e/contest leave §7- Leave the current contest");
            sender.sendMessage("§e/contest list §7- List all available contests");
            sender.sendMessage("§e/contest info <n> §7- Show contest details");
            sender.sendMessage("§e/contest leaderboard <n> §7- View top 10 winners");
            sender.sendMessage("§e/contest history <n> §7- View last 10 contests");
            sender.sendMessage("§e/contest reimburse §7- Trade vouchers for copper picks");
            
            // Show custom upgrade commands for contests that have them
            for (Contest c : manager.getContests().values()) {
                if (c.getUpgradeCommand() != null && !c.getUpgradeCommand().isEmpty()) {
                    sender.sendMessage("§e/contest " + c.getUpgradeCommand() + " §7- Upgrade " + c.getName() + " prize");
                }
            }
            
            if (isAdmin) {
                sender.sendMessage("§c§lAdmin Commands:");
                sender.sendMessage("§e/contest start <n> §7- Start a contest");
                sender.sendMessage("§e/contest end §7- End current contest");
                sender.sendMessage("§e/contest reload §7- Reload config");
                sender.sendMessage("§e/contest debug §7- Toggle debug logging");
                sender.sendMessage("§e/contest testprize <contest> <tier> §7- Test prize items");
                sender.sendMessage("§e/contest voucherstatus §7- Check first winner status");
            }
            return true;
        }
        
        String sub = args[0].toLowerCase();
        
        if (sub.equals("join")) {
            if (sender instanceof Player p) manager.join(p);
        } else if (sub.equals("leave")) {
            if (sender instanceof Player p) manager.leave(p);
        } else if (sub.equals("confirmleave")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("Player only command.");
                return true;
            }
            if (args.length < 2) {
                p.sendMessage("§cUse the clickable buttons or type: §e/contest confirmleave yes §cor §e/contest confirmleave no");
                return true;
            }
            boolean confirmed = args[1].equalsIgnoreCase("yes");
            manager.confirmLeave(p, confirmed);
        } else if (sub.equals("list")) {
            sender.sendMessage("");
            sender.sendMessage("§6§lAvailable Contests:");
            sender.sendMessage("");
            
            // Get current contest name
            String currentContestName = null;
            Contest currentContest = manager.getCurrentContest();
            if (currentContest != null) {
                currentContestName = currentContest.getName();
            }
            
            // Get all contest names and sort alphabetically
            List<String> contestNames = new ArrayList<>(manager.getContests().keySet());
            contestNames.sort(String.CASE_INSENSITIVE_ORDER);
            
            // Display each contest
            for (String name : contestNames) {
                if (name.equals(currentContestName)) {
                    sender.sendMessage("  §a" + name + " §7(active)");
                } else {
                    sender.sendMessage("  §e" + name);
                }
            }
            
            sender.sendMessage("");
            sender.sendMessage("§7Use §e/contest info <name> §7for details");
            sender.sendMessage("");
        } else if (sub.equals("info")) {
            if (args.length < 2) {
                sender.sendMessage("§e/contest info <contest_name>");
                sender.sendMessage("§7Example: /contest info cobble");
                return true;
            }
            Contest c = manager.getContests().get(args[1]);
            if (c == null) {
                sender.sendMessage("§cNo such contest: " + args[1]);
                sender.sendMessage("§7Use §e/contest list §7to see available contests.");
                return true;
            }
            
            sender.sendMessage("");
            sender.sendMessage("§b" + c.getName().toUpperCase() + " CONTEST");
            sender.sendMessage("");
            
            sender.sendMessage("§aWHAT TO DO:");
            // Use custom description if provided, otherwise generate from contest type
            if (c.getDescription() != null && !c.getDescription().isEmpty()) {
                sender.sendMessage("  " + c.getDescription());
            } else if (c.isRuleBased()) {
                // Default message for rule-based contests without custom description
                sender.sendMessage("  Complete actions to earn points");
            } else {
                // Generate description from statistic type for old contests
                String action = getActionDescription(c.getStatType(), c.getStatKey());
                sender.sendMessage("  " + action);
            }
            sender.sendMessage("");
            
            sender.sendMessage("§aWHERE:");
            sender.sendMessage("  World: " + c.getAreaWorld().getName());
            sender.sendMessage("  X: " + c.getMinX() + " to " + c.getMaxX());
            sender.sendMessage("  Y: " + c.getMinY() + " to " + c.getMaxY());
            sender.sendMessage("  Z: " + c.getMinZ() + " to " + c.getMaxZ());
            sender.sendMessage("");
            
            sender.sendMessage("§aDURATION:");
            sender.sendMessage("  " + c.getDurationMinutes() + " minutes");
            sender.sendMessage("");
            
            sender.sendMessage("§aHOW TO JOIN:");
            sender.sendMessage("  Type: /contest join");
            sender.sendMessage("");
            
            sender.sendMessage("§aPRIZE:");
            if (c.getWinnerPrize() != null) {
                String prizeName = c.getWinnerPrize().getItemMeta() != null && c.getWinnerPrize().getItemMeta().hasDisplayName() 
                    ? c.getWinnerPrize().getItemMeta().getDisplayName() 
                    : c.getWinnerPrize().getType().toString();
                sender.sendMessage("  " + prizeName);
            } else {
                sender.sendMessage("  No prize configured");
            }
            
            if (c.getUpgrades() != null && !c.getUpgrades().isEmpty()) {
                sender.sendMessage("  Can be upgraded! Use: /contest " + c.getUpgradeCommand());
            }
            sender.sendMessage("");
            
            if (manager.isContestRunning() && manager.getCurrentContest().getName().equals(c.getName())) {
                sender.sendMessage("§aSTATUS: CONTEST IS CURRENTLY ACTIVE!");
            } else {
                sender.sendMessage("§7STATUS: Contest is not currently running");
            }
            sender.sendMessage("");
            
        } else if (sub.equals("leaderboard")) {
            if (args.length < 2) {
                sender.sendMessage("§e/contest leaderboard <contest>");
                return true;
            }
            
            String contestName = args[1];
            Contest contest = manager.getContests().get(contestName);
            if (contest == null) {
                sender.sendMessage("§cNo such contest: " + contestName);
                return true;
            }
            
            List<Map.Entry<UUID, Integer>> topWinners = manager.getDb().getTopWinners(contestName, 10);
            
            sender.sendMessage("");
            sender.sendMessage("§6§lTop 10 Winners - " + contestName + " contest");
            sender.sendMessage("");
            
            if (topWinners.isEmpty()) {
                sender.sendMessage("§7No one has won this contest yet.");
            } else {
                int rank = 1;
                for (Map.Entry<UUID, Integer> entry : topWinners) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(entry.getKey());
                    int wins = entry.getValue();
                    String winText = wins == 1 ? "win" : "wins";
                    sender.sendMessage("§e" + rank + ". §f" + p.getName() + "§7: §a" + wins + " " + winText);
                    rank++;
                }
            }
            
            sender.sendMessage("");
        } else if (sub.equals("history")) {
            if (args.length < 2) {
                sender.sendMessage("§e/contest history <contest>");
                return true;
            }
            
            String contestName = args[1];
            Contest contest = manager.getContests().get(contestName);
            if (contest == null) {
                sender.sendMessage("§cNo such contest: " + contestName);
                return true;
            }
            
            ResultSet rs2 = manager.getDb().getHistory(contestName, 10);
            if (rs2 == null) {
                sender.sendMessage("§cError fetching history.");
                return true;
            }
            
            sender.sendMessage("");
            sender.sendMessage("§aLast 10 winners - " + contestName);
            sender.sendMessage("");
            
            try {
                int count = 1;
                boolean hasResults = false;
                while (rs2.next() && count <= 10) {
                    hasResults = true;
                    String winners = rs2.getString("winners");
                    sender.sendMessage(count + ". Winners: " + winners);
                    count++;
                }
                
                if (!hasResults) {
                    sender.sendMessage("§7No history data yet for this contest.");
                }
                
                sender.sendMessage("");
            } catch (SQLException e) {
                sender.sendMessage("§cError: " + e.getMessage());
            }
        } else if (sub.equals("start")) {
            if (args.length < 2) {
                sender.sendMessage("/contest start <n>");
                return true;
            }
            manager.startContest(args[1]);
        } else if (sub.equals("end")) {
            manager.endContest();
        } else if (sub.equals("reload")) {
            manager.loadContests();
            sender.sendMessage("Reloaded contests.");
        } else if (sub.equals("debug")) {
            // Admin/Console only - toggle debug mode
            if (!(sender.isOp() || sender.hasPermission("azpcontests.admin") || !(sender instanceof Player))) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }
            
            boolean newState = !ContestManager.isDebugMode();
            ContestManager.setDebugMode(newState);
            
            String status = newState ? "§aENABLED" : "§cDISABLED";
            sender.sendMessage("§6[Contest Debug] " + status);
            
            if (newState) {
                sender.sendMessage("§7Debug logging is now active. Check console for detailed event logs.");
            } else {
                sender.sendMessage("§7Debug logging is now disabled.");
            }
        } else if (sub.equals("testprize")) {
            // Admin only - test prize items
            if (!sender.isOp() && !sender.hasPermission("azpcontests.admin")) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }
            if (!(sender instanceof Player p)) {
                sender.sendMessage("Player only.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage("§e/contest testprize <contest> <tier|winner>");
                sender.sendMessage("§7Example: /contest testprize cobble tier1");
                sender.sendMessage("§7Example: /contest testprize cobble winner");
                return true;
            }
            
            String contestName = args[1];
            String tierName = args[2];
            
            Contest contest = manager.getContests().get(contestName);
            if (contest == null) {
                sender.sendMessage("§cContest not found: " + contestName);
                return true;
            }
            
            ItemStack item = null;
            
            if (tierName.equalsIgnoreCase("winner")) {
                // Give winner prize
                item = contest.getWinnerPrize();
                if (item != null) {
                    giveItemSafely(p, item);
                    sender.sendMessage("§aGave you the winner prize for " + contestName + "!");
                } else {
                    sender.sendMessage("§cNo winner prize configured for " + contestName);
                }
            } else {
                // Give upgrade tier prize
                UpgradeTier tier = null;
                for (UpgradeTier t : contest.getUpgrades()) {
                    if (t.getName().equalsIgnoreCase(tierName)) {
                        tier = t;
                        break;
                    }
                }
                
                if (tier != null) {
                    item = tier.getGive();
                    if (item != null) {
                        giveItemSafely(p, item);
                        sender.sendMessage("§aGave you " + tierName + " prize for " + contestName + "!");
                    } else {
                        sender.sendMessage("§cNo item configured for " + tierName + " in " + contestName);
                    }
                } else {
                    sender.sendMessage("§cTier not found: " + tierName);
                    sender.sendMessage("§7Available tiers: " + contest.getUpgrades().stream()
                        .map(UpgradeTier::getName).collect(Collectors.joining(", ")));
                }
            }
        } else if (sub.equals("voucherstatus")) {
            // Admin only - check voucher first winner status
            if (!sender.isOp() && !sender.hasPermission("azpcontests.admin")) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }
            
            DatabaseManager.FirstWinner firstWinner = manager.getDb().getFirstWinner("cobble", "tempvoucher");
            
            sender.sendMessage("");
            sender.sendMessage("§aVOUCHER SYSTEM STATUS");
            sender.sendMessage("");
            
            if (firstWinner == null) {
                sender.sendMessage("§7Status: §eWAITING FOR FIRST WINNER");
                sender.sendMessage("§7No one has traded 30 vouchers yet.");
            } else {
                sender.sendMessage("§7Status: §6VOUCHER TIER LOCKED");
                sender.sendMessage("§7First Winner: §a" + firstWinner.playerName);
                sender.sendMessage("§7Won on: §f" + new java.util.Date(firstWinner.timestamp));
                sender.sendMessage("");
                sender.sendMessage("§7Tempvoucher tier is locked until pickaxe is configured.");
                sender.sendMessage("§7All other tiers work normally.");
            }
            sender.sendMessage("");
            
        } else if (sub.equals("reimburse")) {
            // Player command - trade 1 voucher for 1 copper pick (after first winner)
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Player only.");
                return true;
            }
            
            // Check if first winner exists (tempvoucher must be locked)
            DatabaseManager.FirstWinner firstWinner = manager.getDb().getFirstWinner("cobble", "tempvoucher");
            if (firstWinner == null) {
                player.sendMessage("§cReimbursement not available yet.");
                player.sendMessage("§7Wait for the first tempvoucher winner to be announced.");
                return true;
            }
            
            // Get the tempvoucher copper pickaxe from cobble contest
            Contest cobbleContest = manager.getContests().get("cobble");
            if (cobbleContest == null) {
                player.sendMessage("§cCobble contest not found!");
                return true;
            }
            
            ItemStack copperPick = null;
            for (UpgradeTier tier : cobbleContest.getUpgrades()) {
                if (tier.getName().equals("tempvoucher")) {
                    copperPick = tier.getGive();
                    break;
                }
            }
            
            if (copperPick == null) {
                player.sendMessage("§cCopper pickaxe not configured!");
                return true;
            }
            
            // Count vouchers in inventory (check for tier1 lore)
            int voucherCount = 0;
            List<Integer> voucherSlots = new ArrayList<>();
            ItemStack[] inv = player.getInventory().getContents();
            for (int i = 0; i < inv.length; i++) {
                ItemStack item = inv[i];
                if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                    List<String> lore = item.getItemMeta().getLore();
                    if (lore.stream().anyMatch(l -> l.contains("Be the first to collect 30"))) {
                        voucherCount += item.getAmount();
                        voucherSlots.add(i);
                    }
                }
            }
            
            if (voucherCount == 0) {
                player.sendMessage("§cYou don't have any vouchers!");
                player.sendMessage("§7Trade 10 stone pickaxes for vouchers first.");
                return true;
            }
            
            // Remove 1 voucher per copper pick
            int picksToGive = voucherCount;
            int toRemove = voucherCount;
            
            for (int slot : voucherSlots) {
                if (toRemove == 0) break;
                ItemStack item = player.getInventory().getItem(slot);
                int amt = item.getAmount();
                if (amt <= toRemove) {
                    player.getInventory().setItem(slot, null);
                    toRemove -= amt;
                } else {
                    item.setAmount(amt - toRemove);
                    toRemove = 0;
                }
            }
            
            // Give copper picks
            for (int i = 0; i < picksToGive; i++) {
                giveItemSafely(player, copperPick.clone());
            }
            
            player.sendMessage("");
            player.sendMessage("§aREIMBURSEMENT COMPLETE");
            player.sendMessage("§7Traded §e" + picksToGive + " §7voucher(s) for");
            player.sendMessage("§7§e" + picksToGive + " §bcopper pickaxe(s)§7!");
            player.sendMessage("");
            
        } else {
            // Check if it's a custom upgrade command (smart detection)
            Contest contest = manager.getContestByUpgradeCommand(sub);
            if (contest != null) {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage("Player only.");
                    return true;
                }
                // Use smart upgrade - detects which contest prize they have
                manager.smartUpgradePrize(p, sub);
            } else {
                sender.sendMessage("Unknown command. Use /contest for help.");
            }
        }
        return true;
    }
    
    /**
     * Safely give item to player. If inventory is full, drops overflow on ground.
     */
    private void giveItemSafely(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
        
        if (!overflow.isEmpty()) {
            Location loc = player.getLocation();
            for (ItemStack leftover : overflow.values()) {
                player.getWorld().dropItemNaturally(loc, leftover);
            }
            player.sendMessage("§eInventory full! Some items dropped at your feet.");
        }
    }

    private String getActionDescription(org.bukkit.Statistic statType, Object statKey) {
        String action = "";
        switch (statType) {
            case MINE_BLOCK:
                action = "Mine " + (statKey != null ? statKey.toString().toLowerCase().replace("_", " ") : "blocks");
                break;
            case USE_ITEM:
                action = "Use " + (statKey != null ? statKey.toString().toLowerCase().replace("_", " ") : "items");
                break;
            case KILL_ENTITY:
                action = "Kill " + (statKey != null ? statKey.toString().toLowerCase().replace("_", " ") : "entities");
                break;
            case FISH_CAUGHT:
                action = "Catch fish";
                break;
            case CRAFT_ITEM:
                action = "Craft " + (statKey != null ? statKey.toString().toLowerCase().replace("_", " ") : "items");
                break;
            case BREAK_ITEM:
                action = "Break " + (statKey != null ? statKey.toString().toLowerCase().replace("_", " ") : "items");
                break;
            case DEATHS:
                action = "Die (yes, really)";
                break;
            default:
                action = statType.toString() + (statKey != null ? " " + statKey.toString() : "");
        }
        return action;
    }
}
