package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.UUID;

@Entity
public class Image extends Model {
    @Id
    public String id;
    public String contentType;
    @Lob
    public byte[] data;

    public static Model.Finder<String,Image> find = new Model.Finder<String,Image>(
            String.class, Image.class
    );

    public Image(String contentType, byte[] data){
        this.id = UUID.randomUUID().toString();
        this.contentType = contentType;
        this.data = data;
    }

}
