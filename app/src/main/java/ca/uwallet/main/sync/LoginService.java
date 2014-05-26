package ca.uwallet.main.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.jsoup.nodes.Document;

import java.io.IOException;

import ca.uwallet.main.data.WatcardContract;
import ca.uwallet.main.sync.accounts.Authenticator;
import ca.uwallet.main.sync.utils.ConnectionHelper;
import ca.uwallet.main.sync.utils.ParseHelper;
import ca.uwallet.main.util.CommonUtils;

/**
 * Created by gabriel on 5/23/14.
 */
public class LoginService extends IntentService{

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String RESULT = "result";
    public static final String KEY_ACCOUNT = "key_account";
    private static final String TAG = "LoginService";
    public static final IntentFilter FILTER = new IntentFilter(TAG);

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
            Intent out = new Intent();
            out.putExtra(RESULT, Result.CONNECTION_ERROR);
            broadcast(out);
            return;
        }

        boolean loginSuccessful = ParseHelper.isLoginSuccessful(doc);
        if (!loginSuccessful) {
            Intent out = new Intent();
            out.putExtra(RESULT, Result.INVALID_CREDENTIALS);
            broadcast(out);
            return;
        }

        // Add the account to the AccountManager
        Account account = new Account(username, Authenticator.ACCOUNT_TYPE);
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
        CommonUtils.requestSync(account);
        Intent out = new Intent();
        out.putExtra(RESULT, Result.SUCCESS);
        out.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);
        out.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Authenticator.ACCOUNT_TYPE);
        out.putExtra(AccountManager.KEY_PASSWORD, password);
        broadcast(out);
    }

    private void broadcast(Intent intent) {
        intent.setAction(TAG);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
