package carminite.interfaces.extensions;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public interface IBlockEntityRenderDispatcherExtension {

	@Nullable
	default  <E extends BlockEntity, S extends BlockEntityRenderState> S carminite$tryExtractRenderState(E blockEntity, float partialTicks, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress, @Nullable Frustum frustum) {
		throw new AssertionError("Implemented via mixin");
	}
}
