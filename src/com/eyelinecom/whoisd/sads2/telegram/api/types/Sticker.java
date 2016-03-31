package com.eyelinecom.whoisd.sads2.telegram.api.types;

/**
 * Created by jeck on 30/03/16
 */
public class Sticker extends AbstractFile<Sticker>{
    private Integer width;
    private Integer height;
    private PhotoSize thumb;

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public PhotoSize getThumb() {
        return thumb;
    }

    public void setThumb(PhotoSize thumb) {
        this.thumb = thumb;
    }

    public Sticker() {

    }
}
