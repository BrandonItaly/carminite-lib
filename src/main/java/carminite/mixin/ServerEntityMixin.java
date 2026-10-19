package carminite.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    @Final
    private Entity entity;

    @ModifyExpressionValue(
        method = "sendChanges()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/MapItem;getSavedData(Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;"
        )
    )
    private MapItemSavedData carminite$sendChanges(
        MapItemSavedData original,
        @Local(name = "itemStack") ItemStack itemStack
    ) {
        return MapItem.getSavedData(itemStack, this.level);
    }

    @Inject(
        method = "sendPairingData(Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
            shift = At.Shift.AFTER
        )
    )
    private void carminite$sendPairingData(
        ServerPlayer player,
        Consumer<Packet<? super ClientGamePacketListener>> broadcast,
        CallbackInfo ci
    ) {
        this.entity.carminite$sendPairingData(player, payload -> broadcast.accept(new ClientboundCustomPayloadPacket(payload)));
    }
}