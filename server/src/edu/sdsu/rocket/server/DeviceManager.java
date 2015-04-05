package edu.sdsu.rocket.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.sdsu.rocket.helpers.Console;
import edu.sdsu.rocket.helpers.RateLimitedRunnable;

public class DeviceManager {
	
	public interface Device {
		public void loop() throws IOException;
	}
	
	private List<DeviceThread> threads = new ArrayList<DeviceThread>();
	
	public DeviceRunnable add(Device device) {
		DeviceRunnable runnable = new DeviceRunnable(device);
		DeviceThread thread = new DeviceThread(runnable);
		thread.setName(device.getClass().getSimpleName());
		threads.add(thread);
		Console.log("Starting " + thread.getName() + " thread.");
		thread.start();
		return runnable;
	}
	
	public void clear() {
		for (Thread thread : threads) {
			Console.log("Stopping " + thread.getName() + " thread.");
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		threads.clear();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < threads.size(); i++) {
			if (i != 0) builder.append("; ");
			
			DeviceThread thread = threads.get(i);
			builder.append(thread.getName() + ": " + thread.runnable.frequency + " Hz");
		}
		return getClass().getSimpleName() + ": [" + builder.toString() + "]";
	}
	
	private class DeviceThread extends Thread {
		
		DeviceRunnable runnable;

		public DeviceThread(DeviceRunnable runnable) {
			super(runnable);
			this.runnable = runnable;
		}
		
	}
	
	public class DeviceRunnable extends RateLimitedRunnable {
		
		private static final long NANOSECONDS_PER_SECOND = 1000000000L;
		
		long start = System.nanoTime();
		long frequency;
		long loops;
		
		private long throttle;
		
		final Device device;

		public DeviceRunnable(Device device) {
			if (device == null) throw new NullPointerException();
			this.device = device;
		}
		
		/**
		 * Set the throttle of the runnable loop.
		 * 
		 * @param throttle (Hz)
		 */
		public void setThrottle(long throttle) {
			this.throttle = throttle;
		}
		
		@Override
		public void loop() {
			try {
				device.loop();
			} catch (IOException e) {
				Console.error(e);
			}
			
			loops++;
			long time = System.nanoTime();
			if (time - start > NANOSECONDS_PER_SECOND) {
				if (throttle != 0) {
					long dt = (time - start) / loops - getSleepNanoseconds(); // actual time per loop
					long t = (time - start) / throttle; // target time per loop
					long sleep = t - dt;
					if (sleep < 0) sleep = 0;
					setSleepNanoseconds(sleep);
				}
				
				frequency = loops;
				loops = 0;
				start = time;
			}
		}
		
	}
	
}
