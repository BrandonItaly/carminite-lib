package carminite.mixin;

import net.minecraft.advancements.predicates.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.predicates.AttributeModifiersPredicate;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AttributeModifiersPredicate.class)
public abstract class AttributeModifiersPredicateMixin implements SingleComponentItemPredicate<ItemAttributeModifiers> {

    @Override
    public boolean matches(DataComponentGetter getter) {
        if (getter instanceof net.minecraft.world.item.ItemStack stack) {
            return this.matches(stack.carminite$getAttributeModifiers());
        }
        return SingleComponentItemPredicate.super.matches(getter);
    }
}