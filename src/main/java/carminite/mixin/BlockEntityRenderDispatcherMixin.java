package carminite.mixin;

import carminite.interfaces.extensions.IBlockEntityRenderDispatcherExtension;
import carminite.interfaces.markers.IExtendedBoundingBoxBlockEntityRenderer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin implements IBlockEntityRenderDispatcherExtension {

	@Shadow
	public abstract @Nullable <E extends BlockEntity, S extends BlockEntityRenderState> BlockEntityRenderer<E, S> getRenderer(E blockEntity);

	@Shadow
	private Vec3 cameraPos;

	@WrapMethod(method = "tryExtractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;")
	private <E extends BlockEntity, S extends BlockEntityRenderState> @Nullable S carminite$redirectTryExtractRenderState(
		E blockEntity,
		float partialTicks,
		ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress,
		Operation<S> original
	) {
		return carminite$tryExtractRenderState(blockEntity, partialTicks, breakProgress, null);
	}

	@Nullable
	public <E extends BlockEntity, S extends BlockEntityRenderState> S carminite$tryExtractRenderState(E blockEntity, float partialTicks, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress, @Nullable Frustum frustum) {
		BlockEntityRenderer<E, S> renderer = this.getRenderer(blockEntity);
		if (renderer == null) {
			return null;
		} else if (!blockEntity.hasLevel() || !blockEntity.getType().isValid(blockEntity.getBlockState())) {
			return null;
		} else if (frustum != null && renderer instanceof IExtendedBoundingBoxBlockEntityRenderer extended && !frustum.isVisible(extended.getRenderBoundingBox(blockEntity))) {
			return null;
		} else if (!renderer.shouldRender(blockEntity, this.cameraPos)) {
			return null;
		} else {
			Vec3 cameraPosition = this.cameraPos;
			S state = renderer.createRenderState();
			renderer.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
			return state;
		}
	}
}
