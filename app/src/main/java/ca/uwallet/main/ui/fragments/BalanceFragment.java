package ca.uwallet.main.ui.fragments;

import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.uwallet.main.R;
import ca.uwallet.main.data.WatcardContract;
import ca.uwallet.main.util.CommonUtils;

/**
 * Balance fragment displays watcard balances in a really simple format
 * By changing TextView values 
 * @author Seikun
 */

public class BalanceFragment extends Fragment implements LoaderCallbacks<Cursor>{
	
	private static final int LOADER_BALANCES_ID = 501;
    private TextView mealTextView;
    private TextView flexTextView;
    private TextView totalTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(LOADER_BALANCES_ID, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_balance, container,
				false);
        mealTextView = (TextView) view.findViewById(R.id.meal_plan_label);
        flexTextView = (TextView) view.findViewById(R.id.flex_dollars_label);
        totalTextView = (TextView) view.findViewById(R.id.total_label);
        return view;
	}

	@Override
	public void onStart() {
		super.onStart();
	}
	
	private void updateLabels(View v, Cursor cursor){
		if (v == null)
			return;
		String mealText;
		String flexText;
		String totalText;
		Resources res = getResources();
		
		if (cursor == null || cursor.getCount() < CommonUtils.BALANCE_CURSOR_COUNT){ // Not synced yet
			mealText = res.getString(R.string.meal_plan) + " " + res.getString(R.string.loading);
			flexText = res.getString(R.string.flex_dollars) + " " + res.getString(R.string.loading);
			totalText = res.getString(R.string.total) + " " + res.getString(R.string.loading);
		}else{ // Show the data
			int[] amounts = CommonUtils.getBalanceAmounts(cursor);
			mealText = res.getString(R.string.meal_plan) + " " + CommonUtils.formatCurrency(amounts[CommonUtils.MEAL_PLAN]);
			flexText = res.getString(R.string.flex_dollars) + " " + CommonUtils.formatCurrency(amounts[CommonUtils.FLEX_DOLLAR]);
			totalText = res.getString(R.string.total) + " " + CommonUtils.formatCurrency(amounts[CommonUtils.TOTAL]);
		}
		mealTextView.setText(mealText);
		flexTextView.setText(flexText);
		totalTextView.setText(totalText);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id != LOADER_BALANCES_ID)
			return null;
		return new CursorLoader(getActivity(), WatcardContract.Balance.CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		updateLabels(getView(), data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		updateLabels(getView(), null);
	}	
}
