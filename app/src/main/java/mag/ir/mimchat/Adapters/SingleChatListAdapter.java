package mag.ir.mimchat.Adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import mag.ir.mimchat.Models.Message;
import mag.ir.mimchat.R;
import mag.ir.mimchat.Utilities.Utils;

public class SingleChatListAdapter extends RecyclerView.Adapter<SingleChatListAdapter.MyViewHolder> {

    private List<Message> userMessageList;
    private Context context;

    private FirebaseAuth auth;
    private String currenUserId;
    private DatabaseReference userRefl;

    public SingleChatListAdapter(List<Message> userMessageList, Context context) {
        this.userMessageList = userMessageList;
        this.context = context;

        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public SingleChatListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item, parent, false);
        return new SingleChatListAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SingleChatListAdapter.MyViewHolder holder, int position) {
        currenUserId = auth.getCurrentUser().getUid();

        Message message = userMessageList.get(position);
        holder.date.setText(Utils.toPersianNumber(message.getDate()));
        holder.time.setText(Utils.toPersianNumber(message.getTime()));

        userRefl = FirebaseDatabase.getInstance().getReference().child("Users").child(message.getFrom());

        userRefl.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.profile).into(holder.image);
                }

                holder.name.setText(dataSnapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (message.getType().equals("text")) {
            holder.message.setVisibility(View.VISIBLE);
            holder.message.setText(message.getMessage());

            if (message.getFrom().equals(currenUserId)) {
                holder.root.setGravity(Gravity.RIGHT);
                holder.rel.setBackgroundColor(context.getResources().getColor(R.color.input));
            } else {
                holder.root.setGravity(Gravity.LEFT);
                holder.rel.setBackgroundColor(context.getResources().getColor(R.color.input2));
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name, time, date, message;
        carbon.widget.LinearLayout rel;
        CircleImageView image;
        RelativeLayout root;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);
            time = itemView.findViewById(R.id.time);
            date = itemView.findViewById(R.id.date);
            message = itemView.findViewById(R.id.message);
            rel = itemView.findViewById(R.id.rel);
            root = itemView.findViewById(R.id.root);
        }
    }

}
