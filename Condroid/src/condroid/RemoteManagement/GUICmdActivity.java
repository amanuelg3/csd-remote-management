package condroid.RemoteManagement;

import java.util.*;

import android.app.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;

import android.os.*;
import android.telephony.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import condroid.RemoteManagement.RemoteSystemDatabase.*;

public class GUICmdActivity extends Activity {
	
	private static final String CLASS_NAME = "GUICmdActivity";
	
	final static int MSG_ID_SENT = 0;	
	final static int MSG_ID_RECEIVED = 1;
	final static int MSG_ID_ERROR = 2;
	final static int MSG_ID_LOGOUT = 3;

	//final long LOGOUT_TIME = 5000;
	
	/*
	 * Database classes
	 */
	private RemoteSystemDatabaseHelper mDBHelper;
	private SQLiteDatabase mCmdDatabase;			// GUI Command Database
	private SQLiteDatabase mCmdHistoryDatabase;		// Command Histoty Database
	private Cursor mCursor = null;
	private Cursor mHistoryCursor = null;
	
	private DeviceCommandDB mDeviceCommandDB;
	private CommandHistoryDB mCommandHistoryDBClass;
	
	static private int mSavedRecordNumber=1;
	private boolean isAboveMax = false;
	/*
	 * SMS variables
	 */
	BroadcastReceiver smsMsgSent = null;
	BroadcastReceiver smsMsgIncoming = null;

	SMSData smsData;
	private int msgType = 0;
	/*
	 * Remote Site Information 
	 */
	private String mLocation;
	private String mPhoneNumber;
	private String mDevice;
	
	/*
	 * Passcode
	 */
	private String mAuthorizedPassCode;
	
	/*
	 * SMS Message
	 */
	String mSelectedCommand;	// Command selected in Spinner widget
	String mSender="";
	/*
	 * TextView Widgets
	 */
	TextView mLocationTextView;
	TextView mPhoneNumberTextView;
	TextView mDeviceTextView;
	TextView mResultTextView;
	
	/*
	 * Spinner
	 */
	//Spinner mOpAmpCommandSpinner;
	Spinner mCommandSpinner;
	
	boolean isLogoutAckReceived;	
	Handler mTimerHandler;

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guicmd);
		
		mDBHelper = new RemoteSystemDatabaseHelper(this);
		mCmdDatabase = mDBHelper.getReadableDatabase();

		// Command History database
		mCmdHistoryDatabase = mDBHelper.getWritableDatabase();
		
		mDeviceCommandDB = new DeviceCommandDB();
		mCommandHistoryDBClass = new CommandHistoryDB();
		
		smsData = new SMSData();
		
		
		/*
		 * TestView widgets
		 */
		mLocationTextView = (TextView)findViewById(R.id.guicmd_location);
		mPhoneNumberTextView = (TextView)findViewById(R.id.guicmd_phonenumber);
		mDeviceTextView = (TextView)findViewById(R.id.guicmd_device);
		
		mResultTextView = (TextView)findViewById(R.id.guicmd_result);
		mResultTextView.setText("");
		/*
		 * Buttons
		 */
		Button guiCmdSendButton = (Button)findViewById(R.id.guicmd_send_button);
		guiCmdSendButton.setOnClickListener(mGuiCmdClickListener);
		
		/*
		mGuiCmdEditButton = (Button)findViewById(R.id.guicmd_edit_button);
		mGuiCmdEditButton.setOnClickListener(mGuiCmdClickListener);
		*/
		
		Button guiCmdLogoutButton = (Button)findViewById(R.id.guicmd_logout_button);
		guiCmdLogoutButton.setOnClickListener(mGuiCmdClickListener);
		
		ImageButton guiHomeButton = (ImageButton)findViewById(R.id.gui_home_image_button);
		guiHomeButton.setOnClickListener(mGuiCmdClickListener);
		
		/*
		 * Spinner
		 */
		mCommandSpinner = (Spinner)findViewById(R.id.guicmd_spinner);
		mCommandSpinner.setOnItemSelectedListener(mItemSelectedListener);
		// Display remote site information which was selected in Authentication process
		
		initMemberVariables();
		getRemoteSiteInfo();
		showRemoteSiteInfo();
		
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		
		// Connect database item to Spinner widget
		Log.i(CLASS_NAME, "showAllCommandListToSpinner");
		showAllCommandListToSpinner();
		
		/*==================================================================
		 * SMS Receiver Function: smsMsgIncoming
		 * 
		 *==================================================================*/

		smsMsgIncoming = new BroadcastReceiver()
		{
			
			public void onReceive(Context context, Intent intent) 
			{
				// TODO Auto-generated method stub
        		SmsMessage[] msgs = null;
                
        		String recvMessage = "";
        	        		
        		Bundle data = intent.getExtras();        		
        		if (data != null) 
        		{
        			Log.i(CLASS_NAME, "Authentication.java: Bundle data received");
        			
                    // SMS uses a data format known as a PDU
                    Object pdus[] = (Object[]) data.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                                      
                    for (int i=0; i<msgs.length; i++)
                    {
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);      
                        mSender = msgs[i].getOriginatingAddress();                                             
                        recvMessage += msgs[i].getMessageBody().toString();                              
                    }
  
                    Log.i(CLASS_NAME, recvMessage);
                    
                    processReceivedMessage(recvMessage);
        		}
        		else
        		{
        			Log.e(CLASS_NAME, "Received Data is null");
        		}        		
			}	// onReceive()			
		};	// BroadcastReceiver()
		
		registerReceiver(smsMsgIncoming, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
		
	}

	@Override
	protected void onPause() 
	{
	
		try 
		{
			if(smsMsgSent != null) 
			{
				unregisterReceiver(smsMsgSent);
				smsMsgSent = null;
			}
		}
		catch(Exception e) 
		{
			Log.e(CLASS_NAME, "Failed to unregister smsMsgSent");
		}
		
		
		try 
		{
			if(smsMsgIncoming != null) 
			{
				unregisterReceiver(smsMsgIncoming);
				smsMsgIncoming = null;
			}
		}
		catch(Exception e) 
		{
			Log.e(CLASS_NAME, "Failed to unregister smsMsgIncoming");
		}

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		try 
		{
			if(smsMsgSent != null) 
			{
				unregisterReceiver(smsMsgSent);
				smsMsgSent = null;
				Log.i(CLASS_NAME, "unregisterReceiver(smsMsgSent");
			}
		}
		catch(Exception e) 
		{
			Log.e(CLASS_NAME, "Failed to unregister smsMsgSent");
		}
		
		
		try 
		{
			if(smsMsgIncoming != null) 
			{
				unregisterReceiver(smsMsgIncoming);
				smsMsgIncoming = null;
				Log.i(CLASS_NAME, "unregisterReceiver(smsMsgIncoming");
			}
		}
		catch(Exception e) 
		{
			Log.e(CLASS_NAME, "Failed to unregister smsMsgIncoming");
		}

		if(mCmdDatabase != null)
		{
			mCmdDatabase.close();
		}
		
		if(mCmdHistoryDatabase != null)
		{
			mCmdHistoryDatabase.close();
		}

		if(mDBHelper != null)
		{
			mDBHelper.close();
		}

		super.onDestroy();
	}
	/*
	 * Button Click Listener
	 */
	Button.OnClickListener mGuiCmdClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId())
			{
			case R.id.guicmd_send_button:
				// Send SMS to the Remote Management System
				msgType = SMSData.LOGIN_NORMAL_MSG;
				sendCommandToRemoteSystem(msgType);
				break;
/*				
			case R.id.guicmd_edit_button:
				// Move to OpticalAMPDBManagerActivity class
				Intent intent = new Intent(GUICmdActivity.this, GUICmdDBManagerActivity.class);
				startActivity(intent);
				finish();
				break;
*/				
			case R.id.guicmd_logout_button:
				msgType = SMSData.LOGOUT;
				sendCommandToRemoteSystem(msgType);
				break;
			case R.id.gui_home_image_button:
				startActivity(new Intent(GUICmdActivity.this, Home.class));
				finish();
			}
			
		}
	};
	
	/*
	 * Spinner widget: item selection listener
	 */
	private AdapterView.OnItemSelectedListener mItemSelectedListener = 
		new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Cursor cursor = (Cursor)(parent.getSelectedItem());
				
				if(cursor != null)
				{
					mSelectedCommand = cursor.getString(
											cursor.getColumnIndex(DeviceCommandDB.COMMAND));
					Log.d(CLASS_NAME, "mSelectedCommand: " + mSelectedCommand);
				}
	               
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		
		};
	/*===========================================================================
     * Function Name: getRemoteSiteInfo
     * 
     * Description
     * 		Get remote site information from SMSData class(static variables)  
     *===========================================================================*/
	private void getRemoteSiteInfo()
	{
		String[] remoteSiteInfo = new String[4];
		
		remoteSiteInfo = smsData.getRemoteSiteInformation();
		
		mLocation = remoteSiteInfo[0];
		mPhoneNumber = remoteSiteInfo[1];
		mDevice = remoteSiteInfo[2];
		mAuthorizedPassCode = remoteSiteInfo[3];
		
		Log.i(CLASS_NAME, "Pass Code : " + mAuthorizedPassCode);

	}
	/*===========================================================================
     * Function Name: showRemoteSiteInfo
     * 
     * Description
     * 		Display Remote site information chosen by an end-user  
     *===========================================================================*/
	private void showRemoteSiteInfo()
	{
		
		if(mLocation.length() != 0)
		{
			mLocationTextView.setText(mLocation);
		}
		else
		{
			Log.e(CLASS_NAME, "Remote Location is null.");
		}
		
		if(mPhoneNumber.length() != 0)
		{
			mPhoneNumberTextView.setText(mPhoneNumber);
		}
		else
		{
			Log.e(CLASS_NAME, "Remote Phone Number is null.");
		}
		
		if(mDevice.length() != 0)
		{
			mDeviceTextView.setText(mDevice);
		}
		else
		{
			Log.e(CLASS_NAME, "Remote Device is null.");
		}
		
		Log.i(CLASS_NAME, mLocation +" " + mPhoneNumber + " " + mDevice);
	}

	/*===========================================================================
     * Function Name: initMemberVariables
     * 
     * Description
     * 		Initialize member variables  
     *===========================================================================*/
	private void initMemberVariables()
	{
		mLocation = "";
		mPhoneNumber = "";
		mDevice = "";

		mAuthorizedPassCode = "";
				
		mSelectedCommand = "";
		isLogoutAckReceived = false;
		
	}
	/*===========================================================================
     * Function Name: processReceivedMessage
     * 
     * Description
     * 		process the received SMS from Remote Management System  
     *===========================================================================*/
	private void processReceivedMessage(String msg)
	{
        // *** Login field checking in any activities
		int login = 0;
		if(smsData.validationCheck(msg) == SMSData.SUCCESS)
		{
			login = smsData.checkLoginValue(msg);
			
			if(login == SMSData.LOGOUT)
			{
				isLogoutAckReceived = true;
				processLogout(MSG_ID_RECEIVED);
			}
			else
				showResultMessage(MSG_ID_RECEIVED, msg);
		}
		else
		{
			showResultMessage(MSG_ID_ERROR, "Received an invalied message(No CRM field)");
		}
	}
	/*===========================================================================
     * Function Name: processLogout
     * 
     * Description
     * 		- When a user receives Logout message from a remote server
     * 		- When a user clicks "Logout" button  
     *===========================================================================*/	
	private void processLogout(int type)
	{
		initMemberVariables();
		smsData.deletePassCode();
		smsData.setAuthenticationValue(false);		// check later: changsu
		smsData.resetRemoteSiteInformation();
		
		showResultMessage(type, "LOGOUT: return to Login screen.");
		startActivity(new Intent(GUICmdActivity.this, Login.class));
		finish();

	}
	
	/*===========================================================================
     * Function Name: showResultMessage
     * 
     * Description
     * 		Display messages to a TextView widget  
     *===========================================================================*/
	private void showResultMessage(int msgType, String msg)
	{
		if(msgType == MSG_ID_SENT)
		{
			mResultTextView.append("# " + msg);
			Log.i(CLASS_NAME, "Sent Msg: " + msg);
		}
		else if(msgType == MSG_ID_RECEIVED)
		{
			
			mResultTextView.append(">> " + msg);
			Log.i(CLASS_NAME, "Received Msg: " + msg);
		}
		else if(msgType == MSG_ID_LOGOUT)
		{
			mResultTextView.append("# Logout message " + msg);
		}
		else
		{
			
			mResultTextView.append("# Error " + msg);
			//Log.e(CLASS_NAME, "Error: " + msg);
		}
		mResultTextView.append("\n");
	}
	/*===========================================================================
     * Function Name: sendCommandToRemoteSystem
     * 
     * Description
     * 		Make a header and body before sending SMS to the Remote Management System  
     *===========================================================================*/
	private void sendCommandToRemoteSystem(int type)
	{
		String command = ""; 
		int len = 0;
		String header = "";
		
		
		if(mPhoneNumber.length() == 0)
		{
			showAlertErrorDialog("Error", "Phone number is null.");
		}
		else
		{
			len = mSelectedCommand.length();					
		
			if((type == SMSData.LOGIN_NORMAL_MSG && len > 0))
			{
				header = smsData.makeCommandHeader(mAuthorizedPassCode, type, len);								
				
				command = header + mSelectedCommand;
				
				sendSMStoRemoteSystem(mPhoneNumber, command);
				
				showResultMessage(MSG_ID_SENT, "Sent message(" + mPhoneNumber + "): "
				 					+ command);
				
				insertCommandToHistory(mSelectedCommand);
				/* Later use
				showResultMessage(MSG_ID_SENT, "Sent message(" + mPhoneNumber + "): "
	 					+ mSelectedCommand);
	 			*/
			}
			else if(type == SMSData.LOGOUT)
			{
				isLogoutAckReceived = false;			
				
				sendLogoutMessage(type, len);	
				insertCommandToHistory("Logout Sent");
				waitingLogoutAckMessage();
			}
			else
			{
				showAlertErrorDialog("Error", "Invalid GUI command!");
			}
			
			
		}
	}

	/*===========================================================================
     * Function Name: sendSMStoRemoteSystem
     * 
     * Description
     * Send SMS to the Remote Management System for delivering commands and parameters  
     *===========================================================================*/
    private void sendSMStoRemoteSystem(String phoneNumber, String message)
    {
    	
        Intent intentMsgSent = new Intent("ACTION_MSG_SENT");
        Intent intentMsgReceipt = new Intent("ACTION_MSG_RECEIPT");
        
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, intentMsgSent, 0);
        PendingIntent receiptIntent = PendingIntent.getBroadcast(this, 0, intentMsgReceipt, 0);
	
        
		if(smsMsgSent != null)
		{
			unregisterReceiver(smsMsgSent);
			Log.i(CLASS_NAME, "unregisterReceiver(receiverMsgSent)");
			smsMsgSent = null;
		}
		// 
		smsMsgSent = new BroadcastReceiver() 
		{
			
			public void onReceive(Context context, Intent intent) 
			{
				switch(getResultCode())
				{
				case Activity.RESULT_OK:
                    Log.i(CLASS_NAME, "Successful transmission!!");
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Log.e(CLASS_NAME, "Nonspecific Failure!!");
                    showResultMessage(MSG_ID_ERROR, "Sending SMS failed: Generic Failure!");
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Log.e(CLASS_NAME, "Radio is turned Off!!");
                    showResultMessage(MSG_ID_ERROR, "Sending SMS failed: Radio is turned off!");
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Log.e(CLASS_NAME, "PDU Failure");                    
                    showResultMessage(MSG_ID_ERROR, "Sending SMS failed: PDU Failure!");
                    break;
				}

			}
		};
		registerReceiver(smsMsgSent, new IntentFilter("ACTION_MSG_SENT"));
		
		SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentIntent, receiptIntent);
    }

	/*===========================================================================
     * Function Name: waitingLogoutAckMessage
     * 
     * Description
     * 		- wait for a logout response message within 1 minute
     * 		  
     *===========================================================================*/	    
    private void waitingLogoutAckMessage()
    {
    	mTimerHandler = new Handler()
    	{
    		int wait = 0;
    		
    		public void handleMessage(Message msg)
    		{
    			if(isLogoutAckReceived == false && wait < SMSData.LOGOUT_TIME)
    			{
    				mTimerHandler.sendEmptyMessageDelayed(0, 1000);
    				wait += 1000;
    				showResultMessage(MSG_ID_LOGOUT, 
    							String.valueOf((SMSData.LOGOUT_TIME - wait)/1000) 
    							+ " seconds left.");
    			}
    			else
    			{
    				//sendLogoutMessage(SMSData.LOGOUT, 0);
    				processLogout(MSG_ID_SENT);
    				finish();
    			}
    		}
    	};
    	
    	mTimerHandler.sendEmptyMessage(0);
    }

	/*===========================================================================
     * Function Name: sendLogoutMessage
     * 
     * Description
     * 		send a logout request message to a remote server  
     *===========================================================================*/	
	private void sendLogoutMessage(int type, int bodyLength)
	{
		String header = "";
		String logoutMsg = "";
		header = smsData.makeCommandHeader(mAuthorizedPassCode, type, bodyLength);
		logoutMsg = header;
		
		sendSMStoRemoteSystem(mPhoneNumber, logoutMsg);		
		showResultMessage(MSG_ID_SENT, "Sent a LOGOUT Message: " + logoutMsg);
	}


	/*=============================================================================
	 * Name: showAllRemoteSiteList
	 * 
	 * Description:
	 * 		- Extract RemoteSiteDB and display items
	 * 		
	 *=============================================================================*/			
	private void showAllCommandListToSpinner()
	{
		//mCursor = queryAllCommands(mCmdDatabase);
		Log.i(CLASS_NAME, "showAllCommandListToSpinner");
		if(mDevice.length() == 0)
		{
			Log.e(CLASS_NAME, "Device name is null.");
			showAlertErrorDialog("Error", "Device name is null.");			
		}
		else
		{
			mCursor = queryCommandbyDevice(mDevice);
			Log.i(CLASS_NAME, "mCursor: " + mCursor);
			if(mCursor.getCount() > 0)
			{
				startManagingCursor(mCursor);
				mCursor.moveToFirst();			
			
				String[] from = new String[] {DeviceCommandDB.COMMAND}; 
				int[] to = new int[] {android.R.id.text1};
				
				SimpleCursorAdapter Adapter = null;
				
				try {
					Adapter = new SimpleCursorAdapter(
												mCommandSpinner.getContext(),
												android.R.layout.simple_spinner_item,
												mCursor,	//Item으로 사용할 DB의 Cursor
												from,	//DB 필드 이름
												to);	//DB필드에 대응되는 xml TextView의 id
					Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				}
				catch(Exception e)
				{
					Log.e(CLASS_NAME, e.getMessage());
				}
				
				mCommandSpinner.setAdapter(Adapter);
				//mCommandSpinner.setSelection(0);
			}
		}
				
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
		Log.i(CLASS_NAME, "queryAll: cursor= " + cursor.getCount());
		return cursor;
	}
	/*=============================================================================
	 * Name: queryCommandbyDevice
	 * 
	 * Description:
	 * 		Query items of specific device type from DeviceCommandDB
	 *=============================================================================*/		
	private Cursor queryCommandbyDevice(String devType)
	{
		Cursor cursor = null;		
		String[] columns = new String[] { 
											DeviceCommandDB.ID,
											DeviceCommandDB.TYPE,
											DeviceCommandDB.COMMAND 
										};
		try 
		{
		    
			//Log.i(CLASS_NAME, "devType= " + devType);
			
			cursor = mCmdDatabase.query(DeviceCommandDB.TABLE_NAME, columns, 
										DeviceCommandDB.TYPE + "=?", new String[]{devType},	
										null, null, null);
			
			if(cursor.getCount() == 0)
			{
				//cursor = null;
				Log.i(CLASS_NAME, "Count is zero.");
			}
		}
		catch(SQLiteException e)
		{
			cursor = null;
			Log.e(CLASS_NAME, e.toString());	
		}
		return cursor;
	}
	/*###############################################################################
	 * 
	 * Database Management module for Command History 
	 * 
	 *###############################################################################*/
	/*=============================================================================
	 * Name: createContentValues
	 * 
	 * Description:
	 * 		create ContentValues before inserting data into database
	 *=============================================================================*/
	public ContentValues createContentValues(String date, String time, 
												String phone, String command)
	{
		ContentValues values = new ContentValues();
		
		values.put(CommandHistoryDB.DATE, date);
		values.put(CommandHistoryDB.TIME, time);
		values.put(CommandHistoryDB.PHONE, phone);
		values.put(CommandHistoryDB.COMMAND, command);
		
		return values;
	}

	/*=============================================================================
	 * Name: insertCommandToHistory
	 * 
	 * Description:
	 * 		Insert executed commands to CommandHistroy Database 
	 * 		Check the maximum count which will be stored. up to 100
	 *=============================================================================*/	
	private void insertCommandToHistory(String command)
	{
		long row =0;
		int currentRow = 0;
		String date = "";
		String time = "";
		String phonenumber = mPhoneNumber;

		////////////////////////////////////////////////////////////////
		date = getDateString();
		time = getTimeString();
		
		ContentValues values = createContentValues(date, time, phonenumber, command);
		
		Log.i(CLASS_NAME, "Insert Command: " + date + time + phonenumber + command);
		mCursor = queryHistoryDatabase(mCmdHistoryDatabase);
		
		currentRow = mCursor.getCount();
		Log.i(CLASS_NAME, "Saved Record Number: " + currentRow);
		
		startManagingCursor(mCursor);
		
		if(currentRow >= (SMSData.MAXIMUM_HISTORY_NUMBER))
		{
			//mSavedRecordNumber = 1; 
			isAboveMax = true;
			//mCursor.moveToFirst();
		}
		
		if(isAboveMax == true)
		{
			
			String[] where = {Integer.toString(mSavedRecordNumber)};

			mCmdHistoryDatabase.update(CommandHistoryDB.TABLE_NAME, values, 
									CommandHistoryDB.ID + "=?", where);
			Log.i(CLASS_NAME, "Update:id= " + mSavedRecordNumber + " command: " + command);
			
			if(mSavedRecordNumber >= SMSData.MAXIMUM_HISTORY_NUMBER)
				mSavedRecordNumber = 1;
			else
				mSavedRecordNumber++;

		}
		else 
		{
			row = mCmdHistoryDatabase.insert(CommandHistoryDB.TABLE_NAME, null, values);
			Log.i(CLASS_NAME, "Insert  command: " + command);
		}

	}
	/*=============================================================================
	 * Name: queryHistoryDatabase
	 * 
	 * Description:
	 * 		Query all items from CommandHistoryDB
	 *=============================================================================*/	
	private Cursor queryHistoryDatabase(SQLiteDatabase db)
	{
		Cursor cursor = null;
		cursor = db.rawQuery(" select * from " + CommandHistoryDB.TABLE_NAME + " order by "
							+ CommandHistoryDB.DEFAULT_SORT_ORDER, null);
		Log.i(CLASS_NAME, "queryHistoryDatabase: cursor= " + cursor);
		return cursor;
	}
	/*=============================================================================
	 * Name: getDateString
	 * 
	 * Description:
	 * 		Convert date information to a String
	 *=============================================================================*/	
	private String getDateString()
	{
		String strDate = "";
		Calendar today = Calendar.getInstance();
		
		strDate =  (today.get(Calendar.MONTH)+1) + "."
					+ today.get(Calendar.DATE) + "."
					+ today.get(Calendar.YEAR);
		
		return strDate;
	}
	/*=============================================================================
	 * Name: getTimeString
	 * 
	 * Description:
	 * 		Convert time information to a String
	 *=============================================================================*/		
	private  String getTimeString()
	{
		String strTime = "";
		int hour = 0;
		int min = 0;
		int sec = 0;
		
		Calendar today = Calendar.getInstance();
		
		hour = today.get(Calendar.HOUR_OF_DAY);
		if(hour < 10)
		{
			strTime += "0";
		}
		strTime += hour + ":";
		
		
		min = today.get(Calendar.MINUTE);
		if(min < 10)
		{
			 strTime += "0";
		}
		strTime += min + ":";
		
		sec = today.get(Calendar.SECOND);
		if(sec < 10)
		{
			strTime += "0";
		}
		strTime += sec+ "";

		return strTime;
	}
	/*=============================================================================
	 * Name: showAlertErrorDialog
	 * 
	 * Description:
	 * 		Show an alert dialog when an error happens
	 *=============================================================================*/	
	private void showAlertErrorDialog(String title, String message)
	{
		AlertDialog.Builder alertDlg = new AlertDialog.Builder(GUICmdActivity.this);
	
		alertDlg.setTitle(title);
		alertDlg.setMessage(message);
		alertDlg.setIcon(R.drawable.icon);
		alertDlg.setPositiveButton("Close", null);
		alertDlg.show();
	}
	
	/*===========================================================================
     * Function Name: clearRemoteSiteInfo
     * 
     * Description
     * 		Initialize TextView widgets with the default string  
     *===========================================================================*/
	private void clearRemoteSiteInfo()
	{
		mLocationTextView.setText("Location");
		mPhoneNumberTextView.setText("Phone Number");
		mDeviceTextView.setText("Device");	
		mAuthorizedPassCode = "";
	}


}
