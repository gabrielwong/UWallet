package com.enghack.uwallet;

import java.util.ArrayList;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.enghack.watcard.Transaction;
import com.enghack.watcard.WatcardInfo;

public class TransactionFragment extends Fragment implements OnClickListener {

	private Listener mListener;
	private TableLayout table;
	private ArrayList<Transaction> list;
	
	public interface Listener {
	}

	public TransactionFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WatcardInfo watcard = (Activity)getActivity().getWatcardInstance();
		list = watcard.getList();
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.fragment_transaction, container,
				false);
		final int width = getActivity().getWindowManager().getDefaultDisplay().getWidth(); 
		
		int dip = (int) TypedValue.applyDimension(
				 TypedValue.COMPLEX_UNIT_DIP,
				 (float) 1,
				 getResources().getDisplayMetrics());
		final LayoutParams lparams = new LayoutParams(width/4,LayoutParams.MATCH_PARENT);
		lparams.gravity = Gravity.LEFT;
		lparams.bottomMargin = 5;
		lparams.topMargin = 5;
		
		for (Transaction trans:list)
		{
			TableRow row = new TableRow(getActivity());
			TextView price, date, terminal;
			price = new TextView(getActivity());
			date = new TextView(getActivity());
			terminal = new TextView(getActivity());
			
			price.setText(String.format("%.2f", trans.getAmount()));
			date.setText(trans.getDate());
			terminal.setText(trans.getTerminal());
			
			price.setLayoutParams(lparams);
			date.setLayoutParams(lparams);
			terminal.setLayoutParams(lparams);
			terminal.setWidth(width/2);
			
			price.setTextSize(30);
			date.setTextSize(30);
			terminal.setTextSize(30);
			
			row.addView(price);
			row.addView(date);
			row.addView(terminal);
			table.addView(row);
		}
		
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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		table = (TableLayout) getActivity().findViewById(R.id.transaction_table);
	}

}
