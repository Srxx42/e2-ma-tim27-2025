package com.example.e2taskly.data.repository.remote;

import com.example.e2taskly.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class UserRemoteDataSource {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    public UserRemoteDataSource() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }
    public void createUserInAuth(String email, String password, OnCompleteListener<AuthResult> listener) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }
    public void sendVerificationEmail() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification();
        }
    }
    public void saveUserDetails(User user) {
        String uid = user.getUid();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("avatar", user.getAvatar());
        userMap.put("level", user.getLevel());
        userMap.put("xp", user.getXp());
        userMap.put("is_activated", user.isActivated());
        userMap.put("registration_time", user.getRegistrationTime());

        db.collection("users").document(uid).set(userMap);
    }
    public void checkUsernameExists(String username, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnCompleteListener(listener);
    }
}
