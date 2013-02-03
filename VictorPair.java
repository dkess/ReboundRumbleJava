package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Victor;

public class VictorPair {
	private Victor va;
	private Victor vb;

	private double adjustA;
	private double adjustB;

	public VictorPair(int a, int b) {
		this(a,1.0,b,1.0);
	}
	
	// adjA and adjB adjust the speed of the specific victor
	public VictorPair(int a, double adjA, int b, double adjB) {
		va = new Victor(a);
		va.set(0);
		adjustA = adjA;

		vb = new Victor(b);
		vb.set(0);
		adjustB = adjB;
	}
	
	// getA and getB return the output
	// being sent to that victor.
	public double getA() {
		return va.get();
	}
	
	public double getB() {
		return vb.get();
	}
	
	// set both victors to the same value
	public void set(double speed) {
		va.set(speed*adjustA);
		vb.set(speed*adjustB);
	}

	// checks the boolean on. If it is true, set
	// the victor to 1 if dir is true, or set it
	// to -1 if dir is false (reverse).
	public void setAsBool(boolean on, boolean dir) {
		set(on ? (dir ? 1 : -1) : 0);
	}
	
	// checks the boolean on. If it is true, set the
	// victor to speed. Otherwise, set it to zero.
	public void checkAndSet(boolean on, double speed) {
		set(on ? speed : 0);
	}
}
