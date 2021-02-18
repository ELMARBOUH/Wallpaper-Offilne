package jlab.offlinephonewallpapers.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.widget.ImageView;
import jlab.offlinephonewallpapers.R;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;
import jlab.offlinephonewallpapers.Activity.Utils.WallpaperManager;

/**
 * Created by Javier on 7/4/2020.
 */

public class SplashActivity extends AppCompatActivity {
    private ImageView ivIcon;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        ivIcon = (ImageView) findViewById(R.id.ivImageInSplash);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.beat);
        ivIcon.startAnimation(animation);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new WallpaperManager(getBaseContext()).getWallpapersThumb();
                        Intent intent = new Intent(getBaseContext(), WallpapersActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                }).start();
            }
        }, 2000);
    }
}
