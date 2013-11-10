package com.enghack.uwallet;

import java.util.ArrayList;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.enghack.watcard.Transaction;

/**
 * Transaction fragment, implements ScrollView to display information in table format
 * @author Andy, Seikun
 *
 */

public class TransactionFragment extends Fragment implements OnClickListener {

	// To be changed depending on settings
	private static int dateFilter = 0;

	private Listener mListener;
	private TableLayout table;
	private ArrayList<Transaction> list;
	private int textSize = 15;

	public interface Listener {
	}

	public TransactionFragment() {
		// Required empty public constructor
	}

	public void setList(ArrayList<Transaction> transactionList) {
		list = transactionList;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		list = MainActivity.getList();
	}

	@SuppressWarnings("null")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_transaction, container,
				false);
		String temporaryDate = "0";
		ScrollView contain = (ScrollView) v.findViewById(R.id.history_contain);

		int width = getActivity().getWindowManager().getDefaultDisplay().getWidth(); 
		TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
				TableLayout.LayoutParams.MATCH_PARENT,
				TableLayout.LayoutParams.WRAP_CONTENT);
		table = new TableLayout(getActivity());
		table.setLayoutParams(tableParams);
		
		TableRow.LayoutParams lparams = new TableRow.LayoutParams(
				width/2,
				TableRow.LayoutParams.MATCH_PARENT);
		
		lparams.weight = 0.25F;
		lparams.gravity = Gravity.LEFT;
		lparams.bottomMargin = 5;
		lparams.topMargin = 5;
		
		TableRow.LayoutParams dateParams = new TableRow.LayoutParams(
				TableRow.LayoutParams.MATCH_PARENT,
				TableRow.LayoutParams.MATCH_PARENT);
		
		dateParams.weight = 0.30F;
		dateParams.gravity = Gravity.LEFT;
		dateParams.topMargin = 25;
		dateParams.rightMargin = 30;

		for (Transaction trans : list) {
			if (filterDate(trans)) {
				if (temporaryDate.equals("0") || !trans.getDate().equals(temporaryDate))
				{
					temporaryDate = trans.getDate();
					TextView date = new TextView(getActivity());
					SpannableString spanString = new SpannableString(temporaryDate);
					spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
					date.setText(spanString);
					date.setTextSize(textSize+10);
					table.addView(date,dateParams);
					
				}
				TableRow row = new TableRow(getActivity());
				TextView price, terminal;
				price = new TextView(getActivity());
				terminal = new TextView(getActivity());

				price.setText(String.format("%.2f$", trans.getAmount()));
				terminal.setText(trans.getTerminal());

				// terminal.setWidth(width/2);

				price.setTextSize(textSize);
				terminal.setTextSize(textSize);

				row.addView(price, lparams);
				// row.addView(date, lparams);
				row.addView(terminal, lparams);
				table.addView(row, new TableLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			}
		}
		contain.addView(table);
/*		return (ScrollView) inflater.inflate(R.layout.fragment_transaction,
				container, false);*/
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (Listener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	private static boolean filterDate(Transaction trans) {
		switch (dateFilter) {
		default:
			return true;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onClick(View view) {
	}
}
