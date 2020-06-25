module noteEditor {

    requires fx.framework.core;
    requires fx.framework.jpa;
    requires fx.framework.resource;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires java.persistence;
    requires lombok;
    requires org.apache.tika.core;
    requires remark;

    requires org.fxmisc.richtext;

    // require that access loggers
    requires slf4j.api;
    requires flowless;
    requires flexmark.util.data;
    requires flexmark.profile.pegdown;
    requires flexmark.util.misc;
    requires flexmark;
    requires freemarker;
    requires jlatexmath;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires org.jsoup;
    requires jnativehook;
    requires java.logging;
    requires org.controlsfx.controls;
    requires epublib.core;

    opens org.swdc.note to
            fx.framework.core,
            javafx.graphics;

    opens org.swdc.note.core.proto to
            fx.framework.core;

    opens org.swdc.note.core.entities to
            fx.framework.jpa,
            com.fasterxml.jackson.databind,
            org.hibernate.orm.core;

    opens org.swdc.note.ui.controllers to
            fx.framework.core,
            javafx.fxml;

    opens org.swdc.note.ui.controllers.dialogs to
            fx.framework.core,
            javafx.fxml;

    opens org.swdc.note.ui.component to
            fx.framework.core;

    opens org.swdc.note.core.render to
            fx.framework.core;

    opens org.swdc.note.core.formatter to
            fx.framework.core;

    opens org.swdc.note.core.service to
            fx.framework.core;

    opens org.swdc.note.config to
            org.controlsfx.controls,
            fx.framework.core;

    opens org.swdc.note.core.repo to
            fx.framework.jpa,
            fx.framework.core;

    opens org.swdc.note.ui.view to
            fx.framework.core,
            javafx.controls,
            javafx.graphics;

    opens org.swdc.note.ui.view.cells to
            fx.framework.core,
            com.fasterxml.jackson.databind,
            javafx.controls,
            javafx.graphics;

    opens org.swdc.note.ui.view.dialogs to
            fx.framework.core,
            javafx.controls,
            javafx.graphics;

    opens views to
            fx.framework.core,
            javafx.graphics;

    opens appIcons to
            fx.framework.resource,
            fx.framework.core;

}