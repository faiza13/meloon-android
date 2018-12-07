package io.github.froger.instamaterial.ui.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import io.github.froger.instamaterial.R;


import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import io.github.froger.instamaterial.InstaMaterialApplication;
import io.github.froger.instamaterial.ui.activity.PublishActivity;
import io.github.froger.instamaterial.ui.model.Tableau;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class CustomListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Tableau> movieItems;
    ImageView thumbNail;
    int photoSize=60;
    Context context;
    private int lastAnimatedItem = -1;
    private boolean lockedAnimations = false;
    private static final int PHOTO_ANIMATION_DELAY = 600;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();
    ImageLoader imageLoader = InstaMaterialApplication.getInstance().getImageLoader();

    public CustomListAdapter(Activity activity, List<Tableau> movieItems,Context context) {
        this.activity = activity;
        this.movieItems = movieItems;
        this.context = context;
    }

    @Override
    public int getCount() {
        return movieItems.size();
    }

    @Override
    public Object getItem(int location) {
        return movieItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_row, null);

        if (imageLoader == null)
            imageLoader = InstaMaterialApplication.getInstance().getImageLoader();
        thumbNail = (ImageView) convertView
                .findViewById(R.id.thumbnail);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView rating = (TextView) convertView.findViewById(R.id.rating);

        bindPhoto( convertView,  position);
        // getting movie data for the row
        Tableau m = movieItems.get(position);

        // thumbnail image


        // title
        title.setText(m.getNom());

        // rating
        rating.setText(String.valueOf(m.getTableau_id()));


        return convertView;
    }
    private void bindPhoto(final View  convertView, final int position) {
        Picasso.with( context)
                .load(R.drawable.icons8liste)
                .transform(new RoundedCornersTransformation(3,2))
                .centerCrop()
                .resize(photoSize, photoSize)
                .into(thumbNail, new Callback() {
                    @Override
                    public void onSuccess() {
                        animatePhoto(thumbNail,position);
                    }

                    @Override
                    public void onError() {

                    }
                });
        if (lastAnimatedItem < position) lastAnimatedItem = position;}


    private void animatePhoto(View thumbNail,int position) {
        if (!lockedAnimations) {
            if (lastAnimatedItem == position) {
                setLockedAnimations(true);
            }

            long animationDelay = PHOTO_ANIMATION_DELAY +position * 30;

            thumbNail.setScaleY(0);
            thumbNail.setScaleX(0);

            thumbNail.animate()
                    .scaleX(1.f).scaleY(1.f)
                    .setInterpolator(new OvershootInterpolator())
                    .setDuration(400)
                    .setStartDelay(200)
                    .start();
        }
    }
    public void setLockedAnimations(boolean lockedAnimations) {
        this.lockedAnimations = lockedAnimations;
    }

}
