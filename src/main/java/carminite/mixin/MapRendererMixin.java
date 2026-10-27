package carminite.mixin;

import carminite.client.map.MapRendererManager;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
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

@Mixin(MapRenderer.class)
public abstract class MapRendererMixin {

    @Shadow
    @Final
    private TextureAtlas decorationSprites;

    @Definition(id = "showOnlyFrame", local = @Local(type = boolean.class, argsOnly = true))
    @Expression("showOnlyFrame")
    @ModifyExpressionValue(
        method = "render(Lnet/minecraft/client/renderer/state/MapRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ZI)V",
        at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private boolean carminite$handleShowOnlyFrame(boolean original) {
        return true;
    }

    @ModifyExpressionValue(
        method = "render(Lnet/minecraft/client/renderer/state/MapRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ZI)V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/state/MapRenderState$MapDecorationRenderState;renderOnFrame:Z",
            opcode = Opcodes.GETFIELD
        )
    )
    private boolean carminite$renderMapDecorationType(
        boolean original,
        @Local(argsOnly = true, name = "mapRenderState") MapRenderState mapRenderState,
        @Local(argsOnly = true, name = "poseStack") PoseStack poseStack,
        @Local(argsOnly = true, name = "submitNodeCollector") SubmitNodeCollector submitNodeCollector,
        @Local(argsOnly = true, name = "showOnlyFrame") boolean showOnlyFrame,
        @Local(argsOnly = true, name = "lightCoords") int lightCoords,
        @Local(name = "count") LocalIntRef count,
        @Local(name = "decoration") MapRenderState.MapDecorationRenderState decoration
    ) {
        boolean shouldRender = !showOnlyFrame || original;
        if (shouldRender && MapRendererManager.render(decoration, poseStack, submitNodeCollector, mapRenderState, this.decorationSprites, showOnlyFrame, lightCoords, count.get())) {
            count.set(count.get() + 1);
            return false;
        }
        return shouldRender;
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
