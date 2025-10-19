package com.example.e2taskly.data.remote;

import com.example.e2taskly.model.UserInventoryItem;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class EquipmentRemoteDataSource {
    private final FirebaseFirestore db;
    public EquipmentRemoteDataSource(){
        this.db = FirebaseFirestore.getInstance();
    }

    public Task<QuerySnapshot> getEquipmentTemplates() {
        return db.collection("equipment_templates").get();
    }
    public Task<QuerySnapshot> getUserInventory(String userId) {
        return db.collection("users").document(userId).collection("inventory").get();
    }
    public Task<DocumentReference> addInventoryItem(String userId, UserInventoryItem item) {
        item.setUserId(userId);
        Task<DocumentReference> addTask = db.collection("users")
                .document(userId)
                .collection("inventory")
                .add(item);

        addTask.addOnSuccessListener(documentReference -> {
            String inventoryId = documentReference.getId();
            item.setInventoryId(inventoryId);

            db.collection("users")
                    .document(userId)
                    .collection("inventory")
                    .document(inventoryId)
                    .set(item);
        });

        return addTask;
    }
    public Task<Void> updateInventoryItem(String userId, UserInventoryItem item) {
        String inventoryId = item.getInventoryId();
        return db.collection("users").document(userId)
                .collection("inventory").document(inventoryId)
                .set(item);
    }
    public Task<Void> deleteInventoryItem(String userId, String inventoryId) {
        return db.collection("users").document(userId)
                .collection("inventory").document(inventoryId)
                .delete();
    }
}
