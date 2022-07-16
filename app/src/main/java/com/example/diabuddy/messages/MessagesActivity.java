package com.example.diabuddy.messages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diabuddy.AlarmReceiver;
import com.example.diabuddy.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

public class MessagesActivity extends AppCompatActivity {

    private MessagesViewModel vm;
    private MutableLiveData<Boolean> isFABOpen = new MutableLiveData<>(false);
    private LinearLayout faqLL, tipsLL, encouragementLL, remindersLL;
    private FloatingActionButton faqFAB, tipsFAB, encouragementFAB, remindersFAB, mainFAB;
    private TextView faqTV, tipsTV, encouragementTV, remindersTV;
    private ArrayList<Pair<Integer,String>> messages = new ArrayList<>();

    private ProgressBar loadingPB;
    private NestedScrollView nestedSV;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        setSupportActionBar(findViewById(R.id.messages_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        vm = new ViewModelProvider(this, getDefaultViewModelProviderFactory()).get(MessagesViewModel.class);

        Bundle extras = getIntent().getExtras();
        if (extras != null) { // lol the indexing here is off by one from below, oops
            int queryType = extras.getInt("queryType");
            if (queryType == 0) { // faq
                int questionIndex = extras.getInt("questionIndex");
                handleFaq(questionIndex);
            } else if (queryType == 1) { // tips
                handleTips();
            } else if (queryType == 2) { // encouragement
                handleEncouragement();
            }
        }

        ConstraintLayout empty = findViewById(R.id.messages_empty);
        empty.setVisibility(View.GONE);

        loadingPB = findViewById(R.id.messages_progressbar);
        nestedSV = findViewById(R.id.messages_nestedSV);
        nestedSV.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == 0) { // v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    loadingPB.setVisibility(View.VISIBLE);
                    if (vm.loadNextPage()) {
                        loadingPB.setVisibility(View.GONE);
                        recyclerView.scrollToPosition(vm.GROUP_SIZE);
                    } else {
                        loadingPB.setVisibility(View.GONE);
                    }
                }
            }
        });

        recyclerView = findViewById(R.id.messages_recycler_view);
        nestedSV.setVisibility(View.GONE);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);

        // 1-indexed... supposedly
        // 0: message from user
        // 1: faq answer (teal)
        // 2: tip (teal)
        // 3: encouragement (teal)
        // 4: reminders (orange)
        // 5: hypo-related warning (red)
        // 6: hyper-related warning (yellow)
        final Observer<ArrayList<Pair<Integer,String>>> observer = new Observer<ArrayList<Pair<Integer, String>>>() {
            @Override
            public void onChanged(ArrayList<Pair<Integer, String>> pairs) {
                messages.clear();
                if (pairs.isEmpty()) {
                    empty.setVisibility(View.VISIBLE);
                    nestedSV.setVisibility(View.GONE);
                    return;
                }
                empty.setVisibility(View.GONE);
                nestedSV.setVisibility(View.VISIBLE);
                messages.addAll(pairs);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
            }
        };
        vm.getMessages().observe(this, observer);

        faqLL = findViewById(R.id.faq_ll);
        tipsLL = findViewById(R.id.tips_ll);
        encouragementLL = findViewById(R.id.encouragement_ll);
        remindersLL = findViewById(R.id.reminder_ll);
        faqFAB = findViewById(R.id.faq_fab);
        tipsFAB = findViewById(R.id.tips_fab);
        encouragementFAB = findViewById(R.id.encouragement_fab);
        remindersFAB = findViewById(R.id.reminder_fab);
        mainFAB = findViewById(R.id.fab);
        faqTV = findViewById(R.id.faq_label);
        tipsTV = findViewById(R.id.tips_label);
        encouragementTV = findViewById(R.id.encouragement_label);
        remindersTV = findViewById(R.id.reminder_label);
        mainFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFABOpen.getValue()) {
                    disappearAnim();
                } else {
                    appearAnim();
                }
            }
        });
        faqFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MessagesActivity.this);
                View dialogView = View.inflate(MessagesActivity.this, R.layout.dialog_faq, null);
                Spinner spinner = dialogView.findViewById(R.id.faq_spinner);
                String[] arr = getResources().getStringArray(R.array.faq_questions);
                ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(MessagesActivity.this, android.R.layout.simple_spinner_item, arr);
                stringArrayAdapter.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
                spinner.setAdapter(stringArrayAdapter);

                builder.setPositiveButton(getResources().getString(R.string.ask), new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { } });
                builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { } });
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleFaq(spinner.getSelectedItemPosition());
                        dialog.dismiss();
                    }
                });
                disappearAnim();
            }
        });
        tipsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTips();
                disappearAnim();
            }
        });
        encouragementFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEncouragement();
                disappearAnim();
            }
        });
        remindersFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleReminders();
                disappearAnim();
            }
        });
    }

    private void handleFaq(int index) {
        vm.addConversation(1, (getResources().getStringArray(R.array.faq_questions))[index], (getResources().getStringArray(R.array.faq_responses))[index]);
    }

    private void handleTips() {
        String[] arr = getResources().getStringArray(R.array.management_tips);
        int randomIndex = (int) Math.floor(Math.random() * arr.length);
        vm.addConversation(2, getResources().getString(R.string.tip_request), arr[randomIndex]);
    }

    private void handleEncouragement() {
        String[] arr = getResources().getStringArray(R.array.encouragement);
        int randomIndex = (int) Math.floor(Math.random() * arr.length);
        vm.addConversation(3, getResources().getString(R.string.encouragement_request), arr[randomIndex]);
    }

    private void handleReminders() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this, R.layout.dialog_reminder, null);
        EditText et = dialogView.findViewById(R.id.reminder_edit_text);
        et.setText("15");
        TextView errorTV = dialogView.findViewById(R.id.reminder_error);
        errorTV.setVisibility(View.GONE);
        builder.setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int mins;
                try {
                    mins = Integer.parseInt(et.getText().toString());
                    if (mins <= 0 || mins > 1440) { // 1 day
                       throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    errorTV.setVisibility(View.VISIBLE);
                    errorTV.setText(getResources().getString(R.string.reminder_error));
                    return;
                }
                Date cur = new Date();
                Instant instant = cur.toInstant().plusSeconds(60 * mins);
                int reqCode = (int) (instant.getEpochSecond() % (1L << 30));

                Intent i = new Intent(MessagesActivity.this, AlarmReceiver.class);
                i.putExtra("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), reqCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (60 * 1000 * mins), pi);

                Toast.makeText(MessagesActivity.this, "Reminder set in " + mins + " minute(s)!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void appearAnim() {
        isFABOpen.setValue(true);
        ObjectAnimator.ofFloat(mainFAB, "rotation", 0f, 180f).setDuration(400).start();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mainFAB.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_close_white_24dp, getTheme()));
            }
        }, 200);

        faqTV.setVisibility(View.VISIBLE);
        tipsTV.setVisibility(View.VISIBLE);
        encouragementTV.setVisibility(View.VISIBLE);
        remindersTV.setVisibility(View.VISIBLE);

        remindersLL.animate().translationY(-getResources().getDimension(R.dimen.fab_up_1));
        encouragementLL.animate().translationY(-getResources().getDimension(R.dimen.fab_up_2));
        tipsLL.animate().translationY(-getResources().getDimension(R.dimen.fab_up_3));
        faqLL.animate().translationY(-getResources().getDimension(R.dimen.fab_up_4));
    }

    private void disappearAnim() {
        isFABOpen.setValue(false);
        ObjectAnimator.ofFloat(mainFAB, "rotation", 180f, 0f).setDuration(400).start();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mainFAB.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_add_comment_white_24dp, getTheme()));
            }
        }, 200);

        faqTV.setVisibility(View.GONE);
        tipsTV.setVisibility(View.GONE);
        encouragementTV.setVisibility(View.GONE);
        remindersTV.setVisibility(View.GONE);

        remindersLL.animate().translationY(0);
        encouragementLL.animate().translationY(0);
        tipsLL.animate().translationY(0);
        faqLL.animate().translationY(0);
    }

    @Override
    public void onBackPressed() {
        if (isFABOpen.getValue()) {
            disappearAnim();
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            if (isFABOpen.getValue()) onBackPressed();
            onBackPressed();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    class RecyclerAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 0) {
                return new OutgoingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_message_outgoing, parent, false));
            } else {
                return new IncomingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_message_incoming, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder.getItemViewType() == 0) {
                OutgoingViewHolder holder = (OutgoingViewHolder) viewHolder;
                holder.textView.setText(messages.get(position).second);
            } else {
                IncomingViewHolder holder = (IncomingViewHolder) viewHolder;
                holder.textView.setText(messages.get(position).second);
                int[] color = {0, R.color.teal_extra_light, R.color.teal_extra_light, R.color.teal_extra_light, R.color.orange, R.color.red, R.color.yellow};
                // 1: faq answer (teal), 2: tip (teal), 3: encouragement (teal), 4: reminders (blue), 5: hypo-related warning (red), 6: hyper-related warning (yellow)
                int actualColor = getResources().getColor(color[messages.get(position).first], getTheme());
                holder.card.setCardBackgroundColor(actualColor);
                holder.triangle.setImageTintList(ColorStateList.valueOf(actualColor));
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).first;
        }

        class OutgoingViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public OutgoingViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.message_text_view);
            }
        }

        class IncomingViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            CardView card;
            ImageView triangle;

            public IncomingViewHolder(@NonNull View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.card);
                triangle = itemView.findViewById(R.id.triangle);
                textView = itemView.findViewById(R.id.message_text_view);
            }
        }
    }
}