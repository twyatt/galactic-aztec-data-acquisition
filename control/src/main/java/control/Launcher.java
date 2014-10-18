package control;

import java.net.URL;

import control.main.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Launcher extends Application {

	private static final String NAME = "Launch Control Client";

	@Override
	public void start(Stage stage) throws Exception {
		URL resource = MainController.class.getResource("MainPane.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		Parent root = (Parent) loader.load();
		
		final MainController controller = loader.getController();
		Scene scene = new Scene(root);
		
		stage.setWidth(640);
		stage.setHeight(720);
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
