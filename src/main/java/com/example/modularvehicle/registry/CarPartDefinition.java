package com.example.modularvehicle.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.HashMap;
import java.util.Map;

public class CarPartDefinition {
    private final String id;
    private final String type;
    private final String displayName;
    private final int maxDurability;
    private final double weight;
    private final Map<String, Object> properties;
    private final CollisionBox collisionBox;
    private final Map<String, Integer> repairMaterials;
    private final String[] compatibleVehicles;
    private final ResourceLocation texture;
    private final ResourceLocation model;
    
    public CarPartDefinition(String id, String type, String displayName, int maxDurability, 
                           double weight, Map<String, Object> properties, CollisionBox collisionBox,
                           Map<String, Integer> repairMaterials, String[] compatibleVehicles,
                           ResourceLocation texture, ResourceLocation model) {
        this.id = id;
        this.type = type;
        this.displayName = displayName;
        this.maxDurability = maxDurability;
        this.weight = weight;
        this.properties = properties;
        this.collisionBox = collisionBox;
        this.repairMaterials = repairMaterials;
        this.compatibleVehicles = compatibleVehicles;
        this.texture = texture;
        this.model = model;
    }
    
    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public String getDisplayName() { return displayName; }
    public int getMaxDurability() { return maxDurability; }
    public double getWeight() { return weight; }
    public Map<String, Object> getProperties() { return properties; }
    public CollisionBox getCollisionBox() { return collisionBox; }
    public Map<String, Integer> getRepairMaterials() { return repairMaterials; }
    public String[] getCompatibleVehicles() { return compatibleVehicles; }
    public ResourceLocation getTexture() { return texture; }
    public ResourceLocation getModel() { return model; }
    
    // 碰撞箱内部类
    public static class CollisionBox {
        private final double x;
        private final double y;
        private final double z;
        private final double offsetX;
        private final double offsetY;
        private final double offsetZ;
        
        public CollisionBox(double x, double y, double z, double offsetX, double offsetY, double offsetZ) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }
        
        // Getters
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        public double getOffsetX() { return offsetX; }
        public double getOffsetY() { return offsetY; }
        public double getOffsetZ() { return offsetZ; }
    }
    
    // JSON解析器
    public static class Parser {
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        
        public static CarPartDefinition fromJson(String json) {
            JsonObject jsonObject = GSON.fromJson(json, JsonObject.class);
            
            String id = jsonObject.get("type").getAsString();
            String type = jsonObject.get("type").getAsString();
            String displayName = jsonObject.get("displayName").getAsString();
            int maxDurability = jsonObject.get("maxDurability").getAsInt();
            double weight = jsonObject.get("weight").getAsDouble();
            
            // 解析属性
            Map<String, Object> properties = new HashMap<>();
            JsonObject propertiesJson = jsonObject.getAsJsonObject("properties");
            for (String key : propertiesJson.keySet()) {
                properties.put(key, propertiesJson.get(key));
            }
            
            // 解析碰撞箱
            JsonObject collisionBoxJson = jsonObject.getAsJsonObject("collisionBox");
            CollisionBox collisionBox = new CollisionBox(
                collisionBoxJson.get("x").getAsDouble(),
                collisionBoxJson.get("y").getAsDouble(),
                collisionBoxJson.get("z").getAsDouble(),
                collisionBoxJson.get("offsetX").getAsDouble(),
                collisionBoxJson.get("offsetY").getAsDouble(),
                collisionBoxJson.get("offsetZ").getAsDouble()
            );
            
            // 解析修复材料
            Map<String, Integer> repairMaterials = new HashMap<>();
            JsonObject repairMaterialsJson = jsonObject.getAsJsonObject("repairMaterials");
            for (String key : repairMaterialsJson.keySet()) {
                repairMaterials.put(key, repairMaterialsJson.get(key).getAsInt());
            }
            
            // 解析兼容载具
            String[] compatibleVehicles = GSON.fromJson(jsonObject.get("compatibleVehicles"), String[].class);
            
            // 解析纹理和模型
            ResourceLocation texture = new ResourceLocation(jsonObject.get("texture").getAsString());
            ResourceLocation model = new ResourceLocation(jsonObject.get("model").getAsString());
            
            return new CarPartDefinition(id, type, displayName, maxDurability, weight, 
                                       properties, collisionBox, repairMaterials, 
                                       compatibleVehicles, texture, model);
        }
    }
}
