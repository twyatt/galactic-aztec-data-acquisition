package edu.sdsu.rocket.core.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class OutputStreamMultiplexer extends OutputStream {
	
	private final List<OutputStream> streams = new ArrayList<OutputStream>();
	
	public OutputStreamMultiplexer(OutputStream ... streams) {
		for (OutputStream stream : streams) {
			this.streams.add(stream);
		}
	}
	
	public void add(OutputStream stream) {
		streams.add(stream);
	}
	
	public void remove(OutputStream stream) {
		streams.remove(stream);
	}

	@Override
	public void write(int b) throws IOException {
		for (OutputStream stream : streams) {
			stream.write(b);
		}
	}
	
	@Override
	public void flush() throws IOException {
		for (OutputStream stream : streams) {
			stream.flush();
		}
	}
	
	@Override
	public void close() throws IOException {
		for (OutputStream stream : streams) {
			stream.close();
		}
	}

}
