package carminite.mixin;

import carminite.interfaces.markers.IExtendedBoundingBoxBlockEntityRenderer;
import carminite.util.LevelRendererFrustumHolder;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {

	@Inject(
		method = "tryExtractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;Z)Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;shouldRender(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/phys/Vec3;)Z",
			shift = At.Shift.BEFORE
		),
		cancellable = true
	)
	private <E extends BlockEntity, S extends BlockEntityRenderState> void carminite$checkExtendedBounds(
		BlockEntity blockEntity,
		float partialTicks,
		ModelFeatureRenderer.CrumblingOverlay breakProgress,
		boolean isGloballyRendered,
		CallbackInfoReturnable<BlockEntityRenderState> cir,
		@Local(name = "renderer") BlockEntityRenderer<E, S> renderer
	) {
		Frustum frustum = LevelRendererFrustumHolder.getFrustum();
		if (frustum != null && renderer instanceof IExtendedBoundingBoxBlockEntityRenderer extended && !frustum.isVisible(extended.getRenderBoundingBox(blockEntity))) {
			cir.setReturnValue(null);
		}
	}
}
