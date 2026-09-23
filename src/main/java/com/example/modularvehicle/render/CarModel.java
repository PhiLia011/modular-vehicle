package com.example.modularvehicle.render;

import com.example.modularvehicle.entity.CarEntity;
import com.example.modularvehicle.entity.CarPart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * 方块风格载具模型。原点放在载具底部，-Z 是车头方向。
 */
public class CarModel extends EntityModel<CarEntity> {
    private final ModelPart root;
    private final ModelPart chassis;
    private final ModelPart shell;
    private final ModelPart engine;
    private final ModelPart seat;
    private final ModelPart fuelTank;
    private final ModelPart battery;
    private final ModelPart wheelFrontLeft;
    private final ModelPart wheelFrontRight;
    private final ModelPart wheelRearLeft;
    private final ModelPart wheelRearRight;

    public CarModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("car");
        chassis = root.getChild("chassis");
        shell = root.getChild("shell");
        engine = root.getChild("engine");
        seat = root.getChild("seat");
        fuelTank = root.getChild("fuel_tank");
        battery = root.getChild("battery");
        wheelFrontLeft = root.getChild("wheel_front_left");
        wheelFrontRight = root.getChild("wheel_front_right");
        wheelRearLeft = root.getChild("wheel_rear_left");
        wheelRearRight = root.getChild("wheel_rear_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition car = root.addOrReplaceChild("car", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        car.addOrReplaceChild("chassis", CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-8.0F, -6.0F, -13.0F, 16.0F, 4.0F, 26.0F), PartPose.ZERO);

        // 车壳包含车身、座舱和车顶；拆下外壳时能直接看到内部部件。
        car.addOrReplaceChild("shell", CubeListBuilder.create()
            .texOffs(0, 30)
            .addBox(-8.0F, -16.0F, -13.0F, 16.0F, 10.0F, 26.0F)
            .texOffs(0, 74)
            .addBox(-6.0F, -24.0F, -8.0F, 12.0F, 8.0F, 16.0F), PartPose.ZERO);

        car.addOrReplaceChild("engine", CubeListBuilder.create()
            .texOffs(84, 16)
            .addBox(-4.0F, -14.0F, -11.0F, 8.0F, 8.0F, 6.0F), PartPose.ZERO);

        car.addOrReplaceChild("seat", CubeListBuilder.create()
            .texOffs(84, 64)
            .addBox(-5.0F, -12.0F, -4.0F, 10.0F, 3.0F, 8.0F)
            .texOffs(84, 86)
            .addBox(-5.0F, -16.0F, 2.0F, 10.0F, 7.0F, 2.0F), PartPose.ZERO);

        car.addOrReplaceChild("fuel_tank", CubeListBuilder.create()
            .texOffs(84, 32)
            .addBox(-4.0F, -14.0F, 6.0F, 8.0F, 6.0F, 6.0F), PartPose.ZERO);

        car.addOrReplaceChild("battery", CubeListBuilder.create()
            .texOffs(84, 48)
            .addBox(1.0F, -13.0F, -4.0F, 4.0F, 4.0F, 6.0F), PartPose.ZERO);

        car.addOrReplaceChild("wheel_front_left", wheelCubes(), PartPose.offset(-6.0F, -3.0F, -9.0F));
        car.addOrReplaceChild("wheel_front_right", wheelCubes(), PartPose.offset(6.0F, -3.0F, -9.0F));
        car.addOrReplaceChild("wheel_rear_left", wheelCubes(), PartPose.offset(-6.0F, -3.0F, 8.0F));
        car.addOrReplaceChild("wheel_rear_right", wheelCubes(), PartPose.offset(6.0F, -3.0F, 8.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    private static CubeListBuilder wheelCubes() {
        return CubeListBuilder.create()
            .texOffs(84, 0)
            .addBox(-2.5F, -3.0F, -3.0F, 5.0F, 6.0F, 6.0F);
    }

    @Override
    public void setupAnim(CarEntity car, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        root.visible = true;
        chassis.visible = true;

        shell.visible = isInstalled(car, CarPart.SLOT_SHELL);
        engine.visible = isInstalled(car, CarPart.SLOT_ENGINE);
        seat.visible = isInstalled(car, CarPart.SLOT_SEAT);
        fuelTank.visible = isInstalled(car, CarPart.SLOT_FUEL_TANK);
        battery.visible = isInstalled(car, CarPart.SLOT_BATTERY);

        wheelFrontLeft.visible = isInstalled(car, CarPart.SLOT_WHEEL_FL);
        wheelFrontRight.visible = isInstalled(car, CarPart.SLOT_WHEEL_FR);
        wheelRearLeft.visible = isInstalled(car, CarPart.SLOT_WHEEL_RL);
        wheelRearRight.visible = isInstalled(car, CarPart.SLOT_WHEEL_RR);

        wheelFrontLeft.xRot = limbSwing * 0.45F;
        wheelFrontRight.xRot = limbSwing * 0.45F;
        wheelRearLeft.xRot = limbSwing * 0.45F;
        wheelRearRight.xRot = limbSwing * 0.45F;
    }

    /**
     * 按实际部件状态分件渲染；耐久度用颜色叠加，方便直接看出损坏程度。
     */
    public void renderCar(CarEntity car, PoseStack poseStack, VertexConsumer vertexConsumer,
                          int packedLight, int packedOverlay) {
        renderPart(chassis, poseStack, vertexConsumer, packedLight, packedOverlay,
            new float[] {1.0F, 1.0F, 1.0F});
        renderPart(shell, poseStack, vertexConsumer, packedLight, packedOverlay,
            durabilityTint(car, CarPart.SLOT_SHELL));
        renderPart(engine, poseStack, vertexConsumer, packedLight, packedOverlay,
            durabilityTint(car, CarPart.SLOT_ENGINE));
        renderPart(seat, poseStack, vertexConsumer, packedLight, packedOverlay,
            durabilityTint(car, CarPart.SLOT_SEAT));
        renderPart(fuelTank, poseStack, vertexConsumer, packedLight, packedOverlay,
            durabilityTint(car, CarPart.SLOT_FUEL_TANK));
        renderPart(battery, poseStack, vertexConsumer, packedLight, packedOverlay,
            durabilityTint(car, CarPart.SLOT_BATTERY));

        renderPart(wheelFrontLeft, poseStack, vertexConsumer, packedLight, packedOverlay,
            durabilityTint(car, CarPart.SLOT_WHEEL_FL));
        renderPart(wheelFrontRight, poseStack, vertexConsumer, packedLight, packedOverlay,
            durabilityTint(car, CarPart.SLOT_WHEEL_FR));
        renderPart(wheelRearLeft, poseStack, vertexConsumer, packedLight, packedOverlay,
            durabilityTint(car, CarPart.SLOT_WHEEL_RL));
        renderPart(wheelRearRight, poseStack, vertexConsumer, packedLight, packedOverlay,
            durabilityTint(car, CarPart.SLOT_WHEEL_RR));
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private static boolean isInstalled(CarEntity car, int slot) {
        CarPart part = car.getPart(slot);
        return part != null && car.isPartInstalled(slot);
    }

    private static float[] durabilityTint(CarEntity car, int slot) {
        CarPart part = car.getPart(slot);
        if (part == null || part.getMaxDurability() == 0) {
            return new float[] {0.25F, 0.25F, 0.25F};
        }

        float durability = (float) part.getDurability() / part.getMaxDurability();
        if (durability > 0.75F) {
            return new float[] {1.0F, 1.0F, 1.0F};
        } else if (durability > 0.5F) {
            return new float[] {1.0F, 0.85F, 0.65F};
        } else if (durability > 0.25F) {
            return new float[] {0.95F, 0.65F, 0.5F};
        } else if (durability > 0.0F) {
            return new float[] {0.8F, 0.38F, 0.34F};
        }
        return new float[] {0.25F, 0.25F, 0.25F};
    }

    private void renderPart(ModelPart part, PoseStack poseStack, VertexConsumer vertexConsumer,
                            int packedLight, int packedOverlay, float[] tint) {
        if (part.visible) {
            part.render(poseStack, vertexConsumer, packedLight, packedOverlay,
                tint[0], tint[1], tint[2], 1.0F);
        }
    }
}
