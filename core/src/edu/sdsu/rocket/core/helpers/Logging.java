package edu.sdsu.rocket.core.helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.badlogic.gdx.utils.Array;

import edu.sdsu.rocket.core.io.ADS1115OutputStream;
import edu.sdsu.rocket.core.io.ADXL345OutputStream;
import edu.sdsu.rocket.core.io.ITG3205OutputStream;
import edu.sdsu.rocket.core.io.MS5611OutputStream;
import edu.sdsu.rocket.core.io.OutputStreamMultiplexer;

public class Logging {
	
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");

	private final Settings settings;
	private final Array<File> dirs = new Array<File>();
	
	public Logging(Settings settings) throws IOException {
		this.settings = settings;
		
		DateFormat dirDateFormat = new SimpleDateFormat(settings.logging.dateFormat);
		String timestamp = dirDateFormat.format(new Date());
		
		for (String dir : settings.logging.directories) {
			File root = new File(dir);
			if (!root.exists()) {
				throw new IOException("Logging directory does not exist: " + root);
			}
			File d = new File(root + FILE_SEPARATOR + timestamp);
			if (!d.exists()) {
				System.out.println("mkdir " + d);
				d.mkdirs();
			}
			dirs.add(d);
		}
	}
	
	public Array<File> getDirectories() {
		return dirs;
	}
	
	public ADXL345OutputStream openADXL345OutputStream() throws FileNotFoundException {
		String file = settings.devices.adxl345.logFile;
		if (file == null) {
			throw new RuntimeException("ADXL345 logFile not defined.");
		}
		
		OutputStreamMultiplexer multiplexer = new OutputStreamMultiplexer();
		ADXL345OutputStream out = new ADXL345OutputStream(multiplexer);
		
		for (File d : dirs) {
			File f = new File(d + FILE_SEPARATOR + file);
			multiplexer.add(new FileOutputStream(f));
		}
		return out;
	}
	
	public ITG3205OutputStream openITG3205OutputStream() throws FileNotFoundException {
		String file = settings.devices.itg3205.logFile;
		if (file == null) {
			throw new RuntimeException("ITG3205 logFile not defined.");
		}
		
		OutputStreamMultiplexer multiplexer = new OutputStreamMultiplexer();
		ITG3205OutputStream out = new ITG3205OutputStream(multiplexer);
		
		for (File d : dirs) {
			File f = new File(d + FILE_SEPARATOR + file);
			multiplexer.add(new FileOutputStream(f));
		}
		return out;
	}
	
	public MS5611OutputStream openMS5611OutputStream() throws FileNotFoundException {
		String file = settings.devices.ms5611.logFile;
		if (file == null) {
			throw new RuntimeException("MS5611 logFile not defined.");
		}
		
		OutputStreamMultiplexer multiplexer = new OutputStreamMultiplexer();
		MS5611OutputStream out = new MS5611OutputStream(multiplexer);
		
		for (File d : dirs) {
			File f = new File(d + FILE_SEPARATOR + file);
			multiplexer.add(new FileOutputStream(f));
		}
		return out;
	}
	
	public ADS1115OutputStream openADS1115OutputStream() throws FileNotFoundException {
		String file = settings.devices.ads1115.logFile;
		if (file == null) {
			throw new RuntimeException("ADS1115 logFile not defined.");
		}
		
		OutputStreamMultiplexer multiplexer = new OutputStreamMultiplexer();
		ADS1115OutputStream out = new ADS1115OutputStream(multiplexer);
		
		for (File d : dirs) {
			File f = new File(d + FILE_SEPARATOR + file);
			multiplexer.add(new FileOutputStream(f));
		}
		return out;
	}
	
	public OutputStreamMultiplexer openGPSOutputStream() throws FileNotFoundException {
		String file = settings.devices.gps.logFile;
		if (file == null) {
			throw new RuntimeException("GPS logFile not defined.");
		}
		
		OutputStreamMultiplexer multiplexer = new OutputStreamMultiplexer();
		for (File d : dirs) {
			File f = new File(d + FILE_SEPARATOR + file);
			multiplexer.add(new FileOutputStream(f));
		}
		return multiplexer;
	}
	
}
