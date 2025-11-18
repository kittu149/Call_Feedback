package com.example.cellular;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerPoster {
    public static void sendFeedbackToServer(final Context context, final String serverUrl, final JSONObject data) {
        new Thread(() -> {
            try {
                URL url = new URL(serverUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(data.toString().getBytes("UTF-8"));
                }
                int responseCode = conn.getResponseCode();
                conn.disconnect();

                Handler handler = new Handler(Looper.getMainLooper());
                if (responseCode == 200 || responseCode == 201) {
                    handler.post(() ->
                            Toast.makeText(context, "Feedback sent to server!", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    handler.post(() ->
                            Toast.makeText(context, "Server error: " + responseCode, Toast.LENGTH_SHORT).show()
                    );
                }
            }
            catch (Exception e) {
                Handler handler = new Handler(Looper.getMainLooper());

                String message;
                if (e instanceof java.net.ConnectException) {
                    message = "Can't connect to server. Check server address/network.";
                } else if (e instanceof java.net.UnknownHostException) {
                    message = "Server address not found. Check the URL.";
                } else if (e instanceof java.net.SocketTimeoutException) {
                    message = "Connection timed out. Try again later.";
                } else {
                    message = "Couldn't send data to server";
                }

                handler.post(() ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                );

                Log.e("ServerPoster", "Send failed: " + e.getMessage(), e);
                e.printStackTrace();
            }
        }).start();
    }
}
