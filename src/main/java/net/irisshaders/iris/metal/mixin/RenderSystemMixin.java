package net.irisshaders.iris.metal.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.metal.IrisMetalDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * RenderSystem mixin - 在 Metal 设备下接管渲染调用。
 *
 * <p>当 metallum 将后端切换为 Metal 时，{@code RenderSystem.getDevice()} 返回的
 * 设备的 {@code backendName()} 为 "Metal"。本 mixin 检测此状态并初始化
 * Iris Metal 后端。</p>
 */
@Mixin(RenderSystem.class)
public class RenderSystemMixin {

    @Inject(method = "initBackend", at = @At("RETURN"))
    private static void iris_metal$onBackendInit(CallbackInfo ci) {
        try {
            String backendName = RenderSystem.getDevice().getDeviceInfo().backendName();
            if ("Metal".equals(backendName)) {
                IrisMetalDevice.get(); // 触发 Metal 设备初始化
            }
        } catch (Throwable ignored) {
            // Metal 设备不可用，保持 OpenGL 路径
        }
    }
}
