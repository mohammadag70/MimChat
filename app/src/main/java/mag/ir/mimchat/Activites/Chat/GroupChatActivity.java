package mag.ir.mimchat.Activites.Chat;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import mag.ir.mimchat.Adapters.GroupChatListAdapter;
import mag.ir.mimchat.Models.Chat;
import mag.ir.mimchat.R;
import mag.ir.mimchat.Utilities.SolarCalendar;
import mag.ir.mimchat.Utilities.Utils;

public class GroupChatActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.send)
    RelativeLayout send;
    @BindView(R.id.inputText)
    EditText inputText;
    @BindView(R.id.back)
    carbon.widget.LinearLayout back;
    @BindView(R.id.gpName)
    TextView gpNameTextView;
    @BindView(R.id.chatRecyclerView)
    RecyclerView chatRecyclerView;
    @BindView(R.id.loadingBar)
    LottieAnimationView loadingBar;

    private String gpName = "";
    private String currenUserId, currentUserName, dateOfMessage, timeOfMessage;

    private FirebaseAuth auth;
    private DatabaseReference userRef, groupNameRef, groupMessageKeyRef;
    private GroupChatListAdapter groupListAdapter;
    private List<Chat> chatList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        ButterKnife.bind(this);

        init();
        getUserInfo();
    }

    private void getUserInfo() {
        userRef.child(currenUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void init() {
        Utils.hideKeyboard(this);
        back.setOnClickListener(this);
        send.setOnClickListener(this);

        try {
            gpName = getIntent().getExtras().get("gpName").toString();
            gpNameTextView.setText(gpName);
        } catch (Exception e) {

        }

        auth = FirebaseAuth.getInstance();
        currenUserId = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(gpName);

        groupListAdapter = new GroupChatListAdapter(chatList, this);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(manager);
        chatRecyclerView.setAdapter(groupListAdapter);
        chatRecyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                finish();
                break;

            case R.id.send:
                sendMessageToServer();
                inputText.setText("");
                chatRecyclerView.scrollToPosition(groupListAdapter.getItemCount() - 1);
                break;
        }
    }

    private void sendMessageToServer() {
        String message = inputText.getText().toString();
        String messageKey = groupNameRef.push().getKey();

        if (message.isEmpty()) {
            Utils.showErrorMessage(this, "اول پامتو بنویس");
            return;
        }

        SolarCalendar solarCalendar = new SolarCalendar();
        dateOfMessage = solarCalendar.date + " " + Utils.getMonth(solarCalendar.month) + " " + solarCalendar.year;

        Date date = new Date();
        timeOfMessage = DateFormat.getTimeInstance().format(date);

        HashMap<String, Object> gpMessageKey = new HashMap<>();
        groupNameRef.updateChildren(gpMessageKey);

        groupMessageKeyRef = groupNameRef.child(messageKey);
        HashMap<String, Object> messageInfoMap = new HashMap<>();
        messageInfoMap.put("name", currentUserName);
        messageInfoMap.put("message", message);
        messageInfoMap.put("date", dateOfMessage);
        messageInfoMap.put("time", timeOfMessage);
        messageInfoMap.put("uid", currenUserId);
        groupMessageKeyRef.updateChildren(messageInfoMap);
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChildren()) {
                        displayMessages(dataSnapshot);
                    } else {
                        loadingBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatList.clear();
    }

    private void displayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()) {
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();
            String uid = (String) ((DataSnapshot) iterator.next()).getValue();

            chatList.add(new Chat(chatMessage, chatName, chatDate, chatTime, uid));
            groupListAdapter.notifyDataSetChanged();

            chatRecyclerView.scrollToPosition(groupListAdapter.getItemCount() - 1);

            loadingBar.setVisibility(View.INVISIBLE);
        }
    }
}
