package com.appengine.unibank.structures;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by nairs on 04-11-2016.
 * Account details and its transactions
 */
public class Account {
    private JSONObject account = new JSONObject();

    public Account(){}

    public Account(String name,String id,double balance){
        account
                .put("name",name)
                .put("id",id)
                .put("balance",balance);
    }

    public void setName(String name){
        account.put("name",name);
    }

    public void setId(String id){
        account.put("id",id);
    }

    public void setTransactions(JSONArray transactions){
        account.put("transactions",transactions);
    }

    public void addTransaction(JSONObject transaction){
        JSONArray transactions = (account.getJSONArray("transactions") != null) ? account.getJSONArray("transactions") : new JSONArray();
        transactions.put(transaction);
        account.put("transactions", transactions);
    }

    public void setError(String error){
        account.put("error",error);
    }

    private void setBalance(double balance){
        account.put("balance",balance);
    }

    public JSONObject getAccount(){
        return this.account;
    }
}
