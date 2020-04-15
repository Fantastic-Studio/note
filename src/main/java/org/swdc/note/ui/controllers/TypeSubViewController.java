package org.swdc.note.ui.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.swdc.fx.FXController;
import org.swdc.fx.FXView;
import org.swdc.fx.anno.Aware;
import org.swdc.fx.anno.Listener;
import org.swdc.note.core.entities.Article;
import org.swdc.note.core.entities.ArticleContent;
import org.swdc.note.core.entities.ArticleType;
import org.swdc.note.core.render.FileExporter;
import org.swdc.note.core.service.ArticleService;
import org.swdc.note.ui.events.RefreshEvent;
import org.swdc.note.ui.view.*;
import org.swdc.note.ui.view.cells.*;
import org.swdc.note.ui.view.dialogs.BatchExportView;
import org.swdc.note.ui.view.dialogs.TypeCreateView;
import org.swdc.note.ui.view.dialogs.TypeEditView;
import org.swdc.note.ui.view.dialogs.TypeExportView;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class TypeSubViewController extends FXController {

    @Aware
    private ArticleService articleService = null;

    @FXML
    private ListView<ArticleType> typeList;

    @FXML
    private ListView<Article> articlesList;

    @FXML
    private ListView<Article> recentlyList;

    @FXML
    private TextField txtSearch;

    @Aware
    private ReaderView readerView = null;

    @Aware
    private ArticleEditorView editorView = null;

    private ObservableList<Article> recently = FXCollections.observableArrayList();

    private ObservableList<ArticleType> typeLists = FXCollections.observableArrayList();

    private ObservableList<Article> articles = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        typeList.getSelectionModel().selectedItemProperty().addListener((observableValue, typeOld, typeNew) -> {
            articles.clear();
            if (typeNew != null) {
                articles.addAll(articleService.getArticles(typeNew));
            }
        });
    }

    @Override
    public void initialize() {
        typeList.setCellFactory(list -> new ArticleTypeListCell(findView(ArticleTypeCell.class)));
        typeList.setItems(typeLists);
        articlesList.setCellFactory(list -> new ArticleListCell(findView(ArticleCell.class)));
        articlesList.setItems(articles);
        articlesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        recentlyList.setCellFactory(list -> new ArticleSimpleListCell(findView(ArticleSimpleCell.class)));
        recentlyList.setItems(recently);
        this.refresh(null);
    }

    @Listener(RefreshEvent.class)
    public void refresh(RefreshEvent event) {
        recently.clear();
        recently.addAll(articleService.getRecently());

        if (event == null || event.getData() == null) {
            typeLists.clear();
            articles.clear();
            typeLists.addAll(this.articleService.getTypes());
            return;
        }
        ArticleType type = event.getData();
        ArticleType selected = typeList.getSelectionModel().getSelectedItem();
        if (type != null) {
            articles.clear();
            articles.addAll(this.articleService.getArticles(selected));
        }
    }

    @FXML
    public void onTypeAdded() {
        TypeCreateView createView = findView(TypeCreateView.class);
        createView.show();
    }

    @FXML
    public void onCreateDocument() {
        ArticleEditorView editorView = findView(ArticleEditorView.class);
        Article article = new Article();
        article.setContent(new ArticleContent());
        article.setCreateDate(new Date());
        article.setTitle("未命名");
        editorView.addArticle(article);
        editorView.show();
    }

    @FXML
    public void onOpenFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("打开文档集");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("所有支持的格式","*.*"));
        File file = chooser.showOpenDialog(null);
        if (file == null) {
            return;
        }
        // 从exporter读取，存在支持的exporter那么就读进来
        // 按照分类和文档两种模式获取和处理
        FileExporter exporter = articleService.getFileExporter(file,true,true);

        if (exporter == null) {
            exporter = articleService.getFileExporter(file, true, false);
            if (exporter == null) {
                return;
            }
            FileExporter targetExporter = exporter;
            CompletableFuture.supplyAsync(() -> targetExporter.readTypeFile(file))
                .whenCompleteAsync((type, e) -> {
                    if (e != null) {
                        logger.error("fail to load article set: " + targetExporter.name());
                        return;
                    }
                    Platform.runLater(() -> {
                        ArticleSetView articleSetView = findView(ArticleSetView.class);
                        articleSetView.loadContent(type);
                        articleSetView.show();
                    });
            });
        } else {
            Article article = exporter.readFile(file);
            ReaderView readerView = findView(ReaderView.class);
            readerView.addArticle(article);
            readerView.show();
        }
    }

    @FXML
    public void showHelp() {
        File path = new File(getAssetsPath() + "/help.mdsrc");
        FileExporter exporter = articleService.getFileExporter(path,true,false);
        ArticleType type = exporter.readTypeFile(path);
        ArticleSetView setView = findView(ArticleSetView.class);
        setView.loadContent(type);
        setView.show();
    }

    public void createDocument(ActionEvent event) {
        ArticleEditorView editorView = findView(ArticleEditorView.class);
        Article article = new Article();
        article.setContent(new ArticleContent());
        article.setCreateDate(new Date());
        article.setTitle("未命名");
        ArticleType type = typeList.getSelectionModel().getSelectedItem();
        if (type != null) {
            article.setType(type);
        }
        editorView.addArticle(article);
        editorView.show();
    }

    public void openArticle(ActionEvent event) {
        List<Article> articles = articlesList.getSelectionModel().getSelectedItems();
        if (articles == null || articles.isEmpty()) {
            return;
        }
        for (Article article: articles) {
            readerView.addArticle(article);
        }
        readerView.show();
    }

    public void deleteArticle(ActionEvent event) {
        List<Article> articles = articlesList.getSelectionModel().getSelectedItems();
        if (articles == null || articles.isEmpty()) {
            return;
        }
        for (Article article: articles) {
            ArticleType type = article.getType();
            articleService.deleteArticle(article.getId());
            this.emit(new RefreshEvent(type, this.getView()));
        }
    }

    public void exportArticle(ActionEvent event) {
        List<Article> articles = articlesList.getSelectionModel().getSelectedItems();
        if (articles == null || articles.isEmpty()) {
            return;
        }
        if (articles.size() == 1) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("另存为");
            fileChooser.getExtensionFilters().addAll(articleService.getExporterFilters(false));
            File file = fileChooser.showSaveDialog(null);
            if (file == null) {
                return;
            }
            FileExporter exporter = articleService.getFileExporter(file,false, true);
            if (exporter == null) {
                return;
            }
            Article article = articlesList.getSelectionModel().getSelectedItem();
            if (article == null) {
                return;
            }
            exporter.writeFile(article,file);
            UIUtils.notification("文档《" + article.getTitle() + "》已经导出。", this.getView());
        } else {
            BatchExportView batchExportView = findView(BatchExportView.class);
            batchExportView.show();
            FileExporter exporter = batchExportView.getSelected();
            if (exporter == null) {
                return;
            }

            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("另存为");
            File directory = directoryChooser.showDialog(null);
            if (directory == null) {
                return;
            }
            Path dir = directory.toPath().toAbsolutePath();
            for (Article article: articles) {
               Path path = dir.resolve(article.getTitle() + "." + exporter.subfix());
                exporter.writeFile(article, path);
            }
            UIUtils.notification("选择的文档已经导出。", this.getView());
        }
    }

    public void editArticle(ActionEvent event) {
        List<Article> articles = articlesList.getSelectionModel().getSelectedItems();
        if (articles == null || articles.isEmpty()) {
            return;
        }
        for (Article article: articles) {
            editorView.addArticle(article);
        }
        editorView.show();
    }

    public void creatType(ActionEvent event) {
        this.onTypeAdded();
    }

    public void deleteType(ActionEvent event) {
        FXView view = getView();
        ArticleType type = typeList.getSelectionModel().getSelectedItem();
        if (type == null) {
            return;
        }
        view.showAlertDialog("提示", "删除分类将会删除分类的全部文档，确定要这样做吗？", Alert.AlertType.CONFIRMATION)
                .ifPresent(btn -> {
                    if (btn == ButtonType.OK) {
                        articleService.deleteType(type.getId());
                        this.emit(new RefreshEvent(type, view));
                    }
                });
    }

    public void onModifyType(ActionEvent e) {
        ArticleType type = typeList.getSelectionModel().getSelectedItem();
        if (type == null) {
            return;
        }
        TypeEditView editView = findView(TypeEditView.class);
        editView.setType(type);
        editView.show();
    }

    public void exportType(ActionEvent e) {
        ArticleType type = typeList.getSelectionModel().getSelectedItem();
        if (type == null) {
            return;
        }
        TypeExportView exportView = findView(TypeExportView.class);
        exportView.show();
        FileExporter exporter = exportView.getSelected();
        if(exporter == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("导出");
        chooser.getExtensionFilters().add(exporter.typeFilter());
        chooser.setInitialFileName(type.getName());
        File target = chooser.showSaveDialog(null);
        if (target == null) {
            return;
        }
        type = articleService.getType(type.getId());
        exporter.writeType(type, target);
        UIUtils.notification("分类《" + type.getName() + "》已经导出。", this.getView());
    }

    @FXML
    public void onSearch() {
        if (txtSearch.getText().isBlank()) {
            return;
        }
        SearchView searchView = findView(SearchView.class);
        searchView.search(txtSearch.getText());
        searchView.show();
        txtSearch.clear();
    }

}