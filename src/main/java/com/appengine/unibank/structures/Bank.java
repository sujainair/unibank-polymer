package com.appengine.unibank.structures;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by nairs on 05-11-2016.
 * Details of a bank and its accounts
 */
public class Bank {
    private JSONObject bank = new JSONObject();

    public Bank(){};

    public Bank(String id,String name, JSONArray accounts){
        bank
                .put("id",id)
                .put("name",name)
                .put("accounts",accounts);
        setBalance(accounts);
    }

    public void setError(String error){
        bank.put("error",error);
    }
    public void setName(String name){
        bank.put("name",name);
    }

    public void setAccounts(JSONArray accounts){
        bank.put("accounts",accounts);
        setBalance(accounts);
    }

    public void addAccount(JSONObject account){
        JSONArray accounts = (bank.getJSONArray("accounts") != null) ? bank.getJSONArray("accounts") : new JSONArray();
        accounts.put(account);
        bank.put("accounts",accounts);
        addBalance(account.getInt("balance"));
    }

    public void setID(String id){
        bank.put("id",id);
    }

    private void setBalance(JSONArray accounts){
        double balance = 0;
        for(int i=0; i<accounts.length(); i++){
            balance += accounts.getJSONObject(i).getDouble("balance");
        }
        bank.put("balance",balance);
    }

    private void addBalance(double amount){
        bank.put("balance",bank.getDouble("balance")+amount);
    }

    public JSONObject getBank(){
        return this.bank;
    }
}
