package ca.uwallet.main;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;
import ca.uwallet.main.data.WatcardContract;
import ca.uwallet.main.util.CommonUtils;

/**
 * Transaction fragment, implements ScrollView to display information in table format
 * @author Andy, Seikun
 *
 */

public class TransactionFragment extends ListFragment implements LoaderCallbacks<Cursor>, SimpleCursorAdapter.ViewBinder{

	private static final int LOADER_TRANSACTION_ID = 137;
	private static final String SORT_ORDER_DESCENDING = "DESC";
	private SimpleCursorAdapter adapter;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		// Set text to display if there's no data
		setEmptyText(getResources().getString(R.string.empty_transaction_message));
		
		// Create the adapter
		int[] to = {R.id.date, R.id.description, R.id.amount};
		String[] from = {WatcardContract.Transaction.COLUMN_NAME_DATE,
				WatcardContract.Terminal.COLUMN_NAME_TEXT,
				WatcardContract.Transaction.COLUMN_NAME_AMOUNT};
		adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.simple_list_transactions, null,
				from, to, 0);
		adapter.setViewBinder(this); // So that dates are displayed correctly
		setListAdapter(adapter);
		
		// Create the transaction loader
		getLoaderManager().initLoader(LOADER_TRANSACTION_ID, null, this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id != LOADER_TRANSACTION_ID)
			return null;
		String sortOrder = WatcardContract.Transaction.COLUMN_NAME_DATE + " " + SORT_ORDER_DESCENDING;
		CursorLoader loader = new CursorLoader(getActivity(), WatcardContract.Transaction.CONTENT_URI,
				null, null, null, sortOrder);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap cursor
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Remove the cursor
		adapter.swapCursor(null);
	}
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		// Display the formatted date
		if (cursor.getColumnName(columnIndex).equals(WatcardContract.Transaction.COLUMN_NAME_DATE)){
			long time = cursor.getLong(columnIndex);
			String s = CommonUtils.formatDate(time);
			((TextView) view).setText(s);
			return true;
		} else if (cursor.getColumnName(columnIndex).equals(WatcardContract.Transaction.COLUMN_NAME_AMOUNT)){
			String s = CommonUtils.formatCurrencyNoSymbol(cursor.getInt(columnIndex));
			((TextView) view).setText(s);
			return true;
		}
		return false;
	}
}
