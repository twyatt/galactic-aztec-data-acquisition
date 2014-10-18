package control.io;

import java.net.InetAddress;

import edu.sdsu.rocket.models.Status;

public class SocketClient {

	public interface ClientListener {
		public void onStatus(Status status);
	}
	private ClientListener listener;
	
	public void setListener(ClientListener listener) {
		this.listener = listener;
	}

	public void connect(InetAddress addr, int port) {
		// TODO Auto-generated method stub
		
	}

	public void disconnect() {
		// TODO Auto-generated method stub
		
	}
	
}
