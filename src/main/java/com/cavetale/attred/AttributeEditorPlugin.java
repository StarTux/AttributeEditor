package com.cavetale.attred;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class AttributeEditorPlugin extends JavaPlugin {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player expected");
            return true;
        }
        if (args.length == 0) return false;
        Player player = (Player)sender;
        switch (args[0]) {
        case "list":
            if (args.length == 1) {
                ItemStack item = player.getInventory().getItemInHand();
                if (item == null || item.getType() == Material.AIR) {
                    sender.sendMessage(ChatColor.RED + "No item in hand");
                    return true;
                }
                List<ItemAttribute> list = ItemAttribute.of(item);
                String pl = list.size() == 1 ? "" : "s";
                player.sendMessage(ChatColor.YELLOW + "" + list.size() + " attribute" + pl + " set on " + item.getType().name().toLowerCase().replace("_", " "));
                for (ItemAttribute inst: list) {
                    player.sendMessage(ChatColor.GRAY + "+ " + ChatColor.WHITE + inst.getAttributeName() + ChatColor.GRAY + " " + inst.getOperationSymbol() + ChatColor.WHITE + String.format("%.02f", inst.getAmount()) + ChatColor.GRAY + " (" + inst.getSlotName() + ")");
                }
                if (list.isEmpty()) {
                    list = ItemAttribute.defaultsOf(item);
                    pl = list.size() == 1 ? "" : "s";
                    player.sendMessage("" + ChatColor.YELLOW + ChatColor.ITALIC + list.size() + " default attribute" + pl + " on " + item.getType().name().toLowerCase().replace("_", " "));
                    for (ItemAttribute inst: list) {
                        player.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + inst.getAttributeName() + ChatColor.GRAY + " " + inst.getOperationSymbol() + ChatColor.WHITE + String.format("%.02f", inst.getAmount()) + ChatColor.GRAY + " (" + inst.getSlotName() + ")");
                    }
                }
                return true;
            }
            break;
        case "add":
            if (args.length >= 3) {
                ItemStack item = player.getInventory().getItemInHand();
                if (item == null || item.getType() == Material.AIR) {
                    sender.sendMessage(ChatColor.RED + "No item in hand");
                    return true;
                }
                Attribute attribute = ItemAttribute.attributeOf(args[1]);
                if (attribute == null) {
                    try {
                        attribute = Attribute.valueOf(args[1]);
                    } catch (IllegalArgumentException iae) {
                        player.sendMessage(ChatColor.RED + "Unknown attribute: " + args[1]);
                        return true;
                    }
                }
                double amount;
                try {
                    amount = Double.parseDouble(args[2]);
                } catch (NumberFormatException nfe) {
                    sender.sendMessage(ChatColor.RED + "Value expected: " + args[2]);
                    return true;
                }
                Operation operation = Operation.ADD_NUMBER;
                long uuidMost = ThreadLocalRandom.current().nextLong();
                long uuidLeast = ThreadLocalRandom.current().nextLong();
                String name = ItemAttribute.attributeNameOf(attribute);
                EquipmentSlot slot = null;
                for (int i = 3; i < args.length; i += 1) {
                    String arg = args[i];
                    String[] toks = arg.split("=", 2);
                    if (toks.length != 2 || toks[0].isEmpty() || toks[1].isEmpty()) {
                        player.sendMessage(ChatColor.RED + "Invalid argument: " + arg);
                        return true;
                    }
                    String key = toks[0];
                    String val = toks[1];
                    switch (key) {
                    case "op": case "operation":
                        try {
                            int num = Integer.parseInt(val);
                            operation = ItemAttribute.operationOf(num);
                            break;
                        } catch (NumberFormatException nfe) {
                            // Continue trying with literal argument
                        } catch (IllegalArgumentException iae) {
                            player.sendMessage(ChatColor.RED + iae.getMessage());
                            return true;
                        }
                        try {
                            operation = Operation.valueOf(val.toUpperCase());
                            break;
                        } catch (IllegalArgumentException iae) {
                            player.sendMessage(ChatColor.RED + "Invalid operation: " + val);
                            return true;
                        }
                    case "slot":
                        try {
                            slot = ItemAttribute.slotOf(val);
                            break;
                        } catch (IllegalArgumentException iae) { }
                        try {
                            slot = EquipmentSlot.valueOf(val);
                        } catch (IllegalArgumentException iae) {
                            player.sendMessage(ChatColor.RED + "Invalid slot: " + val);
                            return true;
                        }
                        break;
                    case "name":
                        name = val;
                        break;
                    case "uuid":
                        try {
                            UUID uuid = UUID.fromString(val);
                            uuidMost = uuid.getMostSignificantBits();
                            uuidLeast = uuid.getLeastSignificantBits();
                        } catch (IllegalArgumentException iae) {
                            player.sendMessage(ChatColor.RED + "Invalid UUID: " + val);
                            return true;
                        }
                        break;
                    case "uuidLeast":
                        try {
                            uuidLeast = Long.parseLong(val);
                        } catch (IllegalArgumentException iae) {
                            player.sendMessage(ChatColor.RED + "Invalid number: " + val);
                            return true;
                        }
                        break;
                    case "uuidMost":
                        try {
                            uuidMost = Long.parseLong(val);
                        } catch (IllegalArgumentException iae) {
                            player.sendMessage(ChatColor.RED + "Invalid number: " + val);
                            return true;
                        }
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + "Invalid argument: " + arg);
                        return true;
                    }
                }
                UUID attrid = new UUID(uuidMost, uuidLeast);
                if (slot == null) slot = ItemAttribute.guessEquipmentSlotOf(item);
                ItemAttribute itemAttribute = new ItemAttribute(attribute, slot, name, amount, operation, attrid);
                item = itemAttribute.addTo(item);
                if (item != item) player.getInventory().setItemInHand(item);
                player.sendMessage(ChatColor.BLUE + "Attribute " + itemAttribute.getAttributeName() + itemAttribute.getOperationSymbol() + String.format("%.02f", itemAttribute.getAmount()) + " added to " + item.getType().name().toLowerCase().replace("_", " "));
                return true;
            }
        case "remove":
            if (args.length == 1) {
                ItemStack item = player.getInventory().getItemInHand();
                if (item == null || item.getType() == Material.AIR) {
                    sender.sendMessage(ChatColor.RED + "No item in hand");
                    return true;
                }
                if (ItemAttribute.reset(item)) {
                    player.sendMessage(ChatColor.BLUE + "Item attributes of " + item.getType().name().toLowerCase().replace("_", " ") + " were reset to default.");
                } else {
                    player.sendMessage(ChatColor.RED + "No item attributes found on " + item.getType().name().toLowerCase().replace("_", " ") + ".");
                }
                return true;
            }
            break;
        default:
            break;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) return null;
        if (args.length == 1) return Arrays.asList("list", "add", "remove").stream().filter(a -> a.startsWith(args[0])).collect(Collectors.toList());
        switch (args[0]) {
        case "list": return null;
        case "add":
            String arg = args[args.length - 1];
            switch (args.length) {
            case 2: return Arrays.stream(Attribute.values()).map(a -> ItemAttribute.attributeNameOf(a)).filter(a -> a.startsWith(arg)).collect(Collectors.toList());
            case 3: return arg.isEmpty() ? Arrays.asList("1") : Collections.emptyList();
            default:
                if (arg.contains("=")) {
                    String[] toks = arg.split("=", 2);
                    switch (toks[0]) {
                    case "op": case "operation": return Arrays.stream(Operation.values()).map(Object::toString).filter(a -> a.startsWith(toks[1])).map(a -> toks[0] + "=" + a).collect(Collectors.toList());
                    case "slot": return Arrays.stream(EquipmentSlot.values()).map(a -> ItemAttribute.slotNameOf(a)).filter(a -> a.startsWith(toks[1])).map(a -> toks[0] + "=" + a).collect(Collectors.toList());
                    case "uuid":
                        String templ = "00000000-0000-0000-0000-000000000000";
                        String pat = "........-....-....-....-............";
                        return toks[1].length() <= templ.length() && toks[1].matches(pat.substring(0, toks[1].length())) ? Arrays.asList(toks[0] + "=" + toks[1] + templ.substring(toks[1].length())) : Collections.emptyList();
                    default: return arg.isEmpty() ? Arrays.asList("0") : Collections.emptyList();
                    }
                } else {
                    return Arrays.asList("op=", "slot=", "name=", "uuid=", "uuidLeast=", "uuidMost=").stream().filter(a -> a.startsWith(arg)).collect(Collectors.toList());
                }
            }
        case "remove": return Collections.emptyList();
        default: return null;
        }
    }
}
