package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.lib.Dashboard;
import org.firstinspires.ftc.teamcode.lib.VelocityMotor;

/**
 * PLACEHOLDER -- no shooter on the robot yet. A velocity-controlled flywheel plus a gate
 * (CR servo) that pushes a piece into it.
 *
 * The gate only opens once the flywheel is at speed, so a shot is never fired from a slow
 * wheel. On a bare chassis there is no wheel to measure, so the placeholder pretends to be at
 * speed whenever it is spinning -- that keeps the controls testable.
 *
 * Velocity control matters for a shooter: raw power drops as the battery drains and shots
 * fall short. RPM and PIDF are live-editable in Panels; tune on the real wheel.
 */
@Configurable
public class Shooter implements Subsystem {

    public static boolean ENABLED = false;
    public static String FLYWHEEL_NAME = "shooter";
    public static String GATE_NAME = "shooterGate";
    public static boolean FLYWHEEL_REVERSED = false;
    public static boolean GATE_REVERSED = false;
    /** goBILDA bare 6000 RPM motor = 28. Change if you gear the flywheel motor. */
    public static double FLYWHEEL_TICKS_PER_REV = 28;

    // Live-editable in Panels.
    public static double TARGET_RPM = 4000;
    public static double GATE_POWER = 1.0;
    public static double VEL_P = 200;
    public static double VEL_I = 0;
    public static double VEL_D = 0;
    public static double VEL_F = 12;

    private final VelocityMotor flywheel;   // null on a bare chassis
    private final CRServo gate;             // null on a bare chassis
    private final String status;

    private boolean spinning = false;
    private boolean fireRequested = false;

    public Shooter(HardwareMap hardwareMap) {
        DcMotorEx motor = ENABLED ? hardwareMap.tryGet(DcMotorEx.class, FLYWHEEL_NAME) : null;
        flywheel = motor == null ? null : new VelocityMotor(motor,
                FLYWHEEL_REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD,
                FLYWHEEL_TICKS_PER_REV);

        gate = ENABLED ? hardwareMap.tryGet(CRServo.class, GATE_NAME) : null;
        if (gate != null) {
            gate.setDirection(GATE_REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
        }

        if (!ENABLED) {
            status = "placeholder (disabled)";
        } else if (flywheel == null || gate == null) {
            status = "MISSING" + (flywheel == null ? " '" + FLYWHEEL_NAME + "'" : "")
                    + (gate == null ? " '" + GATE_NAME + "'" : "") + " in robot config";
        } else {
            status = "active";
        }
    }

    public void setSpinning(boolean spinning) {
        this.spinning = spinning;
    }

    public boolean isSpinning() {
        return spinning;
    }

    /** Hold true to feed pieces. Ignored until the flywheel is at speed. */
    public void setFire(boolean fire) {
        this.fireRequested = fire;
    }

    public boolean atSpeed() {
        if (flywheel == null) {
            return spinning;   // placeholder: no wheel to measure
        }
        return flywheel.atSpeed();
    }

    /** True while the gate is actually pushing a piece. */
    public boolean isFiring() {
        return fireRequested && spinning && atSpeed();
    }

    @Override
    public void update() {
        if (flywheel != null) {
            flywheel.setPIDF(VEL_P, VEL_I, VEL_D, VEL_F);
            flywheel.setTargetRPM(spinning ? TARGET_RPM : 0);
        }
        if (gate != null) {
            gate.setPower(isFiring() ? GATE_POWER : 0);
        }
    }

    @Override
    public void report(Dashboard dashboard) {
        dashboard.section("SHOOTER");
        dashboard.addData("Shooter", (spinning ? "SPINNING" : "OFF") + "  [" + status + "]");
        dashboard.addData("RPM (actual / target)", flywheel == null ? "-- / " + Math.round(TARGET_RPM)
                : Math.round(flywheel.getRPM()) + " / " + Math.round(flywheel.getTargetRPM()));
        dashboard.addData("Ready to fire", atSpeed());
        dashboard.addData("Firing", isFiring());
    }

    @Override
    public void stop() {
        spinning = false;
        fireRequested = false;
        update();
    }
}
