package com.example.android.architecture.blueprints.todoapp.tasks;

import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.NavigationView;
import android.support.test.espresso.IdlingResource;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsFragment;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsPresenter;
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String CURRENT_FILTERING_KEY = "CURRENT_FILTERING_KEY";

    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.nav_view) NavigationView mNavigationView;
    @BindView(R.id.contentFrame) FrameLayout mContentFrame;

    private Menu mNavViewMenu;

    private TasksFragment mTasksFragment;
    private TasksPresenter mTasksPresenter;
    private StatisticsFragment mStatisticsFragment;
    private StatisticsPresenter mStatisticsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Set up the toolbar.
        setSupportActionBar(mToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(getString(R.string.app_name));
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mTasksFragment = (TasksFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (mTasksFragment == null) {
            mTasksFragment = TasksFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mTasksFragment, R.id.contentFrame);
        }

        mStatisticsFragment = (StatisticsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (mStatisticsFragment == null) {
            mStatisticsFragment = StatisticsFragment.newInstance();
        }

        // Create the presenter
        mTasksPresenter = new TasksPresenter(
                Injection.provideTasksRepository(getApplicationContext()),
                mTasksFragment,
                Injection.provideSchedulerProvider());

        mStatisticsPresenter = new StatisticsPresenter(
                Injection.provideTasksRepository(getApplicationContext()),
                mStatisticsFragment,
                Injection.provideSchedulerProvider());

        // Set up the navigation drawer.
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        setupDrawerContent(mNavigationView);

        mNavViewMenu = mNavigationView.getMenu();

        // Load previously saved state, if available.
        if (savedInstanceState != null) {
            TasksFilterType currentFiltering = (TasksFilterType) savedInstanceState.getSerializable(CURRENT_FILTERING_KEY);
            if (currentFiltering != null) {
                mTasksPresenter.setFiltering(currentFiltering);
            }
        }
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(CURRENT_FILTERING_KEY, mTasksPresenter.getFiltering());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    if (!menuItem.isChecked()) {
                        switch (menuItem.getItemId()) {
                            case R.id.list_navigation_menu_item:
                                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), mTasksFragment, R.id.contentFrame);
                                uncheckNavMenuItems();
                                menuItem.setChecked(true);
                                break;
                            case R.id.statistics_navigation_menu_item:
                                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), mStatisticsFragment, R.id.contentFrame);
                                uncheckNavMenuItems();
                                menuItem.setChecked(true);
                                break;
                            default:
                                break;
                        }
                    }
                    // Close the navigation drawer when an item is selected.
                    menuItem.setChecked(true);
                    mDrawerLayout.closeDrawers();
                    return true;
                });
    }

    private void uncheckNavMenuItems() {
        int size = mNavViewMenu.size();
        for (int i = 0; i < size; i++) {
            mNavViewMenu.getItem(i).setChecked(false);
        }
    }
}
