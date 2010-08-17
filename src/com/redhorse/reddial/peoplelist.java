/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redhorse.reddial;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.redhorse.reddial.reddial.AppsAdapter;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * A list view example where the 
 * data comes from a cursor.
 */
public class peoplelist extends Activity implements OnItemClickListener  {
    
	private Cursor c;
	private ListView mList;

	private List<String> mContactURI;
	private List<String> mContactID;
	private List<String> mContact;
	private List<Bitmap> mContactPhoto;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        // Get a cursor with all people
//        c = getContentResolver().query(People.CONTENT_URI, null, null, null, null);
//        startManagingCursor(c);
//
//        ListAdapter adapter = new SimpleCursorAdapter(this, 
//                // Use a template that displays a text view
//                android.R.layout.simple_list_item_1, 
//                // Give the cursor to the list adatper
//                c, 
//                // Map the NAME column in the people database to...
//                new String[] {People.NAME} ,
//                // The "text1" view defined in the XML template
//                new int[] {android.R.id.text1}); 
//        setContentView(R.layout.peoplelist);
// 		  mList = (ListView) findViewById(R.id.PeopleListView);
//		  mList.setAdapter(adapter);
//		  mList.setOnItemClickListener(this);

		loadApps();
		setContentView(R.layout.peoplelist);
		mList = (ListView) findViewById(R.id.PeopleListView);
		mList.setAdapter(new AppsAdapter());
		mList.setOnItemClickListener(this);
    }

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Intent i = getIntent();  
        Bundle b = new Bundle();  
//        b.putString("uid", (c.getString(c.getColumnIndexOrThrow(People._ID))));
        b.putString("uid", mContactID.get(arg2));
        i.putExtras(b);  
        peoplelist.this.setResult(RESULT_OK, i);  
		peoplelist.this.finish();	
	}

	public class AppsAdapter extends BaseAdapter {
		public AppsAdapter() {
		}

		public View getView(int position, View convertView, ViewGroup parent) {

        	// 从layout文件生成list里面的内容
            convertView = LayoutInflater.from(getApplicationContext()).inflate  
            (R.layout.peoplerow,null);  
              
            TextView mTextView = (TextView)convertView.findViewById(R.id.imageTitle);  
            mTextView.setText(mContact.get(position));  
            ImageView mImageView = (ImageView)convertView.findViewById(R.id.imageView);  
            mImageView.setImageBitmap(mContactPhoto.get(position));
            GradientDrawable grad = new GradientDrawable( 
            		   Orientation.TOP_BOTTOM, 
            		   new int[] {Color.DKGRAY, Color.BLACK} 
            		);
            convertView.setBackgroundDrawable(grad);
            return convertView;          	
			
		}

		public final int getCount() {
			return mContact.size();
		}

		public final Object getItem(int position) {
			return mContact.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}
	}
	private void loadApps() {
		 mContactID = new ArrayList<String>();
		 mContactURI = new ArrayList<String>();
		 mContact = new ArrayList<String>();
		 mContactPhoto = new ArrayList<Bitmap>();

		 Uri uri = null;
		 String columnName = null;
		 int os_version = Integer.parseInt(Build.VERSION.SDK.toString());
		 if (os_version > 4) {// 2.x，sdk版本
		 uri = Uri.parse("content://com.android.contacts/contacts");// new
		 // Uri("content://com.android.contacts/contacts");
		 columnName = "display_name";
		 } else {// 1.6以下SDK
		 uri = Contacts.People.CONTENT_URI;
		 columnName = Contacts.People.NAME;
		 }
		 String tmpNum = new String();// 记录临时号码
		 // 处理联系人和邮件
		 Cursor c = getContentResolver().query(uri, null,
		 null, null, null);// 查询所有包含content的名字
		
		 //获取联系人姓名
		 for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
		 int uid = c.getInt(c.getColumnIndexOrThrow(People._ID));
		 Uri auri = ContentUris.withAppendedId(People.CONTENT_URI, uid);
		 Cursor ac = managedQuery(auri, null, null, null, null);
		 ac.moveToFirst();
		 String name =
		 ac.getString(ac.getColumnIndexOrThrow(People.DISPLAY_NAME));//
		 String userid = ac.getString(ac.getColumnIndexOrThrow(People._ID));//
		 InputStream is = People.openContactPhotoInputStream(
		 getContentResolver(), auri);
		 Bitmap mBitmap = BitmapFactory.decodeStream(is);
		 mContactID.add(userid);
		 Log.e("reddial:uid", userid);
		 mContactURI.add(auri.toString());
//		 if (name.equalsIgnoreCase("")) {
//			 mContact.add(ac.getString(ac.getColumnIndexOrThrow(People.NUMBER)));
//		 } else {
//			 mContact.add(name);		 
//		 }
		 mContact.add(name);		 
		 mContactPhoto.add(mBitmap);
		 }
	}	
}
