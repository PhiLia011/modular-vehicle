package com.example.modularvehicle.collision;

import com.example.modularvehicle.entity.CarEntity;
import com.example.modularvehicle.entity.CarPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.ArrayList;
import java.util.List;

public class CarCollisionManager {
    private final CarEntity carEntity;
    private final List<PartCollisionBox> collisionBoxes = new ArrayList<>();
    
    public CarCollisionManager(CarEntity carEntity) {
        this.carEntity = carEntity;
        initializeCollisionBoxes();
    }
    
    private void initializeCollisionBoxes() {
        // 基于载具的6个位置创建碰撞箱
        // 驾驶座
        collisionBoxes.add(new PartCollisionBox(
            CarPart.PartType.SEAT,
            new Vec3(0.3, 0.2, -0.3),
            new Vec3(0.4, 0.4, 0.6)
        ));
        
        // 副驾驶
        collisionBoxes.add(new PartCollisionBox(
            CarPart.PartType.SEAT,
            new Vec3(-0.3, 0.2, -0.3),
            new Vec3(0.4, 0.4, 0.6)
        ));
        
        // 后座左
        collisionBoxes.add(new PartCollisionBox(
            CarPart.PartType.SEAT,
            new Vec3(0.3, 0.2, 0.3),
            new Vec3(0.4, 0.4, 0.6)
        ));
        
        // 后座右
        collisionBoxes.add(new PartCollisionBox(
            CarPart.PartType.SEAT,
            new Vec3(-0.3, 0.2, 0.3),
            new Vec3(0.4, 0.4, 0.6)
        ));
        
        // 后座中
        collisionBoxes.add(new PartCollisionBox(
            CarPart.PartType.SEAT,
            new Vec3(0, 0.2, 0),
            new Vec3(0.4, 0.4, 0.6)
        ));
        
        // 行李箱
        collisionBoxes.add(new PartCollisionBox(
            CarPart.PartType.TRUNK,
            new Vec3(0, 0.2, 0.6),
            new Vec3(0.8, 0.4, 0.4)
        ));
        
        // 引擎
        collisionBoxes.add(new PartCollisionBox(
            CarPart.PartType.ENGINE,
            new Vec3(0, 0.3, -0.2),
            new Vec3(0.6, 0.3, 0.4)
        ));
        
        // 轮子（前轮）
        collisionBoxes.add(new PartCollisionBox(
            CarPart.PartType.WHEEL,
            new Vec3(-0.75, 0, -0.2),
            new Vec3(0.3, 0.3, 0.4)
        ));
        
        // 轮子（后轮）
        collisionBoxes.add(new PartCollisionBox(
            CarPart.PartType.WHEEL,
            new Vec3(0.75, 0, -0.2),
            new Vec3(0.3, 0.3, 0.4)
        ));
    }
    
    // 获取动态碰撞箱（只包含已安装的部件）
    public VoxelShape getDynamicCollisionShape() {
        VoxelShape combinedShape = Shapes.empty();
        
        for (PartCollisionBox box : collisionBoxes) {
            CarPart part = carEntity.getPart(getPartIndex(box.getPartType()));
            if (part != null && part.isInstalled() && !part.isBroken()) {
                // 将AABB转换为VoxelShape并合并
                VoxelShape partShape = Shapes.create(box.getLocalAABB());
                combinedShape = Shapes.or(combinedShape, partShape);
            }
        }
        
        return combinedShape;
    }
    
    // 获取基础碰撞箱（载具主体）
    public AABB getBaseBoundingBox() {
        return new AABB(-0.8, 0, -0.4, 0.8, 0.8, 0.4);
    }
    
    // 获取所有碰撞箱（用于碰撞检测）
    public List<AABB> getAllCollisionBoxes() {
        List<AABB> boxes = new ArrayList<>();
        Vec3 carPos = carEntity.position();
        
        for (PartCollisionBox box : collisionBoxes) {
            CarPart part = carEntity.getPart(getPartIndex(box.getPartType()));
            if (part != null && part.isInstalled() && !part.isBroken()) {
                boxes.add(box.getWorldAABB(carPos));
            }
        }
        
        return boxes;
    }
    
    // 根据部件类型获取碰撞箱
    public PartCollisionBox getCollisionBoxForPart(CarPart.PartType partType) {
        for (PartCollisionBox box : collisionBoxes) {
            if (box.getPartType() == partType) {
                return box;
            }
        }
        return null;
    }
    
    // 获取部件索引
    private int getPartIndex(CarPart.PartType partType) {
        switch (partType) {
            case ENGINE: return 0;
            case WHEEL: return 1;
            case SEAT: return 2;
            case BATTERY: return 3;
            case TRUNK: return 4;
            case LIGHT: return 5;
            default: return -1;
        }
    }
    
    // 检查特定部件是否有效
    public boolean isPartEffective(CarPart.PartType partType) {
        CarPart part = carEntity.getPart(getPartIndex(partType));
        return part != null && part.isInstalled() && !part.isBroken();
    }
    
    // 获取载具的整体碰撞箱
    public AABB getCarBoundingBox() {
        VoxelShape dynamicShape = getDynamicCollisionShape();
        if (dynamicShape.isEmpty()) {
            return getBaseBoundingBox();
        }
        
        // 将VoxelShape转换为AABB
        return dynamicShape.bounds();
    }
}
