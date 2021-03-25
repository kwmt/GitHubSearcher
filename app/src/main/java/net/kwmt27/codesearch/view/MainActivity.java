package net.kwmt27.codesearch.view;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import android.text.TextUtils;
import android.view.View;

import com.roughike.bottombar.BottomBar;

import net.kwmt27.codesearch.ModelLocator;
import net.kwmt27.codesearch.R;
import net.kwmt27.codesearch.analytics.AnalyticsManager;
import net.kwmt27.codesearch.entity.GithubRepoEntity;
import net.kwmt27.codesearch.presenter.main.IMainPresenter;
import net.kwmt27.codesearch.presenter.main.MainPresenter;
import net.kwmt27.codesearch.util.Logger;
import net.kwmt27.codesearch.util.ToastUtil;
import net.kwmt27.codesearch.view.customtabs.ChromeCustomTabs;
import net.kwmt27.codesearch.view.events.EventListFragment;
import net.kwmt27.codesearch.view.repolist.RepositoryListFragment;
import net.kwmt27.codesearch.view.top.TopFragment;

public class MainActivity extends BaseActivity implements MainPresenter.IMainView {

    private boolean mSignedIn = false;
    private boolean isAddedAdOfEvent = false;
    private boolean isAddedAdOfRepository = false;

    private final static int INITIAL_TAB_RESID = R.id.tab_timeline;

    private IMainPresenter mMainPresenter;
    private boolean mIsCustomTabsServiceSuccess = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIsCustomTabsServiceSuccess = ChromeCustomTabs.bindService(this);

        mMainPresenter = new MainPresenter(this);
        mMainPresenter.onCreate(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        if(mIsCustomTabsServiceSuccess) {
            ChromeCustomTabs.unbind(this);
        }
        super.onDestroy();
    }

    private void switchScreen() {
        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);

        String token = ModelLocator.getLoginModel().getAccessToken();
        if (TextUtils.isEmpty(token)) {
            replaceFragment(TopFragment.newInstance(), false, 0);
            bottomBar.setVisibility(View.GONE);
            return;
        }

        bottomBar.setVisibility(View.VISIBLE);
        bottomBar.selectTabWithId(INITIAL_TAB_RESID);
        mSignedIn = true;

        bottomBar.setOnTabSelectListener(tabId -> {
            switch (tabId) {
                case R.id.tab_timeline: {
                    AnalyticsManager.getInstance(this).sendClickButton(AnalyticsManager.Param.Screen.MAIN, AnalyticsManager.Param.Widget.TIMELINE_TAB);
                    // FIXME: findFragmentByTagがnullになるので、常にnewInstanceされる
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(EventListFragment.TAG);
                    getSupportFragmentManager().executePendingTransactions();
                    if (fragment == null) {
                        fragment = EventListFragment.newInstance(isAddedAdOfEvent);
                    }
                    replaceFragmentWithAnimate(fragment, true, R.string.title_timeline_list, EventListFragment.TAG);
                    ((MainFragment) fragment).moveToTop();
                    isAddedAdOfEvent = true;
                }
                break;

                case R.id.tab_repository: {
                    AnalyticsManager.getInstance(this).sendClickButton(AnalyticsManager.Param.Screen.MAIN, AnalyticsManager.Param.Widget.REPOSITORY_TAB);
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(RepositoryListFragment.TAG);
                    getSupportFragmentManager().executePendingTransactions();
                    if (fragment == null) {
                        fragment = RepositoryListFragment.newInstance(isAddedAdOfRepository);
                    }
                    replaceFragmentWithAnimate(fragment, true, R.string.title_repository_list, RepositoryListFragment.TAG);
                    ((MainFragment) fragment).moveToTop();
                    isAddedAdOfRepository = true;
                }
                break;
//                case R.id.tab_favorites:
//                    replaceFragmentWithAnimate(eventListFragment, true, R.string.title_favorite_list);
//                    break;
            }
        });

        bottomBar.setOnTabReselectListener(tabId -> {
            switch (tabId) {
                case R.id.tab_timeline:
                    AnalyticsManager.getInstance(this).sendClickButton(AnalyticsManager.Param.Screen.MAIN, AnalyticsManager.Param.Widget.TIMELINE_TAB);
                    moveToTop(EventListFragment.TAG);
                break;
                case R.id.tab_repository:
                    AnalyticsManager.getInstance(this).sendClickButton(AnalyticsManager.Param.Screen.MAIN, AnalyticsManager.Param.Widget.REPOSITORY_TAB);
                    moveToTop(RepositoryListFragment.TAG);
                break;
//                case R.id.tab_favorites:
//                    break;
            }
        });
    }

    private void moveToTop(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if(fragment == null) { return; }
        ((MainFragment) fragment).moveToTop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d("onResume");
        if (mSignedIn) {
            return;
        }
        switchScreen();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isAddedAdOfEvent = false;
        isAddedAdOfRepository = false;
    }

    private void replaceFragment(Fragment fragment, boolean showToolBar, int titleResId) {
        replaceFragment(fragment, showToolBar, titleResId, false, null);
    }

    private void replaceFragmentWithAnimate(Fragment fragment, boolean showToolBar, int titleResId, String tag) {
        replaceFragment(fragment, showToolBar, titleResId, true, tag);
    }

    private void replaceFragment(Fragment fragment, boolean showToolBar, int titleResId, boolean animate, String tag) {
        int visibility = showToolBar ? View.VISIBLE : View.GONE;
        findViewById(R.id.toolbar).setVisibility(visibility);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (titleResId != 0) {
                actionBar.setTitle(titleResId);
            }
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (animate) {
            ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        }
        ft.replace(R.id.container, fragment, tag).commit();
    }


    @Override
    public void setupComponents(String url, GithubRepoEntity githubRepoEntity) {
        setUpActionBar(false);
        switchScreen();

        if(url != null) {
            ChromeCustomTabs.open(this, url, githubRepoEntity, false);
            return;
        }
    }

    @Override
    public void showError(String message) {
        ToastUtil.show(this, message);
    }

}
