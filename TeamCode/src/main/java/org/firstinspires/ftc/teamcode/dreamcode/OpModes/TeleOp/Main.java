package org.firstinspires.ftc.teamcode.dreamcode.OpModes.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.dreamcode.Robot;
import org.firstinspires.ftc.teamcode.dreamcode.States.DriveMode;

@TeleOp
public class Main extends Robot {
    int presses = 0;

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void loop() {
        super.loop();
        super.getDrive().POVMecanumDrive(-gamepad1.left_stick_y,
                gamepad1.left_stick_x, gamepad1.right_stick_x, DriveMode.Optimized);

        if (gamepad1.a) {super.getIo().openClaw();}
        if (gamepad1.b) {super.getIo().closeClaw();}
        if (gamepad1.y) {super.getIo().raiseLift();}
        if (gamepad1.x) {super.getIo().dropLift();}
        if (gamepad1.dpad_up) {super.getIo().setLiftHigh();}
        if (gamepad1.dpad_left || gamepad1.dpad_right) {super.getIo().setLiftMid();}
        if (gamepad1.dpad_down) {super.getIo().setLiftLow();}
        if (gamepad1.left_bumper || gamepad1.right_bumper) {super.getIo().setLiftDown();}

        super.getIo().PosAdjustLift(gamepad1.right_trigger - gamepad1.left_trigger);
        super.getIo().runLift(gamepad1.right_trigger - gamepad1.left_trigger);
    }
}
