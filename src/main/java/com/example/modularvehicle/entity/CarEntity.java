package com.example.modularvehicle.entity;

import com.example.modularvehicle.item.CarPartItem;
import com.example.modularvehicle.registry.CarPartDefinition;
import com.example.modularvehicle.registry.PartRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Optional;

/**
 * 模块化载具实体。
 * 部件槽位容器基于 ItemStackHandler（需求文档第 4 项），
 * 每个部件耐久独立存储在实体 NBT（需求第 6 项），
 * 潜行+空手右键拆卸、手持部件右键安装（需求第 7 项）。
 */
public class CarEntity extends Mob {
    public static final EntityType<CarEntity> TYPE = EntityType.Builder.of(CarEntity::new, MobCategory.MISC)
            .sized(1.8f, 1.4f)
            .clientTrackingRange(10)
            .updateInterval(2)
            .build("car");

    private static final int SEAT_COUNT = 6;
    private static final Vec3[] SEAT_POSITIONS = {
        new Vec3(0.3, 0.5, -0.3),   // 驾驶座
        new Vec3(-0.3, 0.5, -0.3),  // 副驾驶
        new Vec3(0.3, 0.5, 0.3),    // 后座左
        new Vec3(-0.3, 0.5, 0.3),   // 后座右
        new Vec3(0, 0.5, -0.7),     // 后座中
        new Vec3(0, 0.5, 0.7)       // 尾部座位
    };

    /** 部件槽位容器（9 槽，空 ItemStack = 未安装）。 */
    private final ItemStackHandler partInventory = new ItemStackHandler(CarPart.SLOT_COUNT);
    private final LazyOptional<IItemHandler> partInventoryCapability = LazyOptional.of(() -> partInventory);

    /** 各槽位部件的耐久状态（独立持久化）。 */
    private final CarPart[] parts = new CarPart[CarPart.SLOT_COUNT];

    private int fuel = 100;
    private boolean isEngineRunning = false;

    private final com.example.modularvehicle.collision.CarCollisionManager collisionManager;
    private final com.example.modularvehicle.collision.CarCollisionEffects collisionEffects;

    public CarEntity(EntityType<? extends CarEntity> type, Level level) {
        super(type, level);
        initializeParts();
        this.collisionManager = new com.example.modularvehicle.collision.CarCollisionManager(this);
        this.collisionEffects = new com.example.modularvehicle.collision.CarCollisionEffects(this);
    }

    private void initializeParts() {
        for (int slot = 0; slot < CarPart.SLOT_COUNT; slot++) {
            parts[slot] = createDefaultPart(slot);
        }
    }

    private CarPart createDefaultPart(int slot) {
        CarPart.PartType type = CarPart.partTypeForSlot(slot);
        CarPartDefinition definition = PartRegistry.getInstance().getDefinitionByType(type.getJsonType());
        int maxDurability = definition != null ? definition.getMaxDurability() : defaultDurability(type);
        return new CarPart(type, maxDurability);
    }

    private static int defaultDurability(CarPart.PartType type) {
        return switch (type) {
            case ENGINE -> 2000;
            case WHEEL -> 1000;
            case SHELL -> 2500;
            case SEAT -> 500;
            case FUEL_TANK -> 1200;
            case BATTERY -> 1500;
        };
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        // TODO: 载具专用同步数据（燃料、部件状态 —— 联机同步需求）
    }

    // ==================== 交互（需求第 7 项） ====================

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 潜行 + 空手：拆卸视线指向的部件
        if (player.isShiftKeyDown() && stack.isEmpty()) {
            return handlePartRemoval(player);
        }
        // 手持部件：安装到对应类型的空槽位
        if (!stack.isEmpty() && stack.getItem() instanceof CarPartItem) {
            return handlePartInstallation(player, stack);
        }
        // 其他：启动/停止引擎
        return handleEngineToggle(player);
    }

    private InteractionResult handlePartRemoval(Player player) {
        if (level().isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }
        int slot = findTargetedSlot(player);
        if (slot < 0) {
            player.sendSystemMessage(Component.literal("§7未指向任何部件"));
            return InteractionResult.sidedSuccess(false);
        }
        if (!isPartInstalled(slot)) {
            player.sendSystemMessage(Component.literal("§7该部位没有已安装的部件"));
            return InteractionResult.sidedSuccess(false);
        }

        // 掉落部件物品（优先进背包，放不下则掉地上）
        ItemStack dropped = partInventory.getStackInSlot(slot).copy();
        if (dropped.isEmpty()) {
            dropped = new ItemStack(itemForType(CarPart.partTypeForSlot(slot)));
        }
        partInventory.setStackInSlot(slot, ItemStack.EMPTY);
        parts[slot].setInstalled(false);

        if (!player.getInventory().add(dropped)) {
            spawnAtLocation(dropped);
        }
        level().playSound(null, blockPosition(),
            net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0f, 1.0f);
        player.sendSystemMessage(Component.literal("§e已拆卸: " + slotName(slot)));
        return InteractionResult.sidedSuccess(false);
    }

    private InteractionResult handlePartInstallation(Player player, ItemStack stack) {
        if (level().isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }
        CarPartItem partItem = (CarPartItem) stack.getItem();
        CarPart.PartType type;
        try {
            type = CarPart.PartType.fromJsonType(partItem.getPartType());
        } catch (IllegalArgumentException e) {
            return InteractionResult.PASS;
        }

        for (int slot = 0; slot < CarPart.SLOT_COUNT; slot++) {
            if (CarPart.partTypeForSlot(slot) == type && !isPartInstalled(slot)) {
                ItemStack installStack = stack.copy();
                installStack.setCount(1);
                partInventory.setStackInSlot(slot, installStack);
                parts[slot].setInstalled(true);
                // 新部件恢复满耐久
                parts[slot].repair(parts[slot].getMaxDurability());

                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                level().playSound(null, blockPosition(),
                    net.minecraft.sounds.SoundEvents.ITEM_FRAME_ADD_ITEM, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0f, 1.0f);
                player.sendSystemMessage(Component.literal("§a已安装: " + slotName(slot)));
                return InteractionResult.sidedSuccess(false);
            }
        }
        player.sendSystemMessage(Component.literal("§7没有可安装的空槽位（" + type.getJsonType() + "）"));
        return InteractionResult.sidedSuccess(false);
    }

    /**
     * 视线射线与各部件碰撞盒求交，返回玩家瞄准的槽位（需求：hitResult 判断部位）。
     * 简化为忽略载具旋转的轴对齐近似。
     */
    public int findTargetedSlot(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(6.0));
        int bestSlot = -1;
        double bestDist = Double.MAX_VALUE;
        for (com.example.modularvehicle.collision.PartCollisionBox box : collisionManager.getCollisionBoxes()) {
            AABB worldBox = box.getLocalAABB().move(position());
            Optional<Vec3> hit = worldBox.clip(eye, end);
            if (hit.isPresent()) {
                double dist = eye.distanceToSqr(hit.get());
                if (dist < bestDist) {
                    bestDist = dist;
                    bestSlot = box.getSlot();
                }
            }
        }
        return bestSlot;
    }

    private InteractionResult handleEngineToggle(Player player) {
        if (collisionManager.isSlotEffective(CarPart.SLOT_ENGINE)) {
            isEngineRunning = !isEngineRunning;
            player.sendSystemMessage(Component.literal(isEngineRunning ? "§a引擎启动" : "§e引擎停止"));
            level().playSound(null, blockPosition(),
                net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.0f, 1.0f);
        } else {
            player.sendSystemMessage(Component.literal("§c引擎未安装或已损坏"));
        }
        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    // ==================== Tick 与物理效果 ====================

    @Override
    public void tick() {
        super.tick();

        collisionEffects.handleCollisionEffects();

        // 燃料消耗
        if (isEngineRunning && fuel > 0) {
            fuel--;
            if (!collisionManager.isSlotEffective(CarPart.SLOT_BATTERY)) {
                isEngineRunning = false;
            }
        }
        if (fuel <= 0 && isEngineRunning) {
            isEngineRunning = false;
        }

        // 油箱破损：漏油/火焰粒子 + 附近实体受伤（需求文档）
        if (!level().isClientSide
                && isPartInstalled(CarPart.SLOT_FUEL_TANK)
                && parts[CarPart.SLOT_FUEL_TANK].isBroken()
                && random.nextInt(20) == 0) {
            if (level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                    getX(), getY() + 0.4, getZ(), 4, 0.4, 0.2, 0.4, 0.01);
            }
            for (Entity entity : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(1.2))) {
                if (entity != this) {
                    entity.hurt(damageSources().inFire(), 2.0f);
                }
            }
        }

        if (isEngineRunning) {
            applyMovement();
        }
    }

    private void applyMovement() {
        Vec3 movement = getDeltaMovement();
        collisionEffects.handleBlockCollision(movement);
        move(net.minecraft.world.entity.MoverType.SELF, movement);

        // 车轮数量影响摩擦（需求：车轮损坏影响地面摩擦力）
        int wheels = collisionManager.getEffectiveWheelCount();
        double groundFriction = 0.70 + 0.075 * wheels; // 0轮=0.70, 4轮=1.00（近似 0.95 基准）
        if (onGround()) {
            setDeltaMovement(movement.multiply(groundFriction, 0, groundFriction));
        } else {
            setDeltaMovement(movement.multiply(0.98, 0.98, 0.98));
        }
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (!level().isClientSide) {
            // 受击随机损坏已安装部件
            int slot = random.nextInt(CarPart.SLOT_COUNT);
            if (isPartInstalled(slot)) {
                parts[slot].damage((int) amount);
            }
            level().playSound(null, blockPosition(),
                net.minecraft.sounds.SoundEvents.METAL_BREAK, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0f, 1.0f);

            // 车身外壳缺失：乘客失去防护，连带受伤（需求文档）
            if (!collisionManager.isSlotEffective(CarPart.SLOT_SHELL)) {
                for (Entity passenger : getPassengers()) {
                    if (passenger instanceof LivingEntity living) {
                        living.hurt(source, amount * 0.5f);
                    }
                }
            }
        }
        return super.hurt(source, amount);
    }

    // ==================== NBT 持久化（需求第 6 项） ====================

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("fuel")) {
            fuel = tag.getInt("fuel");
        } else {
            fuel = 100;
        }

        CompoundTag partsTag = tag.getCompound("parts");
        for (int i = 0; i < CarPart.SLOT_COUNT; i++) {
            if (partsTag.contains("part_" + i)) {
                parts[i] = CarPart.loadFromNBT(partsTag.getCompound("part_" + i));
            } else if (parts[i] == null) {
                parts[i] = createDefaultPart(i);
            }
        }

        if (tag.contains("inventory")) {
            partInventory.deserializeNBT(tag.getCompound("inventory"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("fuel", fuel);

        CompoundTag partsTag = new CompoundTag();
        for (int i = 0; i < CarPart.SLOT_COUNT; i++) {
            CompoundTag partTag = new CompoundTag();
            parts[i].saveToNBT(partTag);
            partsTag.put("part_" + i, partTag);
        }
        tag.put("parts", partsTag);
        tag.put("inventory", partInventory.serializeNBT());
    }

    // ==================== 乘客 ====================

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        int passengerIndex = getPassengers().indexOf(passenger);
        if (passengerIndex >= 0 && passengerIndex < SEAT_POSITIONS.length) {
            Vec3 offset = SEAT_POSITIONS[passengerIndex];
            callback.accept(this, offset.x, offset.y, offset.z);
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().size() < SEAT_COUNT && passenger instanceof Player;
    }

    // ==================== 碰撞箱（需求第 5 项：动态组合） ====================

    @Override
    protected AABB makeBoundingBox() {
        // Entity 基类构造期间本方法即被调用，此时 collisionManager 尚未初始化
        if (collisionManager == null) {
            return super.makeBoundingBox();
        }
        return collisionManager.getCarBoundingBox();
    }

    // ==================== Capability（IItemHandler 暴露） ====================

    @Override
    public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return partInventoryCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        partInventoryCapability.invalidate();
    }

    // ==================== 状态访问 ====================

    public boolean isPartInstalled(int slot) {
        return !partInventory.getStackInSlot(slot).isEmpty() || parts[slot].isInstalled();
    }

    public ItemStack getPartStack(int slot) {
        return partInventory.getStackInSlot(slot);
    }

    public void toggleEngine() {
        if (collisionManager.isSlotEffective(CarPart.SLOT_ENGINE)) {
            isEngineRunning = !isEngineRunning;
        }
    }

    public int getFuel() { return fuel; }

    public void addFuel(int amount) { fuel = Math.min(100, fuel + amount); }

    public boolean isEngineRunning() { return isEngineRunning; }

    public CarPart getPart(int index) {
        if (index >= 0 && index < parts.length) {
            return parts[index];
        }
        return null;
    }

    public CarPartDefinition getPartDefinition(int index) {
        if (index < 0 || index >= CarPart.SLOT_COUNT) return null;
        return PartRegistry.getInstance().getDefinitionByType(CarPart.partTypeForSlot(index).getJsonType());
    }

    public com.example.modularvehicle.collision.CarCollisionManager getCollisionManager() {
        return collisionManager;
    }

    public com.example.modularvehicle.collision.CarCollisionEffects getCollisionEffects() {
        return collisionEffects;
    }

    /** 槽位中文名（用于提示）。 */
    public static String slotName(int slot) {
        return switch (slot) {
            case CarPart.SLOT_ENGINE -> "引擎";
            case CarPart.SLOT_WHEEL_FL -> "左前轮";
            case CarPart.SLOT_WHEEL_FR -> "右前轮";
            case CarPart.SLOT_WHEEL_RL -> "左后轮";
            case CarPart.SLOT_WHEEL_RR -> "右后轮";
            case CarPart.SLOT_SHELL -> "车身外壳";
            case CarPart.SLOT_SEAT -> "座椅";
            case CarPart.SLOT_FUEL_TANK -> "油箱";
            case CarPart.SLOT_BATTERY -> "电池";
            default -> "未知槽位";
        };
    }

    private static net.minecraft.world.item.Item itemForType(CarPart.PartType type) {
        return switch (type) {
            case ENGINE -> com.example.modularvehicle.registry.ModItems.ENGINE_ITEM.get();
            case WHEEL -> com.example.modularvehicle.registry.ModItems.WHEEL_ITEM.get();
            case SHELL -> com.example.modularvehicle.registry.ModItems.SHELL_ITEM.get();
            case SEAT -> com.example.modularvehicle.registry.ModItems.SEAT_ITEM.get();
            case FUEL_TANK -> com.example.modularvehicle.registry.ModItems.FUEL_TANK_ITEM.get();
            case BATTERY -> com.example.modularvehicle.registry.ModItems.BATTERY_ITEM.get();
        };
    }
}
