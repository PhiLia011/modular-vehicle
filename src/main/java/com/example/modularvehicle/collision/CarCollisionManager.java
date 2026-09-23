package com.example.modularvehicle.collision;

import com.example.modularvehicle.entity.CarEntity;
import com.example.modularvehicle.entity.CarPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态碰撞管理：只组合已安装且未损坏部件的碰撞盒（需求第 5 项）。
 * 拆掉车身外壳/车轮后碰撞箱相应缺失。
 */
public class CarCollisionManager {
    private final CarEntity carEntity;
    private final List<PartCollisionBox> collisionBoxes = new ArrayList<>();

    public CarCollisionManager(CarEntity carEntity) {
        this.carEntity = carEntity;
        initializeCollisionBoxes();
    }

    private void initializeCollisionBoxes() {
        // 引擎（车头）
        collisionBoxes.add(new PartCollisionBox(CarPart.SLOT_ENGINE,
            new Vec3(0, 0.45, -0.95), new Vec3(0.9, 0.5, 0.5)));
        // 四轮（四角）
        collisionBoxes.add(new PartCollisionBox(CarPart.SLOT_WHEEL_FL,
            new Vec3(-0.95, 0.2, -0.85), new Vec3(0.35, 0.5, 0.35)));
        collisionBoxes.add(new PartCollisionBox(CarPart.SLOT_WHEEL_FR,
            new Vec3(0.95, 0.2, -0.85), new Vec3(0.35, 0.5, 0.35)));
        collisionBoxes.add(new PartCollisionBox(CarPart.SLOT_WHEEL_RL,
            new Vec3(-0.95, 0.2, 0.85), new Vec3(0.35, 0.5, 0.35)));
        collisionBoxes.add(new PartCollisionBox(CarPart.SLOT_WHEEL_RR,
            new Vec3(0.95, 0.2, 0.85), new Vec3(0.35, 0.5, 0.35)));
        // 车身外壳（覆盖主体）
        collisionBoxes.add(new PartCollisionBox(CarPart.SLOT_SHELL,
            new Vec3(0, 0.75, 0), new Vec3(1.9, 0.85, 2.2)));
        // 座椅（中部）
        collisionBoxes.add(new PartCollisionBox(CarPart.SLOT_SEAT,
            new Vec3(0, 0.6, 0.1), new Vec3(1.2, 0.5, 1.0)));
        // 油箱（车尾下部）
        collisionBoxes.add(new PartCollisionBox(CarPart.SLOT_FUEL_TANK,
            new Vec3(0, 0.3, 1.05), new Vec3(0.8, 0.45, 0.5)));
        // 电池（座椅下方）
        collisionBoxes.add(new PartCollisionBox(CarPart.SLOT_BATTERY,
            new Vec3(0, 0.25, 0.3), new Vec3(0.6, 0.35, 0.5)));
    }

    public List<PartCollisionBox> getCollisionBoxes() {
        return collisionBoxes;
    }

    /** 槽位是否有效（已安装且未损坏）。 */
    public boolean isSlotEffective(int slot) {
        if (slot < 0 || slot >= CarPart.SLOT_COUNT) return false;
        if (!carEntity.isPartInstalled(slot)) return false;
        CarPart part = carEntity.getPart(slot);
        return part != null && !part.isBroken();
    }

    /** 部件类型是否有效（WHEEL = 至少一个轮子有效）。 */
    public boolean isPartEffective(CarPart.PartType type) {
        return switch (type) {
            case ENGINE -> isSlotEffective(CarPart.SLOT_ENGINE);
            case WHEEL -> getEffectiveWheelCount() >= 1;
            case SHELL -> isSlotEffective(CarPart.SLOT_SHELL);
            case SEAT -> isSlotEffective(CarPart.SLOT_SEAT);
            case FUEL_TANK -> isSlotEffective(CarPart.SLOT_FUEL_TANK);
            case BATTERY -> isSlotEffective(CarPart.SLOT_BATTERY);
        };
    }

    /** 有效车轮数量（0-4），影响速度/转向/摩擦（需求文档）。 */
    public int getEffectiveWheelCount() {
        int count = 0;
        for (int slot : CarPart.WHEEL_SLOTS) {
            if (isSlotEffective(slot)) count++;
        }
        return count;
    }

    /** 动态碰撞形状：只合并有效部件。 */
    public VoxelShape getDynamicCollisionShape() {
        VoxelShape combined = Shapes.empty();
        for (PartCollisionBox box : collisionBoxes) {
            if (isSlotEffective(box.getSlot())) {
                combined = Shapes.or(combined, Shapes.create(box.getLocalAABB()));
            }
        }
        return combined;
    }

    /** 底盘基础碰撞箱（保证载具不会因部件全空而无法交互）。 */
    public AABB getBaseBoundingBox() {
        return new AABB(-0.9, 0, -1.1, 0.9, 0.5, 1.1);
    }

    public List<AABB> getAllCollisionBoxes() {
        List<AABB> boxes = new ArrayList<>();
        Vec3 carPos = carEntity.position();
        for (PartCollisionBox box : collisionBoxes) {
            if (isSlotEffective(box.getSlot())) {
                boxes.add(box.getWorldAABB(carPos));
            }
        }
        return boxes;
    }

    public PartCollisionBox getCollisionBoxForSlot(int slot) {
        for (PartCollisionBox box : collisionBoxes) {
            if (box.getSlot() == slot) {
                return box;
            }
        }
        return null;
    }

    /** 载具整体碰撞箱：有效部件并集，空则退回底盘。 */
    public AABB getCarBoundingBox() {
        VoxelShape shape = getDynamicCollisionShape();
        if (shape.isEmpty()) {
            return getBaseBoundingBox();
        }
        return shape.bounds();
    }
}
