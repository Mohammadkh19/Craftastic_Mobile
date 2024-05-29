package com.example.craftastic;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;

public class Craft {
    private int craftId;
    private String craftTitle, craftDescription, craftCondition, userId;
    private double latitude, longitude;
    private float price;
    private String postDate;
    private boolean isFavorite;
    private CraftImage firstImage;
    private ArrayList<CraftImage> craftImages;

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public ArrayList<CraftImage> getCraftImages() {
        return craftImages;
    }

    public void setCraftImages(ArrayList<CraftImage> craftImages) {
        this.craftImages = craftImages;
    }

    public Craft() {
        craftId = -1;
    }

    public CraftImage getFirstImage() {
        return firstImage;
    }

    public void setFirstImage(CraftImage firstImage) {
        this.firstImage = firstImage;
    }

    public int getCraftId() {
        return craftId;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public void setCraftId(int craftId) {
        this.craftId = craftId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCraftTitle() {
        return craftTitle;
    }

    public void setCraftTitle(String craftTitle) {
        this.craftTitle = craftTitle;
    }

    public String getCraftDescription() {
        return craftDescription;
    }

    public void setCraftDescription(String craftDescription) {
        this.craftDescription = craftDescription;
    }

    public String getCraftCondition() {
        return craftCondition;
    }

    public void setCraftCondition(String craftCondition) {
        this.craftCondition = craftCondition;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
