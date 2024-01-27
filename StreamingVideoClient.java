import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.application.Platform;



public class StreamingVideoClient extends Application {

    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Slider progressBar;
    private Stage primaryStage;
    private Stage videoStage;

    private Service<Void> videoService;

    private ExecutorService executorService;
    private Socket socket;
    private ObjectInputStream in;




    public static void main(String[] args) {
        try {
            launch(args);    
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        
    }
    private void openMP3Window(byte[] mp3Data) {
        MP3Player mp3Player = new MP3Player(mp3Data, primaryStage);
    }
    
    private void openVideoWindow(byte[] videoData) {
        VideoWindow videoWindow = new VideoWindow(videoData, primaryStage);
    }
    
    private void openImageWindow(byte[] imageData) {
        ImageWindow imageWindow = new ImageWindow(imageData);
    }
    
    
    


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
    
        try {
            socket = new Socket("localhost", 12345);
            in = new ObjectInputStream(socket.getInputStream());
         
    
            List<String> videoList = (List<String>) in.readObject();
            System.out.println("Liste des vidéos reçue du serveur.");
    
            ListView<String> listView = new ListView<>();
            ObservableList<String> observableList = FXCollections.observableArrayList(videoList);
            listView.setItems(observableList);
    
            listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    if (videoService != null) {
                        videoService.cancel(); // Annuler le service actuel s'il existe
                    }
                    createVideoService(newValue); // Créer un nouveau service pour le nouveau fichier sélectionné
                }
            });
    
            VBox root = new VBox();
            root.setSpacing(10);
            root.setPadding(new Insets(10));

            Button refreshButton = new Button("Rafraîchir");
            refreshButton.setOnAction(event -> restartApplication());
            root.getChildren().addAll(listView, refreshButton);

           
    
            Scene scene = new Scene(root, 307, 250);
            primaryStage.setTitle("Video Player");
            primaryStage.setScene(scene);
            primaryStage.show();
    
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    


    private void createVideoService(String selectedFile) {
        if (videoService != null) {
            videoService.cancel();
        }
    
        videoService = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(selectedFile);
                        out.flush();
                        out.reset(); // Réinitialiser le flux de sortie
    
                        byte[] fileData = (byte[]) in.readObject();
    
                        Platform.runLater(() -> {
                            openMediaWindow(selectedFile, fileData);
                        });
    
                        return null;
                    }
                };
            }
        };
    
        videoService.start();
    }
    
    

    
    
        // Méthode pour redémarrer l'application
        private void restartApplication() {
            Platform.runLater(() -> {
                try {
                    Stage stage = new Stage();
                    new StreamingVideoClient().start(stage);
                    primaryStage.close(); // Ferme la fenêtre principale actuelle
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        


    private void openMediaWindow(String selectedFile, byte[] fileData) {
        if (selectedFile.endsWith(".mp4")) {
            openVideoWindow(fileData);
        } else if (selectedFile.endsWith(".mp3")) {
            openMP3Window(fileData);
        } else if (selectedFile.endsWith(".jpg") || selectedFile.endsWith(".png")) {
            openImageWindow(fileData);
        }
    }


private void playMP3(byte[] mp3Data) {
    try {
        File tempFile = File.createTempFile("temp", ".mp3");
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(mp3Data);
        fos.close();

        Media media = new Media(tempFile.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);

        // Créer une nouvelle fenêtre pour afficher le lecteur MP3
        Stage mp3Stage = new Stage();
        VBox mp3Root = new VBox();
        MediaView mediaView = new MediaView(mediaPlayer);
        mediaView.setFitWidth(300);
        mediaView.setFitHeight(150);

        // Bouton Play
        Button playButton = new Button("Play");
        playButton.setOnAction(event -> mediaPlayer.play());

        // Bouton Pause
        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(event -> mediaPlayer.pause());

        // Bouton Stop
        Button stopButton = new Button("Stop");
        stopButton.setOnAction(event -> {
            mediaPlayer.stop();
            mp3Stage.close(); // Fermer la fenêtre MP3
            primaryStage.show(); // Afficher à nouveau la fenêtre principale pour choisir un autre fichier
        });

        HBox controls = new HBox(playButton, pauseButton, stopButton);
        controls.setSpacing(10);

        mp3Root.getChildren().addAll(mediaView, controls);
        mp3Stage.setScene(new Scene(mp3Root, 300, 50));
        mp3Stage.setTitle("MP3 Player");
        mp3Stage.show();

    } catch (IOException e) {
        e.printStackTrace();
    }
}




    private void displayImage(byte[] imageData) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            Image image = new Image(inputStream);
            ImageView imageView = new ImageView(image);

            // Créez une nouvelle fenêtre pour afficher l'image
            Stage imageStage = new Stage();
            imageStage.setTitle("Image Viewer");
            imageStage.setScene(new Scene(new VBox(imageView), 400, 300));
            imageStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void playVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    private void pauseVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    private void createVideoWindow() {
        // Créer une nouvelle fenêtre pour afficher la vidéo
        VBox videoRoot = new VBox();
        MediaView smallMediaView = new MediaView(mediaPlayer);
        smallMediaView.setFitWidth(300);
        smallMediaView.setFitHeight(150);
        videoRoot.getChildren().add(smallMediaView);
    
        // Ajouter la barre de progression à l'interface utilisateur
        HBox controls = new HBox();
        controls.setSpacing(10);
    
        Button playButton = new Button("Play");
        playButton.setOnAction(event -> playVideo());
        controls.getChildren().add(playButton);
    
        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(event -> pauseVideo());
        controls.getChildren().add(pauseButton);
    
        Slider progressBar = new Slider();
        progressBar.setMin(0);
        progressBar.setMax(100);
        progressBar.setValue(0);
        progressBar.valueProperty().addListener((observable, oldValue, newValue) ->
                mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(newValue.doubleValue() / 100)));
    
        // Ajouter le bouton "Fin"
        Button stopButton = new Button("Fin");
        stopButton.setOnAction(event -> stopVideo());
        controls.getChildren().add(stopButton);
    
        videoRoot.getChildren().addAll(controls, progressBar);
    
        Scene videoScene = new Scene(videoRoot, 300, 250);
        Stage videoStage = new Stage();
        videoStage.setTitle("Video Player");
        videoStage.setScene(videoScene);
        videoStage.show();
    }
    
    private void stopVideo() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                // Fermer la fenêtre vidéo
                if (videoStage != null) {
                    videoStage.close();
                    videoStage = null; // Réinitialiser la référence videoStage à null
                }
                primaryStage.show(); // Afficher à nouveau la fenêtre principale pour permettre la sélection d'une nouvelle vidéo
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void stopMedia() {
        try {
            Platform.runLater(() -> {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}






// & "C:\Program Files\Java\jdk-11\bin\javac" MonFichierJava.java

// & "C:\Program Files\Java\jdk-11\bin\java" MonFichierJava

// & "C:\Program Files\Java\jdk-11\bin\javac" --module-path "C:\Program Files\Java\javafx-sdk-17.0.10\lib" --add-modules javafx.controls StreamingVideoServer.java


// javac --module-path "C:\Program Files\Java\javafx-sdk-17.0.10\lib" --add-modules javafx.controls javaFx.java


// jar -xf /chemin/vers/votre/installation/javafx-sdk-21.0.1/lib/javafx-controls.jar META-INF/MANIFEST.MF cat META-INF/MANIFEST.MF


// jar -xf C:\Users\MSI\Downloads\openjfx-21.0.1_windows-x64_bin-sdk\javafx-sdk-21.0.1\lib\javafx-controls.jar META-INF/MANIFEST.MF cat META-INF/MANIFEST.MF

//C:\Program Files\Java\javafx-sdk-17.0.10\lib\javafx.swing.jar
//C:\Program Files\Java\javafx-sdk-17.0.10\lib\javafx.graphics.jar
//C:\Program Files\Java\javafx-sdk-17.0.10\lib\javafx.controls.jar
//C:\Program Files\Java\javafx-sdk-17.0.10\lib\javafx.base.jar
//C:\Program Files\Java\javafx-sdk-17.0.10\lib\javafx.media.jar

// C:\Program Files\Java\javafx-sdk-17.0.10\lib


//ny ficompilena sy fiexecutena-----------------------------------------------------------------------------------

// PS C:\RENOUVELENA\Socket> & "C:\Program Files\Java\jdk-11\bin\javac" StreamingVideoServer.java    
// PS C:\RENOUVELENA\Socket> & "C:\Program Files\Java\jdk-11\bin\java" StreamingVideoServer

//& "C:\Program Files\Java\jdk-11\bin\javac" --module-path "C:\Program Files\Java\javafx-sdk-17.0.10\lib" --add-modules javafx.controls StreamingVideoClient.java
//& "C:\Program Files\Java\jdk-11\bin\java" --module-path "C:\Program Files\Java\javafx-sdk-17.0.10\lib" --add-modules javafx.controls,javafx.media StreamingVideoClient