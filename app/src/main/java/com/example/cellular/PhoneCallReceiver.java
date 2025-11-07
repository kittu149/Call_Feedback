package com.example.cellular;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.telephony.TelephonyManager;

public class PhoneCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            if (Settings.canDrawOverlays(context)) {
                Intent svc = new Intent(context, OverlayService.class);
                svc.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(svc);
            }
        }
    }
}
