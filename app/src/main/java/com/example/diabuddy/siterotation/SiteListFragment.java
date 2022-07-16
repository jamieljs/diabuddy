package com.example.diabuddy.siterotation;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diabuddy.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SiteListFragment extends Fragment {

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    public static SiteListFragment newInstance() {
        return new SiteListFragment();
    }

    private boolean isFabOpen = false;

    private View view;

    private SiteViewModel vm;
    private LinearLayout addLL, deleteAllLL, resetLL;
    private FloatingActionButton addFAB, deleteAllFAB, resetFAB, mainFAB;
    private TextView addTV, deleteAllTV, resetTV;

    private ConstraintLayout empty;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private ArrayList<SiteViewModel.Site> siteList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_site_list, container, false);
        vm = SiteRotationActivity.vm;

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                if (isFabOpen) disappearAnim();
                else requireActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        Button backToOverviewBtn = view.findViewById(R.id.back_to_overview_button);
        backToOverviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFabOpen = false;
                Navigation.findNavController(view).navigate(R.id.action_siteListFragment_to_siteOverviewFragment);
            }
        });

        empty = view.findViewById(R.id.messages_empty);
        empty.setVisibility(View.GONE);

        recyclerView = view.findViewById(R.id.site_recycler_view);
        recyclerView.setVisibility(View.GONE);
        layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);

        addLL = view.findViewById(R.id.add_ll);
        deleteAllLL = view.findViewById(R.id.delete_all_ll);
        resetLL = view.findViewById(R.id.reset_ll);
        addFAB = view.findViewById(R.id.add_fab);
        deleteAllFAB = view.findViewById(R.id.delete_all_fab);
        resetFAB = view.findViewById(R.id.reset_fab);
        mainFAB = view.findViewById(R.id.fab);
        addTV = view.findViewById(R.id.add_label);
        deleteAllTV = view.findViewById(R.id.delete_all_label);
        resetTV = view.findViewById(R.id.reset_label);
        mainFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFabOpen) {
                    disappearAnim();
                } else {
                    appearAnim();
                }
            }
        });
        addFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAdd();
                disappearAnim();
            }
        });
        deleteAllFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleDeleteAll();
                disappearAnim();
            }
        });
        resetFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleReset();
                disappearAnim();
            }
        });

        final Observer<ArrayList<SiteViewModel.Site>> siteObserver = new Observer<ArrayList<SiteViewModel.Site>>() {
            @Override
            public void onChanged(ArrayList<SiteViewModel.Site> sites) {
                siteList.clear();
                siteList.addAll(sites);

                adapter.notifyDataSetChanged();
                if (siteList.isEmpty()) {
                    empty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    empty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        };
        vm.getSiteList().observe(getViewLifecycleOwner(), siteObserver);

        return view;
    }

    private void handleAdd() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = View.inflate(requireContext(), R.layout.dialog_add_site,null);

        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText siteET = dialogView.findViewById(R.id.site_name_edit_text);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = siteET.getText().toString();
                vm.addSite(name);
                dialog.dismiss();
            }
        });
    }

    private void handleDeleteAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(getString(R.string.delete_all_confirmation));
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(getString(R.string.delete_all), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                vm.deleteAll();
            }
        });
        builder.create().show();
    }

    private void handleMarkOne(int index, boolean isChecked) {
        vm.markOne(index, isChecked);
    }

    private void handleDeleteOne(int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(getString(R.string.delete_one_confirmation));
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                vm.deleteOne(index);
            }
        });
        builder.create().show();
    }

    private void handleReset() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(getString(R.string.reset_sites_confirmation));
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(getString(R.string.reset), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                vm.reset();
            }
        });
        builder.create().show();
    }

    private void appearAnim() {
        isFabOpen = true;
        ObjectAnimator.ofFloat(mainFAB, "rotation", 0f, 180f).setDuration(400).start();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mainFAB.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_close_white_24dp, requireContext().getTheme()));
            }
        }, 200);

        addTV.setVisibility(View.VISIBLE);
        deleteAllTV.setVisibility(View.VISIBLE);
        resetTV.setVisibility(View.VISIBLE);

        resetLL.animate().translationY(-getResources().getDimension(R.dimen.fab_up_1));
        deleteAllLL.animate().translationY(-getResources().getDimension(R.dimen.fab_up_2));
        addLL.animate().translationY(-getResources().getDimension(R.dimen.fab_up_3));
    }

    private void disappearAnim() {
        isFabOpen = false;
        ObjectAnimator.ofFloat(mainFAB, "rotation", 180f, 0f).setDuration(400).start();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mainFAB.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_construction_white_24dp, requireContext().getTheme()));
            }
        }, 200);

        addTV.setVisibility(View.GONE);
        deleteAllTV.setVisibility(View.GONE);
        resetTV.setVisibility(View.GONE);

        resetLL.animate().translationY(0);
        deleteAllLL.animate().translationY(0);
        addLL.animate().translationY(0);
    }

    class RecyclerAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_site, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            ViewHolder holder = (ViewHolder) viewHolder;
            if (siteList.size() > position) {
                holder.checkBox.setText(siteList.get(holder.getAdapterPosition()).getName());
                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked != siteList.get(holder.getAdapterPosition()).getUsed()) handleMarkOne(siteList.get(holder.getAdapterPosition()).getId(), isChecked);
                    }
                });
                holder.checkBox.setChecked(siteList.get(holder.getAdapterPosition()).getUsed());
                holder.delBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleDeleteOne(siteList.get(holder.getAdapterPosition()).getId());
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return siteList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;
            ImageButton delBtn;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.site_check_box);
                delBtn = itemView.findViewById(R.id.delete_site_button);
            }
        }
    }

}