package mag.ir.mimchat.Activites.Main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import mag.ir.mimchat.Models.Contact;
import mag.ir.mimchat.R;

import static maes.tech.intentanim.CustomIntent.customType;

public class FindFriendsActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.back)
    carbon.widget.LinearLayout back;
    @BindView(R.id.findFriendsRecyclerView)
    RecyclerView findFriendsRecyclerView;
    @BindView(R.id.loadingBar)
    LottieAnimationView loadingBar;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        back.setOnClickListener(this);

        findFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(userRef, Contact.class).build();

        FirebaseRecyclerAdapter<Contact, FindFriendViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position, @NonNull Contact model) {
                holder.name.setText(model.getName());
                if (!model.getStatus().isEmpty()) {
                    holder.status.setText(model.getStatus());
                }
                if (!model.getImage().isEmpty()) {
                    Picasso.get().load(model.getImage()).placeholder(R.drawable.profile).into(holder.image);
                }
                loadingBar.setVisibility(View.GONE);

                holder.rel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                        intent.putExtra("uid", model.getUid());
                        intent.putExtra("name", model.getName());
                        intent.putExtra("status", model.getStatus());
                        intent.putExtra("image", model.getImage());
                        intent.putExtra("wall", model.getBackground());
                        customType(FindFriendsActivity.this, "left-to-right");
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_item, parent, false);
                FindFriendViewHolder holder = new FindFriendViewHolder(view);
                return holder;
            }
        };

        findFriendsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {

        TextView name, status;
        CircleImageView image;
        carbon.widget.LinearLayout rel;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.userName);
            status = itemView.findViewById(R.id.userStatus);
            image = itemView.findViewById(R.id.userImage);
            rel = itemView.findViewById(R.id.rel);
        }
    }
}
