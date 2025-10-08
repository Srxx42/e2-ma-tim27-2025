package com.example.e2taskly.presentation.adapter;

import android.content.Context;
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
import com.example.e2taskly.service.EquipmentService;

import java.util.List;
import java.util.Locale;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private final Context context;
    private final List<EquipmentTemplate> equipmentList;
    private final OnPurchaseClickListener purchaseClickListener;
    private final EquipmentService equipmentService;
    private final int previousBossReward;

    public interface OnPurchaseClickListener {
        void onPurchaseClick(EquipmentTemplate item);
    }

    public ShopAdapter(Context context, List<EquipmentTemplate> equipmentList, OnPurchaseClickListener purchaseClickListener, int previousBossReward) {
        this.context = context;
        this.equipmentList = equipmentList;
        this.purchaseClickListener = purchaseClickListener;
        this.previousBossReward = previousBossReward;
        this.equipmentService = new EquipmentService(context);
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        EquipmentTemplate currentItem = equipmentList.get(position);

        holder.textViewItemName.setText(currentItem.getName());
        holder.textViewItemDescription.setText(currentItem.getDescription());
        String itemId = currentItem.getId();
        if (itemId.contains("boots")) {
            holder.imageViewItem.setImageResource(R.drawable.ic_boots);
        } else if (itemId.contains("gloves")) {
            holder.imageViewItem.setImageResource(R.drawable.ic_gloves);
        } else if (itemId.contains("shield")) {
            holder.imageViewItem.setImageResource(R.drawable.ic_shield);
        } else if (itemId.contains("potion")) {
            holder.imageViewItem.setImageResource(R.drawable.ic_potion);
        } else if (itemId.contains("bow")) {
            holder.imageViewItem.setImageResource(R.drawable.ic_bow);
        } else if (itemId.contains("sword")) {
            holder.imageViewItem.setImageResource(R.drawable.ic_sword);
        }

        int itemPrice = equipmentService.calculateItemPrice(currentItem, previousBossReward);

        holder.textViewItemPrice.setText(String.format(Locale.getDefault(), "Price: %d coins", itemPrice));

        holder.buttonPurchase.setOnClickListener(v -> {
            if (purchaseClickListener != null) {
                purchaseClickListener.onPurchaseClick(currentItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return equipmentList != null ? equipmentList.size() : 0;
    }

    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView textViewItemName;
        TextView textViewItemDescription;
        TextView textViewItemPrice;
        Button buttonPurchase;
        ImageView imageViewItem;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItemName = itemView.findViewById(R.id.textViewItemName);
            textViewItemDescription = itemView.findViewById(R.id.textViewItemDescription);
            textViewItemPrice = itemView.findViewById(R.id.textViewItemPrice);
            buttonPurchase = itemView.findViewById(R.id.buttonPurchase);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            textViewItemName = itemView.findViewById(R.id.textViewItemName);
            textViewItemDescription = itemView.findViewById(R.id.textViewItemDescription);
            textViewItemPrice = itemView.findViewById(R.id.textViewItemPrice);
            buttonPurchase = itemView.findViewById(R.id.buttonPurchase);
        }
    }
}
