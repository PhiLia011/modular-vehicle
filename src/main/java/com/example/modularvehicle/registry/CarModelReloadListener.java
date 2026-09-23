package com.example.modularvehicle.registry;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * 载具模型重载监听器（占位实现）。
 * TODO: 加载载具主体模型与部件模型（JSON 或程序化生成）。
 */
public class CarModelReloadListener extends SimplePreparableReloadListener<Void> {

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void data, ResourceManager resourceManager, ProfilerFiller profiler) {
        // 模型重载：当前为占位
    }
}
