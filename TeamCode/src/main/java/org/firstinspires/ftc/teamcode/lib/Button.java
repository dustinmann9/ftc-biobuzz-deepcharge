package org.firstinspires.ftc.teamcode.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * A gamepad button. down() asks if it is held; pressed()/released() catch the moment it changes.
 *
 * Toggle buttons are opt-in: make them with Group.toggle() and they flip themselves on every
 * press, so there is no "if (pressed()) toggle();" line to forget.
 */
public class Button {

    private final BooleanSupplier source;
    private final boolean isToggle;
    private boolean now, before;
    private boolean on;

    private Button(BooleanSupplier source, boolean isToggle) {
        this.source = source;
        this.isToggle = isToggle;
    }

    /** Reads the gamepad. Group.update() calls this once per loop for every button. */
    public void update() {
        before = now;
        now = source.getAsBoolean();
        if (isToggle && pressed()) {
            on = !on;
        }
    }

    /** Is the button held down right now? */
    public boolean down() {
        return now;
    }

    /** Was the button just pushed down? True for one loop per press. */
    public boolean pressed() {
        return now && !before;
    }

    /** Was the button just let go? */
    public boolean released() {
        return !now && before;
    }

    /** Toggle buttons only: is it currently switched on? */
    public boolean isOn() {
        return on;
    }

    /** Toggle buttons only: force the state, e.g. to start with the intake running. */
    public void setOn(boolean on) {
        this.on = on;
    }

    /** A set of buttons that get read together, so none can be forgotten. */
    public static class Group {
        private final List<Button> all = new ArrayList<>();

        /** A plain button: use down(), pressed(), released(). */
        public Button add(BooleanSupplier source) {
            return register(new Button(source, false));
        }

        /** A button that flips on/off by itself on every press: use isOn(). */
        public Button toggle(BooleanSupplier source) {
            return register(new Button(source, true));
        }

        private Button register(Button button) {
            all.add(button);
            return button;
        }

        public void update() {
            for (Button button : all) {
                button.update();
            }
        }
    }
}
