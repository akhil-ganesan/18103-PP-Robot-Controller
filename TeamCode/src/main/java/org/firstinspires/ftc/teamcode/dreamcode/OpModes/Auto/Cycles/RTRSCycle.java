package org.firstinspires.ftc.teamcode.dreamcode.OpModes.Auto.Cycles;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.dreamcode.OpModes.Auto.AutoTemplate;

@Autonomous
public class RTRSCycle extends AutoTemplate {

    @Override
    public void buildPath() {
        setStartA(0);
        //path.add(this::closeClaw);
        path.add(() -> tilePointDrive(0, 1, 0));
        //path.add(this::openClaw);
        path.add(() -> tilePointDrive(0, .95, 0));
        //path.add(this::setLiftLow); -> Set to highest cone height
        //path.add(() -> tilePointDrive(2, .9, 0));
        sameSideCyclePark();
    }


}