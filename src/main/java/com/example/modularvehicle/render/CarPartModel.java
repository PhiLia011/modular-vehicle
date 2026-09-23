package com.example.modularvehicle.render;

import com.example.modularvehicle.entity.CarPart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;

public class CarPartModel implements BakedModel {
    private final CarPart.PartType partType;
    private final ResourceLocation texture;
    private final List<BakedQuad> quads = new ArrayList<>();
    
    public CarPartModel(CarPart.PartType partType, ResourceLocation texture) {
        this.partType = partType;
        this.texture = texture;
        this.generateQuads();
    }

    private void generateQuads() {
        switch (partType) {
            case ENGINE:
                generateEngineQuads();
                break;
            case WHEEL:
                generateWheelQuads();
                break;
            case SEAT:
                generateSeatQuads();
                break;
            case BATTERY:
                generateBatteryQuads();
                break;
            case SHELL:
                generateShellQuads();
                break;
            case FUEL_TANK:
                generateFuelTankQuads();
                break;
        }
    }

    private void generateEngineQuads() {
        // 引擎立方体
        addCube(0, 0, 0, 1, 1, 1, 0.8f, 0.2f, 0.2f);
    }

    private void generateWheelQuads() {
        // 轮子圆柱体（简化为立方体）
        addCube(0, 0, 0, 0.8f, 0.8f, 0.8f, 0.1f, 0.1f, 0.1f);
    }

    private void generateSeatQuads() {
        // 座位立方体
        addCube(0, 0, 0, 1, 0.6f, 1, 0.6f, 0.3f, 0.6f);
    }

    private void generateBatteryQuads() {
        // 电池立方体
        addCube(0, 0, 0, 0.8f, 0.4f, 1.2f, 0.2f, 0.8f, 0.2f);
    }

    private void generateShellQuads() {
        // 行李箱立方体
        addCube(0, 0, 0, 1, 0.8f, 1, 0.6f, 0.4f, 0.6f);
    }

    private void generateFuelTankQuads() {
        // 灯光立方体
        addCube(0, 0, 0, 0.3f, 0.3f, 0.3f, 1.0f, 1.0f, 0.8f);
    }

    private void addCube(float x1, float y1, float z1, float x2, float y2, float z2, 
                        float r, float g, float b) {
        // 前面
        quads.add(createQuad(
            new Vector3f(x1, y1, z1), new Vector3f(x2, y1, z1), 
            new Vector3f(x2, y2, z1), new Vector3f(x1, y2, z1),
            Direction.SOUTH, r, g, b
        ));
        
        // 后面
        quads.add(createQuad(
            new Vector3f(x1, y1, z2), new Vector3f(x1, y2, z2), 
            new Vector3f(x2, y2, z2), new Vector3f(x2, y1, z2),
            Direction.NORTH, r, g, b
        ));
        
        // 顶面
        quads.add(createQuad(
            new Vector3f(x1, y2, z1), new Vector3f(x2, y2, z1), 
            new Vector3f(x2, y2, z2), new Vector3f(x1, y2, z2),
            Direction.UP, r, g, b
        ));
        
        // 底面
        quads.add(createQuad(
            new Vector3f(x1, y1, z1), new Vector3f(x1, y1, z2), 
            new Vector3f(x2, y1, z2), new Vector3f(x2, y1, z1),
            Direction.DOWN, r, g, b
        ));
        
        // 左面
        quads.add(createQuad(
            new Vector3f(x1, y1, z1), new Vector3f(x1, y2, z1), 
            new Vector3f(x1, y2, z2), new Vector3f(x1, y1, z2),
            Direction.WEST, r, g, b
        ));
        
        // 右面
        quads.add(createQuad(
            new Vector3f(x2, y1, z1), new Vector3f(x2, y1, z2), 
            new Vector3f(x2, y2, z2), new Vector3f(x2, y2, z1),
            Direction.EAST, r, g, b
        ));
    }

    private BakedQuad createQuad(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, 
                                Direction direction, float r, float g, float b) {
        // TODO: 完整四边形烘焙（FaceBakery + BlockElementFace），当前返回空 quad 占位
        return new BakedQuad(new int[0], 0, direction, null, false); // TODO_SPRITE_MARK: sprite 占位为 null，待接入真实贴图
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction direction, RandomSource random) {
        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }


    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return null;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, 
                      int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        Matrix4f poseMatrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();
        
        for (BakedQuad quad : quads) {
            vertexConsumer.vertex(poseMatrix, quad.getVertices()[0], quad.getVertices()[1], quad.getVertices()[2])
                .color(red, green, blue, alpha)
                .uv(quad.getSprite().getU(quad.getVertices()[0]), quad.getSprite().getV(quad.getVertices()[1]))
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normalMatrix, quad.getVertices()[3], quad.getVertices()[4], quad.getVertices()[5])
                .endVertex();
        }
    }
}
