package javafxradio;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.media.EqualizerBand;
import javafx.scene.media.MediaPlayer;

public class EqualizerView extends AbstractView{
    private static final double START_FREQ = 250.0;
    private static final int BAND_COUNT = 7;
    
    private SpectrumBar[] spectrumBars;
    private SpectrumListener spectrumListener;
    
    public EqualizerView(SongModel songModel){
        super(songModel);
        
        songModel.mediaPlayerProperty().addListener(new MediaPlayerListener());
        createEQInterface();
        
        getViewNode().sceneProperty().addListener(new ChangeListener<Scene>(){
            @Override
            public void changed(ObservableValue<? extends Scene> observable,
                    Scene oldValue, Scene newValue) {
                final MediaPlayer mp = EqualizerView.this.songModel.getMediaPlayer();
                if(newValue != null){
                    mp.setAudioSpectrumListener(spectrumListener);
                }else{
                    mp.setAudioSpectrumListener(null);
                }
            }            
        });
    }
    
    @Override
    protected Node initView() {
        final GridPane gp = new GridPane();
        gp.setPadding(new Insets(10));
        gp.setHgap(20);
        
        RowConstraints middle = new RowConstraints();
        RowConstraints outside = new RowConstraints();
        outside.setVgrow(Priority.ALWAYS);
        
        gp.getRowConstraints().addAll(outside, middle, outside);
//        createBackButton(gp);
        return gp;
    }

    private void createEQInterface() {
        final GridPane gp = (GridPane)getViewNode();
        final MediaPlayer mp = songModel.getMediaPlayer();
        
        createEQBands(gp, mp);
        createSpectrumBars(gp, mp);
        spectrumListener = new SpectrumListener(START_FREQ, mp, spectrumBars);
    }
    
    private void createEQBands(GridPane gp, MediaPlayer mp){
        final ObservableList<EqualizerBand> bands = 
                mp.getAudioEqualizer().getBands();
        
        bands.clear();
        
        double min = EqualizerBand.MIN_GAIN;
        double max = EqualizerBand.MAX_GAIN;
        double mid = (max - min)/2;
        double freq = START_FREQ;
        
        //creating equalizer bands
        for (int j = 0; j < BAND_COUNT; j++) {
            //calculating a value between 0 and 2*pi
            double theta = (double)j / (double)(BAND_COUNT-1) * (2*Math.PI);
            
            //use cos function to calculate a scale value between 0 and 0.4
            double scale = 0.4*(1+Math.cos(theta));
            
            //setting gain to be between midpoint and 0.9*max
            double gain = min + mid + (mid*scale);
            
            bands.add(new EqualizerBand(freq, freq/2, gain));
            freq *=2;
        }
        
        //creates a Slider control for each of the created bands
        for (int i = 0; i < bands.size(); i++) {
            EqualizerBand eb = bands.get(i);
            Slider s = createEQSlider(eb, min, max);
            
            final Label l = new Label(formatFrequency(eb.getCenterFrequency()));
            l.getStyleClass().addAll("mediaText", "eqLabel");
            
            GridPane.setHalignment(l, HPos.CENTER);
            GridPane.setHalignment(s, HPos.CENTER);
            GridPane.setHgrow(s, Priority.ALWAYS);
            
            gp.add(l, i, 1);
            gp.add(s, i, 2);
        }
    }
    
    private Slider createEQSlider(EqualizerBand eb, double min, double max){
        final Slider s = new Slider(min, max, eb.getGain());
        s.getStyleClass().add("eqSlider");
        s.setOrientation(Orientation.VERTICAL);
        s.valueProperty().bindBidirectional(eb.gainProperty());
        s.valueProperty().setValue(0.5);
        s.setPrefWidth(44);
        
        return s;
    }
    
    private String formatFrequency(double centerFrequency){
        if(centerFrequency < 1000){
            return String.format("%.0f Hz", centerFrequency);
        } else {
            return String.format("%.1f kHz", centerFrequency/1000);
        }
    }
    
    private void createSpectrumBars(GridPane gp, MediaPlayer mp){
        spectrumBars = new SpectrumBar[BAND_COUNT];
        for (int i = 0; i < spectrumBars.length; i++) {
            spectrumBars[i] = new SpectrumBar(100, 20);
            spectrumBars[i].setMaxWidth(44);
            GridPane.setHalignment(spectrumBars[i], HPos.CENTER);
            gp.add(spectrumBars[i], i, 0);
        }
    }
    
    private class MediaPlayerListener implements ChangeListener<MediaPlayer>{
        @Override
        public void changed(ObservableValue<? extends MediaPlayer> observable, MediaPlayer oldValue, MediaPlayer newValue) {
            if(oldValue != null){
                oldValue.setAudioSpectrumListener(null);
                clearGridPane();                
            }
            createEQInterface();
        }

        private void clearGridPane() {
            for(Node node :((GridPane)getViewNode()).getChildren()){
                GridPane.clearConstraints(viewNode);
            }
            ((GridPane)getViewNode()).getChildren().clear();
        }        
    }
}
