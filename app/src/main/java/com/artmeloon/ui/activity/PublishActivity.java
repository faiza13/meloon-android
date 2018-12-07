package com.artmeloon.ui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.media.ExifInterface;
import android.util.Base64;
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
import android.widget.LinearLayout;
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
import com.artmeloon.ui.model.Tableau;
import com.artmeloon.ui.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import com.artmeloon.R;

/**
 * Created by Miroslaw Stanek on 21.02.15.
 */
public class PublishActivity extends BaseActivity {
    public static final String ARG_TAKEN_PHOTO_URI = "arg_taken_photo_uri";
    private static final String url = UrlInfo.ip+"meloon/tableau/Api.php?apicall=getheroes"+"&idCurrent=" + BaseDrawerActivity.currentUserId;

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

    EditText description;

    @BindView(R.id.ivPhoto)
    ImageView ivPhoto;
    Uri filePath;
    Bitmap bitmap,decoded;
    int bitmap_size = 96;
    User user;

    private boolean propagatingToggleState = false;
    private Uri photoUri;
    private int photoSize;
   LinearLayout createTable;

    public static void
    openWithPhotoUri(Activity openingActivity, Uri photoUri, User user) {
        Intent intent = new Intent(openingActivity, PublishActivity.class);
        intent.putExtra(ARG_TAKEN_PHOTO_URI, photoUri);
        intent.putExtra("user", user);
        openingActivity.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        tbFollowers=(ImageButton)findViewById(R.id.tbFollowers);
        listView = (ListView) findViewById(R.id.list);
        description = (EditText) findViewById(R.id.description);
        createTable = (LinearLayout) findViewById(R.id.createTable);





        adapter = new CustomListAdapter(this, movieList, PublishActivity.this);
        listView.setAdapter(adapter);
        tableauList();


        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_grey600_24dp);
        photoSize = getResources().getDimensionPixelSize(R.dimen.publish_photo_thumbnail_size);

        user = (User) getIntent().getExtras().getSerializable("user");

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
        loadphoto( photoUri );
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Tableau item = (Tableau) listView.getItemAtPosition(position);
                idTab= item.getTableau_id();
                uploadImage( );

            }
        });
        createTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(PublishActivity.this,createTableActivity.class);
                intent.putExtra(ARG_TAKEN_PHOTO_URI, photoUri);
                intent.putExtra("user", user);

                startActivity(intent);
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
        getMenuInflater().inflate(R.menu.menu_publish, menu);
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


    @OnClick(R.id.tbFollowers)
    public void onTakePhotoClick() {
        //PublishActivity.openWithPhotoUri(this, Uri.parse("http://stackoverflow.com"));
       Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        InputStream in;


        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();

            try {

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                in = getContentResolver().openInputStream(filePath);
                ExifInterface exifInterface = new ExifInterface(in);
                // ExifInterface exif = new ExifInterface( filePath.getPath());
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                System.out.println("");
                if (orientation == 6) {
                    matrix.postRotate(90);
                }
                else if (orientation == 3) {
                    matrix.postRotate(180);
                }
                else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                // 512 adalah resolusi tertinggi setelah image di resize, bisa di ganti.
            } catch (IOException e) {
                e.printStackTrace();
            }
            setToImageView( bitmap);


    }



    }
    protected void loadphoto(Uri photoUri ) {
        InputStream in;




            try {
                //mengambil fambar dari Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                in = getContentResolver().openInputStream(photoUri);
                ExifInterface exifInterface = new ExifInterface(in);
                // ExifInterface exif = new ExifInterface( filePath.getPath());
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                System.out.println("");
                if (orientation == 6) {
                    matrix.postRotate(90);
                }
                else if (orientation == 3) {
                    matrix.postRotate(180);
                }
                else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                // 512 adalah resolusi tertinggi setelah image di resize, bisa di ganti.
            } catch (IOException e) {
                e.printStackTrace();
            }
            setToImageView( bitmap);
        }
    private void setToImageView(Bitmap bmp) {
        //compress image
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, bitmap_size, bytes);

        decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes.toByteArray()));

        //menampilkan gambar yang dipilih dari camera/gallery ke ImageView
        ivPhoto.setScaleX(0);
        ivPhoto.setScaleY(0);
        ivPhoto.setImageBitmap(Bitmap.createScaledBitmap(bitmap, photoSize, photoSize, false));

        ivPhoto.animate()
                .scaleX(1.f).scaleY(1.f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(400)
                .setStartDelay(200)
                .start();
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
         String UPLOAD_URL = UrlInfo.ip+"meloon/epingle/upload_image/upload.php";

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

                                Toast.makeText(PublishActivity.this, jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(PublishActivity.this, jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
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
                params.put(KEY_IMAGE, getStringImage(decoded));
                params.put(KEY_DESCRIPTION, description.getText().toString().trim());
                params.put("idTableau",String.valueOf(idTab) );

                //kembali ke parameters
                Log.e(TAG, "" + params);
                return params;
            }
        };

        InstaMaterialApplication.getInstance().addToReqQueue(stringRequest, tag_json_obj);
    }
    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, bitmap_size, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }



}
