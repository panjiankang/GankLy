package com.gank.gankly.presenter.impl;

import android.app.Activity;

import com.gank.gankly.bean.GankResult;
import com.gank.gankly.bean.ResultsBean;
import com.gank.gankly.config.MeiziArrayList;
import com.gank.gankly.model.BaseModel;
import com.gank.gankly.model.impl.MeiziModelImpl;
import com.gank.gankly.presenter.BaseAsynDataSource;
import com.gank.gankly.presenter.ViewControl;
import com.gank.gankly.utils.CrashUtils;
import com.gank.gankly.view.IMeiziView;
import com.socks.library.KLog;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Create by LingYan on 2016-07-13
 * Email:137387869@qq.com
 */
public class MeiziPresenterImpl extends BaseAsynDataSource<IMeiziView<List<ResultsBean>>> {
    private BaseModel mModel;
    private ViewControl mViewControl;

    public MeiziPresenterImpl(Activity mActivity, IMeiziView<List<ResultsBean>> view) {
        super(mActivity, view);
        mModel = new MeiziModelImpl();
        mViewControl = new ViewControl();
    }

    @Override
    public void fetchNew() {
        initFirstPage();
        fetchData();
    }

    @Override
    public void fetchMore() {
        if (isMore()) {
            mIView.showRefresh();
            fetchData();
        }
    }

    @Override
    public void fetchData() {
        final int page = getNextPage();
        final int limit = getLimit();
        mModel.fetchData(page, limit, new Observer<GankResult>() {
            @Override
            public void onError(Throwable e) {
                KLog.e(e);
                CrashUtils.crashReport(e);
                mIView.hideRefresh();
                mViewControl.onError(page, isFirst(), isNetworkAvailable(), mIView);
            }

            @Override
            public void onComplete() {
                mIView.hideRefresh();
                mIView.showContent();
                int nextPage = page + 1;
                setNextPage(nextPage);
                setFirst(false);
            }

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(GankResult gankResult) {
                mViewControl.onNext(page, limit, gankResult.getResults(), mIView, new ViewControl.CallBackViewShow() {
                    @Override
                    public void hasMore(boolean more) {
                        setHasMore(more);
                    }
                });
                MeiziArrayList.getInstance().addBeanAndPage(gankResult.getResults(), page);
            }
        });
    }
}
