package com.example.e2taskly.service;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.example.e2taskly.data.repository.UserRepository;
import com.example.e2taskly.model.User;
import com.example.e2taskly.util.SharedPreferencesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UserService {
    private final UserRepository userRepository;
    private final SharedPreferencesUtil sharedPreferences;
    private final LevelingService levelingService;
    public UserService(Context context){

        userRepository = new UserRepository(context);
        sharedPreferences = new SharedPreferencesUtil(context);
        levelingService = new LevelingService();
    }
    public Task<AuthResult> registerUser(String email, String username, String password, String confirmPassword, String selectedAvatar){
        String validationError = validateRegistrationInput(email, username, password, confirmPassword, selectedAvatar);
        if(validationError != null){
            return Tasks.forException(new Exception(validationError));
        }

        return userRepository.checkUsernameExists(username)
                .continueWithTask(isUniqueTask -> {
                    if (!isUniqueTask.isSuccessful()) {
                        throw isUniqueTask.getException();
                    }
                    if (isUniqueTask.getResult() != null && !isUniqueTask.getResult().isEmpty()) {
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
                    newUser.setActiveDaysStreak(0);
                    newUser.setLastActivityDate(new Date());

                    return userRepository.registerUser(email, password, newUser);
                })
                .continueWithTask(authTask -> {
                    if (!authTask.isSuccessful() && authTask.getException() != null && authTask.getException().getCause() instanceof FirebaseAuthUserCollisionException) {
                        return Tasks.forException(new Exception("The provided email address is already in use."));
                    }
                    return authTask;
                });
    }

    public Task<AuthResult> loginUser(String email, String password){
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return Tasks.forException(new Exception("Please enter your email and password."));
        }

        return userRepository.loginUser(email, password).continueWithTask(loginTask -> {
            if (!loginTask.isSuccessful()) {
                throw new Exception("Incorrect email or password.", loginTask.getException());
            }

            FirebaseUser firebaseUser = userRepository.getCurrentUser();
            if (firebaseUser == null) {
                throw new Exception("User not found after login.");
            }

            if (firebaseUser.isEmailVerified()) {
                userRepository.updateUserActivationStatus(firebaseUser.getUid(), true);
                sharedPreferences.saveUserSession(firebaseUser.getUid());
                return loginTask;
            } else {
                long creationTimestamp = firebaseUser.getMetadata().getCreationTimestamp();
                long twentyFourHoursInMs = 24 * 60 * 60 * 1000;
                boolean isExpired = (System.currentTimeMillis() - creationTimestamp) > twentyFourHoursInMs;

                if (isExpired) {
                    return userRepository.reauthenticateAndDeleteUser(email, password)
                            .continueWithTask(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    throw new Exception("Your activation link has expired. The account has been deleted, please register again.");
                                } else {
                                    throw new Exception("Error deleting the old account. Please contact support.", deleteTask.getException());
                                }
                            });
                } else {
                    userRepository.logout();
                    throw new Exception("Account not activated. Please check your email for the verification link.");
                }
            }
        });
    }
    public Task<User> getUserProfile(String uid) {
        if (uid == null || uid.isEmpty()) {
            return Tasks.forException(new Exception("No user is currently logged in."));
        }
        return userRepository.getUserProfile(uid);
    }

    public Task<Void> changePassword(String oldPassword, String newPassword, String confirmPassword) {
        String validationError = validatePasswordChange(oldPassword, newPassword, confirmPassword);
        if (validationError != null) {
            return Tasks.forException(new Exception(validationError));
        }
        return userRepository.changePassword(oldPassword, newPassword);
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
    public Task<List<User>> getAllUsers(){
        return userRepository.getAllUsers();
    }
    public void updateDailyStreak(User user){
        if(user ==null || user.getUid()==null){
            Log.e("StreakUpdate","User object is invalid");
            return;
        }
        Date lastActivity = user.getLastActivityDate();
        Date today = new Date();
        if(lastActivity==null){

            userRepository.updateStreakData(user.getUid(),1);
            return;
        }
        if(isSameDay(lastActivity,user.getRegistrationTime()) && user.getRegistrationTime()!=null){
            userRepository.updateStreakData(user.getUid(),1);
            return;
        }
        if(isSameDay(lastActivity,today)){
            return;
        }
        if(wasYesterday(lastActivity,today)){
            int newStreak = user.getActiveDaysStreak()+1;
            userRepository.updateStreakData(user.getUid(),newStreak);
        }else{
            userRepository.updateStreakData(user.getUid(),1);
        }
    }
    public boolean isSameDay(Date lastDate,Date today){
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(lastDate);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(today);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    public boolean wasYesterday(Date lastDate,Date today){
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(lastDate);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(today);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        long diff = cal2.getTimeInMillis()-cal1.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(diff)==1;
    }
    public void logoutUser() {
        userRepository.logout();
        sharedPreferences.clearUserSession();
    }
    public void addXpToUser(String uid,int xpToAdd){
        userRepository.getUserProfile(uid).addOnCompleteListener(getTask->{
           if(!getTask.isSuccessful() || getTask.getResult() == null){
               Log.e("addXpToUser","Failed to get user profile.",getTask.getException());
               return;
           }
           User user = getTask.getResult();
           user.setXp(user.getXp() + xpToAdd);
           int xpNeededForNextLevel = levelingService.getXpForLevel(user.getLevel()+1);

           while(user.getXp()>=xpNeededForNextLevel){
               user.setLevel(user.getLevel()+1);
               int ppGained = levelingService.getPowerPointsForLevel(user.getLevel());
               user.setPowerPoints(user.getPowerPoints() + ppGained);
               user.setTitle(levelingService.getTitleForLevel(user.getLevel()));
               xpNeededForNextLevel = levelingService.getXpForLevel(user.getLevel()+1);
//               updateTaskXpForUser(user.getUid(), user.getLevel());
           }
           userRepository.updateUser(user);
        });
    }
//    private void updateTaskXpForUser(String userId, int newLevel) {
//        List<com.example.e2taskly.model.Task> tasks = taskRepository.getTasksByCreator(userId);
//
//        for(com.example.e2taskly.model.Task task : tasks){
//            int baseImportanceXp = task.getImportance().getXpValue();
//            int baseDifficultyXp = task.getDifficulty().getXpValue();
//            int newImportanceXp = levelingService.calculateNextXpGain(baseImportanceXp,newLevel);
//            int newDifficultyXp = levelingService.calculateNextXpGain(baseDifficultyXp,newLevel);
//
//            // zbir oba
//            task.setValueXP(newImportanceXp + newDifficultyXp);
//
//            taskRepository.updateTask(task);
//        }
//    }
    public Task<Void> addFriend(String currentUid,String friendUid) {
    if (currentUid == null || currentUid.equals(friendUid)) {
        return Tasks.forException(new Exception("You can't add yourself as a friend."));
    }
    return userRepository.addFriend(currentUid, friendUid);
    }
    public Task<Void> removeFriend(String currentUid,String friendUid) {
        if (currentUid == null) {
            return Tasks.forException(new Exception("User is not logged in."));
        }
        return userRepository.removeFriend(currentUid, friendUid);
    }
    public Task<List<User>> getFriendsForCurrentUser(String currentUid) {
        if (currentUid == null) {
            return Tasks.forException(new Exception("User is not logged in."));
        }
        return userRepository.getUserProfile(currentUid).continueWithTask(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                throw task.getException();
            }
            User currentUser = task.getResult();
            List<String> friendIds = currentUser.getFriendIds();

            if (friendIds == null || friendIds.isEmpty()) {
                return Tasks.forResult(new ArrayList<>());
            }

            return userRepository.getFriends(friendIds);
        });
    }
    public Task<List<User>> searchUsers(String query) {
        return userRepository.searchUsersByUsername(query);
    }
    public Task<List<User>> getUsersByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        return userRepository.getUsersByIds(userIds);
    }
    public Task<Void> updateUserFcmToken(String uid, String token) {
        return userRepository.updateUserFcmToken(uid, token);
    }
    public Task<User> getUserLocallyFirst(String uid){
        if (uid == null || uid.isEmpty()) {
            return Tasks.forException(new Exception("No user is currently logged in."));
        }
        return userRepository.getUserLocallyFirst(uid);
    }

    public int getUserLevel(){
        String userUid = "";
        FirebaseUser firebaseUser = userRepository.getCurrentUser();
        if(firebaseUser != null){
            userUid = firebaseUser.getUid();
        }

        if(!userUid.isEmpty()) {
           Task<User> user = userRepository.getUserProfile(userUid);
           return user.getResult().getLevel();
        }
        return -1;
    }
}
