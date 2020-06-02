package jlab.offlinephonewallpapers.Activity.Utils;

import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.support.annotation.NonNull;
import jlab.offlinephonewallpapers.Activity.Utils.Wallpaper;
import jlab.offlinephonewallpapers.Activity.Utils.WallpaperManager;

/*
 * Created by Javier on 1/10/2016.
 */
public class WallpapersAdapter extends ArrayAdapter<Wallpaper> {

    private OnGetSetViewListener monGetSetViewListener;
    private WallpaperManager wallpaperManager;
    private int count = 0;

    public WallpapersAdapter (Context context, int count) {
        super(context, 0);
        this.count = count;
        wallpaperManager = new WallpaperManager(context);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        try {
            convertView = this.monGetSetViewListener.getView(parent, position);
            this.monGetSetViewListener.setView(convertView, position);
        } catch (Exception | OutOfMemoryError ignored) {
            ignored.printStackTrace();
        }
        return convertView;
    }

    public void setonGetSetViewListener(OnGetSetViewListener monGetSetViewListener) {
        this.monGetSetViewListener = monGetSetViewListener;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public interface OnGetSetViewListener {
        View getView(ViewGroup parent, int position);

        void setView(View view, int position);
    }
}
