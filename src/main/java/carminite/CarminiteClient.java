package carminite;

import carminite.entity.AdvancedAddEntityPayload;
import carminite.entity.IEntityWithComplexSpawn;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import carminite.entity.IMultiPartEntity;
import carminite.entity.PartEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class CarminiteClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerClientMultipartEvents();

        ClientPlayNetworking.registerGlobalReceiver(AdvancedAddEntityPayload.TYPE, (advancedAddEntityPayload, context) -> {
		try {
			Entity entity = context.player().level().getEntity(advancedAddEntityPayload.entityId());
			if (entity instanceof IEntityWithComplexSpawn entityAdditionalSpawnData) {
				final RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(advancedAddEntityPayload.customPayload()), entity.registryAccess());
				try {
					entityAdditionalSpawnData.readSpawnData(buf);
				} finally {
					buf.release();
				}
			}
		} catch (Throwable t) {
			Carminite.LOGGER.error("Carminite: Failed to handle advanced add entity from server.", t);
			context.responseSender().disconnect(Component.literal("Carminite: Failed to handle advanced add entity from server."));
		}
	});
    }

    private static void registerClientMultipartEvents() {
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof IMultiPartEntity partEntity && partEntity.isMultipartEntity()) {
                for (PartEntity<?> part : partEntity.getParts()) {
                    world.carminite$getPartEntityMap().put(part.getId(), part);
                }
            }
        });
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof IMultiPartEntity partEntity && partEntity.isMultipartEntity()) {
                for (PartEntity<?> part : partEntity.getParts()) {
                    world.carminite$getPartEntityMap().remove(part.getId());
                }
            }
        });
    }
}