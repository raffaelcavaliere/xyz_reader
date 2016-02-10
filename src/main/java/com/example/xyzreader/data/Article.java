package com.example.xyzreader.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by raffaelcavaliere on 2016-01-31.
 */
public class Article implements Parcelable {

    private long _id;
    private String _title;
    private long _published_date;
    private String _author;
    private String _thumb_url;
    private String _photo_url;
    private float _aspect_ratio;
    private String _body;

    public Article (long id, String title, long published_date, String author, String thumb_url, String photo_url, float aspect_ratio, String body) {
        _id = id;
        _title = title;
        _published_date = published_date;
        _author = author;
        _thumb_url = thumb_url;
        _photo_url = photo_url;
        _aspect_ratio = aspect_ratio;
        _body = body;
    }

    public long getId() {
        return _id;
    }

    public String getTitle() {
        return _title;
    }

    public long getPublishedDate() {
        return _published_date;
    }

    public String getAuthor() {
        return _author;
    }

    public String getThumbUrl() {
        return _thumb_url;
    }

    public String getPhotoUrl() {
        return _photo_url;
    }

    public float getAspectRatio() {
        return _aspect_ratio;
    }

    public String getBody() {
        return _body;
    }

    private Article(Parcel in) {
        Bundle bundle = in.readBundle();
        _id = bundle.getLong("id");
        _title = bundle.getString("title");
        _published_date = bundle.getLong("published_date");
        _author = bundle.getString("author");
        _thumb_url = bundle.getString("thumb_url");
        _photo_url = bundle.getString("photo_url");
        _aspect_ratio = bundle.getFloat("aspect_ratio");
        _body = bundle.getString("body");
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        Bundle bundle = new Bundle();
        bundle.putLong("id", _id);
        bundle.putString("title", _title);
        bundle.putLong("published_date", _published_date);
        bundle.putString("author", _author);
        bundle.putString("thumb_url", _thumb_url);
        bundle.putString("photo_url", _photo_url);
        bundle.putFloat("aspect_ratio", _aspect_ratio);
        bundle.putString("body", _body);
        out.writeBundle(bundle);
    }

    public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>() {
        public Article createFromParcel(Parcel in) {
            return new Article(in);
        }
        public Article[] newArray(int size) {
            return new Article[size];
        }
    };


}
