package com.example.diabuddy.messages;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MessagesViewModel extends ViewModel {

    private final CollectionReference messagesDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("messages");
    private MutableLiveData<ArrayList<Pair<Integer,String>>> messages = new MutableLiveData<>(new ArrayList<>());
    private final String MESSAGES_KEY = "messages";
    private final String GROUPS_KEY = "groups";
    private final String GROUP_KEY = "group";
    private final String TYPE_KEY = "type";
    private final String CONTENT_KEY = "content";
    public static final int GROUP_SIZE = 50;
    private int curGroup;

    public MessagesViewModel() {
        messagesDB.document(MESSAGES_KEY).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                ArrayList<Pair<Integer, String>> list = new ArrayList<>();
                if (task.isSuccessful()) {
                    try {
                        List<DocumentReference> groups = (List<DocumentReference>) task.getResult().get(GROUPS_KEY);
                        if (groups.size() > 1) {
                            curGroup = groups.size() - 3;
                            DocumentReference secondLastGroup = groups.get(groups.size()-2);
                            secondLastGroup.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task1) {
                                    if (task1.isSuccessful()) {
                                        List<String> contentList = (List<String>) task1.getResult().get(CONTENT_KEY);
                                        List<Integer> typeList = (List<Integer>) task1.getResult().get(TYPE_KEY);
                                        for (int i = 0; i < contentList.size() && i < typeList.size(); i++) {
                                            list.add(new Pair<>(typeList.get(i), contentList.get(i)));
                                        }
                                    }
                                }
                            });
                        } else {
                            curGroup = groups.size() - 2;
                        }
                        DocumentReference lastGroup = groups.get(groups.size()-1);
                        lastGroup.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task1) {
                                if (task1.isSuccessful()) {
                                    List<String> contentList = (List<String>) task1.getResult().get(CONTENT_KEY);
                                    List<Integer> typeList = (List<Integer>) task1.getResult().get(TYPE_KEY);
                                    for (int i = 0; i < contentList.size() && i < typeList.size(); i++) {
                                        list.add(new Pair<>(typeList.get(i), contentList.get(i)));
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e("MessagesViewModel", e.getMessage());
                    }
                }
                messages.setValue(list);
            }
        });
    }

    public MutableLiveData<ArrayList<Pair<Integer, String>>> getMessages() {
        return messages;
    }

    public boolean loadNextPage() {
        if (curGroup < 0) return false;
        messagesDB.document(MESSAGES_KEY).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                ArrayList<Pair<Integer, String>> list = new ArrayList<>();
                if (task.isSuccessful()) {
                    try {
                        List<DocumentReference> groups = (List<DocumentReference>) task.getResult().get(GROUPS_KEY);
                        DocumentReference group = groups.get(curGroup);
                        curGroup--;
                        group.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task1) {
                                if (task1.isSuccessful()) {
                                    List<String> contentList = (List<String>) task1.getResult().get(CONTENT_KEY);
                                    List<Integer> typeList = (List<Integer>) task1.getResult().get(TYPE_KEY);
                                    for (int i = 0; i < contentList.size() && i < typeList.size(); i++) {
                                        list.add(new Pair<>(typeList.get(i), contentList.get(i)));
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e("MessagesViewModel", e.getMessage());
                    }
                }
                list.addAll(messages.getValue());
                messages.setValue(list);
            }
        });
        return true;
    }

    private DocumentReference addMessageToGroup(int type, String msg, int group) {
        messagesDB.document(GROUP_KEY + group).update(CONTENT_KEY, FieldValue.arrayUnion(msg));
        messagesDB.document(GROUP_KEY + group).update(TYPE_KEY, FieldValue.arrayUnion(type));
        return messagesDB.document(GROUP_KEY + group);
    }

    private void addMessage(int type, String msg) {
        messagesDB.document(MESSAGES_KEY).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    try {
                        List<DocumentReference> groups = (List<DocumentReference>) task.getResult().get(GROUPS_KEY);
                        int index = groups.size()-1;
                        DocumentReference lastGroup = groups.get(index);
                        lastGroup.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task1) {
                                if (task1.isSuccessful()) {
                                    List<Integer> typeList = (List<Integer>) task1.getResult().get(TYPE_KEY);
                                    if (typeList.size() == GROUP_SIZE) {
                                        DocumentReference ref = addMessageToGroup(type, msg, index+1);
                                        messagesDB.document(MESSAGES_KEY).update(GROUPS_KEY, FieldValue.arrayUnion(ref));
                                    } else {
                                        addMessageToGroup(type, msg, index);
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e("MessagesViewModel", e.getMessage());
                    }
                }
            }
        });
    }

    public void addConversation(int type, String outgoing, String incoming) {
        addMessage(0, outgoing);
        addMessage(type, incoming);
    }
}