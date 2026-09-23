package com.example.modularvehicle.network;

import com.example.modularvehicle.ModularVehicle;
import com.example.modularvehicle.client.ClientGuiHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

/**
 * 网络通道（Forge 1.20.1 SimpleChannel）。
 * 注意：本类在双端加载，任何客户端类引用必须经 DistExecutor 间接调用（见 ClientGuiHandler）。
 * TODO: 部件状态/碰撞箱重建的联机同步（需求文档第 7 项）。
 */
public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ModularVehicle.MOD_ID, "main"),
        () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(packetId++, WorkbenchOpenPacket.class,
            WorkbenchOpenPacket::encode, WorkbenchOpenPacket::decode, WorkbenchOpenPacket::handle);
        CHANNEL.registerMessage(packetId++, CarOpenPacket.class,
            CarOpenPacket::encode, CarOpenPacket::decode, CarOpenPacket::handle);
        CHANNEL.registerMessage(packetId++, PartInstallOpenPacket.class,
            PartInstallOpenPacket::encode, PartInstallOpenPacket::decode, PartInstallOpenPacket::handle);
        CHANNEL.registerMessage(packetId++, CarStatusOpenPacket.class,
            CarStatusOpenPacket::encode, CarStatusOpenPacket::decode, CarStatusOpenPacket::handle);
        CHANNEL.registerMessage(packetId++, ConfigOpenPacket.class,
            ConfigOpenPacket::encode, ConfigOpenPacket::decode, ConfigOpenPacket::handle);
    }

    // ---------- 工作台打开包 ----------
    public static class WorkbenchOpenPacket {
        private final BlockPos pos;

        public WorkbenchOpenPacket(BlockPos pos) { this.pos = pos; }

        public static void encode(WorkbenchOpenPacket msg, FriendlyByteBuf buf) { buf.writeBlockPos(msg.pos); }

        public static WorkbenchOpenPacket decode(FriendlyByteBuf buf) { return new WorkbenchOpenPacket(buf.readBlockPos()); }

        public static void handle(WorkbenchOpenPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientGuiHandler.openWorkbench(msg.pos)));
            ctx.get().setPacketHandled(true);
        }
    }

    // ---------- 载具打开包 ----------
    public static class CarOpenPacket {
        private final int entityId;

        public CarOpenPacket(int entityId) { this.entityId = entityId; }

        public static void encode(CarOpenPacket msg, FriendlyByteBuf buf) { buf.writeInt(msg.entityId); }

        public static CarOpenPacket decode(FriendlyByteBuf buf) { return new CarOpenPacket(buf.readInt()); }

        public static void handle(CarOpenPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientGuiHandler.openCar(msg.entityId)));
            ctx.get().setPacketHandled(true);
        }
    }

    // ---------- 部件安装打开包 ----------
    public static class PartInstallOpenPacket {
        private final BlockPos pos;
        private final String partType;

        public PartInstallOpenPacket(BlockPos pos, String partType) {
            this.pos = pos;
            this.partType = partType;
        }

        public static void encode(PartInstallOpenPacket msg, FriendlyByteBuf buf) {
            buf.writeBlockPos(msg.pos);
            buf.writeUtf(msg.partType);
        }

        public static PartInstallOpenPacket decode(FriendlyByteBuf buf) {
            return new PartInstallOpenPacket(buf.readBlockPos(), buf.readUtf());
        }

        public static void handle(PartInstallOpenPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientGuiHandler.openPartInstall(msg.pos, msg.partType)));
            ctx.get().setPacketHandled(true);
        }
    }

    // ---------- 载具状态打开包 ----------
    public static class CarStatusOpenPacket {
        private final int entityId;

        public CarStatusOpenPacket(int entityId) { this.entityId = entityId; }

        public static void encode(CarStatusOpenPacket msg, FriendlyByteBuf buf) { buf.writeInt(msg.entityId); }

        public static CarStatusOpenPacket decode(FriendlyByteBuf buf) { return new CarStatusOpenPacket(buf.readInt()); }

        public static void handle(CarStatusOpenPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientGuiHandler.openCarStatus(msg.entityId)));
            ctx.get().setPacketHandled(true);
        }
    }

    // ---------- 配置打开包 ----------
    public static class ConfigOpenPacket {
        private final int entityId;

        public ConfigOpenPacket(int entityId) { this.entityId = entityId; }

        public static void encode(ConfigOpenPacket msg, FriendlyByteBuf buf) { buf.writeInt(msg.entityId); }

        public static ConfigOpenPacket decode(FriendlyByteBuf buf) { return new ConfigOpenPacket(buf.readInt()); }

        public static void handle(ConfigOpenPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientGuiHandler.openConfig(msg.entityId)));
            ctx.get().setPacketHandled(true);
        }
    }
}
