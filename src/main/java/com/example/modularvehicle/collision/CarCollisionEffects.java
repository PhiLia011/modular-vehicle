package com.example.modularvehicle.collision;

import com.example.modularvehicle.config.CarCollisionConfig;
import com.example.modularvehicle.entity.CarEntity;
import com.example.modularvehicle.entity.CarPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 碰撞物理反馈：部件状态影响速度/转向/摩擦，方块与实体碰撞造成部件损伤。
 */
public class CarCollisionEffects {
    private final CarEntity carEntity;
    private final CarCollisionManager collisionManager;

    public CarCollisionEffects(CarEntity carEntity) {
        this.carEntity = carEntity;
        this.collisionManager = carEntity.getCollisionManager();
    }

    public void handleCollisionEffects() {
        if (!carEntity.level().isClientSide) {
            applySpeedPenalties();
            applySteeringPenalties();
            applyFrictionEffects();
            checkPartFailureEffects();
        }
    }

    // 速度惩罚（引擎/电池损坏 + 车轮数量渐进）
    private void applySpeedPenalties() {
        double maxSpeed = CarCollisionConfig.VALUES.baseSpeed.get();

        if (!collisionManager.isSlotEffective(CarPart.SLOT_ENGINE)) {
            maxSpeed *= CarCollisionConfig.VALUES.engineDamageSpeedMultiplier.get();
        }
        if (!collisionManager.isSlotEffective(CarPart.SLOT_BATTERY)) {
            maxSpeed *= CarCollisionConfig.VALUES.batteryDamageSpeedMultiplier.get();
        }

        // 车轮数量渐进影响：4轮=无惩罚，0轮=完整惩罚
        int wheels = collisionManager.getEffectiveWheelCount();
        double wheelMult = CarCollisionConfig.VALUES.wheelDamageSpeedMultiplier.get();
        maxSpeed *= wheelMult + (1.0 - wheelMult) * (wheels / 4.0);

        Vec3 currentMotion = carEntity.getDeltaMovement();
        double currentSpeed = currentMotion.length();
        if (currentSpeed > maxSpeed) {
            carEntity.setDeltaMovement(currentMotion.normalize().scale(maxSpeed));
        }
    }

    // 转向惩罚（<2 个有效车轮时转向困难）
    private void applySteeringPenalties() {
        int wheels = collisionManager.getEffectiveWheelCount();
        if (wheels < 2) {
            double steeringMultiplier = CarCollisionConfig.VALUES.wheelDamageSteeringMultiplier.get();
            carEntity.setYRot((float) (carEntity.getYRot() * steeringMultiplier));
        }
    }

    // 摩擦效果（车轮数量渐进）
    private void applyFrictionEffects() {
        double friction = 0.98;
        int wheels = collisionManager.getEffectiveWheelCount();
        if (wheels < 4) {
            double mult = CarCollisionConfig.VALUES.wheelDamageFrictionMultiplier.get();
            friction *= mult + (1.0 - mult) * (wheels / 4.0);
        }
        Vec3 currentMotion = carEntity.getDeltaMovement();
        carEntity.setDeltaMovement(currentMotion.scale(friction));
    }

    // 部件失效自动停机
    private void checkPartFailureEffects() {
        if (carEntity.isEngineRunning()) {
            if (!collisionManager.isSlotEffective(CarPart.SLOT_ENGINE)
                    || !collisionManager.isSlotEffective(CarPart.SLOT_BATTERY)) {
                carEntity.toggleEngine();
                carEntity.level().broadcastEntityEvent(carEntity, (byte) 4);
            }
        }
    }

    // 方块碰撞：按概率损伤部件 + 反弹
    public void handleBlockCollision(Vec3 movement) {
        Level level = carEntity.level();

        Vec3 frontPos = carEntity.position().add(movement.normalize().scale(1.0));
        if (!level.isEmptyBlock(carEntity.blockPosition().offset(
                (int) movement.x, (int) movement.y, (int) movement.z))) {

            double damageChance = CarCollisionConfig.VALUES.collisionDamageChance.get();
            if (level.random.nextDouble() < damageChance) {
                int randomPartIndex = level.random.nextInt(CarPart.SLOT_COUNT);
                if (carEntity.isPartInstalled(randomPartIndex)) {
                    carEntity.getPart(randomPartIndex).damage(1);
                }
                if (randomPartIndex == CarPart.SLOT_ENGINE && carEntity.isEngineRunning()) {
                    carEntity.toggleEngine();
                }
            }

            carEntity.setDeltaMovement(movement.scale(-0.5));
            level.playSound(null, carEntity.blockPosition(),
                net.minecraft.sounds.SoundEvents.METAL_BREAK, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0f, 1.0f);
        }
    }

    // 实体碰撞
    public void handleEntityCollision(Entity other) {
        if (other instanceof CarEntity otherCar) {
            Vec3 collisionForce = carEntity.getDeltaMovement().add(otherCar.getDeltaMovement()).scale(0.5);
            carEntity.setDeltaMovement(carEntity.getDeltaMovement().add(collisionForce.scale(-0.3)));
            otherCar.setDeltaMovement(otherCar.getDeltaMovement().add(collisionForce.scale(0.3)));

            double damageChance = CarCollisionConfig.VALUES.entityCollisionDamageChance.get();
            if (carEntity.level().random.nextDouble() < damageChance) {
                int randomPartIndex = carEntity.level().random.nextInt(CarPart.SLOT_COUNT);
                if (carEntity.isPartInstalled(randomPartIndex)) {
                    carEntity.getPart(randomPartIndex).damage(2);
                }
            }
        } else if (other instanceof LivingEntity living) {
            Vec3 pushDirection = other.position().subtract(carEntity.position()).normalize();
            living.setDeltaMovement(pushDirection.scale(2.0));
            living.hurt(carEntity.damageSources().mobAttack(carEntity), 2.0f);
            carEntity.setDeltaMovement(carEntity.getDeltaMovement().scale(-0.5));
        }
    }

    public String getCarStatus() {
        StringBuilder status = new StringBuilder("载具状态:\n");
        status.append("引擎: ").append(carEntity.isEngineRunning() ? "运行中" : "停止").append("\n");
        status.append("燃料: ").append(carEntity.getFuel()).append("/100\n");
        for (int i = 0; i < CarPart.SLOT_COUNT; i++) {
            status.append(CarEntity.slotName(i)).append(": ")
                  .append(carEntity.isPartInstalled(i) ? (carEntity.getPart(i).isBroken() ? "损坏" : "正常") : "未安装")
                  .append(" (").append(carEntity.getPart(i).getDurability())
                  .append("/").append(carEntity.getPart(i).getMaxDurability()).append(")\n");
        }
        return status.toString();
    }
}
