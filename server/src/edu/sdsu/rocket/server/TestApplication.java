package edu.sdsu.rocket.server;

import java.io.IOException;

import com.badlogic.gdx.math.MathUtils;

import edu.sdsu.rocket.models.Sensors;

public class TestApplication extends Application {

	private float x;

	public TestApplication(Sensors sensors) {
		super(sensors);
	}
	
	@Override
	protected void setupSensors() throws IOException {
		sensors.setAccelerometerScalingFactor(0.001f);
		sensors.setGyroscopeScalingFactor(1f);
	}
	
	@Override
	protected void loopSensors() throws IOException {
		x += 0.01f;
		float c = MathUtils.cos(x); // -1 to 1
		float s = MathUtils.sin(x); // -1 to 1
		float cp = (c / 2f) + 0.5f; // 0 to 1
		float sp = (s / 2f) + 0.5f; // 0 to 1
		
		sensors.accelerometer[Sensors.X_INDEX] = (short) (s * 9.8 * 100);
		sensors.accelerometer[Sensors.Y_INDEX] = (short) (c * 9.8 * 100);
		sensors.accelerometer[Sensors.Z_INDEX] = (short) (s * 9.8 * 100);
		sensors.gyroscope[Sensors.X_INDEX] = (short) (s * 360);
		sensors.gyroscope[Sensors.Y_INDEX] = (short) (c * 360);
		sensors.gyroscope[Sensors.Z_INDEX] = (short) (s * 360);
		sensors.barometer[Sensors.BAROMETER_PRESSURE_INDEX]    = (int) cp * 1000 * 100;
		sensors.barometer[Sensors.BAROMETER_TEMPERATURE_INDEX] = (int) sp * 100 * 100;
		sensors.analog[Sensors.MOTOR_INDEX]    = sp * 5000;
		sensors.analog[Sensors.LOX_INDEX]      = sp * 5000;
		sensors.analog[Sensors.KEROSENE_INDEX] = sp * 5000;
		sensors.analog[Sensors.HELIUM_INDEX]   = sp * 5000;
		
		logger.log(ADS1115_LOG, sensors.analog);
		logger.log(ADXL345_LOG, sensors.accelerometer);
		
		try {
			Thread.sleep(10L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
