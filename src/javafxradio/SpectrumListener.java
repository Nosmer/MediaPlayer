package javafxradio;

import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.MediaPlayer;

class SpectrumListener implements AudioSpectrumListener{
    private final SpectrumBar[] bars;
    private double minValue;
    private double[] norms;
    private int[] spectrumBucketCount;

    SpectrumListener(double startFreq, MediaPlayer mp, SpectrumBar[] bars) {
        this.bars = bars;
        this.minValue = mp.getAudioSpectrumThreshold();
        this.norms = createNormArray();
        
        int bandCount = mp.getAudioSpectrumNumBands();
        this.spectrumBucketCount = createBucketCount(startFreq, bandCount);
    }    

    @Override
    public void spectrumDataUpdate(double timestamp, double duration,
            float[] magnitudes, float[] phases) {
        int index = 0;
        int bucketIndex = 0;
        int currentBucketCount = 0;
        double sum = 0.0;
        
        //iterate through the array and use the bucket count to sum
        //the magnitudes of the bands that correspond to each equalizer band
        while(index < magnitudes.length){
            sum += magnitudes[index] - minValue;
            ++currentBucketCount;
            
            if(currentBucketCount >= spectrumBucketCount[bucketIndex]){
                bars[bucketIndex].setValue(sum / norms[bucketIndex]);
                currentBucketCount = 0;
                sum = 0.0;
                ++bucketIndex;
            }
            ++index;
        }
    }
    
    //normalizes the magnitude values that are later computed for each eq band
    private double[] createNormArray() {
        double[] normArray = new double[bars.length];
        double currentNorm = 0.05;
        for (int i = 0; i < normArray.length; i++) {
            normArray[i] = 1 + currentNorm;
            currentNorm *= 2;
        }
        
        return normArray;
    }
    
    //figures out how many spectrum bands fall within each of equalizer bands
    private int[] createBucketCount(double startFreq, int bandCount) {
        int[] bucketCount = new int[bars.length];
        
        //assume spectrum data covers frequencies up to 22.05 kHz
        double bandwidth = 22050.0 / bandCount;
        double centerFreq = bandwidth/2;
        double currentSpectrumFreq = centerFreq;
        double currentEQFreq = startFreq / 2;
        double currentCutoff = 0;
        int currentBucketIndex = -1;
        
        for (int i = 0; i < bandCount; i++) {
            if(currentSpectrumFreq > currentCutoff){
                currentEQFreq *= 2;
                currentCutoff = currentEQFreq + currentEQFreq / 2;
                ++currentBucketIndex;
                if(currentBucketIndex == bucketCount.length){
                    break;
                }
            }
            
            ++bucketCount[currentBucketIndex];
            currentSpectrumFreq += bandwidth;
        }
        return bucketCount;
    }
    
}
