package com.example.modularvehicle.registry;

import com.example.modularvehicle.ModularVehicle;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 配方注册。1.20.1 的合成配方走数据生成/JSON 数据包（见 PartRecipeProvider），
 * 运行时不通过 FMLCommonSetupEvent 注册配方本体。
 */
public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ModularVehicle.MOD_ID);

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
