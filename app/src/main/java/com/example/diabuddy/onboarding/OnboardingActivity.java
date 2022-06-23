package com.example.diabuddy.onboarding;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import com.example.diabuddy.R;

public class OnboardingActivity extends AppCompatActivity implements OnboardSettingsFragment.OnFragmentInteractionListener, WalkthroughFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}