package com.example.android.blossom;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * put a call between two users in the server
 *
 */

public class CallLoader extends AsyncTaskLoader<String> {


    /** Tag for log messages */
    private static final String LOG_TAG = CallLoader.class.getName();

    /** Query */
    private String mUrl;
    private String mUserID_1;
    private String mUserID_2;
    private String mFamilyID;

    /**
     * Constructor
     *
     * @param context of the activity
     * @param url to load data from
     */
    public CallLoader(Context context, String url,
                      String userID_1, String userID_2, String familyID) {
        super(context);
        mUrl = url;
        mUserID_1 = userID_1;
        mUserID_2 = userID_2;
        mFamilyID = familyID;
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
        String res = Utils.makeEvent(mUrl, mUserID_1 ,mUserID_2, mFamilyID);
        return res;
    }
}
