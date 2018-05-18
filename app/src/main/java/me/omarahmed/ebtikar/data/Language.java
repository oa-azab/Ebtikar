package me.omarahmed.ebtikar.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Dell on 18/05/2018.
 */

public class Language {
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("level")
    @Expose
    private String level;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
