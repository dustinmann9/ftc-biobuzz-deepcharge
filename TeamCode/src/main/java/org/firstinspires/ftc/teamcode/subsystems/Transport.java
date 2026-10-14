package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.lib.Dashboard;

/**
 * PLACEHOLDER -- no transport on the robot yet. Moves game pieces from the intake to the
 * shooter. Written for a continuous-rotation servo; swap to a DcMotorEx if you build it with
 * a motor -- nothing outside this file needs to change.
 *
 * Same rules as Intake: ENABLED = false never touches hardware, a missing device is reported
 * instead of crashing.
 */
@Configurable
public class Transport implements Subsystem {

    public static boolean ENABLED = false;
    public static String HARDWARE_NAME = "transport";
    public static boolean REVERSED = false;

    public static double FEED_POWER = 1.0;
    public static double REVERSE_POWER = -0.6;

    public enum State { OFF, FEED, REVERSE }

    private final CRServo servo;
    private final String status;
    private State state = State.OFF;

    public Transport(HardwareMap hardwareMap) {
        servo = ENABLED ? hardwareMap.tryGet(CRServo.class, HARDWARE_NAME) : null;
        if (servo != null) {
            servo.setDirection(REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
        }
        status = !ENABLED ? "placeholder (disabled)"
                : servo == null ? "MISSING '" + HARDWARE_NAME + "' in robot config"
                : "active";
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    @Override
    public void update() {
        if (servo == null) {
            return;
        }
        switch (state) {
            case FEED:    servo.setPower(FEED_POWER);    break;
            case REVERSE: servo.setPower(REVERSE_POWER); break;
            default:      servo.setPower(0);             break;
        }
    }

    @Override
    public void report(Dashboard dashboard) {
        dashboard.section("TRANSPORT");
        dashboard.addData("Transport", state + "  [" + status + "]");
    }

    @Override
    public void stop() {
        state = State.OFF;
        update();
    }
}
