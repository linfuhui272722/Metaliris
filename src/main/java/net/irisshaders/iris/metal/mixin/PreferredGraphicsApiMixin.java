package net.irisshaders.iris.metal.mixin;

import com.mojang.blaze3d.systems.GpuBackend;
import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.metal.IrisMetalDevice;
import net.minecraft.client.PreferredGraphicsApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * PreferredGraphicsApi mixin - 在 metallum Metal 环境下确保 Iris 感知 Metal 后端。
 *
 * <p>metallum 已经通过自己的 mixin 让 PreferredGraphicsApi.DEFAULT 指向 Metal。
 * 本 mixin 作为 Iris 侧的补充，在 Metal 后端激活时初始化 Iris Metal 设备。</p>
 */
@Mixin(PreferredGraphicsApi.class)
abstract class PreferredGraphicsApiMixin {

    @Inject(method = "getBackendsToTry", at = @At("RETURN"))
    private void iris_metal$onGetBackends(CallbackInfoReturnable<GpuBackend[]> cir) {
        // 当 metallum 注入了 Metal 后端后，检查并初始化 Iris Metal 设备
        try {
            if (RenderSystem.getDevice() != null) {
                String backendName = RenderSystem.getDevice().getDeviceInfo().backendName();
                if ("Metal".equals(backendName)) {
                    IrisMetalDevice.get();
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
