
package com.artmeloon.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.artmeloon.Utils;
import com.artmeloon.ui.adapter.AbonneAdapter;
import com.artmeloon.ui.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import com.artmeloon.R;

public class AbonneeActivity extends BaseDrawerActivity {


    @BindView(R.id.contentRoot)
    LinearLayout contentRoot;
    @BindView(R.id.rvComments)
    RecyclerView rvComments;
    ProgressDialog PD;
    int iSelectedItem;
    ArrayList<Integer> array_list;

    private AbonneAdapter abonneAdapter;
    private int drawingStartLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abonnee);
         iSelectedItem = getIntent().getIntExtra("user_id", 1);

         ReadDataFromDB();



            contentRoot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    contentRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                    startIntroAnimation();
                    return true;
                }
            });

    }
    private void ReadDataFromDB() {
        PD = new ProgressDialog(this);
        PD.setMessage("Loading.....");
        String url = UrlInfo.ip+"meloon/user/readAbonne.php?idUser="+iSelectedItem;

        JsonObjectRequest jreq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int success = response.getInt("success");
                    if (success == 1) {
                        ArrayList array_list=new ArrayList<User>() ;
                        JSONArray ja = response.getJSONArray("abonnees");

                        for (int i = 0; i < ja.length(); i++) {


                            JSONObject jobj = ja.getJSONObject(i);
                            User user =new User();
                            user.setProfil_id(Integer.parseInt(jobj.getString("id")));
                            user.setPrenom(jobj.getString("prenom"));
                            user.setNom(jobj.getString("nom"));
                            user.setPhotoPath(jobj.getString("photoPath"));
                            user.setTablesNbr(jobj.getInt("tablesnbr"));
                            user.setFollowersNbr(jobj.getInt("followersnbr"));
                            array_list.add(user);
                        }

                        setupComments(array_list);
                    } // if ends
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                PD.dismiss();
            }
        });

        // Adding request to request queue
        com.artmeloon.InstaMaterialApplication.getInstance().addToReqQueue(jreq);
    }


    private void setupComments(ArrayList list) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvComments.setLayoutManager(linearLayoutManager);
        rvComments.setHasFixedSize(true);

        abonneAdapter = new AbonneAdapter(this,list, AbonneeActivity.this);
        rvComments.setAdapter(abonneAdapter);
        rvComments.setOverScrollMode(View.OVER_SCROLL_NEVER);
        rvComments.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

            }
        });
    }


    private void startIntroAnimation() {
        ViewCompat.setElevation(getToolbar(), 0);
        contentRoot.setScaleY(0.1f);
        contentRoot.setPivotY(drawingStartLocation);


        contentRoot.animate()
                .scaleY(1)
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ViewCompat.setElevation(getToolbar(), Utils.dpToPx(8));
                    }
                })
                .start();
    }




}
