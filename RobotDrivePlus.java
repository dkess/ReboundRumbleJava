package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.RobotDrive;
import com.sun.squawk.util.MathUtils;

public class RobotDrivePlus extends RobotDrive {
	private double jitterRange;
	public double getJitterRange() {
		return jitterRange;
	}
	public void setJitterRange(double a) {
		jitterRange = a;
	}

	private double straightExp;
	public double getStraightExp() {
		return straightExp;
	}
	public void setStraightExp(double a) {
		straightExp = a;
	}

	public boolean straightDrive(double power) {
		if (Math.abs(power) > jitterRange) {
			double speed = 0;
			if (power > 0) {
				speed = MathUtils.pow(1+straightExp, power) - straightExp;
			} else if (power < 0) {
				speed = -(MathUtils.pow(1+straightExp, Math.abs(power)) - straightExp);
			}

			tankDrive(speed,speed);
			return true;
		} else {
			return false;
		}
	}

	public boolean cheesyDrive(double power, double turn, boolean spin) {
		if (Math.abs(power) > jitterRange || Math.abs(turn) > jitterRange) {
			return false;
		}
		double angular_power = 0;
		double overPower = 0.0;
		double sensitivity = 1.25;
		double rPower = 0;
		double lPower = 0;

		if(spin) {
			overPower = 1;
			sensitivity = 1;
			angular_power = -turn;
		} else {
			overPower = 0;
			angular_power = power * turn * sensitivity;
		}

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

	public boolean jitTankDrive(double leftValue, double rightValue) {
		if (Math.abs(leftValue) > jitterRange || Math.abs(rightValue) > jitterRange) {
			return false;
		}
		tankDrive(leftValue, rightValue);
		return true;
	}

	public RobotDrivePlus(int arg0, int arg1, int arg2, int arg3) {
		super(arg0, arg1, arg2, arg3);
	}
}
