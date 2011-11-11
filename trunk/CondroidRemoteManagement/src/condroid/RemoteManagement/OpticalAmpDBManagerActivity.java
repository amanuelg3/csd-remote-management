package condroid.RemoteManagement;


import android.app.*;
import android.content.*;
import android.database.*;

import android.database.sqlite.*;
import android.graphics.drawable.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import condroid.RemoteManagement.RemoteSystemDatabase.*;

public class OpticalAmpDBManagerActivity extends Activity
{
	/* 
	 * DEVICE TYPE
	 */
	final static String DEVICE_OPAMP = "opamp";
	final static String DEVICE_WIFI = "wifi";
	final static String DEVICE_WATER_SENSOR = "water";
	final static String DEVICE_ETC = "etc";
	/*
	 * Column Index
	 */
	final static int COL_ID = 0;
	final static int COL_TYPE = 1;
	final static int COL_COMMAND = 2;
	
	/*
	 * Database classes
	 */
	private RemoteSystemDatabaseHelper mDBHelper;
	private SQLiteDatabase mCmdDatabase;	
	private Cursor mCursor = null;
	private DeviceCommandDB mDeviceCommandDB;

	/*
	 * Member variables
	 */
	String mDeviceType;		// Connected to radio group
	String mCommandName;	// Connected to Edit Text
	
	Long mCommandDBRowId;	// the number of rows which was inserted
	Long mSelectedItem;		// a selected item in ListView widget
	/*
	 * Widgets
	 */
	
	Button mOpAmpCmdAddButton;
	Button mOpAmpCmdClearButton;
	
	Button mOpAmpBackButton;	// Move back to OpticalAmpActivity
	Button mOpAmpDeleteButton;	// Delete Item
	
	EditText mCmdEditText;
	
	RadioGroup mRadioDevice;	// Radio Button for device types
	ListView mCommandDBList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.opamp_cmd);
		
		mDBHelper = new RemoteSystemDatabaseHelper(this);
		mCmdDatabase = mDBHelper.getWritableDatabase();

		mDeviceCommandDB = new DeviceCommandDB();
		
		mDeviceType = DEVICE_OPAMP;
		mCommandName = "";
		mCommandDBRowId = null;
		mSelectedItem = null;
		
		// Radio group checked change listener
		mRadioDevice = (RadioGroup)findViewById(R.id.opamp_radio_group_device);
		mRadioDevice.setOnCheckedChangeListener(mOpAmpCheckedChangeListener);
		
		// EditText
		mCmdEditText = (EditText)findViewById(R.id.opamp_cmd_text);		
		mCmdEditText.setText("");
		
		// Button click listener
		mOpAmpCmdAddButton = (Button)findViewById(R.id.opamp_cmd_add_button);
		mOpAmpCmdClearButton = (Button)findViewById(R.id.opamp_cmd_clear_button);
		
		mOpAmpCmdAddButton.setOnClickListener(mClickListener);
		mOpAmpCmdClearButton.setOnClickListener(mClickListener);
		
		// ListView in opamp_cmd.xml
		mCommandDBList = (ListView)findViewById(R.id.opamp_cmd_list);
		mCommandDBList.setOnItemClickListener(mCmdItemClickListener);
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(mCmdDatabase != null)
		{
			mCmdDatabase.close();
		}

		if(mDBHelper != null)
		{
			mDBHelper.close();
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
			
		mDeviceType = DEVICE_OPAMP;		// Default value
		
		showAllCommandList();
		
	}
	
	RadioGroup.OnCheckedChangeListener mOpAmpCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			if(group.getId() == R.id.opamp_radio_group_device)
			{
				switch(checkedId)
				{
				case R.id.opamp_radio_optical_amp:
					mDeviceType = DEVICE_OPAMP;
					break;
				case R.id.opamp_radio_wifi:
					mDeviceType = DEVICE_WIFI;
					break;
				case R.id.opamp_radio_water_sensor:
					mDeviceType = DEVICE_WATER_SENSOR;
					break;
				case R.id.opamp_radio_etc:
					mDeviceType = DEVICE_ETC;
					break;
					
				}
			}
			
		}
	};

	/*=============================================================================
	 * Name: mCmdItemClickListener
	 * 
	 * Description:
	 * 		- Callback function when a listview is clicked
	 * 		- ListView click listener
	 *=============================================================================*/	
	AdapterView.OnItemClickListener mCmdItemClickListener = new AdapterView.OnItemClickListener() 
	{
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Cursor cursor = null;
			String message = "Selected Command:" + "\n";
			
			mSelectedItem = id;
			cursor = queryCommandItem(id);
			startManagingCursor(cursor);

			String type = cursor.getString(COL_TYPE);
			String command = cursor.getString(COL_COMMAND);
			
			/*
			 * Store selected item to DeviceCommandDB's member variables 
			 */		
			mDeviceCommandDB.setDeviceCommandItem(type, command);
			
			
			message += type + " / " + command;
						
			AlertDialog.Builder alertDlg 
						= new AlertDialog.Builder(OpticalAmpDBManagerActivity.this);

			alertDlg.setTitle("Command database");
			alertDlg.setMessage(message);
			alertDlg.setIcon(R.drawable.icon);			
			alertDlg.setPositiveButton("Delete", mClickDialogButton);			
			alertDlg.setNegativeButton("Cancel", mClickDialogButton);
			alertDlg.setCancelable(false);
			
			alertDlg.show();
			

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
						
			// Delete Button => Move to Authentication.java (Authentication.xml)
			if(which == DialogInterface.BUTTON1)
			{
				Log.i("cmd", "Delete Selected Item: " + mSelectedItem);
				deleteCommandItem(mSelectedItem);				
			}
			
		}
	};

	Button.OnClickListener mClickListener = new View.OnClickListener() 
	{
	
		@Override
		public void onClick(View v) 
		{
			// TODO Auto-generated method stub
			switch(v.getId())
			{
			case R.id.opamp_cmd_add_button:
				// add a commnd to table_command
				insertCommandToDatabase();
				break;
			case R.id.opamp_cmd_clear_button:
				// Clear Command Edit Text widget
				clearCmdEditText();
				break;
			}
		}
	};

	/*=============================================================================
	 * Name: showAllRemoteSiteList
	 * 
	 * Description:
	 * 		- Extract RemoteSiteDB and display items
	 * 		
	 *=============================================================================*/			
	private void showAllCommandList()
	{
		mCursor = queryAllCommands(mCmdDatabase);
		Log.i("cmd", "DeviceCommandDB: Count= " + mCursor.getCount());		
		
		mCommandDBRowId = Long.valueOf(mCursor.getCount());
	
		mCursor.moveToFirst();
		startManagingCursor(mCursor);
		
		String[] from = new String[] {DeviceCommandDB.TYPE, DeviceCommandDB.COMMAND};
		
		 
		int[] to = new int[] {R.id.cmd_list_device_type, R.id.cmd_list_command_name};
		
		// ListView for displaying items in opamp_cmd_db.xml
		ListView listView = (ListView)findViewById(R.id.opamp_cmd_list);	
		
		ListAdapter Adapter = null;
		
		try {
			Adapter = new SimpleCursorAdapter(
										listView.getContext(),  
										R.layout.cmd_list,	// cmd_list.xml
										mCursor,	//Item으로 사용할 DB의 Cursor
										from,	//DB 필드 이름
										to);	//DB필드에 대응되는 xml TextView의 id
		}
		catch(Exception e)
		{
			Log.e("opamp", e.getMessage());
		}
		
		mCommandDBList.setAdapter(Adapter);
		mCommandDBList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mCommandDBList.setItemsCanFocus(true);
		mCommandDBList.setOnItemClickListener(mCmdItemClickListener);
		
		//db.close();
		
	}

	
	public void clearCmdEditText()
	{
		mCommandName = "";
		mCmdEditText.setText("");
		
		// Set default value and check optical ampl radio button
		mDeviceType = DEVICE_OPAMP;
		mRadioDevice.check(R.id.opamp_radio_optical_amp);

	}
	/*=============================================================================
	 * Name: showAlertErrorDialog
	 * 
	 * Description:
	 * 		Show an alert dialog when an error happens
	 *=============================================================================*/	
	public void showAlertErrorDialog(String title, String message)
	{
		AlertDialog.Builder alertDlg 
		= new AlertDialog.Builder(OpticalAmpDBManagerActivity.this);
	
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
	 * Name: insertCommandToDatabase
	 * 
	 * Description:
	 * 		insert one item to RemoteSiteDB database
	 *=============================================================================*/	
	public void insertCommandToDatabase()
	{
		long row = 0;
		String message = "";		
		Cursor cursor = null;
		
		mCommandName = mCmdEditText.getText().toString();
		
		if(mDeviceType.length() == 0)
			mDeviceType = DEVICE_OPAMP;

		if(mCommandName.length() == 0)
		{
			message = "Please input a command";
			showAlertErrorDialog("Error", message);
		}
		else
		{
			ContentValues values = createContentValues(mDeviceType, mCommandName);
			
			cursor = checkDuplicatedCommand(mCommandName);
			
			if(cursor == null)
			{
				// No duplicated data
				//long insert (String table, String nullColumnHack, ContentValues values)
				row = mCmdDatabase.insert(DeviceCommandDB.TABLE_NAME, null, values);
				mCommandDBRowId = row;
				
				Log.i("cmd", "insert[" + mCommandDBRowId + "] " + mCommandName);
			}
			else
			{
				Log.i("cmd", "update: " + mCommandName);
				
				//int update (String table, ContentValues values, 
				//				String whereClause, String[] whereArgs)
				String[] where = {mCommandName};
				mCmdDatabase.update(DeviceCommandDB.TABLE_NAME, values, 
								DeviceCommandDB.COMMAND + "=?", where);				
			}
		
			//db.close();
			clearCmdEditText();
		}
		showAllCommandList();
		
	}
	/*=============================================================================
	 * Name: checkDuplicatedCommand
	 * 
	 * Description:
	 * 		duplication check of command 
	 *=============================================================================*/	
	public Cursor checkDuplicatedCommand(String command)
	{
		Cursor cursor = null;
		
		String[] columns = new String[] {DeviceCommandDB.ID,
											DeviceCommandDB.TYPE,
											DeviceCommandDB.COMMAND};
		Log.i("cmd", "checkDuplicatedCommand");
		try
		{

			cursor = mCmdDatabase.query(DeviceCommandDB.TABLE_NAME, columns, 
									DeviceCommandDB.COMMAND + "=?", 
									new String[]{command}, null, null, null);
						
			if(cursor.getCount() == 0)
			{
				cursor = null;
			}
			else
			{
				while(cursor.moveToNext())
				{
					Log.e("cmd", "index: " + cursor.getString(0));
					Log.e("cmd", "type: " + cursor.getString(1));
					Log.e("cmd", "command: " + cursor.getString(2));				
				}
			}
			
		}
		catch(SQLiteException e)
		{
			cursor = null;
			Log.e("cmd", e.toString());
		}
		
		
		return cursor;
	}

	/*=============================================================================
	 * Name: createContentValues
	 * 
	 * Description:
	 * 		create ContentValues before insertion to database
	 *=============================================================================*/
	public ContentValues createContentValues(String type, String command)
	{
		ContentValues values = new ContentValues();
		
		values.put(DeviceCommandDB.TYPE, type);
		values.put(DeviceCommandDB.COMMAND, command);
		
		return values;
	}

	/*=============================================================================
	 * Name: queryAllCommands
	 * 
	 * Description:
	 * 		Query all items from DeviceCommandDB
	 *=============================================================================*/	
	private Cursor queryAllCommands(SQLiteDatabase db)
	{
		Cursor cursor = null;
		cursor = db.rawQuery(" select * from " + DeviceCommandDB.TABLE_NAME + " order by "
							+ DeviceCommandDB.DEFAULT_SORT_ORDER, null);
		Log.i("cmd", "queryAll: cursor= " + cursor);
		return cursor;
	}
	/*=============================================================================
	 * Name: queryRemoteSiteItem
	 * 
	 * Description:
	 * 		Query one item with row id from RemoteSiteDB
	 *=============================================================================*/	
	public Cursor queryCommandItem(long rowId) throws SQLException
	{
		Cursor cursor = null;
		String[] columns = new String[] {DeviceCommandDB.ID,
										DeviceCommandDB.TYPE,
										DeviceCommandDB.COMMAND };
		
		String id = Long.toString(rowId);
		
		try 
		{
			/*
			cursor = mCmdDatabase.query(true, DeviceCommandDB.TABLE_NAME, columns, 
										DeviceCommandDB.ID + " = " + rowId, 
										null, null, null, null, null);
			*/
			cursor = mCmdDatabase.query(DeviceCommandDB.TABLE_NAME, columns, 
					DeviceCommandDB.ID + " =? ", new String[] {id}, null, null, null);

			if(cursor != null)
			{
				cursor.moveToFirst();
			}
			else
			{
				Log.i("cmd", "cursor is null");
			}

		}
		catch(Exception e)
		{
			cursor = null;
			Log.e("cmd", e.getMessage());
		}
		
		return cursor;
	}
	/*======================================================================================
	 * Name: deleteRemoteSite
	 * 
	 * Description:
	 * 		delete a row from RemoteSiteDB database 
	 *=====================================================================================*/
	public void deleteCommandItem(Long rowId)
	{
		int deletedRow;

		String[] whereArgs = {rowId.toString()};
		
		// int delete(String table, String whereClause, String[] whereArgs);
		deletedRow = mCmdDatabase.delete(DeviceCommandDB.TABLE_NAME, 
										DeviceCommandDB.ID + "=?", 
										whereArgs);
		
		Log.i("cmd", "deleteCommandItem: " + deletedRow);
		
		mCursor.requery();		
	}
	

}
