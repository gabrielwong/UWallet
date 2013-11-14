package ca.uwallet.main;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import ca.uwallet.main.util.ProviderUtils;


public class StatsFragment extends Fragment {

	public StatsFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_stats, container,
				false);
		

		((LinearLayout)v).addView(getBalanceChart());
		
		return v;
	}

	private DefaultRenderer buildRenderer(int[] colors){
		DefaultRenderer renderer = new DefaultRenderer();
		for (int color : colors) {
	        SimpleSeriesRenderer r = new SimpleSeriesRenderer();
	        r.setColor(color);
	        renderer.addSeriesRenderer(r);
	    }
		renderer.setBackgroundColor(0x00000000);
		renderer.setPanEnabled(false);
		renderer.setZoomEnabled(false);
		renderer.setLabelsTextSize(30);
		renderer.setShowLegend(false);
	    return renderer;
	}
	
	private GraphicalView getBalanceChart(){
		Context context = getActivity();
		CategorySeries series = new CategorySeries("Balance");
		
		int[] amounts = ProviderUtils.getBalanceAmounts(getActivity());
		series.add("Meal Plan", ProviderUtils.getMealBalance(amounts));
		series.add("Flex Dollars", ProviderUtils.getFlexBalance(amounts));
		
		int[] colors = {0xFF00FF00, 0xFFFFFF00};
		DefaultRenderer renderer = buildRenderer(colors);
		return ChartFactory.getPieChartView(context, series, renderer);
	}
	
	private GraphicalView getTransactionChart(){
		return null;
	}
}
