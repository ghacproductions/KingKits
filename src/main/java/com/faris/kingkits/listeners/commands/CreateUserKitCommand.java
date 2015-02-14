package com.faris.kingkits.listeners.commands;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.helpers.Lang;
import com.faris.kingkits.helpers.Utils;
import com.faris.kingkits.listeners.PlayerCommand;
import com.faris.kingkits.listeners.event.custom.PlayerCreateKitEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateUserKitCommand extends PlayerCommand {

    public CreateUserKitCommand(KingKits instance) {
        super(instance);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean onCommand(Player p, String command, String[] args) {
        if (command.equalsIgnoreCase("createukit")) {
            if (p.hasPermission(this.getPlugin().permissions.kitUCreateCommand)) {
                if (this.getPlugin().cmdValues.createUKits) {
                    if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(p.getWorld().getName())) {
                        if (args.length == 0) {
                            Lang.sendMessage(p, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<kit>|<kit> <guiitem>]");
                            Lang.sendMessage(p, Lang.COMMAND_CREATE_UKIT_DESCRIPTION);
                        } else if (args.length > 0 && args.length < 3) {
                            String kitName = args[0];

                            boolean containsRealKit = this.getPlugin().getKitsConfig().contains(kitName);
                            if (!containsRealKit) {
                                List<String> currentKits = this.getPlugin().getKitList();
                                List<String> currentKitsLC = Utils.toLowerCaseList(currentKits);
                                containsRealKit = currentKitsLC.contains(kitName.toLowerCase());
                            }

                            if (!containsRealKit) {
                                List<String> currentKits = this.getPlugin().getKitList(p.getName());
                                boolean containsKit = this.getPlugin().getUserKitsConfig().contains(p.getName() + "." + kitName);
                                if (!containsKit) {
                                    List<String> currentKitsLC = Utils.toLowerCaseList(currentKits);
                                    if (currentKitsLC.contains(kitName.toLowerCase()))
                                        kitName = currentKits.get(currentKitsLC.indexOf(kitName.toLowerCase()));
                                    containsKit = currentKits.contains(kitName);
                                }

                                if (!this.containsIllegalCharacters(kitName)) {
                                    List<Kit> playerKits = this.getPlugin().userKitList.get(p.getName());
                                    if (playerKits == null) playerKits = new ArrayList<Kit>();
                                    int maxSizePerm = 0;
                                    for (int i = 1; i <= 54; i++) {
                                        if (maxSizePerm < i && p.hasPermission("kingkits.kit.limit." + i))
                                            maxSizePerm = i;
                                    }
                                    if (maxSizePerm > playerKits.size()) {
                                        if (args.length == 2) {
                                            if (args[1].contains(":")) {
                                                String[] guiSplit = args[1].split(":");
                                                if (guiSplit.length == 2) {
                                                    if (!this.isInteger(guiSplit[0]) || !this.isInteger(guiSplit[1])) {
                                                        Lang.sendMessage(p, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<kit>|<kit> <guiitem>]");
                                                        return true;
                                                    }
                                                } else {
                                                    if (!this.isInteger(args[1])) {
                                                        Lang.sendMessage(p, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<kit>|<kit> <guiitem>]");
                                                        return true;
                                                    }
                                                }
                                            } else {
                                                if (!this.isInteger(args[1])) {
                                                    Lang.sendMessage(p, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<kit>|<kit> <guiitem>]");
                                                    return true;
                                                }
                                            }
                                        }

                                        Map<Integer, ItemStack> itemsInInv = new HashMap<Integer, ItemStack>();
                                        List<ItemStack> armourInInv = new ArrayList<ItemStack>();
                                        ItemStack[] pContents = p.getInventory().getContents();
                                        if (pContents == null) pContents = new ItemStack[p.getInventory().getSize()];
                                        for (int i = 0; i < p.getInventory().getSize(); i++) {
                                            if (pContents.length > i && pContents[i] != null)
                                                itemsInInv.put(i, pContents[i]);
                                            else itemsInInv.put(i, new ItemStack(Material.AIR));
                                        }
                                        for (ItemStack armour : p.getInventory().getArmorContents())
                                            if (armour != null && armour.getType() != Material.AIR)
                                                armourInInv.add(armour);
                                        PlayerCreateKitEvent createKitEvent = new PlayerCreateKitEvent(p, kitName, itemsInInv, armourInInv, true);
                                        p.getServer().getPluginManager().callEvent(createKitEvent);

                                        if (!createKitEvent.isCancelled()) {
                                            itemsInInv = createKitEvent.getKitContentsWithSlots();
                                            armourInInv = createKitEvent.getKitArmour();
                                            if (itemsInInv.size() > 0 || armourInInv.size() > 0) {
                                                if (containsKit) {
                                                    this.getPlugin().getUserKitsConfig().set(p.getName() + "." + kitName, null);
                                                    this.getPlugin().saveUserKitsConfig();
                                                    if (playerKits != null) {
                                                        List<Kit> newKits = new ArrayList<Kit>();
                                                        for (Kit playerKit : playerKits) {
                                                            if (playerKit != null && !playerKit.getRealName().toLowerCase().equals(kitName.toLowerCase()))
                                                                newKits.add(playerKit);
                                                        }
                                                        this.getPlugin().userKitList.put(p.getName(), newKits);
                                                    }
                                                }

                                                final Kit kit = new Kit(kitName, itemsInInv).setRealName(kitName).setArmour(armourInInv).setUserKit(true);
                                                if (args.length == 2) {
                                                    ItemStack guiItem = null;
                                                    try {
                                                        guiItem = new ItemStack(Integer.parseInt(args[1]));
                                                    } catch (Exception ex) {
                                                    }
                                                    try {
                                                        if (args[1].contains(":")) {
                                                            String[] guiSplit = args[1].split(":");
                                                            guiItem = new ItemStack(Integer.parseInt(guiSplit[0]));
                                                            guiItem.setDurability(Short.parseShort(guiSplit[1]));
                                                        }
                                                    } catch (Exception ex) {
                                                    }
                                                    if (guiItem != null) {
                                                        if (guiItem.getType() != Material.AIR) {
                                                            kit.setGuiItem(guiItem);
                                                        }
                                                    }
                                                }

                                                List<PotionEffect> kitPotionEffects = new ArrayList<PotionEffect>();
                                                for (PotionEffect potionEffect : p.getActivePotionEffects()) {
                                                    if (potionEffect != null) kitPotionEffects.add(potionEffect);
                                                }
                                                if (!kitPotionEffects.isEmpty()) kit.setPotionEffects(kitPotionEffects);
                                                kit.setMaxHealth((int) p.getMaxHealth());

                                                this.getPlugin().getUserKitsConfig().set(p.getName() + "." + kitName, kit.serialize());
                                                if (playerKits == null) playerKits = new ArrayList<Kit>();
                                                playerKits.add(kit);
                                                this.getPlugin().userKitList.put(p.getName(), playerKits);
                                                this.getPlugin().saveUserKitsConfig();

                                                Lang.sendMessage(p, containsKit ? Lang.COMMAND_CREATE_OVERWRITTEN : Lang.COMMAND_CREATE_CREATED, kitName);

                                                if (this.getPlugin().configValues.removeItemsOnCreateKit) {
                                                    p.getInventory().clear();
                                                    p.getInventory().setArmorContents(null);
                                                }
                                            } else {
                                                Lang.sendMessage(p, Lang.COMMAND_CREATE_EMPTY_INV);
                                            }
                                        } else {
                                            Lang.sendMessage(p, Lang.COMMAND_CREATE_DENIED);
                                        }
                                    } else {
                                        Lang.sendMessage(p, Lang.COMMAND_CREATE_UKIT_MAX_PERSONAL_KITS);
                                    }
                                } else {
                                    Lang.sendMessage(p, Lang.COMMAND_CREATE_ILLEGAL_CHARACTERS);
                                }
                            } else {
                                Lang.sendMessage(p, Lang.COMMAND_CREATE_UKIT_EXISTS);
                            }
                        } else {
                            Lang.sendMessage(p, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<kit>|<kit> <guiitem>]");
                        }
                    } else {
                        Lang.sendMessage(p, Lang.COMMAND_GEN_WORLD);
                    }
                } else {
                    Lang.sendMessage(p, Lang.COMMAND_GEN_DISABLED);
                }
            } else {
                this.sendNoAccess(p);
            }
            return true;
        }
        return false;
    }

}
