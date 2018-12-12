package javafxradio;

import java.net.URL;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;

class PlayerControlsView extends AbstractView {
    private Node controlPanel;
    private Slider volumeSlider;
    private Slider positionSlider;
    private Label statusLabel;    
    private Label totalDurationLabel;
    private Label currentTimeLabel;
    private Image playImg;
    private Image pauseImg;
    private ImageView playPauseIcon;
    private StatusListener statusListener;
    private CurrentTimeListener currentTimeListener;
    
    
    public PlayerControlsView(SongModel songModel){
        super(songModel);

        songModel.mediaPlayerProperty().addListener(new MediaPlayerListener());        
        statusListener = new StatusListener();
        currentTimeListener = new CurrentTimeListener();
        addListenersAndBindings(songModel.getMediaPlayer());
    }
    
    @Override
    protected Node initView(){
        controlPanel = createControlPanel();
        volumeSlider = createVolumeSlider("volumeSlider");
        statusLabel = createLabel("Buffering", "statusDisplay");
        positionSlider = createSlider("positionSlider");        
        totalDurationLabel = createLabel("00:00", "mediaText");
        currentTimeLabel = createLabel("00:00", "mediaText");
        
        final ImageView lowVol = new ImageView();
        lowVol.setId("volumeLow");
        final ImageView highVol = new ImageView();
        highVol.setId("volumeHigh");
        
        positionSlider.valueChangingProperty().addListener(new PositionListener());
                
        final GridPane gp = new GridPane();
        gp.setHgap(3);
        gp.setVgap(3);
        
        final ColumnConstraints spacerCol = new ColumnConstraints(45,90,90);
        final ColumnConstraints middleCol = new ColumnConstraints(350);
                
        gp.getColumnConstraints().addAll(spacerCol,middleCol,
                spacerCol);
        
        GridPane.setHalignment(lowVol, HPos.LEFT);
        GridPane.setHalignment(highVol, HPos.RIGHT);
        GridPane.setValignment(volumeSlider, VPos.TOP);
        GridPane.setHalignment(statusLabel, HPos.RIGHT);
        GridPane.setValignment(statusLabel, VPos.TOP);
        GridPane.setHalignment(currentTimeLabel, HPos.RIGHT);
        
        gp.add(lowVol, 0,0);
        gp.add(highVol, 0,0);
        gp.add(volumeSlider, 0,1);        
        gp.add(controlPanel, 1,0,1,2);
        gp.add(statusLabel, 2,1);
        gp.add(currentTimeLabel, 0,2);
        gp.add(positionSlider, 1,2);
        gp.add(totalDurationLabel, 2,2);
                
        return gp;
    }
    
    //position slider
    private Slider createSlider(String id){
        final Slider slider = new Slider(0.0, 1.0, 0.1);
        slider.setId(id);
        slider.setValue(0);
        
        return slider;
    }
    
    private Slider createVolumeSlider(String id){
        final Slider slider = new Slider(0.0, 1.0, 0.1);
        slider.setId(id);
        
        return slider;
    }
    
    private Label createLabel(String text, String styleClass){
        final Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        
        return label;
    }           
    
    private Button createPlayPauseButton(){
        URL url = getClass().getResource("resources/pause.png");
        pauseImg = new Image(url.toString());
        
        url = getClass().getResource("resources/play.png");
        playImg = new Image(url.toString());
        
        playPauseIcon = new ImageView(playImg);
        
        final Button playPauseButton = new Button(null, playPauseIcon);
        playPauseButton.setId("playPauseButton");
        playPauseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final MediaPlayer player = songModel.getMediaPlayer();
                if(player.getStatus() == MediaPlayer.Status.PLAYING){
                    player.pause();
                } else {
                    player.play();
                }
            }
        });
        
        return playPauseButton;
    }
                   
    private Node createControlPanel(){
        final HBox hbox = new HBox();
        final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
        hbox.setAlignment(Pos.CENTER);
        hbox.setFillHeight(false);
        
        final Button playPauseButton = createPlayPauseButton();
        
        final Button seekStartButton = new Button();
        seekStartButton.setId("seekStartButton");
        seekStartButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                seekAndUpdatePosition(Duration.ZERO);
            }
        });
        
        final Button seekEndButton = new Button();
        seekEndButton.setId("seekEndButton");
        seekEndButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {                
                final Duration totalDuration = mediaPlayer.getTotalDuration();
                final Duration second = Duration.seconds(1);
                seekAndUpdatePosition(totalDuration.subtract(second));
            }
        });
        
        hbox.getChildren().addAll(seekStartButton, playPauseButton, seekEndButton);
        
        return hbox;
    }
    
    private void seekAndUpdatePosition(Duration duration){
        final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
        
        if(mediaPlayer.getStatus() == Status.STOPPED)
            mediaPlayer.pause();
        
        mediaPlayer.seek(duration);
        
        if(mediaPlayer.getStatus() != Status.PLAYING)
            updatePositionSlider(duration);
    }
    
    private void addListenersAndBindings(MediaPlayer mediaPlayer){
        mediaPlayer.statusProperty().addListener(statusListener);
        mediaPlayer.currentTimeProperty().addListener(currentTimeListener);
        mediaPlayer.totalDurationProperty().addListener(new TotalDurationListener());
        
        mediaPlayer.setOnEndOfMedia(new Runnable(){
            @Override
            public void run() {
                statusLabel.setText("STOPPED");
            }            
        });
        
        volumeSlider.valueProperty().bindBidirectional
            (songModel.getMediaPlayer().volumeProperty());
        volumeSlider.valueProperty().setValue(0.5);
    }
    
    public void removeListenersAndBinidngs(MediaPlayer mediaPlayer){
        volumeSlider.valueProperty().unbind();
        mediaPlayer.statusProperty().removeListener(statusListener);
        mediaPlayer.currentTimeProperty().removeListener(currentTimeListener);
    }
    
    //creates new listeners and deletes old ones whenever you start using another file
    private class MediaPlayerListener implements ChangeListener<MediaPlayer>{
        @Override
        public void changed(ObservableValue<? extends MediaPlayer> observable,
                MediaPlayer oldValue, MediaPlayer newValue) {
            if(oldValue != null){
                    removeListenersAndBinidngs(oldValue);
                }
            addListenersAndBindings(newValue);
        }        
    }
    //calls out to updateStatus whenever the property status changes
    public class StatusListener implements InvalidationListener{
        @Override
        public void invalidated(Observable observable) {
            Platform.runLater(new Runnable(){
                @Override
                public void run() {
                    updateStatus(songModel.getMediaPlayer().getStatus());
                }            
            });
        }                 
    }

    private void updateStatus(Status newStatus){
            if(newStatus == Status.UNKNOWN || newStatus == null){
                controlPanel.setDisable(true);
                positionSlider.setDisable(true);
                statusLabel.setText("Buffering");
            }else{
                controlPanel.setDisable(false);
                positionSlider.setDisable(false);
                statusLabel.setText(newStatus.toString());
                
                if(newStatus == Status.PLAYING){
                    playPauseIcon.setImage(pauseImg);
                } else {
                    playPauseIcon.setImage(playImg);
                }
            }                
        }
        
    //displays currentTime value
    private class CurrentTimeListener implements InvalidationListener {
        @Override
        public void invalidated(Observable observable) {
            //preforms updates to live scene graph nodes
            Platform.runLater(new Runnable(){
                @Override
                public void run() {
                    final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
                    final Duration currentTime = mediaPlayer.getCurrentTime();
                    currentTimeLabel.setText(formatDuration(currentTime));
                    updatePositionSlider(currentTime);
                }                
            });
        }        
    }
    
    private class TotalDurationListener implements InvalidationListener {
        @Override
        public void invalidated(Observable observable) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
                    final Duration totalDuration = mediaPlayer.getTotalDuration();
                    totalDurationLabel.setText(formatDuration(totalDuration));
                }
            });
        }        
    }
    
    //allows dragging of positionSlider to another position
    private void updatePositionSlider(Duration currentTime){
        if(positionSlider.isValueChanging())
            return;
        
        final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
        final Duration total = mediaPlayer.getTotalDuration();
        
        if(total == null || currentTime == null){
            positionSlider.setValue(0);
        }else{
            positionSlider.setValue(currentTime.toMillis() / total.toMillis());
            updateStatus(songModel.getMediaPlayer().getStatus());
        }
    }
    
    //formatting for currentTime and totalDuration
    private String formatDuration(Duration duration){
        double millis = duration.toMillis();
        int seconds = (int) (millis/1000) %60;
        int minutes = (int) (millis/(1000*60));
        return String.format("%02d:%02d", minutes,seconds);
    }
    
    //waits for the user to stop dragging a slider
    private class PositionListener implements ChangeListener<Boolean>{
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, 
                Boolean oldValue, Boolean newValue) {
            if(oldValue && !newValue){
                double pos = positionSlider.getValue();
                final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
                final Duration seekTo = mediaPlayer.getTotalDuration().multiply(pos);
                seekAndUpdatePosition(seekTo);
            }
        }        
    }
}
