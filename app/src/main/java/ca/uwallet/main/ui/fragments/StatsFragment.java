package ca.uwallet.main.ui.fragments;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.MultipleCategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import ca.uwallet.main.R;
import ca.uwallet.main.data.WatcardContract;
import ca.uwallet.main.util.CommonUtils;


/**
 * Fragment used to display statistics
 * @author Andy
 *
 */
public class StatsFragment extends Fragment implements LoaderCallbacks<Cursor>, OnClickListener {
	
	private static final int LOADER_BALANCES_ID = 17;
	private static final int BALANCE_CHART_ID = 123456;
	private GraphicalView dataChart;
	private MultipleCategorySeries series;
	private DefaultRenderer renderer;
	
	public int colors[];
	private int darkColors[];
	private int lightColors[];
	
	private int indexPreviousTouch = -1;

	public StatsFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_stats, container,
				false);
		v.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {	
				SeriesSelection seriesSelection = dataChart.getCurrentSeriesAndPoint();
				
		        if (seriesSelection != null) 
		        {
		        	if (indexPreviousTouch != -1)
		        	{
			        	renderer.getSeriesRendererAt(indexPreviousTouch).setHighlighted(false);
			        	renderer.getSeriesRendererAt(indexPreviousTouch).setColor(colors[indexPreviousTouch]);
		        	}
		        	
		        	indexPreviousTouch = seriesSelection.getPointIndex();
		            renderer.setCenterDisplay(indexPreviousTouch);
		            renderer.setDisplayColor(lightColors[indexPreviousTouch]);
		            // renderer.getSeriesRendererAt(indexPreviousTouch).setHighlighted(true);
		            renderer.getSeriesRendererAt(indexPreviousTouch).setColor(darkColors[indexPreviousTouch]);
		            dataChart.repaint();
		            
		          }
		        return;
			}
			
		});

		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		colors = new int[]{getResources().getColor(R.color.BLUE),
				getResources().getColor(R.color.PURPLE),
				getResources().getColor(R.color.GREEN),
				getResources().getColor(R.color.ORANGE),
				getResources().getColor(R.color.RED)};
		darkColors = new int[]{getResources().getColor(R.color.DARK_BLUE),
				getResources().getColor(R.color.DARK_PURPLE),
				getResources().getColor(R.color.DARK_GREEN),
				getResources().getColor(R.color.DARK_ORANGE),
				getResources().getColor(R.color.DARK_RED)};
		lightColors = new int[]{getResources().getColor(R.color.DARK_GREY),
				getResources().getColor(R.color.DARK_GREY),
				getResources().getColor(R.color.DARK_GREY),
				getResources().getColor(R.color.DARK_GREY),
				getResources().getColor(R.color.DARK_GREY)}; 
		
		getLoaderManager().initLoader(LOADER_BALANCES_ID, null, this);
	}

	private DefaultRenderer buildRenderer(int length){
		DefaultRenderer renderer = new DefaultRenderer();
		for (int i=0; i<length;i++) {
	        SimpleSeriesRenderer r = new SimpleSeriesRenderer();
	        r.setColor(colors[i]);
	        renderer.addSeriesRenderer(r);
	        r.setShowLegendItem(true);
	    }
		renderer.setBackgroundColor(0xFF000000);
		renderer.setPanEnabled(false);
		renderer.setZoomEnabled(false);
		renderer.setLabelsTextSize(30);
		renderer.setLegendTextSize(40);
		renderer.setShowLegend(false);
		renderer.setClickEnabled(true);
		renderer.setShowLabels(false);
	    return renderer;
	}
	
	private GraphicalView getBalanceChart(int[] amounts){
		Context context = getActivity();
		series = new MultipleCategorySeries("Balance");
		series.add(new String[] {"Meal Plan",  "Flex Dollars"}, 
				new double[] {(double)amounts[0]/100,
				(double)amounts[1]/100});
		renderer = buildRenderer(series.getItemCount(0));
		return ChartFactory.getDoughnutChartView(context, series, renderer);
	}
	
	private void appendView(View v, int id){
		ViewGroup parent = (ViewGroup)getView();
		if (parent != null){
			View old = parent.findViewById(id);
			if (old != null)
				parent.removeView(old);
			if (v != null)
				parent.addView(v);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id != LOADER_BALANCES_ID)
			return null;
		return new CursorLoader(getActivity(), WatcardContract.Balance.CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		int[] amounts = CommonUtils.getBalanceAmounts(data);
		dataChart= getBalanceChart(amounts);
		appendView(dataChart, BALANCE_CHART_ID);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		appendView(null, BALANCE_CHART_ID);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
