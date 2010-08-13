package com.redhorse.reddial;

import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
/*必须引用database.Cursor,Contacts.People
 与 net.uri等类来使用联系人数据*/
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class contactlist extends Activity {
	/* 声明四个UI变量与一个常数作为Activity接收返回值用 */
	private TextView mTextView01;
	private Button mButton01;
	private EditText mEditText01;
	private EditText mEditText02;
	private static final int PICK_CONTACT_SUBACTIVITY = 2;
	private Editor editor;

	private dbDialConfigAdapter dbDial = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences share = this.getPreferences(MODE_PRIVATE);
		editor = share.edit();// 取得编辑器
//		editor.putString("p1", "");
//		editor.commit();// 提交刷新数据

        dbDial = new dbDialConfigAdapter(this);
        dbDial.open();

        setContentView(R.layout.test);

		/*
		 * 通过findViewById构造器来构造一个TextView, 两个EditText,一个Button对象*
		 */
		mTextView01 = (TextView) findViewById(R.id.myTextView1);
		mEditText01 = (EditText) findViewById(R.id.myEditText01);
		mEditText02 = (EditText) findViewById(R.id.myEditText02);
		mButton01 = (Button) findViewById(R.id.myButton1);

		/* 设置onClickListener 让用户点击Button时搜索联系人 */
		mButton01.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/* 建构Uri来取得联系人的资源位置 */
				Uri uri = Uri.parse("content://contacts/people");
				/* 通过Intent来取得联系人数据并返回所选的值 */
				Intent intent = new Intent(Intent.ACTION_PICK, uri);
				/* 打开新的Activity并期望该Activity返回值 */
				startActivityForResult(intent, PICK_CONTACT_SUBACTIVITY);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
		case PICK_CONTACT_SUBACTIVITY:
			Uri uri = data.getData();
			if (uri != null) {

				dbDial.deleteAllItems();
    			dbDial.insertItem("", "", uri.getPath());
    			editor.putString("p1", uri.toString());
    			editor.commit();// 提交刷新数据
    			
    			Log.e("reddial", uri.getPath());
	    		
				Cursor c = managedQuery(uri, null, null, null, null);
				c.moveToFirst();
				String name = c.getString(c
						.getColumnIndexOrThrow(People.DISPLAY_NAME));// People.NAME
				StringBuilder num = new StringBuilder();
				String contactId = c.getString(c.getColumnIndex(People._ID));
				
//				for (int i=0;i<c.getColumnCount();i++) {
//					String phoneNumber = c.getString(i);
//					num.append(c.getColumnName(i)+"="+phoneNumber);
//					num.append(";\n");
//					
//				}

				// if has phone number
				Cursor phones = getContentResolver()
						.query(Phones.CONTENT_URI, null, 
		                        Phones.PERSON_ID + "=" + String.valueOf(contactId), null, null);;
				while (phones.moveToNext()) {
					String phoneNumber = phones.getString(phones
							.getColumnIndex(People.NUMBER));
					// 多个号码如何处理
					num.append(phoneNumber);
					num.append(";");
				}
				num.append(phones.getCount());
				
				phones.close();

				mEditText01.setText(name);
				mEditText02.setText(num);
				break;
			}
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	public static Cursor queryContactPhones(Context context, long contactId) {
		if (context == null)
			return null;
		Cursor c = context.getContentResolver().query(Phones.CONTENT_URI, null,
				Phones.PERSON_ID + "=" + String.valueOf(contactId), null, null);
		if (c != null)
			c.moveToFirst();
		return c;
	}
}
