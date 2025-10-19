package com.example.e2taskly.presentation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.e2taskly.R;
import com.example.e2taskly.model.Boss;
import com.example.e2taskly.model.SpecialMissionProgress;
import com.example.e2taskly.model.User;
import com.example.e2taskly.service.MissionProgressService;
import java.util.List;
import java.util.Map;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder> {

    private final Context context;
    private final List<SpecialMissionProgress> progressList;
    private final Map<String, User> userMap;
    private final Boss currentBoss;
    private final MissionProgressService missionProgressService;

    public ProgressAdapter(Context context, List<SpecialMissionProgress> progressList, Map<String, User> userMap, Boss currentBoss, MissionProgressService missionProgressService) {
        this.context = context;
        this.progressList = progressList;
        this.userMap = userMap;
        this.currentBoss = currentBoss;
        this.missionProgressService = missionProgressService;
    }

    @NonNull
    @Override
    public ProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_progress, parent, false);
        return new ProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressViewHolder holder, int position) {
        SpecialMissionProgress progress = progressList.get(position);
        User user = userMap.get(progress.getUserUid());
        if (user != null) {
            holder.bind(progress, user);
        }
    }

    @Override
    public int getItemCount() {
        return progressList.size();
    }

    class ProgressViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivCheck;
        TextView tvUsername, tvPercentage, tvEasy, tvHard, tvAttacks, tvShop, tvMessages;
        ProgressBar pbProgress;

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_user_avatar);
            ivCheck = itemView.findViewById(R.id.iv_check_progress);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvPercentage = itemView.findViewById(R.id.tv_user_percentage);
            tvEasy = itemView.findViewById(R.id.tv_easy_task_count);
            tvHard = itemView.findViewById(R.id.tv_hard_task_count);
            tvAttacks = itemView.findViewById(R.id.tv_boss_attacks_count);
            tvShop = itemView.findViewById(R.id.tv_items_bought_count);
            tvMessages = itemView.findViewById(R.id.tv_messages_count);
            pbProgress = itemView.findViewById(R.id.pb_user_progress);
        }

        void bind(SpecialMissionProgress progress, User user) {
            tvUsername.setText(user.getUsername());
            tvEasy.setText(String.valueOf(progress.getEasyTaskCount()));
            tvHard.setText(String.valueOf(progress.getHardTaskCount()));
            tvAttacks.setText(String.valueOf(progress.getSuccessfulBossHitCount()));
            tvShop.setText(String.valueOf(progress.getShoppingCount()));
            tvMessages.setText(String.valueOf(progress.getMessageCount().size()));

            int resId = context.getResources().getIdentifier(user.getAvatar(), "drawable", context.getPackageName());
            if (resId != 0) ivAvatar.setImageResource(resId);

            ivCheck.setVisibility(progress.isCompletedAll() ? View.VISIBLE : View.GONE);

            // Važno: Resetuj progress bar pre asinhronog poziva zbog recikliranja
            pbProgress.setProgress(0);
            tvPercentage.setText("0%");

            missionProgressService.calculateUserProgress(user.getUid(), currentBoss.getBossAppearanceDate())
                    .addOnSuccessListener(percentage -> {
                        // Provera da li je ViewHolder i dalje vezan za isti item
                        if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                            pbProgress.setProgress(percentage);
                            tvPercentage.setText(percentage + "%");
                        }
                    });
        }
    }
}