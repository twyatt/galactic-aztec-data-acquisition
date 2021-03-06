package edu.sdsu.rocket.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.badlogic.gdx.utils.Array;

import edu.sdsu.rocket.core.io.ADS1115OutputStream;
import edu.sdsu.rocket.core.io.ADXL345OutputStream;
import edu.sdsu.rocket.core.io.HMC5883LOutputStream;
import edu.sdsu.rocket.core.io.ITG3205OutputStream;
import edu.sdsu.rocket.core.io.MS5611OutputStream;
import edu.sdsu.rocket.core.io.OutputStreamMultiplexer;

public class Logging {
	
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	private ADXL345OutputStream adxl345Log;
	private ITG3205OutputStream itg3205Log;
	private HMC5883LOutputStream hmc5883llog;
	private MS5611OutputStream ms5611Log;
	private ADS1115OutputStream ads1115Log;
	private OutputStreamMultiplexer gpsLog;
	private OutputStreamMultiplexer xtend900log;

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
	
	public ADXL345OutputStream getADXL345OutputStream() throws FileNotFoundException {
		if (adxl345Log == null) {
			adxl345Log = openADXL345OutputStream();
		}
		return adxl345Log;
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
	
	public void closeADXL345OutputStream() throws IOException {
		if (adxl345Log != null) {
			adxl345Log.close();
		}
		adxl345Log = null;
	}
	
	public ITG3205OutputStream getITG3205OutputStream() throws FileNotFoundException {
		if (itg3205Log == null) {
			itg3205Log = openITG3205OutputStream();
		}
		return itg3205Log;
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
	
	public void closeITG3205OutputStream() throws IOException {
		if (itg3205Log != null) {
			itg3205Log.close();
		}
		itg3205Log = null;
	}
	
	public HMC5883LOutputStream getHMC5883LOutputStream() throws FileNotFoundException {
		if (hmc5883llog == null) {
			hmc5883llog = openHMC5883LOutputStream();
		}
		return hmc5883llog;
	}
	
	public HMC5883LOutputStream openHMC5883LOutputStream() throws FileNotFoundException {
		String file = settings.devices.hmc5883l.logFile;
		if (file == null) {
			throw new RuntimeException("HMC5883L logFile not defined.");
		}
		
		OutputStreamMultiplexer multiplexer = new OutputStreamMultiplexer();
		HMC5883LOutputStream out = new HMC5883LOutputStream(multiplexer);
		
		for (File d : dirs) {
			File f = new File(d + FILE_SEPARATOR + file);
			multiplexer.add(new FileOutputStream(f));
		}
		return out;
	}
	
	public void closeHMC5883LOutputStream() throws IOException {
		if (hmc5883llog != null) {
			hmc5883llog.close();
		}
		hmc5883llog = null;
	}
	
	public MS5611OutputStream getMS5611OutputStream() throws FileNotFoundException {
		if (ms5611Log == null) {
			ms5611Log = openMS5611OutputStream();
		}
		return ms5611Log;
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
	
	public void closeMS5611OutputStream() throws IOException {
		if (ms5611Log != null) {
			ms5611Log.close();
		}
		ms5611Log = null;
	}
	
	public ADS1115OutputStream getADS1115OutputStream() throws FileNotFoundException {
		if (ads1115Log == null) {
			ads1115Log = openADS1115OutputStream();
		}
		return ads1115Log;
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
	
	public void closeADS1115OutputStream() throws IOException {
		if (ads1115Log != null) {
			ads1115Log.close();
		}
		ads1115Log = null;
	}
	
	public OutputStream getGPSOutputStream() throws FileNotFoundException {
		if (gpsLog == null) {
			gpsLog = openGPSOutputStream();
		}
		return gpsLog;
	}
	
	public OutputStreamMultiplexer openGPSOutputStream() throws FileNotFoundException {
		String file = settings.devices.gps.logFile;
		if (file == null) {
			throw new RuntimeException("GPS logFile not defined.");
		}
		
		gpsLog = new OutputStreamMultiplexer();
		for (File d : dirs) {
			File f = new File(d + FILE_SEPARATOR + file);
			gpsLog.add(new FileOutputStream(f));
		}
		return gpsLog;
	}
	
	public void closeGPSOutputStream() throws IOException {
		if (gpsLog != null) {
			gpsLog.close();
		}
		gpsLog = null;
	}
	
	public OutputStream getXTend900OutputStream() throws FileNotFoundException {
		if (xtend900log == null) {
			xtend900log = openXTend900OutputStream();
		}
		return xtend900log;
	}
	
	public OutputStreamMultiplexer openXTend900OutputStream() throws FileNotFoundException {
		String file = settings.devices.xtend900.logFile;
		if (file == null) {
			throw new RuntimeException("XTend 900 logFile not defined.");
		}
		
		xtend900log = new OutputStreamMultiplexer();
		for (File d : dirs) {
			File f = new File(d + FILE_SEPARATOR + file);
			xtend900log.add(new FileOutputStream(f));
		}
		return xtend900log;
	}
	
	public void closeXTend900OutputStream() throws IOException {
		if (xtend900log != null) {
			xtend900log.close();
		}
		xtend900log = null;
	}
	
	public void close() {
		try {
			closeADXL345OutputStream();
		} catch (IOException e) {
			System.err.println(e);
		}
		try {
			closeITG3205OutputStream();
		} catch (IOException e) {
			System.err.println(e);
		}
		try {
			closeHMC5883LOutputStream();
		} catch (IOException e) {
			System.err.println(e);
		}
		try {
			closeMS5611OutputStream();
		} catch (IOException e) {
			System.err.println(e);
		}
		try {
			closeADS1115OutputStream();
		} catch (IOException e) {
			System.err.println(e);
		}
		try {
			closeGPSOutputStream();
		} catch (IOException e) {
			System.err.println(e);
		}
		try {
			closeXTend900OutputStream();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}
