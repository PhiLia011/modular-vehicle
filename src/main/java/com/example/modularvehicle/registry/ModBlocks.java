package com.example.modularvehicle.registry;

import com.example.modularvehicle.ModularVehicle;
import com.example.modularvehicle.block.WorkbenchBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, ModularVehicle.MOD_ID);

    public static final RegistryObject<Block> WORKBENCH = BLOCKS.register("workbench",
        () -> new WorkbenchBlock(Block.Properties.of()
            .strength(2.5f)
            .noOcclusion()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
