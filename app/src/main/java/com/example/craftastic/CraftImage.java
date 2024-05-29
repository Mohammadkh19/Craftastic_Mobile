package com.example.craftastic;

public class CraftImage {
    private int imageId;
    private int craftId;
    private byte[] imageData;

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getCraftId() {
        return craftId;
    }

    public void setCraftId(int craftId) {
        this.craftId = craftId;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}
