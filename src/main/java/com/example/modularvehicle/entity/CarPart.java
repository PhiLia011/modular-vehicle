package com.example.modularvehicle.entity;

import net.minecraft.nbt.CompoundTag;

public class CarPart {
    public enum PartType {
        ENGINE,      // 引擎
        WHEEL,       // 轮子
        SEAT,        // 座位
        BATTERY,     // 电池
        TRUNK,       // 行李箱
        LIGHT;       // 灯光
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

    public PartType getType() {
        return type;
    }

    public int getDurability() {
        return durability;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

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
