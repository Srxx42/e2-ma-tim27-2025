package com.example.e2taskly.data.remote;

import com.example.e2taskly.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
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
    public Task<AuthResult> createUserInAuth(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }
    public Task<AuthResult> signInWithEmailAndPassword(String email, String password){
        return mAuth.signInWithEmailAndPassword(email, password);
    }
    public void sendVerificationEmail() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification();
        }
    }
    public Task<Void> saveUserDetails(User user) {
        String uid = user.getUid();
        return db.collection("users").document(uid).set(user);
    }
    public Task<QuerySnapshot> checkUsernameExists(String username) {
        return db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get();
    }
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public Task<Void> reauthenticateUser(String email, String password) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return Tasks.forException(new Exception("User not logged in for re-authentication."));
        }
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        return user.reauthenticate(credential);
    }
    public Task<Void> deleteUserFromAuth() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return Tasks.forException(new Exception("User not logged in for deletion."));
        }
        return user.delete();
    }
    public Task<Void> deleteUserDetails(String uid) {
        return db.collection("users").document(uid).delete();
    }
    public void logoutUser() {
        mAuth.signOut();
    }
}
