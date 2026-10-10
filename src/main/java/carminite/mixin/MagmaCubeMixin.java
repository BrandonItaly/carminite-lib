package carminite.mixin;

import carminite.events.hooks.CommonHooks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MagmaCube.class)
public class MagmaCubeMixin {

	@Inject(
		method = "jumpFromGround()V",
		at = @At("TAIL")
	)
	private void carminite$livingJump(CallbackInfo ci) {
		CommonHooks.onLivingJump((LivingEntity) (Object) this);
	}
}