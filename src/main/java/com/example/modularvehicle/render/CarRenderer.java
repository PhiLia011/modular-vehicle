package com.example.modularvehicle.render;

import com.example.modularvehicle.entity.CarEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CarRenderer extends EntityRenderer<CarEntity> {
    private static final ResourceLocation CAR_TEXTURE = new ResourceLocation("modular_vehicle", "textures/entity/car.png");
    private static final CarModel CAR_MODEL = new CarModel(CarModel.createBodyLayer().bakeRoot());
    
    public CarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
        this.shadowStrength = 0.8f;
    }

    @Override
    public ResourceLocation getTextureLocation(CarEntity car) {
        return CAR_TEXTURE;
    }

    @Override
    public void render(CarEntity car, float entityYaw, float partialTick, PoseStack poseStack, 
                     MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // 使用标准实体模型坐标系；模型自身朝 -Z，因此这里按载具朝向旋转。
        poseStack.translate(0.0F, 1.501F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        CAR_MODEL.setupAnim(car, 0.0F, 0.0F, partialTick, 0.0F, 0.0F);
        CAR_MODEL.renderCar(car, poseStack,
            bufferSource.getBuffer(CAR_MODEL.renderType(CAR_TEXTURE)),
            packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        // 调用父类渲染（名称标签等）
        super.render(car, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

}
