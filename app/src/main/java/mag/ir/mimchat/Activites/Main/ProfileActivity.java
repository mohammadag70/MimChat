package mag.ir.mimchat.Activites.Main;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import carbon.view.View;
import de.hdodenhof.circleimageview.CircleImageView;
import mag.ir.mimchat.R;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.back)
    carbon.widget.LinearLayout back;
    @BindView(R.id.status)
    TextView status;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.image)
    CircleImageView image;
    @BindView(R.id.sendMessageButton)
    carbon.widget.Button sendMessageButton;
    @BindView(R.id.declineMessageButton)
    carbon.widget.Button declineMessageButton;
    @BindView(R.id.wallpaper)
    ImageView wallpaper;

    private String receiverUserId = "";
    private String userName = "";
    private String userImage = "";
    private String userStatus = "";
    private String userwall = "";
    private String current_state = "";
    private String senderUserId = "";

    private DatabaseReference userRef, chatRequestRef, contactsRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        current_state = "new";

        init();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void init() {
        back.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        senderUserId = auth.getCurrentUser().getUid();

        try {
            receiverUserId = getIntent().getExtras().get("uid").toString();
            userName = getIntent().getExtras().get("name").toString();
            userStatus = getIntent().getExtras().get("status").toString();
            userImage = getIntent().getExtras().get("image").toString();
            userwall = getIntent().getExtras().get("wall").toString();

            if (!userImage.isEmpty()) {
                Picasso.get().load(userImage).into(image);
            }

            if (!userwall.isEmpty()) {
                Picasso.get().load(userwall).into(wallpaper);
            }

            name.setText(userName);
            if (userStatus.isEmpty()) {
                status.setText("بدون وضعیت");
                status.setTextColor(getResources().getColor(R.color.red));
            } else {
                status.setTextColor(getResources().getColor(R.color.colorPrimary));
                status.setText(userStatus);
            }

            manageChatRequests();

        } catch (Exception e) {
        }
    }

    private void manageChatRequests() {

        chatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserId)) {
                    String req_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                    if (req_type.equals("sent")) {
                        current_state = "request_sent";
                        sendMessageButton.setText("لغو درخواست");
                    } else if (req_type.equals("received")) {
                        current_state = "request_received";
                        sendMessageButton.setText("قبول درخواست چت");
                        declineMessageButton.setVisibility(View.VISIBLE);
                        declineMessageButton.setEnabled(true);

                        declineMessageButton.setOnClickListener(new android.view.View.OnClickListener() {
                            @Override
                            public void onClick(android.view.View view) {
                                cancelChatRequest();
                            }
                        });
                    }
                } else {
                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverUserId)) {
                                current_state = "friends";
                                sendMessageButton.setText("حذف دوستی");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (!senderUserId.equals(receiverUserId)) {
            sendMessageButton.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View view) {
                    sendMessageButton.setEnabled(false);

                    if (current_state.equals("new")) {
                        sendChatRequest();
                    }

                    if (current_state.equals("request_sent")) {
                        cancelChatRequest();
                    }

                    if (current_state.equals("request_received")) {
                        acceptChatRequest();
                    }

                    if (current_state.equals("friends")) {
                        removeConatct();
                    }
                }
            });
        } else {
            sendMessageButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeConatct() {
        contactsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        sendMessageButton.setEnabled(true);
                                        current_state = "new";
                                        sendMessageButton.setText("درخواست چت");

                                        declineMessageButton.setVisibility(android.view.View.INVISIBLE);
                                        declineMessageButton.setEnabled(false);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void acceptChatRequest() {
        contactsRef.child(senderUserId).child(receiverUserId)
                .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contactsRef.child(receiverUserId).child(senderUserId)
                            .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                chatRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            chatRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        sendMessageButton.setEnabled(true);
                                                        current_state = "friends";
                                                        sendMessageButton.setText("حذف دوستی");

                                                        declineMessageButton.setVisibility(android.view.View.INVISIBLE);
                                                        declineMessageButton.setEnabled(false);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void cancelChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        sendMessageButton.setEnabled(true);
                                        current_state = "new";
                                        sendMessageButton.setText("درخواست چت");

                                        declineMessageButton.setVisibility(android.view.View.INVISIBLE);
                                        declineMessageButton.setEnabled(false);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void sendChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendMessageButton.setEnabled(true);
                                                current_state = "request_sent";
                                                sendMessageButton.setText("لغو درخواست");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    public void onClick(android.view.View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                finish();
                break;
        }
    }
}
