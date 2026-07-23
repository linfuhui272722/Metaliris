# Iris Metal Backend - 构建配置说明

## 项目结构

```
iris-metal-src/
├── build.gradle                    # Gradle 构建脚本
├── gradle.properties               # Gradle 属性
├── settings.gradle                 # Gradle 设置
├── src/main/java/net/irisshaders/iris/metal/
│   ├── IrisMetalDevice.java        # Metal 设备与命令队列管理
│   ├── IrisMetalRenderSystem.java  # GL 状态机模拟层（替代 IrisRenderSystem）
│   ├── IrisMetalPipelineManager.java # pipeline 调度中枢
│   ├── bridge/
│   │   └── IrisMetalNativeBridge.java # JNI/FFM 原生桥接
│   ├── shader/
│   │   └── MetalShaderCompiler.java # GLSL→SPIRV→MSL 编译器
│   ├── program/
│   │   ├── MetalCompiledProgram.java # 渲染管线状态对象
│   │   └── MetalVertexDescriptor.java # 顶点布局描述
│   ├── texture/
│   │   ├── MetalTexture.java       # 纹理实现
│   │   ├── MetalPixelFormat.java   # GL↔Metal 格式映射
│   │   ├── MetalTextureUploader.java # 像素上传
│   │   └── MetalTextureDownloader.java # 像素下载
│   ├── framebuffer/
│   │   ├── MetalFramebuffer.java   # 帧缓冲区
│   │   └── MetalRenderPassEncoder.java # 渲染命令编码器
│   ├── blending/
│   │   ├── MetalBlendState.java    # blend 状态
│   │   └── MetalSamplerStateCache.java # sampler state 缓存
│   ├── sampler/
│   │   └── MetalProgramSamplers.java # sampler 绑定管理
│   ├── image/
│   │   └── MetalProgramImages.java # image 绑定管理
│   ├── buffer/
│   │   └── MetalBuffer.java        # GPU buffer
│   └── uniform/
│       └── MetalUniformBlock.java  # uniform 管理
├── src/main/native/
│   └── IrisMetalNative.swift       # Swift 原生代码（编译为 libiris_metal.dylib）
└── src/main/resources/
    ├── fabric.mod.json             # Fabric mod 元数据
    └── iris_metal.mixins.json      # Mixin 配置
```

## 构建依赖

- **JDK 25**（MC 26.2 要求）
- **Fabric Loom** 1.16-SNAPSHOT
- **Fabric Loader** 0.19.3+
- **Minecraft** 26.2
- **Sodium** mc26.2-0.9.0-fabric
- **Metallum** 0.0.21（运行时依赖，提供 Metal 设备环境）
- **LWJGL**（含 SPIRV-Cross 绑定 `org.lwjgl.util.spvc`）

## 构建步骤

### 1. 编译原生库（仅 macOS）

```bash
cd src/main/native
swiftc -O -target arm64-apple-macos14.0 \
  -framework Foundation -framework Metal -framework MetalKit \
  -framework QuartzCore \
  -emit-library IrisMetalNative.swift \
  -o ../resources/natives/macos/libiris_metal.dylib
```

### 2. 构建 mod jar

```bash
# 设置 JDK 25
export JAVA_HOME=/path/to/jdk-25

# 构建
./gradlew build
```

构建产物在 `build/libs/` 目录下。

## 重要说明

1. **本代码无法在非 macOS 环境编译验证**：Metal 框架只在 macOS 上可用，
   原生 Swift 代码只能在 macOS 上编译。本环境（x86_64 Linux）无法编译或测试。

2. **需要 metallum 运行时**：Iris Metal 后端依赖 metallum 提供的 Metal 设备
   环境。在非 metallum 环境下，Iris 会回退到 OpenGL 路径。

3. **代码状态**：本源码是完整的架构实现，但未经运行时验证。在 macOS 上
   集成时需要：
   - 补全 native bridge 中所有 Swift 函数的实现
   - 调试 SPIRV-Cross 的 GLSL→MSL 转换边界情况
   - 验证与 metallum 命令编码的协调
   - 测试 MakeUp 光影的实际渲染效果

4. **与 Iris 主仓库的关系**：本 Metal 后端应作为 Iris 26.2 分支的补丁集成。
   集成方式是将 `metal/` 包加入 Iris 的 `common` 模块，并通过条件编译/
   运行时检测在 Metal 设备下启用 Metal 路径。
