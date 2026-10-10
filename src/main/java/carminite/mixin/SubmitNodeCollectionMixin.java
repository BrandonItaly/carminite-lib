package carminite.mixin;

import carminite.client.ExtendedBlockFeatureRenderer;
import carminite.interfaces.extensions.IOrderedSubmitNodeCollectorExtension;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(SubmitNodeCollection.class)
public abstract class SubmitNodeCollectionMixin implements IOrderedSubmitNodeCollectorExtension {
    @Override
    public void carminite$submitMultiLayerBlockModel(PoseStack poseStack, List<BlockStateModelPart> modelParts,
        boolean translucent, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor) {
        ExtendedBlockFeatureRenderer.submit((OrderedSubmitNodeCollector) this, poseStack, modelParts,
            tintLayers, lightCoords, overlayCoords, outlineColor);
    }
}
