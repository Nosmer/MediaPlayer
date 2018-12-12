package javafxradio;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;

//enshures that all views have access to SongModel
public abstract class AbstractView {
    protected final SongModel songModel;
    protected final Node viewNode;
       
    public AbstractView(SongModel songModel){
        this.songModel = songModel;
        this.viewNode = initView();
        }

        public Node getViewNode() {
            return viewNode;
        }       
                       
        protected abstract Node initView();
}
