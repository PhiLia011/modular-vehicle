package com.example.modularvehicle.command;

import java.util.Map;

import com.example.modularvehicle.registry.CarPartDefinition;
import com.example.modularvehicle.registry.PartRegistry;
import com.example.modularvehicle.registry.PartValidation;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PartDebugCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("partdebug")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayer();
                if (player != null) {
                    listAllParts(player);
                }
                return 1;
            })
            .then(Commands.literal("info")
                .then(Commands.argument("partId", StringArgumentType.string())
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayer();
                        String partId = StringArgumentType.getString(context, "partId");
                        
                        if (player != null) {
                            showPartInfo(player, partId);
                        }
                        return 1;
                    })
                )
            )
            .then(Commands.literal("validate")
                .then(Commands.argument("partId", StringArgumentType.string())
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayer();
                        String partId = StringArgumentType.getString(context, "partId");
                        
                        if (player != null) {
                            validatePart(player, partId);
                        }
                        return 1;
                    })
                )
            )
        );
    }
    
    private static void listAllParts(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("=== 部件定义列表 ==="));
        
        for (CarPartDefinition definition : PartRegistry.getInstance().getAllDefinitions().values()) {
            player.sendSystemMessage(Component.literal(
                String.format("%s (%s) - 耐久度: %d, 重量: %.1f",
                    definition.getDisplayName(),
                    definition.getType(),
                    definition.getMaxDurability(),
                    definition.getWeight()
                )
            ));
        }
    }
    
    private static void showPartInfo(ServerPlayer player, String partId) {
        CarPartDefinition definition = PartRegistry.getInstance().getDefinition(partId);
        
        if (definition == null) {
            player.sendSystemMessage(Component.literal("部件定义不存在: " + partId));
            return;
        }
        
        player.sendSystemMessage(Component.literal("=== 部件信息 ==="));
        player.sendSystemMessage(Component.literal("ID: " + definition.getId()));
        player.sendSystemMessage(Component.literal("类型: " + definition.getType()));
        player.sendSystemMessage(Component.literal("显示名称: " + definition.getDisplayName()));
        player.sendSystemMessage(Component.literal("最大耐久度: " + definition.getMaxDurability()));
        player.sendSystemMessage(Component.literal("重量: " + definition.getWeight()));
        
        player.sendSystemMessage(Component.literal("属性:"));
        for (Map.Entry<String, Object> entry : definition.getProperties().entrySet()) {
            player.sendSystemMessage(Component.literal("  " + entry.getKey() + ": " + entry.getValue()));
        }
        
        player.sendSystemMessage(Component.literal("碰撞箱:"));
        CarPartDefinition.CollisionBox box = definition.getCollisionBox();
        player.sendSystemMessage(Component.literal(
            String.format("  尺寸: %.2f x %.2f x %.2f", box.getX(), box.getY(), box.getZ())
        ));
        player.sendSystemMessage(Component.literal(
            String.format("  偏移: %.2f, %.2f, %.2f", box.getOffsetX(), box.getOffsetY(), box.getOffsetZ())
        ));
        
        player.sendSystemMessage(Component.literal("修复材料:"));
        for (Map.Entry<String, Integer> entry : definition.getRepairMaterials().entrySet()) {
            player.sendSystemMessage(Component.literal("  " + entry.getKey() + ": " + entry.getValue()));
        }
        
        player.sendSystemMessage(Component.literal("兼容载具:"));
        for (String vehicle : definition.getCompatibleVehicles()) {
            player.sendSystemMessage(Component.literal("  " + vehicle));
        }
        
        player.sendSystemMessage(Component.literal("纹理: " + definition.getTexture()));
        player.sendSystemMessage(Component.literal("模型: " + definition.getModel()));
    }
    
    private static void validatePart(ServerPlayer player, String partId) {
        CarPartDefinition definition = PartRegistry.getInstance().getDefinition(partId);
        
        if (definition == null) {
            player.sendSystemMessage(Component.literal("部件定义不存在: " + partId));
            return;
        }
        
        PartValidation.ValidationResult result = PartValidation.validateDefinition(definition);
        
        if (result.isValid()) {
            player.sendSystemMessage(Component.literal("✅ " + definition.getDisplayName() + " - 验证通过"));
        } else {
            player.sendSystemMessage(Component.literal("❌ " + definition.getDisplayName() + " - 验证失败"));
            player.sendSystemMessage(Component.literal(result.getErrorSummary()));
        }
    }
}
