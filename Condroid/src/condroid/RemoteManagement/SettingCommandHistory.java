package condroid.RemoteManagement;


import android.app.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import condroid.RemoteManagement.RemoteSystemDatabase.*;

public class SettingCommandHistory extends Activity{
	private static final String CLASS_NAME = "CommandHistory";
	
	/*
	 * Widgets
	 */
	ListView mCmdHistoryListView;
	
	/*
	 * Database classes and variables
	 */
	private RemoteSystemDatabaseHelper mDBHelper;
	private SQLiteDatabase mCmdHistoryDatabase;	
	private Cursor mCursor = null;
	private CommandHistoryDB mCommandHistoryDBClass;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_command_history);
		
		// Init Database
		mDBHelper = new RemoteSystemDatabaseHelper(this);
		mCmdHistoryDatabase = mDBHelper.getWritableDatabase();
		mCommandHistoryDBClass = new CommandHistoryDB();

		
		mCmdHistoryListView = (ListView)findViewById(R.id.setting_command_history_listview);
		mCmdHistoryListView.setOnItemClickListener(mCmdHistoryItemClickListener);
		
		Button deleteButton = (Button)findViewById(R.id.setting_history_delete_button);
		deleteButton.setOnClickListener(mClickListener);
		
		Button backButton = (Button)findViewById(R.id.setting_history_back_button);
		backButton.setOnClickListener(mClickListener);
		
		ImageButton homeButton = (ImageButton)findViewById(R.id.history_home_image_button);
		homeButton.setOnClickListener(mClickListener);
		
		
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(mCmdHistoryDatabase != null)
		{
			mCmdHistoryDatabase.close();
		}

		if(mDBHelper != null)
		{
			mDBHelper.close();
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		showAllCommandHistoryList();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	Button.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v)
		{
			switch(v.getId())
			{
			case R.id.setting_history_delete_button:
				Toast.makeText(getApplicationContext(), 
								"Deleting all command history...", 
								Toast.LENGTH_SHORT).show();
				
				// Delete all records in CommandHistoryDB
				mCmdHistoryDatabase.delete(CommandHistoryDB.TABLE_NAME, null, null);
				mCursor.requery();
				break;
			case R.id.setting_history_back_button:
				startActivity(new Intent(SettingCommandHistory.this, Settings.class));
				finish();
				break;
			case R.id.history_home_image_button:
				startActivity(new Intent(SettingCommandHistory.this, Home.class));
				finish();
				break;
			}

		}
	};

	AdapterView.OnItemClickListener mCmdHistoryItemClickListener = 
			new AdapterView.OnItemClickListener() 
	{
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Cursor cursor = null;
			String message = "Selected Command:" + "\n";
		}
	}; 
	/*=============================================================================
	 * Name: showAllCommandHistoryList
	 * 
	 * Description:
	 * 		- display all records in CommandHistoryDB
	 * 		- Date | Time | Phone Number | Command |
	 *=============================================================================*/			
	private void showAllCommandHistoryList()
	{
		mCursor = queryAllCommandHistory(mCmdHistoryDatabase);
		Log.i(CLASS_NAME, "DeviceCommandDB: Count= " + mCursor.getCount());		
	
		mCursor.moveToFirst();
		startManagingCursor(mCursor);
		
		String[] from = new String[] {
										CommandHistoryDB.DATE,
										CommandHistoryDB.TIME,
										CommandHistoryDB.PHONE,
										CommandHistoryDB.COMMAND};
		
		 
		int[] to = new int[] {
								R.id.command_history_date, 
								R.id.command_history_time,
								R.id.command_history_phone,
								R.id.command_history_command};
		
		// ListView widget for displaying items in "setting_command_history.xml"
		ListView listView = (ListView)findViewById(R.id.setting_command_history_listview);	
		
		ListAdapter Adapter = null;
		
		try {
			Adapter = new SimpleCursorAdapter(
										listView.getContext(),  
										R.layout.setting_command_history_list,	// setting_command_history_list.xml
										mCursor,	//Item으로 사용할 DB의 Cursor
										from,	//DB 필드 이름
										to);	//DB필드에 대응되는 xml TextView의 id
		}
		catch(Exception e)
		{
			Log.e(CLASS_NAME, e.getMessage());
		}
		
		mCmdHistoryListView.setAdapter(Adapter);
		mCmdHistoryListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mCmdHistoryListView.setItemsCanFocus(true);
			
	
		
	}


	/*=============================================================================
	 * Name: queryAllCommandHistory
	 * 
	 * Description:
	 * 		Query all records from CommandHistoryDB
	 *=============================================================================*/	
	private Cursor queryAllCommandHistory(SQLiteDatabase db)
	{
		Cursor cursor = null;
		cursor = db.rawQuery(" select * from " + CommandHistoryDB.TABLE_NAME + " order by "
							+ CommandHistoryDB.DEFAULT_SORT_ORDER, null);
	
		return cursor;
	}

}
