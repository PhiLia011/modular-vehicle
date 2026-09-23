package com.example.modularvehicle.collision;

import com.example.modularvehicle.ModularVehicle;
import com.example.modularvehicle.entity.CarEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 载具碰撞/交互事件处理。
 * 说明：Forge 1.20.1 没有 EntityCollisionEvent / EntityCollideBlockEvent，
 * 实体与方块的碰撞伤害由 CarEntity.tick() 与 CarCollisionEffects 内部处理。
 * TODO: 载具内/外玩家交互逻辑（需求：潜行+右键拆卸，手持部件右键安装）。
 */
@Mod.EventBusSubscriber(modid = ModularVehicle.MOD_ID)
public class CarCollisionEvents {

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof CarEntity car) {
            Player player = event.getEntity();
            if (!event.getLevel().isClientSide()) {
                // TODO: 在载具上/外的交互逻辑
            }
        }
    }
}
