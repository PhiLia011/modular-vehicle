package com.example.modularvehicle.render;

import com.example.modularvehicle.entity.CarEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class CarRenderer extends EntityRenderer<CarEntity> {
    private static final ResourceLocation CAR_TEXTURE = new ResourceLocation("modular_vehicle", "textures/entity/car.png");
    private static final ResourceLocation CAR_PARTS_TEXTURE = new ResourceLocation("modular_vehicle", "textures/entity/car_parts.png");
    
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
        
        // 应用载具旋转和位置
        poseStack.translate(0, 0.35, 0); // 调整Y轴偏移
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        
        // 渲染主体
        renderCarBody(car, poseStack, bufferSource, packedLight);
        
        // 渲染部件
        renderCarParts(car, poseStack, bufferSource, packedLight);
        
        // 渲染内部结构
        renderInternalStructure(car, poseStack, bufferSource, packedLight);
        
        // 渲染轮廓
        renderCarOutline(car, poseStack, bufferSource, packedLight);
        
        poseStack.popPose();
        
        // 调用父类渲染
        super.render(car, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderCarBody(CarEntity car, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(CAR_TEXTURE));
        
        // 创建载具主体立方体
        // 底盘
        renderCube(poseStack, vertexConsumer, 
            -0.8f, 0.0f, -0.4f,  // x1, y1, z1
            0.8f, 0.2f, 0.4f,   // x2, y2, z2
            1.0f, 1.0f, 1.0f,   // r, g, b
            packedLight);
        
        // 车身
        renderCube(poseStack, vertexConsumer,
            -0.7f, 0.2f, -0.3f,
            0.7f, 0.8f, 0.3f,
            0.8f, 0.8f, 0.8f,
            packedLight);
    }

    private void renderCarParts(CarEntity car, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(CAR_PARTS_TEXTURE));
        
        // 渲染引擎（根据耐久度变色）
        renderEngineWithDurability(car, poseStack, vertexConsumer, packedLight);
        
        // 渲染轮子
        renderWheels(car, poseStack, vertexConsumer, packedLight);
        
        // 渲染灯光
        renderLights(car, poseStack, vertexConsumer, packedLight);
    }

    private void renderEngineWithDurability(CarEntity car, PoseStack poseStack, 
                                          VertexConsumer vertexConsumer, int packedLight) {
        // 获取引擎耐久度
        float durability = (float) car.getPart(0).getDurability() / car.getPart(0).getMaxDurability();
        
        float r, g, b;
        if (durability > 0.75f) {
            // >75%: 绿色
            r = 0.0f; g = 1.0f; b = 0.0f;
        } else if (durability > 0.5f) {
            // 50-75%: 轻微损坏，轻微偏黄
            r = 0.5f; g = 1.0f; b = 0.0f;
        } else if (durability > 0.25f) {
            // 25-50%: 黄色偏移
            r = 1.0f; g = 1.0f; b = 0.0f;
        } else if (durability > 0.0f) {
            // <25%: 红色
            r = 1.0f; g = 0.0f; b = 0.0f;
        } else {
            // =0%: 无效/透明
            r = 0.3f; g = 0.3f; b = 0.3f;
        }
        
        // 渲染引擎部件
        renderCube(poseStack, vertexConsumer,
            -0.3f, 0.3f, -0.2f,
            0.3f, 0.6f, 0.2f,
            r, g, b,
            packedLight);
    }

    private void renderWheels(CarEntity car, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight) {
        // 前轮
        renderCube(poseStack, vertexConsumer,
            -0.9f, 0.0f, -0.3f,
            -0.6f, 0.3f, -0.1f,
            0.2f, 0.2f, 0.2f,
            packedLight);
        
        // 后轮
        renderCube(poseStack, vertexConsumer,
            0.6f, 0.0f, -0.3f,
            0.9f, 0.3f, -0.1f,
            0.2f, 0.2f, 0.2f,
            packedLight);
    }

    private void renderLights(CarEntity car, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight) {
        // 前灯
        renderCube(poseStack, vertexConsumer,
            -0.7f, 0.6f, -0.35f,
            -0.5f, 0.8f, -0.25f,
            1.0f, 1.0f, 0.8f,
            packedLight);
        
        // 后灯
        renderCube(poseStack, vertexConsumer,
            0.5f, 0.6f, -0.35f,
            0.7f, 0.8f, -0.25f,
            1.0f, 0.3f, 0.3f,
            packedLight);
    }

    private void renderInternalStructure(CarEntity car, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(CAR_PARTS_TEXTURE));
        
        // 渲染座椅框架
        // 驾驶座
        renderCube(poseStack, vertexConsumer,
            0.2f, 0.25f, -0.2f,
            0.4f, 0.45f, 0.0f,
            0.6f, 0.4f, 0.2f,
            packedLight);
        
        // 副驾驶
        renderCube(poseStack, vertexConsumer,
            -0.4f, 0.25f, -0.2f,
            -0.2f, 0.45f, 0.0f,
            0.6f, 0.4f, 0.2f,
            packedLight);
        
        // 内部结构框架
        renderCube(poseStack, vertexConsumer,
            -0.6f, 0.3f, 0.1f,
            0.6f, 0.5f, 0.3f,
            0.4f, 0.4f, 0.6f,
            packedLight);
    }

    private void renderCarOutline(CarEntity car, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // 使用轮廓渲染类型
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.outline(CAR_TEXTURE));
        
        // 渲染载具轮廓
        renderCubeOutline(poseStack, vertexConsumer,
            -0.8f, 0.0f, -0.4f,
            0.8f, 0.8f, 0.4f,
            0.0f, 1.0f, 1.0f, // 青色轮廓
            packedLight);
    }

    private void renderCube(PoseStack poseStack, VertexConsumer vertexConsumer, 
                           float x1, float y1, float z1, float x2, float y2, float z2,
                           float r, float g, float b, int packedLight) {
        // 简化的立方体渲染
        // 前面
        vertexConsumer.vertex(poseStack.last().pose(), x1, y1, z1).color(r, g, b, 1.0f).uv(0, 0).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), x2, y1, z1).color(r, g, b, 1.0f).uv(1, 0).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), x2, y2, z1).color(r, g, b, 1.0f).uv(1, 1).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), x1, y2, z1).color(r, g, b, 1.0f).uv(0, 1).endVertex();
        
        // 后面
        vertexConsumer.vertex(poseStack.last().pose(), x1, y1, z2).color(r, g, b, 1.0f).uv(0, 0).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), x1, y2, z2).color(r, g, b, 1.0f).uv(0, 1).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), x2, y2, z2).color(r, g, b, 1.0f).uv(1, 1).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), x2, y1, z2).color(r, g, b, 1.0f).uv(1, 0).endVertex();
        
        // 其他面...
    }

    private void renderCubeOutline(PoseStack poseStack, VertexConsumer vertexConsumer,
                                 float x1, float y1, float z1, float x2, float y2, float z2,
                                 float r, float g, float b, int packedLight) {
        // 简化的轮廓渲染
        // 底边
        vertexConsumer.vertex(poseStack.last().pose(), x1, y1, z1).color(r, g, b, 1.0f).uv(0, 0).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), x2, y1, z1).color(r, g, b, 1.0f).uv(1, 0).endVertex();
        
        // 顶边
        vertexConsumer.vertex(poseStack.last().pose(), x1, y2, z1).color(r, g, b, 1.0f).uv(0, 1).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), x2, y2, z1).color(r, g, b, 1.0f).uv(1, 1).endVertex();
        
        // 前边
        vertexConsumer.vertex(poseStack.last().pose(), x1, y1, z2).color(r, g, b, 1.0f).uv(0, 0).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), x1, y2, z2).color(r, g, b, 1.0f).uv(0, 1).endVertex();
        
        // 后边
        vertexConsumer.vertex(poseStack.last().pose(), x2, y1, z2).color(r, g, b, 1.0f).uv(1, 0).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), x2, y2, z2).color(r, g, b, 1.0f).uv(1, 1).endVertex();
    }
}
