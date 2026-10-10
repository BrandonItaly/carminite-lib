package carminite.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ExtendedBlockFeatureRenderer {
    // Submit each material separately so vanilla handles transparency and outlines
    public static void submit(OrderedSubmitNodeCollector collector, PoseStack poseStack,
                              List<BlockStateModelPart> parts, int[] tints,
                              int light, int overlay, int outline) {
        for (BlockStateModelPart part : parts) {
            Map<RenderType, Map<@Nullable Direction, List<BakedQuad>>> layers = new LinkedHashMap<>();
            for (Direction direction : Direction.values()) {
                collect(layers, direction, part.getQuads(direction));
            }
            collect(layers, null, part.getQuads(null));
            layers.forEach((type, quads) -> collector.submitBlockModel(poseStack, type,
                List.of(new LayerPart(part, quads)), tints, light, overlay, outline));
        }
    }

    private static void collect(Map<RenderType, Map<@Nullable Direction, List<BakedQuad>>> layers,
                                @Nullable Direction direction, List<BakedQuad> quads) {
        for (BakedQuad quad : quads) {
            layers.computeIfAbsent(quad.materialInfo().itemRenderType(), _ -> new HashMap<>())
                .computeIfAbsent(direction, _ -> new ArrayList<>()).add(quad);
        }
    }

    private record LayerPart(BlockStateModelPart original,
                             Map<@Nullable Direction, List<BakedQuad>> quads) implements BlockStateModelPart {
        @Override
        public List<BakedQuad> getQuads(@Nullable Direction direction) {
            return quads.getOrDefault(direction, List.of());
        }

        @Override
        public boolean useAmbientOcclusion() {
            return original.useAmbientOcclusion();
        }

        @Override
        public Material.Baked particleMaterial() {
            return original.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return original.materialFlags();
        }
    }

    private ExtendedBlockFeatureRenderer() {}
}
