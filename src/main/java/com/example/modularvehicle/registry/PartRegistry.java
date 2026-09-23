package com.example.modularvehicle.registry;

import com.example.modularvehicle.ModularVehicle;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 部件定义注册表：从 data/modular_vehicle/parts/*.json 加载部件定义。
 * 注册到服务器重载监听器，支持 /reload 与 F3+T 热重载（需求文档第 6 项）。
 */
public class PartRegistry extends SimplePreparableReloadListener<Map<String, CarPartDefinition>> {
    private static final PartRegistry INSTANCE = new PartRegistry();
    private final Map<String, CarPartDefinition> definitions = new ConcurrentHashMap<>();
    private final Map<String, CarPartDefinition> definitionsById = new ConcurrentHashMap<>();

    private PartRegistry() {}

    public static PartRegistry getInstance() {
        return INSTANCE;
    }

    public void register(String id, CarPartDefinition definition) {
        definitions.put(id, definition);
        definitionsById.put(definition.getId(), definition);
    }

    public CarPartDefinition getDefinition(String id) {
        return definitionsById.get(id);
    }

    public CarPartDefinition getDefinitionByType(String type) {
        return definitions.get(type);
    }

    public Map<String, CarPartDefinition> getAllDefinitions() {
        return Maps.newHashMap(definitionsById);
    }

    public boolean contains(String id) {
        return definitionsById.containsKey(id);
    }

    @Override
    protected Map<String, CarPartDefinition> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, CarPartDefinition> loaded = new HashMap<>();
        // 遍历所有命名空间下 parts/ 目录中的 JSON 文件
        for (Map.Entry<ResourceLocation, net.minecraft.server.packs.resources.Resource> entry :
                resourceManager.listResources("parts", rl -> rl.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation location = entry.getKey();
            try (var in = entry.getValue().open()) {
                String json = new String(in.readAllBytes());
                CarPartDefinition definition = CarPartDefinition.Parser.fromJson(json);
                loaded.put(definition.getId(), definition);
            } catch (Exception e) {
                System.err.println("Failed to load part definition: " + location + " - " + e.getMessage());
            }
        }
        return loaded;
    }

    @Override
    protected void apply(Map<String, CarPartDefinition> loaded, ResourceManager resourceManager, ProfilerFiller profiler) {
        definitions.clear();
        definitionsById.clear();
        for (CarPartDefinition definition : loaded.values()) {
            register(definition.getId(), definition);
        }
        System.out.println("Loaded " + definitions.size() + " part definitions");
    }

    /** 注册到服务器数据重载流程（Forge 主事件总线的 AddReloadListenerEvent）。 */
    public static void register() {
        MinecraftForge.EVENT_BUS.register(Listener.class);
    }

    static class Listener {
        @SubscribeEvent
        public static void onAddReloadListeners(AddReloadListenerEvent event) {
            event.addListener(INSTANCE);
        }
    }
}
