package com.example.diabuddy.challenges;

import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.diabuddy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ChallengesFragment extends Fragment {

    private int[] challenges = {R.string.seventy_challenge, R.string.eighty_challenge, R.string.ninety_challenge,
            R.string.hundred_challenge, R.string.notes_challenge, R.string.nutrition_challenge};
    private int[] descID = {R.string.seventy_desc, R.string.eighty_desc, R.string.ninety_desc, R.string.hundred_desc,
            R.string.notes_desc, R.string.nutrition_desc};
    private ArrayList<Integer> count = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0));
    private final DocumentReference challengeDB = FirebaseFirestore.getInstance().collection("users")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
    private final String SEVENTY_KEY = "challenges-70";
    private final String EIGHTY_KEY = "challenges-80";
    private final String NINETY_KEY = "challenges-90";
    private final String HUNDRED_KEY = "challenges-100";
    private final String NOTES_KEY = "challenges-notes";
    private final String NUTRITION_KEY = "challenges-nutrition";

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;

    public ChallengesFragment() { }

    public static ChallengesFragment newInstance() {
        return new ChallengesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_challenges, container, false);

        recyclerView = view.findViewById(R.id.challenges_recycler_view);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);

        challengeDB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        count.set(0, ((List) data.get(SEVENTY_KEY)).size());
                        count.set(1, ((List) data.get(EIGHTY_KEY)).size());
                        count.set(2, ((List) data.get(NINETY_KEY)).size());
                        count.set(3, ((List) data.get(HUNDRED_KEY)).size());
                        count.set(4, ((List) data.get(NOTES_KEY)).size());
                        count.set(5, (Integer) data.get(NUTRITION_KEY));
                        adapter.notifyDataSetChanged();
                        return;
                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setMessage("Error getting data. Please exit this page and retry.")
                        .setTitle("Error");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        challengeDB.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e == null && snapshot != null && snapshot.exists()) {
                    Map<String, Object> data = snapshot.getData();
                    count.set(0, ((List) data.get(SEVENTY_KEY)).size());
                    count.set(1, ((List) data.get(EIGHTY_KEY)).size());
                    count.set(2, ((List) data.get(NINETY_KEY)).size());
                    count.set(3, ((List) data.get(HUNDRED_KEY)).size());
                    count.set(4, ((List) data.get(NOTES_KEY)).size());
                    count.set(5, (Integer) data.get(NUTRITION_KEY));
                    adapter.notifyDataSetChanged();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setMessage("Error getting data. Please exit this page and retry.")
                        .setTitle("Error");
                AlertDialog invalidLoginDialog = builder.create();
                invalidLoginDialog.show();
            }
        });

        return view;
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_challenge, parent,false);
            ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.challenge.setText(getResources().getString(challenges[position]));
            holder.timesAchieved.setText(String.valueOf(count.get(position)));
            int color = R.color.red;
            if (count.get(position) >= 100) color = R.color.teal_extra_light;
            else if (count.get(position) >= 60) color = R.color.yellow;
            else if (count.get(position) >= 30) color = R.color.orange;
            holder.item.setCardBackgroundColor(getResources().getColor(color, requireContext().getTheme()));
        }

        @Override
        public int getItemCount() {
            return challenges.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            CardView item;
            TextView challenge;
            TextView timesAchieved;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                item = (CardView) itemView;
                challenge = itemView.findViewById(R.id.challenge_tv);
                timesAchieved = itemView.findViewById(R.id.times_achieved_tv);

                itemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        View dialogView = View.inflate(view.getContext(), R.layout.dialog_challenge,null);
                        TextView descTV = dialogView.findViewById(R.id.challenge_dialog_tv);
                        descTV.setText(getResources().getString(descID[getAdapterPosition()]));

                        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        builder.setView(dialogView);
                        builder.create().show();
                    }
                });

            }
        }
    }

}