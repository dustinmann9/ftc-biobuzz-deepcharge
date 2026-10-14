package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.drive.DriveTrain;
import org.firstinspires.ftc.teamcode.lib.Button;
import org.firstinspires.ftc.teamcode.lib.Dashboard;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.Transport;

/**
 * Everything both BioBuzz TeleOps share: controls, field-centric math, mechanisms, dashboard.
 * The two OpModes differ only in which DriveTrain createDrive() returns. A control change made
 * here reaches both, so the backup never drifts out of date.
 *
 * Not an OpMode on its own (it's abstract and has no @TeleOp), so it never shows on the DS.
 * The loop never waits for anything.
 *
 * Controls (gamepad 1, or a controller plugged into the Panels browser tab):
 *   left stick        drive (field-centric by default)
 *   right stick X     turn
 *   left trigger      hold for slow mode
 *   right trigger     hold for turbo
 *   A                 make the way the robot faces now the new "forward"
 *   BACK              switch field-centric / robot-centric
 *   B                 hold to lock in place (Pedro: holds position; backup: brakes)
 *   X                 backup only: switch BRAKE / COAST
 *   Y                 backup only: switch velocity / raw power (needs drive encoders!)
 *   D-pad Up          toggle intake (+ transport)
 *   D-pad Down        hold to eject (intake and transport backwards)
 *   D-pad Left        toggle shooter flywheel
 *   right bumper      hold to fire (only feeds once the flywheel is at speed)
 */
public abstract class BioBuzzTeleOpBase extends LinearOpMode {

    /** Which drivetrain this TeleOp uses. Called once, during init. */
    protected abstract DriveTrain createDrive(HardwareMap hardwareMap);

    private DriveTrain drive;
    private Dashboard dashboard;
    private Gamepad pad;

    private Intake intake;
    private Transport transport;
    private Shooter shooter;

    /** Field heading that counts as "away from the driver". Set at start and by button A. */
    private double driverForwardHeading;

    // Must be ABOVE the buttons -- they register themselves while being created.
    private final Button.Group buttons = new Button.Group();

    private final Button slowMode       = buttons.add(() -> pad.left_trigger > 0.5);
    private final Button turboMode      = buttons.add(() -> pad.right_trigger > 0.5);
    private final Button resetForward   = buttons.add(() -> pad.a);
    private final Button toggleCentric  = buttons.add(() -> pad.back);
    private final Button lockPosition   = buttons.add(() -> pad.b);
    private final Button brakeMode      = buttons.add(() -> pad.x);
    private final Button velocityMode   = buttons.add(() -> pad.y);

    private final Button intakeOn       = buttons.toggle(() -> pad.dpad_up);
    private final Button eject          = buttons.add(() -> pad.dpad_down);
    private final Button flywheelOn     = buttons.toggle(() -> pad.dpad_left);
    private final Button fire           = buttons.add(() -> pad.right_bumper);

    @Override
    public void runOpMode() {
        dashboard = new Dashboard(telemetry);
        drive = createDrive(hardwareMap);

        intake = new Intake(hardwareMap);
        transport = new Transport(hardwareMap);
        shooter = new Shooter(hardwareMap);

        // INIT: keep the sensors running so heading/pose are live before PLAY.
        while (opModeInInit()) {
            drive.update();
            dashboard.addLine("BioBuzz TeleOp [" + drive.name() + "] ready. Press PLAY.");
            drive.report(dashboard);
            dashboard.update();
        }
        if (isStopRequested()) {
            return;
        }

        driverForwardHeading = drive.heading();

        while (opModeIsActive()) {
            readGamepad();
            updateDriving();
            drive.update();
            updateMechanisms();
            updateDashboard();
        }

        drive.stop();
        intake.stop();
        transport.stop();
        shooter.stop();
    }

    /** Reads the gamepad once, so everything below sees the same thing. */
    private void readGamepad() {
        pad = dashboard.mergeGamepad(gamepad1);
        buttons.update();
    }

    private void updateDriving() {
        if (toggleCentric.pressed()) {
            DriverSettings.FIELD_CENTRIC = !DriverSettings.FIELD_CENTRIC;   // same switch Panels shows
        }
        if (resetForward.pressed()) {
            driverForwardHeading = drive.heading();
        }
        if (brakeMode.pressed()) {
            drive.toggleBrakeMode();
        }
        if (velocityMode.pressed()) {
            drive.toggleVelocityMode();
        }

        if (lockPosition.down()) {
            drive.lock(lockPosition.pressed());
            return;
        }

        // Pedro's frame: +forward, +left, +turn = counter-clockwise. The gamepad's Y axis is
        // negative when pushed forward, and its X axes are positive to the right, so all
        // three are flipped.
        double forward = -deadzone(pad.left_stick_y);
        double left = -deadzone(pad.left_stick_x);
        double turn = -deadzone(pad.right_stick_x) * DriverSettings.TURN_SCALE;

        if (DriverSettings.FIELD_CENTRIC) {
            // Rotate the stick (which is in the driver's frame) into the robot's frame.
            double angle = drive.heading() - driverForwardHeading;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double robotForward = forward * cos + left * sin;
            double robotLeft = -forward * sin + left * cos;
            forward = robotForward;
            left = robotLeft;
        }

        double speed = DriverSettings.NORMAL_SPEED;
        if (slowMode.down()) {
            speed = DriverSettings.SLOW_SPEED;
        } else if (turboMode.down()) {
            speed = DriverSettings.TURBO_SPEED;
        }

        drive.drive(forward * speed, left * speed, turn * speed);
    }

    private void updateMechanisms() {
        // Ejecting wins over the toggle and lasts only while held. The toggle is left alone,
        // so letting go puts the intake back to what it was doing.
        if (eject.down()) {
            intake.setState(Intake.State.EJECT);
            transport.setState(Transport.State.REVERSE);
        } else {
            intake.setState(intakeOn.isOn() ? Intake.State.INTAKE : Intake.State.OFF);
            // The transport runs while intaking or firing, so pieces keep moving to the shooter.
            boolean feed = intakeOn.isOn() || shooter.isFiring();
            transport.setState(feed ? Transport.State.FEED : Transport.State.OFF);
        }

        shooter.setSpinning(flywheelOn.isOn());
        shooter.setFire(fire.down());

        intake.update();
        transport.update();
        shooter.update();
    }

    private void updateDashboard() {
        dashboard.section("DRIVE: " + drive.name());
        dashboard.addData("Mode", lockPosition.down() ? "LOCKED"
                : DriverSettings.FIELD_CENTRIC ? "FIELD-CENTRIC" : "ROBOT-CENTRIC");
        dashboard.addData("Speed", slowMode.down() ? "SLOW" : turboMode.down() ? "TURBO" : "NORMAL");
        drive.report(dashboard);

        intake.report(dashboard);
        transport.report(dashboard);
        shooter.report(dashboard);

        dashboard.update();
    }

    private static double deadzone(double stick) {
        return Math.abs(stick) < DriverSettings.STICK_DEADZONE ? 0.0 : stick;
    }
}
