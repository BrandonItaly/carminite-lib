package carminite.mixin;

import carminite.events.hooks.ClientHooks;
import carminite.interfaces.markers.IContinuousUseItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FirstPersonHandsAndItems.class)
public class ItemInHandRendererMixin {

	@WrapOperation(
		method = "tick(Lnet/minecraft/client/player/LocalPlayer;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/FirstPersonHandsAndItems;shouldInstantlyReplaceVisibleItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/player/LocalPlayer;)Z",
			ordinal = 0
		)
	)
	private boolean carminite$shouldCauseReequipAnimationFirst(
		FirstPersonHandsAndItems instance,
		ItemStack currentlyVisibleItem,
		ItemStack expectedItem,
		LocalPlayer player,
		Operation<Boolean> original
	) {
		if (!(currentlyVisibleItem.getItem() instanceof IContinuousUseItem)) {
			return original.call(instance, currentlyVisibleItem, expectedItem, player);
		}
		return !ClientHooks.shouldCauseReequipAnimation(
			currentlyVisibleItem,
			expectedItem,
			player.getInventory().getSelectedSlot()
		);
	}

	@WrapOperation(
		method = "tick(Lnet/minecraft/client/player/LocalPlayer;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/FirstPersonHandsAndItems;shouldInstantlyReplaceVisibleItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/player/LocalPlayer;)Z",
			ordinal = 1
		)
	)
	private boolean carminite$shouldCauseReequipAnimationSecond(
		FirstPersonHandsAndItems instance,
		ItemStack currentlyVisibleItem,
		ItemStack expectedItem,
		LocalPlayer player,
		Operation<Boolean> original
	) {
		if (!(currentlyVisibleItem.getItem() instanceof IContinuousUseItem)) {
			return original.call(instance, currentlyVisibleItem, expectedItem, player);
		}
		return !ClientHooks.shouldCauseReequipAnimation(
			currentlyVisibleItem,
			expectedItem,
			-1
		);
	}

	@Redirect(
		method = "extractMapRenderState(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/state/MapRenderState;)Z",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/MapItem;getSavedData(Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;"
		)
	)
	private MapItemSavedData carminite$renderMap(
		MapId id,
		Level level,
		@Local(argsOnly = true, name = "itemStack") ItemStack itemStack
	) {
		return MapItem.getSavedData(itemStack, level);
	}
}
