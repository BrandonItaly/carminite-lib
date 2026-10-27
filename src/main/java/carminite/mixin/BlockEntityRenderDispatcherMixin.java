package carminite.mixin;

import carminite.interfaces.markers.IExtendedBoundingBoxBlockEntityRenderer;
import carminite.interfaces.extensions.IBlockEntityRenderDispatcherExtension;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin implements IBlockEntityRenderDispatcherExtension {
    @Shadow
    public abstract @Nullable <E extends BlockEntity, S extends BlockEntityRenderState> BlockEntityRenderer<E, S> getRenderer(E blockEntity);

    @Shadow
    public abstract <E extends BlockEntity, S extends BlockEntityRenderState> @Nullable S tryExtractRenderState(E blockEntity, float partialTicks, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress, boolean isGloballyRendered);

    @Override
    public <E extends BlockEntity, S extends BlockEntityRenderState> @Nullable S carminite$tryExtractRenderState(E blockEntity, float partialTicks, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress, @Nullable Frustum frustum) {
        BlockEntityRenderer<E, S> renderer = this.getRenderer(blockEntity);
        return renderer == null ? null : this.carminite$tryExtractRenderState(blockEntity, partialTicks, breakProgress, renderer.shouldRenderOffScreen(), frustum);
    }

    @Override
    public <E extends BlockEntity, S extends BlockEntityRenderState> @Nullable S carminite$tryExtractRenderState(E blockEntity, float partialTicks, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress, boolean isGloballyRendered, @Nullable Frustum frustum) {
        BlockEntityRenderer<E, S> renderer = this.getRenderer(blockEntity);
        if (frustum != null && renderer instanceof IExtendedBoundingBoxBlockEntityRenderer extended && !frustum.isVisible(extended.getRenderBoundingBox(blockEntity))) {
            return null;
        }
        return this.tryExtractRenderState(blockEntity, partialTicks, breakProgress, isGloballyRendered);
    }
}
