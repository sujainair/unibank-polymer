package com.appengine.unibank.servlets;

import com.appengine.unibank.methods.FirebaseMethods;
import com.appengine.unibank.methods.OauthMethods;
import com.appengine.unibank.methods.OauthProperties;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by nairs on 06-11-2016.
 * Create a transfer request
 */
public class MakeTransfer extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject body = new JSONObject(req.getReader().readLine());
        JSONArray params = body.getJSONArray("params");
        JSONObject actionParams = params.getJSONObject(1);
        String uid = FirebaseMethods.getUID(params.getJSONObject(0).getString("idToken"));
        if(uid.contentEquals("error")){
            resp.getWriter().println("{ \"error\" : \"could not verify user\"");
            return;
        }
        JSONObject jsonPayload = new JSONObject()
                .put("to",new JSONObject()
                        .put("bank_id",actionParams.getString("to_bank"))
                        .put("account_id",actionParams.getString("to_acc")))
                .put("value", new JSONObject()
                                .put("currency","EUR")
                                .put("amount",actionParams.getString("amount")))
                .put("description",actionParams.getString("description"));
        String transferURL = OauthProperties.getInstance().API_LINK + "/banks/" + actionParams.getString("from_bank") + "/accounts/" + actionParams.getString("from_acc") + "/owner/transaction-request-types/SANDBOX_TAN/transaction-requests";
        FirebaseDatabase firebasedatabase = FirebaseDatabase.getInstance(FirebaseMethods.getApp());
        DatabaseReference ref = firebasedatabase.getReference("/users/" + uid + "/accounts/" + actionParams.getString("from_bank"));
        JSONObject tokenInfo = getTokenInfo(ref);
        JSONObject response = new JSONObject().put("error","error");
        try {
            response = new JSONObject(OauthMethods.post(transferURL,jsonPayload.toString(),tokenInfo.getString("token"),tokenInfo.getString("token_secret")));
        } catch (OAuthMessageSignerException | OAuthExpectationFailedException | OAuthCommunicationException e) {
            e.printStackTrace();
        }
        resp.getWriter().println(response.toString());
    }

    private JSONObject getTokenInfo(DatabaseReference ref){
        final AtomicBoolean done = new AtomicBoolean(false);
        final AtomicReference dataRef = new AtomicReference();
        ref.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                JSONObject jsonObject = new JSONObject();
                for (DataSnapshot each: dataSnapshot.getChildren()) {
                    if(each.getKey().toString().contentEquals("token")||each.getKey().toString().contentEquals("token_secret")) {
                        jsonObject.put(each.getKey(), each.getValue());
                    }
                }
                dataRef.set(jsonObject);
                done.set(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                JSONObject error = new JSONObject().put("error","error");
                dataRef.set(error);
                done.set(true);
            }
        });
        while (! done.get());
        return new JSONObject(dataRef.get().toString());
    }
}
