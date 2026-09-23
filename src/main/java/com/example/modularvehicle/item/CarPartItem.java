package com.example.modularvehicle.item;

import com.example.modularvehicle.registry.CarPartDefinition;
import com.example.modularvehicle.registry.PartRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CarPartItem extends Item {
    private final String definitionId;
    
    public CarPartItem(String definitionId, Properties properties) {
        super(properties);
        this.definitionId = definitionId;
    }
    

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // 检查是否有对应的部件定义
        CarPartDefinition definition = PartRegistry.getInstance().getDefinition(definitionId);
        if (definition == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("部件定义不存在: " + definitionId));
            return InteractionResultHolder.fail(itemStack);
        }

        // TODO: 实现部件使用逻辑（对准载具、类型校验、安装）
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
    
    // 获取部件定义
    public CarPartDefinition getDefinition() {
        return PartRegistry.getInstance().getDefinition(definitionId);
    }
    
    // 获取部件类型
    public String getPartType() {
        CarPartDefinition definition = getDefinition();
        return definition != null ? definition.getType() : "unknown";
    }
    
    // 获取显示名称
    public String getDisplayName() {
        CarPartDefinition definition = getDefinition();
        return definition != null ? definition.getDisplayName() : "未知部件";
    }
    
    // 获取最大耐久度
    public int getMaxDurability() {
        CarPartDefinition definition = getDefinition();
        return definition != null ? definition.getMaxDurability() : 100;
    }
    
    // 获取重量
    public double getWeight() {
        CarPartDefinition definition = getDefinition();
        return definition != null ? definition.getWeight() : 0;
    }
    
    // 检查是否兼容指定载具
    public boolean isCompatible(String vehicleType) {
        CarPartDefinition definition = getDefinition();
        if (definition == null) return false;
        
        for (String compatible : definition.getCompatibleVehicles()) {
            if (compatible.equals(vehicleType)) {
                return true;
            }
        }
        return false;
    }
    
    // 获取修复材料
    public java.util.Map<String, Integer> getRepairMaterials() {
        CarPartDefinition definition = getDefinition();
        return definition != null ? definition.getRepairMaterials() : java.util.Collections.emptyMap();
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // 始终显示 enchanted 效果
    }
}
