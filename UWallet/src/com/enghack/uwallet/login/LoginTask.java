package com.enghack.uwallet.login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.os.AsyncTask;


public class LoginTask extends AsyncTask<String,Void, Document>{
	
	ResponseListener mListener;
	
	public interface ResponseListener
	{
		public void onResponseFinish(Document doc); 
	}

    public LoginTask(ResponseListener listener){
        this.mListener = listener;
    }
    
	public LoginTask()
	{
		// Need this to be empty
	}
	
	@Override
	protected Document doInBackground(String... string) {
	

    	String endResult = null;
    	DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = null;
        HttpEntity entity;
        Document doc = new Document();
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
            entity = response.getEntity();
            InputStream is = entity.getContent();           
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            endResult = sb.toString();
            System.out.println(endResult);
                
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        doc = Jsoup.parse(endResult);     
		
		return doc;
	}

	@Override
	protected void onPostExecute(Document result) {
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
