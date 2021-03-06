package mag.ir.mimchat.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import mag.ir.mimchat.Activites.Chat.SingleChatActivity;
import mag.ir.mimchat.Activites.Main.MainActivity;
import mag.ir.mimchat.Models.Contact;
import mag.ir.mimchat.R;
import mag.ir.mimchat.Utilities.Utils;

import static maes.tech.intentanim.CustomIntent.customType;

/**
 * A simple {@link Fragment} subclass.
 */
public class SingleChatFragment extends Fragment {

    @BindView(R.id.singleRecyclerView)
    RecyclerView singleRecyclerView;
    @BindView(R.id.coolLay)
    RelativeLayout coolLay;
    @BindView(R.id.loadingBar)
    LottieAnimationView loadingBar;

    private DatabaseReference chatsRef, userRef;
    private FirebaseAuth auth;
    private String currentUserId;
    private String imgPath = "default";

    public SingleChatFragment() {
    }

    public static SingleChatFragment newInstance() {
        return new SingleChatFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_single_chat, container, false);
        ButterKnife.bind(this, view);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        singleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(chatsRef, Contact.class).build();

        FirebaseRecyclerAdapter<Contact, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, ChatsViewHolder>(options) {

            @Override
            public void onDataChanged() {
                if (options.getSnapshots().size() == 0) {
                    loadingBar.setVisibility(View.GONE);
                    coolLay.setVisibility(View.VISIBLE);
                }

            }

            @Override
            protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Contact model) {
                String userIds = getRef(position).getKey();

                userRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("image")) {
                                imgPath = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(imgPath).into(holder.profileImage);
                            } else {
                                imgPath = "default";
                            }
                            holder.userName.setText(dataSnapshot.child("name").getValue().toString());

                            if (dataSnapshot.child("userState").hasChild("state")) {
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();

                                if (state.equals("آنلاین")) {
                                    holder.userStatus.setText("آنلاین");
                                    holder.onlineOrOffline.setBackgroundColor(Color.parseColor("#2EDD3D"));
                                } else {
                                    holder.userStatus.setText("آخرین بازدید در تاریخ " + Utils.toPersianNumber(date) + " ساعت " + Utils.toPersianNumber(time));
                                    holder.onlineOrOffline.setBackgroundColor(Color.parseColor("#A07C7C7C"));
                                }
                            } else {
                                holder.userStatus.setText("آفلاین");
                                holder.onlineOrOffline.setBackgroundColor(getActivity().getResources().getColor(R.color.offline));
                            }

                            holder.rel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent chatIntent = new Intent(getContext(), SingleChatActivity.class);
                                    chatIntent.putExtra("visit_user_id", userIds);
                                    if (dataSnapshot.hasChild("image")) {
                                        imgPath = dataSnapshot.child("image").getValue().toString();
                                        chatIntent.putExtra("visit_user_image", imgPath);
                                    } else {
                                        imgPath = "default";
                                        chatIntent.putExtra("visit_user_image", imgPath);
                                    }
                                    chatIntent.putExtra("visit_user_name", dataSnapshot.child("name").getValue().toString());
                                    startActivity(chatIntent);
                                    MainActivity.gotoAnotherPage = true;
                                    customType(getActivity(), "left-to-right");
                                }
                            });

                            coolLay.setVisibility(View.INVISIBLE);
                            loadingBar.setVisibility(View.GONE);
                        } else {
                            loadingBar.setVisibility(View.GONE);
                            coolLay.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        loadingBar.setVisibility(View.GONE);
                        coolLay.setVisibility(View.VISIBLE);
                    }
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
                ChatsViewHolder viewHolder = new ChatsViewHolder(view);
                return viewHolder;
            }
        };

        singleRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;
        TextView userStatus, userName;
        carbon.widget.LinearLayout rel;
        View onlineOrOffline;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.userImage);
            userStatus = itemView.findViewById(R.id.userStatus);
            userName = itemView.findViewById(R.id.userName);
            rel = itemView.findViewById(R.id.rel);
            onlineOrOffline = itemView.findViewById(R.id.onlineOrOffline);
        }
    }
}
