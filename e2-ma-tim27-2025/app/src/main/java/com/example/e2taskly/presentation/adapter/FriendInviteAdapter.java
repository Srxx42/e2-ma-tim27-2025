package com.example.e2taskly.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e2taskly.R;
import com.example.e2taskly.model.User;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class FriendInviteAdapter extends RecyclerView.Adapter<FriendInviteAdapter.FriendInviteViewHolder> {

    private final List<User> friendList;
    private final Set<User> selectedFriends = new HashSet<>();

    public FriendInviteAdapter(List<User> friendList) {
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public FriendInviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_invite, parent, false);
        return new FriendInviteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendInviteViewHolder holder, int position) {
        User friend = friendList.get(position);
        holder.bind(friend);
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public List<User> getSelectedFriends() {
        return new ArrayList<>(selectedFriends);
    }

    class FriendInviteViewHolder extends RecyclerView.ViewHolder {
        MaterialCheckBox checkBoxFriend;

        public FriendInviteViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxFriend = itemView.findViewById(R.id.checkboxFriend);
        }

        public void bind(final User friend) {
            checkBoxFriend.setText(friend.getUsername());
            checkBoxFriend.setOnCheckedChangeListener(null);
            checkBoxFriend.setChecked(selectedFriends.contains(friend));

            checkBoxFriend.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedFriends.add(friend);
                } else {
                    selectedFriends.remove(friend);
                }
            });
        }
    }
}
