package com.appengine.unibank.servlets;

import com.appengine.unibank.methods.FirebaseMethods;
import com.appengine.unibank.methods.OauthMethods;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by nairs on 23-10-2016.
 * Handle the callback from openbankapi
 */
public class OauthBank extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FirebaseDatabase firebasedatabase = FirebaseDatabase.getInstance(FirebaseMethods.getApp());
        DatabaseReference ref = firebasedatabase.getReference("/users/" + req.getSession().getAttribute("uid").toString() + "/accounts/" + req.getSession().getAttribute("newBank").toString());
        try {
            String[] token = OauthMethods.initiate("https://apisandbox.openbankproject.com/oauth/token",req.getParameter("oauth_verifier"),req.getSession().getAttribute("token").toString(), req.getSession().getAttribute("token_secret").toString());
            ref.child("validity").setValue("valid");
            ref.child("token").setValue(token[0]);
            ref.child("token_secret").setValue(token[1]);
            resp.getWriter().print("You may close this window");
        } catch (OAuthMessageSignerException | OAuthExpectationFailedException | OAuthCommunicationException e) {
            ref.child("validity").setValue("failed");
            resp.getWriter().print("An error occured");
            e.printStackTrace();
        } catch (IOException e){
            ref.child("validity").setValue("failed");
            e.printStackTrace();
        } finally {
            req.getSession().invalidate();
        }
    }
}
