package carminite.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import carminite.interfaces.extensions.IWeightedListExtension;
import com.google.common.collect.ImmutableList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;

/**
 * Borrowed from Kilt, thanks BluSpring!
 */
@Mixin(WeightedList.class)
public abstract class WeightedListMixin {

    @Mixin(WeightedList.Builder.class)
    public abstract static class BuilderMixin<E> implements IWeightedListExtension.BuilderExtension<E> {

        @Shadow
        @Final
        private ImmutableList.Builder<Weighted<E>> result;

        @Unique private final List<Weighted<E>> carminite$removed = new ArrayList<>();

        @Override
        public WeightedList.Builder<E> carminite$add(Weighted<E> value) {
           this.result.add(value);
           return (WeightedList.Builder<E>) (Object) this;
        }

        @Override
        public WeightedList.Builder<E> carminite$addAll(WeightedList<E> values) {
            this.carminite$addAll(values.unwrap());
            return (WeightedList.Builder<E>) (Object) this;
        }

        @Override
        public WeightedList.Builder<E> carminite$addAll(Collection<Weighted<E>> values) {
            this.result.addAll(values);
            return (WeightedList.Builder<E>) (Object) this;
        }

        @Override
        public WeightedList.Builder<E> carminite$remove(E value) {
            this.carminite$removeIf(weighted -> weighted.value().equals(value));
            return (WeightedList.Builder<E>) (Object) this;
        }

        @Override
        public WeightedList.Builder<E> carminite$remove(Weighted<E> value) {
            this.carminite$removed.add(value);
            return (WeightedList.Builder<E>) (Object) this;
        }

        @Override
        public WeightedList.Builder<E> carminite$removeIf(Predicate<Weighted<E>> filter) {
            for (Weighted<E> weighted : this.result.build()) {
                if (filter.test(weighted)) {
                    this.carminite$removed.add(weighted);
                }
            }
            return (WeightedList.Builder<E>) (Object) this;
        }

        @Override
        public List<Weighted<E>> carminite$getList() {
            return this.carminite$withoutRemoved(this.result.build());
        }

        @Unique
        private List<Weighted<E>> carminite$withoutRemoved(List<? extends Weighted<E>> original) {
            var list = new ArrayList<>(original);
            for (Weighted<E> weighted : this.carminite$removed) {
                list.remove(weighted);
            }

            return Collections.unmodifiableList(list);
        }

        @ModifyArg(
            method = "build()Lnet/minecraft/util/random/WeightedList;",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/util/random/WeightedList;<init>(Ljava/util/List;)V"
            )
        )
        private List<? extends Weighted<E>> carminite$buildWithoutRemoved(List<? extends Weighted<E>> items) {
            return this.carminite$withoutRemoved(items);
        }
    }
}