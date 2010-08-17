package com.redhorse.reddial;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.redhorse.reddial.setdial;
import com.redhorse.reddial.R;
import com.redhorse.reddial.setdial.AppsAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class setdial extends Activity implements OnItemClickListener {

	private GridView mGrid;
	private dbDialConfigAdapter dbStart = null;
	private List<String> mContactURI;
	private List<String> mContactID;
	private List<String> mContact;
	private List<Bitmap> mContactPhoto;
	private static final int STARTALL_REQUEST = 0;
	private static final int STARTCONFIG_REQUEST = 1;
	private static final int STARTWEIBO_REQUEST = 2;
	private static final int STARTPICK_REQUEST = 3;

	private final static int ITEM_ID_SELECT = 0;
	private final static int ITEM_ID_DELETE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// notification(this, "小红马快速启动：随时启动你的最爱!");
		Intent intent = new Intent();
		intent.setClass(this, ServiceRed.class);
		startService(intent);

		dbStart = new dbDialConfigAdapter(this);
		dbStart.open();

		loadApps();

		setContentView(R.layout.main);
		mGrid = (GridView) findViewById(R.id.myGrid);
		mGrid.setAdapter(new AppsAdapter());
		mGrid.setOnItemClickListener(this);
		Button button = (Button) findViewById(R.id.Button01);
		button.setOnClickListener(Button01Listener);
		button = (Button) findViewById(R.id.Button02);
		button.setOnClickListener(Button02Listener);
		button = (Button) findViewById(R.id.Button03);
		button.setOnClickListener(Button03Listener);
		button = (Button) findViewById(R.id.weibogrid);
		button.setOnClickListener(weibogridListener);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Intent it = new Intent();
		switch (requestCode) {
		case STARTPICK_REQUEST:
			switch (resultCode) {
			case RESULT_OK:
                startActivity(new Intent(Intent.ACTION_VIEW,data.getData()));  
                Log.i("reddial",""+data.getData());  
				break;
			default:
				break;
			}
			break;
		case STARTALL_REQUEST:
			switch (resultCode) {
			case RESULT_OK:
				// Bundle b = data.getExtras();
				// String msg = b.getString("msg");
				// if (msg.equalsIgnoreCase("back")) {
				// } else if (msg.equalsIgnoreCase("open")) {
				// String packageName = b.getString("packageName");
				// String name = b.getString("name");
				// Iterator it1 = mAllApps.iterator();
				// ResolveInfo info = null;
				// while (it1.hasNext()) {
				// info = (ResolveInfo) it1.next();
				// if (packageName.equals(info.activityInfo.packageName) &&
				// name.equalsIgnoreCase(info.activityInfo.name)) {
				// Intent intent = new Intent();
				// intent.setClassName(info.activityInfo.packageName,
				// info.activityInfo.name);
				// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// startActivity(intent);
				// break;
				// }
				// }
				// finish();
				// }
				break;
			default:
				finish();
				break;
			}
			break;
		case STARTWEIBO_REQUEST:
			break;
		default:
			finish();
			break;
		}
		Log.e("reddial", "back");
	}

	private OnClickListener Button01Listener = new OnClickListener() {
		public void onClick(View v) {
			// Intent setting = new Intent();
			// setting.setClass(reddial.this, AppAll.class);
			// startActivityForResult(setting, STARTALL_REQUEST);
		}
	};

	private OnClickListener Button02Listener = new OnClickListener() {
		public void onClick(View v) {
			Intent setting = new Intent();
			setting.setClass(setdial.this, contactlist.class);
			startActivityForResult(setting, STARTCONFIG_REQUEST);
		}
	};

	private OnClickListener Button03Listener = new OnClickListener() {
		public void onClick(View v) {
			Intent i = getIntent();
			Bundle b = new Bundle();
			b.putString("msg", "quit");
			i.putExtras(b);
			setdial.this.setResult(RESULT_OK, i);
			dbStart.close();
			setdial.this.finish();
		}
	};

	private OnClickListener weibogridListener = new OnClickListener() {
		public void onClick(View v) {
			// Intent setting = new Intent();
			// setting.setClass(reddial.this, weibo.class);
			// startActivityForResult(setting, STARTWEIBO_REQUEST);
		}
	};

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
        	String name = ac.getString(ac.getColumnIndexOrThrow(People.DISPLAY_NAME));// People.NAME
        	String userid = ac.getString(ac.getColumnIndexOrThrow(People._ID));// People._ID
			InputStream is = People.openContactPhotoInputStream(
					getContentResolver(), auri);
			Bitmap mBitmap = BitmapFactory.decodeStream(is);
			mContactID.add(userid);
			Log.e("reddial:uid", userid);
			mContactURI.add(auri.toString());
			mContact.add(name);
			mContactPhoto.add(mBitmap);
        }
	}

	// 重点在这里面
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		AlertDialog opDialog = new AlertDialog.Builder(setdial.this)
        .setTitle("选项")
        .setItems(R.array.select_dialog_items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String[] items = getResources().getStringArray(R.array.select_dialog_items);
        		switch (which) {
        		case ITEM_ID_SELECT:
        	        int sdk=new Integer(Build.VERSION.SDK).intValue();          
        	        Uri uri = null;
        	        if (sdk>=5) {  
        	            try {  
        	                Class clazz=Class.forName("android.provider.ContactsContract$Contacts");  
        	                uri=(Uri)clazz.getField("CONTENT_URI").get(clazz);  
        	            }  
        	            catch (Throwable t) {  
        	                Log.e("reddial", "Exception when determining CONTENT_URI", t);  
        	            }  
        	        }  
        	        else {  
        	        	uri=Contacts.People.CONTENT_URI;  
        	        }  
                    Intent i=new Intent(Intent.ACTION_PICK, uri);  
                    startActivityForResult(i, STARTPICK_REQUEST);
        			break;
        		case ITEM_ID_DELETE:
//        			String id = ((HashMap) list.getItemAtPosition((int) arg3)).get("ItemID").toString();
//        			Log.e("debug", id);
//        			dbBookmarks.deleteTitle(id);
//        			listItem.remove((int) arg3);
//        			list.setAdapter(listItemAdapter);
        			break;
        		}
            }
        })
        .create();
		opDialog.show();
	}

	public class AppsAdapter extends BaseAdapter {
		public AppsAdapter() {
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			LinearLayout layout = new LinearLayout(setdial.this);
			layout.setOrientation(LinearLayout.VERTICAL);

			String info = mContact.get(position);
			Bitmap img = mContactPhoto.get(position);
			layout.addView(addTitleView(img, info));

			return layout;
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

		public View addTitleView(Bitmap image, String title) {
			LinearLayout layout = new LinearLayout(setdial.this);
			layout.setOrientation(LinearLayout.VERTICAL);

			ImageView iv = new ImageView(setdial.this);
			iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
			iv.setLayoutParams(new GridView.LayoutParams(60, 60));
			iv.setImageBitmap(image);

			layout.addView(iv);

			TextView tv = new TextView(setdial.this);
			// tv.setTransformationMethod(SingleLineTransformationMethod.getInstance());
			tv.setSingleLine(true);
			tv.setText(title);
			tv.setTextSize(18);

			layout.addView(tv, new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));

			layout.setGravity(Gravity.CENTER);
			return layout;
		}

	}

	// 创建菜单
	private final static int ITEM_ID_SETTING = 11;
	private final static int ITEM_ID_ABOUT = 12;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		// menu.add(1, ITEM_ID_SETTING, 0, R.string.setting).setIcon(
		// R.drawable.menu_syssettings);
		menu.add(1, ITEM_ID_ABOUT, 0, R.string.about).setIcon(
				R.drawable.menu_help);
		return true;
	}

	// 给菜单加事件
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case ITEM_ID_SETTING:
			// Intent setting = new Intent();
			// setting.setClass(redhorse.this, reddial.class);
			// startActivity(setting);
			break;
		case ITEM_ID_ABOUT:
			Intent setting = new Intent();
			setting.setClass(setdial.this, Feedback.class);
			startActivity(setting);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private NotificationManager mNM;

}