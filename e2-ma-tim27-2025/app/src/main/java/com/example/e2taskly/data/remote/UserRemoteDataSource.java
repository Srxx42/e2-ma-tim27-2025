package com.example.e2taskly.data.remote;

import com.example.e2taskly.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRemoteDataSource {
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
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
    public Task<Void> updateUserActivationStatus(String uid, boolean isActivated) {
        return db.collection("users")
                .document(uid)
                .update("activated",isActivated);
    }
    public Task<DocumentSnapshot> getUserDetails(String uid) {
        return db.collection("users").document(uid).get();
    }
    public Task<Void> updatePassword(String newPassword){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            return user.updatePassword(newPassword);
        }
        return Tasks.forException(new Exception("User not logged in"));
    }
    public Task<QuerySnapshot> getAllUsers(){
        return db.collection("users").get();
    }
    public void updateStreakData(String uid, Date date, int newStreak){
        db.collection("users").document(uid)
                .update("activeDaysStreak",newStreak,
                        "lastActivityDate", date
                );
    }
    public void logoutUser() {
        mAuth.signOut();
    }
    public Task<Void> addFriend(String currentUid, String friendUid) {
        DocumentReference currentUserDoc = db.collection("users").document(currentUid);
        DocumentReference friendUserDoc = db.collection("users").document(friendUid);

        WriteBatch batch = db.batch();

        batch.update(currentUserDoc, "friendIds", FieldValue.arrayUnion(friendUid));

        batch.update(friendUserDoc, "friendIds", FieldValue.arrayUnion(currentUid));

        return batch.commit();
    }
    public Task<Void> removeFriend(String currentUid, String friendUid) {
        DocumentReference currentUserDoc = db.collection("users").document(currentUid);
        DocumentReference friendUserDoc = db.collection("users").document(friendUid);

        WriteBatch batch = db.batch();

        batch.update(currentUserDoc, "friendIds", FieldValue.arrayRemove(friendUid));

        batch.update(friendUserDoc, "friendIds", FieldValue.arrayRemove(currentUid));

        return batch.commit();
    }
    public Task<List<User>> getUsersByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        final int CHUNK_SIZE = 30;

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for (int i = 0; i < userIds.size(); i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, userIds.size());
            List<String> chunk = userIds.subList(i, end);

            Task<QuerySnapshot> task = db.collection("users")
                    .whereIn("uid", chunk)
                    .get();
            tasks.add(task);
        }

        return Tasks.whenAllSuccess(tasks).continueWith(task -> {
            List<User> userList = new ArrayList<>();

            List<Object> querySnapshots = task.getResult();

            for (Object snapshotObject : querySnapshots) {
                QuerySnapshot snapshot = (QuerySnapshot) snapshotObject;
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
            }
            return userList;
        });
    }
    public Task<QuerySnapshot> searchUsersByUsername(String query) {
        if (query == null || query.isEmpty()) {
            return Tasks.forResult(null);
        }

        return db.collection("users")
                .orderBy("username")
                .startAt(query)
                .endAt(query + '\uf8ff')
                .limit(20)
                .get();
    }
    public Task<Void> updateUserAllianceId(String uid, String allianceId) {
        return db.collection("users").document(uid).update("allianceId", allianceId);
    }
    public Task<Void> updateUserFcmToken(String uid, String token) {
        return db.collection("users").document(uid).update("fcmToken", token);
    }
    public Task<Void> updateUserCoins(String uid, int newCoinAmount) {
        return db.collection("users").document(uid).update("coins", newCoinAmount);
    }
}
