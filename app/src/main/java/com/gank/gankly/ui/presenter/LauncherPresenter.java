package com.gank.gankly.ui.presenter;

import android.app.Activity;

import com.gank.gankly.bean.CheckVersion;
import com.gank.gankly.network.api.DownloadApi;
import com.gank.gankly.ui.view.ILauncher;
import com.gank.gankly.utils.AppUtils;
import com.socks.library.KLog;

import java.io.InputStream;

import rx.Subscriber;
import rx.functions.Action1;

/**
 * Create by LingYan on 2016-06-01
 */
public class LauncherPresenter extends BasePresenter<ILauncher> {
    private DownloadApi mDownloadApi;

    public LauncherPresenter(Activity mActivity, ILauncher view) {
        super(mActivity, view);
        mDownloadApi = new DownloadApi();
    }

    public void checkVersion() {
        mDownloadApi.checkVersion(new Subscriber<CheckVersion>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                KLog.e(e);
            }

            @Override
            public void onNext(CheckVersion checkVersion) {
                int curVersion = AppUtils.getVersionCode(mActivity);
                if (checkVersion.getCode() == curVersion) {
                    mIView.callUpdate(checkVersion);
                }
            }
        });
    }

    public void downloadApk(Action1<InputStream> next, Subscriber subscriber) {
        mDownloadApi.downloadApk(next, subscriber);
    }
}
