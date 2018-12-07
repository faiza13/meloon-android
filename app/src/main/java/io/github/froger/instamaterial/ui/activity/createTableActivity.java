package io.github.froger.instamaterial.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.github.froger.instamaterial.InstaMaterialApplication;
import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.ui.model.User;


public class createTableActivity extends AppCompatActivity {
    android.support.v7.widget.Toolbar toolbar;
    MenuItem ok;
    TextView name, description;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    String tag_json_obj = "json_obj_req";
    public static final String ARG_TAKEN_PHOTO_URI = "arg_taken_photo_uri";
User user;
Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_table);
        toolbar=(android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        name=(TextView) findViewById(R.id.name);
        description=(TextView) findViewById(R.id.description);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.icons8cancel);
        user = (User) getIntent().getExtras().getSerializable("user");
        photoUri = getIntent().getParcelableExtra(ARG_TAKEN_PHOTO_URI);



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editphoto, menu);

        ok=menu.findItem(R.id.ok);
        ok.setVisible(true);







        return true;
    }
    private void addTable() {
        //menampilkan progress dialog
          String url = UrlInfo.ip+"meloon/tableau/Api.php?apicall=createhero";


        final ProgressDialog loading = ProgressDialog.show(this, "loading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jObj = new JSONObject(response);
                           int success = jObj.getInt(TAG_SUCCESS);

                            if (success == 1) {
                                Toast.makeText(createTableActivity.this, jObj.getString("message"), Toast.LENGTH_LONG).show();

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
                Map<String, String> params = new HashMap<String, String>();


                params.put("description", description.getText().toString().trim());
                params.put("nom",name.getText().toString());
                params.put("id_creator",String.valueOf(BaseDrawerActivity.currentUserId));

                return params;
            }
        };

        InstaMaterialApplication.getInstance().addToReqQueue(stringRequest, tag_json_obj);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ok) {
            addTable();
            Intent intent=new Intent(createTableActivity.this,PublishActivity.class);
            intent.putExtra(ARG_TAKEN_PHOTO_URI, photoUri);
            intent.putExtra("user", user);


            startActivity(intent);


            return true;


        }else {
            return super.onOptionsItemSelected(item);
        }
    }

}
