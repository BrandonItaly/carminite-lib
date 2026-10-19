package carminite.interfaces.extensions;

import carminite.entity.AdvancedAddEntityPayload;
import carminite.entity.IEntityWithComplexSpawn;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

public interface IEntityExtension {
	default boolean carminite$canRiderInteract() {
		return false;
	}

	default void carminite$sendPairingData(ServerPlayer serverPlayer, Consumer<CustomPacketPayload> bundleBuilder) {
		if (this instanceof IEntityWithComplexSpawn) {
			bundleBuilder.accept(new AdvancedAddEntityPayload((Entity) this));
		}
	}
}