package mag.ir.mimchat.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import mag.ir.mimchat.Adapters.GroupListAdapter;
import mag.ir.mimchat.Models.Group;
import mag.ir.mimchat.R;

public class GroupChatFragment extends Fragment {

    @BindView(R.id.groupRecyclerView)
    RecyclerView groupRecyclerView;
    @BindView(R.id.coolLay)
    RelativeLayout coolLay;
    @BindView(R.id.loadingBar)
    LottieAnimationView loadingBar;

    private static GroupListAdapter groupListAdapter;
    private List<Group> gpList = new ArrayList<>();

    private DatabaseReference groupRef;

    public GroupChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);
        ButterKnife.bind(this, view);

        init();

        return view;
    }

    private void init() {
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        groupListAdapter = new GroupListAdapter(gpList, getActivity());
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        groupRecyclerView.setLayoutManager(manager);
        groupRecyclerView.setAdapter(groupListAdapter);

        getGroupList();
    }

    private void getGroupList() {
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    gpList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String gpName = (String) snapshot.getKey();
                        gpList.add(new Group(gpName, snapshot.child("date").getValue().toString()
                                , snapshot.child("time").getValue().toString(), snapshot.child("uid").getValue().toString(),
                                snapshot.child("username").getValue().toString()));
                    }
                    groupListAdapter.notifyDataSetChanged();
                    coolLay.setVisibility(View.INVISIBLE);
                    loadingBar.setVisibility(View.INVISIBLE);
                } else {
                    loadingBar.setVisibility(View.INVISIBLE);
                    coolLay.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loadingBar.setVisibility(View.INVISIBLE);
                coolLay.setVisibility(View.VISIBLE);
            }
        });
    }

    public static GroupChatFragment newInstance() {
        return new GroupChatFragment();
    }

    public void notifyAdapter() {
        getGroupList();
    }
}
