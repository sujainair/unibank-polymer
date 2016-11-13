package com.appengine.unibank.methods;

import com.appengine.unibank.structures.Account;
import com.appengine.unibank.structures.Bank;
import com.appengine.unibank.structures.Transaction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by nairs on 03-11-2016.
 * Creates the JSON result for GetBanks
 */
public class GetBanksJSONGenerator {
    private static final JSONObject JSON_ERROR = new JSONObject().put("error","error"); //General error message

    /**
     * All the user accounts and transactions for those accounts
     * @param ref Database ref pointing to /users/uid/accounts
     * @return JSONArray of details
     */
    public static JSONArray getAccounts(DatabaseReference ref){
        //Get a list of all the banks the user has added
        JSONObject banksObj = getBankList(ref);
        if(banksObj.equals(JSON_ERROR)) return new JSONArray().put(JSON_ERROR);

        //Get details for the users banks
        JSONArray banksDetails = new JSONArray();
        Iterator<String> banks = banksObj.keys();
        while (banks.hasNext()){
            String nextBank = banks.next(); //Get the key value i.e bank ID

            //Create a new bank
            Bank bank = new Bank();
            bank.setID(nextBank);
            //Bank contains JSON with some details added by user
            JSONObject accountsObj = banksObj.getJSONObject(nextBank);
            //Check if this bank is authenticated
            if(! accountsObj.getString("validity").contentEquals("valid")) {
                bank.setError("Bank is not authenticated!"); //Notify this bank is not authenticated by sending error
                banksDetails.put(bank.getBank());
                continue;
            }
            accountsObj.remove("validity");

            String token = accountsObj.getString("token");
            String tokenSecret = accountsObj.getString("token_secret");
            JSONObject bankDetails = new JSONObject().put("error","Error connecting to the bank!"); //Default error
            try {
                //Get bank name from this ID
                String getBankDetailsUrl = OauthProperties.getInstance().getAPI_LINK() + "/banks/" + nextBank;
                bankDetails = new JSONObject(OauthMethods.get(getBankDetailsUrl,token,tokenSecret));
                bank.setName(bankDetails.getString("full_name"));
                bank.setAccounts(getAccountsDetails(accountsObj,nextBank));
            } catch (OAuthExpectationFailedException | OAuthCommunicationException | IOException | OAuthMessageSignerException e) {
                bank.setError("Error connecting to the bank!");
            } catch (JSONException e){
                try {
                    bank.setError(bankDetails.getString("error")); //In case there is an error returned by api
                } catch (JSONException e2){
                    //Do nothing
                }
            }
            banksDetails.put(bank.getBank());
        }
        return banksDetails;
    }

    /**
     *
     * @param accountsObj JSON under /users/uid/accounts/bank/
     * @param bankID The bank to which this JSON belongs
     * @return JSONArray of all accounts and transactions for the user
     */
    private static JSONArray getAccountsDetails(JSONObject accountsObj,String bankID) throws IOException {
        JSONArray accountDetails = new JSONArray(); //Value to return

        //Get token and token secret for
        String token = accountsObj.getString("token");
        String tokenSecret = accountsObj.getString("token_secret");
        accountsObj.remove("token");
        accountsObj.remove("token_secret");
        //All remaining key value pairs are accounts in the bank
        Iterator<String> accounts = accountsObj.keys();
        //Get transaction info for each of these accounts
        while (accounts.hasNext()){
            String nextAcc = accountsObj.getString(accounts.next()); //Get value at key i.e. account ID

            String getAccDetailsUrl = OauthProperties.getInstance().getAPI_LINK() + "/banks/" + bankID + "/accounts/" + nextAcc + "/owner/account";
            Account account = new Account();
            account.setName(nextAcc); //Set default
            //Create id for html element
            String accId = nextAcc + "_" + bankID; //accID_bankID
            account.setId(accId);
            account.setError("Could not retrieve details"); //Default error

            try {
                JSONObject accDetails = new JSONObject(OauthMethods.get(getAccDetailsUrl, token, tokenSecret));
                //Create an account with this name
                account = new Account(accDetails.getString("id"), accId, accDetails.getJSONObject("balance").getDouble("amount"));
                //Get transactions for this account from apisandbox request
                String getUrl = OauthProperties.getInstance().getAPI_LINK() + "/banks/" + bankID + "/accounts/" + nextAcc + "/owner/transactions";
                JSONObject transactions = new JSONObject(OauthMethods.get(getUrl, token, tokenSecret));
                account.setTransactions(getTransactionsDetails(transactions));
            } catch (IOException | OAuthMessageSignerException | OAuthExpectationFailedException | OAuthCommunicationException e) {
                account.setError("Could not retrieve details");
            } catch (Exception e){
                //ignore everything else. Move on to next account after adding the generic error below
            }
            accountDetails.put(account.getAccount());
        }
        return accountDetails;
    }

    /**
     *
     * @param transactionsObj JSON as returned by apisandbox
     * @return JSONArray of transactions in the account as needed by this app
     */
    private static JSONArray getTransactionsDetails(JSONObject transactionsObj){
        JSONArray transactionsDetails = new JSONArray(); //Value to return
        JSONArray transactions = transactionsObj.getJSONArray("transactions");
        for(int i=0; i<transactions.length(); i++){
            Transaction transaction = new Transaction(transactions.getJSONObject(i).getString("id"),
                    transactions.getJSONObject(i).getJSONObject("other_account").getJSONObject("holder").getString("name"),
                    transactions.getJSONObject(i).getJSONObject("details").getJSONObject("value").getDouble("amount"),
                    transactions.getJSONObject(i).getJSONObject("details").getString("completed"),
                    transactions.getJSONObject(i).getJSONObject("other_account").getJSONObject("bank").getString("name"),
                    transactions.getJSONObject(i).getJSONObject("details").getJSONObject("new_balance").getDouble("amount"),
                    transactions.getJSONObject(i).getJSONObject("details").get("description").toString());
            transactionsDetails.put(transaction.getTransaction());
        }
        return transactionsDetails;
    }

    /**
     *
     * @param ref Firebase ref to /users/uid/accounts
     * @return JSONObject under ref
     */
    private static JSONObject getBankList (DatabaseReference ref) {
        //Query firebase for all banks user has added
        final AtomicBoolean done = new AtomicBoolean(false);
        final AtomicReference jsonRef = new AtomicReference<>();
        ref.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                JSONObject banks = new JSONObject();
                for (DataSnapshot bank: dataSnapshot.getChildren()) {
                    JSONObject details = new JSONObject();
                    for (DataSnapshot detail : bank.getChildren()){
                        details.put(detail.getKey(),detail.getValue());
                    }
                    banks.put(bank.getKey(),details);
                }
                jsonRef.set(banks);
                done.set(true);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                jsonRef.set(JSON_ERROR);
                done.set(true);
            }
        });
        //Wait for the bankList to be updated
        while(! done.get());
        return new JSONObject(jsonRef.get().toString());
    }
}
