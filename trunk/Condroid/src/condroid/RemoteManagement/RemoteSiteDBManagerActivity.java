package condroid.RemoteManagement;


import android.app.*;
import android.content.*;


import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;


import android.database.*;
import android.database.sqlite.SQLiteDatabase;

import condroid.RemoteManagement.RemoteSystemDatabase.*;


public class RemoteSiteDBManagerActivity extends Activity{
	private static final String CLASS_NAME = "RemoteSiteDB";
	/*
	final static int DEVICE_OPAMP = 0x1;
	final static int DEVICE_WIFI = 0x2;
	final static int DEVICE_WATER_SENSOR = 0x4;
	final static int DEVICE_ETC = 0x8;
	*/
	/*
	 * Device Name
	 */
	final static String DEVICE_OPAMP = "opamp";
	final static String DEVICE_WIFI = "wifi";
	final static String DEVICE_WATER_SENSOR = "water";
	final static String DEVICE_ETC = "etc";
	
	/*
	 * Column Index
	 */
	final static int COL_ID = 0;
	final static int COL_LOCATION = 1;
	final static int COL_PHONENUMBER = 2;
	final static int COL_DEVICEID = 3;

	/*
	 * Database classes
	 */
	RemoteSystemDatabaseHelper mDBHelper;
	SQLiteDatabase mDatabase;	
	public Cursor mCursor = null;
	
	RemoteSiteDB mRemoteSiteItem;
	//DeviceCommandDB mDeviceCommandDB;
	Long mRemoteDBRowId;	// the number of rows which was inserted
	
	/*
	 * Widgets
	 */
	// Radio Group Control
	RadioGroup mRadioDevice;
	
	/*
	 * EditText widgets
	 */
	EditText editLocation;
	EditText editPhoneNumber;
	
	/*
	 * Remote Site Information
	 */
	String mRemoteLocation;
	String mRemotePhoneNumber;
	String mRemoteDeviceType;		// Device Type (OR operation)
	
	
	ListView mRemoteSiteList;	
	long mSelectedId;				// ListView selected id
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remote_site);
		
				
		mDBHelper = new RemoteSystemDatabaseHelper(this);
		mDatabase = mDBHelper.getWritableDatabase();

		mRemoteSiteItem = new RemoteSiteDB();	
		//mDeviceCommandDB = new DeviceCommandDB();
		
		mRemoteDBRowId = null;
		mSelectedId = 0;
		
		// ListView for RemoteSite database
		mRemoteSiteList = (ListView)findViewById(R.id.remote_site_list);
		mRemoteSiteList.setOnItemClickListener(mItemClickListener);
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		//차후 구현
		super.onPause();
		

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	
		
		mRemoteLocation = "";
		mRemotePhoneNumber = "";
		mRemoteDeviceType = DEVICE_OPAMP;

	
		// Display RemoteSiteDB to ListBox when this activity starts
		showAllRemoteSiteList();	  
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(mDBHelper != null)
		{
			mDBHelper.close();
		}
		
		if(mDatabase != null)
		{
			mDatabase.close();
		}

	}
	
	/*=============================================================================
	 * Name: showAllRemoteSiteList
	 * 
	 * Description:
	 * 		- Extract RemoteSiteDB and display items
	 * 		
	 *=============================================================================*/			
	public void showAllRemoteSiteList()
	{
		Log.i(CLASS_NAME, "showAllRemoteSiteList");
		
		mCursor = queryAllRemoteSite(mDatabase);
		
		mRemoteDBRowId = Long.valueOf(mCursor.getCount());
		Log.i(CLASS_NAME, "RemoteSiteDB: Count= " + mCursor.getCount() + "/ " + mRemoteDBRowId);	
	
		mCursor.moveToFirst();
		startManagingCursor(mCursor);
		
		String[] from = new String[] {RemoteSiteDB.LOCATION, 
									RemoteSiteDB.PHONE_NUMBER,
									RemoteSiteDB.DEVICE_ID };
		
		int[] to = new int[] {R.id.remote_site_list_location, 
							R.id.remote_site_list_phone, 
							R.id.remote_site_list_device};
		 
		ListView listView = (ListView)findViewById(R.id.remote_site_list);
		
		//SimpleCursorAdapter Adapter = null;
		ListAdapter Adapter = null;
	
		try {
			Adapter = new SimpleCursorAdapter(
										listView.getContext(),  
										R.layout.remote_site_list,
										mCursor,	//Item으로 사용할 DB의 Cursor
										from,	//DB 필드 이름
										to);	//DB필드에 대응되는 xml TextView의 id
		}
		catch(Exception e)
		{
			Log.e(CLASS_NAME, e.getMessage());
		}
		
		mRemoteSiteList.setAdapter(Adapter);
		mRemoteSiteList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mRemoteSiteList.setItemsCanFocus(true);
		mRemoteSiteList.setOnItemClickListener(mItemClickListener);
		
		//db.close();
		
	}

	
	/*=============================================================================
	 * Name: mItemClickListener
	 * 
	 * Description:
	 * 		- Callback function when a listview is clicked
	 * 		- ListView click listener
	 *=============================================================================*/	
	AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() 
	{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
		{
			final long deleteId = id;
			Cursor cursor = null;
			String message = "Selected site:" + "\n";
			 
			Log.i(CLASS_NAME, "ItemClickListener: " + deleteId);
			
			mSelectedId = id;
			
			cursor = queryRemoteSiteItem(deleteId);
			startManagingCursor(cursor);
			
			String location = cursor.getString(COL_LOCATION);
			String phonenumber = cursor.getString(COL_PHONENUMBER);
			String deviceid = cursor.getString(COL_DEVICEID);
			
			/*
			 * Store selected item to RemoteSiteDB's member variables 
			 */		
			mRemoteSiteItem.setRemoteSiteItem(location, phonenumber, deviceid);
			
			message += location + " / " + phonenumber + " / " + deviceid;
						
			AlertDialog.Builder alertDlg 
						= new AlertDialog.Builder(RemoteSiteDBManagerActivity.this);

			alertDlg.setTitle("Remote Site Information");
			alertDlg.setMessage(message);
			alertDlg.setIcon(R.drawable.icon);			
			alertDlg.setPositiveButton("Select", mClickDialogButton);			
			//alertDlg.setNegativeButton("Delete", mClickDialogButton);
			alertDlg.setCancelable(false);
			
			alertDlg.show();
			
			//cursor.close();
		}
		
	}; 
	
	/*=============================================================================
	 * Name: mClickDialogButton
	 * 
	 * Description:
	 * 		- Callback function of alert dialog's buttons
	 * 		- OK, Delete Button in the alert dialog
	 *=============================================================================*/	
	DialogInterface.OnClickListener mClickDialogButton = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
						
			// Ok Button => Move to Authentication.java (Authentication.xml)
			if(which == DialogInterface.BUTTON1)
			{
				
				Intent intent = new Intent(RemoteSiteDBManagerActivity.this, Authentication.class);
				
				String[] selectedValues = mRemoteSiteItem.getRemoteSiteItem();
				
				Log.i(CLASS_NAME, "Selected OK Item: " 
							+ selectedValues[0] + " "
							+ selectedValues[1] + " "
							+ selectedValues[2]);

				
				intent.putExtra("remote_site", selectedValues);
				//startActivity(intent);
				setResult(RESULT_OK, intent);
				
				finish();
			}
			else
			{
				// Delete Button
				//Log.i(CLASS_NAME, "Delete Selected Item: " + mSelectedId);
				//deleteRemoteSite(mSelectedId);
			}
			
		}
	};
	
	/*=============================================================================
	 * Name: showAlertErrorDialog
	 * 
	 * Description:
	 * 		Show an alert dialog when an error happens
	 *=============================================================================*/	
	public void showAlertErrorDialog(String title, String message)
	{
		AlertDialog.Builder alertDlg 
		= new AlertDialog.Builder(RemoteSiteDBManagerActivity.this);
	
		alertDlg.setTitle(title);
		alertDlg.setMessage(message);
		alertDlg.setIcon(R.drawable.icon);
		alertDlg.setPositiveButton("Close", null);
		alertDlg.show();
	}
	

	
	/*=============================================================================
	 * For Database Operation Functions
	 *=============================================================================*/
	/*=============================================================================
	 * Name: insertSiteToDatabase
	 * 
	 * Description:
	 * 		insert one item to RemoteSiteDB database
	 *=============================================================================*/	
	public void insertSiteToDatabase()
	{
		long row = 0;
		String message = "";		
		Cursor cursor = null;
		
		mRemoteLocation = editLocation.getText().toString();
		mRemotePhoneNumber = editPhoneNumber.getText().toString();
		if(mRemoteDeviceType.length() == 0)
			mRemoteDeviceType = DEVICE_OPAMP;
		
		// EditText validation check
		if(mRemoteLocation.length()==0 || mRemotePhoneNumber.length()==0)
		{
			if(mRemoteLocation.length()==0)
				message="Please input location field.";
			else if(mRemotePhoneNumber.length()==0)
				message="Please input phone number field.";
			
			showAlertErrorDialog("Error", message);	
		}
		else
		{
			
			ContentValues values = createContentValues(mRemoteLocation,
														mRemotePhoneNumber,
														mRemoteDeviceType);
			
			cursor = checkDuplicatedPhoneNumber(mRemotePhoneNumber);
			Log.i(CLASS_NAME, "cursor: " + cursor);
			if(cursor == null)
			{
				// No duplicated data
				row = mDatabase.insert(RemoteSiteDB.TABLE_NAME, null, values);
				mRemoteDBRowId = row;
				Log.i(CLASS_NAME, "insert: " + mRemoteDBRowId);
			}
			else
			{
				Log.i(CLASS_NAME, "update: " + mRemotePhoneNumber);
				mDatabase.update(RemoteSiteDB.TABLE_NAME, values, 
								RemoteSiteDB.PHONE_NUMBER + "=" + mRemotePhoneNumber, null);				
			}
		
			//db.close();
			
		}
		showAllRemoteSiteList();
	}
	/*=============================================================================
	 * Name: checkDuplicatedPhoneNumber
	 * 
	 * Description:
	 * 		duplication check of phone number 
	 *=============================================================================*/	
	public Cursor checkDuplicatedPhoneNumber(String number)
	{
		Cursor cursor = null;
		
		String[] columns = new String[] { RemoteSiteDB.ID, 
											RemoteSiteDB.LOCATION,
										 	RemoteSiteDB.PHONE_NUMBER, 
										 	RemoteSiteDB.DEVICE_ID};
		//Cursor query (String table, String[] columns, 
		//				String selection, String[] selectionArgs, 
		//				String groupBy, String having, String orderBy)
		
		cursor = mDatabase.query(RemoteSiteDB.TABLE_NAME, columns, 
								RemoteSiteDB.PHONE_NUMBER + "=" + number,
								null, null, null, null, null);
		
		Log.e(CLASS_NAME, "cursor.getCount(): " + cursor.getCount());
		if(cursor.getCount() == 0)
		{
			cursor = null;
		}
		else
		{
			while(cursor.moveToNext())
			{
				String index = cursor.getString(0);
				String loc = cursor.getString(1);
				String phone = cursor.getString(2);
				Log.e(CLASS_NAME, "index: " + index );
				Log.e(CLASS_NAME, "loc: " + loc);
				Log.e(CLASS_NAME, "phone: " + phone);
				
			}
		}
		return cursor;
	}
	/*=============================================================================
	 * Name: updateRemoteSite
	 * 
	 * Description:
	 * 		update a RemoteSite item on database 
	 *=============================================================================*/	
	public void updateRemoteSite(long rowId, String location, String phonenum, String device_id)
	{
		ContentValues updateValues = createContentValues(location, phonenum, device_id);
		
		// public int update (String table, ContentValues values, 
		//						String whereClause, String[] whereArgs)
		
		mDatabase.update(RemoteSiteDB.TABLE_NAME, updateValues, 
				RemoteSiteDB.ID + "=" + rowId, null);
	}

	/*=============================================================================
	 * Name: createContentValues
	 * 
	 * Description:
	 * 		create ContentValues before insertion to database
	 *=============================================================================*/
	public ContentValues createContentValues(String location, String phonenum, String id)
	{
		ContentValues values = new ContentValues();
		
		values.put(RemoteSiteDB.LOCATION, location);
		values.put(RemoteSiteDB.PHONE_NUMBER, phonenum);
		values.put(RemoteSiteDB.DEVICE_ID, id);
			
		return values;
	}

	/*=============================================================================
	 * Name: queryAllRemoteSite
	 * 
	 * Description:
	 * 		Query all items from RemoteSiteDB
	 *=============================================================================*/	
	public Cursor queryAllRemoteSite(SQLiteDatabase db)
	{
		
		return db.rawQuery("SELECT * FROM " + RemoteSiteDB.TABLE_NAME + " ORDER BY "
							+ RemoteSiteDB.DEFAULT_SORT_ORDER, null);
	}
	/*=============================================================================
	 * Name: queryRemoteSiteItem
	 * 
	 * Description:
	 * 		Query one item with row id from RemoteSiteDB
	 *=============================================================================*/	
	public Cursor queryRemoteSiteItem(long rowId) throws SQLException
	{
		Cursor cursor = null;
		String[] columns = new String[] { 
											RemoteSiteDB.ID,
											RemoteSiteDB.LOCATION,
											RemoteSiteDB.PHONE_NUMBER,
											RemoteSiteDB.DEVICE_ID
											};
		
		try 
		{
			cursor = mDatabase.query(true, RemoteSiteDB.TABLE_NAME, columns, 
									RemoteSiteDB.ID + " = " + rowId, 
									null, null, null, null, null);
			if(cursor != null)
			{
				cursor.moveToFirst();
			}
			else
			{
				Log.i(CLASS_NAME, "cursor is null");
			}

		}
		catch(Exception e)
		{
			Log.e(CLASS_NAME, e.getMessage());
			
		}
		
		return cursor;
	}
	/*======================================================================================
	 * Name: deleteRemoteSite
	 * 
	 * Description:
	 * 		delete a row from RemoteSiteDB database 
	 *=====================================================================================*/
	public void deleteRemoteSite(long rowId)
	{
		int deletedRow;
		deletedRow = mDatabase.delete(RemoteSiteDB.TABLE_NAME, RemoteSiteDB.ID + "=" + rowId, null);
		Log.i(CLASS_NAME, "deleteRemoteSite: " + deletedRow);
		
		mCursor.requery();
		
	}
	

}
