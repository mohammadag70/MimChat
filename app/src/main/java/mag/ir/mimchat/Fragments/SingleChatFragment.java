package mag.ir.mimchat.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import mag.ir.mimchat.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SingleChatFragment extends Fragment {

    public SingleChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_chat, container, false);
    }

    public static SingleChatFragment newInstance() {
        return new SingleChatFragment();
    }

}
