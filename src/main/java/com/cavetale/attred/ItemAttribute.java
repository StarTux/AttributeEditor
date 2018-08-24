package com.cavetale.attred;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_13_R1.ItemStack;
import net.minecraft.server.v1_13_R1.NBTBase;
import net.minecraft.server.v1_13_R1.NBTTagCompound;
import net.minecraft.server.v1_13_R1.NBTTagList;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.inventory.EquipmentSlot;

@RequiredArgsConstructor @Getter
public final class ItemAttribute {
    private static Field fieldCraftItemStackHandle = null;
    private static final String TAG_ATTRS = "AttributeModifiers";
    private static final Map<String, Attribute> NAME_ATTR_MAP;
    private static final Map<Attribute, String> ATTR_NAME_MAP;
    public final Attribute attribute;
    public final EquipmentSlot slot;
    public final String name;
    public final double amount;
    public final Operation operation;
    public final UUID uuid;

    static {
        NAME_ATTR_MAP = new HashMap<>();
        ATTR_NAME_MAP = new EnumMap<>(Attribute.class);
        for (Attribute attribute: Attribute.values()) {
            String[] attrNames = attribute.name().split("_");
            StringBuilder sb = new StringBuilder();
            sb.append(attrNames[0].toLowerCase()).append(".");
            sb.append(attrNames[1].toLowerCase());
            for (int i = 2; i < attrNames.length; i += 1) sb.append(attrNames[i].substring(0, 1)).append(attrNames[i].substring(1).toLowerCase());
            String name = sb.toString();
            NAME_ATTR_MAP.put(name, attribute);
            ATTR_NAME_MAP.put(attribute, name);
        }
    }

    /**
     * Get a list of attributes on an item.
     * @param bukkitItem The item
     * @return List of attributes
     */
    public static List<ItemAttribute> of(org.bukkit.inventory.ItemStack bukkitItem) {
        List<ItemAttribute> result = new ArrayList<>();
        NBTTagList attrList = getAttributeTag(bukkitItem);
        if (attrList == null) return result;
        for (int i = 0; i < attrList.size(); i += 1) {
            NBTTagCompound attrInst = attrList.getCompound(i);
            String attrSlot = attrInst.getString("Slot");
            String attrAttributeName = attrInst.getString("AttributeName");
            String attrName = attrInst.getString("Name");
            double attrAmount = attrInst.getDouble("Amount");
            int attrOperation = attrInst.getInt("Operation");
            long attrUUIDLeast = attrInst.getLong("UUIDLeast");
            long attrUUIDMost = attrInst.getLong("UUIDMost");
            ItemAttribute entry = new ItemAttribute(attributeOf(attrAttributeName), slotOf(attrSlot), attrName, attrAmount, operationOf(attrOperation), new UUID(attrUUIDMost, attrUUIDLeast));
            result.add(entry);
        }
        return result;
    }

    /**
     * List default values from vanilla Minecraft. This list may not be exhaustive.
     * CAUTION: Correctness NOT guaranteed.
     * CAUTION: Vanilla UUIDs are currently NOT used.
     * @param item The item
     * @result A list of attributes
     */
    public static List<ItemAttribute> defaultsOf(org.bukkit.inventory.ItemStack item) {
        List<ItemAttribute> result = new ArrayList<>();
        Material mat = item.getType();
        double armor = DefaultValues.getDefaultArmor(mat);
        double tough = DefaultValues.getDefaultArmorToughness(mat);
        double damag = DefaultValues.getDefaultAttackDamage(mat);
        double speed = DefaultValues.getDefaultAttackSpeed(mat);
        if (armor > 0) result.add(new ItemAttribute(Attribute.GENERIC_ARMOR, guessEquipmentSlotOf(item), attributeNameOf(Attribute.GENERIC_ARMOR), armor, Operation.ADD_NUMBER, UUID.randomUUID()));
        if (tough > 0) result.add(new ItemAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS, guessEquipmentSlotOf(item), attributeNameOf(Attribute.GENERIC_ARMOR_TOUGHNESS), tough, Operation.ADD_NUMBER, UUID.randomUUID()));
        if (damag > 0) result.add(new ItemAttribute(Attribute.GENERIC_ATTACK_DAMAGE, guessEquipmentSlotOf(item), attributeNameOf(Attribute.GENERIC_ATTACK_DAMAGE), damag, Operation.ADD_NUMBER, UUID.randomUUID()));
        if (speed > 0) result.add(new ItemAttribute(Attribute.GENERIC_ATTACK_SPEED, guessEquipmentSlotOf(item), attributeNameOf(Attribute.GENERIC_ATTACK_SPEED), speed, Operation.ADD_NUMBER, UUID.randomUUID()));
        return result;
    }

    /**
     * Add this attribute to an item.
     * @param bukkitItem The item
     * @param inst The attribute
     * @return The original item or a copy if necessary
     */
    public org.bukkit.inventory.ItemStack addTo(org.bukkit.inventory.ItemStack bukkitItem) {
        CraftItemStack obcItem = asCraftItemStack(bukkitItem);
        NBTTagList list = getOrCreateAttributeTag(obcItem);
        NBTTagCompound attrInst = new NBTTagCompound();
        String attrSlot;
        int attrOperation;
        attrInst.setString("Slot", getSlotName());
        attrInst.setString("AttributeName", getAttributeName());
        attrInst.setString("Name", this.name);
        attrInst.setDouble("Amount", this.amount);
        attrInst.setInt("Operation", getOperationNumber());
        attrInst.setLong("UUIDMost", this.uuid.getMostSignificantBits());
        attrInst.setLong("UUIDLeast", this.uuid.getLeastSignificantBits());
        list.add(attrInst);
        return obcItem;
    }

    /**
     * Remove all attributes from an item.
     * @param bukkitItem The item
     * @return true if item had attribute tag, false otherwise
     */
    public static boolean reset(org.bukkit.inventory.ItemStack bukkitItem) {
        return removeAttributeTag(bukkitItem);
    }

    public static String attributeNameOf(Attribute attribute) {
        return ATTR_NAME_MAP.get(attribute);
    }

    public String getAttributeName() {
        return ATTR_NAME_MAP.get(this.attribute);
    }

    public static Attribute attributeOf(String name) {
        return NAME_ATTR_MAP.get(name);
    }

    public String getSlotName() {
        return slotNameOf(this.slot);
    }

    public static String slotNameOf(EquipmentSlot slot) {
        switch (slot) {
        case HAND: return "mainhand";
        case OFF_HAND: return "offhand";
        case HEAD: return "head";
        case CHEST: return "chest";
        case LEGS: return "legs";
        case FEET: return "feet";
        default: return "mainhand";
        }
    }

    public static EquipmentSlot slotOf(String name) {
        switch (name) {
        case "mainhand": return EquipmentSlot.HAND;
        case "offhand": return EquipmentSlot.OFF_HAND;
        case "head": return EquipmentSlot.HEAD;
        case "chest": return EquipmentSlot.CHEST;
        case "legs": return EquipmentSlot.LEGS;
        case "feet": return EquipmentSlot.FEET;
        default: throw new IllegalArgumentException("Invalid item slot: " + name);
        }
    }

    public int getOperationNumber() {
        switch (this.operation) {
        case ADD_NUMBER: return 0;
        case ADD_SCALAR: return 1;
        case MULTIPLY_SCALAR_1: return 2;
        default: return 0;
        }
    }

    public String getOperationSymbol() {
        switch (this.operation) {
        case ADD_NUMBER: return "+";
        case ADD_SCALAR: return "*";
        case MULTIPLY_SCALAR_1: return "+*"; // eek
        default: return "?";
        }
    }

    public static Operation operationOf(int number) {
        switch (number) {
        case 0: return Operation.ADD_NUMBER;
        case 1: return Operation.ADD_SCALAR;
        case 2: return Operation.MULTIPLY_SCALAR_1;
        default: throw new IllegalArgumentException("Bad operaton: " + 0);
        }
    }

    public static EquipmentSlot guessEquipmentSlotOf(org.bukkit.inventory.ItemStack item) {
        String itemName = item.getType().name();
        if (itemName.contains("_HELMET")) {
            return EquipmentSlot.HEAD;
        } else if (itemName.contains("_CHESTPLATE")) {
            return EquipmentSlot.CHEST;
        } else if (itemName.contains("_LEGGINGS")) {
            return EquipmentSlot.LEGS;
        } else if (itemName.contains("_BOOTS")) {
            return EquipmentSlot.FEET;
        } else {
            return EquipmentSlot.HAND;
        }
    }

    private static Field getFieldCraftItemStackHandle() {
        if (fieldCraftItemStackHandle == null) {
            try {
                fieldCraftItemStackHandle = CraftItemStack.class.getDeclaredField("handle");
            } catch (NoSuchFieldException nsfe) {
                nsfe.printStackTrace();
                fieldCraftItemStackHandle = null;
                throw new RuntimeException(nsfe);
            }
        }
        return fieldCraftItemStackHandle;
    }

    private static NBTTagList getAttributeTag(org.bukkit.inventory.ItemStack bukkitItem) {
        try {
            if (!(bukkitItem instanceof CraftItemStack)) return null;
            CraftItemStack obcItem = (CraftItemStack)bukkitItem;
            getFieldCraftItemStackHandle().setAccessible(true);
            ItemStack nmsItem = (ItemStack)fieldCraftItemStackHandle.get(obcItem);
            NBTTagCompound tag = nmsItem.getTag();
            if (tag == null) return null;
            NBTBase base = tag.get(TAG_ATTRS);
            if (base == null || !(base instanceof NBTTagList)) return null;
            return (NBTTagList)base;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean removeAttributeTag(org.bukkit.inventory.ItemStack bukkitItem) {
        try {
            if (!(bukkitItem instanceof CraftItemStack)) return false;
            CraftItemStack obcItem = (CraftItemStack)bukkitItem;
            getFieldCraftItemStackHandle().setAccessible(true);
            ItemStack nmsItem = (ItemStack)fieldCraftItemStackHandle.get(obcItem);
            NBTTagCompound tag = nmsItem.getTag();
            if (tag == null) return false;
            tag.remove(TAG_ATTRS);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static CraftItemStack asCraftItemStack(org.bukkit.inventory.ItemStack bukkitItem) {
        if (bukkitItem instanceof CraftItemStack) return (CraftItemStack)bukkitItem;
        return CraftItemStack.asCraftCopy(bukkitItem);
    }

    private static NBTTagList getOrCreateAttributeTag(CraftItemStack obcItem) {
        try {
            getFieldCraftItemStackHandle().setAccessible(true);
            ItemStack nmsItem = (ItemStack)fieldCraftItemStackHandle.get(obcItem);
            NBTTagCompound tag = nmsItem.getTag();
            if (tag == null) {
                tag = new NBTTagCompound();
                nmsItem.setTag(tag);
            }
            NBTBase base = tag.get(TAG_ATTRS);
            if (base == null || !(base instanceof NBTTagList)) {
                NBTTagList list = new NBTTagList();
                tag.set(TAG_ATTRS, list);
                return list;
            }
            return (NBTTagList)base;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
