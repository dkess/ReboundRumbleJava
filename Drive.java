package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SpeedController;

public class Drive extends RobotDrive {
	// should maintain a constant speed on both sides of the robot
	// by reading encoder values
	public boolean straightDriveEnc(double power, double leftRate, double rightRate) {
		double kp = Preferences.getInstance().getDouble("kp", 0.1);
		if (power != 0) {
			double lspeed, rspeed;
			lspeed = rspeed = 0;
			
			rspeed = lspeed = curveInput(power,2);
			if (Math.abs(leftRate) > Math.abs(rightRate)) {
				lspeed -= (leftRate-rightRate)*kp;
			} else {
				rspeed -= (rightRate-leftRate)*kp;
			}

			tankDrive(lspeed,rspeed);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean straightDrive(double power) {
		return straightDriveEnc(power,0,0);
	}

	// thanks to team 254 for CheesyDrive
	// cheesy drive uses one joystick for throttle, and the other for turning
	// also supports a "quickturn" function that allows the robot to spin
	// in place
	double old_turn;
	double neg_inertia_accumulator;
	double quickStopAccumulator;
	public boolean cheesyDrive(double power, double turn, boolean spin) {
		if (power == 0) {
			return false;
		}

		double neg_inertia = turn - old_turn;
		old_turn = turn;

		turn = curveInput(turn,2);

		double neg_inertia_scalar;
		neg_inertia_scalar = 5;

		double neg_inertia_power = neg_inertia * neg_inertia_scalar;
		neg_inertia_accumulator += neg_inertia_power;
		turn += neg_inertia_power;
		if(neg_inertia_accumulator > 1) {
			neg_inertia_accumulator -= 1;
		} else if (neg_inertia_accumulator < -1) {
			neg_inertia_accumulator += 1;
		} else {
			neg_inertia_accumulator = 0;
		}

		double overPower = 0.0;
		double angular_power = 0;
		if (spin) {
			if (Math.abs(power) < 0.2) {
				quickStopAccumulator = 0.8*quickStopAccumulator + 0.2*turn*5;
			}
			overPower = 1;
			angular_power = turn;
		} else {
			angular_power = power * turn - quickStopAccumulator;
			if (quickStopAccumulator > 1) {
				quickStopAccumulator -= 1;
			} else if (quickStopAccumulator < -1) {
				quickStopAccumulator += 1;
			} else {
				quickStopAccumulator = 0;
			}
		}

		double rPower = 0;
		double lPower = 0;

		rPower = lPower = power;
		lPower += angular_power;
		rPower -= angular_power;

		if (lPower > 1) {
			rPower-= overPower * (lPower - 1);
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

		tankDrive(lPower, rPower);
		return true;
	}
	
	public double getLeft() {
		return (m_frontLeftMotor.get() + m_rearLeftMotor.get())/2;
	}
	public double getRight() {
		return (m_frontRightMotor.get() + m_rearRightMotor.get())/2;
	}

	public Drive(int arg0, int arg1, int arg2, int arg3) {
		super(arg0, arg1, arg2, arg3);
		old_turn = 0;
		neg_inertia_accumulator = 0;
		quickStopAccumulator = 0;
	}
	public Drive(SpeedController frontLeftMotor,
			SpeedController rearLeftMotor,
			SpeedController frontRightMotor,
			SpeedController rearRightMotor) {
		super(frontLeftMotor,rearLeftMotor,frontRightMotor,rearRightMotor);
		old_turn = 0;
		neg_inertia_accumulator = 0;
		quickStopAccumulator = 0;
	}

	private double curveInput(double in, int iterations) {
		if (iterations > 0) {
			return curveInput(Math.sin(Math.PI*in/2),iterations-1);
		} else {
			return in;
		}
	}
	
	// use removeJitter to get rid of the jitter from joysticks
	static public double removeJitter(double in, double jitterRange) {
		if (Math.abs(in) < jitterRange) {
			return 0;
		} else {
			return in;
		}
	}
}
