package com.example.hospitalfinder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserLocationActivity extends AppCompatActivity {

    private EditText stateInput, districtInput;
    private Button submitButton;
    private String username, firstName, lastName;
    private double latitude = 0.0, longitude = 0.0;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location);

        stateInput = findViewById(R.id.stateInput);
        districtInput = findViewById(R.id.districtInput);
        submitButton = findViewById(R.id.submitLocation);

        // Retrieve user data from intent
        Intent intent = getIntent();
        firstName = intent.getStringExtra("first_name");
        lastName = intent.getStringExtra("last_name");
        username = intent.getStringExtra("username");

        // Initialize Location Provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation(); // Fetch location on start

        submitButton.setOnClickListener(v -> saveUserLocation());
    }

    // ✅ Fetch current location (Latitude & Longitude)
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                districtInput.setText(getDistrictName(latitude, longitude)); // Autofill district
                Log.d("LOCATION", "Lat: " + latitude + ", Lng: " + longitude);
            } else {
                Log.e("LOCATION", "Failed to get location");
            }
        });
    }

    // ✅ Get district name from Latitude & Longitude
    private String getDistrictName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                return addresses.get(0).getSubAdminArea(); // District Name
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown District";
    }

    // ✅ Save User Location to Database
    private void saveUserLocation() {
        String state = stateInput.getText().toString().trim();
        String district = districtInput.getText().toString().trim();

        if (state.isEmpty() || district.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Debugging Logs
        Log.d("API_REQUEST", "Sending username: " + username);
        Log.d("API_REQUEST", "State: " + state + ", District: " + district + ", Latitude: " + latitude + ", Longitude: " + longitude);

        APIService apiService = RetrofitClient.getAPIService();
        Call<ServerResponse> call = apiService.saveUserLocation(username, state, district, latitude, longitude);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Log.d("API_RESPONSE", "Location saved successfully");
                        Toast.makeText(UserLocationActivity.this, "Location Saved!", Toast.LENGTH_SHORT).show();

                        // Navigate to MainActivity
                        Intent intent = new Intent(UserLocationActivity.this, MainActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("first_name", firstName);
                        intent.putExtra("last_name", lastName);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e("API_ERROR", "Error saving location: " + response.body().getMessage());
                        Toast.makeText(UserLocationActivity.this, "Error saving location", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("API_ERROR", "Response error");
                    Toast.makeText(UserLocationActivity.this, "Response Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Log.e("API_ERROR", "Network failure: " + t.getMessage());
                Toast.makeText(UserLocationActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }
}
