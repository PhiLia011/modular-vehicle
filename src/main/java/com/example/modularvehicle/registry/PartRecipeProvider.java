package com.example.modularvehicle.registry;

import com.example.modularvehicle.ModularVehicle;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

/**
 * 部件配方生成器（1.20.1 数据生成 API：Consumer<FinishedRecipe>）。
 * TODO: 接入正式 datagen run，把配方输出到 src/generated/resources；
 *       部件修复配方（需求文档 repairMaterials 字段）尚未实现。
 */
public class PartRecipeProvider {

    public static void registerRecipes(Consumer<FinishedRecipe> output, RegistryAccess registryAccess) {
        generateShapedRecipe(output,
            new ItemLike[]{Items.IRON_INGOT, Items.IRON_INGOT, Items.IRON_INGOT,
                           Items.IRON_INGOT, Items.CHEST, Items.IRON_INGOT,
                           Items.IRON_INGOT, Items.IRON_INGOT, Items.IRON_INGOT},
            "workbench", ModBlocks.WORKBENCH.get());

        generateShapedRecipe(output,
            new ItemLike[]{Items.IRON_INGOT, Items.REDSTONE, Items.IRON_INGOT,
                           Items.IRON_INGOT, Items.DIAMOND, Items.IRON_INGOT,
                           Items.IRON_INGOT, Items.REDSTONE, Items.IRON_INGOT},
            "engine", ModItems.ENGINE_ITEM.get());

        generateShapedRecipe(output,
            new ItemLike[]{Items.IRON_INGOT, Items.RABBIT_HIDE, Items.IRON_INGOT,
                           Items.IRON_NUGGET, Items.RABBIT_HIDE, Items.IRON_NUGGET,
                           Items.IRON_INGOT, Items.RABBIT_HIDE, Items.IRON_INGOT},
            "wheel", ModItems.WHEEL_ITEM.get());
    }

    private static void generateShapedRecipe(Consumer<FinishedRecipe> output, ItemLike[] ingredients, String name, ItemLike result) {
        char[] symbols = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result);
        builder.pattern("ABC").pattern("DEF").pattern("GHI");
        for (int i = 0; i < ingredients.length; i++) {
            builder.define(symbols[i], ingredients[i]);
        }
        builder.unlockedBy("has_iron_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT));
        builder.save(output, new ResourceLocation(ModularVehicle.MOD_ID, name));
    }
}
