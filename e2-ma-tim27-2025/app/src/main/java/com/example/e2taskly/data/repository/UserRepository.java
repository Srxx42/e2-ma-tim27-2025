package com.example.e2taskly.data.repository;

import android.content.Context;

import com.example.e2taskly.data.database.UserLocalDataSource;
import com.example.e2taskly.data.remote.UserRemoteDataSource;
import com.example.e2taskly.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
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
}
