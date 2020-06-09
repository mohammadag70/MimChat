package mag.ir.mimchat.Fragments;

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
import mag.ir.mimchat.Models.Contact;
import mag.ir.mimchat.R;

public class ContactsFragment extends Fragment {

    @BindView(R.id.contactsRecyclerView)
    RecyclerView contactsRecyclerView;
    @BindView(R.id.loadingBar)
    LottieAnimationView loadingBar;
    @BindView(R.id.coolLay)
    RelativeLayout coolLay;

    private DatabaseReference contactRef, userRef;
    private FirebaseAuth auth;

    private String currentUserId;

    public ContactsFragment() {
    }

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, view);

        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(contactRef, Contact.class).build();

        FirebaseRecyclerAdapter<Contact, ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, ContactsViewHolder>(options) {

            @Override
            public void onDataChanged() {
                if (options.getSnapshots().size() == 0) {
                    loadingBar.setVisibility(View.GONE);
                    coolLay.setVisibility(View.VISIBLE);
                }

            }

            @Override
            protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position, @NonNull Contact model) {
                String userIds = getRef(position).getKey();

                userRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            if (dataSnapshot.child("userState").hasChild("state")) {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();

                                if (state.equals("آنلاین")) {
                                    holder.onlineOrOffline.setBackgroundColor(Color.parseColor("#2EDD3D"));
                                } else {
                                    holder.onlineOrOffline.setBackgroundColor(Color.parseColor("#A07C7C7C"));
                                }
                            } else {
                                holder.onlineOrOffline.setBackgroundColor(Color.parseColor("#A07C7C7C"));
                            }

                            if (dataSnapshot.hasChild("image")) {
                                Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(holder.image);
                            }
                            holder.name.setText(dataSnapshot.child("name").getValue().toString());
                            if (dataSnapshot.hasChild("status"))
                                holder.status.setText(dataSnapshot.child("status").getValue().toString());
                            else
                                holder.status.setText("این مخاطب وضعیتی ندارد");

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
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        contactsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView name, status;
        CircleImageView image;
        carbon.widget.LinearLayout rel;
        View onlineOrOffline;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.userName);
            status = itemView.findViewById(R.id.userStatus);
            image = itemView.findViewById(R.id.userImage);
            rel = itemView.findViewById(R.id.rel);
            onlineOrOffline = itemView.findViewById(R.id.onlineOrOffline);
        }
    }
}
