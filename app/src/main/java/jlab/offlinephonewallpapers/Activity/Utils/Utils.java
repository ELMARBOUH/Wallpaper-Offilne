package jlab.offlinephonewallpapers.Activity.Utils;
/*
 * Created by Javier on 30/05/2020.
 */

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import jlab.offlinephonewallpapers.R;

public class Utils {
    public static final String WALLPAPER_ID_KEY = "WALLPAPER_ID_KEY";
    public static final String WALLPAPER_MIME_TYPE_KEY = "image/jpeg";
    public static final String SHOW_FAVORITES_KEY = "SHOW_FAVORITES_KEY";
    public static final String IS_FAVORITES_WALLPAPERS_KEY = "IS_FAVORITES_WALLPAPERS_KEY";
    public static final java.lang.String REFRESH_ON_RESUME_KEY = "REFRESH_ON_RESUME_KEY";
    private static final int PERMISSION_REQUEST_CODE = 9101;

    public static void showAboutDialog(final Context context, final View viewForSnack) {
        try {
            new AlertDialog.Builder(context, R.style.TextAppearance)
                    .setTitle(R.string.about)
                    .setMessage(R.string.about_content)
                    .setPositiveButton(R.string.accept, null)
                    .setNegativeButton(context.getString(R.string.contact), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                context.startActivity(new Intent(Intent.ACTION_SENDTO)
                                        .setData(Uri.parse(String.format("mailto:%s", context.getString(R.string.mail)))));
                            } catch (Exception | OutOfMemoryError ignored) {
                                ignored.printStackTrace();
                                Utils.showSnackBar(R.string.app_mail_not_found, viewForSnack);
                            }
                        }
                    })
                    .show();
        } catch (Exception | OutOfMemoryError ignored) {
            ignored.printStackTrace();
        }
    }


    public static void showSnackBar(int msg, View viewForSnack) {
        Snackbar snackbar = createSnackBar(msg, viewForSnack);
        if (snackbar != null) {
            ((TextView) snackbar.getView().findViewById(R.id.snackbar_text)).setTextColor(viewForSnack.getResources().getColor(R.color.white));
            snackbar.setActionTextColor(viewForSnack.getResources().getColor(R.color.colorAccent));
            snackbar.show();
        }
    }


    public static void showSnackBar(String msg, View viewForSnack) {
        Snackbar snackbar = Utils.createSnackBar(msg, viewForSnack);
        if (snackbar != null) {
            ((TextView) snackbar.getView().findViewById(R.id.snackbar_text)).setTextColor(viewForSnack.getResources().getColor(R.color.white));
            snackbar.show();
        }
    }

    public static Snackbar createSnackBar(String message, View viewForSnack) {
        if (viewForSnack == null)
            return null;
        Snackbar result = Snackbar.make(viewForSnack, message, Snackbar.LENGTH_LONG);
        ((TextView) result.getView().findViewById(R.id.snackbar_text)).setTextColor(viewForSnack.getResources().getColor(R.color.white));
        return result;
    }

    public static Snackbar createSnackBar(int message, View viewForSnack) {
        if (viewForSnack == null)
            return null;
        return Snackbar.make(viewForSnack, message, Snackbar.LENGTH_LONG);
    }


    public static void rateApp(Context context) {
        Uri uri = Uri.parse(String.format("market://details?id=%s", context.getPackageName()));
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (Exception | OutOfMemoryError ignored) {
            ignored.printStackTrace();
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(String.format("https://play.google.com/store/apps/details?id=%s"
                            , context.getPackageName()))));
        }
    }

    public static void addFileToContent(Context context, String path) {
        MediaScannerConnection.scanFile(context.getApplicationContext(), new String[]{path}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String s, Uri uri) {

                    }
                });
    }


    public static boolean requestPermission(Activity activity) {
        boolean request = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> requestPermissions = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                request = true;
            }
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                request = true;
            }
            if (request)
                requestAllPermission(activity, requestPermissions);
        }
        return request;
    }

    private static void requestAllPermission(Activity activity, ArrayList<String> requestPermissions) {
        String[] permission = new String[requestPermissions.size()];
        for (int i = 0; i < permission.length; i++)
            permission[i] = requestPermissions.get(i);
        ActivityCompat.requestPermissions(activity, permission, PERMISSION_REQUEST_CODE);
    }

    public static void shareWallpaper(Wallpaper current, Activity activity, View viewForSnack) {
        try {
            if (!requestPermission(activity)) {
                Uri uri = exportWallpaperToAppFolder(activity, current, null);
                if (uri != null && !requestPermission(activity)) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType(Utils.WALLPAPER_MIME_TYPE_KEY);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share)));
                } else
                    Utils.showSnackBar(R.string.can_not_share_wallpaper, viewForSnack);
            } else
                Utils.showSnackBar(R.string.provide_permissions, viewForSnack);
        }
        catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    public static Uri exportWallpaperToAppFolder(final Context context, Wallpaper current, String name) {
        name = name != null && name.length() > 0 ? name : "temp";
        final String pathToWallpaper = String.format("%s/jlab.Wallpapers/%s.jpg"
                , Environment.getExternalStorageDirectory().getPath()
                , name);
        String pathParentDir = String.format("%s/jlab.Wallpapers"
                , Environment.getExternalStorageDirectory().getPath());
        DocumentFile parentDir = DocumentFile.fromFile(new File(pathParentDir))
                , rootDir = DocumentFile.fromFile(Environment.getExternalStorageDirectory());
        File fileWallpaper = new File(pathToWallpaper);
        Uri result = null;
        try {
            if((parentDir.exists() || rootDir.createDirectory("jlab.Wallpapers").exists())
                    && (fileWallpaper.exists() || parentDir.createFile("", name + ".jpg").exists())) {
                FileOutputStream fw = new FileOutputStream(fileWallpaper);
                byte[] buffer = current.getWallpaper();
                fw.write(buffer);
                fw.close();
                result = Uri.parse(String.format("file://%s", pathToWallpaper));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.addFileToContent(context, pathToWallpaper);
                    }
                }).start();
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return result;
    }

    public static void setWallpaperAs(Wallpaper current, Activity activity, View viewForSnack) {
        try {
            if (!requestPermission(activity)) {
                Uri uri = exportWallpaperToAppFolder(activity, current, null);
                if (uri != null) {
                    Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                    intent.putExtra(".jpg", Utils.WALLPAPER_MIME_TYPE_KEY);
                    intent.setDataAndType(uri, Utils.WALLPAPER_MIME_TYPE_KEY);
                    activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.set_image_as)));
                } else
                    Utils.showSnackBar(R.string.can_not_set_as_image, viewForSnack);
            } else
                Utils.showSnackBar(R.string.provide_permissions, viewForSnack);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    public static void saveWallpaper(Wallpaper currentWallpaper, Activity activity, View viewForSnack) {
        try {
            if (!requestPermission(activity)) {
                if (exportWallpaperToAppFolder(activity, currentWallpaper, currentWallpaper.getId() + "") == null)
                    Utils.showSnackBar(R.string.can_not_save_image, viewForSnack);
                else
                    Utils.showSnackBar(R.string.save_complete, viewForSnack);
            } else
                Utils.showSnackBar(R.string.provide_permissions, viewForSnack);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }
}
