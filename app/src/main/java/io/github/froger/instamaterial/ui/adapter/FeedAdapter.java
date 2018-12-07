package io.github.froger.instamaterial.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.froger.instamaterial.InstaMaterialApplication;
import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.ui.activity.AbonneeActivity;
import io.github.froger.instamaterial.ui.activity.AbonnementActivity;
import io.github.froger.instamaterial.ui.activity.BaseDrawerActivity;
import io.github.froger.instamaterial.ui.activity.MainActivity;
import io.github.froger.instamaterial.ui.activity.SocialMediaPublishActivity;
import io.github.froger.instamaterial.ui.activity.UrlInfo;
import io.github.froger.instamaterial.ui.activity.UserProfileActivity;
import io.github.froger.instamaterial.ui.model.Epingle;
import io.github.froger.instamaterial.ui.model.User;
import io.github.froger.instamaterial.ui.utils.CircleTransformation;
import io.github.froger.instamaterial.ui.view.LoadingFeedItemView;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String ACTION_LIKE_BUTTON_CLICKED = "action_like_button_button";
    public static final String ACTION_LIKE_IMAGE_CLICKED = "action_like_image_button";

    public static final int VIEW_TYPE_DEFAULT = 1;
    public static final int VIEW_TYPE_LOADER = 2;
    private final List<Epingle> feedItems = new ArrayList<>();

    private Context context;
    private OnFeedItemClickListener onFeedItemClickListener;
    String resultCount,userImageURL;
    Integer nbr;
    String top;
    ArrayList<Integer> listnb;
    private boolean showLoadingView = false;
    private ArrayList<Epingle> epingles;
    public FeedAdapter(Context context, ArrayList<Epingle> photos) {
        this.context = context;
        this.epingles = photos;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DEFAULT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
            CellFeedViewHolder cellFeedViewHolder = new CellFeedViewHolder(view,context);
            setupClickableViews(view, cellFeedViewHolder);
            return cellFeedViewHolder;
        } else if (viewType == VIEW_TYPE_LOADER) {
            LoadingFeedItemView view = new LoadingFeedItemView(context);
            view.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            );
            return new LoadingCellFeedViewHolder(view,context);
        }

        return null;
    }

    private void setupClickableViews(final View view, final CellFeedViewHolder cellFeedViewHolder) {
        cellFeedViewHolder.btnComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onCommentsClick(view, cellFeedViewHolder.getAdapterPosition());
            }
        });
        cellFeedViewHolder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onFeedItemClickListener.onMoreClick(v, cellFeedViewHolder.getAdapterPosition());
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                String description = feedItems.get(adapterPosition).getDescription();
                String image = feedItems.get(adapterPosition).getEpingle_path();
                Intent intent = new Intent(context,SocialMediaPublishActivity.class);
                intent.putExtra("EPINGLE_DESCRIPTION", description);
                intent.putExtra("EPINGLE_IMAGE", image);
                context.startActivity(intent);
            }
        });
        cellFeedViewHolder.ivFeedCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                feedItems.get(adapterPosition).likesCount++;
                notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showLikedSnackbar();
                }
            }
        });
        cellFeedViewHolder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                notifyItemChanged(adapterPosition, ACTION_LIKE_BUTTON_CLICKED);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showLikedSnackbar();
                }
                OnInsertLike(1,1);
                Integer epingle_id = feedItems.get(adapterPosition).getEpingle_id();
                OnGetCountLikes(epingle_id);
                cellFeedViewHolder.tsLikesCounter.setText(resultCount);
            }
        });
        cellFeedViewHolder.ivUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                int id_creator = feedItems.get(adapterPosition).getId_creator();
//                onFeedItemClickListener.onProfileClick(view);
                //Intent intent = new Intent(context, UserProfileActivity.class);
                //intent.putExtra("idutil", id_creator);
                //context.startActivity(intent);
                int[] startingLocation = new int[2];
                v.getLocationOnScreen(startingLocation);
                startingLocation[0] += v.getWidth() / 2;
                UserProfileActivity.startUserProfileFromLocation(startingLocation, BaseDrawerActivity.mainActivity,id_creator);*/
            }
        });

    }
    public void OnInsertLike(Integer userID, Integer epingle_ID){

        BackgroundLikes backgroundWorkerLogin = new BackgroundLikes(context);
        backgroundWorkerLogin.execute(userID,epingle_ID);
    }
    class BackgroundLikes extends AsyncTask<Integer,Void,String> {
        Context context;

        public BackgroundLikes(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Integer... params) {

            String login_url = "http://172.19.4.7/InsertLike.php";
            try {
                Integer userID = params[0];
                Integer epingle_ID = params[1];
                URL url = new URL(login_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("userid", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(userID), "UTF-8") + "&"
                        + URLEncoder.encode("epingleid", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(epingle_ID), "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result = "";
                String line = "";

                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("insert like success")) {
                Toast.makeText(context, "Like aaded", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Error inserting like", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
    public void OnGetCountLikes(Integer epingle_ID){

        BackgroundCountLikes backgroundCountLikes = new BackgroundCountLikes(context);
        backgroundCountLikes.execute(epingle_ID);
    }
    class BackgroundCountLikes extends AsyncTask<Integer,Void,String> {
        Context context;

        public BackgroundCountLikes(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Integer... params) {

            String login_url = "http://172.16.250.175/getCountLikesEpingle.php";
            try {
                Integer epingle_ID = params[0];
                URL url = new URL(login_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("epingleid", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(epingle_ID), "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result = "";
                String line = "";

                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(String result) {
            resultCount ="";
            resultCount = result;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }




    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        //((CellFeedViewHolder) viewHolder).bindView(feedItems.get(position));
        final Epingle epingle = epingles.get(position);

        ((CellFeedViewHolder) viewHolder).ivFeedBottom.setText(epingle.getDescription());
        Picasso.with(context)
                .load(epingle.getUser().getPhotoPath())
                .resize(60, 80)
                .centerCrop()
                .transform(new CropCircleTransformation())
                .into(((CellFeedViewHolder) viewHolder).ivUserProfile);




        ((CellFeedViewHolder) viewHolder).userName.setText(epingle.getUser().getPrenom()+" "+epingle.getUser().getNom());


        String epingleurl = epingle.getEpingle_path();

        Picasso.with(context)
                .load(epingle.getEpingle_path())
                .into(   ((CellFeedViewHolder) viewHolder).ivFeedCenter);






if (epingle.getId_creator()!=BaseDrawerActivity.currentUserId){
        if (epingle.getUser().getFollowing()==0){

            ((CellFeedViewHolder) viewHolder).btnFollow.setVisibility(View.VISIBLE);
            ((CellFeedViewHolder) viewHolder).btnFollow.setText("FOLLOW");
            ((CellFeedViewHolder) viewHolder).btnFollow.setId(epingle.getUser().getProfil_id());
            ((CellFeedViewHolder) viewHolder).btnFollow.setBackgroundResource(R.drawable.btn_following);

        }
        else if(epingle.getUser().getFollowing()>0) {
            ((CellFeedViewHolder) viewHolder).btnFollow.setVisibility(View.VISIBLE);
            ((CellFeedViewHolder) viewHolder).btnFollow.setText("UNFOLLOW");
            ((CellFeedViewHolder) viewHolder).btnFollow.setId(epingle.getUser().getProfil_id());

            ((CellFeedViewHolder) viewHolder).btnFollow.setBackgroundResource(R.drawable.btn_unfollowing);



        }}
         ((CellFeedViewHolder) viewHolder).btnFollow.setOnClickListener(new View.OnClickListener() {

           @Override
            public void onClick(View view) {
                Button bouton=(Button) view;
                if(bouton.getText()=="FOLLOW"){
                    bouton.setText("UNFOLLOW");

                    bouton.setBackgroundResource(R.drawable.btn_unfollowing);
                     followFromDB(bouton.getId());

                }
                else {
                    bouton.setText("FOLLOW");

                    bouton.setBackgroundResource(R.drawable.btn_following);
                     unfollowFromDB(bouton.getId());



                }
            }
        });






        int id_creator = epingle.getId_creator();
        OnSerachUserPicture(id_creator);



        if (getItemViewType(position) == VIEW_TYPE_LOADER) {
            bindLoadingFeedItem((LoadingCellFeedViewHolder) viewHolder);
        }
    }

    private void bindLoadingFeedItem(final LoadingCellFeedViewHolder holder) {
        holder.loadingFeedItemView.setOnLoadingFinishedListener(new LoadingFeedItemView.OnLoadingFinishedListener() {
            @Override
            public void onLoadingFinished() {
                showLoadingView = false;
                notifyItemChanged(0);
            }
        });
        holder.loadingFeedItemView.startLoading();
    }
    private void unfollowFromDB(int id) {

        String url1 = UrlInfo.ip+"meloon/user/unfollow.php?idUser="+id +"&idCurrent=" + BaseDrawerActivity.currentUserId;
        JsonObjectRequest jreq = new JsonObjectRequest(Request.Method.GET, url1, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int success = response.getInt("success");
                    if (success == 1) {






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
    private void followFromDB(int id) {

        String url1 = UrlInfo.ip+"meloon/user/follow.php?idUser="+id +"&idCurrent=" + BaseDrawerActivity.currentUserId;
        JsonObjectRequest jreq = new JsonObjectRequest(Request.Method.GET, url1, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int success = response.getInt("success");
                    if (success == 1) {



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
    public int getItemViewType(int position) {
        if (showLoadingView && position == 0) {
            return VIEW_TYPE_LOADER;
        } else {
            return VIEW_TYPE_DEFAULT;
        }
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    public void updateItems(boolean animated) {
        feedItems.clear();
        feedItems.addAll(epingles);
        if (animated) {
            notifyItemRangeInserted(0, feedItems.size());
        } else {
            notifyDataSetChanged();
        }
    }

    public void setOnFeedItemClickListener(OnFeedItemClickListener onFeedItemClickListener) {
        this.onFeedItemClickListener = onFeedItemClickListener;
    }

    public void showLoadingView() {
        showLoadingView = true;
        notifyItemChanged(0);
    }

    public static class CellFeedViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.userName)
        TextView userName;
        @BindView(R.id.ivFeedCenter)
        ImageView ivFeedCenter;
        @BindView(R.id.ivFeedBottom)
        TextView ivFeedBottom;
        @BindView(R.id.btnComments)
        ImageButton btnComments;
        @BindView(R.id.btnLike)
        ImageButton btnLike;
        @BindView(R.id.btnMore)
        ImageButton btnMore;
        @BindView(R.id.vBgLike)
        View vBgLike;
        @BindView(R.id.ivLike)
        ImageView ivLike;
        @BindView(R.id.tsLikesCounter)
        TextView tsLikesCounter;
        @BindView(R.id.ivUserProfile)
        ImageView ivUserProfile;
        @BindView(R.id.vImageRoot)
        FrameLayout vImageRoot;
        @BindView(R.id.btnFollow)
        Button btnFollow;
        Epingle feedItem;
        Context contextt;

        public CellFeedViewHolder(View view,Context c) {
            super(view);
            contextt = c;
            ButterKnife.bind(this, view);
        }

        public void bindView(Epingle feedItem) {
            this.feedItem = feedItem;
            int adapterPosition = getAdapterPosition();
            btnLike.setImageResource(feedItem.isLiked() ? R.drawable.ic_heart_red : R.drawable.ic_heart_outline_grey);
        }

        public Epingle getFeedItem() {
            return feedItem;
        }
    }

    public static class LoadingCellFeedViewHolder extends CellFeedViewHolder {

        LoadingFeedItemView loadingFeedItemView;

        public LoadingCellFeedViewHolder(LoadingFeedItemView view, Context c) {
            super(view,c);
            this.loadingFeedItemView = view;
        }

        @Override
        public void bindView(Epingle feedItem) {
            super.bindView(feedItem);
        }
    }

    public interface OnFeedItemClickListener {
        void onCommentsClick(View v, int position);

        void onMoreClick(View v, int position);

        void onProfileClick(View v);
    }

    public void OnSerachUserPicture(int id_user) {
        BackgroundSerachUserPicture backgroundSerachUserPicture = new BackgroundSerachUserPicture(context);
        backgroundSerachUserPicture.execute(id_user);
    }
    class BackgroundSerachUserPicture extends AsyncTask<Integer,Void,String> {
        Context context;

        public BackgroundSerachUserPicture(Context context){
            this.context=context;
        }

        @Override
        protected String doInBackground(Integer...params){

            String login_url="http://172.16.250.175/readUserPicture.php";
            try{
                int id_creator=params[0];
                URL url=new URL(login_url);
                HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream=httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String post_data= URLEncoder.encode("id_creator","UTF-8")+"="+URLEncoder.encode(String.valueOf(id_creator),"UTF-8");
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
            userImageURL = result;
        }

        @Override
        protected void onProgressUpdate(Void...values){
            super.onProgressUpdate(values);
        }

    }


}