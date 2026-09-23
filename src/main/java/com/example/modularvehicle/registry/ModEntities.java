package com.example.modularvehicle.registry;

import com.example.modularvehicle.ModularVehicle;
import com.example.modularvehicle.entity.CarEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ModularVehicle.MOD_ID);

    public static final RegistryObject<EntityType<CarEntity>> CAR = ENTITIES.register("car",
        () -> CarEntity.TYPE);

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
