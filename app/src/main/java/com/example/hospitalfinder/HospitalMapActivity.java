package com.example.hospitalfinder;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class HospitalMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String API_URL = "http://10.0.2.2/ICT602/hospital_finder/get_hospitals.php?json=1"; // Ensure JSON response

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_map);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Fetch hospital locations in the background
        new Thread(this::fetchHospitalLocations).start();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void fetchHospitalLocations() {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // Read response
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();

            String response = result.toString();
            Log.d("API_RESPONSE", "Response: " + response); // Log response for debugging

            parseJson(response);

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Failed to retrieve data", Toast.LENGTH_SHORT).show());
        }
    }

    private void parseJson(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            ArrayList<LatLng> hospitalLocations = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.optString("name", "Unknown Hospital");
                double latitude = obj.optDouble("latitude", 0.0);
                double longitude = obj.optDouble("longitude", 0.0);

                if (latitude != 0.0 && longitude != 0.0) {
                    LatLng location = new LatLng(latitude, longitude);
                    hospitalLocations.add(location);

                    runOnUiThread(() -> mMap.addMarker(new MarkerOptions().position(location).title(name)));
                }
            }

            // Move camera to the first hospital location if available
            if (!hospitalLocations.isEmpty()) {
                runOnUiThread(() -> mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hospitalLocations.get(0), 12)));
            } else {
                runOnUiThread(() -> Toast.makeText(this, "No hospital locations found", Toast.LENGTH_SHORT).show());
            }

        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Error parsing JSON data", Toast.LENGTH_SHORT).show());
        }
    }
}
