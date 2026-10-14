package org.firstinspires.ftc.teamcode.lib;

import com.pedropathing.math.Pose;

/**
 * Hands the robot's position from autonomous to teleop.
 *
 * Static fields survive between OpModes as long as the Robot Controller app is not restarted,
 * so auto writes here every loop and teleop reads it once in init. If the RC restarts in
 * between (or you run teleop on its own), pose is null and teleop starts from FALLBACK.
 */
public final class PoseStorage {

    /** Where teleop assumes the robot is when no auto ran first. */
    public static final Pose FALLBACK = new Pose(72, 72, 0);

    private static Pose pose = null;

    private PoseStorage() { }

    public static void save(Pose current) {
        pose = current;
    }

    /** The last pose auto saved, or FALLBACK. */
    public static Pose load() {
        return pose != null ? pose : FALLBACK;
    }

    public static boolean hasPose() {
        return pose != null;
    }

    /** Call after using it, so a later teleop-only run doesn't pick up a stale pose. */
    public static void clear() {
        pose = null;
    }
}
