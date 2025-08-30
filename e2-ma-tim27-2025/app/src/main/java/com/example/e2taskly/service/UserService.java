package com.example.e2taskly.service;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;

import com.example.e2taskly.data.repository.UserRepository;
import com.example.e2taskly.model.User;
import com.example.e2taskly.util.SharedPreferencesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.List;

public class UserService {
    private final UserRepository userRepository;
    private final SharedPreferencesUtil sharedPreferences;
    public UserService(Context context){

        userRepository = new UserRepository(context);
        sharedPreferences = new SharedPreferencesUtil(context);
    }
    public void registerUser(String email, String username,String password, String confirmPassword, String selectedAvatar, OnCompleteListener<AuthResult> listener){
        String validationError = validateRegistrationInput(email, username, password, confirmPassword, selectedAvatar);
        if(validationError!=null){
            listener.onComplete(Tasks.forException(new Exception(validationError)));
            return;
        }

        userRepository.checkUsernameExists(username)
                .continueWithTask(isUniqueTask -> {
                    if (!isUniqueTask.isSuccessful()) { throw isUniqueTask.getException(); }
                    if (!isUniqueTask.getResult().isEmpty()) {
                        throw new Exception("Username already exists.");
                    }

                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(username);
                    newUser.setAvatar(selectedAvatar);
                    newUser.setLevel(1);
                    newUser.setXp(0);
                    newUser.setActivated(false);
                    newUser.setRegistrationTime(new Date());
                    newUser.setTitle("Rookie");
                    newUser.setPowerPoints(0);
                    newUser.setCoins(0);

                    return userRepository.registerUser(email, password, newUser);
                })
                .addOnCompleteListener(authTask -> {
                    if (!authTask.isSuccessful() && authTask.getException() != null && authTask.getException().getCause() instanceof FirebaseAuthUserCollisionException) {
                        Exception emailExistsException = new Exception("The provided email address is already in use.");
                        listener.onComplete(Tasks.forException(emailExistsException));
                    } else {
                        listener.onComplete(authTask);
                    }
                });
    }
    public void loginUser(String email, String password, OnCompleteListener<AuthResult> listener){
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            Task<AuthResult> failedTask = Tasks.forException(new Exception("Please enter your email and password."));
            listener.onComplete(failedTask);
            return;
        }
        userRepository.loginUser(email, password).addOnCompleteListener(loginTask -> {
            if (loginTask.isSuccessful()) {
                FirebaseUser firebaseUser = userRepository.getCurrentUser();

                if (firebaseUser == null) {
                    listener.onComplete(Tasks.forException(new Exception("User not found after login.")));
                    return;
                }

                if (firebaseUser.isEmailVerified()) {

                    userRepository.updateUserActivationStatus(firebaseUser.getUid(), true);

                    sharedPreferences.saveUserSession(firebaseUser.getUid());

                    listener.onComplete(loginTask);

                } else {
                    long creationTimestamp = firebaseUser.getMetadata().getCreationTimestamp();
                    long twentyFourHoursInMs = 24 * 60 * 60 * 1000;
                    boolean isExpired = (System.currentTimeMillis() - creationTimestamp) > twentyFourHoursInMs;

                    if (isExpired) {
                        userRepository.reauthenticateAndDeleteUser(email, password)
                                .addOnCompleteListener(deleteTask -> {
                                    if (deleteTask.isSuccessful()) {
                                        Exception userDeletedException = new Exception("Your activation link has expired. The account has been deleted, please register again.");
                                        listener.onComplete(Tasks.forException(userDeletedException));
                                    } else {
                                        Exception deleteFailedException = new Exception("Error deleting the old account. Please contact support.");
                                        listener.onComplete(Tasks.forException(deleteFailedException));
                                    }
                                });
                    } else {
                        userRepository.logout();
                        Exception notVerifiedException = new Exception("Account not activated. Please check your email for the verification link.");
                        listener.onComplete(Tasks.forException(notVerifiedException));
                    }
                }
            } else {
                Exception loginFailedException = new Exception("Incorrect email or password.");
                listener.onComplete(Tasks.forException(loginFailedException));
            }
        });
    }
    public void getUserProfile(String uid,OnCompleteListener<User> listener) {
        if (uid==null || uid.isEmpty()) {
            listener.onComplete(Tasks.forException(new Exception("No user is currently logged in.")));
            return;
        }
        userRepository.getUserProfile(uid).addOnCompleteListener(listener);
    }
    public void changePassword(String oldPassword, String newPassword, String confirmPassword, OnCompleteListener<Void> listener) {
        String validationError = validatePasswordChange(oldPassword, newPassword, confirmPassword);
        if (validationError != null) {
            listener.onComplete(Tasks.forException(new Exception(validationError)));
            return;
        }

        userRepository.changePassword(oldPassword, newPassword).addOnCompleteListener(listener);
    }
    private String validateRegistrationInput(String email, String username, String password, String confirmPassword, String selectedAvatar) {
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
    private String validatePasswordChange(String oldPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            return "All password fields are required.";
        }
        if (newPassword.length() < 6) {
            return "The new password must be at least 6 characters long.";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "The new passwords do not match.";
        }
        if (oldPassword.equals(newPassword)) {
            return "The new password cannot be the same as the old password.";
        }
        return null;
    }
    public String getCurrentUserId(){
        FirebaseUser firebaseUser = userRepository.getCurrentUser();
        if(firebaseUser != null){
            return firebaseUser.getUid();
        }
        return null;
    }
    public void getAllUsers(OnCompleteListener<List<User>> listener){
        userRepository.getAllUsers().addOnCompleteListener(listener);
    }
    public void logoutUser() {
        userRepository.logout();
        sharedPreferences.clearUserSession();
    }
}
