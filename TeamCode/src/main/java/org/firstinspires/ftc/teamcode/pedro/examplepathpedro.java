
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

@Autonomous(name = "Safe BioBuzz Ramp Auto", group = "Autonomous")
public class examplepathpedro extends OpMode {

    private Follower follower;
    private final PoseFactory p = PoseFactory.degrees();

    // --- FIELD CONFIGURATION & POSES (0-144 Inch Standard Grid) ---

    // Starting on the Audience Wall, 24 inches in from the corner.
    // Half of robot length (16/2 = 8) is subtracted if back is flush against the wall.
    private final Pose startPose = p.of(24, 8, 0);

    // --- POSITION 1: First Blue Hive / Ramp Alignment ---
    // The exact destination aligned squarely in front of the left ramp
    private final Pose blueHiveRampPose = p.of(20, 116, 90);
    // Pulls the robot inward to stay clear of the corner boundaries during transit
    private final Pose curveToHiveControl = p.of(40, 60, 45);

    // --- POSITION 2: Opposite Side Blue Ramp Alignment ---
    // Target position cleanly matching the opposite ramp
    private final Pose oppositeRampPose = p.of(124, 116, 90);
    // Pushes the robot out toward the center field line to wide-sweep the center structures
    private final Pose escapeHiveControl = p.of(45, 90, 90);
    private final Pose approachOppositeControl = p.of(100, 90, 90);

    // --- PATH GENERATORS ---

    // Path 1: Curves out from the wall to align straight down into the first ramp
    private Path scoreFirstRampPath() {
        return curve(startPose, curveToHiveControl, blueHiveRampPose)
                .linear(startPose, blueHiveRampPose);
    }

    // Path 2: Wide sweep away from the first structure, around the center elements, onto the second ramp
    private Path scoreSecondRampPath() {
        return curve(blueHiveRampPose, escapeHiveControl, approachOppositeControl, oppositeRampPose)
                .linear(blueHiveRampPose, oppositeRampPose);
    }

    @Override
    public void init() {
        Scheduler.reset();
        follower = Constants.create(hardwareMap);
        follower.setPose(startPose); // Sync localizer with absolute wall position
    }

    @Override
    public void start() {
        // Sequentially route path segments to the Ivy scheduler framework
        schedule(
                // 1. Move safely from the wall to the first scoring ramp
                follow(follower, scoreFirstRampPath()),

                // TODO: insert code here for your shooter mechanism to empty elements

                // 2. Clear the structure perimeter and shift to the opposite side ramp
                follow(follower, scoreSecondRampPath())

                // TODO: insert code here for your second shooter activation
        );
    }

    @Override
    public void loop() {
        follower.update();  // Drive hardware-level motor vectors via Pinpoint feedback loops
        Scheduler.execute(); // Run the sequential execution list managed by Ivy
    }
}
