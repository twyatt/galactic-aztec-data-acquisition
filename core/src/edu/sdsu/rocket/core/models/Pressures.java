package edu.sdsu.rocket.core.models;

public class Pressures {

	public static final double MOTOR_MAX_PRESSURE    = 100;
	public static final double LOX_MAX_PRESSURE      = 600;
	public static final double KEROSENE_MAX_PRESSURE = 600;
	public static final double HELIUM_MAX_PRESSURE   = 2500;
	
	private final Analog analog;

	public Pressures(Analog analog) {
		this.analog = analog;
	}
	
	public float getMotor() {
//		float volts = analog.getA0()  / 1000f;
//		return MathHelper.translate(volts, 0f, 3.3f, 0f, (float) MOTOR_MAX_PRESSURE);
		
		// P51-500-A-A-I36-5V-000-000
		// calibrated transducer #1 on Nov 13, 2014
		return 0.210439f * analog.getA0() - 150.502f;
	}
	
	public float getLOX() {
//		float volts = analog.getA1()  / 1000f;
//		return MathHelper.translate(volts, 0f, 3.3f, 0f, (float) LOX_MAX_PRESSURE);
		
		// P51-500-A-A-I36-5V-000-000
		// calibrated transducer #2 on Nov 11, 2014
		return 0.20688f * analog.getA1() - 143.273f;
	}
	
	public float getKerosene() {
//		float volts = analog.getA2() / 1000f;
//		return MathHelper.translate(volts, 0f, 3.3f, 0f, (float) KEROSENE_MAX_PRESSURE);
		
		// P51-500-A-A-I36-5V-000-000
		// calibrated transducer #3 on Nov 11, 2014
		return 0.212968f * analog.getA2() - 147.109f;
	}
	
	public float getHelium() {
//		float volts = analog.getA3() / 1000f;
//		return MathHelper.translate(volts, 0f, 3.3f, 0f, (float) HELIUM_MAX_PRESSURE);
		
		// MSP-300-2K5-P-4-N-1
		// calibrated transducer #4 on Nov 13, 2014
		return 1.060797f * analog.getA3() - 653.691f;
	}
	
}
