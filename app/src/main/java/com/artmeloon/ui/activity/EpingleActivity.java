package com.artmeloon.ui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.artmeloon.InstaMaterialApplication;
import com.artmeloon.Utils;
import com.artmeloon.ui.adapter.CustomListAdapter;
import com.artmeloon.ui.model.Epingle;
import com.artmeloon.ui.model.Tableau;
import com.artmeloon.ui.model.User;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import com.artmeloon.R;

/**
 * Created by Miroslaw Stanek on 21.02.15.
 */
public class EpingleActivity extends BaseActivity {
    public static final String ARG_TAKEN_PHOTO_URI = "arg_taken_photo_uri";
    private static final String url = UrlInfo.ip+"meloon/tableau/Api.php?apicall=getheroes"+"&idCurrent=" + BaseDrawerActivity.currentUserId;
    private String UPLOAD_URL = UrlInfo.ip+"meloon/epingle/upload_image/pinphoto.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";
    private String KEY_CREATOR = "id_creator";
    private String KEY_DESCRIPTION= "description";
    int success;
    String tag_json_obj = "json_obj_req";
    private static final String TAG = PublishActivity.class.getSimpleName();

    private List<Tableau> movieList = new ArrayList<Tableau>();
    private CustomListAdapter adapter;
    private ListView listView;
    int idTab;


    View v;


    ImageButton tbFollowers;
    User user;

    EditText description;

    @BindView(R.id.ivPhoto)
    ImageView ivPhoto;

    Epingle epingle;
    private Uri photoUri;
    private int photoSize;
    String ARG_SELECTED_PHOTO= "arg_selected_photo";

    public static void openWithPhotoUri(Activity openingActivity, Uri photoUri) {
        Intent intent = new Intent(openingActivity, PublishActivity.class);
        intent.putExtra(ARG_TAKEN_PHOTO_URI, photoUri);
        openingActivity.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epingle);

        tbFollowers=(ImageButton)findViewById(R.id.tbFollowers);
        listView = (ListView) findViewById(R.id.list);
        description = (EditText) findViewById(R.id.description);





        adapter = new CustomListAdapter(this, movieList, EpingleActivity.this);
        listView.setAdapter(adapter);
        tableauList();

        toolbar.setNavigationIcon(R.drawable.icons8cancel);
        photoSize = getResources().getDimensionPixelSize(R.dimen.publish_photo_thumbnail_size);

        if (savedInstanceState == null) {
            photoUri = getIntent().getParcelableExtra(ARG_TAKEN_PHOTO_URI);
        } else {
            photoUri = savedInstanceState.getParcelable(ARG_TAKEN_PHOTO_URI);
        }
        updateStatusBarColor();

        ivPhoto.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                ivPhoto.getViewTreeObserver().removeOnPreDrawListener(this);
                //loadThumbnailPhoto();
                return true;
            }
        });
        epingle =(Epingle) getIntent().getExtras().getSerializable(ARG_SELECTED_PHOTO);
        user =(User) getIntent().getExtras().getSerializable("user");
        ivPhoto.setScaleX(0);
        ivPhoto.setScaleY(0);
        Picasso.with( this)
                .load(epingle.getEpingle_path())
                .centerCrop()
                .resize(96, 96)
                .into(ivPhoto);

        ivPhoto.animate()
                .scaleX(1.f).scaleY(1.f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(400)
                .setStartDelay(200)
                .start();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Tableau item = (Tableau) listView.getItemAtPosition(position);
                idTab= item.getTableau_id();
                uploadImage( );

            }
        });

    }
    public void onProfileClick() {
        int[] startingLocation = new int[2];
        //v.getLocationOnScreen(startingLocation);
        startingLocation[0] += 516;
        UserProfileActivity.startUserProfileFromLocation(startingLocation, this, BaseDrawerActivity.currentUserId);
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
        //getMenuInflater().inflate(R.menu.menu_editphoto, menu);
        return true;
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

    private void uploadImage() {
        //menampilkan progress dialog
        final ProgressDialog loading = ProgressDialog.show(this, "Uploading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "Response: " + response.toString());

                        try {
                            JSONObject jObj = new JSONObject(response);
                            success = jObj.getInt(TAG_SUCCESS);

                            if (success == 1) {
                                Log.e("v Add", jObj.toString());
                                onProfileClick();

                                Toast.makeText(EpingleActivity.this, jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(EpingleActivity.this, jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //menghilangkan progress dialog
                        loading.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //menghilangkan progress dialog
                        loading.dismiss();

                        //menampilkan toast
//                        Toast.makeText(PublishActivity.this, error.getMessage().toString(), Toast.LENGTH_LONG).show();
                        //  Log.e(TAG, error.getMessage().toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //membuat parameters
                Map<String, String> params = new HashMap<String, String>();

                //menambah parameter yang di kirim ke web servis
                params.put(KEY_CREATOR, String.valueOf(BaseDrawerActivity.currentUserId));
                params.put(KEY_IMAGE, epingle.getEpingle_path());
                params.put(KEY_DESCRIPTION, description.getText().toString().trim());
                params.put("idTableau",String.valueOf(idTab) );

                //kembali ke parameters
                Log.e(TAG, "" + params);
                return params;
            }
        };

        InstaMaterialApplication.getInstance().addToReqQueue(stringRequest, tag_json_obj);
    }


}
