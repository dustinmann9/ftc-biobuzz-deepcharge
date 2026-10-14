package org.firstinspires.ftc.teamcode.pedro;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "Pedro Motor Direction Test", group = "Calibration")
public class PedroMotorDirectionTest extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        // 1. Initialize motors using the exact names from your Constants.java
        DcMotor frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        DcMotor frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        DcMotor backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        DcMotor backRight = hardwareMap.get(DcMotor.class, "backRight");

        // 2. Apply the directions currently set in your Constants.java
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE); // Or REVERSE, depending on your test

        // Set to float so you can easily see which wheel is actively resisting/spinning
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        telemetry.addLine("Ready! Place the robot on a block/stand so wheels spin freely.");
        telemetry.update();

        waitForStart();

        if (isStopRequested()) return;

        double testPower = 0.3; // Low power for safety

        // --- TEST 1: FRONT LEFT ---
        runSingleMotor(frontLeft, "FRONT LEFT", testPower);

        // --- TEST 2: FRONT RIGHT ---
        runSingleMotor(frontRight, "FRONT RIGHT", testPower);

        // --- TEST 3: BACK LEFT ---
        runSingleMotor(backLeft, "BACK LEFT", testPower);

        // --- TEST 4: BACK RIGHT ---
        runSingleMotor(backRight, "BACK RIGHT", testPower);

        // Turn everything off when done
        stopAllMotors(frontLeft, frontRight, backLeft, backRight);
        telemetry.addLine("Test Complete! All motors stopped.");
        telemetry.update();
    }

    private void runSingleMotor(DcMotor motor, String motorName, double power) {
        if (isStopRequested()) return;

        motor.setPower(power);

        // Keep running for 2 seconds while updating telemetry
        long startTime = System.currentTimeMillis();
        while (opModeIsActive() && (System.currentTimeMillis() - startTime < 2000)) {
            telemetry.addData("ACTIVE MOTOR", motorName);
            telemetry.addLine("This wheel MUST spin FORWARD.");
            telemetry.update();
        }

        motor.setPower(0);
        sleep(500); // Small pause between motors
    }

    private void stopAllMotors(DcMotor fl, DcMotor fr, DcMotor bl, DcMotor br) {
        fl.setPower(0);
        fr.setPower(0);
        bl.setPower(0);
        br.setPower(0);
    }
}
