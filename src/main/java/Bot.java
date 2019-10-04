
import Dictionary.IssueClassification;
import Dictionary.IssueStatus;
import Dictionary.IssueType;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateStringConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Bot extends Application {
    public static TextArea console = new TextArea();
    public static Button startButton = new Button("Start Bot");
    public static Button stopButton = new Button("Stop");
    public static ProgressIndicator progressIndicator = new ProgressIndicator();
    public static ComboBox<String> issueTypeDropDown = new ComboBox<String>();
    public static ComboBox<String> classificationDropDown = new ComboBox<String>();
    public static ComboBox<String> statusDropDown = new ComboBox<String>();
    public static DatePicker startDate = new DatePicker();
    public static DatePicker endDate = new DatePicker(LocalDate.of(2019, 1, 1));

    private Controller c;
    public static boolean logFlag = true;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws ParseException {
        SimpleDateFormat datetime = new SimpleDateFormat("MMM dd yyyy hh:mm a");

        Label issueTypeLabel = new Label();
        issueTypeLabel.setText("Issue type:");
        startDate.setEditable(false);
        endDate.setEditable(false);
        Label labelDateStart = new Label();
        Label labelDateEnd = new Label();
        Label labelStatus = new Label();
        Label labelClassification = new Label();
        labelDateStart.setText("Start date:");
        labelDateEnd.setText("End date:");
        labelStatus.setText("Status:");
        labelClassification.setText("Classification:");

        for (IssueType i : IssueType.values()) {
            issueTypeDropDown.getItems().add(i.getName());
        }

        for (IssueClassification i : IssueClassification.values()) {
            classificationDropDown.getItems().add(i.getName());
        }

        for (IssueStatus i : IssueStatus.values()) {
            statusDropDown.getItems().add(i.getName());
        }

        issueTypeDropDown.setValue(IssueType.WNOG.getName());
        classificationDropDown.setValue(IssueClassification.ALL.getName());
        statusDropDown.setValue(IssueStatus.ALL.getName());

        startDate.setValue(LocalDate.of(2014, 1, 1));

        console.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                console.setScrollTop(Double.MAX_VALUE);
            }
        });

        labelDateStart.setPrefSize(100, 20);
        labelDateEnd.setPrefSize(100, 20);
        labelStatus.setPrefSize(150, 20);
        labelClassification.setPrefSize(100, 20);
        issueTypeLabel.setPrefSize(80, 20);
        issueTypeDropDown.setPrefSize(80, 20);

        startDate.setPrefSize(100, 20);
        endDate.setPrefSize(100, 20);
        statusDropDown.setPrefSize(150, 20);
        classificationDropDown.setPrefSize(100, 20);

        labelDateStart.setLayoutX(250.0);
        labelDateStart.setLayoutY(5.0);
        labelDateEnd.setLayoutX(375.0);
        labelDateEnd.setLayoutY(5.0);

        issueTypeLabel.setLayoutX(250.0);
        issueTypeLabel.setLayoutY(60.0);
        labelStatus.setLayoutX(350.0);
        labelStatus.setLayoutY(60.0);
        labelClassification.setLayoutX(525.0);
        labelClassification.setLayoutY(60.0);

        startDate.setLayoutX(250.0);
        startDate.setLayoutY(30.0);
        endDate.setLayoutX(375.0);
        endDate.setLayoutY(30.0);

        issueTypeDropDown.setLayoutX(250.0);
        issueTypeDropDown.setLayoutY(85.0);
        statusDropDown.setLayoutX(350.0);
        statusDropDown.setLayoutY(85.0);
        classificationDropDown.setLayoutX(525.0);
        classificationDropDown.setLayoutY(85.0);

        startButton.setLayoutX(5.0);
        startButton.setLayoutY(20.0);
        startButton.setPrefSize(100, 40);

        stopButton.setPrefSize(100, 40);
        stopButton.setDisable(true);
        stopButton.setLayoutX(115.0);
        stopButton.setLayoutY(20.0);

        console.setEditable(false);
        console.setMinSize(800.0, 350.0);
        console.setMaxSize(800.0, 350.0);
        console.setLayoutX(5);
        console.setLayoutY(130);
        console.setText("Press Button \"Start Bot\"");

        progressIndicator.setLayoutX(780);
        progressIndicator.setLayoutY(30);
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(20.0, 20.0);

        Group root = new Group();
        //root.setDisable(true);
        root.getChildren().addAll(
                startButton,
                stopButton,
                progressIndicator,
                labelDateStart,
                labelDateEnd,
                labelStatus,
                labelClassification,
                issueTypeLabel,
                issueTypeDropDown,
                statusDropDown,
                classificationDropDown,
                startDate,
                endDate,
                console);

        Scene scene = new Scene(root, 810, 500);

        primaryStage.setTitle("Bot Destroyer Application");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        Bot.startButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                progressIndicator.setVisible(true);

                c = new Controller(
                        issueTypeDropDown.getValue(),
                        startDate.getValue(),
                        endDate.getValue(),
                        statusDropDown.getValue(),
                        classificationDropDown.getValue()
                       );

                c.start();
                c.startBot();
                logFlag = true;
                Bot.startButton.setDisable(true);
                Bot.stopButton.setDisable(false);
                issueTypeDropDown.setDisable(true);
                classificationDropDown.setDisable(true);
                statusDropDown.setDisable(true);
                startDate.setDisable(true);
                endDate.setDisable(true);
            }
        });

        Bot.stopButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                c.traces.writeInfo("Stopping...");
                logFlag = false;
                Bot.stopButton.setDisable(true);
                c.stopBot();
                c = null;
            }
        });
    }
}


