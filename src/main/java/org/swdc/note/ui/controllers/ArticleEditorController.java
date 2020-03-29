package org.swdc.note.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.fxmisc.richtext.CodeArea;
import org.swdc.fx.FXController;
import org.swdc.fx.anno.Aware;
import org.swdc.note.core.entities.Article;
import org.swdc.note.core.entities.ArticleContent;
import org.swdc.note.core.entities.ArticleResource;
import org.swdc.note.core.entities.ArticleType;
import org.swdc.note.core.service.ArticleService;
import org.swdc.note.ui.component.RectResult;
import org.swdc.note.ui.component.RectSelector;
import org.swdc.note.ui.events.RefreshEvent;
import org.swdc.note.ui.view.ArticleEditorView;
import org.swdc.note.ui.view.EditorContentView;
import org.swdc.note.ui.view.dialogs.ImagesView;
import org.swdc.note.ui.view.dialogs.TypeSelectView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;

import static org.swdc.fx.FXView.fxViewByView;

public class ArticleEditorController extends FXController {

    @FXML
    private TabPane articlesTab;

    @Aware
    private ArticleService articleService = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private void onToolBold() {
        Tab select = articlesTab.getSelectionModel().getSelectedItem();
        if (select == null) {
            return;
        }
        EditorContentView viewEditor = fxViewByView(select.getContent(), EditorContentView.class);
        CodeArea codeArea = viewEditor.getCodeArea();

        String sel = codeArea.getSelectedText();
        IndexRange range = codeArea.getSelection();
        if(sel==null||sel.equals("")){
            // 获取光标位置
            IndexRange rgCurr = new IndexRange(codeArea.getCaretPosition(),codeArea.getCaretPosition());
            // 插入内容
            codeArea.replaceText(rgCurr,"**内容写在这里**");
            return;
        }
        sel = ((ArticleEditorView)getView()).reduceDesc(sel,"**");
        codeArea.replaceText(range.getStart(),range.getEnd(),sel);
    }

    @FXML
    private void onToolItalic() {
        Tab select = articlesTab.getSelectionModel().getSelectedItem();
        if (select == null) {
            return;
        }
        EditorContentView viewEditor = fxViewByView(select.getContent(), EditorContentView.class);
        CodeArea codeArea = viewEditor.getCodeArea();

        String sel = codeArea.getSelectedText();
        IndexRange range = codeArea.getSelection();
        if(sel==null||sel.equals("")){
            IndexRange rgCurr = new IndexRange(codeArea.getCaretPosition(),codeArea.getCaretPosition());
            codeArea.replaceText(rgCurr,"*内容写在这里*");
            return;
        }
        sel = ((ArticleEditorView)getView()).reduceDesc(sel,"*");
        codeArea.replaceText(range.getStart(),range.getEnd(),sel);
    }

    @FXML
    private void onToolImage() {
        Tab select = articlesTab.getSelectionModel().getSelectedItem();
        if (select == null) {
            return;
        }
        EditorContentView viewEditor = fxViewByView(select.getContent(), EditorContentView.class);
        ImagesView imagesView = viewEditor.getImagesView();
        viewEditor.getImagesView().show();
        CodeArea area = viewEditor.getCodeArea();
        Optional.ofNullable(imagesView.getSelectedImageName()).ifPresent(img->{
            IndexRange rgCurr = new IndexRange(area.getCaretPosition(),area.getCaretPosition());
            area.replaceText(rgCurr,"![输入简介]["+img+"]");
        });
        imagesView.clear();
    }

    @FXML
    private void onToolTable() {
        Tab select = articlesTab.getSelectionModel().getSelectedItem();
        if (select == null) {
            return;
        }
        EditorContentView viewEditor =fxViewByView(select.getContent(), EditorContentView.class);
        CodeArea codeArea = viewEditor.getCodeArea();

        ArticleEditorView view = getView();

        Button btnTable = view.findById("table");
        RectSelector rectSelector = view.getTableSelector();
        rectSelector.setX(ArticleEditorView.getScreenX(btnTable));
        rectSelector.setY(ArticleEditorView.getScreenY(btnTable) + btnTable.getHeight());
        RectResult rectResult = rectSelector.showAndWait().orElse(new RectResult());
        String table = "";
        for (int i = 0;i < rectResult.getxCount() + 1; i++){
            for (int j = 0;j < rectResult.getyCount();j++){
                if(i!=1){
                    table = table + "| <内容> ";
                }else {
                    table = table + "|:-----:";
                }
                if(j+1 == rectResult.getyCount()){
                    table = table + "|";
                }
            }
            table = table + "\n";
        }
        IndexRange rgCurr = new IndexRange(codeArea.getCaretPosition(),codeArea.getCaretPosition());
        codeArea.replaceText(rgCurr,"\n\n"+table);
    }

    @FXML
    private void onToolListOl(){
        Tab select = articlesTab.getSelectionModel().getSelectedItem();
        if (select == null) {
            return;
        }
        EditorContentView viewEditor = fxViewByView(select.getContent(), EditorContentView.class);
        CodeArea codeArea = viewEditor.getCodeArea();
        int idx = 1;
        String txt = codeArea.getSelectedText();
        if(txt == null || txt.equals("")){
            IndexRange rgCurr = new IndexRange(codeArea.getCaretPosition(),codeArea.getCaretPosition());
            codeArea.replaceText(rgCurr," 1. 列表项a \n 2. 列表项b \n 3. 列表项c");
            return;
        }
        List<String> list = Arrays.asList(txt.split("\n"));
        StringBuilder sb = new StringBuilder();
        for (String str:list) {
            sb.append("\n " )
                    .append((idx++))
                    .append(". ")
                    .append(str);
        }
        codeArea.replaceText(codeArea.getSelection(),sb.toString());
    }

    @FXML
    private void onToolListUl() {
        Tab select = articlesTab.getSelectionModel().getSelectedItem();
        if (select == null) {
            return;
        }
        EditorContentView viewEditor = fxViewByView(select.getContent(), EditorContentView.class);
        CodeArea codeArea = viewEditor.getCodeArea();
        String txt = codeArea.getSelectedText();
        if(txt == null || txt.equals("")){
            IndexRange rgCurr = new IndexRange(codeArea.getCaretPosition(),codeArea.getCaretPosition());
            codeArea.replaceText(rgCurr," - 列表项a \n - 列表项b \n - 列表项c");
            return;
        }
        List<String> list = Arrays.asList(txt.split("\n"));
        StringBuilder sb = new StringBuilder();
        list.forEach(str->{
            if(str.trim().equals("")){
                return;
            }
            sb.append("\n - " ).append(str);
        });
        codeArea.replaceText(codeArea.getSelection(),sb.toString());
    }

    @FXML
    private void onToolTodo() {
        Tab select = articlesTab.getSelectionModel().getSelectedItem();
        if (select == null) {
            return;
        }
        EditorContentView viewEditor = fxViewByView(select.getContent(), EditorContentView.class);
        CodeArea codeArea = viewEditor.getCodeArea();
        String sel = codeArea.getSelectedText();
        IndexRange range = codeArea.getSelection();
        if (sel == null|| sel.equals("")){
            IndexRange rgCurr = new IndexRange(codeArea.getCaretPosition(),codeArea.getCaretPosition());
            codeArea.replaceText(rgCurr,"\n * [ ] 计划列表");
            return;
        }
        sel = "\n * [ ] "+sel+"\n";
        codeArea.replaceText(range.getStart(),range.getEnd(),sel);
    }

    @FXML
    private void onToolCode() {
        Tab select = articlesTab.getSelectionModel().getSelectedItem();
        if (select == null) {
            return;
        }
        EditorContentView viewEditor = fxViewByView(select.getContent(), EditorContentView.class);
        CodeArea codeArea = viewEditor.getCodeArea();
        String sel = codeArea.getSelectedText();
        IndexRange range = codeArea.getSelection();
        if (sel == null|| sel.equals("")){
            IndexRange rgCurr = new IndexRange(codeArea.getCaretPosition(),codeArea.getCaretPosition());
            codeArea.replaceText(rgCurr,"\n```language\n \\\\ 在这里编写代码 \n ```");
            return;
        }
        sel = "```language\n"+sel+"\n```";
        codeArea.replaceText(range.getStart(),range.getEnd(),sel);
    }

    @FXML
    private void onToolThrow() {
        Tab select = articlesTab.getSelectionModel().getSelectedItem();
        if (select == null) {
            return;
        }
        EditorContentView viewEditor = fxViewByView(select.getContent(), EditorContentView.class);
        CodeArea codeArea = viewEditor.getCodeArea();

        String sel = codeArea.getSelectedText();
        IndexRange range = codeArea.getSelection();
        if(sel==null||sel.equals("")){
            IndexRange rgCurr = new IndexRange(codeArea.getCaretPosition(),codeArea.getCaretPosition());
            codeArea.replaceText(rgCurr,"~~内容写在这里~~");
            return;
        }
        sel = ((ArticleEditorView)getView()).reduceDesc(sel,"~~");
        codeArea.replaceText(range.getStart(),range.getEnd(),sel);
    }

    @FXML
    private void onToolQuite() {
        Tab select = articlesTab.getSelectionModel().getSelectedItem();
        if (select == null) {
            return;
        }
        EditorContentView viewEditor = fxViewByView(select.getContent(), EditorContentView.class);
        CodeArea codeArea = viewEditor.getCodeArea();

        String text = codeArea.getSelectedText();
        if(text==null||text.equals("")){
            IndexRange rgCurr = new IndexRange(codeArea.getCaretPosition(),codeArea.getCaretPosition());
            codeArea.replaceText(rgCurr,"\n> 输入引用的内容\n");
            return;
        }
        StringBuilder sb = new StringBuilder("\n");
        List<String> list = Arrays.asList(text.split("\n"));
        for (String str:  list) {
            sb.append("\n> "+str );
        }
        codeArea.replaceText(codeArea.getSelection(),sb.toString());
    }

    @FXML
    private void onToolSave() {
        ArticleEditorView view = getView();
        Article article = view.getEditingArticle();
        Tab select = articlesTab.getSelectionModel().getSelectedItem();
        if (select == null) {
            return;
        }
        EditorContentView editor = fxViewByView(select.getContent(), EditorContentView.class);
        String source = editor.getCodeArea().getText();
        ArticleResource resource = new ArticleResource();
        if (article.getType() == null) {
            view.showAlertDialog("提示","请设置分类，然后重新保存。", Alert.AlertType.ERROR);
            return;
        }
        Map<String, ByteBuffer> images = editor.getImagesView().getImages();
        Map<String, byte[]> imageData = new HashMap<>(images.size());
        for (Map.Entry<String,ByteBuffer> item :images.entrySet()) {
            imageData.put(item.getKey(), item.getValue().array());
        }
        resource.setImages(imageData);
        ArticleContent content = article.getContent();
        if (content == null) {
            content = new ArticleContent();
        }
        content.setResources(resource);
        content.setSource(source);
        try {
            if(!articleService.saveArticle(article, content)) {
                view.showAlertDialog("提示", "保存失败, 请填写必要信息。", Alert.AlertType.ERROR);
            } else {
                editor.setSaved();
                select.setText(article.getTitle());
                this.emit(new RefreshEvent(article,this));
            }
        } catch (Exception e) {
            PrintWriter writer = new PrintWriter(new StringWriter());
            e.printStackTrace(writer);
            view.showAlertDialog("提示", "保存失败: 存储遇到异常 - \n" + writer.toString(), Alert.AlertType.ERROR);
            logger.error("fail to save article ", e);
        }
    }

    @FXML
    private void changeType() {
        TypeSelectView selectView = findView(TypeSelectView.class);
        selectView.show();
        ArticleType type = selectView.getSelected();
        if (type == null) {
            return;
        }
        ArticleEditorView view = getView();
        Article article = view.getEditingArticle();
        article.setType(type);
        view.refresh();
    }

}
