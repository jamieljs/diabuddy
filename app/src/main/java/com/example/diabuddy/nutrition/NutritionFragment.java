package com.example.diabuddy.nutrition;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.diabuddy2021.R;
import com.example.diabuddy2021.foodtemplate.FoodTemplateActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

public class NutritionFragment extends Fragment {

    private NutritionViewModel mViewModel;
    private View root;
    private ExtendedFloatingActionButton extendedFAB;

    private ArrayList<Pair<String,String>> result = new ArrayList<>();
    private int selectedIndex = -1;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;

    public static NutritionFragment newInstance() {
        return new NutritionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_nutrition, container, false);

        recyclerView = root.findViewById(R.id.nutrition_recycler_view);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);

        extendedFAB = root.findViewById(R.id.extended_fab);
        extendedFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), FoodTemplateActivity.class);
                i.putExtra("toAdd", result.get(selectedIndex).first + " (100g)");
                ArrayList<String> arr = mViewModel.viewNutrients(selectedIndex);
                try {
                    if (!arr.isEmpty() && arr.get(0).length() > 23) i.putExtra("carbs", Double.parseDouble(arr.get(0).substring(23))); // carbohydrates
                    else i.putExtra("carbs",0);
                } catch (NumberFormatException ex) {
                    i.putExtra("carbs", 0);
                }
                startActivity(i);
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(NutritionViewModel.class);

        TextView instructionsTV = root.findViewById(R.id.nutrition_instructions);
        instructionsTV.setVisibility(View.GONE);

        ConstraintLayout empty = root.findViewById(R.id.nutrition_empty);
        empty.setVisibility(View.VISIBLE);

        EditText searchET = root.findViewById(R.id.query_edit_text);
        ImageButton searchBtn = root.findViewById(R.id.nutrition_search);
        ProgressBar loading = root.findViewById(R.id.nutrition_loading);
        loading.setVisibility(View.GONE);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extendedFAB.setEnabled(false);
                loading.setVisibility(View.VISIBLE);
                String query = searchET.getText().toString();
                if (!query.matches("^[a-zA-Z0-9 ]+$")) {
                    loading.setVisibility(View.GONE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(getString(R.string.invalid_query_text))
                            .setTitle(getString(R.string.invalid_query));
                    AlertDialog invalid = builder.create();
                    invalid.show();
                    return;
                }
                result.clear();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Pair<String,String>> res = mViewModel.query(query);
                        new Handler(Looper.getMainLooper()).post(new Runnable () {
                            @Override
                            public void run () {
                                result = res;
                                if (res.isEmpty()) {
                                    empty.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                    instructionsTV.setVisibility(View.GONE);
                                } else {
                                    empty.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    instructionsTV.setVisibility(View.VISIBLE);
                                }
                                adapter.notifyDataSetChanged();
                                loading.setVisibility(View.GONE);
                            }
                        });
                    }
                });
                thread.start();
            }
        });
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_nutrition, parent,false);
            ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.name.setText(result.get(position).first);
            holder.desc.setText(result.get(position).second);
            if (holder.getAdapterPosition() == selectedIndex) {
                ArrayList<String> arr = mViewModel.viewNutrients(holder.getAdapterPosition());
                for (int i = 0; i < arr.size(); i++) {
                    ((TextView)holder.nutrientLayout.getChildAt(i)).setText(arr.get(i));
                }
                holder.nutrientLayout.setVisibility(View.VISIBLE);
                holder.item.setCardBackgroundColor(getResources().getColor(R.color.orange, getContext().getTheme()));
            } else {
                holder.nutrientLayout.setVisibility(View.GONE);
                holder.item.setCardBackgroundColor(getResources().getColor(R.color.teal_extra_light, getContext().getTheme()));
            }
        }

        private View expandedView = null;
        private LinearLayout expandedLayout = null;

        @Override
        public int getItemCount() {
            return result.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            TextView name;
            TextView desc;
            LinearLayout nutrientLayout;
            CardView item;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                item = (CardView) itemView;
                name = itemView.findViewById(R.id.nutrition_name);
                desc = itemView.findViewById(R.id.nutrition_desc);
                nutrientLayout = itemView.findViewById(R.id.nutrient_layout);

                itemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        if (nutrientLayout.getVisibility() == View.GONE) {
                            if (selectedIndex != -1 && expandedLayout.getVisibility() == View.VISIBLE) { // a different one within the frame was selected
                                expandedView.callOnClick();
                            }
                            selectedIndex = getAdapterPosition();
                            expandedView = itemView;
                            expandedLayout = nutrientLayout;
                            extendedFAB.setEnabled(true);
                            ArrayList<String> arr = mViewModel.viewNutrients(getAdapterPosition());
                            for (int i = 0; i < arr.size(); i++) {
                                System.out.println(nutrientLayout.getChildCount());
                                ((TextView)nutrientLayout.getChildAt(i)).setText(arr.get(i));
                            }
                            nutrientLayout.setVisibility(View.VISIBLE);
                            item.setCardBackgroundColor(getResources().getColor(R.color.orange, getContext().getTheme()));
                        } else {
                            selectedIndex = -1;
                            expandedView = null;
                            expandedLayout = null;
                            extendedFAB.setEnabled(false);
                            nutrientLayout.setVisibility(View.GONE);
                            item.setCardBackgroundColor(getResources().getColor(R.color.teal_extra_light, getContext().getTheme()));
                        }
                    }
                });
            }
        }
    }
}