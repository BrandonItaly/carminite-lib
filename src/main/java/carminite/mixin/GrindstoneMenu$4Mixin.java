package carminite.mixin;

import carminite.events.hooks.CommonHooks;
import carminite.interfaces.extensions.IGrindstoneMenuExtension;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$4")
public abstract class GrindstoneMenu$4Mixin implements IGrindstoneMenuExtension{

    @Final
    @Shadow
    GrindstoneMenu this$0;

    @Shadow
    @Final
    ContainerLevelAccess val$access;

    @Shadow
    protected abstract int getExperienceAmount(Level level);

    @Inject(
        method = "onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void carminite$onTake(
        Player player,
        ItemStack carried,
        CallbackInfo ci
    ) {
        if (CommonHooks.onGrindstoneTake(this$0.repairSlots, this.val$access, player, this::getExperienceAmount)) {
            ci.cancel();
        }
    }

    @Inject(
        method = "getExperienceAmount(Lnet/minecraft/world/level/Level;)I",
        at = @At("HEAD"),
        cancellable = true
    )
    private void carminite$getExperienceAmount(
        Level level,
        CallbackInfoReturnable<Integer> cir
    ) {
        if (this.carminite$getXp() >= -1) {
            cir.setReturnValue(this.carminite$getXp());
        }
    }
}