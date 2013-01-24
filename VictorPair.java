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

	public VictorPair(int a, double adjA, int b, double adjB) {
		va = new Victor(a);
		va.set(0);
		adjustA = adjA;

		vb = new Victor(b);
		vb.set(0);
		adjustB = adjB;
	}

	public double getA() {
		return va.get();
	}
	
	public double getB() {
		return vb.get();
	}

	public void set(double speed) {
		va.set(speed*adjustA);
		vb.set(speed*adjustB);
	}

	public void setAsBool(boolean on, boolean dir) {
		set(on ? (dir ? 1 : -1) : 0);
	}

	public void checkAndSet(boolean on, double speed) {
		set(on ? speed : 0);
	}
}
