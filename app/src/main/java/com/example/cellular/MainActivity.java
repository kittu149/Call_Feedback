package com.example.cellular;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 10;
    private final String[] permissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        } else {
            checkOverlayPermission();
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (allPermissionsGranted()) {
                checkOverlayPermission();
            } else {
                Toast.makeText(this, "Phone and Location permissions are required for this app to function.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkOverlayPermission() {
        findViewById(R.id.btnStartOverlay).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, OverlayPermissionActivity.class));
        });
    }
}