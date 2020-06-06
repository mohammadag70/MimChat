package mag.ir.mimchat.Activites.Chat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import carbon.view.View;
import de.hdodenhof.circleimageview.CircleImageView;
import mag.ir.mimchat.Adapters.SingleChatListAdapter;
import mag.ir.mimchat.Models.Message;
import mag.ir.mimchat.R;
import mag.ir.mimchat.Utilities.SolarCalendar;
import mag.ir.mimchat.Utilities.Utils;

public class SingleChatActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.profileImage)
    CircleImageView profileImage;
    @BindView(R.id.profileName)
    TextView profileName;
    @BindView(R.id.profileLastSeen)
    TextView profileLastSeen;
    @BindView(R.id.send)
    RelativeLayout send;
    @BindView(R.id.inputText)
    EditText inputText;
    @BindView(R.id.back)
    carbon.widget.LinearLayout back;
    @BindView(R.id.chatRecyclerView)
    RecyclerView chatRecyclerView;
    @BindView(R.id.loadingBar)
    LottieAnimationView loadingBar;

    private String messageSenderId = "";
    private String messageReceiverId = "";
    private String messageReceiverName = "";
    private String messageReceiverImage = "";
    private String timeOfMessage = "";
    private String dateOfMessage = "";

    private FirebaseAuth auth;
    private DatabaseReference rootRef;

    private final List<Message> messageList = new ArrayList<>();
    private SingleChatListAdapter singleChatListAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);
        ButterKnife.bind(this);

        init();

    }

    private void init() {
        Utils.hideKeyboard(this);
        back.setOnClickListener(this);
        send.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        messageSenderId = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        try {
            messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
            messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
            messageReceiverImage = getIntent().getExtras().get("visit_user_image").toString();

            profileName.setText(messageReceiverName);
            if (messageReceiverImage.equals("default"))
                Picasso.get().load(R.drawable.profile).into(profileImage);
            else
                Picasso.get().load(messageReceiverImage).into(profileImage);
        } catch (Exception ignored) {
        }

        singleChatListAdapter = new SingleChatListAdapter(messageList, this);
        linearLayoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatRecyclerView.setAdapter(singleChatListAdapter);
    }

    @Override
    public void onClick(android.view.View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                finish();
                break;

            case R.id.send:
                sendMessageToServer();
                inputText.setText("");
                // chatRecyclerView.scrollToPosition(groupListAdapter.getItemCount() - 1);
                break;
        }
    }

    private void sendMessageToServer() {
        String message = inputText.getText().toString();

        if (message.isEmpty()) {
            Utils.showErrorMessage(this, "اول پیامتو بنویس");
            return;
        }

        SolarCalendar solarCalendar = new SolarCalendar();
        dateOfMessage = solarCalendar.date + " " + Utils.getMonth(solarCalendar.month) + " " + solarCalendar.year;

        Date date = new Date();
        timeOfMessage = DateFormat.getTimeInstance().format(date);

        String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
        String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

        DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();
        String messagePushId = userMessageKeyRef.getKey();

        Map messageTextBody = new HashMap();
        messageTextBody.put("message", message);
        messageTextBody.put("type", "text");
        messageTextBody.put("from", messageSenderId);
        messageTextBody.put("date", dateOfMessage);
        messageTextBody.put("time", timeOfMessage);

        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
        messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                } else {
                    loadingBar.setVisibility(android.view.View.GONE);
                    Utils.showErrorMessage(SingleChatActivity.this, "مشکل از سرور");
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        messageList.clear();
    }

    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);
                singleChatListAdapter.notifyDataSetChanged();
                loadingBar.setVisibility(android.view.View.GONE);

                chatRecyclerView.smoothScrollToPosition(chatRecyclerView.getAdapter().getItemCount());

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
}
