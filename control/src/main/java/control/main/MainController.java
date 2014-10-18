package control.main;

import java.io.IOException;
import java.net.InetAddress;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import control.io.SocketClient;
import edu.sdsu.rocket.models.Status;

@SuppressWarnings("deprecation")
public class MainController {
	
	private static final int PORT = 4444;
	
	private static final String CONNECT    = "Connect";
	private static final String DISCONNECT = "Disconnect";
	
	@FXML private TextField hostTextField;
	@FXML private Button connectButton;
	
	private SocketClient client = new SocketClient();
	
	/**
	 * Constructor for the controller.
	 * 
	 * Called prior to the initialize() method.
	 */
	public MainController() {
		client.setListener(new SocketClient.ClientListener() {
			@Override
			public void onStatus(final Status status) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						updateStatus(status);
					}
				});
			}
		});
	}
	
	/**
	 * Initialize the controller.
	 * 
	 * Automatically called after the FXML view has been loaded.
	 * 
	 * Configures the child Life Line controller/view.
	 */
	@FXML
	private void initialize() {
		createStatus();
	}

	private void createStatus() {
		
	}
	
	public void updateStatus(Status status) {
		
	}
	
	@FXML
	private void onConnect(ActionEvent event) {
		if (CONNECT.equals(connectButton.getText())) {
			try {
				InetAddress addr = InetAddress.getByName(hostTextField.getText());
				client.connect(addr, PORT);
				connectButton.setText(DISCONNECT);
			} catch (IOException e) {
				e.printStackTrace();
				Dialogs.create()
					.title("Connect")
					.masthead("Failed to connect.")
					.message(e.getMessage())
					.showException(e);
			}
		} else {
			client.disconnect();
			connectButton.setText(CONNECT);
		}
		
		event.consume();
	}

	/**
	 * Determines if a shutdown (quit) process should commence.
	 * 
	 * @return
	 */
	public boolean requestQuit() {
		Action response = Dialogs.create()
			.title("Quit")
			.masthead("Are you sure you want to quit?")
			.actions(Dialog.ACTION_CANCEL, Dialog.ACTION_YES)
			.showConfirm();
		return response == Dialog.ACTION_YES;
	}

}
