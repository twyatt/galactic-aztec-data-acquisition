package edu.sdsu.rocket.server;

import java.io.FileOutputStream;
import java.io.IOException;

import com.badlogic.gdx.math.MathUtils;

import edu.sdsu.rocket.helpers.Console;
import edu.sdsu.rocket.io.ADS1115OutputStream;
import edu.sdsu.rocket.io.ADXL345OutputStream;
import edu.sdsu.rocket.io.ITG3205OutputStream;
import edu.sdsu.rocket.io.MS5611OutputStream;
import edu.sdsu.rocket.models.Sensors;

public class TestApplication extends Application {

	private float x;

	public TestApplication(Sensors sensors) {
		super(sensors);
	}
	
	@Override
	protected void setupSensors() throws IOException {
		Console.log("Setup ADS1115.");
		ads1115log = new ADS1115OutputStream(new FileOutputStream(logDir + FILE_SEPARATOR + ADS1115_LOG));
		
		Console.log("Setup ADX345.");
		adxl345log = new ADXL345OutputStream(new FileOutputStream(logDir + FILE_SEPARATOR + ADXL345_LOG));
		sensors.accelerometer.setScalingFactor(0.001f);
		adxl345log.writeScalingFactor(0.001f);
		
		Console.log("Setup ITG3205.");
		itg3205log = new ITG3205OutputStream(new FileOutputStream(logDir + FILE_SEPARATOR + ITG3205_LOG));
		sensors.gyroscope.setScalingFactor(1f);
		itg3205log.writeScalingFactor(1f);
		
		Console.log("Setup MS5611.");
		ms5611log = new MS5611OutputStream(new FileOutputStream(logDir + FILE_SEPARATOR + MS5611_LOG));
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					x += 0.01f;
					float c = MathUtils.cos(x); // -1 to 1
					float s = MathUtils.sin(x); // -1 to 1
					float cp = (c / 2f) + 0.5f; // 0 to 1
					float sp = (s / 2f) + 0.5f; // 0 to 1
					
					try {
						sensors.accelerometer.setRawX((int) (s * 9.8 * 100));
						sensors.accelerometer.setRawY((int) (c * 9.8 * 100));
						sensors.accelerometer.setRawZ((int) (s * 9.8 * 100));
						adxl345log.writeValues((short) sensors.accelerometer.getRawX(), (short) sensors.accelerometer.getRawY(), (short) sensors.accelerometer.getRawZ());
						
						sensors.gyroscope.setRawX((short) (s * 360));
						sensors.gyroscope.setRawY((short) (c * 360));
						sensors.gyroscope.setRawZ((short) (s * 360));
						itg3205log.writeValues((short) sensors.gyroscope.getRawX(), (short) sensors.gyroscope.getRawY(), (short) sensors.gyroscope.getRawZ());
						
						sensors.barometer.setRawTemperature((int) (sp * 100 * 100));
						sensors.barometer.setRawPressure((int) (cp * 1000 * 100));
						ms5611log.writeValues(sensors.barometer.getRawTemperature(), sensors.barometer.getRawPressure());
						
						sensors.analog.setA0(sp * 3300);
						ads1115log.writeValue(0, sensors.analog.getA0());
						sensors.analog.setA1(sp * 3300);
						ads1115log.writeValue(1, sensors.analog.getA0());
						sensors.analog.setA2(sp * 3300);
						ads1115log.writeValue(2, sensors.analog.getA0());
						sensors.analog.setA3(sp * 3300);
						ads1115log.writeValue(3, sensors.analog.getA0());
					} catch (IOException e) {
						Console.error(e);
					}
					
					try {
						Thread.sleep(10L);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		}).start();
	}

}
