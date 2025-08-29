package com.example.e2taskly.data.repository;

import android.content.Context;

import com.example.e2taskly.data.database.UserLocalDataSource;
import com.example.e2taskly.data.remote.UserRemoteDataSource;
import com.example.e2taskly.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;

public class UserRepository {
    private UserLocalDataSource localDataSource;
    private UserRemoteDataSource remoteDataSource;
    public UserRepository(Context context){
        localDataSource = new UserLocalDataSource(context);
        remoteDataSource = new UserRemoteDataSource();
    }
    public void registerUser(String email, String password, User user, OnCompleteListener<AuthResult> listener){
        remoteDataSource.createUserInAuth(email,password,taskAuth -> {
            if(taskAuth.isSuccessful()){
                FirebaseUser firebaseUser = taskAuth.getResult().getUser();
                String uid = firebaseUser.getUid();
                user.setUid(uid);
                remoteDataSource.sendVerificationEmail();
                remoteDataSource.saveUserDetails(user);
                localDataSource.addUser(user);

            }
            listener.onComplete(taskAuth);
        });
    }
    public void checkUsernameExists(String username,OnCompleteListener<QuerySnapshot> listener){
        remoteDataSource.checkUsernameExists(username,listener);
    }
    public void loginUser(String email, String password,OnCompleteListener<AuthResult> listener) {
        remoteDataSource.signInWithEmailAndPassword(email, password,listener);
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

                        throw reauthTask.getException();
                    }


                    Task<Void> deleteAuthTask = remoteDataSource.deleteUserFromAuth();


                    Task<Void> deleteFirestoreTask = remoteDataSource.deleteUserDetails(uidToDelete);


                    localDataSource.deleteUser(uidToDelete); // Pretpostavljam da imate ovakvu metodu

                    // Koristimo Tasks.whenAll da sačekamo da se završe obe remote operacije
                    return Tasks.whenAll(deleteAuthTask, deleteFirestoreTask);
                });
    }
    public void updateUserActivationStatus(String uid, boolean isActivated) {

        localDataSource.updateUserActivationStatus(uid, isActivated);
    }

    public FirebaseUser getCurrentUser() {
        return remoteDataSource.getCurrentUser();
    }
    public void logoutUser(){
        remoteDataSource.logoutUser();
    }

}
