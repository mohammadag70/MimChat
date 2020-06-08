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

import java.util.List;

import mag.ir.mimchat.Models.Chat;
import mag.ir.mimchat.R;
import mag.ir.mimchat.Utilities.Utils;


public class GroupChatListAdapter extends RecyclerView.Adapter<GroupChatListAdapter.MyViewHolder> {

    List<Chat> chatList;
    Context context;
    private FirebaseAuth auth;
    private String currenUserId;

    public GroupChatListAdapter(List<Chat> chatList, Context context) {
        this.chatList = chatList;
        this.context = context;

        auth = FirebaseAuth.getInstance();
        currenUserId = auth.getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public GroupChatListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gp_chat_item, parent, false);
        return new GroupChatListAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatListAdapter.MyViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        holder.date.setText(Utils.toPersianNumber(chat.getDate()));
        holder.time.setText(Utils.toPersianNumber(chat.getTime()));
        holder.name.setText(chat.getName());
        holder.message.setText(chat.getMessage());

        if (currenUserId.equals(chat.getUid())) {
            holder.root.setGravity(Gravity.RIGHT);
            holder.rel.setBackgroundColor(context.getResources().getColor(R.color.input));
        } else {
            holder.root.setGravity(Gravity.LEFT);
            holder.rel.setBackgroundColor(context.getResources().getColor(R.color.input2));
        }

        if (Utils.isPersian(chat.getMessage())) {
            holder.message.setGravity(Gravity.RIGHT);
        } else {
            holder.message.setGravity(Gravity.LEFT);
        }

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name, time, date, message;
        carbon.widget.LinearLayout rel;
        RelativeLayout root;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            time = itemView.findViewById(R.id.time);
            date = itemView.findViewById(R.id.date);
            message = itemView.findViewById(R.id.message);
            rel = itemView.findViewById(R.id.rel);
            root = itemView.findViewById(R.id.root);
        }
    }
}
