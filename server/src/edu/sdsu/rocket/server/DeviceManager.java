package edu.sdsu.rocket.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeviceManager {
	
	public interface Device {
		public void loop() throws IOException;
	}
	
	private List<DeviceThread> threads = new ArrayList<DeviceThread>();

	public void add(Device device) {
		add(device, 0);
	}
	
	public void add(Device device, long throttle) {
		DeviceThread thread = new DeviceThread(new DeviceRunnable(device, throttle));
		thread.setName(device.getClass().getSimpleName());
		threads.add(thread);
		Console.log("Starting " + thread.getName() + " thread.");
		thread.start();
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
	
	private class DeviceRunnable implements Runnable {
		
		long start = System.nanoTime();
		long frequency;
		long loops;
		
		private long throttle;
		private long sleep;
		
		final Device device;

		public DeviceRunnable(Device device, long throttle) {
			if (device == null) throw new NullPointerException();
			this.device = device;
			this.throttle = throttle;
		}
		
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					device.loop();
					
					if (sleep != 0) {
						// http://stackoverflow.com/q/4300653/196486
						if (sleep > 999999) {
							long ms = sleep / 1000000;
							long ns = sleep % 1000000;
							Thread.sleep(ms, (int) ns);
						} else {
							Thread.sleep(0, (int) sleep);
						}
					}
					
					loops++;
					long time = System.nanoTime();
					if (time - start > 1000000000) {
						if (throttle != 0) {
							long dt = (time - start) / loops - sleep; // actual time per loop
							long t = (time - start) / throttle; // target time per loop
							sleep = t - dt;
							if (sleep < 0) sleep = 0;
						}
						
						frequency = loops;
						loops = 0;
						start = time;
					}
				} catch (IOException e) {
					Console.error(e.getMessage());
				} catch (InterruptedException e) {
					return;
				}
			}
		}
		
	}
	
}
