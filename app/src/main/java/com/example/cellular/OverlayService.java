package com.example.cellular;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import org.json.JSONObject;
import java.util.List;
import android.telephony.TelephonyManager;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrength;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoNr;

public class OverlayService extends Service {
    private WindowManager windowManager;
    private View overlayView;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        overlayView = inflater.inflate(R.layout.overlay_feedback, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.graphics.PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;
        windowManager.addView(overlayView, params);

        setupOverlayInteractions();
        RadioGroup firstInput = overlayView.findViewById(R.id.q1_quality_group);
        if (firstInput != null) {
            firstInput.requestFocus();
        }
    }

    private void setupOverlayInteractions() {
        // Q1
        RadioGroup q1Group = overlayView.findViewById(R.id.q1_quality_group);

        // Q2 (CheckBoxes)
        CheckBox q2Dropped = overlayView.findViewById(R.id.q2_dropped);
        CheckBox q2CouldNotHear = overlayView.findViewById(R.id.q2_could_not_hear);
        CheckBox q2OtherCouldNotHear = overlayView.findViewById(R.id.q2_other_could_not_hear);
        CheckBox q2BackgroundNoise = overlayView.findViewById(R.id.q2_background_noise);
        CheckBox q2Echo = overlayView.findViewById(R.id.q2_echo);

        // Q3
        RadioGroup q3EnvGroup = overlayView.findViewById(R.id.q3_env_group);

        // Q4 comments (was Q5)
        EditText q4EditText = overlayView.findViewById(R.id.q4_comments);

        // Buttons
        Button submit = overlayView.findViewById(R.id.overlaySubmitButton);
        Button close = overlayView.findViewById(R.id.overlayCloseButton);

        submit.setOnClickListener(v -> {
            String q1 = getRadioSelection(q1Group, new int[] {
                    R.id.q1_good, R.id.q1_fair, R.id.q1_poor
            }, new String[] {
                    "Good", "Fair", "Poor"
            });

            StringBuilder q2Selections = new StringBuilder();
            if (q2Dropped.isChecked()) q2Selections.append("Call was dropped,");
            if (q2CouldNotHear.isChecked()) q2Selections.append("Could not hear the other person properly,");
            if (q2OtherCouldNotHear.isChecked()) q2Selections.append("The other person could not hear me properly,");
            if (q2BackgroundNoise.isChecked()) q2Selections.append("There was background noise,");
            if (q2Echo.isChecked()) q2Selections.append("There was echo during the call,");

            if (q2Selections.length() > 0)
                q2Selections.setLength(q2Selections.length() - 1); // Remove trailing comma

            String q3 = getRadioSelection(q3EnvGroup, new int[] {
                    R.id.q3_indoor, R.id.q3_outdoor, R.id.q3_vehicle, R.id.q3_noisy, R.id.q3_quiet
            }, new String[] {
                    "Indoor", "Outdoor", "In Vehicle", "Noisy Area", "Quiet Area"
            });

            String q4 = q4EditText.getText().toString();

            getLastLocationAndSendFeedback(q1, q2Selections.toString(), q3, q4, "");
        });

        close.setOnClickListener(v -> removeOverlay());
    }

    private String getRadioSelection(RadioGroup group, int[] ids, String[] values) {
        int id = group.getCheckedRadioButtonId();
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == id) return values[i];
        }
        return "";
    }

    private String getNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        if (caps == null) return "NONE";
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return "WiFi";
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int type = tm.getDataNetworkType();
            switch (type) {
                case TelephonyManager.NETWORK_TYPE_LTE: return "4G";
                case TelephonyManager.NETWORK_TYPE_NR: return "5G";
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_HSPA: return "3G";
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE: return "2G";
                default: return "Cellular";
            }
        }
        return "Unknown";
    }

    private String getSignalStrength(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            return "Unknown";
        try {
            List<CellInfo> cellInfoList = tm.getAllCellInfo();
            if (cellInfoList != null) {
                for (CellInfo ci : cellInfoList) {
                    if (ci instanceof CellInfoLte) {
                        return ((CellInfoLte) ci).getCellSignalStrength().getDbm() + " dBm (LTE)";
                    }
                    if (ci instanceof CellInfoWcdma) {
                        return ((CellInfoWcdma) ci).getCellSignalStrength().getDbm() + " dBm (WCDMA)";
                    }
                    if (ci instanceof CellInfoGsm) {
                        return ((CellInfoGsm) ci).getCellSignalStrength().getDbm() + " dBm (GSM)";
                    }
                    if (ci instanceof CellInfoNr) {
                        return ((CellInfoNr) ci).getCellSignalStrength().getDbm() + " dBm (5G)";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unavailable";
    }

    private void getLastLocationAndSendFeedback(
            String q1, String q2, String q3, String q4, String location
    ) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(loc -> {
                        String locationString = (loc != null)
                                ? loc.getLatitude() + "," + loc.getLongitude()
                                : "Location not available";
                        sendFeedbackAsync(q1, q2, q3, q4, locationString);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Could not get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        sendFeedbackAsync(q1, q2, q3, q4, "Location retrieval failed");
                    });
        } else {
            sendFeedbackAsync(q1, q2, q3, q4, "Location permission denied");
            Toast.makeText(this, "Location permission denied. Cannot record location.", Toast.LENGTH_LONG).show();
        }
    }

    private void sendFeedbackAsync(String q1, String q2, String q3, String q4, String location) {
        String networkType = getNetworkType(this);
        String signalStrength = getSignalStrength(this);
        JSONObject data = new JSONObject();
        try {
            data.put("overall_quality", q1);
            data.put("audio_issues", q2);
            data.put("environment", q3);
            data.put("comments", q4); // Now Q4 directly refers to comments
            data.put("location", location);
            data.put("connection_type", networkType);
            data.put("signal_strength", signalStrength);
            data.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() ->
                    Toast.makeText(this, "Couldn't make the json file!", Toast.LENGTH_SHORT).show()
            );
            e.printStackTrace();
        }
        String serverUrl = "http://10.51.21.115:5003/feedback";
        ServerPoster.sendFeedbackToServer(this, serverUrl, data);
        removeOverlay();
    }

    private void removeOverlay() {
        if (overlayView != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
