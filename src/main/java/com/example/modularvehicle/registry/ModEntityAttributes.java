package com.example.modularvehicle.registry;

import com.example.modularvehicle.entity.CarEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

/** 实体属性注册（Mob 子类必须有，否则生成实体时崩溃）。 */
public class ModEntityAttributes {

    public static void register(EntityAttributeCreationEvent event) {
        AttributeSupplier attributes = Mob.createMobAttributes()
            .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 40.0)
            .add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 0.3)
            .build();
        event.put(ModEntities.CAR.get(), attributes);
    }
}
