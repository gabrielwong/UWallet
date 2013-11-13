package ca.uwallet.main.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.nodes.Element;

import ca.uwallet.main.watcard.Transaction;

/**
 * Parse all of the information and clean up all of the values of each
 * Transaction in a readable way
 * @author Andy
 *
 */
public class HTMLParser {
	
	private ArrayList<String> values = new ArrayList<String>();
	public static HashMap<Integer,String> map = new HashMap<Integer, String>();
	public HTMLParser() {
		// Void Constructor
	}
	
	/**
	 * Receives a table as parameter and parses it to
	 * give a list of transactions containing relevant informatoin
	 * @param table
	 * @return
	 */
	public ArrayList<Transaction> parseHist(Element table)
	{
		int count = 0;
		
		ArrayList<Transaction> transactionList = new ArrayList<Transaction>();
		// For all table rows greater than the first one
		// First one has title of each column
		for (Element tr:table.select("tr:gt(1)"))
		{
			// To be modified
			Transaction trans = new Transaction(
					count++,
					Double.parseDouble(spaceFilter(tr.getElementById("oneweb_financial_history_td_amount").text())),
					filterNonNumerical(tr.getElementById("oneweb_financial_history_td_date").text()),
					filterNonAlphabetic(tr.getElementById("oneweb_financial_history_td_trantype").text()),
					filterNonAlphabetic(tr.getElementById("oneweb_financial_history_td_terminal").text()));
			transactionList.add(trans);
		}
		return transactionList;
	}
	
	public double parseBalance(Element table, int index1, int index2)
	{
		double sum=0;
		for (int i=index1; i<index2; i++)
		{
			Element tr = table.select("tr:eq("+i+")").first();
			sum += Double.parseDouble(spaceFilter(tr.getElementById("oneweb_balance_information_td_amount").text()));
		}
		System.out.println(sum);
		return sum;
	}
	
	/**
	 * Filters out all spaces for the price
	 * @param a
	 * @return
	 */
	public String spaceFilter(String a)
	{
		return a.replaceAll("\\s+","");
	}
	
	private int filterNonAlphabetic(String s)
	{
		s = s.replaceAll("[^A-Z -]","");
		if (!values.contains(s))
		{
			values.add(s);
			map.put(Integer.valueOf(values.indexOf(s)),s);
		}
		return values.indexOf(s);
	}
	
	private Integer filterNonNumerical(String s)
	{
		String onlyLetters = "[^0-9]";
        return Integer.parseInt(s.replaceAll(onlyLetters,""));
	}
}