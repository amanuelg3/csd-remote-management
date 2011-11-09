package condroid.RemoteManagement;

import android.database.sqlite.*;
import android.util.*;
import android.content.Context;

import condroid.RemoteManagement.RemoteSystemDatabase.RemoteSiteDB;
import condroid.RemoteManagement.RemoteSystemDatabase.OpticalAMP;

public class RemoteSystemDatabaseHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "crm_database.db";
    private static final int DATABASE_VERSION = 1;
        
    RemoteSystemDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i("remo", "Create Database : " + DATABASE_NAME);
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
		db.execSQL("CREATE TABLE " + RemoteSiteDB.TABLE_NAME+ " ("
                + RemoteSiteDB.ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + RemoteSiteDB.LOCATION + " TEXT,"
                + RemoteSiteDB.PHONE_NUMBER + " TEXT, "
                + RemoteSiteDB.DEVICE_ID + " TEXT" 
                + ");");
		
		Log.i("remo", "Create RemoteSiteDB table");
		
		// Create the DeviceNameDB table
		// CREATE TABLE table_optical_amp(
		//	id INTEGER PRIMARY KEY AUTOINCREMENT,
		//	command TEXT NOT NULL);

		//공백 주의: 반드시 변수명과 타입사이에는 공백이 있어야 함.
		
		db.execSQL("CREATE TABLE " + OpticalAMP.TABLE_NAME + " ("
				+ OpticalAMP.ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
				+ OpticalAMP.COMMAND + " TEXT"
				+ ");");
				
		Log.i("remo", "Created: RemoteSiteDB, DeviceNameDB");
	
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + RemoteSiteDB.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + OpticalAMP.TABLE_NAME);
		onCreate(db);
		
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
