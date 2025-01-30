package com.example.hospitalfinder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView tvGreeting, tvPlaceName, tvLatLong;
    private String firstName, lastName, state, district;
    private double latitude, longitude;
    private String username;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        tvGreeting = findViewById(R.id.tvGreeting);
        tvPlaceName = findViewById(R.id.tvPlaceName);
        tvLatLong = findViewById(R.id.tvLatLong);

        Button btnFindHospital = findViewById(R.id.btnFindHospital);
        Button btnFindPharmacy = findViewById(R.id.btnPharmacy);
        Button btnCheckIn = findViewById(R.id.btnCheckIn);
        Button btnAbout = findViewById(R.id.btnAbout);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Retrieve username from Intent
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Username not found. Please log in again.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            Log.d("MainActivity", "Fetching data for username: " + username);
            new FetchUserLocationTask().execute(username);
        }

        // Button Click Actions
        btnFindHospital.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HospitalMapActivity.class)));
        btnFindPharmacy.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PharmacyMapActivity.class)));
        btnCheckIn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CheckInActivity.class)));
        btnAbout.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AboutActivity.class)));
        btnLogout.setOnClickListener(v -> {
            Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logoutIntent);
            finish();
        });
    }

    // ✅ Fetch Stored User Location from the Database
    @SuppressLint("StaticFieldLeak")
    private class FetchUserLocationTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String result = "";
            try {
                // Make API request to fetch user location
                URL url = new URL("http://10.0.2.2/ICT602/hospital_finder/getUserLocation.php?username=" + username);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                result = stringBuilder.toString();
                reader.close();
                inputStream.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("API Response", "Raw JSON: " + result);

            if (result == null || result.isEmpty()) {
                Toast.makeText(MainActivity.this, "Server returned empty response", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);

                if (!jsonObject.has("success") || !jsonObject.getBoolean("success")) {
                    Toast.makeText(MainActivity.this, "Error: " + jsonObject.optString("message", "Unknown error"), Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject data = jsonObject.getJSONObject("data");

                firstName = data.optString("first_name", "N/A");
                lastName = data.optString("last_name", "N/A");
                state = data.optString("state", "N/A");
                district = data.optString("district", "N/A");
                latitude = data.optDouble("latitude", 0.0); // ✅ Fetch stored latitude
                longitude = data.optDouble("longitude", 0.0); // ✅ Fetch stored longitude

                // ✅ Debugging Logs
                Log.d("Fetched Data", "First Name: " + firstName);
                Log.d("Fetched Data", "Last Name: " + lastName);
                Log.d("Fetched Data", "State: " + state);
                Log.d("Fetched Data", "District: " + district);
                Log.d("Fetched Data", "Latitude: " + latitude);
                Log.d("Fetched Data", "Longitude: " + longitude);

                // ✅ Display stored latitude & longitude
                runOnUiThread(() -> {
                    tvGreeting.setText("Welcome, " + firstName + " " + lastName + "!");
                    tvPlaceName.setText("\nYour Location:\n" + district + ", " + state);
                    tvLatLong.setText("\nLatitude: " + latitude + "\nLongitude: " + longitude);
                });

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON_ERROR", "Error parsing JSON: " + result);
                Toast.makeText(MainActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
