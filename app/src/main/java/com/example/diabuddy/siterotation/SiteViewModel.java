package com.example.diabuddy.siterotation;

import android.renderscript.Sampler;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SiteViewModel extends ViewModel {

    protected class Site implements Comparable<Site> {
        String name;
        boolean used;
        int id;

        public Site(String name, boolean used, int id) {
            this.name = name;
            this.used = used;
            this.id = id;
        }

        @Override
        public int compareTo(Site o) {
            return this.id - o.id;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public boolean getUsed() {
            return used;
        }
    }

    private final CollectionReference sitesDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("sites");
    private final String NAME_KEY = "name";
    private final String USED_KEY = "used";
    private final String INDEX_KEY = "index";

    // name, used, index
    private MutableLiveData<ArrayList<Site>> siteList = new MutableLiveData<>(new ArrayList<>());

    public SiteViewModel() {
        sitesDB.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<Site> sites = new ArrayList<>();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        sites.add(doc.toObject(Site.class));
                    }
                }
                Collections.sort(sites);
                siteList.setValue(sites);
            }
        });
        sitesDB.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                ArrayList<Site> sites = new ArrayList<>();
                if (error == null && value != null && !value.isEmpty()) {
                    for (QueryDocumentSnapshot doc : value) {
                        if (doc.get(NAME_KEY) != null) {
                            sites.add(doc.toObject(Site.class));
                        }
                    }
                }
                Collections.sort(sites);
                siteList.setValue(sites);
            }
        });
    }

    public MutableLiveData<ArrayList<Site>> getSiteList() {
        return siteList;
    }

    public void addSite(String name) {
        int size = siteList.getValue().size();
        Site site = new Site(name, false, siteList.getValue().get(size - 1).getId() + 1);
        sitesDB.add(site);
    }

    public void deleteAll() {
        sitesDB.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        sitesDB.document(doc.getId()).delete();
                    }
                }
            }
        });
    }

    public void markOne(int index, boolean isChecked) {
        sitesDB.whereEqualTo(INDEX_KEY, index)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                sitesDB.document(doc.getId()).update(USED_KEY, isChecked);
                            }
                        }
                    }
                });
    }

    public void deleteOne(int index) {
        sitesDB.whereEqualTo(INDEX_KEY, index)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                sitesDB.document(doc.getId()).delete();
                            }
                        }
                    }
                });
    }

    public void reset() {
        sitesDB.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        sitesDB.document(doc.getId()).update(USED_KEY, false);
                    }
                }
            }
        });
    }
}
