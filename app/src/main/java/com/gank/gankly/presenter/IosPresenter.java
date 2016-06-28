package com.gank.gankly.presenter;

import android.app.Activity;

import com.gank.gankly.App;
import com.gank.gankly.R;
import com.gank.gankly.bean.GankResult;
import com.gank.gankly.bean.ResultsBean;
import com.gank.gankly.config.MeiziArrayList;
import com.gank.gankly.network.api.GankApi;
import com.gank.gankly.view.IIosView;
import com.socks.library.KLog;

import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Create by LingYan on 2016-05-24
 */
public class IosPresenter extends BasePresenter<IIosView> {
    private int limit = 20;
    private int mGirlCurPage = 1;
    private int mIosCurPage;
    private boolean isGirlLoadMore;
    private boolean isIosLoadMore;

    public IosPresenter(Activity mActivity, IIosView view) {
        super(mActivity, view);
    }

    public void fetchDate() {
        mIView.showRefresh();
        GankApi.getInstance()
                .getGankService().fetchIosGoods(limit, mIosCurPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GankResult>() {
                    @Override
                    public void onCompleted() {
                        mIView.hideRefresh();
                        mIView.onCompleted();
                        mIosCurPage = mIosCurPage + 1;
                    }

                    @Override
                    public void onError(Throwable e) {
                        mIView.hideRefresh();
                        mIView.onError(e, "");
                    }

                    @Override
                    public void onNext(GankResult gankResult) {
                        toNext(mIosCurPage, gankResult);
                    }
                });
    }

    public void fetchNextIos() {
        if (isIosLoadMore) {
            fetchDate();
        }
    }

    public void fetchIos() {
        mIosCurPage = 1;
        fetchDate();
    }

    public void fetchNextGirl() {
        KLog.d("fetchNextGirl," + isGirlLoadMore + ",mGirlCurPage:" + mGirlCurPage);
        if (isGirlLoadMore) {
            fetchGirlDate(mGirlCurPage);
        }
    }

    public void fetchGirl() {
        mGirlCurPage = 1;
        fetchGirlDate(mGirlCurPage);
    }

    private void fetchGirlDate(final int page) {
        mIView.showRefresh();
        GankApi.getInstance().fetchWelfare(limit, page, new Subscriber<GankResult>() {
            @Override
            public void onCompleted() {
                mIView.hideRefresh();
                mIView.showContent();
                mIView.onCompleted();
                mGirlCurPage = mGirlCurPage + 1;
            }

            @Override
            public void onError(Throwable e) {
                KLog.e("onError，" + e);
                mIView.hideRefresh();
                boolean isNetWork = isNetworkAvailable();
                toError(page, isNetWork, e);
            }

            @Override
            public void onNext(GankResult gankResult) {
                KLog.d("gankResult:" + gankResult.getSize());
                toNext(page, gankResult);
                MeiziArrayList.getInstance().addBeanAndPage(gankResult.getResults(), page);
            }
        });
    }

    private void toError(int page, boolean isNetWork, Throwable e) {
        KLog.d("isNetWork:" + isNetWork + ",page:" + page);
        int size = MeiziArrayList.getInstance().size();
        if (page > 1 || size > 0) {
            int resId = R.string.loading_network_failure;
            if (isNetWork) {
                resId = R.string.tip_server_error;
            }
            mIView.onError(e, App.getAppString(resId));
        } else {
            KLog.d("MeiziArrayList.getInstance().size():" + MeiziArrayList.getInstance().size());
            if (isNetWork) {
                mIView.showError();
            } else {
                mIView.showDisNetWork();
            }
        }
    }

    private void toNext(int page, GankResult gankResult) {
        if (!gankResult.isEmpty()) {
            List<ResultsBean> list = gankResult.getResults();
            if (page == 1) {
                mIView.clear();
                mIView.refillDate(list);
            } else {
                mIView.appendMoreDate(list);
            }
            if (gankResult.getSize() < limit) {
                isGirlLoadMore = false;
                isIosLoadMore = false;
                mIView.hasNoMoreDate();
            } else {
                isGirlLoadMore = true;
                isIosLoadMore = true;
            }
        } else {
            isGirlLoadMore = false;
            isIosLoadMore = false;
            if (page <= 1) {
                mIView.showEmpty();
            } else {
                mIView.hasNoMoreDate();
            }
        }
    }
}
