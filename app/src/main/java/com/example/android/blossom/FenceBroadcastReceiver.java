package com.example.android.blossom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.awareness.fence.FenceState;

/**
 * fence receiver
 */

public class FenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "FenceBroadcastReceiver";
    private static long currentTime;
    private static long lastTime = -1;
    private static long timeBetweenCalls = 10; // in seconds

    @Override
    public void onReceive(Context context, Intent intent) {
        FenceState fenceState = FenceState.extract(intent);

        Log.d(TAG, "Received a Fence Broadcast");

        switch (fenceState.getCurrentState()) {
            case FenceState.TRUE:
                Log.i(TAG, "Received a FenceUpdate - user in vehicle");


                // check last time popup was show
                currentTime = System.currentTimeMillis() - timeBetweenCalls * 1000;

                if (lastTime == -1 || currentTime > lastTime) {
                    lastTime = currentTime + timeBetweenCalls * 1000;

                    // TODO: 10/06/2017 show available popup
                } else {
                    Log.i(TAG, "Received a FenceUpdate, but not in time!");
                }
                break;

            case FenceState.FALSE:
                Log.i(TAG, "Received a FenceUpdate - user not in vehicle");
                break;

            case FenceState.UNKNOWN:
                Log.i(TAG, "Received a FenceUpdate -  The fence is in an unknown state.");
                break;
        }
    }



}
