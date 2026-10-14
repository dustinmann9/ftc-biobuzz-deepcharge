package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.drive.DriveTrain;
import org.firstinspires.ftc.teamcode.drive.PedroDrive;

/**
 * The normal TeleOp: drives through Pedro, heading from the Pinpoint. Runs on its own --
 * no auto needed first.
 * If it won't init or drives strangely, run "BioBuzz TeleOp (Backup)" instead.
 * Controls are listed in BioBuzzTeleOpBase.
 */
@TeleOp(name = "BioBuzz TeleOp", group = "Drive")
public class BioBuzzTeleOp extends BioBuzzTeleOpBase {

    @Override
    protected DriveTrain createDrive(HardwareMap hardwareMap) {
        return new PedroDrive(hardwareMap);
    }
}
