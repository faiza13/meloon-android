package io.github.froger.instamaterial.ui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.Utils;
import io.github.froger.instamaterial.ui.adapter.SingleFeed;
import io.github.froger.instamaterial.ui.adapter.SingleFeedItemAnimator;
import io.github.froger.instamaterial.ui.model.Epingle;
import io.github.froger.instamaterial.ui.model.User;
import io.github.froger.instamaterial.ui.view.FeedContextMenu;
import io.github.froger.instamaterial.ui.view.FeedContextMenuManager;


public class DetailPhotoActivity  extends AppCompatActivity implements SingleFeed.OnFeedItemClickListener, FeedContextMenu.OnFeedContextMenuItemClickListener  {


    public static final String ARG_SELECTED_PHOTO = "arg_selected_photo";
    private static final int ANIM_DURATION_TOOLBAR = 300;
    private static final int ANIM_DURATION_FAB = 400;

    RecyclerView rvFeed;
    private SingleFeed feedAdapter;
     ImageView ivFeedCenter;
     ImageView ivFeedBottom;
    Epingle epingle;
    MenuItem pushpin;
    android.support.v7.widget.Toolbar toolbar;
    CoordinatorLayout content;
    User currrentUser;

    public static void openWithPhotoUri(Activity openingActivity, Epingle epingle, User user) {
        Intent intent = new Intent(openingActivity, DetailPhotoActivity.class);
        intent.putExtra(ARG_SELECTED_PHOTO, epingle);
        intent.putExtra("current user", user);
        openingActivity.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_photo);
        final Epingle selectedPhotoId =(Epingle) getIntent().getExtras().getSerializable(ARG_SELECTED_PHOTO);
        currrentUser =(User) getIntent().getExtras().getSerializable("current user");

        toolbar=(android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.icons8back);

          rvFeed=(RecyclerView) findViewById(R.id.rv);
        setupFeed();
         feedAdapter.updateItems(true);


    }


    private void startIntroAnimation() {
        content=(CoordinatorLayout)findViewById(R.id.content);

        int actionbarSize = Utils.dpToPx(56);
        toolbar.setTranslationY(-actionbarSize);
       // getIvLogo().setTranslationY(-actionbarSize);

        toolbar.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(300);

    }




    private void setupFeed() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        rvFeed.setLayoutManager(linearLayoutManager);
        final Epingle selectedPhotoId =(Epingle) getIntent().getExtras().getSerializable(ARG_SELECTED_PHOTO);
        currrentUser =(User) getIntent().getExtras().getSerializable("current user");


        feedAdapter = new SingleFeed(this,selectedPhotoId,currrentUser);
        feedAdapter.setOnFeedItemClickListener(this);
        rvFeed.setAdapter(feedAdapter);
        rvFeed.setItemAnimator(new SingleFeedItemAnimator());
    }

    @Override
    public void onProfileClick(View v) {
        int[] startingLocation = new int[2];
        //v.getLocationOnScreen(startingLocation);
        startingLocation[0] += 516;
        UserProfileActivity.startUserProfileFromLocation(startingLocation, this,currrentUser.getProfil_id());
        overridePendingTransition(0, 0);

    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateStatusBarColor() {
        if (Utils.isAndroid5()) {
            getWindow().setStatusBarColor(0xff888888);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);


        startIntroAnimation();

        return true;
    }
    @Override

    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem register = menu.findItem(R.id.edit);
        if (currrentUser.getProfil_id()== BaseDrawerActivity.currentUserId){
            register.setVisible(true);

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.edit) {
            final Intent intent = new Intent(this, EditActivity.class);
            final Epingle selectedPhotoId =(Epingle) getIntent().getExtras().getSerializable(ARG_SELECTED_PHOTO);

            intent.putExtra(ARG_SELECTED_PHOTO, selectedPhotoId);
            startActivity(intent);
            overridePendingTransition(0, 0);
            return true;
        }
        if (item.getItemId() == R.id.pushpin) {
            final Intent intent = new Intent(this, EpingleActivity.class);
            final Epingle selectedPhotoId =(Epingle) getIntent().getExtras().getSerializable(ARG_SELECTED_PHOTO);

            intent.putExtra(ARG_SELECTED_PHOTO, selectedPhotoId);
            startActivity(intent);
            overridePendingTransition(0, 0);
            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void bringMainActivityToTop() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(MainActivity.ACTION_SHOW_LOADING_ITEM);
        startActivity(intent);


    }

    @Override
    public void onCommentsClick(View v, int position) {
        final Intent intent = new Intent(this, CommentsActivity.class);
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        intent.putExtra(CommentsActivity.ARG_DRAWING_START_LOCATION, startingLocation[1]);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onMoreClick(View v, int itemPosition) {
        FeedContextMenuManager.getInstance().toggleContextMenuFromView(v, itemPosition, this);
    }


    @Override
    public void onReportClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onSharePhotoClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();

    }

    @Override
    public void onCopyShareUrlClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onCancelClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

}
