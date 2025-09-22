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
    private final List<User> userList;
    private final Context context;
    private final List<String> currentUserFriendIds;
    private final UserService userService;
    private String currentUserId;

    public UserAdapter(Context context, List<User> userList, List<String> currentUserFriendIds, UserService userService, String currentUserId) {
        this.context = context;
        this.userList = userList;
        this.currentUserFriendIds = currentUserFriendIds;
        this.userService = userService;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }
    public void updateUserList(List<User> newList) {
        userList.clear();
        userList.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar,imageViewAddFriend;
        TextView textViewUsername, textViewTitle, textViewLevel;
        LinearLayout layoutUserInfo;



        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            imageViewAddFriend = itemView.findViewById(R.id.imageViewAddFriend);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewLevel = itemView.findViewById(R.id.textViewLevel);
            layoutUserInfo = itemView.findViewById(R.id.layoutUserInfo);
        }

        void bind(final User user) {
            textViewUsername.setText(user.getUsername());
            textViewTitle.setText(user.getTitle());
            textViewLevel.setText("Level: " + user.getLevel());

            if (user.getAvatar() != null) {
                int resId = context.getResources().getIdentifier(user.getAvatar(), "drawable", context.getPackageName());
                if (resId != 0) imageViewAvatar.setImageResource(resId);
            }


            View.OnClickListener openProfileListener = v -> {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_USER_ID, user.getUid());
                context.startActivity(intent);
            };
            itemView.setOnClickListener(openProfileListener);
            layoutUserInfo.setOnClickListener(openProfileListener);

            if (currentUserFriendIds.contains(user.getUid())) {
                // User IS a friend, show remove icon and logic
                imageViewAddFriend.setImageResource(R.drawable.ic_remove_friend); // You need to create this drawable
                imageViewAddFriend.setOnClickListener(v -> {
                    new MaterialAlertDialogBuilder(context)
                            .setTitle("Remove Friend")
                            .setMessage("Are you sure you want to remove " + user.getUsername() + " as a friend?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Remove", (dialog, which) -> {
                                performRemoveFriend(user);
                            })
                            .show();
                });
            } else {
                // User IS NOT a friend, show add icon and logic
                imageViewAddFriend.setImageResource(R.drawable.ic_add_friend);
                imageViewAddFriend.setOnClickListener(v -> {
                    performAddFriend(user);
                });
            }
        }
        private void performAddFriend(User user) {
            imageViewAddFriend.setEnabled(false);
            userService.addFriend(currentUserId,user.getUid()).addOnCompleteListener(task -> {
                imageViewAddFriend.setEnabled(true);
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Added " + user.getUsername() + " as a friend.", Toast.LENGTH_SHORT).show();
                    currentUserFriendIds.add(user.getUid());
                    notifyItemChanged(getAdapterPosition()); // Refresh this item to show the new state
                } else {
                    Toast.makeText(context, "Failed to add friend.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void performRemoveFriend(User user) {
            imageViewAddFriend.setEnabled(false);
            userService.removeFriend(currentUserId,user.getUid()).addOnCompleteListener(task -> {
                imageViewAddFriend.setEnabled(true);
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Removed " + user.getUsername() + " from friends.", Toast.LENGTH_SHORT).show();
                    currentUserFriendIds.remove(user.getUid());
                    notifyItemChanged(getAdapterPosition()); // Refresh this item
                } else {
                    Toast.makeText(context, "Failed to remove friend.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
