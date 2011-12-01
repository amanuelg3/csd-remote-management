package condroid.RemoteManagement;

import android.database.sqlite.*;
import android.util.*;
import android.content.Context;

import condroid.RemoteManagement.RemoteSystemDatabase.RemoteSiteDB;
import condroid.RemoteManagement.RemoteSystemDatabase.DeviceCommandDB;
import condroid.RemoteManagement.RemoteSystemDatabase.CommandHistoryDB;

public class RemoteSystemDatabaseHelper extends SQLiteOpenHelper{
	private static final String CLASS_NAME = "DBHelper";
    private static final String DATABASE_NAME = "crm_database.db";
    private static final int DATABASE_VERSION = 1;
    /*
    private static final String CREATE_TABLE_1 =
    	" create table " + table1 +
    	" (_id integer primary key autoincrement," +
    	" title text not null, body text not null);";

    	private static final String CREATE_TABLE_2 =
    	" create table " + TAGS_TABLE +
    	" (_id integer primary key autoincrement," +
    	" tagName text not null)";
    */	
    private static final String CREATE_REMOTE_SITE_TABLE = 
    				" create table " + RemoteSiteDB.TABLE_NAME + 
    				" (" + RemoteSiteDB.ID + " integer primary key autoincrement," +
    					RemoteSiteDB.LOCATION + " text not null, " +
    					RemoteSiteDB.PHONE_NUMBER + " text not null, " +
    					RemoteSiteDB.DEVICE_ID + " text not null);";
    
    private static final String CREATE_DEVICE_CMD_TABLE = 
    				" create table " + DeviceCommandDB.TABLE_NAME + 
    				" (" + DeviceCommandDB.ID + " integer primary key autoincrement," +
    					DeviceCommandDB.TYPE + " text not null, " +
    					DeviceCommandDB.COMMAND + " text not null);";
    
    private static final String CREATE_COMMAND_HISTORY_TABLE = 
    				" create table " + CommandHistoryDB.TABLE_NAME +
    				" (" + CommandHistoryDB.ID + " integer primary key autoincrement," +
    					CommandHistoryDB.DATE + " text not null, " +
    					CommandHistoryDB.TIME + " text not null, " +
    					CommandHistoryDB.PHONE + " text not null, " +
    					CommandHistoryDB.COMMAND + " text not null);";
     
    public RemoteSystemDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i(CLASS_NAME, "Create Database : " + DATABASE_NAME);
    }
 
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		// Create the RemoteSiteDB table
		// SQL =>
		// CREATE TABLE table_remote_site(
		//	id INTEGER PRIMARY KEY AUTOINCREMENT,
		//	remote_site_location TEXT NOT NULL,
		//	remote_site_phone INTEGER,
		//	remote_site_device_id INTEGER);
		//공백 주의: 반드시 변수명과 타입사이에는 공백이 있어야 함.
		try
		{
			db.execSQL(CREATE_REMOTE_SITE_TABLE);
			db.execSQL(CREATE_DEVICE_CMD_TABLE);
			db.execSQL(CREATE_COMMAND_HISTORY_TABLE);
		}
		catch(SQLiteException e) 
		{
			Log.e(CLASS_NAME,e.toString());
		}
				
		Log.i(CLASS_NAME, "Created tables: RemoteSiteDB, DeviceCommandDB, CommandHistroyDB");
		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + RemoteSiteDB.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + DeviceCommandDB.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + CommandHistoryDB.TABLE_NAME);
		onCreate(db);
		
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
