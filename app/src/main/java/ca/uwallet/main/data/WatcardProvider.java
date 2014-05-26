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
			" ON " + WatcardContract.Transaction.TABLE_NAME + "." + WatcardContract.Transaction.COLUMN_NAME_TERMINAL +
			" = " + WatcardContract.Terminal.TABLE_NAME + "." + WatcardContract.Terminal._ID; // TODO join category
	
	// Create the UriMatcher
	private static final String AUTHORITY = WatcardContract.CONTENT_AUTHORITY;
	private static final UriMatcher URI_MATCHER;
	private static final int ROUTE_TRANSACTION = 1,
							 ROUTE_TRANSACTION_ID = 2,
							 ROUTE_BALANCE = 3,
							 ROUTE_BALANCE_ID = 4,
							 ROUTE_TERMINAL = 5,
							 ROUTE_TERMINAL_ID = 6,
							 ROUTE_CATEGORY = 7,
							 ROUTE_CATEGORY_ID = 8;
	static{
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, WatcardContract.PATH_TRANSACTION , ROUTE_TRANSACTION);
		URI_MATCHER.addURI(AUTHORITY, WatcardContract.PATH_TRANSACTION + "/#", ROUTE_TRANSACTION_ID);
		URI_MATCHER.addURI(AUTHORITY, WatcardContract.PATH_BALANCE, ROUTE_BALANCE);
		URI_MATCHER.addURI(AUTHORITY, WatcardContract.PATH_BALANCE + "/#", ROUTE_BALANCE_ID);
		URI_MATCHER.addURI(AUTHORITY, WatcardContract.PATH_TERMINAL, ROUTE_TERMINAL);
		URI_MATCHER.addURI(AUTHORITY, WatcardContract.PATH_TERMINAL + "/#", ROUTE_TERMINAL_ID);
		URI_MATCHER.addURI(AUTHORITY, WatcardContract.PATH_CATEGORY, ROUTE_CATEGORY);
		URI_MATCHER.addURI(AUTHORITY, WatcardContract.PATH_CATEGORY + "/#", ROUTE_CATEGORY_ID);
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
		switch(URI_MATCHER.match(uri)){
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
        switch (URI_MATCHER.match(uri)) {
            case ROUTE_TRANSACTION_ID: {
                String id = uri.getLastPathSegment();
                builder.table(JOINED_TRANSACTION_TABLE)
                        .where(WatcardContract.Transaction._ID + "=?", id);
                break;
            }
            case ROUTE_TRANSACTION: {
                builder.table(JOINED_TRANSACTION_TABLE)
                        .where(selection, selectionArgs);
                break;
            }
            case ROUTE_BALANCE_ID: {
                String id = uri.getLastPathSegment();
                builder.table(WatcardContract.Balance.TABLE_NAME)
                        .where(WatcardContract.Balance._ID + "=?", id);
                break;
            }
            case ROUTE_BALANCE: {
                builder.table(WatcardContract.Balance.TABLE_NAME)
                        .where(selection, selectionArgs);
                break;
            }
            case ROUTE_TERMINAL_ID: {
                String id = uri.getLastPathSegment();
                builder.table(WatcardContract.Terminal.TABLE_NAME)
                        .where(WatcardContract.Terminal._ID + "=?", id);
                break;
            }
            case ROUTE_TERMINAL: {
                builder.table(WatcardContract.Terminal.TABLE_NAME)
                        .where(selection, selectionArgs);
                break;
            }
            case ROUTE_CATEGORY_ID: {
                String id = uri.getLastPathSegment();
                builder.table(WatcardContract.Category.TABLE_NAME)
                        .where(WatcardContract.Category._ID + "=?", id);
                break;
            }
            case ROUTE_CATEGORY: {
                builder.table(WatcardContract.Category.TABLE_NAME)
                        .where(selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Cursor cursor = builder.query(db, projection, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
	}
	
	/**
     * Insert a new entry into the database.
     */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        assert db != null;
        final int match = URI_MATCHER.match(uri);
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
        final int match = URI_MATCHER.match(uri);
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
       final int match = URI_MATCHER.match(uri);
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
