package carminite.interfaces.extensions;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.jspecify.annotations.Nullable;

public interface ILevelRendererExtension {
	default void carminite$extractVisibleBlockEntities(Camera camera, float deltaPartialTick, LevelRenderState levelRenderState, @Nullable Frustum cullFrustum) {
		throw new AssertionError("Implemented via mixin");
	}
}
