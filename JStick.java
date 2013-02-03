package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Joystick;

public class JStick {
	public static final int XBOX_A = 1;
	public static final int XBOX_B = 2;
	public static final int XBOX_X = 3;
	public static final int XBOX_Y = 4;
	public static final int XBOX_LB = 5;
	public static final int XBOX_RB = 6;
	public static final int XBOX_BACK = 7;
	public static final int XBOX_START = 8;
	public static final int XBOX_LJ = 9;
	public static final int XBOX_RJ = 10;

	public static final int XBOX_LSX = 1; // left stick x
	public static final int XBOX_LSY = 2; // left stick y
	public static final int XBOX_TRIG = 3; // left is positive
	public static final int XBOX_RSX = 4; // right stick x
	public static final int XBOX_RSY = 5; // right stick y
	public static final int XBOX_DPAD = 6; // buggy

	public static final int JOYSTICK_KNOB = 3;

	private Joystick jstick;
	private boolean[] buttonPressed;
	private boolean[] buttonLastPressed;
	private double[] axes;

	public JStick(int port, int maxButtons, int maxAxes) {
		jstick = new Joystick(port);
		buttonPressed = new boolean[maxButtons+1];
		buttonLastPressed = new boolean[maxButtons+1];
		axes = new double[maxAxes+1];
	}

	public void update() {
		//buttonLastPressed = buttonPressed.clone();
		//System.arrayCopy(buttonPressed, 0, buttonPressed, 0, buttonPressed.length);
		//buttonLastPressed = Arrays.copyOf(buttonPressed, buttonPressed.length);

		for(int i = 1; i < buttonPressed.length; ++i) {
			buttonLastPressed[i] = buttonPressed[i];
			buttonPressed[i] = jstick.getRawButton(i);
		}

		for(int i = 1; i < axes.length; ++i) {
			axes[i] = jstick.getRawAxis(i);
		}
	}

	public boolean isPressed(int b) {
		try {
			return buttonPressed[b];
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isReleased(int b) {
		try {
			return !buttonPressed[b] && buttonLastPressed[b];
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public double getAxis(int b) {
		try {
			return axes[b];
		} catch (ArrayIndexOutOfBoundsException e) {
			return 0;
		}
	}
	
	public static double removeJitter(double in, double jitterRange) {
		if (Math.abs(in) > jitterRange) {
			return in;
		} else {
			return 0;
		}
	}
}
