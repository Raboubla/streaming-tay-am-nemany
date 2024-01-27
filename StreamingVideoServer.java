import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class StreamingVideoServer {
    private static List<String> videoList = new ArrayList<>();

    static {
        // Ajoutez ici toutes les vidéos que le serveur possède
        videoList.add("valiny.mp4");
        videoList.add("bunny.mp4");
        videoList.add("fanampiana.mp3");
        videoList.add("boubla.jpg");
        videoList.add("sisu.mp4");

        // Ajoutez autant de vidéos que nécessaire
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new VideoServerThread(clientSocket)).start();
        }
    }

    public static List<String> getVideoList() {
        return videoList;
    }
}



class VideoServerThread implements Runnable {
    private Socket clientSocket;

    public VideoServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

            // Envoyer la liste des fichiers au client
            out.writeObject(StreamingVideoServer.getVideoList());

            // Attendre la sélection du client
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            String selectedFile = (String) in.readObject();

            // Charger et envoyer le fichier sélectionné
            File file = new File("C:/RENOUVELENA/Socket/videos/" + selectedFile);
            byte[] fileData = Files.readAllBytes(file.toPath());
            out.writeObject(fileData);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
