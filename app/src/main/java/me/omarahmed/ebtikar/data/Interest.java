package me.omarahmed.ebtikar.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Dell on 18/05/2018.
 */

public class Interest {
    @SerializedName("title")
    @Expose
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
