package org.swdc.note.ui.controllers.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.swdc.fx.FXController;
import org.swdc.fx.anno.Aware;
import org.swdc.note.core.entities.ArticleType;
import org.swdc.note.core.service.ArticleService;
import org.swdc.note.ui.events.RefreshEvent;
import org.swdc.note.ui.events.RefreshType;
import org.swdc.note.ui.view.UIUtils;
import org.swdc.note.ui.view.dialogs.TypeEditView;

import java.net.URL;
import java.util.ResourceBundle;

public class TypeEditController extends FXController {

    @Aware
    private ArticleService articleService = null;

    @FXML
    private TextField txtName;

    private ArticleType type;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    public void onCancel() {
        txtName.setText("");
        TypeEditView editView = getView();
        editView.close();
    }

    @FXML
    public void onSave() {
        if (txtName.getText().isBlank()) {
            return;
        }
        TypeEditView editView = getView();
        if (type == null) {
            return;
        }
        type = articleService.getType(type.getId());
        type.setName(txtName.getText());
        ArticleType saved = articleService.saveType(type);
        if(saved != null) {
            editView.close();
            UIUtils.notification("分类《" + type.getName() + "》 保存成功！",editView);
        }
        this.emit(new RefreshEvent(type, this,RefreshType.UPDATE));
        txtName.setText("");
    }


    public void setType(ArticleType type) {
        this.type = type;
    }
}
