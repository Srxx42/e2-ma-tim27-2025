package com.example.e2taskly.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.e2taskly.data.database.EquipmentLocalDataSource;
import com.example.e2taskly.data.remote.EquipmentRemoteDataSource;
import com.example.e2taskly.model.EquipmentTemplate;
import com.example.e2taskly.model.UserInventoryItem;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EquipmentRepository {
    private final EquipmentLocalDataSource localDataSource;
    private final EquipmentRemoteDataSource remoteDataSource;
    public EquipmentRepository(Context context){
        this.localDataSource = new EquipmentLocalDataSource(context);
        this.remoteDataSource = new EquipmentRemoteDataSource();
    }
    public Task<List<EquipmentTemplate>> syncAndGetEquipmentTemplates() {
        return remoteDataSource.getEquipmentTemplates().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            List<EquipmentTemplate> templates = new ArrayList<>();
            for (QueryDocumentSnapshot document : task.getResult()) {
                EquipmentTemplate template = document.toObject(EquipmentTemplate.class);
                Log.e("IDIBRE",template.getName());
                templates.add(template);
            }
            localDataSource.saveTemplates(templates);

            return templates;
        });
    }
    public List<EquipmentTemplate> getLocalEquipmentTemplates(){
        return localDataSource.getTemplates();
    }
    public Task<List<UserInventoryItem>> syncAndGetUserInventory(String userId) {
        return remoteDataSource.getUserInventory(userId).continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            List<UserInventoryItem> inventoryItems = new ArrayList<>();
            for (QueryDocumentSnapshot document : task.getResult()) {
                UserInventoryItem item = document.toObject(UserInventoryItem.class);
                item.setInventoryId(document.getId());
                inventoryItems.add(item);
            }

            localDataSource.saveInventoryForUser(userId, inventoryItems);

            return inventoryItems;
        });
    }
    public List<UserInventoryItem> getLocalUserInventory(String userId){
        return localDataSource.getInventoryForUser(userId);
    }
    public Task<DocumentReference> buyInventoryItem(String userId, UserInventoryItem item){
        return remoteDataSource.addInventoryItem(userId, item).addOnSuccessListener(documentReference -> {
            syncAndGetUserInventory(userId);
        });
    }
    public Task<Void> updateInventoryItem(String userId, UserInventoryItem item) {
        return remoteDataSource.updateInventoryItem(userId, item).addOnSuccessListener(aVoid -> {
            syncAndGetUserInventory(userId);
        });
    }
    public Task<Void> deleteInventoryItem(String userId, String inventoryId) {
        return remoteDataSource.deleteInventoryItem(userId, inventoryId).addOnSuccessListener(aVoid -> {
            syncAndGetUserInventory(userId);
        });
    }
    public void clearLocalInventory(String userId) {
        localDataSource.clearUserInventory(userId);
    }

}
