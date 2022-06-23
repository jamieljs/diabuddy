package com.example.diabuddy.foodtemplate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.diabuddy.R;

import java.util.ArrayList;

public class FoodItemFragment extends Fragment {

    public FoodItemFragment() {
        // Required empty public constructor
    }

    public static FoodItemFragment newInstance() {
        FoodItemFragment fragment = new FoodItemFragment();
        return fragment;
    }

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private ArrayList<FoodItem> items = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_food_item, container, false);

        TextView instructionsTV = root.findViewById(R.id.food_item_instructions);
        instructionsTV.setVisibility(View.GONE);

        ConstraintLayout empty = root.findViewById(R.id.food_item_empty);
        empty.setVisibility(View.GONE);

        recyclerView = root.findViewById(R.id.food_item_recycler_view);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);

        final Observer<ArrayList<FoodItem>> observer = new Observer<ArrayList<FoodItem>>() {
            @Override
            public void onChanged(ArrayList<FoodItem> arr) {
                items = arr;
                if (items.isEmpty()) {
                    empty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    instructionsTV.setVisibility(View.GONE);
                } else {
                    empty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    instructionsTV.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }
        };
        SectionsPagerAdapter.vm.getFoodItems().observe(getViewLifecycleOwner(), observer);

        return root;
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_food_item, parent,false);
            ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.curItem = items.get(position);
            holder.name.setText(holder.curItem.getName());
            holder.carbs.setText(String.valueOf(holder.curItem.getCarbs()));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            FoodItem curItem;
            TextView name;
            TextView carbs;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.food_item_name);
                carbs = itemView.findViewById(R.id.food_carb);

                itemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        View dialogView = View.inflate(view.getContext(), R.layout.dialog_food_item,null);
                        EditText nameET = dialogView.findViewById(R.id.name_edit_text);
                        nameET.setText(name.getText());
                        EditText carbsET = dialogView.findViewById(R.id.carbs_edit_text);
                        carbsET.setText(carbs.getText());
                        TextView errorTV = dialogView.findViewById(R.id.food_item_error);
                        errorTV.setVisibility(View.GONE);

                        System.out.println(curItem.getId());

                        builder.setNegativeButton(getString(R.string.discard_edit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.setPositiveButton(getString(R.string.save_edit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        builder.setView(dialogView);
                        AlertDialog dialog = builder.create();
                        dialog.show();

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (nameET.getText().toString().equals("")) {
                                    errorTV.setVisibility(View.VISIBLE);
                                    errorTV.setText(getResources().getString(R.string.empty_food_name_error));
                                } else if (carbsET.getText().toString().equals("")) {
                                    errorTV.setVisibility(View.VISIBLE);
                                    errorTV.setText(getResources().getString(R.string.empty_food_carbs_error));
                                } else if (!SectionsPagerAdapter.vm.modifyFoodItem(curItem.getId(), String.valueOf(nameET.getText()), Double.parseDouble(String.valueOf(carbsET.getText())))) {
                                    errorTV.setVisibility(View.VISIBLE);
                                    errorTV.setText(getResources().getString(R.string.food_item_error));
                                } else {
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                });

            }
        }
    }
}