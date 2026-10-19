package carminite;

import carminite.entity.AdvancedAddEntityPayload;
import carminite.entity.IMultiPartEntity;
import carminite.entity.PartEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class CarminiteClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerClientMultipartEvents();

        ClientPlayNetworking.registerGlobalReceiver(AdvancedAddEntityPayload.TYPE, AdvancedAddEntityPayload::handle);
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