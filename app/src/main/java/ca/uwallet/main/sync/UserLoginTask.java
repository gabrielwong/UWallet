package ca.uwallet.main.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.nodes.Document;

import java.io.IOException;

import ca.uwallet.main.LoginActivity;
import ca.uwallet.main.R;
import ca.uwallet.main.sync.utils.ConnectionHelper;
import ca.uwallet.main.sync.utils.ParseHelper;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class UserLoginTask extends AsyncTask<Void, Void, Result> {

    private LoginActivity loginActivity;
    private Account account;
    private static final String TAG = "UserLoginTask";

    public UserLoginTask(LoginActivity loginActivity) {
        this.loginActivity = loginActivity;
    }

    @Override
    protected Result doInBackground(Void... params) {
        // Authenticate the login by fetching the balance document
        Document doc;
        try {
            doc = ConnectionHelper.getBalanceDocument(loginActivity.mUsername, loginActivity.mPassword);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return Result.CONNECTION_ERROR;
        }

        boolean loginSuccessful = ParseHelper.isLoginSuccessful(doc);
        if (!loginSuccessful)
            return Result.INVALID_CREDENTIALS;

        // Add the account to the AccountManager
        account = new Account(loginActivity.mUsername, LoginActivity.ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) loginActivity.getSystemService(Context.ACCOUNT_SERVICE);
        boolean addSuccess = accountManager.addAccountExplicitly(account, loginActivity.mPassword, null);
        if (addSuccess){
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, LoginActivity.CONTENT_AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, LoginActivity.CONTENT_AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            //ContentResolver.addPeriodicSync(
            //        account, CONTENT_AUTHORITY, new Bundle(),SYNC_FREQUENCY);
        } else{
            // Change password if we could not add the account (error or more likely already added)
            accountManager.setPassword(account, loginActivity.mPassword);
        }

        return Result.SUCCESS;
    }

    @Override
    protected void onPostExecute(final Result result) {
        loginActivity.mAuthTask = null;
        loginActivity.showProgress(false);

        switch(result){
        case SUCCESS:
            // Send back results to AccountManager
            final Intent authenticatorIntent = new Intent();
            authenticatorIntent.putExtra(AccountManager.KEY_ACCOUNT_NAME, loginActivity.mUsername);
            authenticatorIntent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, LoginActivity.ACCOUNT_TYPE);
            authenticatorIntent.putExtra(AccountManager.KEY_PASSWORD, loginActivity.mPassword);
            loginActivity.setAccountAuthenticatorResult(authenticatorIntent.getExtras());
            final Intent resultIntent = new Intent();
            resultIntent.putExtra(LoginActivity.KEY_ACCOUNT, account);
            loginActivity.setResult(Activity.RESULT_OK, resultIntent);
            loginActivity.finish();
            break;
        case INVALID_CREDENTIALS:
            // Indicate incorrect password and prompt
            loginActivity.mPasswordView.setError(loginActivity.getString(R.string.error_incorrect_credentials));
            loginActivity.mPasswordView.requestFocus();
            break;
        case CONNECTION_ERROR:
            // Inform that there is a connection error
            loginActivity.showToast(loginActivity.getString(R.string.error_connection_failed), Toast.LENGTH_LONG);
            break;
        }
    }

    @Override
    protected void onCancelled() {
        loginActivity.mAuthTask = null;
        loginActivity.showProgress(false);
    }

    public enum Result {
        SUCCESS, INVALID_CREDENTIALS, CONNECTION_ERROR
    }
}
