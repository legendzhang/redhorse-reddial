package com.redhorse.reddial;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.SingleLineTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpConnection;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import weibo4j.Status;
import weibo4j.Weibo;
import weibo4j.WeiboException;

//Need the following import to get access to the app resources, since this
//class is in a sub-package.

public class weibo extends Activity implements OnItemClickListener {

	private EditText weibocontent;
	private EditText username;
	private EditText password;

	private String strUsername;
	private String strPassword;
	private SharedPreferences share;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.weibo);
		Button button = (Button) findViewById(R.id.btn_weibo_send);
		button.setOnClickListener(SendListener);
		button = (Button) findViewById(R.id.btn_weibo_quit);
		button.setOnClickListener(QuitListener);
		weibocontent = (EditText) findViewById(R.id.et_weibo);
		weibocontent.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View arg0, boolean isFocused) {
				if (isFocused == true) {
					weibocontent.selectAll();
				} else {
				}
			}
		});
		username = (EditText) findViewById(R.id.et_weibo_username);
		username.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View arg0, boolean isFocused) {
				if (isFocused == true) {
					username.selectAll();
				} else {
				}
			}
		});
		password = (EditText) findViewById(R.id.et_weibo_password);
		password.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View arg0, boolean isFocused) {
				if (isFocused == true) {
					password.selectAll();
				} else {
				}
			}
		});
		
		share = this.getPreferences(MODE_PRIVATE);
		strUsername = share.getString("strUsername", "");
		strPassword = share.getString("strPassword", "");
		username.setText(strUsername);
		password.setText(strPassword);

	}

	private OnClickListener QuitListener = new OnClickListener() {
		public void onClick(View v) {
			weibo.this.finish();
		}
	};

	private OnClickListener SendListener = new OnClickListener() {
		public void onClick(View v) {
	        long l1 = System.currentTimeMillis();
	        weibocontent = (EditText) findViewById(R.id.et_weibo);
			username = (EditText) findViewById(R.id.et_weibo_username);
			password = (EditText) findViewById(R.id.et_weibo_password);
			Editor edUsername = share.edit();
			edUsername.putString("strUsername", username.getText().toString());
			edUsername.commit();
			Editor edPassword = share.edit();
			edPassword.putString("strPassword", password.getText().toString());
			edPassword.commit();
			
	        Weibo weibo = new Weibo(username.getText().toString(), password.getText().toString());
	         Status status = null;
	 		try {
	 			status = weibo.updateStatus(weibocontent.getText().toString());
	 		} catch (WeiboException e2) {
	 			// TODO Auto-generated catch block
	 			e2.printStackTrace();
	 		}		
		}
	};

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

	}
}
