package jlab.offlinephonewallpapers.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.View;
import android.os.Bundle;
import android.view.MenuItem;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import jlab.offlinephonewallpapers.Activity.Utils.WallpapersAdapter;
import jlab.offlinephonewallpapers.R;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest;
import android.support.v7.app.AppCompatActivity;
import jlab.offlinephonewallpapers.Activity.Utils.Utils;
import jlab.offlinephonewallpapers.Activity.Utils.Wallpaper;


import java.util.ArrayList;

import jlab.offlinephonewallpapers.Activity.Utils.WallpaperManager;

public class WallpapersActivity extends AppCompatActivity {

    private ListView lvLeft, lvRight;
    private boolean dispatched, showFavorites, refreshOnResume;
    private WallpaperManager wallpaperManager;
    private int positionMiddle = 0;
    private WallpapersAdapter leftAdapter, rightAdapter;
    private ArrayList<Wallpaper> allWallpapers;
    private LayoutInflater mlInflater;
    private WallpapersAdapter.OnGetSetViewListener leftOnGetSetListener = new WallpapersAdapter.OnGetSetViewListener() {
        @Override
        public View getView(ViewGroup parent, int position) {
            return mlInflater.inflate(R.layout.item_list_view, parent, false);
        }

        @Override
        public void setView(View view, final int position) {
            final ImageView imageView = view.findViewById(R.id.ivWallpaper);
            LinearLayout linearLayout = view.findViewById(R.id.llImageContent);
            final Wallpaper current = allWallpapers.get(position);
            ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
            double div = (current.getWidth() * 2f) / getScreenWidth();
            params.height = (int) (current.getHeight() / div);
            linearLayout.setLayoutParams(params);
            final byte[] image = current.getThumbnails();
            Glide.with(getBaseContext()).load(image).centerCrop().into(imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickWallpaperItem(position);
                }
            });
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClickWallpaperItem(position);
                    return false;
                }
            });
        }
    };

    private WallpapersAdapter.OnGetSetViewListener rightOnGetSetListener = new WallpapersAdapter.OnGetSetViewListener() {
        @Override
        public View getView(ViewGroup parent, int position) {
            return mlInflater.inflate(R.layout.item_list_view, parent, false);
        }

        @Override
        public void setView(View view, final int position) {
            final ImageView imageView = view.findViewById(R.id.ivWallpaper);
            LinearLayout linearLayout = view.findViewById(R.id.llImageContent);
            final Wallpaper current = allWallpapers.get(position + positionMiddle);
            ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
            double div = (current.getWidth() * 2f) / getScreenWidth();
            params.height = (int) (current.getHeight() / div);
            linearLayout.setLayoutParams(params);
            final byte [] image = current.getThumbnails();
            Glide.with(getBaseContext()).load(image).centerCrop().into(imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickWallpaperItem(position + positionMiddle);
                }
            });
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClickWallpaperItem(position + positionMiddle);
                    return false;
                }
            });
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            MotionEvent event1 = MotionEvent.obtain(event);
            if (v.equals(lvLeft) && !dispatched) {
                dispatched = true;
                event1.setLocation(event.getX() + getScreenWidth() / 2, event.getY());
                lvRight.dispatchTouchEvent(event1);
            } else if (v.equals(lvRight) && !dispatched) {
                dispatched = true;
                event1.setLocation(event.getX() - getScreenWidth() / 2, event.getY());
                lvLeft.dispatchTouchEvent(event1);
            }
            dispatched = false;
            return false;
        }
    };

    private int getScreenWidth () {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    private void onClickWallpaperItem (int position) {
        if(!dispatched) {
            dispatched = true;
            refreshOnResume = showFavorites;
            Intent intent = new Intent(this, WallpaperViewActivity.class);
            intent.putExtra(Utils.WALLPAPER_ID_KEY, position);
            intent.putExtra(Utils.IS_FAVORITES_WALLPAPERS_KEY, showFavorites);
            startActivity(intent);
        }
    }

    private void onLongClickWallpaperItem (int position) {
        if(!dispatched) {
            dispatched = true;
            final Wallpaper current = allWallpapers.get(position);
            new AlertDialog.Builder(this)
                    .setItems(new String[]{
                            getString(current.isFavorite()
                                    ? R.string.remove_of_favorite_folder
                                    : R.string.add_to_favorite_folder),
                            getString(R.string.share),
                            getString(R.string.set_image_as),
                            getString(R.string.save)
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            if(position == 0) {
                                //Add or Remove from favorite
                                if (!current.isFavorite()) {
                                    wallpaperManager.setFavoriteWallpaper(current.getId(), true);
                                    current.setFavorite(true);
                                } else {
                                    wallpaperManager.setFavoriteWallpaper(current.getId(), false);
                                    current.setFavorite(false);
                                }
                            }
                            else if(position == 1) {
                                //shareWallpaper
                                Utils.shareWallpaper(current, WallpapersActivity.this, lvLeft);
                            }
                            else if(position == 2) {
                                //Set image as
                                Utils.setWallpaperAs(current, WallpapersActivity.this, lvLeft);
                            }
                            else {
                                //Save
                                Utils.saveWallpaper(current, WallpapersActivity.this, lvLeft);
                            }
                        }
                    }).show();
            vibrate();
            dispatched = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        dispatched = false;
        if(refreshOnResume) {
            refreshOnResume = false;
            loadAdapters();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpapers);

        this.mlInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        this.lvLeft = (ListView) findViewById(R.id.lvLeftWallpapers);
        this.lvRight = (ListView) findViewById(R.id.lvRightWallpapers);
        this.wallpaperManager = new WallpaperManager(this);

        this.showFavorites = savedInstanceState != null && savedInstanceState.getBoolean(Utils.SHOW_FAVORITES_KEY);
        this.refreshOnResume = savedInstanceState != null && savedInstanceState.getBoolean(Utils.REFRESH_ON_RESUME_KEY);

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
        if (!refreshOnResume)
            loadAdapters();
    }

    private void loadAdapters() {
        this.allWallpapers = showFavorites
                ? this.wallpaperManager.getWallpapersThumbFavorites()
                : this.wallpaperManager.getWallpapersThumb();
        this.positionMiddle = this.allWallpapers.size() / 2;

        this.setTitle(showFavorites ? R.string.favorites_wallpapers : R.string.app_name);

        this.leftAdapter = new WallpapersAdapter(this, this.allWallpapers.size() == 1
                ? 1 : this.positionMiddle);
        this.rightAdapter = new WallpapersAdapter(this, this.allWallpapers.size() > 1
                ? this.positionMiddle + (this.allWallpapers.size() % 2 == 0 ? 0 : 1)
                : 0);

        leftAdapter.setonGetSetViewListener(this.leftOnGetSetListener);
        rightAdapter.setonGetSetViewListener(this.rightOnGetSetListener);

        lvLeft.setAdapter(this.leftAdapter);
        lvRight.setAdapter(this.rightAdapter);

        lvLeft.setOnTouchListener(this.onTouchListener);
        lvRight.setOnTouchListener(this.onTouchListener);

        lvLeft.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onClickWallpaperItem(position);
            }
        });
        lvRight.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onClickWallpaperItem(position + positionMiddle);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wallpapers, menu);
        MenuItem item = menu.findItem(R.id.mnFavorite);
        item.setIcon(showFavorites
                ? R.drawable.img_favorite_checked
                : R.drawable.img_favorite_not_checked);
        item.setTitle(showFavorites
                ? R.string.hide_favorite_folder
                : R.string.show_favorite_folder);
        return true;
    }

    private void vibrate() {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(50);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnFavorite:
                showFavorites = !showFavorites;
                item.setIcon(showFavorites ? R.drawable.img_favorite_checked : R.drawable.img_favorite_not_checked);
                item.setTitle(showFavorites ? R.string.hide_favorite_folder : R.string.show_favorite_folder);
                loadAdapters();
                break;
            case R.id.mnRateApp:
                try {
                    Utils.rateApp(this);
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                break;
            case R.id.mnAbout:
                Utils.showAboutDialog(this, lvLeft);
                break;
            case R.id.mnClose:
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Utils.SHOW_FAVORITES_KEY, showFavorites);
        outState.putBoolean(Utils.REFRESH_ON_RESUME_KEY, refreshOnResume);
    }
}
