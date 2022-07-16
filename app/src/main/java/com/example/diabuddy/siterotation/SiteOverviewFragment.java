package com.example.diabuddy.siterotation;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.diabuddy.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SiteOverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SiteOverviewFragment extends Fragment {

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    public static SiteOverviewFragment newInstance() {
        return new SiteOverviewFragment();
    }

    private TextView tv;
    private Button useThisBtn;
    private int index = -1;
    private ArrayList<SiteViewModel.Site> list = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_site_overview, container, false);

        tv = view.findViewById(R.id.next_site_name_tv);
        useThisBtn = view.findViewById(R.id.use_this_site_button);
        useThisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index == -1) return;
                SiteRotationActivity.vm.markOne(list.get(index).getId(), true);
            }
        });

        Button editSitesBtn = view.findViewById(R.id.edit_sites_button);
        editSitesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_siteOverviewFragment_to_siteListFragment);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        final Observer<ArrayList<SiteViewModel.Site>> siteObserver = new Observer<ArrayList<SiteViewModel.Site>>() {
            @Override
            public void onChanged(ArrayList<SiteViewModel.Site> sites) {
                list.clear();
                list.addAll(sites);
                for (int i = 0; i < sites.size(); i++) {
                    if (!sites.get(i).getUsed()) {
                        tv.setText(sites.get(i).getName());
                        index = i;
                        useThisBtn.setEnabled(true);
                        return;
                    }
                }
                tv.setText(getString(R.string.no_unused_site));
                index = -1;
                useThisBtn.setEnabled(false);
            }
        };
        SiteRotationActivity.vm.getSiteList().observe(getViewLifecycleOwner(), siteObserver);
    }
}