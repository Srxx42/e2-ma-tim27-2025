package com.example.e2taskly.presentation.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e2taskly.R;
import com.example.e2taskly.model.User;
import com.example.e2taskly.presentation.activity.FriendsListActivity;
import com.example.e2taskly.presentation.activity.ProfileActivity;
import com.example.e2taskly.service.UserService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private final Context context;
    private final List<User> displayedUserList;
    private final List<String> friendIdList;
    private final UserService userService;
    private String currentUserId;
    public FriendAdapter(Context context, List<User> displayedUserList, List<String> friendIdList, UserService userService, String currentUserId) {
        this.context = context;
        this.displayedUserList = displayedUserList;
        this.friendIdList = friendIdList;
        this.userService = userService;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        holder.bind(displayedUserList.get(position));
    }

    @Override
    public int getItemCount() {
        return displayedUserList.size();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar, imageViewAction;
        TextView textViewUsername, textViewTitle, textViewLevel;

        public FriendViewHolder(@NonNull View itemView) {
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

            if (friendIdList.contains(user.getUid())) {
                imageViewAction.setImageResource(R.drawable.ic_remove_friend);
                imageViewAction.setOnClickListener(v -> showRemoveFriendDialog(user));
            } else {
                imageViewAction.setImageResource(R.drawable.ic_add_friend);
                imageViewAction.setOnClickListener(v -> addFriend(user));
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_USER_ID, user.getUid());
                context.startActivity(intent);
            });
        }

        private void showRemoveFriendDialog(User user) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Remove Friend")
                    .setMessage("Are you sure you want to remove " + user.getUsername() + "?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Remove", (dialog, which) -> removeFriend(user))
                    .show();
        }

        private void addFriend(User user) {
            imageViewAction.setEnabled(false);
            // ISPRAVAN POZIV - samo sa ID-jem prijatelja
            userService.addFriend(currentUserId,user.getUid()).addOnCompleteListener(task -> {
                imageViewAction.setEnabled(true);
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Added " + user.getUsername(), Toast.LENGTH_SHORT).show();
                    friendIdList.add(user.getUid());
                    // *** GLAVNA ISPRAVKA: AŽURIRAJ I CACHE LISTU PRIJATELJA ***
                    FriendsListActivity.myFriendsListCache.add(user);
                    notifyItemChanged(getAdapterPosition());
                } else {
                    Toast.makeText(context, "Failed to add friend", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void removeFriend(User user) {
            imageViewAction.setEnabled(false);
            // ISPRAVAN POZIV - samo sa ID-jem prijatelja
            userService.removeFriend(currentUserId,user.getUid()).addOnCompleteListener(task -> {
                imageViewAction.setEnabled(true);
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Removed " + user.getUsername(), Toast.LENGTH_SHORT).show();
                    friendIdList.remove(user.getUid());
                    // *** GLAVNA ISPRAVKA: AŽURIRAJ I CACHE LISTU PRIJATELJA ***
                    FriendsListActivity.myFriendsListCache.removeIf(u -> u.getUid().equals(user.getUid()));

                    // Ako nismo u pretrazi, ukloni item iz liste vizuelno
                    if (!FriendsListActivity.isSearchActive) {
                        int currentPosition = getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            displayedUserList.remove(currentPosition);
                            notifyItemRemoved(currentPosition);
                            // Proveri da li je lista sada prazna
                            if (displayedUserList.isEmpty()) {
                                ((FriendsListActivity)context).updateUIVisibility();
                            }
                        }
                    } else {
                        // Ako smo u pretrazi, samo osveži ikonicu
                        notifyItemChanged(getAdapterPosition());
                    }
                } else {
                    Toast.makeText(context, "Failed to remove friend", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
