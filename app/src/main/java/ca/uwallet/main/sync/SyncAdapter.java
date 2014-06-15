package ca.uwallet.main.sync;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.nodes.Document;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squareup.otto.Produce;

import ca.uwallet.main.bus.event.SyncStatusEvent;
import ca.uwallet.main.data.WatcardProvider;
import ca.uwallet.main.model.Transaction;
import ca.uwallet.main.data.WatcardContract;
import ca.uwallet.main.sync.utils.ConnectionHelper;
import ca.uwallet.main.sync.utils.ParseHelper;
import ca.uwallet.main.bus.BusProvider;
import ca.uwallet.main.util.Constants;

/**
 * Handle syncing of WatCard data.
 * @author Gabriel
 *
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter{

	private static final String TAG = "SyncAdapter";
	public static final int ADDED_BY_WATCARD = 0;
	public static final int ADDED_BY_COMPILED = 1;
	public static final int ADDED_BY_USER = 2;
    private SyncStatusEvent.Status syncStatus = SyncStatusEvent.Status.FINISHED;
    private Handler handler = new Handler(Looper.getMainLooper());

    /**
	 * Set up sync adapter.
	 * @param context
	 * @param autoInitialize
	 */
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		Log.i(TAG, "Begin network synchronization");
        setSyncStatus(SyncStatusEvent.Status.STARTED);
		// Get login details
		AccountManager accountManager = AccountManager.get(getContext());
		String username = account.name;
		String password = accountManager.getPassword(account);
		
		syncBalances(username, password, syncResult);
		syncTransactions(username, password, syncResult);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences.edit().putLong(Constants.LAST_SYNC, System.currentTimeMillis()).commit();
        setSyncStatus(SyncStatusEvent.Status.FINISHED);
	}
	
	/**
	 * Syncs the transactions from the WatCard server.
	 * @param username
	 * @param password
	 * @param syncResult
	 */
	public void syncTransactions(String username, String password, SyncResult syncResult){
		// Fetch the document
		Log.i(TAG, "Fetching transaction HTML from network");
		Document doc;
		try{
		doc = ConnectionHelper.getTransactionDocument(username, password);
		} catch(IOException e){
			Log.e(TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
		}
		
		// Check if login was successful
		if (!ParseHelper.isLoginSuccessful(doc)){
			Log.e(TAG, "Login unsucessful");
			syncResult.stats.numAuthExceptions++;
			return;
		}
		
		// Parse the data
		ArrayList<Transaction> transactions;
		try{
			transactions = ParseHelper.parseTransactions(doc);
		} catch(ParseException e){
			Log.e(TAG, "Error parsing data: " + e.toString());
            syncResult.stats.numParseExceptions++;
            return;
		}
		HashMap<Integer, String> terminalMap;
		try{
			terminalMap = ParseHelper.parseTransactionsToTerminal(doc);
		} catch(ParseException e){
			Log.e(TAG, e.toString());
			syncResult.stats.numParseExceptions++;
			return;
		}
		
		// Update db
		try{
			updateTransactionData(transactions, syncResult);
			updateTerminalData(terminalMap, ADDED_BY_WATCARD, syncResult);
		} catch (RemoteException e){
			Log.e(TAG, "Error updating transaction database: " + e.toString());
			syncResult.databaseError = true;
			return;
		} catch (OperationApplicationException e){
			Log.e(TAG, "Error updating transaction database: " + e.toString());
            syncResult.databaseError = true;
            return;
		}
	}
	
	/**
	 * Sync balances from Watcard server.
	 * @param username
	 * @param password
	 * @param syncResult
	 */
	public void syncBalances(String username, String password, SyncResult syncResult){
		// Fetch balance HTML
		Log.i(TAG, "Fetching balances HTML from network");
		Document doc;
		try{
			doc = ConnectionHelper.getBalanceDocument(username, password);
		} catch(IOException e){
			Log.e(TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
		}
		
		// Check login success
		if (!ParseHelper.isLoginSuccessful(doc)){
			Log.e(TAG, "Balance login unsucessful");
			syncResult.stats.numAuthExceptions++;
			return;
		}
		
		// Parse balances
		ArrayList<Integer> balances;
		try{
		balances = ParseHelper.parseBalances(doc);
		} catch (ParseException e){
			Log.e(TAG, e.toString());
			syncResult.stats.numParseExceptions++;
			return;
		}
		
		// Update DB
		try{
			updateBalanceData(balances, syncResult);
		} catch (RemoteException e){
			Log.e(TAG, "Error updating balance table: " + e.toString());
			syncResult.databaseError = true;
			return;
		} catch (OperationApplicationException e){
			Log.e(TAG, "Error updating balance table: " + e.toString());
            syncResult.databaseError = true;
            return;
		}
	}
	
	/**
	 * Updates the transaction database with new transactions. For now it deletes all the old data and inserts the new.
	 * @param transactions
	 * @throws OperationApplicationException 
	 * @throws RemoteException 
	 */
	public void updateTransactionData(ArrayList<Transaction> transactions, final SyncResult syncResult) throws RemoteException, OperationApplicationException{
		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		
		// Delete the old data
        Uri uri = WatcardContract.Transaction.CONTENT_URI.buildUpon()
                .appendQueryParameter(WatcardProvider.QUERY_NOTIFY, "false").build();
		Log.i(TAG, "Scheduling delete of old transactions");
		batch.add(ContentProviderOperation.newDelete(uri).build());
		syncResult.stats.numDeletes++;
		
		Log.i(TAG, "Scheduling insert of new transactions");
		// Schedule transaction inserts
		for (Transaction t : transactions){
			batch.add(ContentProviderOperation.newInsert(uri)
					.withValue(WatcardContract.Transaction.COLUMN_NAME_AMOUNT, t.getAmount())
					.withValue(WatcardContract.Transaction.COLUMN_NAME_DATE, t.getDate())
					.withValue(WatcardContract.Transaction.COLUMN_NAME_MONEY_TYPE, t.getType())
					.withValue(WatcardContract.Transaction.COLUMN_NAME_TERMINAL, t.getTerminal())
					.build());
			syncResult.stats.numInserts++;
		}
		
		Log.i(TAG, "Applying batch update of transactions");
        ContentResolver contentResolver = getContext().getContentResolver();
		contentResolver.applyBatch(WatcardContract.CONTENT_AUTHORITY, batch);
	}
	
	/**
	 * Updates the balance data with new balances. Deletes the old balances and inserts new data.
	 * @param balances
	 * @param syncResult
	 * @throws RemoteException
	 * @throws OperationApplicationException
	 */
	public void updateBalanceData(ArrayList<Integer> balances, final SyncResult syncResult) throws RemoteException, OperationApplicationException{
		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		
		// Delete the old data
        Uri uri = WatcardContract.Balance.CONTENT_URI.buildUpon()
                .appendQueryParameter(WatcardProvider.QUERY_NOTIFY, "false").build();
		Log.i(TAG, "Scheduling delete of old balances");
		batch.add(ContentProviderOperation.newDelete(uri).build());
		syncResult.stats.numDeletes++;
		
		Log.i(TAG, "Scheduling insert of new balances");
		for (Integer amount : balances){
			batch.add(ContentProviderOperation.newInsert(uri)
					.withValue(WatcardContract.Balance.COLUMN_NAME_AMOUNT, amount)
					.build());
			syncResult.stats.numInserts++;
		}
		
		Log.i(TAG, "Applying batch update of balances");
        ContentResolver contentResolver = getContext().getContentResolver();
		contentResolver.applyBatch(WatcardContract.CONTENT_AUTHORITY, batch);
		contentResolver.notifyChange(WatcardContract.Balance.CONTENT_URI, null, false);
	}
	
	public void updateTerminalData(HashMap<Integer, String> map, int priority, final SyncResult syncResult) throws RemoteException, OperationApplicationException{
		//TODO deal with category
		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		
		Log.i(TAG, "Fetching local terminal entries for merge");
		Uri uri = WatcardContract.Terminal.CONTENT_URI.buildUpon()
                .appendQueryParameter(WatcardProvider.QUERY_NOTIFY, "false").build();
		Cursor c = getContext().getContentResolver().query(uri, null, null, null, null);
		assert c != null;
		int idColumn = c.getColumnIndex(WatcardContract.Terminal._ID);
		int textPriorityColumn = c.getColumnIndex(WatcardContract.Terminal.COLUMN_NAME_TEXT_PRIORITY);
		Log.i(TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");
		while(c.moveToNext()){
			int id = c.getInt(idColumn);
			int oldPriority = c.getInt(textPriorityColumn);
			// Skip if we would be overriding an entry with higher priority
			if (priority < oldPriority){
				map.remove(id);
				continue;
			}
			String match = map.get(id);
			// There's an entry that needs updating
			if (match != null){
				Uri existingUri = uri.buildUpon()
                        .appendPath(Integer.toString(id)).build();
				batch.add(ContentProviderOperation.newUpdate(existingUri)
						.withValue(WatcardContract.Terminal.COLUMN_NAME_TEXT, map.get(id))
						.withValue(WatcardContract.Terminal.COLUMN_NAME_TEXT_PRIORITY, priority)
						.build());
				syncResult.stats.numUpdates++;
				map.remove(id);
			}
		}
		c.close();
		
		for (int id : map.keySet()){
			batch.add(ContentProviderOperation.newInsert(uri)
					.withValue(WatcardContract.Terminal._ID, id)
					.withValue(WatcardContract.Terminal.COLUMN_NAME_TEXT, map.get(id))
					.withValue(WatcardContract.Terminal.COLUMN_NAME_TEXT_PRIORITY, priority)
					.build());
			syncResult.stats.numUpdates++;
		}
		
		Log.i(TAG, "Applying batch update of terminals");
        ContentResolver contentResolver = getContext().getContentResolver();
		contentResolver.applyBatch(WatcardContract.CONTENT_AUTHORITY, batch);
        contentResolver.notifyChange(WatcardContract.Transaction.CONTENT_URI, null, false);
        contentResolver.notifyChange(WatcardContract.Terminal.CONTENT_URI, null, false);
	}

    @Produce public SyncStatusEvent produceSyncStatus() {
        return new SyncStatusEvent(syncStatus);
    }

    private void setSyncStatus(SyncStatusEvent.Status syncStatus) {
        this.syncStatus = syncStatus;
        handler.post(new Runnable() {
            @Override
            public void run() {
                BusProvider.getInstance().post(produceSyncStatus());
            }
        });
    }
}
