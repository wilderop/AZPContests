package com.xai.contestplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private ContestManager contestManager;
    private DatabaseManager dbManager;

    @Override
    public void onEnable() {
        // Save default configs
        saveDefaultConfig();
        saveResource("contests.yml", false);

        dbManager = new DatabaseManager(this);
        dbManager.init();
        
        // Check if database initialized successfully
        if (!dbManager.isConnected()) {
            getLogger().severe("===========================================");
            getLogger().severe("DATABASE INITIALIZATION FAILED!");
            getLogger().severe("Plugin cannot function without database.");
            getLogger().severe("Please check file permissions and restart.");
            getLogger().severe("===========================================");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        contestManager = new ContestManager(this, dbManager);
        contestManager.loadContests();

        // Check for and resume any active contest from before server restart
        contestManager.checkAndResumeContest();

        getCommand("contest").setExecutor(new ContestCommand(contestManager));
        getCommand("contest").setTabCompleter(new ContestTabCompleter(contestManager));

        getServer().getPluginManager().registerEvents(new PlayerListener(contestManager), this);

        contestManager.startScheduler();
    }

    @Override
    public void onDisable() {
        // Save contest state if running (for resumption after restart)
        if (contestManager != null && contestManager.isContestRunning()) {
            contestManager.saveContestStateOnShutdown();
        }
        
        // Shutdown contest manager (cancel tasks, save attribution data)
        if (contestManager != null) {
            contestManager.shutdown();
        }
        
        if (dbManager != null) {
            dbManager.close();
        }
    }
}
