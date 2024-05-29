package com.example.craftastic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class CraftsDataSource {
    private DBHelper dbHelper;
    private SQLiteDatabase database;

    public CraftsDataSource(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public boolean insertCraft(Craft c) {
        boolean didSucceed = false;
        try {
            LocalDate today = LocalDate.now();
            String formattedDate = today.format(DateTimeFormatter.ISO_DATE);
            ContentValues initialValues = new ContentValues();
            initialValues.put("craftname", c.getCraftTitle());
            initialValues.put("craftprice", c.getPrice());
            initialValues.put("craftcondition", c.getCraftCondition());
            initialValues.put("craftdescription", c.getCraftDescription());
            initialValues.put("longitude", c.getLongitude());
            initialValues.put("latitude", c.getLatitude());
            initialValues.put("postdate", formattedDate);
            initialValues.put("userId", c.getUserId());
            didSucceed = database.insert("crafts", null, initialValues) > 0;
        } catch (Exception e) {
            Log.d("Crafts DB", "Something wrong");
        }
        return didSucceed;
    }

    public boolean updateCraft(Craft craft) {
        boolean didSucceed = false;
        try {
            ContentValues updateValues = new ContentValues();
            updateValues.put("craftname", craft.getCraftTitle());
            updateValues.put("craftprice", craft.getPrice());
            updateValues.put("craftcondition", craft.getCraftCondition());
            updateValues.put("craftdescription", craft.getCraftDescription());
            updateValues.put("longitude", craft.getLongitude());
            updateValues.put("latitude", craft.getLatitude());
            updateValues.put("userId", craft.getUserId());

            didSucceed = database.update("crafts", updateValues, "id = ?", new String[]{String.valueOf(craft.getCraftId())}) > 0;

        } catch (Exception e) {
            Log.d("Update Craft", "Failed to update craft: " + e.getMessage());
        }
        return didSucceed;
    }


    public boolean insertImage(CraftImage image) {
        boolean didSucceed = false;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("craft_id", image.getCraftId());
            initialValues.put("image", image.getImageData());
            didSucceed = database.insert("photos", null, initialValues) > 0;
        } catch (Exception e) {
            Log.d("Photos DB", "Something wrong");
        }
        return didSucceed;
    }

    public int getCraftLastID() {
        int newID = 1;
        String query = "SELECT MAX(id) FROM crafts";
        try {
            Cursor cursor = database.rawQuery(query, null);
            cursor.moveToFirst();
            newID = cursor.getInt(0);
            cursor.close();
        } catch (Exception e) {
            newID = -1;
        }
        return newID;
    }

    public ArrayList<Craft> getAllCrafts() {
        ArrayList<Craft> crafts = new ArrayList<>();
        String query = "SELECT c.id, c.craftname, c.craftprice, i.image FROM crafts c " +
                "LEFT JOIN photos i ON c.id = i.craft_id AND i.photo_id IN (" +
                "SELECT MIN(photo_id) FROM photos GROUP BY craft_id) " +
                "ORDER BY c.id;";

        try {
            Cursor cursor = database.rawQuery(query,null);
            if (cursor.moveToFirst()) {
                do {
                    Craft craft = new Craft();
                    craft.setCraftId(cursor.getInt(0));
                    craft.setCraftTitle(cursor.getString(1));
                    craft.setPrice(cursor.getFloat(2));
                    byte[] imageData = cursor.getBlob(3);
                    CraftImage craftImage = new CraftImage();
                    craftImage.setImageData(imageData);
                    craft.setFirstImage(craftImage);
                    crafts.add(craft);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.d("DatabaseError", "Error fetching user posted crafts: " + e.getMessage());
            crafts = new ArrayList<>();
        }
        return crafts;
    }

    public ArrayList<Craft> getUserPostedCrafts(String userId) {
        ArrayList<Craft> crafts = new ArrayList<>();
        String query = "SELECT c.id, c.craftname, c.craftprice, c.postdate, i.image FROM crafts c " +
                "LEFT JOIN photos i ON c.id = i.craft_id AND i.photo_id IN (" +
                "SELECT MIN(photo_id) FROM photos GROUP BY craft_id) " +
                "WHERE c.userId = ? ORDER BY c.id;";

        try {
            Cursor cursor = database.rawQuery(query, new String[]{userId});
            if (cursor.moveToFirst()) {
                do {
                    Craft craft = new Craft();
                    craft.setCraftId(cursor.getInt(0));
                    craft.setCraftTitle(cursor.getString(1));
                    craft.setPrice(cursor.getFloat(2));
                    craft.setPostDate(cursor.getString(3));
                    byte[] imageData = cursor.getBlob(4);
                    CraftImage craftImage = new CraftImage();
                    craftImage.setImageData(imageData);
                    craft.setFirstImage(craftImage);
                    crafts.add(craft);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.d("DatabaseError", "Error fetching user posted crafts: " + e.getMessage());
            crafts = new ArrayList<>();
        }
        return crafts;
    }

    public Craft getSepecfiedCraft(int id){
        Craft c = new Craft();
        String query = "SELECT * FROM crafts WHERE id = " + id;

        Cursor cursor = database.rawQuery(query,null);
        if(cursor.moveToFirst()){
            c.setCraftId(cursor.getInt(0));
            c.setCraftTitle(cursor.getString(1));
            c.setPrice(cursor.getFloat(2));
            c.setCraftCondition(cursor.getString(3));
            c.setCraftDescription(cursor.getString(4));
            c.setLongitude(cursor.getDouble(5));
            c.setLatitude(cursor.getDouble(6));
            c.setPostDate(cursor.getString(7));
            c.setUserId(cursor.getString(8));
        }
        cursor.close();
        ArrayList<CraftImage> images = new ArrayList<>();
        String imagesQuery = "SELECT image FROM photos WHERE craft_id = ?";
        Cursor imagesCursor = database.rawQuery(imagesQuery, new String[]{String.valueOf(id)});
        while (imagesCursor.moveToNext()) {
            byte[] imageData = imagesCursor.getBlob(0);
            CraftImage craftImage = new CraftImage();
            craftImage.setImageData(imageData);
            images.add(craftImage);
        }
        c.setCraftImages(images);
        imagesCursor.close();
        return c;
    }

    public boolean deleteCraft(int id){
        boolean photosDeleted = false;
        boolean craftDeleted = false;
        try {
            photosDeleted = database.delete("photos","craft_id=" +id, null)>0;
            craftDeleted = database.delete("crafts", "id=" +id, null)>0;
            return craftDeleted && photosDeleted;
        }catch (Exception e){
            return false;
        }
    }

    public boolean deleteCraftImages(int craftId) {
        boolean didSucceed = false;
        try {
            didSucceed = database.delete("photos", "craft_id = ?", new String[]{String.valueOf(craftId)}) > 0;
        } catch (Exception e) {
            Log.d("Delete Images", "Error deleting images: " + e.getMessage());
        }
        return didSucceed;
    }


    public boolean addToFavorites(int craftId, String userId) {
        boolean didSucceed = false;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("craft_id", craftId);
            initialValues.put("userId", userId);
            didSucceed = database.insert("favorites", null, initialValues) > 0;
        } catch (Exception e) {
            Log.d("Photos DB", "Something wrong");
        }
        return didSucceed;
    }

    public boolean removeFromFavorites(int craftId, String userId) {
        boolean didSucceed = false;
        try {
            didSucceed = database.delete("favorites", "craft_id = ? AND userId = ?", new String[] {String.valueOf(craftId), userId}) > 0;
        } catch (Exception e) {
            Log.d("Photos DB", "Something wrong");
        }
        return didSucceed;
    }

    public boolean isCraftFavorited(int craftId, String userId) {
        boolean isFavorited = false;
        try {
            String query = "SELECT COUNT(*) FROM favorites WHERE craft_id = ? AND userId = ?";
            Cursor cursor = database.rawQuery(query, new String[] {String.valueOf(craftId), userId});
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                isFavorited = true;
            }
            cursor.close();
        } catch (Exception e) {
            Log.d("Check Favorite", "Error checking if craft is favorited: " + e.getMessage());
        }
        return isFavorited;
    }

    public ArrayList<Craft> getUserFavorites(String userId) {
        ArrayList<Craft> crafts = new ArrayList<>();
        String query = "SELECT c.id, c.craftname, c.craftprice, c.postdate, c.latitude, c.longitude, i.image FROM crafts c " +
                "LEFT JOIN photos i ON c.id = i.craft_id AND i.photo_id IN (" +
                "SELECT MIN(photo_id) FROM photos GROUP BY craft_id) " +
                "JOIN favorites f ON f.craft_id = c.id " +
                "WHERE f.userId = ? ORDER BY c.id;";

        try {
            Cursor cursor = database.rawQuery(query, new String[]{userId});
            if (cursor.moveToFirst()) {
                do {
                    Craft craft = new Craft();
                    craft.setCraftId(cursor.getInt(0));
                    craft.setCraftTitle(cursor.getString(1));
                    craft.setPrice(cursor.getFloat(2));
                    craft.setPostDate(cursor.getString(3));
                    craft.setLatitude(cursor.getDouble(4));
                    craft.setLongitude(cursor.getDouble(5));
                    byte[] imageData = cursor.getBlob(6);
                    CraftImage craftImage = new CraftImage();
                    craftImage.setImageData(imageData);
                    craft.setFirstImage(craftImage);
                    crafts.add(craft);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.d("DatabaseError", "Error fetching user posted crafts: " + e.getMessage());
            crafts = new ArrayList<>();
        }
        return crafts;
    }


}
