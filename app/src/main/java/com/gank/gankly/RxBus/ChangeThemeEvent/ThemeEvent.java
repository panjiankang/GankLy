package com.gank.gankly.RxBus.ChangeThemeEvent;

/**
 * Create by LingYan on 2016-09-02
 * Email:137387869@qq.com
 */
public class ThemeEvent {
    private boolean isRefreshUi;

    public ThemeEvent(boolean isRefreshUi) {
        this.isRefreshUi = isRefreshUi;
    }

    public boolean isRefreshUi() {
        return isRefreshUi;
    }

    public void setRefreshUi(boolean refreshUi) {
        isRefreshUi = refreshUi;
    }
}
