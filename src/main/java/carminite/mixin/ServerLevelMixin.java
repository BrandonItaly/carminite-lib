package carminite.mixin;

import carminite.events.hooks.EventHooks;
import carminite.events.neoforge.EntityJoinLevelEvent;
import carminite.interfaces.extensions.ILevelExtension;
import carminite.entity.PartEntity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements ILevelExtension {

	@ModifyReturnValue(
		method = "getEntityOrPart(I)Lnet/minecraft/world/entity/Entity;",
		at = @At("RETURN")
	)
	public Entity carminite$getMultipart(Entity entity, int id) {
		if (entity == null) {
			Int2ObjectMap<PartEntity<?>> partEntityMap = carminite$getPartEntityMap();
			if (partEntityMap != null) {
				return partEntityMap.get(id);
			}
		}
		return entity;
	}

	@WrapOperation(
		method = "tickNonPassenger(Lnet/minecraft/world/entity/Entity;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;tick()V"
		)
	)
	private void carminite$entityTick(
		Entity instance,
		Operation<Void> original
	) {
		if (!EventHooks.fireEntityTickPre(instance).isCanceled()) {
			original.call(instance);
			EventHooks.fireEntityTickPost(instance);
		}
	}

	@Inject(
		method = "addPlayer(Lnet/minecraft/server/level/ServerPlayer;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void carminite$entityJoinLevel(
		ServerPlayer player,
		CallbackInfo ci
	) {
		if (new EntityJoinLevelEvent(player, (Level) (Object) this).post().isCanceled()) {
			ci.cancel();
		}
	}
}