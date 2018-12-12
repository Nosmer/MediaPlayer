package javafxradio;

import java.io.File;
import java.net.URL;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class JavaFXRadio extends Application {
    private final SongModel songModel;
    private MetadataView metaDataView;
    private PlayerControlsView playerControlsView;
    private Button eqBtn;
    private Button playlistBtn;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    public JavaFXRadio(){
        songModel = new SongModel();
    }
    
    @Override
    public void start(Stage primaryStage){
        URL path = getClass().getResource("resources/Ok.mp3");
        URL icon = getClass().getResource("resources/music-player.png");
        songModel.setURL(path.toString());
    
        HBox bottomBox = new HBox();    
        eqBtn = createEQButton(primaryStage);
        playlistBtn = createOpenButton(primaryStage);
    
        metaDataView = new MetadataView(songModel);
        playerControlsView = new PlayerControlsView(songModel);
        bottomBox.getChildren().addAll(playlistBtn,
            playerControlsView.getViewNode(), eqBtn);
        bottomBox.setAlignment(Pos.BOTTOM_CENTER);
    
        final BorderPane root = new BorderPane();
        root.setCenter(metaDataView.getViewNode());
        root.setBottom(bottomBox);
        root.setPadding(new Insets(10));
    
        final Scene scene = new Scene(root, 650, 400);
        initSceneDragAndDrop(scene);
    
        final URL stylesheet = getClass().getResource("playerStyle.css");
        scene.getStylesheets().add(stylesheet.toString());
    
        primaryStage.getIcons().add(new Image(icon.toString()));
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("New Player");
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(e -> Platform.exit());
    
        songModel.getMediaPlayer().play();
    }
    
    private Button createOpenButton(Stage primaryStage) {
        final Button openButton = new Button();        
        openButton.setId("openButton");
        openButton.setOnAction(actionEvent -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Open");
            File song = fc.showOpenDialog(primaryStage.getScene().getWindow());
                        
            if(song != null){
                songModel.setURL(song.toURI().toString());
                songModel.getMediaPlayer().play();
            }
        });
        openButton.setPrefWidth(32);
        openButton.setPrefHeight(32);
        
        return openButton;
    }
    
    private Button createEQButton(Stage primaryStage) {
        final Button button = new Button();
        button.setId("eqButton");
        button.setOnAction(actionEvent -> {
            EqualizerView equalizerView = new EqualizerView(songModel);
            URL icon = getClass().getResource("resources/equalizer.png");
            
            final BorderPane pane = new BorderPane();
            pane.setCenter(equalizerView.getViewNode());
            
            final Scene scene = new Scene(pane, 600, 400);
            final URL stylesheet = getClass().getResource("playerStyle.css");
            scene.getStylesheets().add(stylesheet.toString());
            Stage eqWindow = new Stage();
            
            eqBtn.disableProperty().bind(eqWindow.showingProperty());
            
            eqWindow.setX(primaryStage.getX()+ primaryStage.getWidth());
            eqWindow.setY(primaryStage.getY());
            eqWindow.getIcons().add(new Image(icon.toString()));
            eqWindow.setScene(scene);
            eqWindow.setResizable(false);
            eqWindow.setTitle("Equalizer");
            eqWindow.show();
        });        
        button.setPrefWidth(32);
        button.setPrefHeight(32);
        
        return button;
    }
               
    private void initSceneDragAndDrop(Scene scene){
        
        //checks if drag content is either a file or url
        scene.setOnDragOver(new EventHandler<DragEvent>(){
            @Override
            public void handle(DragEvent event){
                Dragboard db = event.getDragboard();
                if(db.hasFiles() || db.hasUrl())
                    event.acceptTransferModes(TransferMode.ANY);
                event.consume();
            }
        });
        
        //gets the url of the first file in the list of dropped files
        scene.setOnDragDropped(new EventHandler<DragEvent>(){
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                String uri = null;
                
                if(db.hasFiles()){
                    uri = db.getFiles().get(0).toURI().toString();
                } else if (db.hasUrl()){
                    uri = db.getUrl();
                }
                
                if(uri != null){
                    songModel.setURL(uri);
                    songModel.getMediaPlayer().play();
                }
                
                event.setDropCompleted(uri != null);
                event.consume();
            }            
        });
    }
        
}
