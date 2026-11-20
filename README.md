# OrienteeringMod 4.3

Minecraft Forge 1.12.2 定向越野比赛 Mod。提供检查点、起点/终点、路线、比赛管理、成统、轨迹和制图功能。

完整使用说明见 [docs/OrienteeringMod-4.3-说明书.md](docs/OrienteeringMod-4.3-说明书.md)。

## 仓库结构

```text
src/main/java/       Java 源码（Forge 1.12.2 SRG 名称）
src/main/resources/  Mod 资源、语言文件和 mcmod.info
docs/                使用说明
build.py             构建脚本
prepare.py           准备编译依赖
build.cmd            Windows 构建入口
```

## 构建

要求：

- Java 8
- Minecraft 1.12.2
- Forge 14.23.5.2861 的启动器配置 `Forge 1.12.2`
- Python 3

在本目录运行：

```bat
build.cmd
```

或：

```bash
python build.py
```

输出：

```text
build/OrienteeringMod-4.3-rebuilt.jar
```

默认 Minecraft 目录是 `%APPDATA%\.minecraft`；可用 `MC_HOME` 指定其他游戏目录，用 `JAVA8_HOME` 指定 Java 8。

首次构建时，`prepare.py` 会从已安装的 Forge 版本准备编译依赖，可能需要网络下载 Eclipse 编译器和 SpecialSource。

## 说明

源码由原版 4.x JAR 反编译恢复，保留 Forge/SRG 方法名；没有恢复原开发工程的注释和构建配置。提交时请不要包含 `build/`、本地生成的 jar、日志、IDE 配置或其他 mod 的文件。
