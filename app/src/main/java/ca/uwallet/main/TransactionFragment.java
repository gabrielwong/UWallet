package ca.uwallet.main;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;
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

public class TransactionFragment extends Fragment implements LoaderCallbacks<Cursor>, SimpleCursorAdapter.ViewBinder{

	private static final int LOADER_TRANSACTION_ID = 137;
	private static final String SORT_ORDER_DESCENDING = "DESC";
    private static final String[] ADAPTER_BIND_FROM = {
            WatcardContract.Transaction.COLUMN_NAME_DATE,
            WatcardContract.Terminal.COLUMN_NAME_TEXT,
            WatcardContract.Transaction.COLUMN_NAME_AMOUNT};
    private static final int[] ADAPTER_BIND_TO = {R.id.date, R.id.description, R.id.amount};

	private SimpleCursorAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, null);

        ListView list = (ListView) view.findViewById(R.id.transaction_list);
        // Create the adapter
        adapter = new SimpleCursorAdapter(getActivity(),
                R.layout.simple_list_transactions, null,
                ADAPTER_BIND_FROM, ADAPTER_BIND_TO, 0);
        adapter.setViewBinder(this); // So that dates are displayed correctly
        list.setAdapter(adapter);
        list.setEmptyView(view.findViewById(R.id.transaction_loading_progress_container));

        // Create the transaction loader
        getLoaderManager().initLoader(LOADER_TRANSACTION_ID, null, this);

        return view;
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
