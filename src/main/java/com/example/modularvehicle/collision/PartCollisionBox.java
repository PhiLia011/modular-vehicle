package com.example.modularvehicle.collision;

import com.example.modularvehicle.entity.CarPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 部件局部碰撞盒（相对载具坐标）。
 * offset 为盒中心相对载具中心的偏移，size 为完整尺寸。
 */
public class PartCollisionBox {
    private final int slot;
    private final Vec3 offset;
    private final Vec3 size;

    public PartCollisionBox(int slot, Vec3 offset, Vec3 size) {
        this.slot = slot;
        this.offset = offset;
        this.size = size;
    }

    public int getSlot() { return slot; }

    public CarPart.PartType getPartType() { return CarPart.partTypeForSlot(slot); }

    /** 局部 AABB（以载具中心为原点）。 */
    public AABB getLocalAABB() {
        return new AABB(
            offset.x - size.x / 2, offset.y - size.y / 2, offset.z - size.z / 2,
            offset.x + size.x / 2, offset.y + size.y / 2, offset.z + size.z / 2);
    }

    /** 世界坐标 AABB。 */
    public AABB getWorldAABB(Vec3 carPos) {
        return getLocalAABB().move(carPos);
    }
}
