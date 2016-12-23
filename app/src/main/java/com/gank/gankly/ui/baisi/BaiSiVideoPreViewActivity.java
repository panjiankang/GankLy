package com.gank.gankly.ui.baisi;

import android.content.res.Configuration;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.gank.gankly.R;
import com.gank.gankly.RxBus.RxBus_;
import com.gank.gankly.ui.baisi.image.GallerySize;
import com.gank.gankly.ui.base.BaseActivity;
import com.gank.gankly.utils.ShareUtils;
import com.superplayer.library.SuperPlayer;

import butterknife.BindView;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Create by LingYan on 2016-12-14
 * Email:137387869@qq.com
 */

public class BaiSiVideoPreViewActivity extends BaseActivity implements SuperPlayer.OnNetChangeListener {
    public static final String URL = "Url";
    public static final String SIZE_HEIGTH = "Size_Height";
    public static final String SIZE_WIDTH = "Size_Width";
    public static final String LOCATION_Y = "Location_Y";
    public static final String TITLE = "title";

    @BindView(R.id.super_player)
    SuperPlayer mSuperPlayer;
    @BindView(R.id.baisi_preview_fl)
    View mView;
    @BindView(R.id.baisi_preview_toolbar)
    Toolbar mToolbar;

    private String mUrl;
    private int mWidth;
    private int mHeigth;
    private int mLocationY;
    private String mTitle;
    private String mShareUrl;

    @Override
    protected int getContentId() {
        return R.layout.fragment_baisi_preview_video;
    }

    @Override
    protected void initValues() {
        RxBus_.getDefault()
                .toObservableSticky(GallerySize.class)
                .subscribe(new Observer<GallerySize>() {
                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(GallerySize gallerySize) {
                        if (gallerySize != null) {
                            mUrl = gallerySize.getUrl();
                            mHeigth = gallerySize.getHeight();
                            mWidth = gallerySize.getWidth();
                            mTitle = gallerySize.getTitle();
                            mLocationY = gallerySize.getPosition();
                            mShareUrl = gallerySize.getShareUrl();
                        }
                    }
                });
    }

    @Override
    protected void initViews() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_ab_back);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void bindListener() {
        mSuperPlayer.setLive(false);
        mSuperPlayer.setOnNetChangeListener(this);//实现网络变化的回调
        mSuperPlayer.play(mUrl);
        mSuperPlayer.setSupportGesture(true);
        mSuperPlayer.setBackgroundColor(getResources().getColor(R.color.background_dark));
        mSuperPlayer.setScaleType(SuperPlayer.SCALETYPE_FITXY);
        mSuperPlayer.setShowTopControl(false);
        mSuperPlayer.setPlayerWH(0, mSuperPlayer.getMeasuredHeight());
        mSuperPlayer.onPrepared(new SuperPlayer.OnPreparedListener() {
            @Override
            public void onPrepared() {
                setPlayerParames();
            }
        })
                .onComplete(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
    }

    private void setPlayerParames() {
        int diff = mWidth - mHeigth;
        int h_diff = mHeigth - mWidth;
        int h;
        int w;
        if (mHeigth > 500 && mWidth > 300 && mWidth < 500) {
            if (h_diff > 150) {
                w = (int) (mWidth * 1.5);
                h = (int) (mHeigth * 1.5);
            } else {
                w = mWidth;
                h = mHeigth;
            }
        } else if ((diff > 150 && diff < 300) || diff == 0 && mWidth < 500) {
            w = mWidth * 2;
            h = mHeigth * 2;
        } else {
            w = 1080;
            h = 1080 * mHeigth / mWidth;
        }

        if (mHeigth < 500 || mWidth < 800) {
            w = 1080;
            h = 1080 * mHeigth / mWidth;
        }

        ViewGroup.LayoutParams m = mSuperPlayer.getLayoutParams();
        m.height = h;
        m.width = w;
        mSuperPlayer.setLayoutParams(m);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSuperPlayer != null) {
            mSuperPlayer.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSuperPlayer != null) {
            mSuperPlayer.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        mSuperPlayer.unregisterNetReceiver();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mSuperPlayer != null) {
                    mSuperPlayer.onDestroy();
                }
            }
        }).start();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mSuperPlayer != null && mSuperPlayer.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mSuperPlayer != null) {
            mSuperPlayer.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.budejie_video_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.budejie_video_collect:
                break;
            case R.id.budejie_video_share:
                ShareUtils.getInstance().shareText(this, mTitle, mShareUrl);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWifi() {

    }

    @Override
    public void onMobile() {

    }

    @Override
    public void onDisConnect() {

    }

    @Override
    public void onNoAvailable() {

    }
}
