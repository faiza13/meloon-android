package io.github.froger.instamaterial.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.github.froger.instamaterial.InstaMaterialApplication;
import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.Utils;
import io.github.froger.instamaterial.ui.model.Epingle;
import io.github.froger.instamaterial.ui.model.Tableau;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class EditActivity extends AppCompatActivity {
     String idTab;
    private   String url,EDIT_URL,DELETE_URL;
    String headUrl=UrlInfo.ip+"/meloon";

    String ARG_SELECTED_PHOTO= "arg_selected_photo";
     ImageView ivPhoto,thumbnail;
    EditText description;
    TextView title;
    int photoSize=60;

    LinearLayout delete;
    CoordinatorLayout root;
    android.support.v7.widget.Toolbar toolbar;
    private static final int ANIM_DURATION_TOOLBAR = 300;
    private static final int ANIM_DURATION_FAB = 400;
    Button check;
    MenuItem ok;
    Epingle selectedPhotoId;
    private Tableau movie;
    private static final String TAG_SUCCESS = "success";
    int success;
    String tag_json_obj = "json_obj_req";
    TextView rating;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ivPhoto=(ImageView)findViewById(R.id.ivPhoto);
        check=(Button)findViewById(R.id.ok);
        description=(EditText)findViewById(R.id.description);
        title=(TextView)findViewById(R.id.title);
        delete=(LinearLayout)findViewById(R.id.delete);
        thumbnail=(ImageView)findViewById(R.id.thumbnail);
        toolbar=(android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
         rating = (TextView) findViewById(R.id.rating);


        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.icons8cancel);
        selectedPhotoId =(Epingle) getIntent().getExtras().getSerializable(ARG_SELECTED_PHOTO);
        idTab=Integer.toString(selectedPhotoId.getIdTableau());
        url = headUrl+"/user/readTableau.php?id="+idTab;
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("click");
                deleteImage();
                int[] startingLocation = new int[2];
                //v.getLocationOnScreen(startingLocation);
                startingLocation[0] += 516;
                UserProfileActivity.startUserProfileFromLocation(startingLocation, EditActivity.this, BaseDrawerActivity.currentUserId);
                overridePendingTransition(0, 0);

            }
        });
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editImage();
                int[] startingLocation = new int[2];
                //v.getLocationOnScreen(startingLocation);
                startingLocation[0] += 516;
                UserProfileActivity.startUserProfileFromLocation(startingLocation, EditActivity.this, BaseDrawerActivity.currentUserId);
                overridePendingTransition(0, 0);

            }
        });


        tableauList();
        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(EditActivity.this,EditPhotoTabActivity.class);
                intent.putExtra("EPINGLE",selectedPhotoId);
                startActivityForResult(intent, 2);
            }
        });
        loadInfoPhoto(selectedPhotoId);


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(resultCode==2 )
        {
             movie=(Tableau) data.getExtras().getSerializable("TABLEAU");
            title.setText(movie.getNom());

            rating.setText(String.valueOf(movie.getTableau_id()));
            Picasso.with( EditActivity.this)
                    .load("http://192.168.43.84/meloon/epingle/upload_image/images/as9a4ihc51e6sesy9xsd.png")
                    .transform(new RoundedCornersTransformation(3,2))
                    .centerCrop()
                    .resize(photoSize, photoSize)
                    .into(thumbnail);}
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
        root=(CoordinatorLayout)findViewById(R.id.content);
        root.setTranslationY(2 * 100);

        int actionbarSize = Utils.dpToPx(56);
        toolbar.setTranslationY(-actionbarSize);
        // getIvLogo().setTranslationY(-actionbarSize);
        ok.getActionView().setTranslationY(-actionbarSize);

        toolbar.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(300);
        ok.getActionView().animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(400)
                .start();
        root.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator(3.f))
                .setDuration(800)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                    }
                })
                .start();

    }
    private void deleteImage() {

        System.out.println(selectedPhotoId.getEpingle_id());
        DELETE_URL =headUrl+ "/epingle/upload_image/deleteEpingle.php?id="+selectedPhotoId.getEpingle_id();

        final ProgressDialog loading = ProgressDialog.show(this, "loading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jObj = new JSONObject(response);
                            success = jObj.getInt(TAG_SUCCESS);

                            if (success == 1) {
                                Log.e("v Add", jObj.toString());

                                Toast.makeText(EditActivity.this, jObj.getString("message"), Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(EditActivity.this, jObj.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        loading.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();


                    }
                })

        ;

        InstaMaterialApplication.getInstance().addToReqQueue(stringRequest, tag_json_obj);
    }
    private void editImage() {
        //menampilkan progress dialog
        EDIT_URL = headUrl+"/epingle/upload_image/editEpingle.php?id="+selectedPhotoId.getEpingle_id();

        final ProgressDialog loading = ProgressDialog.show(this, "loading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EDIT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jObj = new JSONObject(response);
                            success = jObj.getInt(TAG_SUCCESS);

                            if (success == 1) {
                                Log.e("v Add", jObj.toString());

                                Toast.makeText(EditActivity.this, jObj.getString("message"), Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(EditActivity.this, jObj.getString("message"), Toast.LENGTH_LONG).show();
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

                params.put("description", description.getText().toString().trim());
                params.put("epingle_id",String.valueOf(selectedPhotoId.getEpingle_id()) );
                params.put("tableau_id",String.valueOf(rating.getText()) );

                //kembali ke parameters
                return params;
            }
        };

        InstaMaterialApplication.getInstance().addToReqQueue(stringRequest, tag_json_obj);
    }


    private  void tableauList(){

        JsonObjectRequest jreq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                try {
                int success = response.getInt("success");
                if (success == 1) {
                        JSONArray ja = response.getJSONArray("tableaux");

                            JSONObject jobj = ja.getJSONObject(0);


                            movie = new Tableau();
                            movie.setNom(jobj.getString("nom"));

                            //movie.setThumbnailUrl(obj.getString("image"));

                            movie.setTableau_id(jobj.getInt("tableau_id"));
                    title.setText(movie.getNom());
                    rating.setText(String.valueOf(movie.getTableau_id()));




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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ok) {
           editImage();
            int[] startingLocation = new int[2];
            //v.getLocationOnScreen(startingLocation);
            startingLocation[0] += 516;
            UserProfileActivity.startUserProfileFromLocation(startingLocation, this, BaseDrawerActivity.currentUserId);
            overridePendingTransition(0, 0);
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    private void loadInfoPhoto(Epingle epingle){

        Picasso.with( this)
                .load(epingle.getEpingle_path())
                .transform(new RoundedCornersTransformation(5,4))
                .centerCrop()
                .resize(98, 110)
                .into(ivPhoto);
        description.setText(epingle.getDescription());
        ivPhoto.animate()
                .scaleX(1.f).scaleY(1.f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(400)
                .setStartDelay(200)
                .start();
        Picasso.with( this)
                .load(epingle.getEpingle_path())
                .transform(new RoundedCornersTransformation(5,4))
                .resize(60, 60)
                .into(thumbnail);

    }
}
