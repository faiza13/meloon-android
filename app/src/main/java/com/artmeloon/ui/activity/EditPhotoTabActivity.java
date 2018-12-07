package com.artmeloon.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.artmeloon.InstaMaterialApplication;
import com.artmeloon.Utils;
import com.artmeloon.ui.adapter.CustomListAdapter;
import com.artmeloon.ui.model.Epingle;
import com.artmeloon.ui.model.Tableau;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.artmeloon.R;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class EditPhotoTabActivity extends AppCompatActivity {
    public static final String ARG_TAKEN_PHOTO_URI = "arg_taken_photo_uri";
    private static final String url = UrlInfo.ip+"meloon/tableau/Api.php?apicall=getheroes"+"&idCurrent=" + BaseDrawerActivity.currentUserId;
    MenuItem ok;
    LinearLayout root;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";
    private String KEY_CREATOR = "id_creator";
    private String KEY_DESCRIPTION= "description";
    int success;
    String tag_json_obj = "json_obj_req";
    private static final String TAG = EditPhotoTabActivity.class.getSimpleName();

    private List<Tableau> movieList = new ArrayList<Tableau>();
    private CustomListAdapter adapter;
    private ListView listView;
    int idTab;
    android.support.v7.widget.Toolbar toolbar;



    View v;


    ImageButton tbFollowers;

    EditText description;

    ImageView ivPhoto;
    Uri filePath;
    Bitmap bitmap,decoded;
    int bitmap_size = 96;

    private boolean propagatingToggleState = false;
    private Uri photoUri;
    private int photoSize;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_photo_tab);
        tbFollowers=(ImageButton)findViewById(R.id.tbFollowers);
        listView = (ListView) findViewById(R.id.list);
        description = (EditText) findViewById(R.id.description);
        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        toolbar=(android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.icons8cancel);




        adapter = new CustomListAdapter(this, movieList, EditPhotoTabActivity.this);
        listView.setAdapter(adapter);
        tableauList();


        photoSize = getResources().getDimensionPixelSize(R.dimen.publish_photo_thumbnail_size);

        updateStatusBarColor();

        final Epingle epingle =(Epingle) getIntent().getExtras().getSerializable("EPINGLE");

        ivPhoto.setScaleX(0);
        ivPhoto.setScaleY(0);
        Picasso.with(EditPhotoTabActivity.this)
                .load(epingle.getEpingle_path())
                .resize(photoSize, photoSize)
                .transform(new RoundedCornersTransformation(3,2))
                .into(ivPhoto);


        ivPhoto.animate()
                .scaleX(1.f).scaleY(1.f)

                .setInterpolator(new OvershootInterpolator())
                .setDuration(400)
                .setStartDelay(200)
                .start();
        description.setText(epingle.getDescription());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Tableau item = (Tableau) listView.getItemAtPosition(position);
                idTab= item.getTableau_id();
                Intent intent=new Intent();
                intent.putExtra("TABLEAU",item);
                setResult(2,intent);
                finish();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateStatusBarColor() {
        if (Utils.isAndroid5()) {
            getWindow().setStatusBarColor(0xff888888);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editphoto, menu);
        ok=menu.findItem(R.id.ok);
        ok.setActionView(R.layout.menu_item_check);

        startIntroAnimation();

        return true;
    }
    private void startIntroAnimation() {
        root=(LinearLayout) findViewById(R.id.content);

        final int screenHeight = Utils.getScreenHeight(root.getContext());
      root.setTranslationY(screenHeight);


        int actionbarSize = Utils.dpToPx(56);
        toolbar.setTranslationY(-actionbarSize);
        // getIvLogo().setTranslationY(-actionbarSize);
        ok.getActionView().setTranslationY(-actionbarSize);

        toolbar.animate()
                .translationY(0)
                .setDuration(300)
                .setStartDelay(300);
        ok.getActionView().animate()
                .translationY(0)
                .setDuration(300)
                .setStartDelay(400)
                .start();
        root.animate()
                .translationY(0)
                .setDuration(800)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //dispatchAddFinished(holder);
                    }
                })
                .start();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_publish) {
            bringMainActivityToTop();
            return true;
        } else {
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_TAKEN_PHOTO_URI, photoUri);
    }





    private  void tableauList(){

        JsonObjectRequest jreq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String error = response.getString("error");
                    if (error == "false") {
                        JSONArray ja = response.getJSONArray("heroes");
                        for (int i = 0; i < ja.length(); i++) {
                            JSONObject jobj = ja.getJSONObject(i);
                            Tableau movie = new Tableau();
                            movie.setNom(jobj.getString("nom"));

                            //movie.setThumbnailUrl(obj.getString("image"));

                            movie.setTableau_id(jobj.getInt("tableau_id"));


                            movieList.add(movie);
                        } // for loop ends

                        adapter.notifyDataSetChanged();


                    } // if ends
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());			}
        });
        // Adding request to request queue
        InstaMaterialApplication.getInstance().addToReqQueue(jreq);
    }





}
