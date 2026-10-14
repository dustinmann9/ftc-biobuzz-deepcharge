package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.pedro.procedures.Tests;
import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.math.Vector2D;
import com.pedropathing.math.Matrix;






public class Constants {
    public static Follower create(HardwareMap h) {
        return new Follower(
                new PinpointLocalizer(h, localizerConfig),
                new Mecanum(h, drivetrainConfig),
                new Foresight(foresightConfig)
        );
    }

    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("frontLeft");
        c.frontRightName.set("frontRight");
        c.backLeftName.set("backLeft");
        c.backRightName.set("backRight");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.REVERSE);

        c.manualBrakeMode.set(true);
    }
    );

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(-3.625293941948358);
        c.yPodOffset.set(6.416896910179318);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.2028873024260306);
                Controller secondaryTranslationalForward = Controller.proportional(0.07496143699785292);
                Controller primaryTranslationalLateral = Controller.proportional(0.40632253206133756);
                Controller secondaryTranslationalLateral = Controller.proportional(0.15012531845864877);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.017366291025395764));
                c.brake.set(Controller.proportionalFeedforward(0.0147613473715864));

                c.headingFeedback.set(Controller.proportional(2.809998778456779));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.0455109118633278, 0.014156502412308068));

                c.linearBrakeCoefficients.set(Matrix.diag(0.0662703145848394, 0.07888077315723822));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.002037379414543449, 0.0019434675964263546));

                c.maxAchievableForwardVelocity.set(60.61107056520691);
                c.maxAchievableStrafeVelocity.set(48.23443165702571);
                c.naturalForwardDeceleration.set(34.40721016329761);
                c.naturalStrafeDeceleration.set(45.077558371999935);
            }
    );

}