package com.example.sanay.blog;

import java.util.Date;

public class BlogPost extends BlogPostId {
    String imageurl, description, userid,thumbnailurl;
    public Date timestamp;

    public BlogPost() {
    }

    public BlogPost(String imageurl, String description, String userid, String thumbnailurl,Date timestamp) {
        this.imageurl = imageurl;
        this.description = description;
        this.userid = userid;
        this.thumbnailurl = thumbnailurl;
        this.timestamp = timestamp;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getThumbnailurl() {
        return thumbnailurl;
    }

    public void setThumbnailurl(String thumbnailurl) {
        this.thumbnailurl = thumbnailurl;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
