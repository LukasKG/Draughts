import java.util.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene; 
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Screen;

/**
 *  This class is used to outsorce the player selection screen.
 * 
 * @author  Lukas Gunthermann
 * @version 1.0
 */
public class SelectionScreen
{
    public static void load(Stage paramStage)
    {
        Stage stage = paramStage;
        
        // Create a new grid pane
        Pane pane = new Pane();
        pane.setBackground(UI.background);
        
        // Inform the player about the game
        Text text = new Text("Welcome to Draughts!\n\nPlease select the players below.");
        text.setStyle("-fx-font-weight: bold");
        text.setWrappingWidth(250);
        text.setTextAlignment(TextAlignment.CENTER);        
        text.setFont(new Font("Ebrima", 28));
        text.setFill(Color.BLACK);
        //text.setFill(Color.rgb(255,252,0));
        text.setStrokeWidth(1); 
        text.setStroke(Color.BLACK); 
        text.setLayoutX(25);
        text.setLayoutY(50);
        pane.getChildren().add(text);
        
        int originX = 50;
        List<String> choices = Arrays.asList("Human", "AI - easy", "AI - medium", "AI - hard", "AI - brutal");
        
        // Player 1
        int originY1 = 220;
        Label label1 = new Label("Player 1 (Black)");
        label1.setFont(new Font("Ebrima", 18));
        label1.setLayoutX(originX);
        label1.setLayoutY(originY1);
        pane.getChildren().add(label1);
        
        TextField name1 = new TextField("Player 1");
        name1.setPrefWidth(200);
        name1.setLayoutX(originX);
        name1.setLayoutY(originY1+35);
        pane.getChildren().add(name1);
        
        ChoiceBox cb1 = new ChoiceBox();
        cb1.getItems().addAll(choices);
        cb1.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener<String>(){
                @Override
                public void changed(ObservableValue<? extends String> observable, //
                        String oldValue, String newValue) {
                    if (newValue != choices.get(0)) 
                    {
                        name1.setText(newValue);
                        name1.setDisable(true);
                    }
                    else
                    {
                        name1.setText("Player 1");
                        name1.setDisable(false);
                    } 
                }
        });
        cb1.setValue(choices.get(0));
        cb1.setLayoutX(originX);
        cb1.setLayoutY(originY1+70);
        pane.getChildren().add(cb1);
        
        // Player 2
        int originY2 = 350;
        Label label2 = new Label("Player 2 (White)");
        label2.setFont(new Font("Ebrima", 18));
        label2.setLayoutX(originX);
        label2.setLayoutY(originY2);
        pane.getChildren().add(label2);
        
        TextField name2 = new TextField("Player 2");
        name2.setPrefWidth(200);
        name2.setLayoutX(originX);
        name2.setLayoutY(originY2+35);
        pane.getChildren().add(name2);
        
        ChoiceBox cb2 = new ChoiceBox();
        cb2.getItems().addAll(choices);
        cb2.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener<String>(){
                @Override
                public void changed(ObservableValue<? extends String> observable, //
                        String oldValue, String newValue) {
                    if (newValue != choices.get(0)) 
                    {
                        name2.setText(newValue);
                        name2.setDisable(true);
                    }
                    else
                    {
                        name2.setText("Player 2");
                        name2.setDisable(false);
                    } 
                }
        });
        cb2.setValue(choices.get(2));
        cb2.setLayoutX(originX);
        cb2.setLayoutY(originY2+70);
        pane.getChildren().add(cb2);
        
        // Create the button to start the game
        Button buttonStart = new Button("Start");
        buttonStart.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent evt) {
                Player p1 = new Player(name1.getText(),cb1.getValue()==choices.get(0),choices.indexOf(cb1.getValue()));
                Player p2 = new Player(name2.getText(),cb2.getValue()==choices.get(0),choices.indexOf(cb2.getValue()));
                UI.startGame(p1,p2);
            }
        });
        buttonStart.setPrefWidth(80);
        buttonStart.setLayoutX(110);
        buttonStart.setLayoutY(490);
        pane.getChildren().add(buttonStart);

        // JavaFX must have a Scene (window content) inside a Stage (window)
        Scene scene = new Scene(pane, 300, 550);
        
        // Show the stage
        stage.setTitle("Draughts");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }
}
