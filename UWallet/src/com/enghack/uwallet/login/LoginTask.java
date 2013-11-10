package com.enghack.uwallet.login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;


public class LoginTask extends AsyncTask<String,Void, Element>{
	
	public ResponseListener mListener;
	Document doc;
	
	public interface ResponseListener
	{
		public void onResponseFinish(Element doc); 
	}

    public LoginTask(ResponseListener listener){
        this.mListener = listener;
    }
    
	public LoginTask()
	{
		// Need this to be empty
	}
	
	@Override
	protected Element doInBackground(String... string) {
	
    	String endResult = null;
    	DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = null;
        HttpEntity entity;
        HttpPost httpost = new HttpPost("https://account.watcard.uwaterloo.ca/watgopher661.asp");
        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair("acnt_1", string[1]));
        nvps.add(new BasicNameValuePair("acnt_2", string[2]));
        nvps.add(new BasicNameValuePair("PASS", "PASS"));
        nvps.add(new BasicNameValuePair("STATUS", "HIST"));
        nvps.add(new BasicNameValuePair("DBDATE", "01/01/0001"));
        nvps.add(new BasicNameValuePair("DEDATE", "01/01/2111"));
        

        try {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF_8"));
            response = httpclient.execute(httpost);
                
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        

        BasicResponseHandler myHandler = new BasicResponseHandler();

        try {
            endResult = myHandler.handleResponse(response);
            System.out.println(endResult);
        } catch (HttpResponseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
        /*try {
			doc = loadXMLFromString(endResult);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        doc = Jsoup.parseBodyFragment(endResult);
        Element table = doc.getElementById("oneweb_financial_history_table");
		return table;
	}
	

	@Override
	protected void onPostExecute(Element result) {
		mListener.onResponseFinish(result);
		return;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}
}
