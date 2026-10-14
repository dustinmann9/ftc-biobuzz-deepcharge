package org.firstinspires.ftc.teamcode.pedro;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import org.firstinspires.ftc.teamcode.pedro.Constants;

@TeleOp(name = "Pedro 3.0 Position Tracker", group = "Utility")
public class GetCoordinatesUtil extends OpMode {

    private Follower follower;
    // Defining your intended baseline starting coordinates
    private final Pose initialPose = new Pose(24.0, 24.0, Math.toRadians(0.0));

    @Override
    public void init() {
        // Instantiate the follower tracking framework using your Constants layout
        follower = Constants.create(hardwareMap);

        // Initial request to force the hardware localizer tracking state
        follower.setPose(initialPose);

        telemetry.addLine("=== SYSTEM INITIALIZED ===");
        telemetry.addLine("Look at the screen during INIT to ensure it matches 24, 24.");
        telemetry.update();
    }

    @Override
    public void init_loop() {
        /*
         * CRITICAL FIX: The Pinpoint chip requires consecutive updates during the init
         * phase to successfully clear out its memory registers and latch onto the offset.
         */
        follower.update();
        follower.setPose(initialPose);

        Pose currentPose = follower.pose();
        telemetry.addLine("====== CALIBRATION / INIT LOOP TRACKING ======");
        telemetry.addData("Live X (Target 24)", "%.2f", currentPose.x());
        telemetry.addData("Live Y (Target 24)", "%.2f", currentPose.y());
        telemetry.addData("Live Heading (Target 0)", "%.2f", Math.toDegrees(currentPose.heading()));
        telemetry.addLine("----------------------------------------------");
        telemetry.addLine("Keep the robot perfectly still. Ensure these numbers settle close to 24, 24.");
        telemetry.update();
    }

    @Override
    public void start() {
        telemetry.clearAll();
    }

    @Override
    public void loop() {
        // Constantly grab the values from your Pinpoint hardware as you push it manually
        follower.update();

        Pose currentPose = follower.pose();

        telemetry.addLine("====== LIVE MANUAL POSITION TRACKING ======");
        telemetry.addData("Current X (Inches)", "%.2f", currentPose.x());
        telemetry.addData("Current Y (Inches)", "%.2f", currentPose.y());
        telemetry.addData("Current Heading (Deg)", "%.2f", Math.toDegrees(currentPose.heading()));
        telemetry.addLine("--------------------------------------------");
        telemetry.addLine("Manually push the robot around the field to observe values:");
        telemetry.addLine("-> Push FORWARD: X must increase.");
        telemetry.addLine("-> Push LEFT: Y must increase.");
        telemetry.addLine("-> Spin COUNTER-CLOCKWISE: Heading must increase.");
        telemetry.update();
    }
}
