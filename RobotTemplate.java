/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.CounterBase;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class RobotTemplate extends IterativeRobot {
	public static final double jitterRange = 0.006;
	
	VictorPair ingest;
	Victor elevator;
	VictorPair shooter;
	Drive robotDrive;
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
		// initialize all the objects
		ingest = new VictorPair(5,6);
		elevator = new Victor(1);

		shooter = new VictorPair(2,4);

		robotDrive = new Drive(8, 7, 10, 9);
		xbox = new JStick(1);
		joystick = new JStick(2);
		compressor = new Compressor(4, 3);
		compressor.start();

		driveGearA = new Solenoid(1);
		driveGearB = new Solenoid(2);
		driveGearA.set(true);
		driveGearB.set(false);
		selectedGear = 1;

		leftEnc = new Encoder(6, 7, true,CounterBase.EncodingType.k2X);
		leftEnc.setDistancePerPulse(0.103672558);

		rightEnc = new Encoder(9, 10, false);
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

		// Press A to toggle cheesy drive
		if (xbox.isReleased(JStick.XBOX_A)) {
			cheesyDrive = !cheesyDrive;
		}

		// Use RB and LB to change gears
		if (xbox.isReleased(JStick.XBOX_RB) && driveGearA.get()) {
			driveGearA.set(false);
			driveGearB.set(true);
			selectedGear = 2;
		} else if (xbox.isReleased(JStick.XBOX_LB) && driveGearB.get()) {
			driveGearA.set(true);
			driveGearB.set(false);
			selectedGear = 1;
		}
	
		// if joystick button 2 is pressed, run the ingestor
		ingest.setAsBool(joystick.isPressed(2), true);

		// if joystick button 2 is pressed, run the elevator backwards
		// otherwise if joystick button 1 is pressed, run the elevator
		// forwards
		elevator.set(joystick.isPressed(2) ? -0.75 :
					(joystick.isPressed(1) ? 0.75 : 0));

		// if button 3 is pressed, run the shooter
		// based on what the knob value is
		shooter.checkAndSet(joystick.isPressed(3), -(joystick.getAxis(JStick.JOYSTICK_KNOB) - 1) / 2);
		
		// if the y button is pressed, reset the encoders
		if (xbox.isReleased(JStick.XBOX_Y)) {
			leftEnc.reset();
			rightEnc.reset();
		}

		double leftStickX = JStick.removeJitter(xbox.getAxis(JStick.XBOX_LSX), jitterRange);
		double leftStickY = JStick.removeJitter(xbox.getAxis(JStick.XBOX_LSY), jitterRange);
		double rightStickY = JStick.removeJitter(xbox.getAxis(JStick.XBOX_RSY), jitterRange);

		if (cheesyDrive) {
			robotDrive.cheesyDrive(rightStickY, leftStickX, xbox.isPressed(JStick.XBOX_LJ));
			lcd.println(DriverStationLCD.Line.kUser4,1,"cheesy");
		} else {
			if (!robotDrive.straightDriveEnc(xbox.getAxis(JStick.XBOX_TRIG),leftEnc.getRate(),rightEnc.getRate())) {
				robotDrive.tankDrive(leftStickY, rightStickY);
				lcd.println(DriverStationLCD.Line.kUser5,1,""+leftStickY);
				lcd.println(DriverStationLCD.Line.kUser6,1,""+rightStickY);
			} else {
				lcd.println(DriverStationLCD.Line.kUser4,1,"straightDrive");
			}
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
		/*
		lcd.println(DriverStationLCD.Line.kUser1, 1, "" + elevator.get());
		lcd.println(DriverStationLCD.Line.kUser2, 1, "" + shooter.getA());
		lcd.println(DriverStationLCD.Line.kUser3, 1, cheesyDrive ? "cheesy" : "tank");
		// lcd.println(DriverStationLCD.Line.kUser3, 1,
		// ""+elevatorVictorA.get());
		lcd.println(DriverStationLCD.Line.kUser4, 1, "" + shooter.getB());
		lcd.println(DriverStationLCD.Line.kUser5, 1, "" + ingest.getA());
		lcd.println(DriverStationLCD.Line.kUser6, 1, "" + ingest.getB());
		*/
		lcd.println(DriverStationLCD.Line.kUser1, 1, ""+xbox.getAxis(JStick.XBOX_LSY));
		lcd.println(DriverStationLCD.Line.kUser2, 1, ""+xbox.getAxis(JStick.XBOX_RSY));
		lcd.println(DriverStationLCD.Line.kUser3, 1, ""+Preferences.getInstance().getKeys());
		
		SmartDashboard.putNumber("LPower", robotDrive.getLeft());
		SmartDashboard.putNumber("RPower", robotDrive.getRight());
		SmartDashboard.putNumber("LEncRaw", leftEnc.get());
		SmartDashboard.putNumber("REncRaw", rightEnc.get());
		SmartDashboard.putNumber("LEncDist", leftEnc.getDistance());
		SmartDashboard.putNumber("REncDist", rightEnc.getDistance());
		SmartDashboard.putNumber("LEncRate", leftEnc.getRate());
		SmartDashboard.putNumber("REncRate", rightEnc.getRate());
		/*
		* 
		 * lcd.println(DriverStationLCD.Line.kUser1, 1, "" +
		 * xbox.getRawAxis(XBOX_LSX) + "," + xbox.getRawAxis(XBOX_LSY));
		 * lcd.println(DriverStationLCD.Line.kUser2, 1, "" +
		 * xbox.getRawAxis(XBOX_RSX) + "," + xbox.getRawAxis(XBOX_RSY));
		 * lcd.println(DriverStationLCD.Line.kUser3, 1, "" +
		 * xbox.getRawAxis(XBOX_TRIG));
		 * lcd.println(DriverStationLCD.Line.kUser4, 1, "" +
		 * (xbox.getRawButton(XBOX_A) ? "A" : " ") +,
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
