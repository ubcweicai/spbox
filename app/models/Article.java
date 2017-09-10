package models;

import com.avaje.ebean.annotation.EnumMapping;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.db.ebean.Model;
import play.libs.Json;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;
//import java.beans.Transient;

@Entity
public class Article extends Model {

    @Id
    public String id;
    public String name;
    public String title;
    @Column(columnDefinition = "LONGTEXT")
    public String content;
    public ArticleType type;

    public static Finder<String,Article> find = new Finder<String,Article>(
            String.class, Article.class
    );


    public Article(String name, String title, String content, ArticleType type)
    {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.title = title;
        this.content = content;
        this.type = type;
        try {
            this.save();
            Logger.info("Create article success!");
        } catch (Exception e) {
            Logger.error("Create article failed: " + e);
        }
    }


    public String getTitle() {
        return title;
    }


    public String getContent()
    {
        return content;
    }


    public ObjectNode toJsonObject()
    {
        ObjectNode result = Json.newObject();
        result.put("id", this.id);
        result.put("name", this.name);
        result.put("title", this.title);
        result.put("content", this.content);
        return result;
    }

    @EnumMapping(nameValuePairs="BUILTIN=0, GENERAL=1")
    public enum ArticleType {
        BUILTIN,  // Admin should not change builtin article's name
        GENERAL;  // General type article (further use)
    }
}

