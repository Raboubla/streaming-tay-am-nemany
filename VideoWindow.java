import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.application.Platform;
import java.io.ByteArrayInputStream;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.application.Platform;

public class VideoWindow {
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Stage videoStage;
    private Stage primaryStage;

    public VideoWindow(byte[] videoData, Stage primaryStage) {
        this.primaryStage = primaryStage;
        createVideoWindow(videoData);
    }

    private void createVideoWindow(byte[] videoData) {
        Service<Void> videoService = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            File tempFile = File.createTempFile("video", ".mp4");
                            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                                fos.write(videoData);
                            }

                            Media media = new Media(tempFile.toURI().toString());
                            mediaPlayer = new MediaPlayer(media);
                            mediaView = new MediaView(mediaPlayer);
                            mediaView.setFitWidth(300);
                            mediaView.setFitHeight(150);

                            Platform.runLater(() -> {
                                createVideoStage();
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
            }
        };

        videoService.start();
    }

    private void createVideoStage() {
        VBox videoRoot = new VBox();
        videoRoot.getChildren().add(mediaView);

        HBox controls = new HBox();
        controls.setSpacing(10);

        Button playButton = new Button("Play");
        playButton.setOnAction(event -> mediaPlayer.play());
        controls.getChildren().add(playButton);

        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(event -> mediaPlayer.pause());
        controls.getChildren().add(pauseButton);

        Slider progressBar = new Slider();
        progressBar.setMin(0);
        progressBar.setMax(100);
        progressBar.setValue(0);
        progressBar.valueProperty().addListener((observable, oldValue, newValue) ->
                mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(newValue.doubleValue() / 100)));

        Button stopButton = new Button("Stop");
        stopButton.setOnAction(event -> {
            stopVideo();
            videoStage.close();
        });
        controls.getChildren().add(stopButton);

        videoRoot.getChildren().addAll(controls, progressBar);

        Scene videoScene = new Scene(videoRoot, 300, 250);
        videoStage = new Stage();
        videoStage.setTitle("Video Player");
        videoStage.setScene(videoScene);
        videoStage.show();
    }

    private void stopVideo() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class MP3Player {
    private MediaPlayer mediaPlayer;
    private Stage mp3Stage;
    private Stage primaryStage;

    public MP3Player(byte[] mp3Data, Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            File tempFile = File.createTempFile("temp", ".mp3");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(mp3Data);
            }

            Media media = new Media(tempFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            VBox mp3Root = new VBox();
            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setFitWidth(300);
            mediaView.setFitHeight(150);

            Button playButton = new Button("Play");
            playButton.setOnAction(event -> mediaPlayer.play());

            Button pauseButton = new Button("Pause");
            pauseButton.setOnAction(event -> mediaPlayer.pause());

            Button stopButton = new Button("Stop");
            stopButton.setOnAction(event -> {
                stopMP3(); // Appel de la méthode stopMP3() pour arrêter la lecture du fichier MP3
                mp3Stage.close(); // Fermer la fenêtre MP3
            });

            HBox controls = new HBox(playButton, pauseButton, stopButton);
            controls.setSpacing(10);

            mp3Root.getChildren().addAll(mediaView, controls);

            Scene mp3Scene = new Scene(mp3Root, 300, 200);
            mp3Stage = new Stage();
            mp3Stage.setTitle("MP3 Player");
            mp3Stage.setScene(mp3Scene);
            mp3Stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void stopMP3() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mp3Stage.close();
                primaryStage.show(); // Afficher à nouveau la fenêtre principale après la fermeture de la fenêtre MP3
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}

class ImageWindow {
    private Stage imageStage;

    public ImageWindow(byte[] imageData) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            Image image = new Image(inputStream);
            ImageView imageView = new ImageView(image);

            VBox imageRoot = new VBox();
            imageRoot.getChildren().add(imageView);

            Scene imageScene = new Scene(imageRoot);
            imageStage = new Stage();
            imageStage.setTitle("Image Viewer");
            imageStage.setScene(imageScene);
            imageStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Stage getImageStage() {
        return imageStage;
    }
}