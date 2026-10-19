package carminite.entity;

import carminite.Carminite;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record AdvancedAddEntityPayload(int entityId, byte[] customPayload) implements CustomPacketPayload {
	public static final Type<AdvancedAddEntityPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Carminite.MOD_ID, "advanced_add_entity"));

	public static final StreamCodec<FriendlyByteBuf, AdvancedAddEntityPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		AdvancedAddEntityPayload::entityId,
		new StreamCodec<>() {
			public byte[] decode(FriendlyByteBuf buf) {
				return buf.readByteArray();
			}
			public void encode(FriendlyByteBuf buf, byte[] data) {
				buf.writeByteArray(data);
			}
		},
		AdvancedAddEntityPayload::customPayload,
		AdvancedAddEntityPayload::new);

	public AdvancedAddEntityPayload(Entity e) {
		this(e.getId(), writeCustomData(e));
	}

	private static byte[] writeCustomData(final Entity entity) {
		if (!(entity instanceof IEntityWithComplexSpawn additionalSpawnData)) {
			return new byte[0];
		}

		final RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), entity.registryAccess());

		try {
			additionalSpawnData.writeSpawnData(buf);
			buf.readerIndex(0);
			final byte[] data = new byte[buf.readableBytes()];
			buf.readBytes(data);
			return data;
		} finally {
			buf.release();
		}
	}

	public static void handle(AdvancedAddEntityPayload advancedAddEntityPayload, ClientPlayNetworking.Context context) {
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
	}

	@Override
	public Type<AdvancedAddEntityPayload> type() {
		return TYPE;
	}
}
