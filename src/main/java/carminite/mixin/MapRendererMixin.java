package carminite.mixin;

import carminite.client.map.MapRendererManager;
import carminite.util.mixin.ElementSkippingList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MapRenderer.class)
public abstract class MapRendererMixin {

    @Shadow
    @Final
    private TextureAtlas decorationSprites;

    @ModifyExpressionValue(
        method = "render(Lnet/minecraft/client/renderer/state/MapRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ZI)V",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/renderer/state/MapRenderState;decorations:Ljava/util/List;",
            opcode = Opcodes.GETFIELD
        )
    )
    private List<MapRenderState.MapDecorationRenderState> carminite$wrapDecorationList(
        List<MapRenderState.MapDecorationRenderState> original,
        @Local(argsOnly = true, name = "mapRenderState") MapRenderState mapRenderState,
        @Local(argsOnly = true, name = "poseStack") PoseStack poseStack,
        @Local(argsOnly = true, name = "submitNodeCollector") SubmitNodeCollector collector,
        @Local(argsOnly = true, name = "showOnlyFrame") boolean showOnlyFrame,
        @Local(argsOnly = true, name = "lightCoords") int lightCoords,
        @Share(namespace = "carminite", value = "drawnCount") LocalIntRef drawnCount
    ) {
        return new ElementSkippingList<>(original, decoration -> {
            if ((!showOnlyFrame || decoration.renderOnFrame) && MapRendererManager.render(decoration, poseStack, collector, mapRenderState, this.decorationSprites, showOnlyFrame, lightCoords, drawnCount.get())) {
                drawnCount.set(drawnCount.get() + 1);
                return true;
            }
            return false;
        });
    }

    @Inject(
        method = "render(Lnet/minecraft/client/renderer/state/MapRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ZI)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
            ordinal = 0
        )
    )
    private void carminite$wrapCount(
        CallbackInfo ci,
        @Local(name = "count") LocalIntRef count,
        @Share(namespace = "carminite", value = "drawnCount") LocalIntRef drawnCount
    ) {
        count.set(drawnCount.get());
        drawnCount.set(drawnCount.get() + 1);
    }

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;Lnet/minecraft/client/renderer/state/MapRenderState;)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;clear()V",
            shift = At.Shift.AFTER
        )
    )
    private void carminite$updateMapRenderState(
        MapId mapId,
        MapItemSavedData mapData,
        MapRenderState mapRenderState,
        CallbackInfo ci
    ) {
        MapRendererManager.onUpdateMapRenderState(mapData, mapRenderState);
    }

    @ModifyExpressionValue(
        method = "extractRenderState(Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;Lnet/minecraft/client/renderer/state/MapRenderState;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/MapRenderer;extractDecorationRenderState(Lnet/minecraft/world/level/saveddata/maps/MapDecoration;)Lnet/minecraft/client/renderer/state/MapRenderState$MapDecorationRenderState;"
        )
    )
    private MapRenderState.MapDecorationRenderState carminite$updateMapDecorationRenderState(
        MapRenderState.MapDecorationRenderState original,
        @Local(argsOnly = true, name = "mapData") MapItemSavedData mapData,
        @Local(argsOnly = true, name = "mapRenderState") MapRenderState mapRenderState,
        @Local(name = "decoration") MapDecoration decoration
    ) {
        return MapRendererManager.onUpdateMapDecorationRenderState(decoration.type(), mapData, mapRenderState, original);
    }

    @Inject(
        method = "extractDecorationRenderState(Lnet/minecraft/world/level/saveddata/maps/MapDecoration;)Lnet/minecraft/client/renderer/state/MapRenderState$MapDecorationRenderState;",
        at = @At("RETURN")
    )
    private void carminite$addStateData(
        MapDecoration decoration,
        CallbackInfoReturnable<MapRenderState.MapDecorationRenderState> cir
    ) {
        cir.getReturnValue().setData(MapRendererManager.DECORATION_TYPE_KEY, decoration.type());
    }
}
