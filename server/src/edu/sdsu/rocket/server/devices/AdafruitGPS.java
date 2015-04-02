package edu.sdsu.rocket.server.devices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.provider.PositionProvider;
import edu.sdsu.rocket.models.GPS;
import edu.sdsu.rocket.server.Console;

public class AdafruitGPS implements SentenceListener {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private Writer writer;

	private GPS gps;

	private PositionProvider provider;

	public AdafruitGPS(InputStream in) {
		SentenceReader reader = new SentenceReader(in);
		reader.addSentenceListener(this);
		
		provider = new PositionProvider(reader);
		
		reader.start();
	}
	
	public PositionProvider getPositionProvider() {
		return provider;
	}
	
	public void setOutputStream(OutputStream out) {
		writer = out == null ? null : new PrintWriter(out);
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

}
