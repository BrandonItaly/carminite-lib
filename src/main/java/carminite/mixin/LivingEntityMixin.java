package carminite.mixin;

import carminite.events.hooks.CarminiteHooks;
import carminite.events.hooks.CommonHooks;
import carminite.events.modified.CarminiteEntityTeleportEvent;
import carminite.events.neoforge.LivingFallEvent;
import carminite.interfaces.markers.IContinuousUseItem;
import carminite.interfaces.markers.ISpecialLandingEffectsBlock;
import carminite.interfaces.markers.ISpecialScaffoldingBlock;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@Shadow
	protected ItemStack useItem;

	@Shadow
	public abstract ItemStack getItemInHand(InteractionHand hand);

	@Shadow
	public abstract InteractionHand getUsedItemHand();

	@WrapOperation(
		method = "checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"
		)
	)
	private <T extends ParticleOptions> int carminite$addLandingEffects(
		ServerLevel instance,
		T particle,
		double x,
		double y,
		double z,
		int count,
		double xDist,
		double yDist,
		double zDist,
		double speed,
		Operation<Integer> original,
		@Local(argsOnly = true, name = "onState") BlockState onState,
		@Local(argsOnly = true, name = "pos") BlockPos pos
	) {
		if (onState.getBlock() instanceof ISpecialLandingEffectsBlock specialLandingEffectsBlock) {
			if (!specialLandingEffectsBlock.addLandingEffects(onState, instance, pos, onState, (LivingEntity) (Object) this, count)) {
				return original.call(instance, particle, x, y, z, count, xDist, yDist, zDist, speed);
			} else {
				return 0;
			}
		}
		return original.call(instance, particle, x, y, z, count, xDist, yDist, zDist, speed);
	}

	@ModifyExpressionValue(
		method = "handleOnClimbable(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"
		)
	)
	private boolean carminite$customScaffoldingMovement(boolean original) {
		LivingEntity livingEntity = (LivingEntity) (Object) this;
		BlockState state = livingEntity.getInBlockState();
		if (state.getBlock() instanceof ISpecialScaffoldingBlock specialScaffoldingBlock) {
			return specialScaffoldingBlock.isScaffolding(state, livingEntity.level(), livingEntity.blockPosition(), livingEntity);
		}
		return original;
	}

	@ModifyExpressionValue(
		method = "updatingUsingItem()V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;isSameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
		)
	)
	private boolean carminite$canContinueUsing(boolean original) {
		if (this.useItem.getItem() instanceof IContinuousUseItem continuousUseItem) {
			ItemStack to = this.getItemInHand(this.getUsedItemHand());
			if (!this.useItem.isEmpty() && !to.isEmpty())
			{
				return continuousUseItem.canContinueUsing(this.useItem, to);
			}
			return false;
		}
		return original;
	}

	@Inject(
		method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/damagesource/DamageSource;getEntity()Lnet/minecraft/world/entity/Entity;",
			shift = At.Shift.BEFORE
		),
		cancellable = true
	)
	private void carminite$livingDeath(
		DamageSource source,
		CallbackInfo ci
	) {
		if (CommonHooks.onLivingDeath((LivingEntity) (Object) this, source)) {
			ci.cancel();
		}
	}

	@Inject(
		method = "jumpFromGround()V",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/world/entity/LivingEntity;needsSync:Z",
			shift = At.Shift.AFTER,
			opcode = Opcodes.PUTFIELD
		)
	)
	private void carminite$livingJump(CallbackInfo ci) {
		CommonHooks.onLivingJump((LivingEntity) (Object) this);
	}

	@Inject(
		method = "doHurtEquipment(Lnet/minecraft/world/damagesource/DamageSource;F[Lnet/minecraft/world/entity/EquipmentSlot;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;",
			shift = At.Shift.BEFORE
		),
		cancellable = true
	)
	private void carminite$armorHurt(
		DamageSource damageSource,
		float damage, EquipmentSlot[] slots,
		CallbackInfo ci,
		@Local(name = "durabilityDamage") int durabilityDamage
	) {
		CommonHooks.onArmorHurt(damageSource, slots, durabilityDamage, (LivingEntity) (Object) this);
		ci.cancel();
	}

	@Inject(
		method = "causeFallDamage(DFLnet/minecraft/world/damagesource/DamageSource;)Z",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;causeFallDamage(DFLnet/minecraft/world/damagesource/DamageSource;)Z"
		),
		cancellable = true
	)
	private void carminite$livingFall(
		double fallDistance,
		float damageModifier,
		DamageSource damageSource,
		CallbackInfoReturnable<Boolean> cir,
		@Local(name = "effectiveFallDistance") double effectiveFallDistance,
		@Share(value = "event", namespace = "carminite") LocalRef<LivingFallEvent> event
	) {
		event.set(CommonHooks.onLivingFall((LivingEntity) (Object) this, effectiveFallDistance, damageModifier));
		if (event.get().isCanceled()) {
			cir.setReturnValue(false);
		}
	}

	@ModifyArgs(
		method = "causeFallDamage(DFLnet/minecraft/world/damagesource/DamageSource;)Z",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;causeFallDamage(DFLnet/minecraft/world/damagesource/DamageSource;)Z"
		)
	)
	private void carminite$modifyCauseFallDamage(
		Args args,
		@Share(value = "event", namespace = "carminite") LocalRef<LivingFallEvent> event
	) {
		args.set(0, event.get().getDistance());
		args.set(1, event.get().getDamageMultiplier());
	}

	@ModifyArgs(
		method = "causeFallDamage(DFLnet/minecraft/world/damagesource/DamageSource;)Z",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;calculateFallDamage(DF)I"
		)
	)
	private void carminite$modifyCalculateFallDamage(
		Args args,
		@Share(value = "event", namespace = "carminite") LocalRef<LivingFallEvent> event
	) {
		args.set(0, event.get().getDistance());
		args.set(1, event.get().getDamageMultiplier());
	}

	@Inject(
		method = "checkPositionAndTeleport(DDDZLjava/util/function/Predicate;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;)Z",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;teleportTo(DDD)V"
		),
		cancellable = true
	)
	private void carminite$entityTeleport(
		CallbackInfoReturnable<Boolean> cir
	) {
		CarminiteEntityTeleportEvent event = CarminiteHooks.onEntityTeleport((LivingEntity) (Object) this);
		if (event.isCanceled()) {
			cir.setReturnValue(false);
		}
	}
}
