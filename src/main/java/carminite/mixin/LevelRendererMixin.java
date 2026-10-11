package carminite.mixin;

import carminite.events.modified.CarminiteRenderLevelStageEvent;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.profiling.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Inject(
        method = "lambda$addMainPass$0(Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;ZLnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;ZZ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;executeOutline(Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;)V"
        )
    )
    private void carminite$afterWeather(CallbackInfo ci) {
        // Both transparency paths have closed their render passes before outlines begin.
        Profiler.get().push("carminite_render_after_weather");
        new CarminiteRenderLevelStageEvent.AfterWeather((LevelRenderer) (Object) this, this.levelRenderState, null, this.levelRenderState.cameraRenderState.viewRotationMatrix).post();
        Profiler.get().pop();
    }
}
