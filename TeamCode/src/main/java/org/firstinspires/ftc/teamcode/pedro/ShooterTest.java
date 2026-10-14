package org.firstinspires.ftc.teamcode.pedro;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "Shooter: Variable Speed Test", group = "Test")
public class ShooterTest extends LinearOpMode {

    private DcMotor shooterMotor = null;

    // Hardcoded to 50% speed
    public static final double SHOOTER_SPEED_PERCENTAGE = 0.70;

    @Override
    public void runOpMode() {
        // Looks for a motor named "test" in your configuration
        shooterMotor = hardwareMap.get(DcMotor.class, "test");
        shooterMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooterMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        telemetry.addData("Status", "Initialized.");
        telemetry.addData("Target Speed", (SHOOTER_SPEED_PERCENTAGE * 100) + "%");
        telemetry.update();

        waitForStart();

        // Turn the shooter motor ON at 80% immediately after pressing PLAY
        shooterMotor.setPower(SHOOTER_SPEED_PERCENTAGE);

        while (opModeIsActive()) {
            telemetry.addData("Shooter Status", "CONSTANTLY RUNNING");
            telemetry.addData("Current Power Setting", shooterMotor.getPower());
            telemetry.update();
        }

        // Safety fallback: kill power when you press STOP on the driver station
        shooterMotor.setPower(0.0);
    }
}
