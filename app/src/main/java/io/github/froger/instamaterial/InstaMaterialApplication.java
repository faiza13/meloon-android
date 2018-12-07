package io.github.froger.instamaterial;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import io.github.froger.instamaterial.ui.utils.LruBitmapCache;
import timber.log.Timber;

/**
 * Created by froger_mcs on 05.11.14.
 */

public class InstaMaterialApplication extends Application {
    public static final String TAG = InstaMaterialApplication.class.getSimpleName();

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private static InstaMaterialApplication mInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());

        mInstance = this;
    }
    public static synchronized InstaMaterialApplication getInstance() {
        return mInstance;
    }
    public RequestQueue getReqQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }
    public ImageLoader getImageLoader() {
        getReqQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }
    public <T> void addToReqQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        getReqQueue().add(req);
    }
    public <T> void addToReqQueue(Request<T> req) {
        req.setTag(TAG);
        getReqQueue().add(req);
    }
    public void cancelPendingReq(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}