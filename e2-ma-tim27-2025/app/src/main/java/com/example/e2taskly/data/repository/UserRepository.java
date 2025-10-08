package com.example.e2taskly.data.repository;

import android.content.Context;

import com.example.e2taskly.data.database.UserLocalDataSource;
import com.example.e2taskly.data.remote.UserRemoteDataSource;
import com.example.e2taskly.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserRepository {
    private UserLocalDataSource localDataSource;
    private UserRemoteDataSource remoteDataSource;
    public UserRepository(Context context){
        localDataSource = new UserLocalDataSource(context);
        remoteDataSource = new UserRemoteDataSource();
    }
    public Task<AuthResult> registerUser(String email, String password, User user){
        return remoteDataSource.createUserInAuth(email, password)
                .onSuccessTask(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();
                        user.setUid(uid);

                        remoteDataSource.sendVerificationEmail();
                        localDataSource.addUser(user);

                        return remoteDataSource.saveUserDetails(user).continueWith(task -> authResult);
                    }
                    return Tasks.forException(new Exception("FirebaseUser is null after creation."));
                });
    }
    public Task<QuerySnapshot> checkUsernameExists(String username){
        return remoteDataSource.checkUsernameExists(username);
    }
    public Task<AuthResult> loginUser(String email, String password) {
        return remoteDataSource.signInWithEmailAndPassword(email, password);
    }
    public Task<Void> reauthenticateAndDeleteUser(String email, String password) {
        FirebaseUser user = remoteDataSource.getCurrentUser();
        if (user == null) {
            return Tasks.forException(new Exception("The user is not logged in."));
        }

        String uidToDelete = user.getUid();


        return remoteDataSource.reauthenticateUser(email, password)
                .continueWithTask(reauthTask -> {
                    if (!reauthTask.isSuccessful()) {

                        Exception exception = reauthTask.getException();
                        if (exception != null) {
                            throw exception;
                        } else {
                            throw new Exception("Reauthentication failed with no exception provided.");
                        }
                    }


                    Task<Void> deleteAuthTask = remoteDataSource.deleteUserFromAuth();


                    Task<Void> deleteFirestoreTask = remoteDataSource.deleteUserDetails(uidToDelete);


                    localDataSource.deleteUser(uidToDelete);


                    return Tasks.whenAll(deleteAuthTask, deleteFirestoreTask);
                });
    }
    public Task<Void> updateUserActivationStatus(String uid, boolean isActivated) {
        localDataSource.updateUserActivationStatus(uid, isActivated);
        return remoteDataSource.updateUserActivationStatus(uid, isActivated);
    }

    public FirebaseUser getCurrentUser() {
        return remoteDataSource.getCurrentUser();
    }
    public Task<User> getUserProfile(String uid) {
        return remoteDataSource.getUserDetails(uid).onSuccessTask(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    return Tasks.forResult(user);
                }
            }
            return Tasks.forException(new Exception("User data not found in Firestore."));
        });
    }
    public Task<Void> changePassword(String oldPassword, String newPassword) {
        FirebaseUser firebaseUser = remoteDataSource.getCurrentUser();
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            return Tasks.forException(new Exception("User not logged in or email is missing."));
        }

        return remoteDataSource.reauthenticateUser(firebaseUser.getEmail(), oldPassword)
                .onSuccessTask(aVoid -> remoteDataSource.updatePassword(newPassword));
    }
    public Task<List<User>> getAllUsers(){
        return remoteDataSource.getAllUsers().onSuccessTask(queryDocumentSnapshots -> {
            if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                List<User> userList = new ArrayList<>();
                for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                    User user = doc.toObject(User.class);
                    if(user != null){
                        userList.add(user);
                    }
                }
                return Tasks.forResult(userList);
            }
            return Tasks.forResult(new ArrayList<>());
        });
    }
    public void updateStreakData(String uid, int newStreak){
        Date date = new Date();
        remoteDataSource.updateStreakData(uid,date,newStreak);
        localDataSource.updateStreakData(uid,date,newStreak);
    }
    public void updateUser(User user){
        localDataSource.updateUser(user);
        remoteDataSource.saveUserDetails(user);
    }
    public void logout(){
        remoteDataSource.logoutUser();
    }
    public Task<Void> addFriend(String currentUid, String friendUid) {
        return remoteDataSource.addFriend(currentUid, friendUid)
                .onSuccessTask(aVoid -> {
                    localDataSource.addFriend(currentUid, friendUid);
                    return Tasks.forResult(null);
                });
    }
    public Task<Void> removeFriend(String currentUid, String friendUid) {
        return remoteDataSource.removeFriend(currentUid, friendUid)
                .onSuccessTask(aVoid -> {
                    localDataSource.removeFriend(currentUid, friendUid);
                    return Tasks.forResult(null);
                });
    }
    public Task<List<User>> getFriends(List<String> friendIds) {
        if (friendIds == null || friendIds.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }
        return remoteDataSource.getUsersByIds(friendIds);
    }
    public Task<List<User>> searchUsersByUsername(String query) {
        return remoteDataSource.searchUsersByUsername(query)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    List<User> userList = new ArrayList<>();
                    if (task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                userList.add(user);
                            }
                        }
                    }
                    return userList;
                });
    }
    public Task<Void> updateUserAllianceId(String uid, String allianceId) {
        localDataSource.updateUserAllianceId(uid, allianceId);
        return remoteDataSource.updateUserAllianceId(uid, allianceId);
    }
    public Task<List<User>> getUsersByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }
        return remoteDataSource.getUsersByIds(userIds);
    }
    public Task<Void> updateUserFcmToken(String uid, String token) {
        localDataSource.updateUserFcmToken(uid, token);
        return remoteDataSource.updateUserFcmToken(uid, token);
    }
    public Task<User> getUserLocallyFirst(String uid) {
        User localUser = localDataSource.getUser(uid);

        if (localUser != null) {
            return Tasks.forResult(localUser);
        } else {
            return remoteDataSource.getUserDetails(uid).onSuccessTask(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User remoteUser = documentSnapshot.toObject(User.class);
                    if (remoteUser != null) {
                        localDataSource.addUser(remoteUser);
                        return Tasks.forResult(remoteUser);
                    }
                }
                return Tasks.forException(new Exception("Korisnik nije pronađen."));
            });
        }
    }
    public Task<Void> updateUserCoins(String uid, int newCoinAmount) {
        return remoteDataSource.updateUserCoins(uid, newCoinAmount).addOnSuccessListener(aVoid -> {
            localDataSource.updateUserCoins(uid, newCoinAmount);
        });
    }
}
