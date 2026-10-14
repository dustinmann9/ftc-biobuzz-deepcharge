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

@Autonomous(name = "Pedro Ivy Example Auto", group = "Autonomous")
public class ExampleAuto extends OpMode {

    private Follower follower;
    private final PoseFactory p = PoseFactory.degrees();

    // Define the autonomous positions on the field
    private final Pose startPose = p.of(24, 24, 0);
    private final Pose parkPose = p.of(48, 48, 90); // Renamed from 'park' to avoid method naming collision

    // Build the path routine using Pedro Pathing 3.0 builder syntax
    private Path park() {
        return line(startPose, parkPose).linear(startPose, parkPose);
    }

    @Override
    public void init() {
        Scheduler.reset(); // Reset Ivy framework scheduler context
        follower = Constants.create(hardwareMap); // Instantiates the follower utilizing your Constants
        follower.setPose(startPose); // Seed the localizer's initial position on the field
    }

    @Override
    public void start() {
        // Enqueue the path sequence straight to Ivy's execution scheduler
        schedule(follow(follower, park()));
    }

    @Override
    public void loop() {
        follower.update(); // Keeps localization tracking and PIDs continuously refreshed
        Scheduler.execute(); // Drives the automated command actions handled by Ivy
    }
}
