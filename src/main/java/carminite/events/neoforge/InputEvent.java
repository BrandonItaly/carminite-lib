package carminite.events.neoforge;

import carminite.events.CarminiteEvent;
import carminite.events.api.ClientEvents;
import net.minecraft.client.input.KeyEvent;
import org.jetbrains.annotations.ApiStatus;

public abstract class InputEvent extends CarminiteEvent {

    @ApiStatus.Internal
    protected InputEvent() {}

    public static class Key extends InputEvent {
        private final KeyEvent keyEvent;
        private final int action;

        @ApiStatus.Internal
        public Key(KeyEvent keyEvent, int action) {
            this.keyEvent = keyEvent;
            this.action = action;
        }

        public KeyEvent getKeyEvent() {
            return keyEvent;
        }

        public int getKey() {
            return this.keyEvent.key();
        }

        public int getScanCode() {
            return this.keyEvent.keycode();
        }

        public int getAction() {
            return this.action;
        }

        public int getModifiers() {
            return this.keyEvent.modifiers();
        }

        @Override
        public Key post() {
            ClientEvents.INPUT_KEY.invoker().onKeyInput(this);
            return this;
        }
    }
}
