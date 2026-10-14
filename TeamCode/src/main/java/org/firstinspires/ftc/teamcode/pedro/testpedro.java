package org.firstinspires.ftc.teamcode.pedro;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.pedropathing.follower.Follower;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import static com.pedropathing.api.Paths.*;
import com.pedropathing.paths.Path;
import com.pedropathing.ivy.Scheduler;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import static com.pedropathing.ivy.pedro.PedroCommands.hold;
import static com.pedropathing.ivy.commands.Commands.*;

@Autonomous(name = "Pedro Long Straight 180 Auto", group = "Autonomous")
public class testpedro extends OpMode {

    private Follower follower;
    private final PoseFactory p = PoseFactory.degrees();
    private com.qualcomm.robotcore.util.ElapsedTime startTimer;

    // --- POSES (0-144 inch standard grid) ---
    // Start: Audience side (X=8), Third tile from right (Y=56), Facing downfield (0 deg)
    private final Pose startPose = p.of(8.0, 56.0, 0.0);

    /*
     * 🛠️ PATH COMPENSATED SPECIFICATIONS:
     * Physical Goal: End at X=134.0 (10 inches away from the 144-inch back wall)
     * Scaled Target: We add your 8-inch drift compensation factor (134.0 + 8.0 = 142.0)
     * Heading Target: Flips exactly 180 degrees at the destination point
     */
    private final Pose targetPose = p.of(142.0, 56.0, 180.0);

    // --- PATH DEFINITION ---
    private Path straightToOtherEnd() {
        // Runs a straight downfield path while smoothly spinning the heading to 180 degrees
        return line(startPose, targetPose).linear(startPose, targetPose);
    }

    @Override
    public void init() {
        Scheduler.reset();
        follower = Constants.create(hardwareMap);
        follower.setPose(startPose);
        startTimer = new com.qualcomm.robotcore.util.ElapsedTime();
    }

    @Override
    public void init_loop() {
        follower.update();
        follower.setPose(startPose);

        telemetry.addLine("=== SYSTEM INITIALIZED ===");
        telemetry.addData("Init X (Target 8)", "%.2f", follower.pose().x());
        telemetry.addData("Init Y (Target 56)", "%.2f", follower.pose().y());
        telemetry.update();
    }

    @Override
    public void start() {
        // Re-enforce standard origin bounds right on the starting frame edge
        follower.setPose(startPose);
        follower.update();
        startTimer.reset();

        // Enqueue actions sequentially into Ivy's scheduler framework
        schedule(
                // 1. Blocks framework updates for 5 seconds to note initial metrics safely
                waitMs(5000),

                // 2. Drive straight down the Y=56 line and rotate 180 degrees
                follow(follower, straightToOtherEnd()),

                // 3. Lock down tightly at exactly 180 degrees heading alignment using your tuned PIDs
                hold(follower, targetPose)
        );
    }

    @Override
    public void loop() {
        follower.update();  // Drives continuous path speed calculation matrices via Pinpoint feedback
        Scheduler.execute(); // Process active scheduled command pipelines

        // --- LIVE COORDINATE TRACKING ON DRIVER STATION ---
        Pose currentPose = follower.pose();
        double secondsLeft = 5.0 - startTimer.seconds();

        telemetry.addLine("====== LIVE PATH POSITION ======");

        if (secondsLeft > 0) {
            telemetry.addLine(String.format("🛑 HOLDING PLACE TO NOTE DOWN: %.1fs left", secondsLeft));
        } else {
            telemetry.addData("Is Sequence Finished?", !follower.isBusy());
        }

        telemetry.addData("Current X (Inches)", "%.2f", currentPose.x());
        telemetry.addData("Current Y (Inches)", "%.2f", currentPose.y());
        telemetry.addData("Current Heading (Deg)", "%.2f", Math.toDegrees(currentPose.heading()));
        telemetry.addLine("--------------------------------");
        telemetry.addData("Target X", "%.1f", targetPose.x());
        telemetry.addData("Target Y", "%.1f", targetPose.y());
        telemetry.addData("Target Heading", "%.1f", Math.toDegrees(targetPose.heading()));
        telemetry.update();
    }
}
