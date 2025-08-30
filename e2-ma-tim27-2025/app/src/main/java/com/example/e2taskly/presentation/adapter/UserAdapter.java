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

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private final List<User> userList;
    private final Context context;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
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
    public void filterList(List<User> filteredList) {
        userList.clear();
        userList.addAll(filteredList);
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

            imageViewAddFriend.setOnClickListener(v -> {
                // TODO: Implementirati logiku za slanje zahteva za prijateljstvo
                Toast.makeText(context, "Friend request sent to " + user.getUsername(), Toast.LENGTH_SHORT).show();
                imageViewAddFriend.setEnabled(false); // Onemogući ikonicu nakon klika
                imageViewAddFriend.setAlpha(0.5f);
            });
        }
    }
}
