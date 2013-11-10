package com.enghack.uwallet.login;

import java.util.ArrayList;

import org.jsoup.nodes.Element;

import com.enghack.watcard.Transaction;

/**
 * Parse all of the information and clean up all of the values of each
 * Transaction in a readable way
 * @author Andy
 *
 */
public class HTMLParser {
	
	
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
					tr.getElementById("oneweb_financial_history_td_date").text(),
					tr.getElementById("oneweb_financial_history_td_trantype").text(),
					tr.getElementById("oneweb_financial_history_td_terminal").text());
			transactionList.add(trans);
		}
		return transactionList;
	}
	
	public double parseFlex(Element table)
	{
		double sum=0;
		for (int i=3; i<6; i++)
		{
			sum += Double.parseDouble(table.select("tr:gt("+i+")").text());
		}
		return sum;
	}
	
	public double parseMeal(Element table)
	{
		for (int i=0;i<3;i++)
		{
			
		}
		return 0;
	}
	
	public double parseOther(Element table)
	{
		for (i=6;i<12;i++)
			
	}
	/**
	 * Filters out all spaces for the price
	 * @param a
	 * @return
	 */
	public static String spaceFilter(String a)
	{
		return a.replaceAll("\\s+","");
	}
}
