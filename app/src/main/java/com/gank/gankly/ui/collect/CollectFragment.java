package com.gank.gankly.ui.collect;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.gank.gankly.R;
import com.gank.gankly.bean.RxCollect;
import com.gank.gankly.data.entity.UrlCollect;
import com.gank.gankly.listener.ItemLongClick;
import com.gank.gankly.mvp.base.FetchFragment;
import com.gank.gankly.mvp.source.LocalDataSource;
import com.gank.gankly.ui.more.MoreActivity;
import com.gank.gankly.ui.web.normal.WebActivity;
import com.gank.gankly.utils.RxUtils;
import com.gank.gankly.widget.DeleteDialog;
import com.gank.gankly.widget.LyRecyclerView;
import com.gank.gankly.widget.LySwipeRefreshLayout;
import com.gank.gankly.widget.MultipleStatusView;
import com.gank.gankly.widget.RecycleViewDivider;
import com.socks.library.KLog;

import java.util.List;

import butterknife.BindView;
import jp.wasabeef.recyclerview.animators.FadeInLeftAnimator;
import rx.Subscriber;

import static android.R.attr.id;

/**
 * 收藏
 * Create by LingYan on 2016-4-25
 * Email:137387869@qq.com
 */
public class CollectFragment extends FetchFragment implements DeleteDialog.DialogListener,
        ItemLongClick, CollectContract.View {
    @BindView(R.id.multiple_status_view)
    MultipleStatusView mMultipleStatusView;
    @BindView(R.id.swipe_refresh)
    LySwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private MoreActivity mActivity;
    private CollectContract.Presenter mPresenter;
    private CollectAdapter mCollectAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_collcet;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MoreActivity) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        RxUtils.getInstance().unCollect(new Subscriber<RxCollect>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                KLog.e(e);
            }

            @Override
            public void onNext(RxCollect rxCollect) {
                if (rxCollect.isCollect()) {
                    onDelete();
                }
            }
        });
    }

    @Override
    protected void initPresenter() {
        mPresenter = new CollectPresenter(LocalDataSource.getInstance(), this);
    }

    @Override
    protected void initValues() {
        mToolbar.setTitle(R.string.mine_my_collect);
        mActivity.setSupportActionBar(mToolbar);
        ActionBar bar = mActivity.getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });
    }

    @Override
    protected void initViews() {
        setSwipeRefreshLayout(mSwipeRefreshLayout);
        initAdapter();
        setRecyclerView();
        initRefresh();
    }

    private void setRecyclerView() {
        mSwipeRefreshLayout.setLayoutManager(new LinearLayoutManager(mActivity));
        mSwipeRefreshLayout.setAdapter(mCollectAdapter);
        mSwipeRefreshLayout.setILyRecycler(new LyRecyclerView.ILyRecycler() {
            @Override
            public void removeRecycle(int pos) {
                long id = mCollectAdapter.getUrlCollect(pos).getId();
                mPresenter.cancelCollect(id);
                mCollectAdapter.deleteItem(pos);
                if (mCollectAdapter.getItemCount() == 0) {
                    showEmpty();
                }
            }

            @Override
            public void onClick(int position) {
                openWebActivity(position);
            }
        });
        mRecyclerView = mSwipeRefreshLayout.getRecyclerView();
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new RecycleViewDivider(mActivity, R.drawable.shape_item_divider));
        mRecyclerView.setItemAnimator(new FadeInLeftAnimator(new OvershootInterpolator(1f)));
        mRecyclerView.getItemAnimator().setAddDuration(500);
        mRecyclerView.getItemAnimator().setRemoveDuration(500);
    }

    private void initAdapter() {
        mCollectAdapter = new CollectAdapter(mActivity);
        mCollectAdapter.setItemLongClick(this);
    }

    @Override
    protected void bindLister() {
        mSwipeRefreshLayout.setOnScrollListener(new LySwipeRefreshLayout.OnSwipeRefRecyclerViewListener() {
            @Override
            public void onRefresh() {
                showRefresh();
                mPresenter.fetchNew();
            }

            @Override
            public void onLoadMore() {
                mPresenter.fetchMore();
            }
        });
    }

    private void initRefresh() {
        mMultipleStatusView.showLoading();
        mPresenter.fetchNew();
    }

    @Override
    public void onLongClick(int position, Object object) {
        UrlCollect mUrlCollect = (UrlCollect) object;
        Bundle bundle = new Bundle();
        bundle.putString(DeleteDialog.CONTENT, mUrlCollect.getComment());
        bundle.putInt(DeleteDialog.ITEM, position);
        DeleteDialog deleteDialog = new DeleteDialog();
        deleteDialog.setListener(this);
        deleteDialog.setArguments(bundle);
        deleteDialog.show(mActivity.getSupportFragmentManager(), DeleteDialog.TAG);
    }

    @Override
    public void onClick(int position, Object object) {

    }

    private void openWebActivity(int position) {
        UrlCollect urlCollect = mCollectAdapter.getUrlCollect(position);
        Bundle bundle = new Bundle();
        bundle.putString(WebActivity.TITLE, urlCollect.getComment());
        bundle.putString(WebActivity.URL, urlCollect.getUrl());
        bundle.putInt(WebActivity.FROM_TYPE, WebActivity.FROM_COLLECT);
        WebActivity.startWebActivity(mActivity, bundle);
    }


    @Override
    public void setAdapterList(List<UrlCollect> list) {
        mCollectAdapter.updateItems(list);
    }

    @Override
    public void appendAdapter(List<UrlCollect> list) {
        mCollectAdapter.addItems(list);
    }

    @Override
    public void onDelete() {
//        mCollectAdapter.deleteItem(mCollectAdapter.getClickItem());
        if (mCollectAdapter.getItemCount() == 0) {
            showEmpty();
        }
    }

    @Override
    public int getItemsCount() {
        return mCollectAdapter.getItemCount();
    }

    @Override
    public void hasNoMoreDate() {
        Snackbar.make(mSwipeRefreshLayout, R.string.tip_no_more_load, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showEmpty() {
        mMultipleStatusView.showEmpty();
    }

    @Override
    public void showDisNetWork() {
        mMultipleStatusView.showNoNetwork();
    }

    @Override
    public void showError() {
        mMultipleStatusView.showError();
    }

    @Override
    public void showLoading() {
        mMultipleStatusView.showLoading();
    }

    @Override
    public void showRefreshError(String errorStr) {

    }

    @Override
    public void showContent() {
        mMultipleStatusView.showContent();
    }

    @Override
    public void showRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onNavigationClick() {
//        int id = mSwipeRefreshLayout.getCurPosition();
        if (id != -1) {
            mPresenter.cancelCollect(id);
        }
    }
}
