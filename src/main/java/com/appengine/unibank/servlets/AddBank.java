package com.appengine.unibank.servlets;

import com.appengine.unibank.methods.FirebaseMethods;
import com.appengine.unibank.methods.OauthMethods;
import com.appengine.unibank.methods.OauthProperties;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by nairs on 22-10-2016.
 * Handles the oauth to add a new bank for the user
 */
public class AddBank extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uid = FirebaseMethods.getUID(req.getParameter("idToken"));
        if(uid.contentEquals("error")){
            resp.getWriter().println("{ \"error\" : \"could not verify user\"");
            return;
        }
        req.getSession().setAttribute("uid",uid);
        try {
            String[] token = OauthMethods.initiate(OauthProperties.getInstance().getCALLBACK_LINK());
            req.getSession().setAttribute("token",token[0]);
            req.getSession().setAttribute("token_secret",token[1]);
            resp.sendRedirect(OauthProperties.getInstance().getOAUTH_LINK() + "/authorize?oauth_token=" + token[0]);
        } catch (OAuthMessageSignerException | OAuthExpectationFailedException | OAuthCommunicationException e) {
            resp.getWriter().print("An error occured");
            e.printStackTrace();
            req.getSession().invalidate();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getSession().setAttribute("newBank",req.getParameter("bank"));
        resp.getWriter().print("{ \"value\" : \"success\" }");
    }
}
