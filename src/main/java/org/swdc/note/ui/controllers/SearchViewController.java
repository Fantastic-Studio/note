package org.swdc.note.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.swdc.fx.FXController;
import org.swdc.fx.anno.Aware;
import org.swdc.note.core.entities.Article;
import org.swdc.note.core.service.ArticleService;
import org.swdc.note.ui.view.cells.ArticleSimpleCell;
import org.swdc.note.ui.view.cells.ArticleSimpleListCell;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SearchViewController extends FXController {

    @FXML
    private TextField txtKeyword;

    @FXML
    private ListView<Article> resultList;

    @Aware
    private ArticleService articleService = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        resultList.setCellFactory(view->new ArticleSimpleListCell(findView(ArticleSimpleCell.class)));
    }

    @FXML
    public void onSearch() {
        List<Article> result = articleService.searchByTitle(txtKeyword.getText());
        resultList.getItems().clear();
        resultList.getItems().addAll(result);
    }

    public void search(String text) {
        txtKeyword.setText(text);
        onSearch();
    }

    public void clear() {
        resultList.getItems().clear();
    }

}
