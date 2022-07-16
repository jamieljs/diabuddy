package com.example.diabuddy.siterotation;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.diabuddy.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class SiteRotationFragment extends Fragment {

    public static SiteRotationFragment newInstance() {
        return new SiteRotationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_site_rotation, container, false);

        TextView titleTV = view.findViewById(R.id.sites_title);
        titleTV.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + getResources().getString(R.string.sites_welcome));

        TextView linkTV = view.findViewById(R.id.sites_description);
        linkTV.setMovementMethod(LinkMovementMethod.getInstance());

        ExtendedFloatingActionButton viewTemplates = view.findViewById(R.id.view_sites_button);
        viewTemplates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SiteRotationActivity.class);
                startActivity(i);
            }
        });

        return view;
    }
}