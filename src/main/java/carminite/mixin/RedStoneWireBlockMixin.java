package carminite.mixin;

import carminite.interfaces.markers.IRedstoneConnectableBlock;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RedstoneWireBlock.class)
public class RedStoneWireBlockMixin {

	@WrapOperation(
		method = "getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/RedstoneWireBlock;shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"
		)
	)
	private boolean carminite$canConnectRedstone(
		BlockState blockState,
		BlockGetter level,
		BlockPos pos,
		Direction direction,
		Operation<Boolean> original
	) {
		if (blockState.getBlock() instanceof IRedstoneConnectableBlock connectable) {
			return connectable.canConnectRedstone(
				blockState,
				level,
				pos,
				direction
			);
		}

		return original.call(
			blockState,
			level,
			pos,
			direction
		);
	}
}
