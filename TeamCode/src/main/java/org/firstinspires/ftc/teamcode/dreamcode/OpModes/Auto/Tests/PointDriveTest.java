package org.firstinspires.ftc.teamcode.dreamcode.OpModes.Auto.Tests;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.dreamcode.OpModes.Auto.AutoTemplate;

@Autonomous
public class PointDriveTest extends AutoTemplate {

    @Override
    public void buildPath() {
        path.add(() -> tilePointDrive(0, 1, 0));
        path.add(() -> tilePointDrive(0, 0, 90));
        path.add(() -> tilePointDrive(1.5,0 , 90));
        //path.add(() -> tilePointDrive(2, 2, 90));
    }

}
