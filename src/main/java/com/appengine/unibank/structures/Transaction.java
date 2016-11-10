package com.appengine.unibank.structures;

import org.json.JSONObject;

/**
 * Created by nairs on 04-11-2016.
 * Transaction details for an account
 */
public class Transaction {
    private JSONObject transaction = new JSONObject();

    public Transaction(String id, String other, double amount, String time, String bank, double balance, String description){
        transaction
                .put("id",id)
                .put("other",other)
                .put("amount",amount)
                .put("time",time)
                .put("bank",bank)
                .put("balance",balance)
                .put("description",description);
    }

    public void setId(String id) {
        transaction.put("id",id);
    }

    public void setOther(String other) {
        transaction.put("other",other);
    }

    public void setAmount(double amount) {
        transaction.put("amount",amount);
    }

    public void setTime(String time) {
        transaction.put("time",time);
    }

    public void setBank(String bank) {
        transaction.put("bank",bank);
    }

    public void setBalance(double balance) {
        transaction.put("balance",balance);
    }

    public void setDescription(String description){
        transaction.put("description",description);
    }

    public JSONObject getTransaction(){
        return this.transaction;
    }
}
