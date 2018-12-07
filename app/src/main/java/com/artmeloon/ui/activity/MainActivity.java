package com.artmeloon.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.artmeloon.InstaMaterialApplication;
import com.artmeloon.Utils;
import com.artmeloon.ui.adapter.FeedAdapter;
import com.artmeloon.ui.adapter.FeedItemAnimator;
import com.artmeloon.ui.model.Epingle;
import com.artmeloon.ui.model.User;
import com.artmeloon.ui.view.FeedContextMenu;
import com.artmeloon.ui.view.FeedContextMenuManager;
import com.kosalgeek.android.json.JsonConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import com.artmeloon.R;


public class MainActivity extends BaseDrawerActivity implements FeedAdapter.OnFeedItemClickListener,
        FeedContextMenu.OnFeedContextMenuItemClickListener {
    public static final String ACTION_SHOW_LOADING_ITEM = "action_show_loading_item";

    String url =  UrlInfo.ip+"AfficherEpingles.php";
    private static final int ANIM_DURATION_TOOLBAR = 300;
    private static final int ANIM_DURATION_FAB = 400;
    public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";

    ArrayList<Epingle> array_list;
    ProgressDialog PD;

    @BindView(R.id.rvFeed)
    RecyclerView rvFeed;
    @BindView(R.id.btnCreate)
    FloatingActionButton fabCreate;
    @BindView(R.id.content)
    CoordinatorLayout clContent;
    @BindView(R.id.searchText)
    EditText searchText;

    private FeedAdapter feedAdapter;
    private boolean pendingIntroAnimation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       /* if(AccessToken.getCurrentAccessToken() == null){
            goLoginScreen();
        }*/
       // BaseDrawerActivity.currentUserId = 1;
        setupFeed();
        //BaseDrawerActivity.currentUserId = (int) getIntent().getExtras().getSerializable("currentUser");

        if (savedInstanceState == null) {
            pendingIntroAnimation = true;
        } else {
            feedAdapter.updateItems(false);
        }
    }
    private void goLoginScreen() {
        Intent intent = new Intent(this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setupFeed() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvFeed.setLayoutManager(linearLayoutManager);
        ReadEpinglesFromDB();
        rvFeed.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                FeedContextMenuManager.getInstance().onScrolled(recyclerView, dx, dy);
            }
        });
        rvFeed.setItemAnimator(new FeedItemAnimator());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (ACTION_SHOW_LOADING_ITEM.equals(intent.getAction())) {
            showFeedLoadingItemDelayed();
        }
    }

    private void showFeedLoadingItemDelayed() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rvFeed.smoothScrollToPosition(0);
                feedAdapter.showLoadingView();
            }
        }, 500);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (pendingIntroAnimation) {
            pendingIntroAnimation = false;
            startIntroAnimation();
        }
        return true;
    }

    private void startIntroAnimation() {
        fabCreate.setTranslationY(2 * getResources().getDimensionPixelOffset(R.dimen.btn_fab_size));

        int actionbarSize = Utils.dpToPx(56);
        getToolbar().setTranslationY(-actionbarSize);
        getIvLogo().setTranslationY(-actionbarSize);
        getInboxMenuItem().getActionView().setTranslationY(-actionbarSize);

        getToolbar().animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(300);
        getIvLogo().animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(400);
        getInboxMenuItem().getActionView().animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startContentAnimation();
                    }
                })
                .start();
    }

    private void startContentAnimation() {
        fabCreate.animate()
                .translationY(0)
                .setInterpolator(new OvershootInterpolator(1.f))
                .setStartDelay(300)
                .setDuration(ANIM_DURATION_FAB)
                .start();
       feedAdapter.updateItems(true);
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
    public void onProfileClick(View v) {
        /*int[] startingLocation = new int[2];
       // v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2;
       // UserProfileActivity.startUserProfileFromLocation(startingLocation, this,);
        overridePendingTransition(0, 0);*/
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

    @OnClick(R.id.btnCreate)
    public void onTakePhotoClick() {
        SocialMediaPublishActivity.openWithPhotoUri(this,Uri.parse("http://stackoverflow.com"));

    }

    public void showLikedSnackbar() {
        Snackbar.make(clContent, "Liked!", Snackbar.LENGTH_SHORT).show();
    }
   /* private void ReadEpinglesFromDB() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("android", response);

                        ArrayList<Epingle> epingles = new JsonConverter<Epingle>()
                                .toArrayList(response, Epingle.class);

                        feedAdapter = new FeedAdapter(getApplicationContext(), epingles);
                        rvFeed.setAdapter(feedAdapter);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error != null){
                            Log.d("android", error.toString());
                            Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

    }*/
   private void ReadEpinglesFromDB() {

       String url1 = UrlInfo.ip+"AfficherEpingles.php?idCurrent=" + BaseDrawerActivity.currentUserId;
       JsonObjectRequest jreq = new JsonObjectRequest(Request.Method.GET, url1, null, new Response.Listener<JSONObject>(){
           @Override
           public void onResponse(JSONObject response) {
               try {
                   int success = response.getInt("success");
                   if (success == 1) {


                       JSONArray ja = response.getJSONArray("epingles");
                       array_list=new ArrayList<Epingle>() ;
                       for (int i = 0; i < ja.length(); i++) {
                           JSONObject jobj = ja.getJSONObject(i);
                           Epingle epingle=new Epingle();
                           epingle.setEpingle_id(Integer.parseInt(jobj.getString("epingle_id")));
                           epingle.setId_creator(Integer.parseInt(jobj.getString("id_creator")));
                           epingle.setDescription(jobj.getString("description"));
                           epingle.setEpingle_path(jobj.getString("epingle_path"));
                           epingle.setIdTableau(Integer.parseInt(jobj.getString("tableau_id")));

                               User usertest =new User();
                               usertest.setProfil_id(Integer.parseInt(jobj.getString("iduser")));
                               usertest.setPrenom(jobj.getString("prenom"));
                                usertest.setNom(jobj.getString("nom"));
                               usertest.setPhotoPath(jobj.getString("photoPath"));
                               usertest.setFollowing(jobj.getInt("following"));
                               epingle.setUser(usertest);

                           array_list.add(epingle);
                       } // for loop ends

                       feedAdapter = new FeedAdapter(getApplicationContext(), array_list);
                       rvFeed.setAdapter(feedAdapter);



                   } // if ends
               } catch (JSONException e) {
                   e.printStackTrace();

               }
           }
       }, new Response.ErrorListener() {
           @Override
           public void onErrorResponse(VolleyError error) {

           }
       });
       // Adding request to request queue
       InstaMaterialApplication.getInstance().addToReqQueue(jreq);
   }


    public void OnSearchEpingle(View view){
        String tags = searchText.getText().toString();

        BackgroundSearchEpingle backgroundSearchEpingle = new BackgroundSearchEpingle(this);
        backgroundSearchEpingle.execute(tags);
    }
    class BackgroundSearchEpingle extends AsyncTask<String,Void,String> {
        Context context;

        public BackgroundSearchEpingle(Context context){
            this.context=context;
        }

        @Override
        protected String doInBackground(String...params){

            String login_url="http://172.16.250.175/searchEpingle.php";
            try{
                String tags=params[0];
                URL url=new URL(login_url);
                HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream=httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String post_data= URLEncoder.encode("tag","UTF-8")+"="+URLEncoder.encode(tags,"UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream=httpURLConnection.getInputStream();
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
                String result="";
                String line="";

                while((line=bufferedReader.readLine())!=null){
                    result+=line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                return result;
            }catch(MalformedURLException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute(){
        }

        @Override
        protected void onPostExecute(String result){
            ArrayList<Epingle> epingles = new JsonConverter<Epingle>()
                    .toArrayList(result, Epingle.class);

            Log.d("0",epingles.toString());
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            rvFeed.setLayoutManager(linearLayoutManager);
            feedAdapter = new FeedAdapter(getApplicationContext(), epingles);
            rvFeed.setAdapter(feedAdapter);
            rvFeed.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    FeedContextMenuManager.getInstance().onScrolled(recyclerView, dx, dy);
                }
            });
            rvFeed.setItemAnimator(new FeedItemAnimator());
            //Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Void...values){
            super.onProgressUpdate(values);
        }

    }
}