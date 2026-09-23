package com.example.modularvehicle.collision;

import com.example.modularvehicle.config.CarCollisionConfig;
import com.example.modularvehicle.entity.CarEntity;
import com.example.modularvehicle.entity.CarPart;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CarCollisionEffects {
    private final CarEntity carEntity;
    private final CarCollisionManager collisionManager;
    
    public CarCollisionEffects(CarEntity carEntity) {
        this.carEntity = carEntity;
        this.collisionManager = new CarCollisionManager(carEntity);
    }
    
    // 处理碰撞影响
    public void handleCollisionEffects() {
        if (!carEntity.level().isClientSide) {
            applySpeedPenalties();
            applySteeringPenalties();
            applyFrictionEffects();
            checkPartFailureEffects();
        }
    }
    
    // 速度惩罚（引擎、电池损坏）
    private void applySpeedPenalties() {
        double maxSpeed = CarCollisionConfig.VALUES.baseSpeed.get();
        
        // 引擎损坏影响速度
        if (!collisionManager.isPartEffective(CarPart.PartType.ENGINE)) {
            maxSpeed *= CarCollisionConfig.VALUES.engineDamageSpeedMultiplier.get();
        }
        
        // 电池损坏影响速度
        if (!collisionManager.isPartEffective(CarPart.PartType.BATTERY)) {
            maxSpeed *= CarCollisionConfig.VALUES.batteryDamageSpeedMultiplier.get();
        }
        
        // 轮子损坏影响速度
        if (!collisionManager.isPartEffective(CarPart.PartType.WHEEL)) {
            maxSpeed *= CarCollisionConfig.VALUES.wheelDamageSpeedMultiplier.get();
        }
        
        // 应用速度限制
        Vec3 currentMotion = carEntity.getDeltaMovement();
        double currentSpeed = currentMotion.length();
        
        if (currentSpeed > maxSpeed) {
            Vec3 limitedMotion = currentMotion.normalize().scale(maxSpeed);
            carEntity.setDeltaMovement(limitedMotion);
        }
    }
    
    // 转向惩罚（轮子损坏）
    private void applySteeringPenalties() {
        // 轮子损坏影响转向
        if (!collisionManager.isPartEffective(CarPart.PartType.WHEEL)) {
            // 转向速度降低
            double steeringMultiplier = CarCollisionConfig.VALUES.wheelDamageSteeringMultiplier.get();
            carEntity.setYRot((float) (carEntity.getYRot() * steeringMultiplier));
        }
        
        // 如果两个轮子都损坏，几乎无法转向
        if (!collisionManager.isPartEffective(CarPart.PartType.WHEEL) || 
            !collisionManager.isPartEffective(CarPart.PartType.WHEEL)) {
            carEntity.setYRot(carEntity.getYRot() * 0.1f);
        }
    }
    
    // 摩擦力效果（轮子、道路条件）
    private void applyFrictionEffects() {
        double friction = CarCollisionConfig.VALUES.baseFriction.get();
        
        // 轮子损坏影响摩擦力
        if (!collisionManager.isPartEffective(CarPart.PartType.WHEEL)) {
            friction *= CarCollisionConfig.VALUES.wheelDamageFrictionMultiplier.get();
        }
        
        // 应用摩擦力
        Vec3 currentMotion = carEntity.getDeltaMovement();
        Vec3 frictionMotion = currentMotion.scale(friction);
        carEntity.setDeltaMovement(frictionMotion);
    }
    
    // 部件失效效果
    private void checkPartFailureEffects() {
        // 引擎失效时自动停止
        if (carEntity.isEngineRunning() && !collisionManager.isPartEffective(CarPart.PartType.ENGINE)) {
            carEntity.toggleEngine();
            carEntity.level().broadcastEntityEvent(carEntity, (byte) 4); // 停止引擎事件
        }
        
        // 电池失效时停止引擎
        if (carEntity.isEngineRunning() && !collisionManager.isPartEffective(CarPart.PartType.BATTERY)) {
            carEntity.toggleEngine();
            carEntity.level().broadcastEntityEvent(carEntity, (byte) 4); // 停止引擎事件
        }
    }
    
    // 处理与方块的碰撞
    public void handleBlockCollision(Vec3 movement) {
        Level level = carEntity.level();
        
        // 检查前方是否有方块
        Vec3 frontPos = carEntity.position().add(movement.normalize().scale(1.0));
        
        // 如果前方有方块，产生碰撞效果
        if (!level.isEmptyBlock(carEntity.blockPosition().offset(
            (int) movement.x, (int) movement.y, (int) movement.z))) {
            
            // 碰撞时损坏部件
            double damageChance = CarCollisionConfig.VALUES.collisionDamageChance.get();
            if (level.random.nextDouble() < damageChance) {
                int randomPartIndex = level.random.nextInt(6);
                carEntity.getPart(randomPartIndex).damage(1);
                
                // 如果引擎损坏，停止引擎
                if (randomPartIndex == 0 && carEntity.isEngineRunning()) {
                    carEntity.toggleEngine();
                }
            }
            
            // 反弹效果
            Vec3 bounce = movement.scale(-0.5);
            carEntity.setDeltaMovement(bounce);
            
            // 播放碰撞音效
            level.playSound(null, carEntity.blockPosition(), 
                net.minecraft.sounds.SoundEvents.METAL_BREAK, 
                net.minecraft.sounds.SoundSource.NEUTRAL, 
                1.0f, 1.0f);
        }
    }
    
    // 处理与其他实体的碰撞
    public void handleEntityCollision(net.minecraft.world.entity.Entity other) {
        // 与其他载具碰撞
        if (other instanceof CarEntity) {
            CarEntity otherCar = (CarEntity) other;
            
            // 计算碰撞力
            Vec3 collisionForce = carEntity.getDeltaMovement().add(otherCar.getDeltaMovement()).scale(0.5);
            
            // 应用碰撞力
            carEntity.setDeltaMovement(carEntity.getDeltaMovement().add(collisionForce.scale(-0.3)));
            otherCar.setDeltaMovement(otherCar.getDeltaMovement().add(collisionForce.scale(0.3)));
            
            // 碰撞时损坏部件
            double damageChance = CarCollisionConfig.VALUES.entityCollisionDamageChance.get();
            if (carEntity.level().random.nextDouble() < damageChance) {
                int randomPartIndex = carEntity.level().random.nextInt(6);
                carEntity.getPart(randomPartIndex).damage(2);
            }
        }
        
        // 与生物碰撞
        else if (other instanceof net.minecraft.world.entity.LivingEntity) {
            net.minecraft.world.entity.LivingEntity living = (net.minecraft.world.entity.LivingEntity) other;
            
            // 推开生物
            Vec3 pushDirection = other.position().subtract(carEntity.position()).normalize();
            living.setDeltaMovement(pushDirection.scale(2.0));
            
            // 对生物造成伤害
            living.hurt(carEntity.damageSources().mobAttack(carEntity), 2.0f);
            
            // 载具反弹
            carEntity.setDeltaMovement(carEntity.getDeltaMovement().scale(-0.5));
        }
    }
    
    // 获取当前载具状态（用于调试）
    public String getCarStatus() {
        StringBuilder status = new StringBuilder("载具状态:\n");
        
        status.append("引擎: ").append(carEntity.isEngineRunning() ? "运行中" : "停止").append("\n");
        status.append("燃料: ").append(carEntity.getFuel()).append("/100\n");
        
        for (int i = 0; i < 6; i++) {
            CarPart part = carEntity.getPart(i);
            status.append(part.getType()).append(": ")
                  .append(part.isInstalled() ? (part.isBroken() ? "损坏" : "正常") : "未安装")
                  .append(" (").append(part.getDurability()).append("/").append(part.getMaxDurability()).append(")\n");
        }
        
        return status.toString();
    }
}
