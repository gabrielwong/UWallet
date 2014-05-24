package ca.uwallet.main.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import ca.uwallet.main.util.SelectionBuilder;

public class WatcardProvider extends ContentProvider{

	private WatcardDatabaseHelper databaseHelper;
	
	private static final String JOINED_TRANSACTION_TABLE =
			WatcardContract.Transaction.TABLE_NAME + " LEFT OUTER JOIN " + WatcardContract.Terminal.TABLE_NAME + 
			" ON " + WatcardContract.Transaction.TABLE_NAME + "" + WatcardContract.Transaction.COLUMN_NAME_TERMINAL +
			" = " + WatcardContract.Terminal.TABLE_NAME + "" + WatcardContract.Terminal._ID; // TODO join category
	
	// Create the UriMatcher
	private static final String AUTHORITY = WatcardContract.CONTENT_AUTHORITY;
	private static final UriMatcher sUriMatcher;
	private static final int ROUTE_TRANSACTION = 1,
							 ROUTE_TRANSACTION_ID = 2,
							 ROUTE_BALANCE = 3,
							 ROUTE_BALANCE_ID = 4,
							 ROUTE_TERMINAL = 5,
							 ROUTE_TERMINAL_ID = 6,
							 ROUTE_CATEGORY = 7,
							 ROUTE_CATEGORY_ID = 8;
	static{
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, WatcardContract.PATH_TRANSACTION , ROUTE_TRANSACTION);
		sUriMatcher.addURI(AUTHORITY, WatcardContract.PATH_TRANSACTION + "/#", ROUTE_TRANSACTION_ID);
		sUriMatcher.addURI(AUTHORITY, WatcardContract.PATH_BALANCE, ROUTE_BALANCE);
		sUriMatcher.addURI(AUTHORITY, WatcardContract.PATH_BALANCE + "/#", ROUTE_BALANCE_ID);
		sUriMatcher.addURI(AUTHORITY, WatcardContract.PATH_TERMINAL , ROUTE_TERMINAL);
		sUriMatcher.addURI(AUTHORITY, WatcardContract.PATH_TERMINAL + "/#", ROUTE_TERMINAL_ID);
		sUriMatcher.addURI(AUTHORITY, WatcardContract.PATH_CATEGORY, ROUTE_CATEGORY);
		sUriMatcher.addURI(AUTHORITY, WatcardContract.PATH_CATEGORY + "/#", ROUTE_CATEGORY_ID);
	}
	
	@Override
	public boolean onCreate() {
		// Create database helper
		databaseHelper = new WatcardDatabaseHelper(getContext());
		return true;
	}
	
	@Override
	/**
     * Determine the mime type for entries returned by a given URI.
     */
	public String getType(Uri uri) {
		switch(sUriMatcher.match(uri)){
		case ROUTE_TRANSACTION:
			return WatcardContract.Transaction.CONTENT_TYPE;
		case ROUTE_TRANSACTION_ID:
			return WatcardContract.Transaction.CONTENT_ITEM_TYPE;
		case ROUTE_BALANCE:
			return WatcardContract.Balance.CONTENT_TYPE;
		case ROUTE_BALANCE_ID:
			return WatcardContract.Transaction.CONTENT_ITEM_TYPE;
		case ROUTE_TERMINAL:
			return WatcardContract.Terminal.CONTENT_TYPE;
		case ROUTE_TERMINAL_ID:
			return WatcardContract.Terminal.CONTENT_ITEM_TYPE;
		case ROUTE_CATEGORY:
			return WatcardContract.Category.CONTENT_TYPE;
		case ROUTE_CATEGORY_ID:
			return WatcardContract.Category.CONTENT_ITEM_TYPE;
		default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}
	
	/**
     * Perform a database query by URI.
     *
     * <p>Currently supports returning all transactions (/transactions) and individual transactions by ID
     * (/transactions/{ID}).
     */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        String id;
        Cursor c;
        Context ctx = getContext();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case ROUTE_TRANSACTION_ID:
                // Return a single entry, by ID.
                id = uri.getLastPathSegment();
                builder.where(WatcardContract.Transaction._ID + "=?", id);
            case ROUTE_TRANSACTION:
                // Return all known entries.
                builder.table(JOINED_TRANSACTION_TABLE)
                       .where(selection, selectionArgs);
                c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            case ROUTE_BALANCE_ID:
            	// Return a single entry, by ID
            	id = uri.getLastPathSegment();
            	builder.where(WatcardContract.Balance._ID + "=?", id);
            case ROUTE_BALANCE:
            	// Return all known entries
            	builder.table(WatcardContract.Balance.TABLE_NAME)
            		   .where(selection, selectionArgs);
            	c = builder.query(db, projection, sortOrder);
            	// Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            case ROUTE_TERMINAL_ID:
            	// Return a single entry, by ID
            	id = uri.getLastPathSegment();
            	builder.where(WatcardContract.Terminal._ID + "=?", id);
            case ROUTE_TERMINAL:
            	// Return all known entries
            	builder.table(WatcardContract.Terminal.TABLE_NAME)
            		   .where(selection, selectionArgs);
            	c = builder.query(db, projection, sortOrder);
            	// Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            case ROUTE_CATEGORY_ID:
            	// Return a single entry, by ID
            	id = uri.getLastPathSegment();
            	builder.where(WatcardContract.Category._ID + "=?", id);
            case ROUTE_CATEGORY:
            	// Return all known entries
            	builder.table(WatcardContract.Category.TABLE_NAME)
            		   .where(selection, selectionArgs);
            	c = builder.query(db, projection, sortOrder);
            	// Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
	}
	
	/**
     * Insert a new entry into the database.
     */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        long id;
        switch (match) {
            case ROUTE_TRANSACTION:
                id = db.insertOrThrow(WatcardContract.Transaction.TABLE_NAME, null, values);
                result = Uri.parse(WatcardContract.Transaction.CONTENT_URI + "/" + id);
                break;
            case ROUTE_BALANCE:
            	id = db.insertOrThrow(WatcardContract.Balance.TABLE_NAME, null, values);
            	result = Uri.parse(WatcardContract.Balance.CONTENT_URI + "/" + id);
            	break;
            case ROUTE_TERMINAL:
            	id = db.insertOrThrow(WatcardContract.Terminal.TABLE_NAME, null, values);
            	result = Uri.parse(WatcardContract.Terminal.CONTENT_URI + "/" + id);
            	break;
            case ROUTE_CATEGORY:
            	id = db.insertOrThrow(WatcardContract.Category.TABLE_NAME, null, values);
            	result = Uri.parse(WatcardContract.Category.CONTENT_URI + "/" + id);
            	break;
            case ROUTE_TRANSACTION_ID:
            case ROUTE_BALANCE_ID:
            case ROUTE_TERMINAL_ID:
            case ROUTE_CATEGORY_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
	}
	
	/**
	 * Delete an entry by database by URI.
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        String id;
        switch (match) {
            case ROUTE_TRANSACTION:
                count = builder.table(WatcardContract.Transaction.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_TRANSACTION_ID:
                id = uri.getLastPathSegment();
                count = builder.table(WatcardContract.Transaction.TABLE_NAME)
                       .where(WatcardContract.Transaction._ID + "=?", id)
                       .where(selection, selectionArgs)
                       .delete(db);
                break;
            case ROUTE_BALANCE:
            	count = builder.table(WatcardContract.Balance.TABLE_NAME)
            				   .where(selection, selectionArgs)
            				   .delete(db);
            	break;
            case ROUTE_BALANCE_ID:
            	id = uri.getLastPathSegment();
            	count = builder.table(WatcardContract.Balance.TABLE_NAME)
            				   .where(WatcardContract.Balance._ID + "=?", id)
            				   .where(selection, selectionArgs)
            				   .delete(db);
            	break;
            case ROUTE_TERMINAL:
            	count = builder.table(WatcardContract.Terminal.TABLE_NAME)
            				   .where(selection, selectionArgs)
            				   .delete(db);
            	break;
            case ROUTE_TERMINAL_ID:
            	id = uri.getLastPathSegment();
            	count = builder.table(WatcardContract.Terminal.TABLE_NAME)
            				   .where(WatcardContract.Terminal._ID + "=?", id)
            				   .where(selection, selectionArgs)
            				   .delete(db);
            	break;
            case ROUTE_CATEGORY:
            	count = builder.table(WatcardContract.Category.TABLE_NAME)
            				   .where(selection, selectionArgs)
            				   .delete(db);
            	break;
            case ROUTE_CATEGORY_ID:
            	id = uri.getLastPathSegment();
            	count = builder.table(WatcardContract.Category.TABLE_NAME)
            				   .where(WatcardContract.Category._ID + "=?", id)
            				   .where(selection, selectionArgs)
            				   .delete(db);
            	break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
	}

	/**
    * Update an entry in the database by URI.
    */
   @Override
   public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
       SelectionBuilder builder = new SelectionBuilder();
       final SQLiteDatabase db = databaseHelper.getWritableDatabase();
       final int match = sUriMatcher.match(uri);
       int count;
       String id;
       switch (match) {
           case ROUTE_TRANSACTION:
               count = builder.table(WatcardContract.Transaction.TABLE_NAME)
                       .where(selection, selectionArgs)
                       .update(db, values);
               break;
           case ROUTE_TRANSACTION_ID:
               id = uri.getLastPathSegment();
               count = builder.table(WatcardContract.Transaction.TABLE_NAME)
                       .where(WatcardContract.Transaction._ID + "=?", id)
                       .where(selection, selectionArgs)
                       .update(db, values);
               break;
           case ROUTE_BALANCE:
        	   count = builder.table(WatcardContract.Balance.TABLE_NAME)
        	   				  .where(selection, selectionArgs)
        	   				  .update(db, values);
           case ROUTE_BALANCE_ID:
        	   id = uri.getLastPathSegment();
        	   count = builder.table(WatcardContract.Balance.TABLE_NAME)
        			   		  .where(WatcardContract.Balance._ID + "=?", id)
        			   		  .where(selection, selectionArgs)
        			   		  .update(db, values);
        	   break;
           case ROUTE_TERMINAL:
        	   count = builder.table(WatcardContract.Terminal.TABLE_NAME)
        	   				  .where(selection, selectionArgs)
        	   				  .update(db, values);
           case ROUTE_TERMINAL_ID:
        	   id = uri.getLastPathSegment();
        	   count = builder.table(WatcardContract.Terminal.TABLE_NAME)
        			   		  .where(WatcardContract.Terminal._ID + "=?", id)
        			   		  .where(selection, selectionArgs)
        			   		  .update(db, values);
        	   break;
           case ROUTE_CATEGORY:
        	   count = builder.table(WatcardContract.Category.TABLE_NAME)
        	   				  .where(selection, selectionArgs)
        	   				  .update(db, values);
           case ROUTE_CATEGORY_ID:
        	   id = uri.getLastPathSegment();
        	   count = builder.table(WatcardContract.Category.TABLE_NAME)
        			   		  .where(WatcardContract.Category._ID + "=?", id)
        			   		  .where(selection, selectionArgs)
        			   		  .update(db, values);
        	   break;
           default:
               throw new UnsupportedOperationException("Unknown uri: " + uri);
       }
       Context ctx = getContext();
       assert ctx != null;
       ctx.getContentResolver().notifyChange(uri, null, false);
       return count;
   }

}
