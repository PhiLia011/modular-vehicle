package com.example.modularvehicle.registry;

import com.example.modularvehicle.entity.CarPart;

import com.example.modularvehicle.ModularVehicle;
import net.minecraft.resources.ResourceLocation;

public class PartValidation {
    
    public static ValidationResult validateDefinition(CarPartDefinition definition) {
        ValidationResult result = new ValidationResult();
        
        // 验证必填字段
        if (definition.getId() == null || definition.getId().isEmpty()) {
            result.addError("ID不能为空");
        }
        
        if (definition.getType() == null || definition.getType().isEmpty()) {
            result.addError("类型不能为空");
        }
        
        if (definition.getDisplayName() == null || definition.getDisplayName().isEmpty()) {
            result.addError("显示名称不能为空");
        }
        
        if (definition.getMaxDurability() <= 0) {
            result.addError("最大耐久度必须大于0");
        }
        
        if (definition.getWeight() <= 0) {
            result.addError("重量必须大于0");
        }
        
        // 验证碰撞箱
        if (definition.getCollisionBox() == null) {
            result.addError("碰撞箱不能为空");
        } else {
            CarPartDefinition.CollisionBox box = definition.getCollisionBox();
            if (box.getX() <= 0 || box.getY() <= 0 || box.getZ() <= 0) {
                result.addError("碰撞箱尺寸必须大于0");
            }
        }
        
        // 验证修复材料
        if (definition.getRepairMaterials() == null || definition.getRepairMaterials().isEmpty()) {
            result.addError("修复材料不能为空");
        }
        
        // 验证兼容载具
        if (definition.getCompatibleVehicles() == null || definition.getCompatibleVehicles().length == 0) {
            result.addError("兼容载具列表不能为空");
        }
        
        // 验证纹理和模型
        if (definition.getTexture() == null) {
            result.addError("纹理路径不能为空");
        }
        
        if (definition.getModel() == null) {
            result.addError("模型路径不能为空");
        }
        
        // 验证类型是否有效
        try {
            CarPart.PartType.valueOf(definition.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            result.addError("无效的部件类型: " + definition.getType());
        }
        
        return result;
    }
    
    public static class ValidationResult {
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public java.util.List<String> getErrors() {
            return java.util.Collections.unmodifiableList(errors);
        }
        
        public String getErrorSummary() {
            if (isValid()) {
                return "验证通过";
            }
            
            StringBuilder sb = new StringBuilder("验证失败:\n");
            for (String error : errors) {
                sb.append("- ").append(error).append("\n");
            }
            return sb.toString();
        }
    }
}
