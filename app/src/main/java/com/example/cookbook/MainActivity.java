package com.example.cookbook;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cookbook.fragments.CookbookFragment;
import com.example.cookbook.fragments.GroceriesFragment;
import com.example.cookbook.fragments.PlannerFragment;
import com.example.cookbook.fragments.ProfileFragment;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_CURRENT_PAGE = "current_page";

    //Tags for each fragment page
    private static final String TAG_COOKBOOK = "page_cookbook";
    private static final String TAG_GROCERIES = "page_groceries";
    private static final String TAG_PLANNER = "page_planner";
    private static final String TAG_PROFILE = "page_profile";

//    private static final String TAG_RECIPE_DETAIL = "page_recipe_detail";

    //Drawer Menu and bottom bar
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigation;
    private int currentPageId = R.id.nav_cookbook;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //find by id
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();


        //The drawer and the bottom bar both just call showPage().
        // Returning true tells them to show the tapped item as selected.
        navigationView.setNavigationItemSelectedListener(item -> {
            showPage(item.getItemId());
            return true;
        });
        bottomNavigation.setOnItemSelectedListener(item -> {
            showPage(item.getItemId());
            return true;
        });

        //Window insets: the sizes of the status bar, navigation bar and camera cut-out.
        final AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        final View fragmentContainer = findViewById(R.id.fragmentContainer);

        final View drawerHeader = navigationView.getHeaderView(0);
        final int drawerHeaderPaddingTop = drawerHeader.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            appBarLayout.setPadding(bars.left, bars.top, bars.right, 0);
            fragmentContainer.setPadding(bars.left, 0, bars.right, 0);
            drawerHeader.setPadding(drawerHeader.getPaddingLeft(), drawerHeaderPaddingTop + bars.top,
                    drawerHeader.getPaddingRight(), drawerHeader.getPaddingBottom());
            return insets;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, new CookbookFragment(), TAG_COOKBOOK)
                    .add(R.id.fragmentContainer, new GroceriesFragment(), TAG_GROCERIES)
                    .add(R.id.fragmentContainer, new PlannerFragment(), TAG_PLANNER)
                    .add(R.id.fragmentContainer, new ProfileFragment(), TAG_PROFILE) //Added about page
                    .commitNow();
        } else {
            currentPageId = savedInstanceState.getInt(KEY_CURRENT_PAGE, R.id.nav_cookbook);
        }
        showPage(currentPageId);
    }
    //Remember the current page, so a rotation doesn't jump back to the Main Page.
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_PAGE, currentPageId);
    }

    private void showPage(int pageId) {
        currentPageId = pageId;

        //Show this page's Fragment and hide the other two. show/hide (instead of
        // replace) keeps each page alive, with its scroll position and selections.
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        String visibleTag = tagForPage(pageId);
        for (String tag : new String[]{TAG_COOKBOOK, TAG_GROCERIES, TAG_PLANNER, TAG_PROFILE}) {
            Fragment page = fragmentManager.findFragmentByTag(tag);
            if (page == null) {
                continue;
            }
            if (tag.equals(visibleTag)) {
                transaction.show(page);
            } else {
                transaction.hide(page);
            }
        }
        transaction.commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titleForPage(pageId));
        }

        navigationView.setCheckedItem(pageId);
        bottomNavigation.getMenu().findItem(pageId).setChecked(true);
        drawerLayout.closeDrawer(GravityCompat.START);

    }

    //Toolbar title for each page.
    private int titleForPage(int pageId) {
        if (pageId == R.id.nav_groceries) {
            return R.string.title_groceries;
        } else if (pageId == R.id.nav_planner) {
            return R.string.title_planner;
        } else if (pageId == R.id.nav_profile){
            return R.string.title_profile;
        }
        return R.string.title_cookbook;
    }

    //Which Fragment tag belongs to a page.
    private String tagForPage(int pageId) {
        if (pageId == R.id.nav_groceries) {
            return TAG_GROCERIES;
        } else if (pageId == R.id.nav_planner) {
            return TAG_PLANNER;
        }else if (pageId == R.id.nav_profile){
            return TAG_PROFILE; //Added About page
        }
        return TAG_COOKBOOK;
    }
}