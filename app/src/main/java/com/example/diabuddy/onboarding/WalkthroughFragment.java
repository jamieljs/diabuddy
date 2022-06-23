package com.example.diabuddy.onboarding;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.diabuddy.R;
import com.example.diabuddy.UserActivity;
import com.google.android.material.tabs.TabLayout;

public class WalkthroughFragment extends Fragment {

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public WalkthroughFragment() {
        // Required empty public constructor
    }

    public static WalkthroughFragment newInstance() {
        WalkthroughFragment fragment = new WalkthroughFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_walkthrough, container, false);
        // Initialize ViewPager view
        ViewPager viewPager = root.findViewById(R.id.viewPagerOnBoarding);
        // create ViewPager adapter
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager());

        // Set Adapter for ViewPager
        viewPager.setAdapter(viewPagerAdapter);

        // Setup dot's indicator
        TabLayout tabLayout = root.findViewById(R.id.tabLayoutIndicator);
        tabLayout.setupWithViewPager(viewPager);

        Button backBtn = root.findViewById(R.id.back_button);
        backBtn.setEnabled(false);
        Button nextBtn = root.findViewById(R.id.next_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = tabLayout.getSelectedTabPosition();
                tabLayout.selectTab(tabLayout.getTabAt(pos - 1));
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = tabLayout.getSelectedTabPosition();
                if (pos == 4) {
                    Intent i = new Intent(getActivity(), UserActivity.class);
                    startActivity(i);
                    getActivity().finish();
                } else {
                    tabLayout.selectTab(tabLayout.getTabAt(pos + 1));
                }
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) backBtn.setEnabled(false);
                else backBtn.setEnabled(true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return root;
    }

    // ViewPager Adapter class
    class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int i) {
            System.out.println("getItem" + i);
            switch (i) {
                case 0: return new OnboardTrendsFragment();
                case 1: return new OnboardMessagesFragment();
                case 2: return new OnboardSitesFragment();
                case 3: return new OnboardFoodFragment();
                default: return new OnboardChallengesFragment();
            }
        }

        @Override
        public int getCount() {
            return 5;
        }

    }
}