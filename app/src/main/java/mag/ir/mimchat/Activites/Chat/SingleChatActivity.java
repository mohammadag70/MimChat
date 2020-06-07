package mag.ir.mimchat.Activites.Chat;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
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
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import mag.ir.mimchat.Adapters.SingleChatListAdapter;
import mag.ir.mimchat.Models.Message;
import mag.ir.mimchat.R;
import mag.ir.mimchat.Utilities.SolarCalendar;
import mag.ir.mimchat.Utilities.Utils;

public class SingleChatActivity extends AppCompatActivity implements View.OnClickListener {

    private final List<Message> messageList = new ArrayList<>();
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
    @BindView(R.id.attachFile)
    RelativeLayout attachFile;
    boolean isListenerAdded = false;
    private String messageSenderId = "";
    private String messageReceiverId = "";
    private String messageReceiverName = "";
    private String messageReceiverImage = "";
    private String timeOfMessage = "";
    private String dateOfMessage = "";
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private SingleChatListAdapter singleChatListAdapter;
    private LinearLayoutManager linearLayoutManager;
    private String checker = "";
    private String myUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);
        ButterKnife.bind(this);

        init();

        displayLastSeen();

        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    loadingBar.setVisibility(android.view.View.GONE);
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
        attachFile.setOnClickListener(this);

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
                break;

            case R.id.attachFile:
                showDialogForChooseFileType();
                break;
        }
    }

    private void showDialogForChooseFileType() {
        CharSequence options[] = new CharSequence[]{
                "تصویر", "فایل PDF", "فایل WORD"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(SingleChatActivity.this);
        builder.setTitle("یکی از گزینه های زیر رو انتخاب کن");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    checker = "image";

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "عکس رو انتخاب کن"), 438);
                }
                if (i == 1) {
                    checker = "pdf";
                }
                if (i == 2) {
                    checker = "docx";
                }
            }
        });

        builder.show();

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
        messageTextBody.put("to", messageReceiverId);
        messageTextBody.put("messageId", messagePushId);
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

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);
                singleChatListAdapter = new SingleChatListAdapter(messageList, SingleChatActivity.this, loadingBar);
                linearLayoutManager = new LinearLayoutManager(SingleChatActivity.this);
                chatRecyclerView.setLayoutManager(linearLayoutManager);
                chatRecyclerView.setAdapter(singleChatListAdapter);
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
        };

        if (!isListenerAdded) {
            rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).addChildEventListener(childEventListener);
            isListenerAdded = true;
        }

    }

    private void displayLastSeen() {
        rootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("userState").hasChild("state")) {
                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    String time = dataSnapshot.child("userState").child("time").getValue().toString();
                    String state = dataSnapshot.child("userState").child("state").getValue().toString();

                    if (state.equals("آنلاین")) {
                        profileLastSeen.setText("آنلاین");
                    } else {
                        profileLastSeen.setText("آخرین بازدید در تاریخ " + date + " ساعت " + time);
                    }
                } else {
                    profileLastSeen.setText("آفلاین");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Dialog dialog = Utils.loading(this);
            dialog.show();

            fileUri = data.getData();

            if (!checker.equals("image")) {

            } else if (checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                SolarCalendar solarCalendar = new SolarCalendar();
                dateOfMessage = solarCalendar.date + " " + Utils.getMonth(solarCalendar.month) + " " + solarCalendar.year;

                Date date = new Date();
                timeOfMessage = DateFormat.getTimeInstance().format(date);

                final String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
                final String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();
                final String messagePushId = userMessageKeyRef.getKey();

                StorageReference filePath = storageReference.child(messagePushId + ".jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()) {
                            Utils.showErrorMessage(SingleChatActivity.this, task.getException().toString());
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("name", fileUri.getPathSegments());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderId);
                            messageTextBody.put("to", messageReceiverId);
                            messageTextBody.put("messageId", messagePushId);
                            messageTextBody.put("date", dateOfMessage);
                            messageTextBody.put("time", timeOfMessage);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        dialog.dismiss();
                                    } else {
                                        loadingBar.setVisibility(android.view.View.GONE);
                                        Utils.showErrorMessage(SingleChatActivity.this, "مشکل از سرور");
                                        dialog.dismiss();
                                    }
                                }
                            });
                        }
                    }
                });


            } else {
                dialog.dismiss();
                Utils.showErrorMessage(this, "هیچیو انتخاب نکردی!");
            }
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
}
