package org.firstinspires.ftc.teamcode.diagnostics;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.lib.Dashboard;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * Push the robot by hand and check the numbers move the right way. Motors never run.
 * Run this after every change to Constants.java, before trusting any auto.
 *
 *   push FORWARD                        ->  x goes UP
 *   push LEFT                           ->  y goes UP
 *   spin COUNTER-CLOCKWISE (from above) ->  heading goes UP
 *   spin in place a full turn           ->  x and y barely move
 *   push exactly 48" along a tape line  ->  x changes by ~48
 *
 * If x/y spiral when you spin, a pod offset or direction is wrong. If 48" reads as 40", the
 * pod type / scale is wrong -- that, not path fudging, is the fix for "stops short".
 * A = reset to 0.
 */
@TeleOp(name = "Localization Check", group = "Diagnostics")
public class LocalizationCheck extends LinearOpMode {
    @Override
    public void runOpMode() {
        Dashboard dashboard = new Dashboard(telemetry);
        Follower follower = Constants.create(hardwareMap);
        follower.setPose(new Pose(0, 0, 0));

        dashboard.addLine("Push by hand. Motors are off. A = reset to 0.");
        dashboard.update();
        waitForStart();

        while (opModeIsActive()) {
            follower.update();
            if (gamepad1.a) {
                follower.setPose(new Pose(0, 0, 0));
            }
            dashboard.addLine("x forward+, y left+, heading CCW+");
            dashboard.showPose(follower.pose());
            dashboard.update();
        }
    }
}
