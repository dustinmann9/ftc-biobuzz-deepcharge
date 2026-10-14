package org.firstinspires.ftc.teamcode.teleop;

import com.bylazar.configurables.annotations.Configurable;

/** Driver feel, shared by both TeleOps. Live-editable in Panels > Configurables; not saved. */
@Configurable
public class DriverSettings {
    /** Stick values smaller than this count as zero, so a worn stick doesn't creep. */
    public static double STICK_DEADZONE = 0.08;
    public static double NORMAL_SPEED = 0.75;
    public static double SLOW_SPEED = 0.30;
    public static double TURBO_SPEED = 1.0;
    /** Turning feels faster than driving at the same power; scale it down separately. */
    public static double TURN_SCALE = 0.8;
    public static boolean FIELD_CENTRIC = true;
}
