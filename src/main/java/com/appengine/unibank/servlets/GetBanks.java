package com.appengine.unibank.servlets;

import com.appengine.unibank.methods.FirebaseMethods;
import com.appengine.unibank.methods.GetBanksJSONGenerator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by nairs on 01-11-2016.
 * Get details of users connected accounts.
 */
public class GetBanks extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uid = FirebaseMethods.getUID(req.getParameter("idToken"));
        if(uid.contentEquals("error")){
            resp.getWriter().println("{ \"error\" : \"could not verify user\"");
            return;
        }
        FirebaseDatabase firebasedatabase = FirebaseDatabase.getInstance(FirebaseMethods.getApp());
        DatabaseReference ref = firebasedatabase.getReference("/users/" + uid + "/accounts/");
        resp.getWriter().println(GetBanksJSONGenerator.getAccounts(ref).toString());
    }
}
