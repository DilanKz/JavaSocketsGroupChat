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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

public class ChatFormController {

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

    Socket socket;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    String imageFilePath;

    @FXML
    void initialize() {
        setHeight();
        new Thread(() -> {
            createSocket();
        });
    }

    void createSocket(){
        try {

            socket = new Socket("localhost", 3000);

            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            dataFilter();

        }catch (IOException ioException){
            System.out.println(ioException.getMessage());
        }
    }

    void dataFilter(){
        try {
            while (true) {
                String dataType = dataInputStream.readUTF();
                if (dataType.equals("IMAGE")) {
                    receiveImages();
                } else {
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
            imageFilePath=projectDir+"ClientApplication\\src\\lk\\PlayTech\\ChatApp\\data\\images" + randomNumber + ".jpg";

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
    }

    @FXML
    void txtMsgOnAction(ActionEvent event) {
        btnSend.fire();
    }
}

