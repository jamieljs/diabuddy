package com.example.diabuddy.foodtemplate;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.diabuddy.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class FoodTemplateFragment extends Fragment {

    public static FoodTemplateFragment newInstance() {
        return new FoodTemplateFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_food_template, container, false);

        TextView titleTV = view.findViewById(R.id.templates_title);
        titleTV.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + getResources().getString(R.string.templates_welcome));

        ExtendedFloatingActionButton viewTemplates = view.findViewById(R.id.view_templates_button);
        viewTemplates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), FoodTemplateActivity.class);
                startActivity(i);
            }
        });

        return view;
    }
}