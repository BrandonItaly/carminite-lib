package carminite.mixin;

import carminite.events.modified.CarminiteRenderLevelStageEvent;
import carminite.interfaces.extensions.ILevelRendererExtension;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin implements ILevelRendererExtension {

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Shadow
    protected abstract boolean shouldShowEntityOutlines();

    @Shadow
    @Final
    private ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections;

    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    @Shadow
    @Final
    private BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    @Shadow
    private @Nullable ClientLevel level;

    @Redirect(
        method = "extractLevel(Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/Camera;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;extractVisibleBlockEntities(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/state/level/LevelRenderState;)V"
        )
    )
    private void carminite$extractFrustum(
        LevelRenderer instance,
        Camera camera,
        float deltaPartialTick,
        LevelRenderState levelRenderState,
        @Local(name = "cullFrustum") Frustum cullFrustum
    ) {
        carminite$extractVisibleBlockEntities(camera, deltaPartialTick, levelRenderState, cullFrustum);
    }

    @Inject(
        method = "lambda$addWeatherPass$0",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/WeatherEffectRenderer;render(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/state/level/WeatherRenderState;)V",
            shift = At.Shift.AFTER
        )
    )
    private void carminite$afterWeather(
        GpuBufferSlice fog,
        int renderDistance,
        CallbackInfo ci
    ) {
        Profiler.get().push("carminite_render_after_weather");
        new CarminiteRenderLevelStageEvent.AfterWeather((LevelRenderer) (Object) this, this.levelRenderState, null, this.levelRenderState.cameraRenderState.viewRotationMatrix).post();
        Profiler.get().pop();
    }

    @Override
    public void carminite$extractVisibleBlockEntities(Camera camera, float deltaPartialTick, LevelRenderState levelRenderState, @Nullable Frustum frustum) {
        Vec3 cameraPos = camera.position();
        double camX = cameraPos.x();
        double camY = cameraPos.y();
        double camZ = cameraPos.z();
        PoseStack poseStack = new PoseStack();
        boolean shouldShowEntityOutlines = this.shouldShowEntityOutlines();

        for (SectionRenderDispatcher.RenderSection section : this.visibleSections) {
            List<BlockEntity> renderableBlockEntities = section.getSectionMesh().getRenderableBlockEntities();
            if (!renderableBlockEntities.isEmpty() && !(section.getVisibility(Util.getMillis()) < 0.3F)) {
                for (BlockEntity blockEntity : renderableBlockEntities) {
                    BlockPos blockPos = blockEntity.getBlockPos();
                    SortedSet<BlockDestructionProgress> progresses = this.destructionProgress.get(blockPos.asLong());
                    ModelFeatureRenderer.CrumblingOverlay breakProgress;
                    if (progresses != null && !progresses.isEmpty()) {
                        poseStack.pushPose();
                        poseStack.translate(blockPos.getX() - camX, blockPos.getY() - camY, blockPos.getZ() - camZ);
                        breakProgress = new ModelFeatureRenderer.CrumblingOverlay(progresses.last().getProgress(), poseStack.last());
                        poseStack.popPose();
                    } else {
                        breakProgress = null;
                    }

                    BlockEntityRenderState state = this.blockEntityRenderDispatcher.carminite$tryExtractRenderState(blockEntity, deltaPartialTick, breakProgress, frustum);
                    if (state != null) {
                        levelRenderState.blockEntityRenderStates.add(state);
                        if (shouldShowEntityOutlines) {
                            levelRenderState.haveGlowingEntities = true;
                        }
                    }
                }
            }
        }

        Iterator<BlockEntity> iterator = this.level.getGloballyRenderedBlockEntities().iterator();

        while (iterator.hasNext()) {
            BlockEntity blockEntity = iterator.next();
            if (blockEntity.isRemoved()) {
                iterator.remove();
            } else {
                BlockEntityRenderState state = this.blockEntityRenderDispatcher.carminite$tryExtractRenderState(blockEntity, deltaPartialTick, null, frustum);
                if (state != null) {
                    levelRenderState.blockEntityRenderStates.add(state);
                    if (shouldShowEntityOutlines) {
                        levelRenderState.haveGlowingEntities = true;
                    }
                }
            }
        }
    }
}
