# Ari - 基于现代化 Minecraft 服务器插件 (正在持续更新中)

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange)](https://adoptium.net/)
[![Folia](https://img.shields.io/badge/Folia-1.21.3%2B-brightgreen)](https://papermc.io/folia)
[![PaperMC](https://img.shields.io/badge/PaperMC-1.21.3%2B-brightgreen)](https://papermc.io/)
[![Vault Dependency](https://img.shields.io/badge/Vault-Required-blue)](https://github.com/MilkBowl/Vault)
[![ARILIB Dependency](https://img.shields.io/badge/ARILIB-Required-blue)](https://github.com/SuperArilo/ARILIB)


✨ 一个高度兼容的Minecraft插件，完美支持 **PaperMC** 和 **Folia** 核心，兼容 1.21.3+  
📦 提供家园传送、地标系统、玩家交互等核心功能 + 实用游戏工具
## ✨ 核心特性
| 功能             | 描述                         |
|----------------|----------------------------|
| ✅ **双核兼容**     | 同时支持 PaperMC 和 Folia 服务端核心 |
| 🗺️ **GUI 系统** | 全功能可视化界面，支持自定义物品图标/名称/排序权重 |
| 💾 **多存储**     | 支持 SQLite 和 MySQL 数据库存储    |
| ⚙️ **深度定制**    | 所有功能均可通过配置文件高度自定义          |
## 🚀 功能列表
| 功能                 | 描述                                               |
|--------------------|--------------------------------------------------|
| 🏠 `Home`          | 多家园系统 + GUI管理，支持图标/名称/权重自定义 `/home`              |
| 🗺️ `Warp`         | 地标系统 + GUI管理，支持传送权限/经济消耗/图标定制 `/warp`            |
| ↩️ `Back`          | 传送点或死亡位置返回 `/back`                               |
| 📡 `Tpa`           | 玩家间传送请求 `/tpa`                                   |
| 🧭 `Spawn`         | 世界出生点传送 `/spawn`                                 |
| 🎲 `Rtp`           | 随机安全传送 `/rtp`                                    |
| ⏳ `时间控制`           | 实时调整游戏时间流速 `/time <period>`                      |
| 🌙 `夜晚加速`          | 多人睡眠加速跳夜                                         |
| 📋 `Tab自定义`        | 完全自定义玩家Tab列表显示格式                                 |
| 📃 `白名单功能(Zako)`   | 自定义服务器玩家的管理方式 `/zako <function>`                 |
| 👓️ `物品属性修改`       | 可快捷修改物品名称和lore属性 `/itemname` `/itemlore`         |
| 📺 `展示物品到聊天栏`      | 可将自己手中的物品展示到聊天栏 `/itemshow`                      |
| 🪑 `Sit`           | 可在指定的楼梯或者半砖上坐下                                   |
| 👨‍🦽 `Player Sit` | 玩家之间可相互骑乘                                        |
| 💥 `防爆保护`          | 防止服务器被熊。可自定义配置                                   |
| 🍞 `快速收获农作物`       | 副手拿种子，主手锄头右键收获更快！                                |
| 💀 `玩家自定义死亡信息显示`   | 可配置游戏中玩家自定义死亡信息                                  |
| 🧡 `实体血条显示`        | 玩家攻击可受伤的实体将会显示实体对应的血量                            |
| 📕 `附魔指令`          | 可通过指令快速对手中的物品进行附魔                                |
| 🧰 `快捷末影箱`         | 通过指令打开自己或其他玩家的的随身末影箱 `/enderchest <player_name>` |
| 🛠️ `服务器维护模式`      | 启动服务器维护模式 `/maintenance`                         |
## 🗯️ 权限列表
| 功能                            | 描述              |
|-------------------------------|-----------------|
| `ari.command.home`            | 打开自己的家列表        |
| `ari.command.sethome`         | 设置家位置           |
| `ari.command.warp`            | 打开服务器公共传送点      |
| `ari.command.setwarp`         | 设置公共传送点         |
| `ari.command.back`            | 返回上一次传送位置       |
| `ari.command.tpa`             | 传送到其他玩家         |
| `ari.command.tpahere`         | 请求其他玩家传送到自己     |
| `ari.command.tpaaccept`       | 接受传送请求          |
| `ari.command.tparefuse`       | 拒绝传送请求          |
| `ari.command.spawn`           | 传送至出生点          |
| `ari.command.setspawn`        | 设置出生点           |
| `ari.command.rtp`             | 随机传送            |
| `ari.command.time`            | 控制时间流逝          |
| `ari.command.itemname`        | 修改物品名称          |
| `ari.command.itemlore`        | 修改物品描述          |
| `ari.command.itemshow`        | 展示物品            |
| `ari.command.zako`            | 服务器玩家设置         |
| `ari.command.enchant`         | 使用自定义附魔         |
| `ari.command.reload`          | 重载插件配置          |
| `ari.command.maintenance`     | 启动服务器维护模式       |
| `ari.count.home.<数量>`         | 设置玩家可拥有的最大家数量限制 |
| `ari.count.warp.<数量>`         | 指定玩家可添加地标的最大数量  |
| `ari.keep-inventory`          | 玩家死亡时保留背包内的物品   |
| `ari.keep-experience`         | 玩家死亡时保留经验值和等级   |
| `ari.cooldown.<传送类型>`         | 指定某种传送类型没有CD时间  |
| `ari.totem.inventory-trigger` | 可将不死图腾放在背包里直接使用 |
## 🛠️ 安装指南

### 环境要求
- Folia 1.21.3+ 服务器核心
- Java 21 或更高版本
- [Vault](https://github.com/MilkBowl/Vault) 前置插件
- [ARILIB](https://github.com/SuperArilo/ARILIB) 前置插件

### 安装步骤
1. 将插件和前置Lib放入 `plugins/` 目录
2. 重启服务器
