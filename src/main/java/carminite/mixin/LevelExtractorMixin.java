package carminite.mixin;

import carminite.util.LevelRendererFrustumHolder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {

	@WrapOperation(
		method = "extract(Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/Camera;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;extractVisibleBlockEntities(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/state/level/LevelRenderState;)V"
		)
	)
	private void carminite$extractFrustum(
		LevelExtractor instance,
		Camera camera,
		float deltaPartialTick,
		LevelRenderState levelRenderState,
		Operation<Void> original,
		@Local(name = "cullFrustum") Frustum cullFrustum
	) {
		LevelRendererFrustumHolder.setFrustum(cullFrustum);
		try {
			original.call(instance, camera, deltaPartialTick, levelRenderState);
		} finally {
			LevelRendererFrustumHolder.clear();
		}
	}
}
