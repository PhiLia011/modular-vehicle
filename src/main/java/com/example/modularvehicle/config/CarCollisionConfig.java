package com.example.modularvehicle.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.io.Reader;
import java.io.StringReader;

public class CarCollisionConfig {
    public static final ForgeConfigSpec SPEC;
    public static final Config VALUES = new Config();
    
    static {
        final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        
        BUILDER.comment("碰撞系统配置").push("collision");
        
        BUILDER.comment("基础速度设置").push("speed");
        VALUES.baseSpeed = BUILDER.define("base_speed", 1.0);
        VALUES.engineDamageSpeedMultiplier = BUILDER.define("engine_damage_speed_multiplier", 0.3);
        VALUES.batteryDamageSpeedMultiplier = BUILDER.define("battery_damage_speed_multiplier", 0.7);
        VALUES.wheelDamageSpeedMultiplier = BUILDER.define("wheel_damage_speed_multiplier", 0.5);
        BUILDER.pop();
        
        BUILDER.comment("转向设置").push("steering");
        VALUES.wheelDamageSteeringMultiplier = BUILDER.define("wheel_damage_steering_multiplier", 0.3);
        BUILDER.pop();
        
        BUILDER.comment("摩擦力设置").push("friction");
        VALUES.baseFriction = BUILDER.define("base_friction", 0.98);
        VALUES.wheelDamageFrictionMultiplier = BUILDER.define("wheel_damage_friction_multiplier", 0.7);
        BUILDER.pop();
        
        BUILDER.comment("碰撞概率设置").push("collision");
        VALUES.collisionDamageChance = BUILDER.define("collision_damage_chance", 0.1);
        VALUES.entityCollisionDamageChance = BUILDER.define("entity_collision_damage_chance", 0.2);
        BUILDER.pop();
        
        BUILDER.pop();
        
        BUILDER.comment("部件配置").push("parts");
        
        BUILDER.comment("引擎配置").push("engine");
        VALUES.engine.maxDurability = BUILDER.define("max_durability", 2000);
        VALUES.engine.fuelConsumptionRate = BUILDER.define("fuel_consumption_rate", 1);
        VALUES.engine.collisionDamage = BUILDER.define("collision_damage", 5);
        BUILDER.pop();
        
        BUILDER.comment("轮子配置").push("wheel");
        VALUES.wheel.maxDurability = BUILDER.define("max_durability", 1000);
        VALUES.wheel.collisionDamage = BUILDER.define("collision_damage", 3);
        VALUES.wheel.steeringPenalty = BUILDER.define("steering_penalty", 0.7);
        BUILDER.pop();
        
        BUILDER.comment("座位配置").push("seat");
        VALUES.seat.maxDurability = BUILDER.define("max_durability", 500);
        VALUES.seat.collisionDamage = BUILDER.define("collision_damage", 2);
        BUILDER.pop();
        
        BUILDER.comment("电池配置").push("battery");
        VALUES.battery.maxDurability = BUILDER.define("max_durability", 1500);
        VALUES.battery.collisionDamage = BUILDER.define("collision_damage", 4);
        BUILDER.pop();
        
        BUILDER.comment("行李箱配置").push("trunk");
        VALUES.trunk.maxDurability = BUILDER.define("max_durability", 800);
        VALUES.trunk.collisionDamage = BUILDER.define("collision_damage", 3);
        BUILDER.pop();
        
        BUILDER.comment("灯光配置").push("light");
        VALUES.light.maxDurability = BUILDER.define("max_durability", 300);
        VALUES.light.collisionDamage = BUILDER.define("collision_damage", 1);
        BUILDER.pop();
        
        BUILDER.pop();
        
        SPEC = BUILDER.build();
    }
    
    public static class Config {
        // 速度设置
        public ForgeConfigSpec.ConfigValue<Double> baseSpeed;
        public ForgeConfigSpec.ConfigValue<Double> engineDamageSpeedMultiplier;
        public ForgeConfigSpec.ConfigValue<Double> batteryDamageSpeedMultiplier;
        public ForgeConfigSpec.ConfigValue<Double> wheelDamageSpeedMultiplier;
        
        // 转向设置
        public ForgeConfigSpec.ConfigValue<Double> wheelDamageSteeringMultiplier;
        
        // 摩擦力设置
        public ForgeConfigSpec.ConfigValue<Double> baseFriction;
        public ForgeConfigSpec.ConfigValue<Double> wheelDamageFrictionMultiplier;
        
        // 碰撞概率设置
        public ForgeConfigSpec.ConfigValue<Double> collisionDamageChance;
        public ForgeConfigSpec.ConfigValue<Double> entityCollisionDamageChance;
        
        // 部件配置
        public final EngineConfig engine = new EngineConfig();
        public final WheelConfig wheel = new WheelConfig();
        public final SeatConfig seat = new SeatConfig();
        public final BatteryConfig battery = new BatteryConfig();
        public final TrunkConfig trunk = new TrunkConfig();
        public final LightConfig light = new LightConfig();
        
        // 配置类
        public static class EngineConfig {
            public ForgeConfigSpec.ConfigValue<Integer> maxDurability;
            public ForgeConfigSpec.ConfigValue<Integer> fuelConsumptionRate;
            public ForgeConfigSpec.ConfigValue<Integer> collisionDamage;
        }
        
        public static class WheelConfig {
            public ForgeConfigSpec.ConfigValue<Integer> maxDurability;
            public ForgeConfigSpec.ConfigValue<Integer> collisionDamage;
            public ForgeConfigSpec.ConfigValue<Double> steeringPenalty;
        }
        
        public static class SeatConfig {
            public ForgeConfigSpec.ConfigValue<Integer> maxDurability;
            public ForgeConfigSpec.ConfigValue<Integer> collisionDamage;
        }
        
        public static class BatteryConfig {
            public ForgeConfigSpec.ConfigValue<Integer> maxDurability;
            public ForgeConfigSpec.ConfigValue<Integer> collisionDamage;
        }
        
        public static class TrunkConfig {
            public ForgeConfigSpec.ConfigValue<Integer> maxDurability;
            public ForgeConfigSpec.ConfigValue<Integer> collisionDamage;
        }
        
        public static class LightConfig {
            public ForgeConfigSpec.ConfigValue<Integer> maxDurability;
            public ForgeConfigSpec.ConfigValue<Integer> collisionDamage;
        }
    }
}
