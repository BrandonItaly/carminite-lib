package carminite.mixin;

import carminite.interfaces.extensions.IBlockEntityRenderDispatcherExtension;
import carminite.interfaces.extensions.ILevelRendererExtension;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin implements ILevelRendererExtension {
    @Unique
    private @Nullable Frustum carminite$frustum;

    @Shadow
    protected abstract void extractVisibleBlockEntities(Camera camera, float deltaPartialTick, LevelRenderState state);

    @WrapOperation(
        method = "extract(Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/Camera;F)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;extractVisibleBlockEntities(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/state/level/LevelRenderState;)V")
    )
    private void carminite$extractFrustum(LevelExtractor instance, Camera camera, float deltaPartialTick, LevelRenderState state, Operation<Void> original, @Local(name = "cullFrustum") Frustum frustum) {
        this.carminite$extractVisibleBlockEntities(camera, deltaPartialTick, state, frustum);
    }

    @Override
    public void carminite$extractVisibleBlockEntities(Camera camera, float deltaPartialTick, LevelRenderState state, @Nullable Frustum frustum) {
        Frustum previous = this.carminite$frustum;
        this.carminite$frustum = frustum;
        try {
            this.extractVisibleBlockEntities(camera, deltaPartialTick, state);
        } finally {
            this.carminite$frustum = previous;
        }
    }

    @WrapOperation(
        method = "extractVisibleBlockEntities(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/state/level/LevelRenderState;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;tryExtractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;Z)Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;")
    )
    private @Nullable BlockEntityRenderState carminite$extractBlockEntity(BlockEntityRenderDispatcher dispatcher, BlockEntity blockEntity, float partialTicks, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress, boolean isGloballyRendered, Operation<BlockEntityRenderState> original) {
        return ((IBlockEntityRenderDispatcherExtension) dispatcher).carminite$tryExtractRenderState(blockEntity, partialTicks, breakProgress, isGloballyRendered, this.carminite$frustum);
    }
}
