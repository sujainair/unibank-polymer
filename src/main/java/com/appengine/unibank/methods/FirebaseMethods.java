package com.appengine.unibank.methods;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.internal.NonNull;
import com.google.firebase.tasks.OnFailureListener;
import com.google.firebase.tasks.OnSuccessListener;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by nairs on 06-11-2016.
 * Verify logged in user for servlets
 */
public class FirebaseMethods {
    public static String getUID(String idToken){
        final AtomicBoolean done = new AtomicBoolean(false);
        final AtomicReference uidRef = new AtomicReference<>();
        FirebaseApp app = getApp();
        FirebaseAuth auth = FirebaseAuth.getInstance(app);
        auth.verifyIdToken(idToken)
                .addOnSuccessListener(new OnSuccessListener<FirebaseToken>() {
                    @Override
                    public void onSuccess(FirebaseToken firebaseToken) {
                        uidRef.set(firebaseToken.getUid());
                        done.set(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uidRef.set("error");
                        done.set(true);
                    }
                });
        while (! done.get());
        return uidRef.toString();
    }

    public static FirebaseApp getApp(){
        FirebaseApp app;
        try{
            app = FirebaseApp.getInstance();
        }catch (IllegalStateException e){
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setServiceAccount(FirebaseMethods.class.getResourceAsStream("/firebase_service_account.json"))
                    .setDatabaseUrl("https://unibank-11d5b.firebaseio.com")
                    .build();
            app = FirebaseApp.initializeApp(options);
        }
        return app;
    }
}
