package ca.uwallet.main.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.jsoup.nodes.Document;

import java.io.IOException;

import ca.uwallet.main.LoginActivity;
import ca.uwallet.main.data.WatcardContract;
import ca.uwallet.main.sync.utils.ConnectionHelper;
import ca.uwallet.main.sync.utils.ParseHelper;

/**
 * Created by gabriel on 5/23/14.
 */
public class LoginService extends IntentService{

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    private static final String TAG = "LoginService";

    public LoginService() {
        super(TAG);
    }

    public enum Result {
        SUCCESS, INVALID_CREDENTIALS, CONNECTION_ERROR
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String username = intent.getStringExtra(USERNAME);
        String password = intent.getStringExtra(PASSWORD);


        // Authenticate the login by fetching the balance document
        Document doc;
        try {
            doc = ConnectionHelper.getBalanceDocument(username, password);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            // TODO indicate error
            return;
        }

        boolean loginSuccessful = ParseHelper.isLoginSuccessful(doc);
        if (!loginSuccessful) {
            // TODO indicate invalid credentials
            return;
        }

        // Add the account to the AccountManager
        Account account = new Account(username, LoginActivity.ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
        boolean addSuccess = accountManager.addAccountExplicitly(account, password, null);
        if (addSuccess){
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, WatcardContract.CONTENT_AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, WatcardContract.CONTENT_AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            //ContentResolver.addPeriodicSync(
            //        account, CONTENT_AUTHORITY, new Bundle(),SYNC_FREQUENCY);
        } else{
            // Change password if we could not add the account (error or more likely already added)
            accountManager.setPassword(account, password);
        }
        // TODO indicate success
    }
}
