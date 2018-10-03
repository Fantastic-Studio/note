package org.swdc.note.app.file;

import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

/**
 * 文件格式接口，用于导入导出数据
 */
public abstract class FileFormater {

    /**
     * 被处理的文件的类型名
     * @return 类型名
     */
    public abstract String getFormatName();

    /**
     * 处理读取
     * @param target
     */
    public abstract <T> T processRead(File target,Class<T> clazz);

    /**
     * 处理写入
     * @param target
     */
    public abstract void processWrite(File target,Object targetObj);

    public abstract List<FileChooser.ExtensionFilter> getFilters();

    public abstract boolean canRead();

    public abstract boolean canWrite();

    public String toString(){
        return this.getFormatName();
    }

}
