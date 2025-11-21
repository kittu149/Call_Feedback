package com.example.cellular;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FormLocalSaver {

    private static final String FILENAME = "form_responses.jsonl";

    public static void saveResponse(Context context,
                                    String q1,
                                    String q2,
                                    String q3,
                                    String q4,
                                    String location,
                                    String areaInfo,
                                    String networkType,
                                    String signalStrength) {
        JSONObject response = new JSONObject();
        try {
            response.put("overall_quality", q1);
            response.put("audio_issues", q2);
            response.put("environment", q3);
            response.put("comments", q4);
            response.put("location", location);
            response.put("area_info", areaInfo);
            response.put("connection_type", networkType);
            response.put("signal_strength", signalStrength);
            response.put("timestamp", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        File file = new File(context.getFilesDir(), FILENAME);
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(response.toString());
            fw.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
