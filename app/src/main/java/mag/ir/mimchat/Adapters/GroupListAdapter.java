package mag.ir.mimchat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mag.ir.mimchat.Models.Group;
import mag.ir.mimchat.R;
import mag.ir.mimchat.Utilities.Utils;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.MyViewHolder> {

    List<Group> gpList;
    Context context;

    public GroupListAdapter(List<Group> gpList, Context context) {
        this.gpList = gpList;
        this.context = context;
    }

    @NonNull
    @Override
    public GroupListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupListAdapter.MyViewHolder holder, int position) {
        Group group = gpList.get(position);
        holder.date.setText(Utils.toPersianNumber(group.getDate()));
        holder.time.setText(Utils.toPersianNumber(group.getTime()));
        holder.gpName.setText(group.getName());
        holder.sazande.setText("ساخته شده توسط: " + group.getUsername());
    }

    @Override
    public int getItemCount() {
        return gpList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView gpName, time, date, sazande;
        RelativeLayout rel;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            gpName = itemView.findViewById(R.id.gpName);
            time = itemView.findViewById(R.id.time);
            date = itemView.findViewById(R.id.date);
            sazande = itemView.findViewById(R.id.sazande);
            rel = itemView.findViewById(R.id.rel);
        }
    }
}