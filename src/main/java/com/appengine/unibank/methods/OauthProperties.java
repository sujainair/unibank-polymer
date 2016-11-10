package com.appengine.unibank.methods;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by nairs on 25-10-2016.
 * Singleton class to load properties for Oauth with openbankapi sandbox
 */
public class OauthProperties {
    private static OauthProperties instance = null;
    public String CONS_KEY, CONS_SECRET, API_LINK;

    private OauthProperties() throws IOException{
        Properties prop = new Properties();
        InputStream propFile = null;
        try{
            propFile = this.getClass().getResourceAsStream("../../../../dev.properties");
            prop.load(propFile);
        } catch (FileNotFoundException e){
            System.err.println("[ERROR] Properties file missing!");
            throw e;
        } catch (IOException e) {
            System.err.println("[ERROR] Unable to read properties file");
            throw e;
        }
        finally {
            if (propFile != null) {
                propFile.close();
            }
        }
        CONS_KEY = prop.getProperty("CONS_KEY");
        CONS_SECRET = prop.getProperty("CONS_SECRET");
        API_LINK = prop.getProperty("API_LINK");
    }

    public static OauthProperties getInstance() throws IOException {
        if(instance == null){
            instance = new OauthProperties();
        }
        return instance;
    }
}
