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
		sensors.accelerometer.setScalingFactor(0.001f);
		sensors.gyroscope.setScalingFactor(1f);
	}
	
	@Override
	public void loop() throws IOException {
		super.loop();
		
		x += 0.01f;
		float c = MathUtils.cos(x); // -1 to 1
		float s = MathUtils.sin(x); // -1 to 1
		float cp = (c / 2f) + 0.5f; // 0 to 1
		float sp = (s / 2f) + 0.5f; // 0 to 1
		
		sensors.accelerometer.setRawX((int) (s * 9.8 * 100));
		sensors.accelerometer.setRawY((int) (c * 9.8 * 100));
		sensors.accelerometer.setRawZ((int) (s * 9.8 * 100));
		logger.log(ADXL345_LOG, sensors.accelerometer.getRawX(), sensors.accelerometer.getRawY(), sensors.accelerometer.getRawZ());
		
		sensors.gyroscope.setRawX((short) (s * 360));
		sensors.gyroscope.setRawY((short) (c * 360));
		sensors.gyroscope.setRawZ((short) (s * 360));
		logger.log(ITG3205_LOG, sensors.gyroscope.getRawX(), sensors.gyroscope.getRawY(), sensors.gyroscope.getRawZ());
		
		sensors.barometer.setRawTemperature((int) sp * 100 * 100);
		sensors.barometer.setRawPressure((int) cp * 1000 * 100);
		logger.log(MS5611_LOG, sensors.barometer.getRawTemperature(), sensors.barometer.getRawPressure());
		
		sensors.analog.setA0(sp * 5000);
		sensors.analog.setA1(sp * 5000);
		sensors.analog.setA2(sp * 5000);
		sensors.analog.setA3(sp * 5000);
		logger.log(ADS1115_LOG, sensors.analog.getA0(), sensors.analog.getA1(), sensors.analog.getA2(), sensors.analog.getA3());
		
		try {
			Thread.sleep(10L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
