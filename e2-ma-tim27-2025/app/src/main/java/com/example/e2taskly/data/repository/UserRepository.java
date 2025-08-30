package com.example.e2taskly.data.repository;

import android.content.Context;

import com.example.e2taskly.data.database.UserLocalDataSource;
import com.example.e2taskly.data.remote.UserRemoteDataSource;
import com.example.e2taskly.model.User;
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
    public void updateUserActivationStatus(String uid, boolean isActivated) {

        localDataSource.updateUserActivationStatus(uid, isActivated);
    }

    public FirebaseUser getCurrentUser() {
        return remoteDataSource.getCurrentUser();
    }
    public void logout(){
        remoteDataSource.logoutUser();
    }

}
