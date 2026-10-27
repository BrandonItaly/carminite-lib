package carminite.entity;

import carminite.Carminite;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
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



	@Override
	public Type<AdvancedAddEntityPayload> type() {
		return TYPE;
	}
}
