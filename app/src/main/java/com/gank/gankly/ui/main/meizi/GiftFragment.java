package com.gank.gankly.ui.main.meizi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gank.gankly.App;
import com.gank.gankly.R;
import com.gank.gankly.RxBus.ChangeThemeEvent.ThemeEvent;
import com.gank.gankly.RxBus.RxBus;
import com.gank.gankly.bean.GiftBean;
import com.gank.gankly.listener.ItemClick;
import com.gank.gankly.presenter.GiftPresenter;
import com.gank.gankly.ui.base.LazyFragment;
import com.gank.gankly.ui.gallery.GalleryActivity;
import com.gank.gankly.ui.main.HomeActivity;
import com.gank.gankly.utils.DisplayUtils;
import com.gank.gankly.utils.StyleUtils;
import com.gank.gankly.view.IGiftView;
import com.gank.gankly.widget.LySwipeRefreshLayout;
import com.gank.gankly.widget.MultipleStatusView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;

/**
 * 清纯妹子
 * Create by LingYan on 2016-05-17
 * Email:137387869@qq.com
 */
public class GiftFragment extends LazyFragment implements ItemClick, IGiftView {
    @BindView(R.id.meizi_swipe_refresh)
    LySwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.multiple_status_view)
    MultipleStatusView mMultipleStatusView;

    private RecyclerView mRecyclerView;
    private GiftAdapter mAdapter;
    private HomeActivity mActivity;

    private int mCurPage = 1;
    private ArrayList<GiftBean> mImageCountList = new ArrayList<>();
    private ProgressDialog mDialog;
    private GiftPresenter mPresenter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_gift;
    }

    public static GiftFragment getInstance() {
        return new GiftFragment();
    }

    @Override
    protected void initPresenter() {
        mPresenter = new GiftPresenter(mActivity, this);
    }

    @Override
    protected void initValues() {
        initRefresh();
        RxBus.getInstance().toObservable(ThemeEvent.class)
                .subscribe(new Consumer<ThemeEvent>() {
                    @Override
                    public void accept(ThemeEvent themeEvent) throws Exception {
                        changeUi();
                    }
                });
    }

    @Override
    protected void initViews() {
        initRecycler();
        setSwipeRefreshLayout(mSwipeRefreshLayout);
    }

    @Override
    protected void bindListener() {
        mAdapter.setOnItemClickListener(this);
    }

    @Override
    protected void initData() {
        initRefresh();
    }

    private void initRefresh() {
        mMultipleStatusView.showLoading();
        onFetchNew();
    }

    private void changeUi() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = mActivity.getTheme();
        theme.resolveAttribute(R.attr.baseAdapterItemBackground, typedValue, true);
        int background = typedValue.data;
        TypedValue textValue = new TypedValue();
        theme.resolveAttribute(R.attr.baseAdapterItemTextColor, textValue, true);
        int textColor = textValue.data;
        theme.resolveAttribute(R.attr.themeBackground, textValue, true);
        int recyclerColor = textValue.data;
        mRecyclerView.setBackgroundColor(recyclerColor);

        int childCount = mRecyclerView.getChildCount();
        for (int childIndex = 0; childIndex < childCount; childIndex++) {
            ViewGroup childView = (ViewGroup) mRecyclerView.getChildAt(childIndex);
            TextView title = (TextView) childView.findViewById(R.id.goods_txt_title);
            title.setTextColor(textColor);
            View rlView = childView.findViewById(R.id.goods_rl_title);
            rlView.setBackgroundColor(background);
        }

        StyleUtils.clearRecyclerViewItem(mRecyclerView);
        StyleUtils.changeSwipeRefreshLayout(mSwipeRefreshLayout);
    }

    private void onFetchNew() {
        mCurPage = 1;
        mPresenter.fetchNew(mCurPage);
    }

    private void onFetchNext() {
        showRefresh();
        mPresenter.fetchNext(mCurPage);
    }

    private void initRecycler() {
        mRecyclerView = mSwipeRefreshLayout.getRecyclerView();
        mSwipeRefreshLayout.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL));

        float leftPadding = DisplayUtils.dp2px(8);// because StaggeredGridLayoutManager left margin
        mRecyclerView.setPadding((int) leftPadding, 0, 0, 0);
        mSwipeRefreshLayout.setOnScrollListener(new LySwipeRefreshLayout.OnSwipeRefRecyclerViewListener() {

            @Override
            public void onRefresh() {
                onFetchNew();
            }

            @Override
            public void onLoadMore() {
                onFetchNext();
            }
        });

        mAdapter = new GiftAdapter(mActivity);
        mSwipeRefreshLayout.setAdapter(mAdapter);
    }


    private void showDialog() {
        if (mDialog == null) {
            mDialog = new ProgressDialog(mActivity);
        }

        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setMessage(App.getAppString(R.string.loading_meizi_images));
        mDialog.setIndeterminate(false);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setProgress(0);
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mPresenter.unSubscribe();
            }
        });

        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    @Override
    public void showEmpty() {

    }

    @Override
    public void showDisNetWork() {

    }

    @Override
    public void showContent() {
        mMultipleStatusView.showContent();
    }

    @Override
    public void showError() {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hasNoMoreDate() {

    }

    @Override
    public void clear() {
        mAdapter.clear();
    }

    @Override
    public void showRefreshError(String errorStr) {

    }

    @Override
    public void refillDate(List<GiftBean> list) {
        mAdapter.updateItems(list);
    }

    @Override
    public void setMax(int max) {
        if (mDialog != null) {
            mDialog.setMax(max);
        }
    }

    @Override
    public void setProgress(int progress) {
        if (mDialog != null) {
            mDialog.setProgress(progress);
        }
    }

    @Override
    public void disDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void setNextPage(int page) {
        this.mCurPage = page;
    }

    @Override
    public void refillImagesCount(List<GiftBean> giftResult) {
        mImageCountList.addAll(giftResult);
    }

    @Override
    public void gotoBrowseActivity(ArrayList<GiftBean> list) {
        Bundle bundle = new Bundle();
        Intent intent = new Intent(mActivity, GalleryActivity.class);
        bundle.putString(GalleryActivity.EXTRA_MODEL, GalleryActivity.EXTRA_GIFT);
        intent.putExtra(GalleryActivity.TAG, bundle);
        intent.putExtra(GalleryActivity.EXTRA_LIST, list);
        mActivity.startActivity(intent);
    }

    @Override
    public void onClick(int position, Object object) {
        showDialog();
        mImageCountList.clear();
        final GiftBean giftBean = (GiftBean) object;
        Observable.create(new ObservableOnSubscribe<GiftBean>() {
            @Override
            public void subscribe(ObservableEmitter<GiftBean> subscriber) throws Exception {
                subscriber.onNext(giftBean);
                subscriber.onComplete();
            }
        })
                .throttleFirst(100, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<GiftBean>() {
                    @Override
                    public void accept(GiftBean giftBean) throws Exception {
                        mPresenter.fetchImagePageList(giftBean.getUrl());
                    }
                });

    }

    public List<GiftBean> getList() {
        return mImageCountList;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (HomeActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    protected void callBackRefreshUi() {

    }
}
