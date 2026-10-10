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
        method = "executeClassicTransparency(Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;Lcom/mojang/renderpearl/api/commands/RenderPass;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/WeatherEffectRenderer;render(Lnet/minecraft/client/renderer/state/level/WeatherRenderState;Lcom/mojang/renderpearl/api/commands/RenderPass;)V",
            shift = At.Shift.AFTER
        )
    )
    private void carminite$afterWeather(CallbackInfo ci) {
        Profiler.get().push("carminite_render_after_weather");
        new CarminiteRenderLevelStageEvent.AfterWeather((LevelRenderer) (Object) this, this.levelRenderState, null, this.levelRenderState.cameraRenderState.viewRotationMatrix).post();
        Profiler.get().pop();
    }
}