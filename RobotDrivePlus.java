package edu.wpi.first.wpilibj.templates;

import com.sun.squawk.util.MathUtils;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.RobotDrive;

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
	
	public boolean straightDrive(double power, double leftRate, double rightRate) {
		double kp = Preferences.getInstance().getDouble("kp", 0.1);
		if (Math.abs(power) > jitterRange) {
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

	double old_turn;
	public boolean cheesyDrive(double power, double turn, boolean spin) {
		if (Math.abs(power) < jitterRange && Math.abs(turn) < jitterRange) {
			return false;
		}

		double neg_inertia = turn - old_turn;
		old_turn = turn;

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
		if (Math.abs(leftValue) < jitterRange && Math.abs(rightValue) < jitterRange) {
			return false;
		}
		
		tankDrive(leftValue, rightValue);
		return true;
	}
	
	public double getLeft() {
		return (m_frontLeftMotor.get() + m_rearLeftMotor.get())/2;
	}
	public double getRight() {
		return (m_frontRightMotor.get() + m_rearRightMotor.get())/2;
	}

	public RobotDrivePlus(int arg0, int arg1, int arg2, int arg3) {
		super(arg0, arg1, arg2, arg3);
		old_turn = 0;
	}

	private double curveInput(double in, int iterations) {
		if (iterations > 0) {
			return curveInput(Math.sin(Math.PI*in/2),iterations-1);
		} else {
			return in;
		}
	}
}
