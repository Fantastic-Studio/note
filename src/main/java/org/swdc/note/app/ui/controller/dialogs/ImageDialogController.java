package org.swdc.note.app.ui.controller.dialogs;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.*;
import org.springframework.context.event.EventListener;
import org.swdc.note.app.entity.Artle;
import org.swdc.note.app.entity.ArtleContext;
import org.swdc.note.app.event.ArtleEditEvent;
import org.swdc.note.app.service.ArtleService;
import org.swdc.note.app.ui.view.dialogs.ImageDialog;
import org.swdc.note.app.util.UIUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.*;

/**
 *  图片选择窗口
 */
@FXMLController
public class ImageDialogController implements Initializable{

    @Autowired
    private ImageDialog dlg;

    @FXML
    private ListView<String> listView;

    @FXML
    private ScrollPane scrPane;

    @Autowired
    private ArtleService artleService;

    @FXML
    protected void openImage() throws Exception{
        FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("图片",".jpg",".png",".bmp",".gif"));
        File file = chooser.showOpenDialog(dlg.getStage());
        Base64.Encoder enc = Base64.getEncoder();
        if(file!=null&& file.exists()){
            String res = enc.encodeToString(UIUtil.readFile(new FileInputStream(file)));
            if(dlg.getImages().keySet().contains(file.getName())){
                return;
            }
            dlg.getImages().put(file.getName(),res);
            listView.getItems().add(file.getName());
        }
    }

    @FXML
    protected void cancel(){
        if(dlg.getStage().isShowing()){
            dlg.getStage().close();
        }
    }

    @FXML
    protected void removeSelected(){
        Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(name->{
            listView.getItems().remove(name);
            dlg.getImages().remove(name);
        });
    }

    @FXML
    protected void insertSelected(){
        Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(name->{
            dlg.setSelectedImage(name);
            dlg.getStage().close();
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if(newValue!=null){
                Base64.Decoder dec = Base64.getDecoder();
                byte[] data = dec.decode(dlg.getImages().get(newValue));
                Image img = new Image(new ByteArrayInputStream(data));
                scrPane.setContent(new ImageView(img));
            }
        }));
    }

    @EventListener
    public void onArtleEdit(ArtleEditEvent e){
        Artle artle = e.getSource();
        ArtleContext context = artleService.loadContext(artle);
        dlg.setImages(context.getImageRes());
        listView.getItems().clear();
        listView.getItems().addAll(context.getImageRes().keySet());
    }

}