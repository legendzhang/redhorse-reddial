package com.redhorse.reddial;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.redhorse.reddial.reddial;
import com.redhorse.reddial.R;
import com.redhorse.reddial.reddial.AppsAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.provider.Contacts.PhonesColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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

public class reddial extends Activity implements OnItemClickListener {

	private GridView mGrid;
	private dbDialConfigAdapter dbDial = null;
	private List<String> mContactURI;
	private List<String> mContactID;
	private List<String> mContact;
	private List<Bitmap> mContactPhoto;
	private static final int STARTALL_REQUEST = 0;
	private static final int STARTCONFIG_REQUEST = 1;
	private static final int STARTWEIBO_REQUEST = 2;
	private static final int STARTPICK_REQUEST = 3;

	private final static int ITEM_ID_OPEN = 0;
	private final static int ITEM_ID_DELETE = 1;
	private final static int ITEM_ID_EDIT = 2;

	private int itempos;
	private SharedPreferences share;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent();
		intent.setClass(this, ServiceRed.class);
		startService(intent);

        dbDial = new dbDialConfigAdapter(this);
        dbDial.open();

		share = this.getPreferences(MODE_PRIVATE);
        
		loadApps();

		setContentView(R.layout.main);
		mGrid = (GridView) findViewById(R.id.myGrid);
		mGrid.setAdapter(new AppsAdapter());
		mGrid.setOnItemClickListener(this);
		Button button = (Button) findViewById(R.id.Button01);
		button.setOnClickListener(Button01Listener);
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
		case STARTCONFIG_REQUEST:
			switch (resultCode) {
			case RESULT_OK:
				break;
			default:
				break;
			}
			break;
		case STARTPICK_REQUEST:
			switch (resultCode) {
			case RESULT_OK:
				Bundle b = data.getExtras();
				String uid = b.getString("uid");
				Log.e("reddial--->",uid);
				int sdk = new Integer(Build.VERSION.SDK).intValue();
				Uri uri = null;
				if (sdk >= 5) {
					try {
						Class clazz = Class
								.forName("android.provider.ContactsContract$Contacts");
						uri = (Uri) clazz.getField("CONTENT_URI")
								.get(clazz);
					} catch (Throwable t) {
						Log.e("reddial",
								"Exception when determining CONTENT_URI",
								t);
					}
				} else {
					uri = Contacts.People.CONTENT_URI;
				}
				mContactID.set(itempos, uid);
				Uri auri = Uri.parse(uri.toString()+"/"+uid); 
    			Editor editor = share.edit();
    			editor.putString("dial"+Integer.toString(itempos), auri.toString());
    			editor.commit();// 提交刷新数据
    			mContactURI.set(itempos, auri.toString());
				Cursor ac = managedQuery(auri, null, null, null, null);
				ac.moveToFirst();
				String name = ac.getString(ac
						.getColumnIndexOrThrow(People.DISPLAY_NAME));// People.NAME
				String userid = ac.getString(ac
						.getColumnIndexOrThrow(People._ID));// People._ID
				InputStream is = People.openContactPhotoInputStream(
						getContentResolver(), auri);
				Bitmap mBitmap = BitmapFactory.decodeStream(is);
				mContact.set(itempos, name);
				mContactPhoto.set(itempos,mBitmap);
//				Uri uri = data.getData();
//				if (uri != null) {
//					dbDial.deleteAllItems();
//	    			dbDial.insertItem("", "", uri.getPath());
//	    			Editor editor = share.edit();
//	    			editor.putString("dial"+Integer.toString(itempos), uri.toString());
//	    			editor.commit();// 提交刷新数据
//	    			mContactURI.set(itempos, uri.toString());
//					Uri auri = Uri.parse(mContactURI.get(itempos).toString());
//					Cursor ac = managedQuery(auri, null, null, null, null);
//					ac.moveToFirst();
//					String name = ac.getString(ac
//							.getColumnIndexOrThrow(People.DISPLAY_NAME));// People.NAME
//					String userid = ac.getString(ac
//							.getColumnIndexOrThrow(People._ID));// People._ID
//					InputStream is = People.openContactPhotoInputStream(
//							getContentResolver(), auri);
//					Bitmap mBitmap = BitmapFactory.decodeStream(is);
//					mContactID.set(itempos, userid);
//					mContact.set(itempos, name);
//					mContactPhoto.set(itempos,mBitmap);
//	    			//setContentView(R.layout.main);	    			
//				}
//				loadApps();
				mGrid.setAdapter(new AppsAdapter());
				break;
			default:
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
			int sdk = new Integer(Build.VERSION.SDK).intValue();
			Uri uri = null;
			if (sdk >= 5) {
				try {
					Class clazz = Class
							.forName("android.provider.ContactsContract$Contacts");
					uri = (Uri) clazz.getField("CONTENT_URI").get(clazz);
				} catch (Throwable t) {
					Log.e("reddial", "Exception when determining CONTENT_URI",
							t);
				}
			} else {
				uri = Contacts.People.CONTENT_URI;
			}
			Intent i = new Intent(Intent.ACTION_VIEW, uri);
			startActivityForResult(i, 1);
		}
	};

	private OnClickListener Button03Listener = new OnClickListener() {
		public void onClick(View v) {
//			Intent i = getIntent();
//			Bundle b = new Bundle();
//			b.putString("msg", "quit");
//			i.putExtras(b);
//			reddial.this.setResult(RESULT_OK, i);
//			dbDial.close();
//			reddial.this.finish();
			Intent DialIntent = new 
			Intent(Intent.ACTION_DIAL,Uri.parse("tel:"));
			/** Use NEW_TASK_LAUNCH to launch the Dialer Activity */ 
			DialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK ); 
			/** Finally start the Activity */ 
			startActivity(DialIntent); 
		}
	};

	private OnClickListener weibogridListener = new OnClickListener() {
		public void onClick(View v) {
//			Intent setting = new Intent();
//			setting.setClass(reddial.this, weibo.class);
//			startActivityForResult(setting, STARTWEIBO_REQUEST);
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setType("vnd.android.cursor.dir/calls");
	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        startActivity(intent); 
		}
	};

	private void loadApps() {
		mContactID = new ArrayList<String>();
		mContactURI = new ArrayList<String>();
		mContact = new ArrayList<String>();
		mContactPhoto = new ArrayList<Bitmap>();

		mContactURI.add(share.getString("dial0", ""));
		mContactURI.add(share.getString("dial1", ""));
		mContactURI.add(share.getString("dial2", ""));
		mContactURI.add(share.getString("dial3", ""));
		mContactURI.add(share.getString("dial4", ""));
		mContactURI.add(share.getString("dial5", ""));
		mContactURI.add(share.getString("dial6", ""));
		mContactURI.add(share.getString("dial7", ""));
		mContactURI.add(share.getString("dial8", ""));
		mContactURI.add(share.getString("dial9", ""));
		mContactURI.add(share.getString("dial10", ""));
		mContactURI.add(share.getString("dial11", ""));

		for (int i = 0; i < mContactURI.size(); i++) {
			if (mContactURI.get(i).toString().equalsIgnoreCase("")) {
				mContactID.add("");
				mContact.add("");
				mContactPhoto.add(null);
//				Resources res = this.getResources();
//				Bitmap mBitmap = ((BitmapDrawable) res.getDrawable(R.drawable.contact)).getBitmap();
//				mContactPhoto.add(mBitmap);
			} else {
				Uri auri = Uri.parse(mContactURI.get(i).toString());
				Cursor ac = managedQuery(auri, null, null, null, null);
				ac.moveToFirst();
				String name = ac.getString(ac
						.getColumnIndexOrThrow(People.DISPLAY_NAME));// People.NAME
				String userid = ac.getString(ac
						.getColumnIndexOrThrow(People._ID));// People._ID
				InputStream is = People.openContactPhotoInputStream(
						getContentResolver(), auri);
				Bitmap mBitmap = BitmapFactory.decodeStream(is);
				mContactID.add(userid);
				mContact.add(name);
				mContactPhoto.add(mBitmap);
			}
		}

		// Uri uri = null;
		// String columnName = null;
		// int os_version = Integer.parseInt(Build.VERSION.SDK.toString());
		// if (os_version > 4) {// 2.x，sdk版本
		// uri = Uri.parse("content://com.android.contacts/contacts");// new
		// // Uri("content://com.android.contacts/contacts");
		// columnName = "display_name";
		// } else {// 1.6以下SDK
		// uri = Contacts.People.CONTENT_URI;
		// columnName = Contacts.People.NAME;
		// }
		// String tmpNum = new String();// 记录临时号码
		// // 处理联系人和邮件
		// Cursor c = getContentResolver().query(uri, null,
		// null, null, null);// 查询所有包含content的名字
		//
		// //获取联系人姓名
		// for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
		// int uid = c.getInt(c.getColumnIndexOrThrow(People._ID));
		// Uri auri = ContentUris.withAppendedId(People.CONTENT_URI, uid);
		// Cursor ac = managedQuery(auri, null, null, null, null);
		// ac.moveToFirst();
		// String name =
		// ac.getString(ac.getColumnIndexOrThrow(People.DISPLAY_NAME));//
		// People.NAME
		// String userid = ac.getString(ac.getColumnIndexOrThrow(People._ID));//
		// People._ID
		// InputStream is = People.openContactPhotoInputStream(
		// getContentResolver(), auri);
		// Bitmap mBitmap = BitmapFactory.decodeStream(is);
		// mContactID.add(userid);
		// Log.e("reddial:uid", userid);
		// mContactURI.add(auri.toString());
		// mContact.add(name);
		// mContactPhoto.add(mBitmap);
		// }
	}

	// 重点在这里面
	public void onItemClick(AdapterView<?> parent, View view,
			final int position, long id) {
		// TODO Auto-generated method stub
		itempos = position;
		final List<String> num = new ArrayList<String>();
		if (mContactURI.get(position).toString().equalsIgnoreCase("")) {
			CharSequence[] cs = { "设置联系人" };
			AlertDialog opDialog = new AlertDialog.Builder(reddial.this)
					.setTitle("选项")
					.setItems(cs, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
//							int sdk = new Integer(Build.VERSION.SDK).intValue();
//							Uri uri = null;
//							if (sdk >= 5) {
//								try {
//									Class clazz = Class
//											.forName("android.provider.ContactsContract$Contacts");
//									uri = (Uri) clazz.getField("CONTENT_URI")
//											.get(clazz);
//								} catch (Throwable t) {
//									Log.e("reddial",
//											"Exception when determining CONTENT_URI",
//											t);
//								}
//							} else {
//								uri = Contacts.People.CONTENT_URI;
//							}
//							Intent i = new Intent(Intent.ACTION_PICK, uri);
//							startActivityForResult(i, STARTPICK_REQUEST);
							 Intent setting = new Intent();
							 setting.setClass(reddial.this, peoplelist.class);
							 startActivityForResult(setting, STARTPICK_REQUEST);
						}
					}).create();
			opDialog.show();
		} else {
			Cursor phones = getContentResolver().query(
					Phones.CONTENT_URI,
					null,
					Phones.PERSON_ID + "="
							+ mContactID.get(position).toString(), null, null);
			;
			while (phones.moveToNext()) {
				String phoneNumber = phones.getString(phones
						.getColumnIndex(People.NUMBER));
				String phoneNumbertype = phones.getString(phones
						.getColumnIndex(People.TYPE));
//				String type    = phones.getString( phones.getColumnIndexOrThrow( PhonesColumns.TYPE ));
				// 多个号码如何处理
				num.add(Contacts.Phones.getDisplayLabel(this, Integer.parseInt(phoneNumbertype), "")+":"+phoneNumber);
			}
			phones.close();
			num.add("设置联系人");

			CharSequence[] cs = num.toArray(new CharSequence[num.size()]);
			AlertDialog opDialog = new AlertDialog.Builder(reddial.this)
					.setTitle("选项")
					.setItems(cs, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if ((((AlertDialog)dialog).getListView().getCount()-1)!=which) {
								Uri uri = Uri.parse("tel:"
										+ num.get(which).toString());
								Intent it = new Intent(Intent.ACTION_CALL, uri);
								startActivity(it);
							} else {
								 Intent setting = new Intent();
								 setting.setClass(reddial.this, peoplelist.class);
								 startActivityForResult(setting, STARTPICK_REQUEST);								
							}
						}
					}).create();
			opDialog.show();
		}
	}

	public class AppsAdapter extends BaseAdapter {
		public AppsAdapter() {
		}

		public View getView(int position, View convertView, ViewGroup parent) {

        	// 从程序生成list里面的内容
//			LinearLayout layout = new LinearLayout(reddial.this);
//			layout.setOrientation(LinearLayout.VERTICAL);
//
//			String info = mContact.get(position);
//			Bitmap img = mContactPhoto.get(position);
//			layout.addView(addTitleView(img, info));
//			return layout;

        	// 从layout文件生成list里面的内容
            convertView = LayoutInflater.from(getApplicationContext()).inflate  
            (R.layout.listitem,null);  
              
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

		public View addTitleView(Bitmap image, String title) {
			LinearLayout layout = new LinearLayout(reddial.this);
			layout.setOrientation(LinearLayout.VERTICAL);

			ImageView iv = new ImageView(reddial.this);
			iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
			iv.setLayoutParams(new GridView.LayoutParams(60, 60));
			iv.setImageBitmap(image);

			layout.addView(iv);

			TextView tv = new TextView(reddial.this);
			// tv.setTransformationMethod(SingleLineTransformationMethod.getInstance());
			tv.setSingleLine(true);
			tv.setText(title);
			tv.setTextSize(18);

			layout.addView(tv, new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.FILL_PARENT));

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
			setting.setClass(reddial.this, Feedback.class);
			startActivity(setting);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private NotificationManager mNM;

	private void notification(Context ctx, String msginfo) {
		try {
			mNM = (NotificationManager) ctx
					.getSystemService(Context.NOTIFICATION_SERVICE);
			Intent intent = new Intent(ctx, reddial.class);
			CharSequence appName = ctx.getString(R.string.app_name);
			Notification notification = new Notification(R.drawable.icon,
					appName, System.currentTimeMillis());
			notification.flags = Notification.FLAG_NO_CLEAR;
			CharSequence appDescription = msginfo;
			notification.setLatestEventInfo(ctx, appName, appDescription,
					PendingIntent.getActivity(ctx, 0, intent,
							PendingIntent.FLAG_CANCEL_CURRENT));
			mNM.notify(0, notification);
		} catch (Exception e) {
			mNM = null;
		}
	}

	private void initProperty() {
		// initProperty("java.vendor.url", "java.vendor.url");
		// initProperty("java.class.path", "java.class.path");
		// initProperty("user.home", "user.home");
		// initProperty("java.class.version", "java.class.version");
		// initProperty("os.version", "os.version");
		// initProperty("java.vendor", "java.vendor");
		// initProperty("user.dir", "user.dir");
		// initProperty("user.timezone", "user.timezone");
		// initProperty("path.separator", "path.separator");
		// initProperty("os.name", "os.name");
		// initProperty("os.arch", "os.arch");
		// initProperty("line.separator", "line.separator");
		// initProperty("file.separator", "file.separator");
		// initProperty("user.name", "user.name");
		// initProperty("java.version", "java.version");
		// initProperty("java.home", "java.home");
		// //机器型号 HTC Magic
		// Build.MODEL
		// //SDK版本 8
		// Build.VERSION.SDK
		// //SDK版本号 2.2
		// Build.VERSION.RELEASE
	}

}