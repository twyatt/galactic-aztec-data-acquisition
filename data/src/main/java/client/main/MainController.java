package client.main;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import com.badlogic.gdx.math.Vector3;

import edu.sdsu.rocket.core.io.DatagramClient;
import edu.sdsu.rocket.core.models.Pressures;
import edu.sdsu.rocket.core.models.Sensors;
import eu.hansolo.enzo.common.Section;
import eu.hansolo.enzo.gauge.Gauge;
import eu.hansolo.enzo.gauge.GaugeBuilder;

@SuppressWarnings("deprecation")
public class MainController {
	
	private static final boolean DEBUG_SENSORS = false;
	private static final int PORT = 4444;
	
	private static final String CONNECT    = "Connect";
	private static final String DISCONNECT = "Disconnect";
	
	@FXML private TextField hostTextField;
	@FXML private Button connectButton;
	@FXML private Slider frequencySlider;
	@FXML private Label frequencyLabel;
	@FXML private FlowPane gaugePane;
	
	private DatagramClient client = new DatagramClient();
	private Gauge lox;
	private Gauge kerosene;
	private Gauge helium;
	private Gauge motor;
	
	private int chartIndex;
	private static final int ACCELEROMETER_DATA_POINTS = 50;
	private static final int GYROSCOPE_DATA_POINTS     = 50;
	
	private NumberAxis accelerometerX;
	private Series<Number, Number> accelerometerXData = new XYChart.Series<Number, Number>();
	private Series<Number, Number> accelerometerYData = new XYChart.Series<Number, Number>();
	private Series<Number, Number> accelerometerZData = new XYChart.Series<Number, Number>();
	
	private NumberAxis gyroscopeX;
	private Series<Number, Number> gyroscopeXData = new XYChart.Series<Number, Number>();
	private Series<Number, Number> gyroscopeYData = new XYChart.Series<Number, Number>();
	private Series<Number, Number> gyroscopeZData = new XYChart.Series<Number, Number>();
	
	private static final Vector3 tmpVec = new Vector3();
	
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
						updateSensors(sensors);
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

		createSensors();
	}

	private void createSensors() {
		if (DEBUG_SENSORS) {
			motor    = makePressureGauge("Motor",    "mV", 5000, 100);
			lox      = makePressureGauge("LOX",      "mV", 5000, 100);
			kerosene = makePressureGauge("Kerosene", "mV", 5000, 100);
			helium   = makePressureGauge("Helium",   "mV", 5000, 100);
			
			List<Section> sections = new ArrayList<Section>();
			sections.add(new Section(3300, 3600));
			sections.add(new Section(3600, 5000));
			
			Gauge[] gauges = new Gauge[] { motor, lox, kerosene, helium };
			for (Gauge gauge : gauges) {
				gauge.setSections(sections);
				gauge.setSectionFill0(Color.YELLOW);
				gauge.setSectionFill1(Color.RED);
			}
		} else {
			motor    = makePressureGauge("Motor",    "PSI", Pressures.MOTOR_MAX_PRESSURE,    1);
			lox      = makePressureGauge("LOX",      "PSI", Pressures.LOX_MAX_PRESSURE,      10);
			kerosene = makePressureGauge("Kerosene", "PSI", Pressures.KEROSENE_MAX_PRESSURE, 10);
			helium   = makePressureGauge("Helium",   "PSI", Pressures.HELIUM_MAX_PRESSURE,   50);
		}
		
		accelerometerX = new NumberAxis();
		NumberAxis accelerometerY = new NumberAxis();
		accelerometerX.setAutoRanging(false);
		accelerometerX.setTickLabelsVisible(false);
		accelerometerY.setLabel("Acceleration (m/s^2)");
		accelerometerY.setForceZeroInRange(true);
		LineChart<Number, Number> accelerometer = makeChart("Accelerometer", accelerometerX, accelerometerY);
		accelerometerXData.setName("X");
		accelerometerYData.setName("Y");
		accelerometerZData.setName("Z");
		accelerometer.getData().add(accelerometerXData);
		accelerometer.getData().add(accelerometerYData);
		accelerometer.getData().add(accelerometerZData);
		
		gyroscopeX = new NumberAxis();
		NumberAxis gyroscopeY = new NumberAxis();
		gyroscopeX.setAutoRanging(false);
		gyroscopeX.setTickLabelsVisible(false);
		gyroscopeY.setLabel("Rotation (deg/sec)");
		gyroscopeY.setForceZeroInRange(true);
		LineChart<Number, Number> gyroscope = makeChart("Gyroscope", gyroscopeX, gyroscopeY);
		gyroscopeXData.setName("X");
		gyroscopeYData.setName("Y");
		gyroscopeZData.setName("Z");
		gyroscope.getData().add(gyroscopeXData);
		gyroscope.getData().add(gyroscopeYData);
		gyroscope.getData().add(gyroscopeZData);
		
		gaugePane.getChildren().add(lox);
		gaugePane.getChildren().add(kerosene);
		gaugePane.getChildren().add(helium);
		gaugePane.getChildren().add(motor);
		gaugePane.getChildren().add(accelerometer);
		gaugePane.getChildren().add(gyroscope);
	}

	private LineChart<Number, Number> makeChart(String title, NumberAxis x, NumberAxis y) {
		LineChart<Number, Number> chart = new LineChart<Number, Number>(x, y);
		chart.setTitle(title);
		chart.setCreateSymbols(false);
		chart.setAnimated(false);
		chart.setHorizontalZeroLineVisible(true);
		chart.setLegendSide(Side.RIGHT);
		chart.setPrefWidth(600);
		chart.setPrefHeight(300);
		return chart;
	}

	private Gauge makePressureGauge(String label, String unit, double maxValue, double minorTickSpace) {
		return GaugeBuilder.create()
				.prefWidth(300).prefHeight(300)
				.styleClass("gauge")
				.title(label)
				.unit(unit)
				.minValue(0)
				.maxValue(maxValue)
				.majorTickSpace(minorTickSpace * 10)
				.minorTickSpace(minorTickSpace)
				.minMeasuredValueVisible(true)
				.maxMeasuredValueVisible(true)
				.animated(false)
				.decimals(1)
				.build();
	}
	
	public void updateSensors(Sensors sensors) {
		if (DEBUG_SENSORS) {
			motor.setValue(sensors.analog.getA0());
			lox.setValue(sensors.analog.getA1());
			kerosene.setValue(sensors.analog.getA2());
			helium.setValue(sensors.analog.getA3());
		} else {
			motor.setValue(sensors.pressures.getMotor());
			lox.setValue(sensors.pressures.getLOX());
			kerosene.setValue(sensors.pressures.getKerosene());
			helium.setValue(sensors.pressures.getHelium());
		}
		
		chartIndex++;
		
		Vector3 accelerometer = tmpVec;
		sensors.accelerometer.get(accelerometer);
		accelerometer.scl(9.8f);
		accelerometerX.setLowerBound(chartIndex - ACCELEROMETER_DATA_POINTS + 1);
		accelerometerX.setUpperBound(chartIndex);
		while (accelerometerXData.getData().size() >= ACCELEROMETER_DATA_POINTS) {
			accelerometerXData.getData().remove(0);
		}
		while (accelerometerYData.getData().size() >= ACCELEROMETER_DATA_POINTS) {
			accelerometerYData.getData().remove(0);
		}
		while (accelerometerZData.getData().size() >= ACCELEROMETER_DATA_POINTS) {
			accelerometerZData.getData().remove(0);
		}
		accelerometerXData.getData().add(new XYChart.Data<Number, Number>(chartIndex, accelerometer.x));
		accelerometerYData.getData().add(new XYChart.Data<Number, Number>(chartIndex, accelerometer.y));
		accelerometerZData.getData().add(new XYChart.Data<Number, Number>(chartIndex, accelerometer.z));
		
		Vector3 gyroscope = tmpVec;
		sensors.gyroscope.get(gyroscope);
		gyroscopeX.setLowerBound(chartIndex - GYROSCOPE_DATA_POINTS + 1);
		gyroscopeX.setUpperBound(chartIndex);
		while (gyroscopeXData.getData().size() >= GYROSCOPE_DATA_POINTS) {
			gyroscopeXData.getData().remove(0);
		}
		while (gyroscopeYData.getData().size() >= GYROSCOPE_DATA_POINTS) {
			gyroscopeYData.getData().remove(0);
		}
		while (gyroscopeZData.getData().size() >= GYROSCOPE_DATA_POINTS) {
			gyroscopeZData.getData().remove(0);
		}
		gyroscopeXData.getData().add(new XYChart.Data<Number, Number>(chartIndex, gyroscope.x));
		gyroscopeYData.getData().add(new XYChart.Data<Number, Number>(chartIndex, gyroscope.y));
		gyroscopeZData.getData().add(new XYChart.Data<Number, Number>(chartIndex, gyroscope.z));
	}
	
	@FXML
	private void onConnect(ActionEvent event) {
		if (CONNECT.equals(connectButton.getText())) {
			try {
				InetAddress addr = InetAddress.getByName(hostTextField.getText());
				client.start(addr, PORT);
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
