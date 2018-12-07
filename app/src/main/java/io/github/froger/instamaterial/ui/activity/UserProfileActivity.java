package io.github.froger.instamaterial.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;
import io.github.froger.instamaterial.InstaMaterialApplication;
import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.ui.adapter.UserProfileAdapter;
import io.github.froger.instamaterial.ui.model.Epingle;
import io.github.froger.instamaterial.ui.model.User;
import io.github.froger.instamaterial.ui.utils.CircleTransformation;
import io.github.froger.instamaterial.ui.view.RevealBackgroundView;

/**
 * Created by Miroslaw Stanek on 14.01.15.
 */
public class UserProfileActivity extends BaseDrawerActivity implements RevealBackgroundView.OnStateChangeListener {


    HashMap<String, String> item;
   ArrayList<Epingle> array_list;
    UserProfileAdapter adapter;

    ProgressDialog PD;

    public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";

    private static final int USER_OPTIONS_ANIMATION_DELAY = 300;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();

    @BindView(R.id.vRevealBackground)
    RevealBackgroundView vRevealBackground;
    @BindView(R.id.rvUserProfile)
    RecyclerView rvUserProfile;

    @BindView(R.id.tlUserProfileTabs)
    TabLayout tlUserProfileTabs;

    @BindView(R.id.ivUserProfilePhoto)
    ImageView ivUserProfilePhoto;
    @BindView(R.id.vUserDetails)
    View vUserDetails;
    @BindView(R.id.btnFollow)
    Button btnFollow;
    @BindView(R.id.vUserStats)
    View vUserStats;
    @BindView(R.id.vUserProfileRoot)
    View vUserProfileRoot;
    @BindView(R.id.btnCreate)
    FloatingActionButton fabCreate;
    TextView nomText  ,emailText,descriptionText ,nbposts,nbfollowing,nbfollowers;
    LinearLayout followers,following;

    private int avatarSize;
    private int profilePhoto;
    private UserProfileAdapter userPhotosAdapter;
    User user;
    int epingle_nbr;
    int id;

    public static void startUserProfileFromLocation(int[] startingLocation, Activity startingActivity,int idUser) {

        Intent intent = new Intent(startingActivity, UserProfileActivity.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        intent.putExtra("idutil", idUser);

        startingActivity.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        id =getIntent().getIntExtra("idutil",1);
        nomText = (TextView) findViewById(R.id.nom);
        emailText = (TextView) findViewById(R.id.email);
        descriptionText = (TextView) findViewById(R.id.description);
        nbposts = (TextView) findViewById(R.id.nbposts);
        nbfollowers = (TextView) findViewById(R.id.nbfollowers);
        nbfollowing = (TextView) findViewById(R.id.nbfollowing);
        followers = (LinearLayout) findViewById(R.id.followers);
        following = (LinearLayout) findViewById(R.id.following);



        this.avatarSize = getResources().getDimensionPixelSize(R.dimen.user_profile_avatar_size);
        this.profilePhoto = R.drawable.img_feed_center_1;

        ReadDataFromDB();



        setupTabs();
        setupUserProfileGrid();
        setupRevealBackground(savedInstanceState);
    }

    private void setupTabs() {
        tlUserProfileTabs.addTab(tlUserProfileTabs.newTab().setIcon(R.drawable.ic_grid_on_white));
        tlUserProfileTabs.addTab(tlUserProfileTabs.newTab().setIcon(R.drawable.ic_list_white));

    }

    private void setupUserProfileGrid() {
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        rvUserProfile.setLayoutManager(layoutManager);
        rvUserProfile.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
              //  userPhotosAdapter.setLockedAnimations(true);
            }
        });
    }

    private void setupRevealBackground(Bundle savedInstanceState) {
        vRevealBackground.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
            vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                    vRevealBackground.startFromLocation(startingLocation);
                    return true;
                }
            });
        } else {
            final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
            vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                    vRevealBackground.startFromLocation(startingLocation);
                    return true;      }
            });
            //vRevealBackground.setToFinishedFrame();
            //userPhotosAdapter.setLockedAnimations(true);

        }
    }

    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            rvUserProfile.setVisibility(View.VISIBLE);
            tlUserProfileTabs.setVisibility(View.VISIBLE);
            vUserProfileRoot.setVisibility(View.VISIBLE);
            ReadEpinglesFromDB();
            animateUserProfileOptions();
            animateUserProfileHeader();
        } else {
            tlUserProfileTabs.setVisibility(View.INVISIBLE);
            rvUserProfile.setVisibility(View.INVISIBLE);
            vUserProfileRoot.setVisibility(View.INVISIBLE);
        }
    }

    private void animateUserProfileOptions() {
        tlUserProfileTabs.setTranslationY(-tlUserProfileTabs.getHeight());
        tlUserProfileTabs.animate().translationY(0).setDuration(300).setStartDelay(USER_OPTIONS_ANIMATION_DELAY).setInterpolator(INTERPOLATOR);
    }

    private void animateUserProfileHeader() {
           vUserProfileRoot.setTranslationY(-vUserProfileRoot.getHeight());
           ivUserProfilePhoto.setTranslationY(-ivUserProfilePhoto.getHeight());
           vUserDetails.setTranslationY(-vUserDetails.getHeight());
           vUserStats.setAlpha(0);

           vUserProfileRoot.animate().translationY(0).setDuration(300).setInterpolator(INTERPOLATOR);
           ivUserProfilePhoto.animate().translationY(0).setDuration(300).setStartDelay(100).setInterpolator(INTERPOLATOR);
           vUserDetails.animate().translationY(0).setDuration(300).setStartDelay(200).setInterpolator(INTERPOLATOR);
           vUserStats.animate().alpha(1).setDuration(200).setStartDelay(400).setInterpolator(INTERPOLATOR).start();
    }
    private void ReadDataFromDB() {
        PD = new ProgressDialog(this);
        PD.setMessage("Loading.....");
        PD.show();
        String url = UrlInfo.ip+"meloon/user/readUser.php?idUser="+id +"&idCurrent=" + BaseDrawerActivity.currentUserId;

        JsonObjectRequest jreq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int success = response.getInt("success");
                    if (success == 1) {






                        JSONArray ja = response.getJSONArray("users");
                        JSONObject jobj = ja.getJSONObject(0);
                        item = new HashMap<String, String>();
                        user =new User();
                        user.setProfil_id(Integer.parseInt(jobj.getString("id")));
                        user.setPrenom(jobj.getString("prenom"));
                        user.setNom(jobj.getString("nom"));
                        user.setEmail(jobj.getString("email"));
                        user.setDescription( jobj.getString("description"));
                        user.setPhotoPath(jobj.getString("photoPath"));
                        user.setFollowersNbr(response.getInt("followersnbr"));
                        user.setFollowingNbr(response.getInt("followingnbr"));
                        Picasso.with(UserProfileActivity.this)
                                .load(user.getPhotoPath())
                                .placeholder(R.drawable.img_circle_placeholder)
                                .resize(avatarSize, avatarSize)
                                .centerCrop()
                                .transform(new CircleTransformation())
                                .into(ivUserProfilePhoto);


                        nomText.setText(user.getPrenom()+" "+user.getNom());
                        emailText.setText(user.getEmail());
                        descriptionText.setText(user.getDescription());


                        nbposts.setText(String.valueOf(response.getInt("epinglenbr")));
                        nbfollowers.setText(String.valueOf(user.getFollowersNbr()));
                        nbfollowing.setText(String.valueOf(user.getFollowingNbr()));
                        followers.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final Intent intent = new Intent(UserProfileActivity.this, AbonneeActivity.class);
                                intent.putExtra("user_id",user.getProfil_id());
                                startActivity(intent);
                            }
                        });
followers.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final Intent intent = new Intent(UserProfileActivity.this, AbonneeActivity.class);
                                intent.putExtra("user_id",user.getProfil_id());
                                startActivity(intent);
                            }
                        });
following.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final Intent intent = new Intent(UserProfileActivity.this, AbonnementActivity.class);
                                intent.putExtra("user_id",user.getProfil_id());
                                startActivity(intent);
                            }
                        });

                        if (id == BaseDrawerActivity.currentUserId){

                            ivUserProfilePhoto.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            });




                        }

                        if (id!= BaseDrawerActivity.currentUserId){
                            if (response.getInt("following")==0){

                                btnFollow.setVisibility(View.VISIBLE);
                                btnFollow.setText("FOLLOW");
                                btnFollow.setBackgroundResource(R.drawable.btn_following);





                            }else {

                                btnFollow.setVisibility(View.VISIBLE);
                                btnFollow.setText("UNFOLLOW");

                                btnFollow.setBackgroundResource(R.drawable.btn_unfollowing);


                            }


                        }
                        btnFollow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(btnFollow.getText()=="FOLLOW"){
                                    btnFollow.setText("UNFOLLOW");

                                    btnFollow.setBackgroundResource(R.drawable.btn_unfollowing);
                                    followFromDB();

                                }
                                else {
                                    btnFollow.setText("FOLLOW");

                                    btnFollow.setBackgroundResource(R.drawable.btn_following);
                                    unfollowFromDB();



                                }
                            }
                        });

                        PD.dismiss();
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
        InstaMaterialApplication.getInstance().addToReqQueue(jreq);

    }
    private void ReadEpinglesFromDB() {
        PD = new ProgressDialog(this);
        PD.setMessage("Loading.....");
        PD.show();
        String url1 = UrlInfo.ip+"meloon/epingle/upload_image/readEpingle.php?idUser="+user.getProfil_id();
        JsonObjectRequest jreq = new JsonObjectRequest(Request.Method.GET, url1, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int success = response.getInt("success");
                    if (success == 1) {

                        epingle_nbr = response.getInt("epinglenbr");
                        nbposts.setText(String.valueOf(epingle_nbr));

                        JSONArray ja = response.getJSONArray("epingles");
                        array_list=new ArrayList<Epingle>() ;
for (int i = 0; i < ja.length(); i++) {
JSONObject jobj = ja.getJSONObject(i);
    Epingle epingle=new Epingle();
    epingle.setEpingle_id(Integer.parseInt(jobj.getString("epingle_id")));
    epingle.setDescription(jobj.getString("description"));
    epingle.setEpingle_path(jobj.getString("epingle_path"));
    epingle.setIdTableau(Integer.parseInt(jobj.getString("tableau_id")));

array_list.add(epingle);
} // for loop ends

 userPhotosAdapter = new UserProfileAdapter(UserProfileActivity.this,array_list,user);
 rvUserProfile.setAdapter(userPhotosAdapter);
                        PD.dismiss();

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
        InstaMaterialApplication.getInstance().addToReqQueue(jreq);
    }

    private void followFromDB() {
        PD = new ProgressDialog(this);
        PD.setMessage("Loading.....");
        PD.show();
        String url1 = UrlInfo.ip+"meloon/user/follow.php?idUser="+id +"&idCurrent=" + BaseDrawerActivity.currentUserId;
        JsonObjectRequest jreq = new JsonObjectRequest(Request.Method.GET, url1, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int success = response.getInt("success");
                    if (success == 1) {
                       int nb=user.getFollowersNbr();
                        nb=nb+1;
                      user.setFollowersNbr(nb);

                      nbfollowers.setText(String.valueOf(user.getFollowersNbr()));



                        PD.dismiss();
                        Toast.makeText(UserProfileActivity.this, "Followed Sucessfully", Toast.LENGTH_LONG).show();


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
        InstaMaterialApplication.getInstance().addToReqQueue(jreq);
    }

    private void unfollowFromDB() {
        PD = new ProgressDialog(this);
        PD.setMessage("Loading.....");
        PD.show();
        String url1 = UrlInfo.ip+"meloon/user/unfollow.php?idUser="+id +"&idCurrent=" + BaseDrawerActivity.currentUserId;
        JsonObjectRequest jreq = new JsonObjectRequest(Request.Method.GET, url1, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int success = response.getInt("success");
                    if (success == 1) {
                        int nb=user.getFollowersNbr();
                        nb=nb-1;
                        user.setFollowersNbr(nb);

                        nbfollowers.setText(String.valueOf(user.getFollowersNbr()));



                        PD.dismiss();
                        Toast.makeText(UserProfileActivity.this, "Unfollowed Sucessfully", Toast.LENGTH_LONG).show();


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
        InstaMaterialApplication.getInstance().addToReqQueue(jreq);
    }
    @OnClick(R.id.btnCreate)
    public void onTakePhotoClick() {
       // PublishActivity.openWithPhotoUri(this, Uri.parse("http://stackoverflow.com"));
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }
 @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
    PublishActivity.openWithPhotoUri( UserProfileActivity.this, filePath,user) ;


     }}
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem register = menu.findItem(R.id.action_inbox);

        register.setVisible(true);
        register.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {








                return true;
            }
        });



        return true;
    }

}





