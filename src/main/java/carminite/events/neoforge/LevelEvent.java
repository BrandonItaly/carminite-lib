package carminite.events.neoforge;

import java.util.List;

import carminite.events.CarminiteEvent;
import carminite.events.ICancellableEvent;
import carminite.events.api.LevelEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.jspecify.annotations.Nullable;

public abstract class LevelEvent extends CarminiteEvent {
    private final LevelAccessor level;

    public LevelEvent(LevelAccessor level) {
        this.level = level;
    }

    public LevelAccessor getLevel() {
        return level;
    }

    public static class PotentialSpawns extends LevelEvent implements ICancellableEvent {
        private final MobCategory mobcategory;
        private final BlockPos pos;
        private WeightedList.@Nullable Builder<MobSpawnSettings.SpawnerData> list;
        private List<Weighted<MobSpawnSettings.SpawnerData>> view;

        public PotentialSpawns(LevelAccessor level, MobCategory category, BlockPos pos, WeightedList<MobSpawnSettings.SpawnerData> oldList) {
            super(level);
            this.pos = pos;
            this.mobcategory = category;
            this.list = null;
            this.view = oldList.unwrap();
        }

        public MobCategory getMobCategory() {
            return mobcategory;
        }

        public BlockPos getPos() {
            return pos;
        }

        public List<Weighted<MobSpawnSettings.SpawnerData>> getSpawnerDataList() {
            return view;
        }

        @SuppressWarnings("all")
        private void makeList() {
            if (list == null) {
                list = WeightedList.builder();
                list.carminite$addAll(view);
                view = list.carminite$getList();
            }
        }

        @SuppressWarnings("all")
        public void addSpawnerData(Weighted<MobSpawnSettings.SpawnerData> data) {
            makeList();
            list.carminite$add(data);
            view = list.carminite$getList();
        }

        @SuppressWarnings("all")
        public void removeSpawnerData(Weighted<MobSpawnSettings.SpawnerData> data) {
            makeList();
            list.carminite$remove(data);
            view = list.carminite$getList();
        }

        @Override
        public PotentialSpawns post() {
            LevelEvents.POTENTIAL_SPAWNS.invoker().getPotentialSpawns(this);
            return this;
        }
    }
}