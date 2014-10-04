package client.main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import client.io.DatagramClient;
import client.math.MathHelper;
import edu.sdsu.rocket.data.models.Sensors;
import eu.hansolo.enzo.gauge.Gauge;
import eu.hansolo.enzo.gauge.GaugeBuilder;

@SuppressWarnings("deprecation")
public class MainController {
	
	private static final String CONNECT    = "Connect";
	private static final String DISCONNECT = "Disconnect";
	
	@FXML private TextField hostTextField;
	@FXML private Button connectButton;
	@FXML private Slider frequencySlider;
	@FXML private Label frequencyLabel;
	@FXML private FlowPane gaugePane;
	
	private DatagramClient client = new DatagramClient();
	private Gauge radial;
	
	/**
	 * Constructor for the controller.
	 * 
	 * Called prior to the initialize() method.
	 */
	public MainController() {
		client.setListener(new DatagramClient.ClientListener() {
			@Override
			public void onSensorData(final Sensors sensors) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						float a = MathHelper.translate(sensors.analog[0] / 1000f, 0f, 3.3f, 0f, 600f);
						radial.setValue(a);
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
		frequencySlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            	int value = newValue.intValue();
            	frequencyLabel.setText(value + " Hz");
            	if (value == 0) {
            		client.pause();
            	} else {
            		client.setFrequency(value);
            		client.resume();
            	}
            }
        });

		radial = GaugeBuilder.create()
				.prefWidth(300).prefHeight(300)
				.title("LOX")
				.unit("PSI")
				.minValue(0)
				.maxValue(600)
				.majorTickSpace(100)
				.minorTickSpace(10)
				.minMeasuredValueVisible(true)
				.maxMeasuredValueVisible(true)
				.animated(false)
				.decimals(1)
				.build();
		gaugePane.getChildren().add(radial);
	}
	
	@FXML
	private void onConnect(ActionEvent event) {
		if (CONNECT.equals(connectButton.getText())) {
			try {
				InetAddress addr = InetAddress.getByName(hostTextField.getText());
				client.setRemoteAddress(new InetSocketAddress(addr, 4444));
				client.start();
				if (frequencySlider.getValue() != 0) {
					client.setFrequency((float) frequencySlider.getValue());
					client.resume();
				}
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
			client.stop();
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
