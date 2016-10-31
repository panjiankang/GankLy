package com.gank.gankly.mvp.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gank.gankly.App;
import com.gank.gankly.data.entity.ReadHistory;
import com.gank.gankly.data.entity.ReadHistoryDao;
import com.gank.gankly.data.entity.UrlCollect;
import com.gank.gankly.data.entity.UrlCollectDao;
import com.gank.gankly.mvp.BaseModel;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

/**
 * Create by LingYan on 2016-10-24
 * Email:137387869@qq.com
 */

public class LocalDataSource extends BaseModel {
    @Nullable
    private static LocalDataSource INSTANCE = null;

    @NonNull
    private UrlCollectDao mUrlCollectDao;

    @NonNull
    private ReadHistoryDao mReadHistoryDao;

    public static LocalDataSource getInstance() {
        if (INSTANCE == null) {
            synchronized (LocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LocalDataSource();
                }
            }
        }
        return INSTANCE;
    }

    private LocalDataSource() {
        mUrlCollectDao = App.getDaoSession().getUrlCollectDao();
        mReadHistoryDao = App.getDaoSession().getReadHistoryDao();
    }

    public Observable<List<UrlCollect>> getCollect(final int offset, final int limit) {
        return Observable.create(new OnSubscribe<List<UrlCollect>>() {
            @Override
            public void call(Subscriber<? super List<UrlCollect>> subscriber) {
                QueryBuilder<UrlCollect> queryBuilder = mUrlCollectDao.queryBuilder();
                queryBuilder.orderDesc(UrlCollectDao.Properties.Date);
                queryBuilder.offset(offset).limit(limit);
                subscriber.onNext(queryBuilder.list());
                subscriber.onCompleted();
            }
        });
    }

    public Observable<String> cancelCollect(final long id) {
        return toObservable(Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                mUrlCollectDao.deleteByKey(id);
                subscriber.onNext("取消成功");
                subscriber.onCompleted();
            }
        }));
    }

    public Observable<List<ReadHistory>> selectReadHistory(final int offset, final int limit) {
        return toObservable(Observable.create(new OnSubscribe<List<ReadHistory>>() {
            @Override
            public void call(Subscriber<? super List<ReadHistory>> subscriber) {
                QueryBuilder<ReadHistory> queryBuilder = mReadHistoryDao.queryBuilder();
                queryBuilder.orderDesc(ReadHistoryDao.Properties.Date);
                queryBuilder.offset(offset).limit(limit);
                subscriber.onNext(queryBuilder.list());
                subscriber.onCompleted();
            }
        }));
    }

    public Observable<Long> insertCollect(final UrlCollect collect) {
        return toObservable(Observable.create(new OnSubscribe<Long>() {

            @Override
            public void call(Subscriber<? super Long> subscriber) {
                long rasId = mUrlCollectDao.insert(collect);
                subscriber.onNext(rasId);
                subscriber.onCompleted();
            }
        }));
    }

    public Observable<Long> insertReadHistory(final ReadHistory history) {
        return toObservable(Observable.create(new OnSubscribe<Long>() {

            @Override
            public void call(Subscriber<? super Long> subscriber) {
                long rasId = mReadHistoryDao.insert(history);
                subscriber.onNext(rasId);
                subscriber.onCompleted();
            }
        }));
    }

    public Observable<List<UrlCollect>> findUrlCollect(final String url) {
        return toObservable(Observable.create(new OnSubscribe<List<UrlCollect>>() {
            @Override
            public void call(Subscriber<? super List<UrlCollect>> subscriber) {
                QueryBuilder<UrlCollect> query = mUrlCollectDao.queryBuilder();
                List<UrlCollect> list = query.where(UrlCollectDao.Properties.Url.eq(url)).list();
                subscriber.onNext(list);
                subscriber.onCompleted();
            }
        }));
    }

    public Observable<List<ReadHistory>> findReadHistory(final String url) {
        return toObservable(Observable.create(new OnSubscribe<List<ReadHistory>>() {
            @Override
            public void call(Subscriber<? super List<ReadHistory>> subscriber) {
                QueryBuilder<ReadHistory> query = mReadHistoryDao.queryBuilder();
                List<ReadHistory> list = query.where(ReadHistoryDao.Properties.Url.eq(url)).list();
                subscriber.onNext(list);
                subscriber.onCompleted();
            }
        }));
    }
}