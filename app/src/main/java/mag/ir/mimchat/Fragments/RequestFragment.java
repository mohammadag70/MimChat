package mag.ir.mimchat.Fragments;

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
import de.hdodenhof.circleimageview.CircleImageView;
import mag.ir.mimchat.Models.Contact;
import mag.ir.mimchat.R;
import mag.ir.mimchat.Utilities.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    @BindView(R.id.requestRecyclerView)
    RecyclerView requestRecyclerView;
    @BindView(R.id.loadingBar)
    LottieAnimationView loadingBar;
    @BindView(R.id.coolLay)
    RelativeLayout coolLay;

    private DatabaseReference chatRequestRef, userRef, contactRef;
    private FirebaseAuth auth;
    private String currentUserId;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_request, container, false);
        ButterKnife.bind(this, view);
        requestRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(chatRequestRef.child(currentUserId), Contact.class)
                .build();

        FirebaseRecyclerAdapter<Contact, RequestHolder> adapter = new FirebaseRecyclerAdapter<Contact, RequestHolder>(options) {

            @Override
            public void onDataChanged() {
                if (options.getSnapshots().size() == 0) {
                    loadingBar.setVisibility(View.GONE);
                    coolLay.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull RequestHolder holder, int position, @NonNull Contact model) {

                holder.acceptButton.setVisibility(View.VISIBLE);
                holder.declineButton.setVisibility(View.VISIBLE);

                final String listUserIds = getRef(position).getKey();
                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            loadingBar.setVisibility(View.VISIBLE);
                            String type = dataSnapshot.getValue().toString();
                            if (type.equals("received")) {
                                userRef.child(listUserIds).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("image")) {
                                            holder.name.setText(dataSnapshot.child("name").getValue().toString());
                                            holder.status.setText("میخواد باهات دوست بشه! ");
                                            Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(holder.image);
                                        } else {
                                            holder.name.setText(dataSnapshot.child("name").getValue().toString());
                                            holder.status.setText("میخواد باهات دوست بشه! ");
                                        }

                                        loadingBar.setVisibility(View.INVISIBLE);
                                        coolLay.setVisibility(View.INVISIBLE);

                                        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                contactRef.child(currentUserId).child(listUserIds).child("Contact")
                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            contactRef.child(listUserIds).child(currentUserId).child("Contact")
                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        chatRequestRef.child(currentUserId).child(listUserIds).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    chatRequestRef.child(listUserIds).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                Utils.showSuccessMessage(getActivity(), "مخاطب جدید ذخیره شد");
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
                                        });

                                        holder.declineButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                chatRequestRef.child(currentUserId).child(listUserIds).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            chatRequestRef.child(listUserIds).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Utils.showSuccessMessage(getActivity(), "درخواست پذیرفته نشد");
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        loadingBar.setVisibility(View.INVISIBLE);
                                        coolLay.setVisibility(View.VISIBLE);
                                    }
                                });
                            } else {
                                holder.itemView.setVisibility(View.GONE);
                            }

                            loadingBar.setVisibility(View.INVISIBLE);
                        } else {
                            coolLay.setVisibility(View.VISIBLE);
                            loadingBar.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        loadingBar.setVisibility(View.INVISIBLE);
                        coolLay.setVisibility(View.VISIBLE);
                    }
                });
            }

            @NonNull
            @Override
            public RequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item, parent, false);
                RequestHolder holder = new RequestHolder(view);
                return holder;
            }
        };

        requestRecyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    public static class RequestHolder extends RecyclerView.ViewHolder {

        TextView name, status;
        CircleImageView image;
        carbon.widget.LinearLayout rel;
        carbon.widget.Button acceptButton, declineButton;

        public RequestHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.userName);
            status = itemView.findViewById(R.id.userStatus);
            image = itemView.findViewById(R.id.userImage);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            declineButton = itemView.findViewById(R.id.declineButton);
            rel = itemView.findViewById(R.id.rel);
        }
    }

}
