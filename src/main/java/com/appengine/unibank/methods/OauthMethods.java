package com.appengine.unibank.methods;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.http.HttpParameters;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by nairs on 22-10-2016.
 * Methods required for oauth with openbankapi sandbox
 */
public class OauthMethods {
    public static String[] initiate(String callback)throws IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
        String CONS_KEY = OauthProperties.getInstance().getCONS_KEY();
        String CONS_SECRET = OauthProperties.getInstance().getCONS_SECRET();

        //Create an HttpURLConnection and add some headers
        URL url = new URL(OauthProperties.getInstance().getOAUTH_LINK() + "/initiate");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);

        //Sign the request
        OAuthConsumer dealabsConsumer = new DefaultOAuthConsumer(CONS_KEY,CONS_SECRET);
        HttpParameters doubleEncodedParams =  new HttpParameters();
        doubleEncodedParams.put("oauth_callback",callback);
        dealabsConsumer.setAdditionalParameters(doubleEncodedParams);
        dealabsConsumer.sign(urlConnection);

        String response = null;
        //Send the request and read the output
        try {
            //System.out.println("Response initiate1 : " + urlConnection.getResponseCode() + " " + urlConnection.getResponseMessage());
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            response = new Scanner(in,"UTF-8").useDelimiter("\\A").next();
        }
        finally {
            urlConnection.disconnect();
        }

        String[] respArray = response.split("&");
        return new String[]{respArray[0].split("=")[1],respArray[1].split("=")[1]};
    }

    public static String[] initiate(String endpointUrl, String oauthVerifier, String token, String tokenSecret) throws IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
        String CONS_KEY = OauthProperties.getInstance().getCONS_KEY();
        String CONS_SECRET = OauthProperties.getInstance().getCONS_SECRET();

        //Create an HttpURLConnection and add some headers
        URL url = new URL(endpointUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);

        //Sign the request
        OAuthConsumer dealabsConsumer = new DefaultOAuthConsumer(CONS_KEY,CONS_SECRET);
        dealabsConsumer.setTokenWithSecret(token,tokenSecret);
        HttpParameters doubleEncodedParams =  new HttpParameters();
        doubleEncodedParams.put("oauth_verifier",oauthVerifier);
        dealabsConsumer.setAdditionalParameters(doubleEncodedParams);
        dealabsConsumer.sign(urlConnection);

        String response = null;
        //Send the request and read the output
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            response = new Scanner(in,"UTF-8").useDelimiter("\\A").next();
        }
        finally {
            urlConnection.disconnect();
        }

        String[] respArray = response.split("&");
        return new String[]{respArray[0].split("=")[1],respArray[1].split("=")[1]};

    }

    public static String get(String endpointUrl, String token, String tokenSecret)throws IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
        String CONS_KEY = OauthProperties.getInstance().getCONS_KEY();
        String CONS_SECRET = OauthProperties.getInstance().getCONS_SECRET();

        //Create an HttpURLConnection and add some headers
        URL url = new URL(endpointUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoOutput(true);

        //Sign the request
        OAuthConsumer dealabsConsumer = new DefaultOAuthConsumer(CONS_KEY,CONS_SECRET);
        dealabsConsumer.setTokenWithSecret(token,tokenSecret);
        dealabsConsumer.sign(urlConnection);

        //Send the request and read the output
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new Scanner(in,"UTF-8").useDelimiter("\\A").next();
        }
        finally {
            urlConnection.disconnect();
        }
    }

    public static String post(String endpointUrl, String JSONPayload, String token, String tokenSecret)throws IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
        String CONS_KEY = OauthProperties.getInstance().getCONS_KEY();
        String CONS_SECRET = OauthProperties.getInstance().getCONS_SECRET();

        //Create an HttpURLConnection and add some headers
        URL url = new URL(endpointUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf8");
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);

        //Sign the request
        OAuthConsumer dealabsConsumer = new DefaultOAuthConsumer(CONS_KEY,CONS_SECRET);
        dealabsConsumer.setTokenWithSecret(token,tokenSecret);
        dealabsConsumer.sign(urlConnection);

        //Send the payload to the connection
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
            outputStreamWriter.write(JSONPayload);
        } finally {
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
        }

        //Send the request and read the output
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new Scanner(in,"UTF-8").useDelimiter("\\A").next();
        }
        finally {
            urlConnection.disconnect();
        }
    }
}
