package org.swdc.note.app.entity;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Map;

/**
 *  文章内容模型
 */
@Entity
public class ArtleContext {

    @Id
    @GeneratedValue
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String content;

    @OneToOne(mappedBy = "context")
    @Getter
    @Setter
    private Artle artle;

    @Column(columnDefinition = "text")
    private String imageResources;

    public Map<String,String> getImageRes(){
       return (Map)JSON.parse(imageResources);
    }

    public void setImageRes(Map imageRes){
        imageResources = JSON.toJSONString(imageRes);
    }

}
