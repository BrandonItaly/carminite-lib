package carminite.mixin;

import carminite.events.hooks.ClientHooks;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {

    @ModifyReturnValue(
        method = "getFieldOfViewModifier(ZF)F",
        at = @At(
            value = "RETURN",
            ordinal = 1
        )
    )
    private float carminite$computeFovModifier(
        float original,
        @Local(name = "effectScale", argsOnly = true) float effectScale,
        @Local(name = "modifier") float modifier
    ) {
        return ClientHooks.getFieldOfViewModifier((AbstractClientPlayer) (Object) this, modifier, effectScale);
    }
}