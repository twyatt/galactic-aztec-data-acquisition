package edu.sdsu.rocket.log2csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVWriter;
import edu.sdsu.rocket.log2csv.ADS1115InputStream.ADS1115Reading;
import edu.sdsu.rocket.log2csv.ADXL345InputStream.ADXL345Reading;
import edu.sdsu.rocket.log2csv.MS5611InputStream.MS5611Reading;

public class Converter {

	private File location;

	public Converter(File location) {
		this.location = location;
	}
	
	public void convert() {
		System.out.print("Converting ADS1115 ... ");
		try {
			convertADS1115();
			System.out.println("Done");
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		System.out.print("Converting ADXL345 ... ");
		try {
			convertADXL345();
			System.out.println("Done");
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		System.out.print("Converting MS5611 ... ");
		try {
			convertMS5611();
			System.out.println("Done");
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public void convertADS1115() throws IOException {
		String name = "ads1115";
		ADS1115InputStream in = new ADS1115InputStream(new FileInputStream(location + File.separator + name + ".log"));
		CSVWriter writer = new CSVWriter(new FileWriter(location + File.separator + name + ".csv"));
		
		writer.writeNext("Timestamp (ns)", "A0 (mV)", "A1 (mV)", "A2 (mV)", "A3 (mV)");
		try {
			ADS1115Reading reading;
			while ((reading = in.readReading()) != null) {
				String[] entries = new String[5];
				entries[0] = String.valueOf(reading.timestamp);
				entries[1] = String.valueOf(reading.values[0]); // a0
				entries[2] = String.valueOf(reading.values[1]); // a1
				entries[3] = String.valueOf(reading.values[2]); // a2
				entries[4] = String.valueOf(reading.values[3]); // a3
				writer.writeNext(entries);
			}
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				System.err.println("Failed to close " + name);
			}
			writer.close();
		}
	}
	
	public void convertADXL345() throws IOException {
		String name = "adxl345";
		ADXL345InputStream in = new ADXL345InputStream(new FileInputStream(location + File.separator + name + ".log"));
		CSVWriter writer = new CSVWriter(new FileWriter(location + File.separator + name + ".csv"));
		
		writer.writeNext("Timestamp (ns)", "Scaling Factor", "X (G)", "Y (G)", "Z (G)");
		try {
			ADXL345Reading reading;
			while ((reading = in.readReading()) != null) {
				String[] entries = new String[5];
				entries[0] = String.valueOf(reading.timestamp);
				entries[1] = String.valueOf(reading.scalingFactor);
				entries[2] = String.valueOf(reading.values[0]); // x
				entries[3] = String.valueOf(reading.values[1]); // y
				entries[4] = String.valueOf(reading.values[2]); // z
				writer.writeNext(entries);
			}
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				System.err.println("Failed to close " + name);
			}
			writer.close();
		}
	}
	
	public void convertMS5611() throws IOException {
		String name = "ms5611";
		MS5611InputStream in = new MS5611InputStream(new FileInputStream(location + File.separator + name + ".log"));
		CSVWriter writer = new CSVWriter(new FileWriter(location + File.separator + name + ".csv"));
		
		writer.writeNext("Timestamp (ns)", "Temperature (C)", "Pressure (mbar)");
		try {
			MS5611Reading reading;
			while ((reading = in.readReading()) != null) {
				String[] entries = new String[3];
				entries[0] = String.valueOf(reading.timestamp);
				entries[1] = String.valueOf((float) reading.values[0] / 100f); // C * 100
				entries[2] = String.valueOf((float) reading.values[1] / 100f); // mbar * 100
				writer.writeNext(entries);
			}
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				System.err.println("Failed to close " + name);
			}
			writer.close();
		}
	}
	
}
