package com.example.e2taskly.presentation.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e2taskly.R;
import com.example.e2taskly.model.User;
import com.example.e2taskly.presentation.activity.ProfileActivity;
import com.example.e2taskly.service.UserService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private List<User> displayedUserList;
    private List<String> friendIdList;
    private final OnFriendActionListener listener;
    private final boolean showActionIcon;

    public interface OnFriendActionListener {
        void onAddFriend(User userToAdd);
        void onRemoveFriend(User userToRemove);
    }
    public UserAdapter(Context context, List<User> userList, List<String> friendIdList, OnFriendActionListener listener) {
        this(context, userList, friendIdList, listener, true);
    }

    public UserAdapter(Context context, List<User> userList, List<String> friendIdList, OnFriendActionListener listener, boolean showActionIcon) {
        this.context = context;
        this.displayedUserList = userList;
        this.friendIdList = friendIdList;
        this.listener = listener;
        this.showActionIcon = showActionIcon;
    }

    public void updateUsers(List<User> newUsers) {
        this.displayedUserList = newUsers;
        notifyDataSetChanged();
    }

    public void updateFriendIds(List<String> newFriendIds) {
        this.friendIdList = newFriendIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(displayedUserList.get(position));
    }

    @Override
    public int getItemCount() {
        return displayedUserList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar, imageViewAction;
        TextView textViewUsername, textViewTitle, textViewLevel;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            imageViewAction = itemView.findViewById(R.id.imageViewAddFriend);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewLevel = itemView.findViewById(R.id.textViewLevel);
        }

        void bind(final User user) {
            textViewUsername.setText(user.getUsername());
            textViewTitle.setText(user.getTitle());
            textViewLevel.setText("Level: " + user.getLevel());

            if (user.getAvatar() != null) {
                int resId = context.getResources().getIdentifier(user.getAvatar(), "drawable", context.getPackageName());
                if (resId != 0) imageViewAvatar.setImageResource(resId);
            }

            if (showActionIcon) {
                imageViewAction.setVisibility(View.VISIBLE);
                if (friendIdList.contains(user.getUid())) {
                    imageViewAction.setImageResource(R.drawable.ic_remove_friend);
                    imageViewAction.setOnClickListener(v -> {
                        if (listener != null) listener.onRemoveFriend(user);
                    });
                } else {
                    imageViewAction.setImageResource(R.drawable.ic_add_friend);
                    imageViewAction.setOnClickListener(v -> {
                        if (listener != null) listener.onAddFriend(user);
                    });
                }
            } else {
                imageViewAction.setVisibility(View.GONE);
            }
            // ===================================

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_USER_ID, user.getUid());
                context.startActivity(intent);
            });
        }
    }
}