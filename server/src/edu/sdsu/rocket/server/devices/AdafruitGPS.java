package edu.sdsu.rocket.server.devices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import edu.sdsu.rocket.models.GPS;
import edu.sdsu.rocket.server.Console;
import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.provider.PositionProvider;
import net.sf.marineapi.provider.event.PositionEvent;
import net.sf.marineapi.provider.event.ProviderListener;

public class AdafruitGPS implements SentenceListener, ProviderListener<PositionEvent> {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private Writer writer;

	private GPS gps;

	public AdafruitGPS(InputStream in) {
		SentenceReader reader = new SentenceReader(in);
		reader.addSentenceListener(this);
		
		PositionProvider provider = new PositionProvider(reader);
		provider.addListener(this);
		
		reader.start();
	}
	
	public void setOutputStream(OutputStream out) {
		if (out == null) {
			writer = null;
		} else {
			writer = new PrintWriter(out);
		}
	}
	
	public void setGPS(GPS gps) {
		this.gps = gps;
	}
	
	@Override
	public void readingPaused() {
		// TODO Auto-generated method stub
	}

	@Override
	public void readingStarted() {
		// TODO Auto-generated method stub
	}

	@Override
	public void readingStopped() {
		// TODO Auto-generated method stub
	}

	@Override
	public void sentenceRead(SentenceEvent event) {
		if (writer != null) {
			String sentence = event.getSentence().toString();
			try {
				writer.write(sentence + LINE_SEPARATOR);
			} catch (IOException e) {
				Console.error("Failed to write GPS sentence, " + e.getMessage());
//				e.printStackTrace();
			}
		}
	}

	@Override
	public void providerUpdate(PositionEvent event) {
		double latitude  = event.getPosition().getLatitude();
		double longitude = event.getPosition().getLongitude();
		double altitude  = event.getPosition().getAltitude();
		gps.set(latitude, longitude, altitude);
	}

}
