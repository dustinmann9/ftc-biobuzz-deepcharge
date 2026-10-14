package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.drive.DirectMecanumDrive;
import org.firstinspires.ftc.teamcode.drive.DriveTrain;

/**
 * BACKUP TeleOp: same controls and mechanisms, but drives the motors directly with the
 * Control Hub IMU. Uses no Pedro and no Pinpoint, so use it when either is broken.
 * Set the motor names, directions and hub orientation in DirectMecanumDrive first.
 */
@TeleOp(name = "BioBuzz TeleOp (Backup)", group = "Drive")
public class BioBuzzTeleOpBackup extends BioBuzzTeleOpBase {

    @Override
    protected DriveTrain createDrive(HardwareMap hardwareMap) {
        return new DirectMecanumDrive(hardwareMap);
    }
}
