package lk.PlayTech.ChatApp.util;

import java.io.*;
import java.net.Socket;

public class MultiClientHandler extends Thread{
    private Socket clientSocket;
    DataOutputStream dataOutputStream;
    String userName;

    public MultiClientHandler(Socket socket,String userName) {
        this.clientSocket = socket;
        this.userName=userName;
    }

    public void run() {
        try {
            dataOutputStream=new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());

            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(System.in));

            while (true){

                String dataType = dataInputStream.readUTF();
                if (dataType.equals("TEXT")){
                    String message =userName+" : "+ dataInputStream.readUTF();

                    if (message.equalsIgnoreCase("CLOSE_CHAT")){
                        break;
                    }

                    Server.broadcastMessage(this.userName,message);
                }else {
                    String imageFilePath = "E:\\IJSE\\IT2\\ChatApp\\ClientSide2\\src\\data\\images\\image.jpg";
                    File receivedImage = new File(imageFilePath);

                    try (FileOutputStream fileOutputStream = new FileOutputStream(receivedImage)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;

                        while ((bytesRead = dataInputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);

                            if (bytesRead < buffer.length) {
                                break;
                            }
                        }
                        Server.broadcastImage(this.userName,receivedImage);

                    } catch (IOException ex) {
                        System.out.println("Error saving the image: " + ex.getMessage());
                    }

                }

            }
            clientSocket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendMessage(String reply) {
        try {
            //Setting the data type to the clients
            dataOutputStream.writeUTF("TEXT");
            dataOutputStream.flush();

            dataOutputStream.writeUTF(reply);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendImages(File file){
        try {
            //Setting the data type to the clients
            dataOutputStream.writeUTF("IMAGE");
            dataOutputStream.flush();


            FileInputStream fileInputStream = new FileInputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;

            // Send the image data to the server
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytesRead);
            }

            dataOutputStream.flush();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
