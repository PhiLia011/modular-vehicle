package com.example.modularvehicle.collision;

import com.example.modularvehicle.entity.CarPart;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PartCollisionBox {
    private final CarPart.PartType partType;
    private final AABB localAABB; // 相对于载具中心的AABB
    private final Vec3 offset; // 相对于载具中心的偏移
    
    public PartCollisionBox(CarPart.PartType partType, Vec3 offset, Vec3 size) {
        this.partType = partType;
        this.offset = offset;
        this.localAABB = new AABB(
            -size.x / 2 + offset.x, -size.y / 2 + offset.y, -size.z / 2 + offset.z,
            size.x / 2 + offset.x, size.y / 2 + offset.y, size.z / 2 + offset.z
        );
    }
    
    public AABB getLocalAABB() {
        return localAABB;
    }
    
    public Vec3 getOffset() {
        return offset;
    }
    
    public CarPart.PartType getPartType() {
        return partType;
    }
    
    // 转换为世界坐标的AABB
    public AABB getWorldAABB(Vec3 carPosition) {
        return localAABB.move(carPosition.x, carPosition.y, carPosition.z);
    }
    
    @Override
    public String toString() {
        return partType + " at " + offset + " with AABB " + localAABB;
    }
}
