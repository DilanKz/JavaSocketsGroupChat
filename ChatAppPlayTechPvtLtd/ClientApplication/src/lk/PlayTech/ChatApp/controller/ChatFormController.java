package lk.PlayTech.ChatApp.controller;

import com.jfoenix.controls.JFXButton;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lk.PlayTech.ChatApp.ChatApplication;

public class ChatFormController implements Initializable {

    public TextField txtLogin;
    public Button btnLogin;
    public Pane loginPane;
    public SVGPath btnResizeSvg;
    public SVGPath btnCloseSvg;
    public JFXButton btnResize;
    public JFXButton btnClose;
    public AnchorPane mainPain;
    public Pane navPane;
    public Pane notificationPane;
    public SVGPath svgBell;
    public Label lblNotiCount;
    public Pane emojiPane;
    public Button btnEmoji1;
    public Button btnEmoji2;
    public Button btnEmoji3;
    public Button btnEmoji4;
    public Button btnEmoji5;
    public Button btnEmoji6;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ScrollPane msgBodyPane;

    @FXML
    private VBox msgBox;

    @FXML
    private JFXButton btnFiles;

    @FXML
    private JFXButton btnEmoji;

    @FXML
    private TextField txtMsg;

    @FXML
    private Button btnSend;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String imageFilePath;
    private int notificationCount=0;
    private boolean isMinimized = false;
    private boolean paneVisible= false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setEmojiText();
        setHeight();
        new Thread(() -> {
            try {

                //making the remote socket
                socket = new Socket("localhost", 3000);

                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                dataFilter();

            }catch (IOException ioException){
                System.out.println(ioException.getMessage());
            }
        }).start();
    }

    void setNotificationCount(){
        Platform.runLater(() -> {
            if (notificationCount!=0){
                svgBell.setVisible(true);
                lblNotiCount.setVisible(true);
                lblNotiCount.setText(" "+notificationCount+" ");
            }else {
                lblNotiCount.setVisible(false);
                svgBell.setVisible(false);
                lblNotiCount.setText("");
            }
        });
    }

    void setEmojiText(){
        btnEmoji1.setText("\uD83D\uDE00");
        btnEmoji2.setText("\uD83E\uDD14");
        btnEmoji3.setText("\uD83C\uDF89");
        btnEmoji4.setText("\uD83D\uDE02");
        btnEmoji5.setText("\uD83D\uDE0D");
        btnEmoji6.setText("\uD83C\uDF1F");
    }


    void dataFilter(){
        try {
            while (true) {

                //updating notifications

                if (isMinimized){
                    setNotificationCount();
                }else {
                    svgBell.setVisible(false);
                    lblNotiCount.setVisible(false);
                }

                //reading messages
                //checking the data type
                String dataType = dataInputStream.readUTF();
                if (dataType.equals("IMAGE")) {
                    notificationCount++;
                    receiveImages();
                } else {
                    notificationCount++;
                    receiveTextMessages();
                }
            }
        }catch (IOException ioException){
            System.out.println(ioException.getMessage());
        }
    }

    void setHeight(){
        msgBox.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                msgBodyPane.setVvalue((Double) newValue);
            }
        });
    }


    void receiveTextMessages() {
        try {
            String message = dataInputStream.readUTF();
            addReceiverMsg(message);
        } catch (IOException ex) {
            System.out.println("Error reading the message: " + ex.getMessage());
        }
    }



    void receiveImages() {
        try {
            Random random = new Random();
            String randomNumber = String.valueOf(random.nextInt(1000));


            String projectDir = System.getProperty("user.dir");
            imageFilePath=projectDir+"\\ClientApplication\\src\\lk\\PlayTech\\ChatApp\\data\\images" + randomNumber + ".jpg";

            File receivedImage = new File(imageFilePath);
            boolean isImageReceived = false;

            try (FileOutputStream fileOutputStream = new FileOutputStream(receivedImage)) {
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = dataInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);

                    if (bytesRead > 0) {
                        isImageReceived = true;
                    }

                    if (bytesRead < buffer.length) {
                        break;
                    }

                }

                if (isImageReceived) {
                    System.out.println("Image received");
                    System.out.println(receivedImage.getAbsolutePath());
                    storeAndShowImage(receivedImage.getPath(), receivedImage);
                    imageFilePath = "noPath";
                } else {
                    System.out.println("No image received");
                    imageFilePath = "noPath";
                }
            } catch (IOException ex) {
                System.out.println("Error saving the image: " + ex.getMessage());
            }
        } catch (Exception ex) {
            System.out.println("Error receiving the image: " + ex.getMessage());
        }
    }

    void addReceiverMsg(String msg) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String[] parts=msg.split("`");


                HBox hBox=new HBox();
                hBox.setAlignment(Pos.CENTER_LEFT);
                hBox.setPadding(new Insets(5,5,5,10));


                Text text=new Text(parts[1]);
                TextFlow textFlow=new TextFlow(text);

                textFlow.setStyle("-fx-background-color:"+parts[0]+" ;-fx-background-radius:20px");
                textFlow.setPadding(new Insets(5,10,5,10));
                //text.setFill(Color.color(0.934,0.945,0.996));
                textFlow.setMaxWidth(400);

                hBox.getChildren().add(textFlow);
                msgBox.getChildren().add(hBox);
            }
        });
    }

    void storeAndShowImage(String path, File receivedImage) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ImageView imageView=new ImageView();
                Image image = new Image(receivedImage.toURI().toString());
                System.out.println(receivedImage.toURI().toString());
                imageView.setImage(image);
                imageView.setFitWidth(150);
                imageView.setFitHeight(150);

                HBox hBox=new HBox();

                imageView.setLayoutX(5);
                imageView.setLayoutY(5);

                hBox.setAlignment(Pos.CENTER_LEFT);
                hBox.setPadding(new Insets(5,5,10,0));
                hBox.getChildren().add(imageView);
                msgBox.getChildren().add(hBox);

                imageFilePath="noPath";
            }
        });
    }

    @FXML
    void btnEmojiOnClick(MouseEvent event) {
        paneVisible = !paneVisible;
        emojiPane.setVisible(paneVisible);
    }

    @FXML
    void btnFilesOnClick(MouseEvent event) throws Exception{
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        File file = fileChooser.showOpenDialog(null);
        ImageView imageView=new ImageView();

        if (file != null) {


            FileInputStream fileInputStream = new FileInputStream(file);

            dataOutputStream.writeUTF("IMAGE");
            dataOutputStream.flush();

            byte[] buffer = new byte[4096];
            int bytesRead;

            // Send the image data to the server
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytesRead);
            }

            dataOutputStream.flush();


            Image image = new Image(file.toURI().toString());
            System.out.println(file.toURI().toString());

            imageView.setImage(image);
            imageView.setFitWidth(150);
            imageView.setFitHeight(150);

            HBox hBox=new HBox();

            imageView.setLayoutX(5);
            imageView.setLayoutY(5);

            hBox.setAlignment(Pos.CENTER_RIGHT);
            hBox.setPadding(new Insets(5,5,10,0));
            hBox.getChildren().add(imageView);
            msgBox.getChildren().add(hBox);

        }

    }

    @FXML
    void btnSendOnClick(MouseEvent event) throws Exception {
        if (!txtMsg.getText().isEmpty()){
            String msg=txtMsg.getText();


            // Send the data type
            dataOutputStream.writeUTF("TEXT");
            dataOutputStream.flush();

            // Send the message
            dataOutputStream.writeUTF(msg);
            dataOutputStream.flush();

            if (!msg.isEmpty()){
                HBox hBox=new HBox();
                hBox.setAlignment(Pos.CENTER_RIGHT);
                hBox.setPadding(new Insets(5,5,5,10));


                Text text=new Text(msg);
                TextFlow textFlow=new TextFlow(text);

                textFlow.setStyle("-fx-background-color: #79E0EE;-fx-background-radius:20px");

                textFlow.setPadding(new Insets(5,10,5,10));
                textFlow.setMaxWidth(400);

                hBox.getChildren().add(textFlow);
                msgBox.getChildren().add(hBox);
                txtMsg.clear();
            }
        }else {
            txtMsg.requestFocus();
        }
    }

    @FXML
    void txtMsgOnAction(ActionEvent event) {
        btnSend.fire();
    }

    @FXML
    void txtLoginOnAction(ActionEvent actionEvent) {
        btnLogin.fire();
    }

    @FXML
    void btnLoginOnAction(ActionEvent actionEvent) throws Exception{
        loginPane.setVisible(false);
        dataOutputStream.writeUTF("#AA77FF`"+txtLogin.getText());
        dataOutputStream.flush();
        btnClose.setVisible(true);
        btnCloseSvg.setVisible(true);
        navPane.setVisible(true);
    }

    public void btnCloseOnClicked(MouseEvent mouseEvent) throws IOException {
        socket.close();
        System.exit(0);
    }

    public void btnResizeOnClicked(MouseEvent mouseEvent) {

        String arrowUp="M201.4 137.4c12.5-12.5 32.8-12.5 45.3 0l160 160c12.5 12.5 12.5 32.8 0 45.3s-32.8 12.5-45.3 0L224 205.3 86.6 342.6c-12.5 12.5-32.8 12.5-45.3 0s-12.5-32.8 0-45.3l160-160z";
        String arrowDown="M201.4 342.6c12.5 12.5 32.8 12.5 45.3 0l160-160c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L224 274.7 86.6 137.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3l160 160z";

        if (btnResizeSvg.getContent().equals(arrowUp)){
            //changing the notification count to zero to start over again
            notificationCount=0;
            isMinimized=true;

            //changing the arrow head of the resize button
            btnResizeSvg.setContent(arrowDown);

            //Changing the size of the anchor pane
            ChatApplication.stage.setHeight(120);
            ChatApplication.stage.setWidth(285);
            navPane.setPrefWidth(270);

            //Showing the notification pane
            notificationPane.setVisible(true);

            //Hiding the message body
            msgBodyPane.setVisible(false);


        } else if (btnResizeSvg.getContent().equals(arrowDown)){
            //changing the arrow head of the resize button
            btnResizeSvg.setContent(arrowUp);

            //Changing the size of the anchor pane
            ChatApplication.stage.setHeight(820);
            ChatApplication.stage.setWidth(750);
            navPane.setPrefWidth(737);

            //Hiding the message body
            notificationPane.setVisible(false);

            //Showing the message body
            msgBodyPane.setVisible(true);
        }

    }

    public void btnEmojisOnAction(ActionEvent actionEvent) {
        Button clickedButton = (Button) actionEvent.getSource();
        String buttonText = clickedButton.getText();
        txtMsg.appendText(buttonText);
        emojiPane.setVisible(false);
        paneVisible=false;
    }

    private double xOffset = 0;
    private double yOffset = 0;

    public void onMouseDragged(MouseEvent mouseEvent) {
        Stage primaryStage = (Stage) navPane.getScene().getWindow();
        primaryStage.setX(mouseEvent.getScreenX() - xOffset);
        primaryStage.setY(mouseEvent.getScreenY() - yOffset);

    }

    public void onMousePressed(MouseEvent mouseEvent) {
        xOffset = mouseEvent.getSceneX();
        yOffset = mouseEvent.getSceneY();
    }
}

