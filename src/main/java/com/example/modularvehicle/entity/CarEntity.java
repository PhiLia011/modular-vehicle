package com.example.modularvehicle.entity;

import com.example.modularvehicle.registry.CarPartDefinition;
import com.example.modularvehicle.item.CarPartItem;
import com.example.modularvehicle.registry.PartRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CarEntity extends Mob {
    public static final EntityType<CarEntity> TYPE = EntityType.Builder.of(CarEntity::new, MobCategory.MISC)
            .sized(1.5f, 1.0f)
            .clientTrackingRange(10)
            .updateInterval(2)
            .build("car");

    private static final int SEAT_COUNT = 6;
    private static final Vec3[] SEAT_POSITIONS = {
        new Vec3(0.3, 0.2, -0.3),   // 驾驶座
        new Vec3(-0.3, 0.2, -0.3),  // 副驾驶
        new Vec3(0.3, 0.2, 0.3),    // 后座左
        new Vec3(-0.3, 0.2, 0.3),   // 后座右
        new Vec3(0, 0.2, -0.6),     // 后座中
        new Vec3(0, 0.2, 0.6)       // 行李箱位置
    };

    // 部件系统（使用定义ID）
    private String[] partDefinitions = new String[6]; // 存储定义ID
    private CarPart[] parts = new CarPart[6];
    private int fuel = 100;
    private boolean isEngineRunning = false;
    
    // 碰撞系统
    private final com.example.modularvehicle.collision.CarCollisionManager collisionManager;
    private final com.example.modularvehicle.collision.CarCollisionEffects collisionEffects;

    public CarEntity(EntityType<? extends CarEntity> type, Level level) {
        super(type, level);
        initializeParts();
        this.collisionManager = new com.example.modularvehicle.collision.CarCollisionManager(this);
        this.collisionEffects = new com.example.modularvehicle.collision.CarCollisionEffects(this);
    }

    private void initializeParts() {
        // 使用定义ID初始化部件
        partDefinitions[0] = "engine";  // 引擎
        partDefinitions[1] = "wheel";   // 轮子
        partDefinitions[2] = "seat";    // 座位
        partDefinitions[3] = "battery"; // 电池
        partDefinitions[4] = "trunk";   // 行李箱
        partDefinitions[5] = "light";   // 灯光
        
        // 从定义创建部件
        for (int i = 0; i < partDefinitions.length; i++) {
            CarPartDefinition definition = PartRegistry.getInstance().getDefinition(partDefinitions[i]);
            if (definition != null) {
                parts[i] = new CarPart(CarPart.PartType.valueOf(definition.getType().toUpperCase()), 
                                      definition.getMaxDurability());
            } else {
                // 使用默认值
                parts[i] = new CarPart(CarPart.PartType.values()[i], 1000);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        // TODO: 载具专用同步数据（燃料、部件状态 —— 联机同步需求）
    }

    @Override
    public void tick() {
        super.tick();
        
        // 处理碰撞效果
        collisionEffects.handleCollisionEffects();
        
        // 消耗燃料
        if (isEngineRunning && fuel > 0) {
            fuel--;
            
            // 检查电池状态
            if (parts[3].isBroken()) {
                isEngineRunning = false;
            }
        }
        
        // 如果燃料耗尽，停止引擎
        if (fuel <= 0 && isEngineRunning) {
            isEngineRunning = false;
        }
        
        // 移动逻辑
        if (isEngineRunning) {
            applyMovement();
        }
    }
    
    private void applyMovement() {
        // 基础移动逻辑
        Vec3 movement = getDeltaMovement();
        
        // 应用碰撞效果
        collisionEffects.handleBlockCollision(movement);
        
        // 更新位置
        move(net.minecraft.world.entity.MoverType.SELF, movement);
        
        // 应用摩擦力
        if (onGround()) {
            setDeltaMovement(movement.multiply(0.95, 0, 0.95));
        } else {
            setDeltaMovement(movement.multiply(0.98, 0.98, 0.98));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        // 读取燃料
        if (tag.contains("fuel")) {
            fuel = tag.getInt("fuel");
        } else {
            fuel = 100; // 新实体的默认燃料
        }
        
        // 读取部件状态
        CompoundTag partsTag = tag.getCompound("parts");
        for (int i = 0; i < parts.length; i++) {
            if (partsTag.contains("part_" + i)) {
                parts[i] = CarPart.loadFromNBT(partsTag.getCompound("part_" + i));
            }
        }
        
        // 读取引擎状态
        isEngineRunning = tag.getBoolean("engine_running");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        // 保存燃料
        tag.putInt("fuel", fuel);
        
        // 保存部件状态
        CompoundTag partsTag = new CompoundTag();
        for (int i = 0; i < parts.length; i++) {
            CompoundTag partTag = new CompoundTag();
            parts[i].saveToNBT(partTag);
            partsTag.put("part_" + i, partTag);
        }
        tag.put("parts", partsTag);
        
        // 保存引擎状态
        tag.putBoolean("engine_running", isEngineRunning);
    }

    @Override
    public boolean canAddPassenger(Entity passenger) {
        // 检查是否可以添加乘客
        return getPassengers().size() < SEAT_COUNT && passenger instanceof Player;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        // 定位乘客到座位
        int passengerIndex = getPassengers().indexOf(passenger);
        if (passengerIndex >= 0 && passengerIndex < SEAT_POSITIONS.length) {
            Vec3 offset = SEAT_POSITIONS[passengerIndex];
            callback.accept(this, offset.x, offset.y, offset.z);
        }
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        // 受到伤害时损坏部件
        if (!level().isClientSide()) {
            // 随机损坏一个部件
            int randomPart = this.random.nextInt(parts.length);
            parts[randomPart].damage((int) amount);
            
            // 播放损坏音效
            level().playSound(null, blockPosition(), 
                net.minecraft.sounds.SoundEvents.METAL_BREAK, 
                net.minecraft.sounds.SoundSource.NEUTRAL, 
                1.0f, 1.0f);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public Component getName() {
        return Component.literal("模块化载具");
    }

    @Override
    public net.minecraft.world.InteractionResult mobInteract(Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        // 左键：安装部件
        if (player.isShiftKeyDown()) {
            return handlePartInstallation(player, itemStack, hand);
        }
        
        // 右键：启动/停止引擎
        else {
            return handleEngineToggle(player);
        }
    }

    private net.minecraft.world.InteractionResult handlePartInstallation(Player player, ItemStack itemStack, net.minecraft.world.InteractionHand hand) {
        // 检查是否为部件物品
        if (itemStack.getItem() instanceof CarPartItem) {
            CarPartItem partItem = (CarPartItem) itemStack.getItem();
            String definitionId = partItem.getPartType();
            
            // 查找对应部件位置
            for (int i = 0; i < partDefinitions.length; i++) {
                if (partDefinitions[i].equals(definitionId) && !parts[i].isInstalled()) {
                    // 安装部件
                    parts[i].setInstalled(true);
                    
                    // 播放安装音效
                    level().playSound(null, blockPosition(), 
                        net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 
                        net.minecraft.sounds.SoundSource.NEUTRAL, 
                        1.0f, 1.0f);
                    
                    // 消耗物品
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }
                    
                    return net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide());
                }
            }
        }
        
        return net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide());
    }

    private net.minecraft.world.InteractionResult handleEngineToggle(Player player) {
        // 启动/停止引擎
        if (collisionManager.isPartEffective(CarPart.PartType.ENGINE)) {
            isEngineRunning = !isEngineRunning;
            if (isEngineRunning) {
                player.sendSystemMessage(Component.literal("引擎启动"));
                level().playSound(null, blockPosition(), 
                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 
                    net.minecraft.sounds.SoundSource.NEUTRAL, 
                    1.0f, 1.0f);
            } else {
                player.sendSystemMessage(Component.literal("引擎停止"));
                level().playSound(null, blockPosition(), 
                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 
                    net.minecraft.sounds.SoundSource.NEUTRAL, 
                    1.0f, 1.0f);
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide());
        } else {
            player.sendSystemMessage(Component.literal("引擎未安装或已损坏"));
            return net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide());
        }
    }

    // 碰撞系统相关方法
    @Override
    protected AABB makeBoundingBox() {
        // Entity 基类构造期间本方法即被调用，此时 collisionManager 尚未初始化
        if (collisionManager == null) {
            return super.makeBoundingBox();
        }
        return collisionManager.getCarBoundingBox();
    }

    /** TODO: 1.20.1 无 Entity#getCollisions，实体级碰撞由碰撞管理器在 tick 中手动检测 */

    // 启动/停止引擎
    public void toggleEngine() {
        if (collisionManager.isPartEffective(CarPart.PartType.ENGINE)) {
            isEngineRunning = !isEngineRunning;
        }
    }

    // 获取燃料
    public int getFuel() {
        return fuel;
    }

    // 添加燃料
    public void addFuel(int amount) {
        fuel = Math.min(100, fuel + amount);
    }

    // 检查引擎是否运行
    public boolean isEngineRunning() {
        return isEngineRunning;
    }

    // 获取部件
    public CarPart getPart(int index) {
        if (index >= 0 && index < parts.length) {
            return parts[index];
        }
        return null;
    }
    
    // 获取部件定义
    public CarPartDefinition getPartDefinition(int index) {
        if (index >= 0 && index < partDefinitions.length) {
            return PartRegistry.getInstance().getDefinition(partDefinitions[index]);
        }
        return null;
    }
    
    // 安装部件
    public boolean installPart(int index, CarPart part) {
        if (index >= 0 && index < parts.length && !parts[index].isInstalled()) {
            parts[index] = part;
            parts[index].setInstalled(true);
            return true;
        }
        return false;
    }
    
    // 获取碰撞管理器
    public com.example.modularvehicle.collision.CarCollisionManager getCollisionManager() {
        return collisionManager;
    }
    
    // 获取碰撞效果处理器
    public com.example.modularvehicle.collision.CarCollisionEffects getCollisionEffects() {
        return collisionEffects;
    }
}
