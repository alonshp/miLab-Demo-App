package com.example.android.blossom;

import android.Manifest;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import org.json.JSONException;
import org.json.JSONObject;
import static com.example.android.blossom.Utils.LOG_TAG;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    private static final String TAG = "FenceActivity";
    private static String phoneNumber = "";
    private static String userName = "";
    private static String userID = "";
    private static String familyID = "";
    private boolean callPopupShowed = false;

    private static final String MY_FENCE_RECEIVER_ACTION = "MY_FENCE_ACTION";
    public static final String FENCE_KEY = "AIzaSyCCJ8vFQBuS_2CThL5FvV0SBhb9MavQcyg";

    private GoogleApiClient mGoogleApiClient;
    private FenceBroadcastReceiver mFenceReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button test = (Button) findViewById(R.id.test_bn);

        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(Awareness.API)
                .build();
        mGoogleApiClient.connect();

        mFenceReceiver = new FenceBroadcastReceiver();

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show popup delayed
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showAvailablePopup();
                    }
                }, 7000);
            }
        });

        addActivityFence();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // We want to receive Broadcasts when activity is paused
        registerReceiver(mFenceReceiver, new IntentFilter(MY_FENCE_RECEIVER_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void addActivityFence() {

        Intent intent = new Intent(MY_FENCE_RECEIVER_ACTION);
        PendingIntent mFencePendingIntent = PendingIntent.getBroadcast(MainActivity.this,
                10001,
                intent,
                0);

        AwarenessFence activityFence = DetectedActivityFence.during(DetectedActivityFence.IN_VEHICLE);
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence(FENCE_KEY, activityFence, mFencePendingIntent)
                        .build())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Fence was successfully registered.");
                        } else {
                            Log.e(TAG, "Fence could not be registered: " + status);
                        }
                    }
                });
    }

    public void addAvailableLoader() {
        LoaderManager loaderManager = getLoaderManager();

        loaderManager.restartLoader(1, null, MainActivity.this);
    }

    public void addCallLoader() {
        LoaderManager loaderManager = getLoaderManager();

        loaderManager.restartLoader(2, null, MainActivity.this);
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userID1 = sharedPrefs.getString(
                "user_id",
                "0");

        if (id == 1) {
            Uri baseUri = Uri.parse("https://milab-blossom.herokuapp.com/");
            Uri.Builder uriBuilder = baseUri.buildUpon();
            return new AvailableLoader(this, uriBuilder.toString(), userID1);
        } else {
            Uri baseUri = Uri.parse("https://milab-blossom.herokuapp.com/");
            Uri.Builder uriBuilder = baseUri.buildUpon();
            return new CallLoader(this, uriBuilder.toString(), userID1, userID, familyID);
        }
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        // push
        if (data.equals("204")) {
            Log.e(LOG_TAG, "the event put to the server");
            return;
        }
        if (data.equals("error")) {
            Log.e(LOG_TAG, "error while put event to the server");
            return;
        }
        // post
        try {
            // no user available
            if (data.equals("404")) {
                Toast.makeText(this, "There is no one available right now",
                        Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, "no one available right now!");
            } else {
                // found user available
                // build up a list of Earthquake objects with the corresponding data.
                JSONObject root = new JSONObject(data);
                phoneNumber = root.getString("contact_phone_number");
                userName = root.getString("contact_name");
                userID = root.getString("contact_user_id");
                familyID = root.getString("contact_family_id");

                Log.e(LOG_TAG, "contact: " + phoneNumber + "  "
                        + userName + "  " + userID + "  " + familyID);

                if (!callPopupShowed) {
                    callPopupShowed = true;
                    showCallPopup(userName);
                }
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("", "Problem parsing the earthquake JSON results", e);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }


    public void showAvailablePopup() {
        callPopupShowed =false;

        final WindowManager manager = (WindowManager)
                this.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.alpha = 1.0f;
        layoutParams.packageName = this.getPackageName();
        layoutParams.buttonBrightness = 1f;
        layoutParams.windowAnimations = android.R.style.Animation_Dialog;

        final View view = View.inflate(this.getApplicationContext(),
                R.layout.available_popup, null);
        Button available = (Button) view.findViewById(R.id.available);
        Button close = (Button) view.findViewById(R.id.dismiss);
        available.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAvailableLoader();
                manager.removeView(view);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.removeView(view);
            }
        });

        manager.addView(view, layoutParams);
    }

    public void showCallPopup(String name) {
        final WindowManager manager = (WindowManager)
                this.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.alpha = 1.0f;
        layoutParams.packageName = this.getPackageName();
        layoutParams.buttonBrightness = 1f;
        layoutParams.windowAnimations = android.R.style.Animation_Dialog;

        final View view = View.inflate(this.getApplicationContext(),
                R.layout.call_popup, null);
        Button available = (Button) view.findViewById(R.id.available);
        Button close = (Button) view.findViewById(R.id.dismiss);

        // update user name
        TextView userName = (TextView) view.findViewById(R.id.available_user);
        userName.setText(name);

        available.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCallLoader();
                manager.removeView(view);
                makeCall(phoneNumber);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.removeView(view);
            }
        });
        manager.addView(view, layoutParams);
    }

    private void makeCall(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intent);  //call activity and make phone call
    }
}


