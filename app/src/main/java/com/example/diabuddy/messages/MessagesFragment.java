package com.example.diabuddy.messages;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.diabuddy.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class MessagesFragment extends Fragment {

    public static MessagesFragment newInstance() {
        return new MessagesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        TextView titleTV = view.findViewById(R.id.messages_title);
        titleTV.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + getResources().getString(R.string.messages_welcome));

        ExtendedFloatingActionButton viewMessages = view.findViewById(R.id.view_messages_button);
        viewMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), MessagesActivity.class);
                startActivity(i);
            }
        });

        return view;
    }

}