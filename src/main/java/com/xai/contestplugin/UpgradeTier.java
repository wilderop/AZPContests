package com.xai.contestplugin;

import org.bukkit.inventory.ItemStack;

public class UpgradeTier {
    private String name;
    private String checkLore;
    private int required;
    private ItemStack give;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCheckLore() { return checkLore; }
    public void setCheckLore(String checkLore) { this.checkLore = checkLore; }
    public int getRequired() { return required; }
    public void setRequired(int required) { this.required = required; }
    public ItemStack getGive() { return give; }
    public void setGive(ItemStack give) { this.give = give; }
}
