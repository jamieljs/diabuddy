package com.example.diabuddy.siterotation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.diabuddy2021.R;

public class SiteRotationActivity extends AppCompatActivity implements SiteOverviewFragment.OnFragmentInteractionListener, SiteListFragment.OnFragmentInteractionListener {

    static SiteViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_rotation);
        setSupportActionBar(findViewById(R.id.site_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        vm = new ViewModelProvider(this).get(SiteViewModel.class);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}