package com.example.hospitalfinder;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class CheckInActivity extends AppCompatActivity {

    private EditText editFirstname, editLastname, editDistrict, editState, editSymptoms, editHospital;
    private Button btnCheckIn, btnClear;

    // Use HTTP for local testing, replace with HTTPS in production
    private static final String CHECKIN_URL = "http://10.0.2.2/ICT602/hospital_finder/checkin.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);

        // Initialize UI elements
        editFirstname = findViewById(R.id.editFirstname);
        editLastname = findViewById(R.id.editLastname);
        editDistrict = findViewById(R.id.editDistrict);
        editState = findViewById(R.id.editState);
        editSymptoms = findViewById(R.id.editSymptoms);
        editHospital = findViewById(R.id.editHospital);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnClear = findViewById(R.id.btnClear);

        // Check-in button action
        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCheckIn();
            }
        });

        // Clear button action
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
            }
        });
    }

    private void saveCheckIn() {
        final String firstname = editFirstname.getText().toString().trim();
        final String lastname = editLastname.getText().toString().trim();
        final String district = editDistrict.getText().toString().trim();
        final String state = editState.getText().toString().trim();
        final String symptoms = editSymptoms.getText().toString().trim();
        final String hospitalName = editHospital.getText().toString().trim();

        if (firstname.isEmpty() || lastname.isEmpty() || district.isEmpty() || state.isEmpty() || hospitalName.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        new CheckInTask().execute(firstname, lastname, district, state, symptoms, hospitalName);
    }

    private void clearFields() {
        editFirstname.setText("");
        editLastname.setText("");
        editDistrict.setText("");
        editState.setText("");
        editSymptoms.setText("");
        editHospital.setText("");
    }

    private class CheckInTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(CHECKIN_URL);
                HttpURLConnection conn;

                if (CHECKIN_URL.startsWith("https")) {
                    // Handle HTTPS connections and bypass SSL for self-signed certificates (Testing only)
                    HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    }}, new java.security.SecureRandom());
                    httpsConn.setSSLSocketFactory(sslContext.getSocketFactory());
                    httpsConn.setHostnameVerifier((hostname, session) -> true); // Bypass hostname verification
                    conn = httpsConn;
                } else {
                    // Handle HTTP connections
                    conn = (HttpURLConnection) url.openConnection();
                }

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                // Prepare POST data
                String postData = "firstname=" + URLEncoder.encode(params[0], "UTF-8") +
                        "&lastname=" + URLEncoder.encode(params[1], "UTF-8") +
                        "&district=" + URLEncoder.encode(params[2], "UTF-8") +
                        "&state=" + URLEncoder.encode(params[3], "UTF-8") +
                        "&symptoms=" + URLEncoder.encode(params[4], "UTF-8") +
                        "&hospital_name=" + URLEncoder.encode(params[5], "UTF-8");

                // Send POST data
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.writeBytes(postData);
                out.flush();
                out.close();

                // Read server response
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.contains("success")) {
                Toast.makeText(CheckInActivity.this, "User check-in feedback posted", Toast.LENGTH_SHORT).show();
                clearFields(); // Clear input fields after successful check-in
            } else {
                Toast.makeText(CheckInActivity.this, "Error: " + result, Toast.LENGTH_LONG).show();
                Log.e("CheckInError", result); // Log error for debugging
            }
        }
    }
}
