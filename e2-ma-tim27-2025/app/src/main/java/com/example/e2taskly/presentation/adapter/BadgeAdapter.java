package com.example.e2taskly.presentation.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e2taskly.R;
import com.example.e2taskly.model.UserBadge;
import com.example.e2taskly.model.enums.BadgeType;

import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private List<UserBadge> badgeList;
    private Context context;

    public BadgeAdapter(Context context, List<UserBadge> badgeList) {
        this.context = context;
        this.badgeList = badgeList;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        UserBadge badge = badgeList.get(position);
        holder.bind(badge);
    }

    @Override
    public int getItemCount() {
        return badgeList.size();
    }
    
    public void updateBadges(List<UserBadge> newBadges) {
        badgeList.clear();
        badgeList.addAll(newBadges);
        notifyDataSetChanged();
    }

    class BadgeViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewBadgeIcon;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewBadgeIcon = itemView.findViewById(R.id.imageViewBadgeIcon);
        }

        void bind(UserBadge badge) {
            int badgeIconResId = getBadgeIcon(badge.getBadgeType());
            if (badgeIconResId != 0) {
                imageViewBadgeIcon.setImageResource(badgeIconResId);
            }
        }

        private int getBadgeIcon(BadgeType badgeType) {
            switch (badgeType) {
                case BRONZE:
                    return R.drawable.badge_bronze;
                case SILVER:
                    return R.drawable.badge_silver;
                case GOLD:
                    return R.drawable.badge_gold;
                case DIAMOND:
                    return R.drawable.badge_diamond;
                default:
                    return 0;
            }
        }
    }
}