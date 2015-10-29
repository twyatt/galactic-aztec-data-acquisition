package client;

import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import client.main.MainController;

public class Launcher extends Application {

	private static final String NAME = "Data Acquisition Client";

	@Override
	public void start(Stage stage) throws Exception {
		// http://stackoverflow.com/a/19603055
		URL resource = getClass().getResource("/MainPane.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		Parent root = loader.load();
		
		final MainController controller = loader.getController();
		Scene scene = new Scene(root);
		
		stage.setWidth(1280);
		stage.setHeight(800);
		stage.setTitle(NAME);
		stage.setScene(scene);
		stage.show();
		
		scene.getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if (controller.requestQuit()) {
					Platform.exit();
					System.exit(0);
				} else {
					event.consume();
				}
			}
		});
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}

}
