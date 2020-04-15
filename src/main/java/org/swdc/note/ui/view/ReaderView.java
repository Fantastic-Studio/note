package org.swdc.note.ui.view;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.swdc.fx.FXView;
import org.swdc.fx.anno.Aware;
import org.swdc.fx.anno.View;
import org.swdc.fx.resource.icons.FontSize;
import org.swdc.fx.resource.icons.MaterialIconsService;
import org.swdc.note.core.entities.Article;
import org.swdc.note.core.entities.ArticleContent;
import org.swdc.note.core.render.HTMLResolver;
import org.swdc.note.core.service.ArticleService;

import java.util.HashMap;
import java.util.Map;

@View(title = "阅读",resizeable = true,background = true,style = "editor")
public class ReaderView extends FXView {

    private ObservableList<Tab> articles = FXCollections.observableArrayList();
    private Map<Article, Tab> articleTabMap = new HashMap<>();

    @Aware
    private HTMLResolver render = null;

    @Aware
    private MaterialIconsService iconsService = null;

    @Aware
    private ArticleService articleService = null;

    @Override
    public void initialize() {
        TabPane articlesTab = findById("articleTab");
        Bindings.bindContentBidirectional(articlesTab.getTabs(), articles);
        Stage stage = getStage();
        stage.setMinWidth(1000);
        stage.setMinHeight(600);
        this.initViewToolButton("edit","edit");
        this.initViewToolButton("delete","delete");
        this.initViewToolButton("toc","library_books");
        stage.setOnCloseRequest(e -> {
            articles.clear();
            articleTabMap.clear();
        });
    }

    private void initViewToolButton(String id, String icon) {
        Button btn = findById(id);
        if (btn == null) {
            return;
        }
        btn.setPadding(new Insets(4,4,4,4));
        btn.setFont(iconsService.getFont(FontSize.MIDDLE_SMALL));
        btn.setText(iconsService.getFontIcon(icon));
    }

    public Article getArticle(Long articleId) {
        if (articleId == null) {
            return null;
        }
        Article select = articleTabMap
                .keySet()
                .stream()
                .filter(k -> articleId.equals(k.getId()))
                .findFirst()
                .orElse(null);
        return select;
    }

    public void refresh(Long articleId) {
        Article article = getArticle(articleId);
        if (article == null) {
            return;
        }
        Tab tab = articleTabMap.get(article);
        Article refreshed = articleService.getArticle(articleId);
        ArticleContent content = refreshed.getContent();
        article.setContent(content);
        WebView view = (WebView) tab.getContent();

        String articleSource = render.renderBytes(content.getSource(),content.getResources().getImages());
        String renderedContext = render.renderHTML(articleSource);
        view.getEngine().loadContent(renderedContext);
    }

    public Tab addArticle(Article article) {
        Article hasOpen = null;
        if (article.getId() != null) {
            hasOpen = getArticle(article.getId());
        }

        TabPane tabPane = findById("articleTab");
        if(hasOpen != null) {
            Tab tab =  articleTabMap.get(hasOpen);
            tabPane.getSelectionModel().select(tab);
            return tab;
        }
        Tab tab = new Tab();
        tab.setClosable(true);
        tab.setText(article.getTitle());
        tab.setClosable(true);
        WebView view = new WebView();

        ArticleContent content = article.getContent();

        String articleSource = render.renderBytes(content.getSource(),content.getResources().getImages());
        String renderedContext = render.renderHTML(articleSource);
        view.getEngine().loadContent(renderedContext);

        tab.setContent(view);
        articleTabMap.put(article, tab);
        articles.add(tab);
        view.prefHeightProperty().bind(tab.getTabPane().heightProperty().subtract(42));
        view.prefWidthProperty().bind(tab.getTabPane().widthProperty().subtract(42));
        tab.setOnCloseRequest(e -> onTabClose(e,tab,article));

        tabPane.getSelectionModel().select(tab);
        return tab;
    }

    public void closeTab(Long articleId) {
        Article article = getArticle(articleId);
        Tab tab = articleTabMap.get(article);
        articles.remove(tab);
        articleTabMap.remove(article);
        if (articles.size() <= 0) {
            this.getStage().close();
        }
    }

    public Article getReadingArticle() {
        TabPane tabPane = findById("articleTab");
        Tab tab  = tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            return null;
        }
        Map.Entry<Article, Tab> entry = articleTabMap.entrySet()
                .stream()
                .filter(ent -> ent.getValue() == tab)
                .findFirst().orElse(null);
        return entry.getKey();
    }

    private void onTabClose(Event e, Tab tab, Article article) {
        articles.remove(tab);
        articleTabMap.remove(article);
        if (articles.size() == 0) {
            getStage().close();
        }
        return;
    }

}