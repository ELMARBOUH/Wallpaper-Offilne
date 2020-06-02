package jlab.offlinephonewallpapers.Activity.Utils;

/*
 * Created by Javier on 30/05/2020.
 */

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Base64;

import java.io.FileInputStream;
import java.io.IOException;

public class Wallpaper {

    private static final String WALLPAPERS_ASSETS_DIR_NAME = "wallpapers";
    private static final String THUMBNAILS_ASSETS_DIR_NAME = "thumbnails";
    private int id, width, height;
    private AssetManager assetManager;
    private byte [] bytesThumb;
    private boolean isFavorite;

    public Wallpaper(AssetManager assetManager, int id, int width, int height, boolean isFavorite) {
        this.id = id;
        this.assetManager = assetManager;
        this.isFavorite = isFavorite;
        this.width = width;
        this.height = height;
    }

    public int getId() {
        return id;
    }

    public byte[] getWallpaper() {
        byte[] result = new byte[0];
        try {
            AssetFileDescriptor fd = assetManager.openFd(String.format("%s/%s.jpg", WALLPAPERS_ASSETS_DIR_NAME, this.id));
            result = new byte[(int) fd.getLength()];
            FileInputStream inputStream = fd.createInputStream();
            inputStream.read(result);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }

    public byte [] getThumbnails() {
        if (bytesThumb == null)
        {
            try {
                AssetFileDescriptor fd = assetManager.openFd(String.format("%s/%s.jpg", THUMBNAILS_ASSETS_DIR_NAME, this.id));
                bytesThumb = new byte[(int) fd.getLength()];
                FileInputStream inputStream = fd.createInputStream();
                inputStream.read(bytesThumb);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytesThumb;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
