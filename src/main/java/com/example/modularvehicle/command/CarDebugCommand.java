package com.example.modularvehicle.command;

import com.example.modularvehicle.entity.CarEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class CarDebugCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cardebug")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                Player player = context.getSource().getPlayer();
                if (player != null) {
                    Entity vehicle = player.getVehicle();
                    if (vehicle instanceof CarEntity) {
                        CarEntity car = (CarEntity) vehicle;
                        player.sendSystemMessage(Component.literal(car.getCollisionEffects().getCarStatus()));
                        return 1;
                    } else {
                        player.sendSystemMessage(Component.literal("你不在载具上"));
                    }
                }
                return 0;
            })
            .then(Commands.literal("fuel")
                .executes(context -> {
                    Player player = context.getSource().getPlayer();
                    if (player != null) {
                        Entity vehicle = player.getVehicle();
                        if (vehicle instanceof CarEntity) {
                            CarEntity car = (CarEntity) vehicle;
                            player.sendSystemMessage(Component.literal(
                                "当前燃料: " + car.getFuel() + "/100"
                            ));
                            return 1;
                        }
                    }
                    return 0;
                })
            )
            .then(Commands.literal("parts")
                .executes(context -> {
                    Player player = context.getSource().getPlayer();
                    if (player != null) {
                        Entity vehicle = player.getVehicle();
                        if (vehicle instanceof CarEntity) {
                            CarEntity car = (CarEntity) vehicle;
                            StringBuilder status = new StringBuilder("部件状态:\n");
                            for (int i = 0; i < 6; i++) {
                                var part = car.getPart(i);
                                status.append(i).append(". ").append(part.getType())
                                      .append(": ").append(part.isInstalled() ? 
                                          (part.isBroken() ? "损坏" : "正常") : "未安装")
                                      .append(" (").append(part.getDurability())
                                      .append("/").append(part.getMaxDurability()).append(")\n");
                            }
                            player.sendSystemMessage(Component.literal(status.toString()));
                            return 1;
                        }
                    }
                    return 0;
                })
            )
            .then(Commands.literal("refuel")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                    .executes(context -> {
                        Player player = context.getSource().getPlayer();
                        int amount = IntegerArgumentType.getInteger(context, "amount");
                        
                        if (player != null) {
                            Entity vehicle = player.getVehicle();
                            if (vehicle instanceof CarEntity) {
                                CarEntity car = (CarEntity) vehicle;
                                car.addFuel(amount);
                                player.sendSystemMessage(Component.literal(
                                    "添加了 " + amount + " 点燃料，当前燃料: " + car.getFuel() + "/100"
                                ));
                                return 1;
                            }
                        }
                        return 0;
                    })
                )
            )
        );
    }
}
