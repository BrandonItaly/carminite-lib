package carminite.events.api;

import carminite.events.neoforge.*;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class PlayerEvents {
    public static final Event<HarvestCheck> HARVEST_CHECK = EventFactory.createArrayBacked(HarvestCheck.class, callbacks -> event -> {
        for (HarvestCheck callback : callbacks) {
            callback.doPlayerHarvestCheck(event);
        }
    });

    public static final Event<ItemCrafted> ITEM_CRAFTED = EventFactory.createArrayBacked(ItemCrafted.class, callbacks -> event -> {
        for (ItemCrafted callback : callbacks) {
            callback.firePlayerCraftingEvent(event);
        }
    });

    public static final Event<RightClickBlock> RIGHT_CLICK_BLOCK = EventFactory.createArrayBacked(RightClickBlock.class, callbacks -> event -> {
        for (RightClickBlock callback : callbacks) {
            callback.onRightClickBlock(event);
        }
    });

    public static final Event<LeftClickEmpty> LEFT_CLICK_EMPTY = EventFactory.createArrayBacked(LeftClickEmpty.class, callbacks -> event -> {
        for (LeftClickEmpty callback : callbacks) {
            callback.onEmptyLeftClick(event);
        }
    });

    public static final Event<AttackEntity> ATTACK_ENTITY = EventFactory.createArrayBacked(AttackEntity.class, callbacks -> event -> {
        for (AttackEntity callback : callbacks) {
            callback.onPlayerAttackTarget(event);
        }
    });

    public static final Event<AdvancementEarned> ADVANCEMENT_EARNED = EventFactory.createArrayBacked(AdvancementEarned.class, callbacks -> event -> {
        for (AdvancementEarned callback : callbacks) {
            callback.onAdvancementEarnedEvent(event);
        }
    });

    public static final Event<ArrowLoose> ARROW_LOOSE = EventFactory.createArrayBacked(ArrowLoose.class, callbacks -> event -> {
        for (ArrowLoose callback : callbacks) {
            callback.onArrowLoose(event);
        }
    });

    @FunctionalInterface
    public interface HarvestCheck {
        void doPlayerHarvestCheck(PlayerEvent.HarvestCheck event);
    }

    @FunctionalInterface
    public interface ItemCrafted {
        void firePlayerCraftingEvent(PlayerEvent.ItemCraftedEvent event);
    }


    @FunctionalInterface
    public interface RightClickBlock {
        void onRightClickBlock(PlayerInteractEvent.RightClickBlock event);
    }

    @FunctionalInterface
    public interface LeftClickEmpty {
        void onEmptyLeftClick(PlayerInteractEvent.LeftClickEmpty event);
    }

    @FunctionalInterface
    public interface AttackEntity {
        void onPlayerAttackTarget(AttackEntityEvent event);
    }

    @FunctionalInterface
    public interface AdvancementEarned {
        void onAdvancementEarnedEvent(AdvancementEvent.AdvancementEarnEvent event);
    }

    @FunctionalInterface
    public interface ArrowLoose {
        void onArrowLoose(ArrowLooseEvent event);
    }
}