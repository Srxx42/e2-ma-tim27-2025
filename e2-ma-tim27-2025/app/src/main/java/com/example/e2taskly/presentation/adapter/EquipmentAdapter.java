package com.example.e2taskly.presentation.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e2taskly.R;
import com.example.e2taskly.model.EquipmentTemplate;
import com.example.e2taskly.model.User;
import com.example.e2taskly.model.UserInventoryItem;
import com.example.e2taskly.model.enums.EquipmentType;
import com.example.e2taskly.service.LevelingService;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder> {

    private Context context;
    private List<UserInventoryItem> inventory;
    private Map<String, EquipmentTemplate> templates;
    private OnActivateClickListener listener;
    private final User currentUser;
    private final LevelingService levelingService;
    private final boolean isActivationLocked;



    public interface OnActivateClickListener {
        void onActivateClick(UserInventoryItem item);
        void onUpgradeClick(UserInventoryItem item);
    }

    public EquipmentAdapter(User currentUser, List<UserInventoryItem> inventory, Map<String, EquipmentTemplate> templates, OnActivateClickListener listener, boolean isActivationLocked) {
        this.currentUser = currentUser;
        this.inventory = inventory;
        this.templates = templates;
        this.listener = listener;
        this.levelingService = new LevelingService();
        this.isActivationLocked = isActivationLocked;
    }

    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_equipment, parent, false);
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        UserInventoryItem currentItem = inventory.get(position);
        EquipmentTemplate template = templates.get(currentItem.getTemplateId());

        if (template != null) {
            holder.textViewName.setText(template.getName());
            holder.textViewDescription.setText(template.getDescription());

            String itemId = template.getId();
            if (itemId.contains("boots")) {
                holder.imageView.setImageResource(R.drawable.ic_boots);
            } else if (itemId.contains("gloves")) {
                holder.imageView.setImageResource(R.drawable.ic_gloves);
            } else if (itemId.contains("shield")) {
                holder.imageView.setImageResource(R.drawable.ic_shield);
            } else if (itemId.contains("potion")) {
                holder.imageView.setImageResource(R.drawable.ic_potion);
            } else if (itemId.contains("bow")) {
                holder.imageView.setImageResource(R.drawable.ic_bow);
            } else if (itemId.contains("sword")) {
                holder.imageView.setImageResource(R.drawable.ic_sword);
            }
            if (template.getType() == EquipmentType.WEAPON) {
                holder.buttonUpgrade.setVisibility(View.VISIBLE);
                holder.textUpgradeCost.setVisibility(View.VISIBLE);
                holder.textViewStatus.setVisibility(View.VISIBLE);


                int reward = levelingService.getCoinsRewardForLevel(currentUser.getLevel());
                int upgradeCost = (int) (reward * (template.getUpgradeCostPercentage()/100.0));
                holder.textUpgradeCost.setText(String.format(Locale.getDefault(), "Cost: %d coins", upgradeCost));
                Log.d("EquipmentAdapter", "Bonus for " + template.getName() + ": " + currentItem.getCurrentBonusValue());
                holder.buttonUpgrade.setOnClickListener(v -> listener.onUpgradeClick(currentItem));
            } else {
                holder.buttonActivate.setVisibility(View.VISIBLE);
                holder.buttonUpgrade.setVisibility(View.GONE);
                holder.textUpgradeCost.setVisibility(View.GONE);
            }
            if (currentItem.isActivated()) {
                holder.buttonActivate.setEnabled(false);
                holder.buttonActivate.setText("Active");
                if (template.getDurationInFights() > 0) {
                    holder.textViewStatus.setText("Active for " + currentItem.getFightsRemaining() + " more battles");
                    holder.textViewStatus.setVisibility(View.VISIBLE);
                } else if(template.getType() == EquipmentType.WEAPON){
                    holder.textViewStatus.setText(String.format(Locale.getDefault(), "Bonus: %.2f%%", currentItem.getCurrentBonusValue()));
                    holder.textViewStatus.setTextColor(Color.parseColor("#007BFF"));
                    holder.textViewStatus.setVisibility(View.VISIBLE);
                } else{
                    holder.textViewStatus.setVisibility(View.GONE);
                }
            } else {
                if (isActivationLocked) {
                    holder.buttonActivate.setEnabled(false);
                } else {
                    holder.buttonActivate.setEnabled(true);
                }
                holder.buttonActivate.setText("Activate");
                holder.textViewStatus.setVisibility(View.GONE);
            }

            holder.buttonActivate.setOnClickListener(v -> listener.onActivateClick(currentItem));
        }
    }

    @Override
    public int getItemCount() {
        return inventory.size();
    }

    static class EquipmentViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewName, textViewDescription, textViewStatus, textUpgradeCost;
        Button buttonActivate, buttonUpgrade;

        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewEquipment);
            textViewName = itemView.findViewById(R.id.textViewEquipmentName);
            textViewDescription = itemView.findViewById(R.id.textViewEquipmentDescription);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            buttonActivate = itemView.findViewById(R.id.buttonActivate);
            buttonUpgrade = itemView.findViewById(R.id.buttonUpgrade);
            textUpgradeCost = itemView.findViewById(R.id.textUpgradeCost);
        }
    }
}
