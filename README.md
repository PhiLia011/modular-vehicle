# Modular Vehicle System（模块化载具系统）

Minecraft **Forge 1.20.1**（47.4.10）模组：可拆装部件的模块化载具。

> 历史说明：本项目最初按"NeoForge 1.20.1"立项，但该组合实际不存在
> （NeoForge 从 MC 1.20.2 起才发布）。经确认后目标定为 **Forge 1.20.1 + Java 17**。

## 当前状态（2026-09-23，已实测）

| 项目 | 状态 | 证据 |
|---|---|---|
| 编译 | ✅ `gradle build` 通过 | 产出 `build/libs/modular_vehicle-1.0.0.jar` |
| 专用服务器启动 | ✅ 通过 | 日志 `Done (6.6s)`，无 ERROR |
| 载具实体生成 | ✅ `summon modular_vehicle:car` 成功 | RCON 实测 |
| 部件 NBT 独立耐久 | ✅ 6 部件数据完整落盘 | `data get entity` 验证 |
| 实体属性注册 | ✅ | EntityAttributeCreationEvent |
| 网络通道 | ✅ SimpleChannel 注册 | 服务器日志无报错 |

## 尚未完成（TODO，对应需求文档）

- [ ] 部件槽位接入 `ItemStackHandler` / Capability（当前为 SimpleContainer 占位，6 槽，需求为 9 槽：引擎/车轮x4/车身外壳/座椅/油箱/电池）
- [ ] 拆装交互：潜行+右键拆卸（hitResult 部位判定）、手持部件右键安装
- [ ] 分层渲染与耐久四档可视化（>75% 正常 / 50-75% 裂纹 / 25-50% 偏暗 / <25% 冒烟）
- [ ] 拆轮后碰撞箱"塌陷"等动态碰撞联动
- [ ] 部件状态变更的联机同步
- [ ] 全部纹理为 0 字节占位（游戏内显示紫黑棋盘格，不崩溃）
- [ ] `runClient` 客户端冒烟测试未跑

## 构建

需要 JDK 17。仓库已含 Gradle Wrapper：

```powershell
.\gradlew.bat build        # 编译 + 打包
.\gradlew.bat runServer    # 启动开发服务器（首次需接受 eula）
```

产物：`build/libs/modular_vehicle-1.0.0.jar`

## 目录结构

```
src/main/java/com/example/modularvehicle/
├── ModularVehicle.java      # 主类
├── entity/                  # CarEntity、CarPart
├── item/                    # CarPartItem
├── block/                   # WorkbenchBlock
├── menu/                    # 菜单与 MenuType
├── client/                  # 屏幕、渲染事件、客户端 GUI 工具
├── screen/  (client)        # 各 GUI Screen
├── registry/                # 注册系统、JSON 部件定义
├── collision/               # 碰撞管理、效果、配置
├── command/                 # 调试命令
├── config/                  # ForgeConfigSpec 配置
└── network/                 # SimpleChannel 网络包
```

数据驱动部件定义：`src/main/resources/data/modular_vehicle/parts/*.json`（支持 `/reload` 热重载）。
