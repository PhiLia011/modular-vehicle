package com.example.modularvehicle.entity;

import net.minecraft.nbt.CompoundTag;

/**
 * 部件数据模型：类型、耐久、最大耐久、安装状态。
 * 耐久独立存储在实体 NBT 中（需求文档第 6 项）。
 */
public class CarPart {

    /** 部件类型（JSON 中的 type 字段使用小写名）。 */
    public enum PartType {
        ENGINE("engine"),
        WHEEL("wheel"),
        SHELL("shell"),
        SEAT("seat"),
        FUEL_TANK("fuel_tank"),
        BATTERY("battery");

        private final String jsonType;

        PartType(String jsonType) { this.jsonType = jsonType; }

        public String getJsonType() { return jsonType; }

        public static PartType fromJsonType(String jsonType) {
            for (PartType t : values()) {
                if (t.jsonType.equalsIgnoreCase(jsonType)) return t;
            }
            throw new IllegalArgumentException("未知部件类型: " + jsonType);
        }
    }

    /** 槽位布局（需求文档：引擎/车轮x4/车身外壳/座椅/油箱/电池）。 */
    public static final int SLOT_COUNT = 9;
    public static final int SLOT_ENGINE = 0;
    public static final int SLOT_WHEEL_FL = 1;
    public static final int SLOT_WHEEL_FR = 2;
    public static final int SLOT_WHEEL_RL = 3;
    public static final int SLOT_WHEEL_RR = 4;
    public static final int SLOT_SHELL = 5;
    public static final int SLOT_SEAT = 6;
    public static final int SLOT_FUEL_TANK = 7;
    public static final int SLOT_BATTERY = 8;
    public static final int[] WHEEL_SLOTS = {SLOT_WHEEL_FL, SLOT_WHEEL_FR, SLOT_WHEEL_RL, SLOT_WHEEL_RR};

    /** 获取槽位对应的部件类型。 */
    public static PartType partTypeForSlot(int slot) {
        return switch (slot) {
            case SLOT_ENGINE -> PartType.ENGINE;
            case SLOT_WHEEL_FL, SLOT_WHEEL_FR, SLOT_WHEEL_RL, SLOT_WHEEL_RR -> PartType.WHEEL;
            case SLOT_SHELL -> PartType.SHELL;
            case SLOT_SEAT -> PartType.SEAT;
            case SLOT_FUEL_TANK -> PartType.FUEL_TANK;
            case SLOT_BATTERY -> PartType.BATTERY;
            default -> throw new IllegalArgumentException("非法槽位: " + slot);
        };
    }

    private final PartType type;
    private int durability;
    private final int maxDurability;
    private boolean installed;

    public CarPart(PartType type, int maxDurability) {
        this.type = type;
        this.maxDurability = maxDurability;
        this.durability = maxDurability;
        this.installed = false;
    }

    public void damage(int amount) {
        this.durability = Math.max(0, this.durability - amount);
    }

    public void repair(int amount) {
        this.durability = Math.min(this.maxDurability, this.durability + amount);
    }

    public boolean isBroken() {
        return this.durability <= 0;
    }

    public PartType getType() { return type; }
    public int getDurability() { return durability; }
    public int getMaxDurability() { return maxDurability; }
    public boolean isInstalled() { return installed; }
    public void setInstalled(boolean installed) { this.installed = installed; }

    public void saveToNBT(CompoundTag tag) {
        tag.putString("type", type.name());
        tag.putInt("durability", durability);
        tag.putInt("maxDurability", maxDurability);
        tag.putBoolean("installed", installed);
    }

    public static CarPart loadFromNBT(CompoundTag tag) {
        PartType type = PartType.valueOf(tag.getString("type"));
        int maxDurability = tag.getInt("maxDurability");
        CarPart part = new CarPart(type, maxDurability);
        part.durability = tag.getInt("durability");
        part.installed = tag.getBoolean("installed");
        return part;
    }

    @Override
    public String toString() {
        return type + " (耐久: " + durability + "/" + maxDurability + ", 已安装: " + installed + ")";
    }
}
