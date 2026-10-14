package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.lib.Dashboard;

/**
 * PLACEHOLDER -- no intake on the robot yet.
 *
 * While ENABLED is false this never touches the hardware map, so the OpMode runs on a bare
 * chassis. The state still changes when the driver presses buttons and shows on the
 * dashboard, so the controls can be practised now.
 *
 * When the intake is built: add a motor named HARDWARE_NAME to the robot config, set
 * ENABLED = true here, and set the direction. If ENABLED is true but the motor is missing
 * from the config, the dashboard says so instead of crashing.
 */
@Configurable
public class Intake implements Subsystem {

    /** Read once at init. Changing it in Panels mid-run has no effect until the next init. */
    public static boolean ENABLED = false;
    public static String HARDWARE_NAME = "intake";
    public static boolean REVERSED = false;

    // Live-editable in Panels.
    public static double INTAKE_POWER = 0.8;
    public static double EJECT_POWER = -0.5;

    public enum State { OFF, INTAKE, EJECT }

    private final DcMotorEx motor;   // null on a bare chassis
    private final String status;
    private State state = State.OFF;

    public Intake(HardwareMap hardwareMap) {
        motor = ENABLED ? hardwareMap.tryGet(DcMotorEx.class, HARDWARE_NAME) : null;
        if (motor != null) {
            motor.setDirection(REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
        status = !ENABLED ? "placeholder (disabled)"
                : motor == null ? "MISSING '" + HARDWARE_NAME + "' in robot config"
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
        if (motor == null) {
            return;
        }
        switch (state) {
            case INTAKE: motor.setPower(INTAKE_POWER); break;
            case EJECT:  motor.setPower(EJECT_POWER);  break;
            default:     motor.setPower(0);            break;
        }
    }

    @Override
    public void report(Dashboard dashboard) {
        dashboard.section("INTAKE");
        dashboard.addData("Intake", state + "  [" + status + "]");
    }

    @Override
    public void stop() {
        state = State.OFF;
        update();
    }
}
