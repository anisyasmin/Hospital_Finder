package com.example.hospitalfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Find the GitHub link TextView
        TextView tvDescription = findViewById(R.id.tvDescription);

        // Make "Link to GitHub" clickable
        tvDescription.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/anisyasmin?tab=repositories"));
            startActivity(browserIntent);
        });
    }
}
