package com.xai.contestplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContestTabCompleter implements TabCompleter {

    private final ContestManager manager;

    public ContestTabCompleter(ContestManager manager) {
        this.manager = manager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - show all available commands
            boolean isAdmin = sender.isOp() || sender.hasPermission("azpcontests.admin");
            
            // Player commands
            completions.add("join");
            completions.add("leave");
            completions.add("list");
            completions.add("info");
            completions.add("leaderboard");
            completions.add("history");
            
            // Custom upgrade commands
            for (Contest c : manager.getContests().values()) {
                if (c.getUpgradeCommand() != null && !c.getUpgradeCommand().isEmpty()) {
                    completions.add(c.getUpgradeCommand());
                }
            }
            
            // Admin commands
            if (isAdmin) {
                completions.add("start");
                completions.add("end");
                completions.add("reload");
                completions.add("debug");
                completions.add("testprize");
            }
            
            // Filter based on what user has typed
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Second argument - context-sensitive completions
            String subCommand = args[0].toLowerCase();
            
            // Confirmation for leave command
            if (subCommand.equals("confirmleave")) {
                completions.add("yes");
                completions.add("no");
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            
            // Commands that need a contest name
            if (subCommand.equals("info") || subCommand.equals("leaderboard") || 
                subCommand.equals("history") || subCommand.equals("start") || 
                subCommand.equals("testprize")) {
                
                // For admin commands, only show to admins
                if ((subCommand.equals("start") || subCommand.equals("testprize")) && 
                    !sender.isOp() && !sender.hasPermission("azpcontests.admin")) {
                    return completions;
                }
                
                // Suggest contest names
                completions.addAll(manager.getContests().keySet());
                
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            // Third argument - for commands with optional limits or tiers
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("leaderboard") || subCommand.equals("history")) {
                // Suggest common limits
                completions.add("5");
                completions.add("10");
                completions.add("25");
                completions.add("50");
                
                return completions.stream()
                        .filter(s -> s.startsWith(args[2]))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("testprize")) {
                // Suggest tier names for the selected contest
                if (!sender.isOp() && !sender.hasPermission("azpcontests.admin")) {
                    return completions;
                }
                
                String contestName = args[1];
                Contest contest = manager.getContests().get(contestName);
                
                if (contest != null) {
                    // Add "winner" option
                    completions.add("winner");
                    
                    // Add all tier names from the contest
                    for (UpgradeTier tier : contest.getUpgrades()) {
                        completions.add(tier.getName());
                    }
                    
                    return completions.stream()
                            .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                            .sorted()
                            .collect(Collectors.toList());
                }
            }
        }
        
        return completions;
    }
}
