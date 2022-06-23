package com.example.diabuddy.about;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.diabuddy.FeedbackActivity;
import com.example.diabuddy.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

public class AboutActivity extends AppCompatActivity {

    private AppBarLayout appbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setSupportActionBar(findViewById(R.id.about_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String url = "https://docs.google.com/document/d/19dVnmK1b_0_nI3Ljy4-0t4fImZ9wl6vzCprVWu-aOxM/edit?usp=sharing";
        Button docBtn = findViewById(R.id.open_app_guide_button);
        docBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri urlLink = Uri.parse(url);
                Intent i = new Intent(Intent.ACTION_VIEW,urlLink);
                startActivity(i);
            }
        });

        Button btn = findViewById(R.id.feedback_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AboutActivity.this, FeedbackActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}