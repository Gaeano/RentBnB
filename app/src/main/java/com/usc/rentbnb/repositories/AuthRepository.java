package com.usc.rentbnb.repositories;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AuthRepository {
    private final FirebaseAuth auth;

    public AuthRepository(){
        auth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser(){
        return auth.getCurrentUser();
    }

    public Task<AuthResult> login (String email, String password){
        return auth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> signUp(String email, String password){
        return auth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> loginWithGoogle(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            return  auth.signInWithCredential(credential);
    }

    public Task<Void> reloadUser(){
        if (auth.getCurrentUser() != null){
            return  getCurrentUser().reload();
        }
        return null;
    }

    public Task<Void> updateProfile(String fullName){
        FirebaseUser user = auth.getCurrentUser();
        if (user != null){
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build();
            return user.updateProfile(profileUpdates);
        }
        return null;
    }

    public Task<Void> changePassword(String currentPassword, String newPassword){
        FirebaseUser user = auth.getCurrentUser();

        if (user != null && user.getEmail() != null){
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

            return user.reauthenticate(credential)
                    .continueWithTask(task ->{
                        if (task.isSuccessful()){
                            return user.updatePassword(newPassword);
                        } else {
                            throw task.getException();
                        }
                    });
        }
        return Tasks.forException(new Exception("User session invalid!"));
    }

    public void logout(){
        auth.signOut();
    }

}
