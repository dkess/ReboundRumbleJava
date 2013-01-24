/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Victor;
import com.sun.squawk.util.MathUtils;

//import java.lang.*;
/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class RobotTemplate extends IterativeRobot {
	VictorPair ingest;
	Victor elevator;
	VictorPair shooter;
	RobotDrive robotDrive;
	JStick xbox;
	JStick joystick;
	Compressor compressor;
	Solenoid driveGearA;
	Solenoid driveGearB;
	Encoder leftEnc;
	Encoder rightEnc;
	DriverStationLCD lcd;
	int selectedGear;

	boolean goingStraight;
	boolean goingBack;
	int leftEncInitial;
	int rightEncInitial;
	int deltaR;
	int deltaL;
	boolean cheesyDrive;

	public RobotTemplate() {
		ingest = new VictorPair(5,6);
		elevator = new Victor(1);
		//elevatorVictorB = new Victor(2);

		shooter = new VictorPair(2,4);

		robotDrive = new RobotDrive(8, 7, 10, 9);
		xbox = new JStick(1,10,6);
		joystick = new JStick(2,11,3);
		compressor = new Compressor(4, 3);
		compressor.start();

		driveGearA = new Solenoid(1);
		driveGearB = new Solenoid(2);
		driveGearA.set(true);
		driveGearB.set(false);
		selectedGear = 1;

		leftEnc = new Encoder(6, 7, false);
		leftEnc.setDistancePerPulse(0.103672558);

		rightEnc = new Encoder(8, 9, false);
		rightEnc.setDistancePerPulse(0.103672558);

		lcd = DriverStationLCD.getInstance();
	}

	public void robotInit() {
		leftEnc.start();
		rightEnc.start();
	}

	/**
	 * This function is called once each time the robot enters autonomous mode.
	 */
	public void autonomousPeriodic() {
	}

	/**
	 * This function is called once each time the robot enters operator control.
	 */
	public void teleopPeriodic() {
		xbox.update();
		joystick.update();

		if (xbox.isReleased(JStick.XBOX_A)) {
			cheesyDrive = !cheesyDrive;
		}

		if (xbox.isReleased(JStick.XBOX_RB) && driveGearA.get()) {
			driveGearA.set(false);
			driveGearB.set(true);
			selectedGear = 2;
		} else if (xbox.isReleased(JStick.XBOX_LB) && driveGearB.get()) {
			driveGearA.set(true);
			driveGearB.set(false);
			selectedGear = 1;
		}

		ingest.setAsBool(joystick.isPressed(2), true);

		elevator.set(joystick.isPressed(2) ? -0.75 :
					(joystick.isPressed(1) ? 0.75 : 0));

		shooter.checkAndSet(joystick.isPressed(3), -(joystick.getAxis(JStick.JOYSTICK_KNOB) - 1) / 2);

		double lspeed, rspeed;
		double leftStickX = xbox.getAxis(JStick.XBOX_LSX);
		double leftStickY = xbox.getAxis(JStick.XBOX_LSY);
		double rightStickY = xbox.getAxis(JStick.XBOX_RSY);

		// comment
		lspeed = Math.abs(leftStickY) >= 0.001 ? leftStickY : 0;
		rspeed = Math.abs(rightStickY) >= 0.001 ? rightStickY : 0;

		double trigger = xbox.getAxis(JStick.XBOX_TRIG);

		if (Math.abs(trigger) > 0.0001) {
			if (trigger > 0.0001) {
				lspeed = rspeed = MathUtils.pow(1.8, trigger) - 0.8;
				goingBack = false;
			} else if (trigger < -0.0001) {
				lspeed = rspeed = MathUtils.pow(1.8, Math.abs(trigger)) - 0.8;
				goingBack = true;
			}
			if (goingStraight) {
				deltaR = rightEnc.getRaw() - rightEncInitial;
				deltaL = leftEnc.getRaw() - leftEncInitial;
				if (deltaR > deltaL) {
					lcd.println(DriverStationLCD.Line.kUser2, 3,
							"inreasing l");
					if (goingBack) {
						// rspeed += (deltaR-deltaL)*0.02;
					} else {
						// lspeed += (deltaR-deltaL)*0.02;
					}
				} else {
					lcd.println(DriverStationLCD.Line.kUser2, 3,
							"inreasing r");
					if (goingBack) {
						// lspeed += (deltaL-deltaR)*0.02;
					} else {
						// rspeed += (deltaL-deltaR)*0.02;
					}
				}
				if (lspeed > 1) {
					rspeed -= (lspeed - 1);
				}
				if (rspeed > 1) {
					lspeed -= (rspeed - 1);
				}
				if (goingBack) {
					rspeed = -rspeed;
					lspeed = -lspeed;
				}
			} else {
				goingStraight = true;
				leftEncInitial = leftEnc.getRaw();
				rightEncInitial = rightEnc.getRaw();
			}
		} else {
			goingStraight = false;
			lcd.println(DriverStationLCD.Line.kUser2, 1, "DL:" + deltaL
					+ " DR:" + deltaR);
		}

		// cheesy drive

		if (lspeed > 1) {
			lspeed = 1;
		} else if (lspeed < -1) {
			lspeed = -1;
		}

		if (rspeed > 1) {
			rspeed = 1;
		} else if (rspeed < -1) {
			rspeed = -1;
		}

		double angular_power = 0;
		double overPower = 0.0;
		double sensitivity = 1.25;
		double rPower = 0;
		double lPower = 0;

		if (xbox.isPressed(JStick.XBOX_X)) {
			overPower = 1;
			sensitivity = 1;
			angular_power = -leftStickX;
		} else {
			overPower = 0;
			angular_power = rightStickY * leftStickX * sensitivity;
		}
		rPower = lPower = rightStickY;
		lPower += angular_power;
		rPower -= angular_power;

		if (lPower > 1) {
			rPower -= overPower * (lPower - 1);
			lPower = 1;
		} else if (rPower > 1) {
			lPower -= overPower * (rPower - 1);
			rPower = 1;
		} else if (lPower < -1) {
			rPower += overPower * (-1 - lPower);
			lPower = -1;
		} else if (rPower < -1) {
			lPower += overPower * (-1 - rPower);
			rPower = -1;
		}

		if (cheesyDrive) {
			robotDrive.tankDrive(lPower, rPower);
		} else {
			robotDrive.tankDrive(lspeed, rspeed);
		}

		/*
		 * if (xbox.getRawButton(XBOX_A)) {
		 * lcd.println(DriverStationLCD.Line.kUser2, 0,
		 * "resetting encoder"); leftEnc.reset(); rightEnc.reset(); } else
		 * if (xbox.getRawButton(XBOX_B)) {
		 * lcd.println(DriverStationLCD.Line.kUser2, 0, "stopping encoder");
		 * leftEnc.stop(); rightEnc.stop(); } else if
		 * (xbox.getRawButton(XBOX_X)) {
		 * lcd.println(DriverStationLCD.Line.kUser2, 0, "stopping encoder");
		 * leftEnc.start(); rightEnc.start(); } else if
		 * (leftEnc.getStopped()) {
		 * lcd.println(DriverStationLCD.Line.kUser2, 0, "Encoder stopped");
		 * }
		 */

		// lcd.println(DriverStationLCD.Line.kUser1, 1, "Raw:" +
		// leftEnc.getRaw() + "," + leftEnc.getRate());
		/*
		lcd.println(DriverStationLCD.Line.kUser1, 1, cheesyDrive ? "cheesy"
				: "tank");
		lcd.println(DriverStationLCD.Line.kUser2, 1, ""
				+ (goingStraight ? "s" : "n"));
		lcd.println(DriverStationLCD.Line.kUser3, 1, "" + lspeed);
		lcd.println(DriverStationLCD.Line.kUser3, 16, "" + rspeed);
		// lcd.println(DriverStationLCD.Line.kUser4, 1, "rev3 " +
		// shooter.getSpeed());
		lcd.println(DriverStationLCD.Line.kUser4, 1, "" + leftEncInitial
				+ " " + rightEncInitial);
		lcd.println(DriverStationLCD.Line.kUser5, 1,
				"" + leftEnc.getDistance());
		lcd.println(DriverStationLCD.Line.kUser6, 1,
				"Knob " + (-(joystick.getRawAxis(JOYSTICK_KNOB) - 1) / 2));
		*/
		lcd.println(DriverStationLCD.Line.kUser1, 1,
				"" + elevator.get());
		lcd.println(DriverStationLCD.Line.kUser2, 1,
				"" + shooter.getA());
		lcd.println(DriverStationLCD.Line.kUser3, 1, cheesyDrive ? "cheesy" : "tank");
		// lcd.println(DriverStationLCD.Line.kUser3, 1,
		// ""+elevatorVictorA.get());
		lcd.println(DriverStationLCD.Line.kUser4, 1,
				"" + shooter.getB());
		lcd.println(DriverStationLCD.Line.kUser5, 1,
				"" + ingest.getA());
		lcd.println(DriverStationLCD.Line.kUser6, 1,
				"" + ingest.getB());
		/*
		 * lcd.println(DriverStationLCD.Line.kUser1, 1, "" +
		 * xbox.getRawAxis(XBOX_LSX) + "," + xbox.getRawAxis(XBOX_LSY));
		 * lcd.println(DriverStationLCD.Line.kUser2, 1, "" +
		 * xbox.getRawAxis(XBOX_RSX) + "," + xbox.getRawAxis(XBOX_RSY));
		 * lcd.println(DriverStationLCD.Line.kUser3, 1, "" +
		 * xbox.getRawAxis(XBOX_TRIG));
		 * lcd.println(DriverStationLCD.Line.kUser4, 1, "" +
		 * (xbox.getRawButton(XBOX_A) ? "A" : " ") +
		 * (xbox.getRawButton(XBOX_B) ? "B" : " ") +
		 * (xbox.getRawButton(XBOX_X) ? "X" : " ") +
		 * (xbox.getRawButton(XBOX_Y) ? "Y" : " ") +
		 * (xbox.getRawButton(XBOX_LB) ? "LB" : "  "));
		 * lcd.println(DriverStationLCD.Line.kUser5, 1, "" +
		 * (joystick.getRawButton(JOYSTICK_1) ? "1" : " ") +
		 * (joystick.getRawButton(JOYSTICK_2) ? "2" : " ") +
		 * (joystick.getRawButton(JOYSTICK_3) ? "3" : " ") +
		 * (joystick.getRawButton(JOYSTICK_4) ? "4" : " "));
		 * lcd.println(DriverStationLCD.Line.kUser6, 1, "rev7 "+loops);
		 */
		lcd.updateLCD();
	}

	/**
	 * This function is called once each time the robot enters test mode.
	 */
	public void test() {
	}
}
