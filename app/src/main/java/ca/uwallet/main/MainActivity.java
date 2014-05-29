package ca.uwallet.main;



import ca.uwallet.main.sync.accounts.Authenticator;
import ca.uwallet.main.util.CommonUtils;
import ca.uwallet.main.util.Constants;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

/**
 * Activity that is launched from the launcher. We switch between screens using fragments.
 * Does not handle login.
 *
 import android.app.FragmentManager
 * @author Gabriel
 *
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

	private static final String TAG = "MainActivity";
	private static final int RC_LOGIN = 17; // Response code for LoginActivity
    private static final long DURATION_BETWEEN_AUTO_SYNC = 1000L * 60L * 30L; // 30 minutes;

    private ViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);

		// Login if no account registered
		if (!isLoggedIn()){
			doLogin();
		} else {
            performSyncIfStale();
        }

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        FragmentPagerAdapter fragmentPagerAdapter =
                new MainPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(fragmentPagerAdapter);

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        for (int i = 0; i < fragmentPagerAdapter.getCount(); i++) {
            ActionBar.Tab tab = actionBar.newTab()
                    .setText(fragmentPagerAdapter.getPageTitle(i))
                    .setTabListener(this);
            actionBar.addTab(tab);
        }
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getActionBar().setSelectedNavigationItem(position);
            }
        });
	}

	@Override
	public void onActivityResult(int requestCode, int responseCode, Intent data){
		Log.i(TAG, "onActivityResult, requestCode: " + requestCode + " responseCode: " + responseCode);
		switch(requestCode){
		// From LoginActivity
		case RC_LOGIN:
			Log.i(TAG, "Received login");
			// Close the app if the user aborted login. Can't do anything without login.
			if (responseCode != RESULT_OK){
				Log.i(TAG, "User cancelled login. Closing down.");
				finish();
			}
            performSync();
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_refresh:
                performSync();
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_logout:
                removeAllAccounts();
                CommonUtils.clearData(this);
                doLogin();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {}

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}

    private void performSync() {
        Account[] accounts = getAccountManager().getAccountsByType(Authenticator.ACCOUNT_TYPE);
        for (Account account : accounts) {
            CommonUtils.requestSync(account);
        }
    }

	/**
	 * Returns the account manager for this activity.
	 * @return
	 */
	private AccountManager getAccountManager(){
		return (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
	}
	
	/**
	 * Removes all accounts from the account manager.
	 */
	private void removeAllAccounts(){
		AccountManager accountManager = getAccountManager();
		Account[] accounts = accountManager.getAccountsByType(Authenticator.ACCOUNT_TYPE);
		for (Account account : accounts){
			accountManager.removeAccount(account, null, null);
		}
	}

    public boolean isLoggedIn() {
        return CommonUtils.getAccountCount(this) > 0;
    }
	
	/**
	 * Launches the LoginActivity.
	 */
	private void doLogin(){
		Intent intent = new Intent(this, LoginActivity.class);
		Log.v(TAG, "Starting login activity");
		startActivityForResult(intent, RC_LOGIN);
	}

    private void performSyncIfStale() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long lastSync = preferences.getLong(Constants.LAST_SYNC, 0L);
        if (lastSync + DURATION_BETWEEN_AUTO_SYNC < System.currentTimeMillis()){
            performSync();
        }
    }

    private enum MainFragments {
        BALANCE, TRANSACTION;
    }

    private class MainPagerAdapter extends FragmentPagerAdapter{

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (MainFragments.values()[position]) {
                case BALANCE:
                    return new BalanceFragment();
                case TRANSACTION:
                    return new TransactionFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return MainFragments.values().length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (MainFragments.values()[position]) {
                case BALANCE:
                    return getText(R.string.fragment_balance_title);
                case TRANSACTION:
                    return getText(R.string.fragment_transaction_title);
                default:
                    return null;
            }
        }
    }
}
