package carminite.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemInstance {

    @Shadow
    public abstract Item getItem();

    @Override
    public int getMaxStackSize() {
        return this.getItem().carminite$getMaxStackSize((ItemStack) (Object) this);
    }


    @WrapMethod(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V")
    private void carminite$forEachModifierTri(
        EquipmentSlotGroup slot,
        TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> consumer,
        Operation<Void> original
    ) {
        ItemStack self = (ItemStack) (Object) this;
        self.carminite$getAttributeModifiers().forEach(slot, consumer);
        EnchantmentHelper.forEachModifier(self, slot, (a, b) -> consumer.accept(a, b, ItemAttributeModifiers.Display.attributeModifiers()));
    }

    @WrapMethod(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V")
    private void carminite$forEachModifierBi(
        EquipmentSlot slot,
        BiConsumer<Holder<Attribute>, AttributeModifier> consumer,
        Operation<Void> original
    ) {
        ItemStack self = (ItemStack) (Object) this;
        self.carminite$getAttributeModifiers().forEach(slot, consumer);
        EnchantmentHelper.forEachModifier(self, slot, consumer);
    }
}