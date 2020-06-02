package jlab.offlinephonewallpapers.Activity.Utils;

/*
 * Created by Javier on 22/03/2020.
 */

import java.util.ArrayList;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class WallpaperManager extends SQLiteAssetHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "wallpapers.db";
    private static final String WALLPAPER_TABLE_NAME = "Wallpaper";
    private static final String WIDTH_COLUMN = "Width";
    private static final String HEIGHT_COLUMN = "Height";
    private static final String ID_COLUMN = "id";
    private static final String FAVORITE_COLUMN = "Favorite";
    private static int count = -1;
    private static ArrayList<Wallpaper> cache;
    private AssetManager assetManager;

    public WallpaperManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.assetManager = context.getAssets();
    }

    public int setFavoriteWallpaper(int wallpaperId, boolean isFavorite) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FAVORITE_COLUMN, isFavorite ? 1 : 0);
        if(cache != null)
            cache.get(wallpaperId).setFavorite(isFavorite);
        return db.update(WALLPAPER_TABLE_NAME, contentValues, ID_COLUMN + " LIKE ?",
                new String[]{String.valueOf(wallpaperId)});
    }

    public ArrayList<Wallpaper> getWallpapersThumb() {
        if(cache == null) {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(WALLPAPER_TABLE_NAME, null, null, null, null, null, null, null);
            cache = new ArrayList<>();
            while (cursor.moveToNext())
                cache.add(new Wallpaper(assetManager, cursor.getInt(cursor.getColumnIndex(ID_COLUMN)),
                        cursor.getInt(cursor.getColumnIndex(WIDTH_COLUMN)),
                        cursor.getInt(cursor.getColumnIndex(HEIGHT_COLUMN)),
                        cursor.getInt(cursor.getColumnIndex(FAVORITE_COLUMN)) != 0));
            cursor.close();
        }
        return cache;
    }

    public ArrayList<Wallpaper> getWallpapersThumbFavorites() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(WALLPAPER_TABLE_NAME, null, FAVORITE_COLUMN + " = 1", null, null, null, null, null);
        ArrayList<Wallpaper> result = new ArrayList<>();
        while (cursor.moveToNext())
            result.add(new Wallpaper(assetManager, cursor.getInt(cursor.getColumnIndex(ID_COLUMN)),
                    cursor.getInt(cursor.getColumnIndex(WIDTH_COLUMN)),
                    cursor.getInt(cursor.getColumnIndex(HEIGHT_COLUMN)),
                    cursor.getInt(cursor.getColumnIndex(FAVORITE_COLUMN)) != 0));
        cursor.close();
        return result;
    }

    public int getCountWallpaper() {
        if (count < 0) {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(WALLPAPER_TABLE_NAME, null, null, null, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }

    public int getCountFavoriteWallpaper() {
        if (count < 0) {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(WALLPAPER_TABLE_NAME, null, FAVORITE_COLUMN + " = 1", null, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }
}
