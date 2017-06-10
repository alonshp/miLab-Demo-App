package com.example.android.blossom;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * post when user is available
 * return another user available, otherwise return 404
 *
 */
public class AvailableLoader extends AsyncTaskLoader<String> {


    /** Tag for log messages */
    private static final String LOG_TAG = AvailableLoader.class.getName();

    /** Query */
    private String mUrl;
    private String mUserID;

    /**
     * Constructor
     *
     * @param context of the activity
     * @param url to load data from
     */
    public AvailableLoader(Context context, String url, String userID) {
        super(context);
        mUrl = url;
        mUserID = userID;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public String loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request.
        String res = Utils.fetchData(mUrl, mUserID);
        return res;
    }
}

