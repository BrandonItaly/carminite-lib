package carminite.events.hooks;

import carminite.events.neoforge.*;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.ApiStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class EventHooks {
	public static boolean onEntityStruckByLightning(Entity entity, LightningBolt bolt) {
		return new EntityStruckByLightningEvent(entity, bolt).post().isCanceled();
	}

	public static boolean doPlayerHarvestCheck(Player player, BlockState state, BlockGetter level, BlockPos pos) {
		boolean vanillaValue = player.hasCorrectToolForDrops(state);
		return new PlayerEvent.HarvestCheck(player, state, level, pos, vanillaValue).post().canHarvest();
	}

	public static int onArrowLoose(ItemStack stack, Level level, Player player, int charge, boolean hasAmmo) {
		var event = new ArrowLooseEvent(player, stack, level, charge, hasAmmo).post();
		if (event.isCanceled())
			return -1;
		return event.getCharge();
	}

	public static void firePlayerCraftingEvent(Player player, ItemStack crafted, Container craftMatrix) {
		new PlayerEvent.ItemCraftedEvent(player, crafted, craftMatrix).post();
	}

	public static EntityTickEvent.Pre fireEntityTickPre(Entity entity) {
		return new EntityTickEvent.Pre(entity).post();
	}

	public static void fireEntityTickPost(Entity entity) {
		new EntityTickEvent.Post(entity).post();
	}

	public static void firePlayerTickPre(Player player) {
		new PlayerTickEvent.Pre(player).post();
	}

	public static void firePlayerTickPost(Player player) {
		new PlayerTickEvent.Post(player).post();
	}

	@ApiStatus.Internal
	public static void onAdvancementEarnedEvent(Player player, AdvancementHolder earned) {
		new AdvancementEvent.AdvancementEarnEvent(player, earned).post();
	}

	public static boolean onProjectileImpact(Projectile projectile, HitResult ray) {
		return new ProjectileImpactEvent(projectile, ray).post().isCanceled();
	}

	public static void onExplosionDetonate(Level level, ServerExplosion explosion, List<Entity> entities, List<BlockPos> blocks) {
		new ExplosionEvent.Detonate(level, explosion, entities, blocks).post();
	}

	public static boolean canMountEntity(Entity entityMounting, Entity entityBeingMounted, boolean isMounting) {
		boolean isCanceled = new EntityMountEvent(entityMounting, entityBeingMounted, entityMounting.level(), isMounting).post().isCanceled();

		if (isCanceled) {
			entityMounting.absSnapTo(entityMounting.getX(), entityMounting.getY(), entityMounting.getZ(), entityMounting.yRotO, entityMounting.xRotO);
			return false;
		} else
			return true;
	}

	private static final WeightedList<MobSpawnSettings.SpawnerData> NO_SPAWNS = WeightedList.of();
	public static WeightedList<MobSpawnSettings.SpawnerData> getPotentialSpawns(LevelAccessor level, MobCategory category, BlockPos pos, WeightedList<MobSpawnSettings.SpawnerData> oldList) {
		LevelEvent.PotentialSpawns event = new LevelEvent.PotentialSpawns(level, category, pos, oldList);
		if (event.post().isCanceled())
			return NO_SPAWNS;
		else if (event.getSpawnerDataList() == oldList.unwrap())
			return oldList;
		return WeightedList.of(event.getSpawnerDataList());
	}
}