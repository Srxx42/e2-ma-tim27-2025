package com.example.e2taskly.service;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;

import com.example.e2taskly.data.repository.UserRepository;
import com.example.e2taskly.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.util.Date;

public class UserService {
    private UserRepository userRepository;
    public UserService(Context context){
        userRepository = new UserRepository(context);
    }
    public void registerUser(String email, String username,String password, String confirmPassword, String selectedAvatar, OnCompleteListener<AuthResult> listener){
        String validationError = validateInput(email, username, password, confirmPassword, selectedAvatar);
        if(validationError!=null){
            Task<AuthResult> failedTask = Tasks.forException(new Exception(validationError));
            listener.onComplete(failedTask);
            return;
        }
        userRepository.checkUsernameExists(username,isUniqueTask->{
            if(isUniqueTask.isSuccessful()){
                if(!isUniqueTask.getResult().isEmpty()){

                }else{
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(username);
                    newUser.setAvatar(selectedAvatar);
                    newUser.setLevel(1);
                    newUser.setXp(0);
                    newUser.setActivated(false);
                    newUser.setRegistrationTime(new Date());
                    userRepository.registerUser(email, password, newUser, authTask -> {
                        if (!authTask.isSuccessful() && authTask.getException() instanceof FirebaseAuthUserCollisionException) {
                            Task<AuthResult> failedTask = Tasks.forException(new Exception("The provided email address is already in use."));
                            listener.onComplete(failedTask);
                        } else {
                            listener.onComplete(authTask);
                        }
                    });
                }
            }else {
                listener.onComplete(Tasks.forException(isUniqueTask.getException()));
            }
        });
    }
    private String validateInput(String email, String username, String password, String confirmPassword, String selectedAvatar) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            return "All fields are required.";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Enter valid email address.";
        }
        String usernamePattern = "^[a-zA-Z0-9_]{3,20}$";
        if (!username.matches(usernamePattern)) {
            return "Username must have 3-20 characters.";
        }
        if (!password.equals(confirmPassword)) {
            return "Password must match.";
        }
        if (selectedAvatar == null || selectedAvatar.isEmpty()) {
            return "Choose avatar.";
        }
        return null;
    }

}
