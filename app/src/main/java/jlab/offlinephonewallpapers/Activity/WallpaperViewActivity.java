package jlab.offlinephonewallpapers.Activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.view.MotionEvent;
import com.bumptech.glide.Glide;
import android.widget.AdapterView;
import android.view.LayoutInflater;

import jlab.offlinephonewallpapers.Activity.Utils.WallpapersAdapter;
import jlab.offlinephonewallpapers.R;
import android.support.v7.widget.Toolbar;
import android.support.annotation.NonNull;
import android.view.animation.AnimationUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.AppBarLayout;

import java.util.ArrayList;

import jlab.offlinephonewallpapers.Activity.Utils.Utils;
import jlab.offlinephonewallpapers.Activity.Utils.Wallpaper;
import jlab.offlinephonewallpapers.Activity.View.ZoomImageView;
import jlab.offlinephonewallpapers.Activity.Utils.WallpaperManager;

/*
 * Created by Javier on 30/05/2020.
 */

public class WallpaperViewActivity extends AppCompatActivity implements View.OnTouchListener,
        AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener, WallpapersAdapter.OnGetSetViewListener{

    private Toolbar toolbar;
    private AppBarLayout barImage;
    private Gallery gallery;
    private int currentIndex = 0;
    private WallpapersAdapter adapter;
    private Wallpaper currentWallpaper;
    private WallpaperManager wallpaperManager;
    private LayoutInflater mlInflater;
    private boolean isFavoriteWallpapers;
    private ArrayList<Wallpaper> allWallpapers;
    private static final int PERMISSION_REQUEST_CODE = 2901;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        this.barImage = (AppBarLayout) findViewById(R.id.ablImageBar);
        this.mlInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        this.gallery = (Gallery) findViewById(R.id.gallery);
        this.wallpaperManager = new WallpaperManager(this);
        this.currentIndex = savedInstanceState != null
                ? savedInstanceState.getInt(Utils.WALLPAPER_ID_KEY)
                : getIntent().getIntExtra(Utils.WALLPAPER_ID_KEY, 0);
        this.isFavoriteWallpapers = savedInstanceState != null
                ? savedInstanceState.getBoolean(Utils.IS_FAVORITES_WALLPAPERS_KEY)
                : getIntent().getBooleanExtra(Utils.IS_FAVORITES_WALLPAPERS_KEY, false);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.toolbar.setTitleTextAppearance(this, R.style.ToolBarApparence);
        setSupportActionBar(toolbar);

        this.allWallpapers = isFavoriteWallpapers
                ? wallpaperManager.getWallpapersThumbFavorites()
                : wallpaperManager.getWallpapersThumb();
        this.adapter = new WallpapersAdapter(this, this.allWallpapers.size());
        this.setTitle("");
        adapter.setonGetSetViewListener(this);
        gallery.setOnItemSelectedListener(this);
        gallery.setOnItemClickListener(this);
        gallery.setAdapter(adapter);
        gallery.setSelection(currentIndex, true);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnFavorite:
                if (currentWallpaper != null) {
                    if (!currentWallpaper.isFavorite()) {
                        wallpaperManager.setFavoriteWallpaper(currentWallpaper.getId(), true);
                        currentWallpaper.setFavorite(true);
                    } else {
                        wallpaperManager.setFavoriteWallpaper(currentWallpaper.getId(), false);
                        currentWallpaper.setFavorite(false);
                    }
                    item.setIcon(currentWallpaper.isFavorite()
                            ? R.drawable.img_favorite_checked
                            : R.drawable.img_favorite_not_checked);
                    item.setTitle(currentWallpaper.isFavorite()
                            ? R.string.remove_of_favorite_folder
                            : R.string.add_to_favorite_folder);
                }
                break;
            case R.id.mnShare:
                //shareWallpaper
                Utils.shareWallpaper(currentWallpaper, this, gallery);
                break;
            case R.id.mnSetImageAs:
                //Set image as
                Utils.setWallpaperAs(currentWallpaper, this, gallery);
                break;
            case R.id.mnSave:
                //Save
                Utils.saveWallpaper(currentWallpaper, this, gallery);
                break;
            case R.id.mnRateApp:
                try {
                    Utils.rateApp(this);
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                break;
            case R.id.mnAbout:
                Utils.showAboutDialog(this, this.toolbar);
                break;
            case R.id.mnClose:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options_image, menu);
        MenuItem item = menu.findItem(R.id.mnFavorite);
        if(currentWallpaper != null) {
            item.setIcon(currentWallpaper.isFavorite()
                    ? R.drawable.img_favorite_checked
                    : R.drawable.img_favorite_not_checked);
            item.setTitle(currentWallpaper.isFavorite()
                    ? R.string.remove_of_favorite_folder
                    : R.string.add_to_favorite_folder);
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Utils.WALLPAPER_ID_KEY, currentWallpaper.getId());
        outState.putBoolean(Utils.IS_FAVORITES_WALLPAPERS_KEY, isFavoriteWallpapers);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return gallery.onTouchEvent(motionEvent);
    }

    @Override
    public View getView(ViewGroup parent, int position) {
        return mlInflater.inflate(R.layout.image_view, parent, false);
    }

    @Override
    public void setView(View view, int position) {
        view.findViewById(R.id.ivImageContent).setOnTouchListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        try {
            currentWallpaper = allWallpapers.get(i);
            currentIndex = i;
            if (view != null) {
                Glide.with(getBaseContext()).load(currentWallpaper.getWallpaper())
                        .into((ZoomImageView) view.findViewById(R.id.ivImageContent));
            }
            invalidateOptionsMenu();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        barImage.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.alpha_in_out));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}