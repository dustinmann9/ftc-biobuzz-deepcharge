package org.firstinspires.ftc.teamcode.pedro;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.lib.Dashboard;
import org.firstinspires.ftc.teamcode.lib.PoseStorage;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.Transport;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.commands.Commands.instant;
import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.commands.Commands.waitUntil;
import static com.pedropathing.ivy.groups.Groups.race;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

/**
 * Red: shoot the preloads, pick up the balls along the bottom wall, come back and shoot again.
 *
 *   1  at START: flywheel spins up, then intake + transport + gate run -> shoot
 *   2  flywheel and gate stop; intake + transport keep running (balls wait at the gate)
 *   3  curve (bowing away from the wall) to APPROACH, just before the balls
 *   4  creep slowly along the row to INTAKE_END, intake first, eating the balls one after
 *      another -- then wait INTAKE_WAIT_MS
 *   5  straight back to SHOOT; once there, flywheel spins up, then gate -> shoot again
 *
 * The heading stays HEADING_DEG the whole time. The creep drives in a straight line from
 * APPROACH to INTAKE_END in any direction (it works out forward/sideways from the heading),
 * so moving the balls only needs new numbers.
 *
 * Field, as measured on the robot: (0,0) is the bottom-right corner, the robot starts against
 * the x = 0 wall, and the balls lie along that wall at higher y. Heading 0 = the robot's
 * forward points +x (away from that wall); 90 = forward points +y (toward the balls). Poses are the robot's CENTER (robot 18" x 18",
 * so the center is 9" from a wall it touches). Targets stay 2-3" off the walls, and every
 * drive has a time limit, so the robot never pushes forever at a point it can't reach.
 *
 * All values are live-editable in Panels (Configurables > BiobuzzRedAuto), read at PLAY.
 * Needs this robot's Pedro tuning finished first.
 */
@Configurable
@Autonomous(name = "Biobuzz Red Shoot Auto", group = "Autonomous")
public class BiobuzzRedAuto extends OpMode {

    public static double HEADING_DEG = 0;

    /** Where the robot is placed: pushed against the wall, so y = 9 (8 is inside it). */
    public static double START_X = 9, START_Y = 80;
    /** Where it comes back to shoot the second time (2" off the wall). */
    public static double SHOOT_X = 11, SHOOT_Y = 80;

    /** End of the curve, just before the first ball. Assumes the balls start near y = 120. */
    public static double APPROACH_X = 15, APPROACH_Y = 128;
    /** Bends the curve up and away from the wall. The robot does not drive through it. */
    public static double CONTROL_X = 36, CONTROL_Y = 104;
    /** End of the creep, where the robot sits after taking the balls. */
    public static double INTAKE_END_X = 11, INTAKE_END_Y = 128;

    /** Power while creeping along the balls (0-1). Lower = slower, gentler intake. */
    public static double CREEP_POWER = 0.3;

    public static double SPIN_UP_MS = 3000;
    /** Long enough for ALL balls to go through. */
    public static double SHOOT_MS = 7000;
    /** Extra time at the end of the row so the last ball gets pulled in. */
    public static double INTAKE_WAIT_MS = 1000;
    /** Safety limits: give up on a drive after this long and carry on. */
    public static double PATH_TIMEOUT_MS = 5000;
    public static double CREEP_TIMEOUT_MS = 4000;

    private final PoseFactory p = PoseFactory.degrees();
    private final ElapsedTime creepTimer = new ElapsedTime();

    private Follower follower;
    private Dashboard dashboard;
    private Intake intake;
    private Transport transport;
    private Shooter shooter;

    private String step = "waiting for PLAY";

    private Pose pose(double x, double y) {
        return p.of(x, y, HEADING_DEG);
    }

    @Override
    public void init() {
        Scheduler.reset();
        dashboard = new Dashboard(telemetry);
        follower = Constants.create(hardwareMap);
        follower.setPose(pose(START_X, START_Y));

        intake = new Intake(hardwareMap);
        transport = new Transport(hardwareMap);
        shooter = new Shooter(hardwareMap);
    }

    @Override
    public void init_loop() {
        follower.update();
        follower.setPose(pose(START_X, START_Y));

        dashboard.addLine("Biobuzz Red Shoot Auto -- robot at the start pose, heading " + HEADING_DEG + ".");
        dashboard.addData("Start (x, y, heading)", START_X + ", " + START_Y + ", " + HEADING_DEG);
        dashboard.showPose(follower.pose());
        intake.report(dashboard);
        transport.report(dashboard);
        shooter.report(dashboard);
        dashboard.update();
    }

    @Override
    public void start() {
        Pose start = pose(START_X, START_Y);
        Pose shootSpot = pose(SHOOT_X, SHOOT_Y);
        Pose approach = pose(APPROACH_X, APPROACH_Y);
        Pose intakeEnd = pose(INTAKE_END_X, INTAKE_END_Y);
        follower.setPose(start);

        Path toBalls = curve(start, pose(CONTROL_X, CONTROL_Y), approach).constant(start);
        Path backToShoot = line(intakeEnd, shootSpot).constant(start);

        shooter.setSpinning(true);
        step = "spinning up (1)";

        schedule(sequential(
                // 1. Shoot the preloads.
                waitMs(SPIN_UP_MS),
                instant(() -> {
                    intake.setState(Intake.State.INTAKE);
                    transport.setState(Transport.State.FEED);
                    shooter.setGateTestOpen(true);
                    step = "shooting (1)";
                }),
                waitMs(SHOOT_MS),

                // 2. Shooter off; intake + transport keep running, balls wait at the gate.
                instant(() -> {
                    shooter.setGateTestOpen(false);
                    shooter.setSpinning(false);
                    step = "curving to the balls";
                }),

                // 3. Curve around to just right of the balls.
                driveWithTimeout(toBalls),

                // 4. Creep backwards along the row, then let the last ball in.
                instant(() -> {
                    creepTimer.reset();
                    // Field direction to the end of the row, turned into the robot's own
                    // forward / left, since manual() drives relative to the robot.
                    double[] dir = creepDirection();
                    double h = Math.toRadians(HEADING_DEG);
                    double forward = dir[0] * Math.cos(h) + dir[1] * Math.sin(h);
                    double left = -dir[0] * Math.sin(h) + dir[1] * Math.cos(h);
                    follower.manual(CREEP_POWER * forward, CREEP_POWER * left, 0);
                    step = "intaking along the wall";
                }),
                waitUntil(() -> pastIntakeEnd(follower.pose())
                        || creepTimer.milliseconds() > CREEP_TIMEOUT_MS),
                instant(() -> follower.hold(follower.pose())),
                waitMs(INTAKE_WAIT_MS),

                // 5. Back to the shooting spot, then spin up and shoot.
                instant(() -> {
                    intake.setState(Intake.State.OFF);
                    transport.setState(Transport.State.OFF);
                    step = "driving back to shoot";
                }),
                driveWithTimeout(backToShoot),
                instant(() -> {
                    follower.hold(follower.pose());
                    shooter.setSpinning(true);
                    step = "spinning up (2)";
                }),
                waitMs(SPIN_UP_MS),
                instant(() -> {
                    transport.setState(Transport.State.FEED);
                    shooter.setGateTestOpen(true);
                    step = "shooting (2)";
                }),
                waitMs(SHOOT_MS),
                instant(() -> {
                    shooter.setGateTestOpen(false);
                    shooter.setSpinning(false);
                    transport.setState(Transport.State.OFF);
                    step = "done -- holding position";
                })
        ));
    }

    /** Unit vector (x, y) on the field from APPROACH to INTAKE_END. */
    private static double[] creepDirection() {
        double dx = INTAKE_END_X - APPROACH_X;
        double dy = INTAKE_END_Y - APPROACH_Y;
        double length = Math.hypot(dx, dy);
        return length == 0 ? new double[] {0, 0} : new double[] {dx / length, dy / length};
    }

    /** True once the robot has reached (or passed) INTAKE_END along the creep direction. */
    private static boolean pastIntakeEnd(Pose pose) {
        double[] dir = creepDirection();
        return (pose.x() - INTAKE_END_X) * dir[0] + (pose.y() - INTAKE_END_Y) * dir[1] >= 0;
    }

    /** Follows a path, but moves on after PATH_TIMEOUT_MS even if the end is never reached. */
    private Command driveWithTimeout(Path path) {
        return race(follow(follower, path), waitMs(PATH_TIMEOUT_MS));
    }

    @Override
    public void loop() {
        follower.update();
        Scheduler.execute();

        intake.update();
        transport.update();
        shooter.update();

        Pose pose = follower.pose();
        PoseStorage.save(pose);

        dashboard.addData("Step", step);
        dashboard.showPose(pose);
        intake.report(dashboard);
        transport.report(dashboard);
        shooter.report(dashboard);
        dashboard.update();
    }

    @Override
    public void stop() {
        Scheduler.reset();
        follower.stop();
        intake.stop();
        transport.stop();
        shooter.stop();
    }
}
