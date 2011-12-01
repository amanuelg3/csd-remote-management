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
import condroid.RemoteManagement.*;
import condroid.RemoteManagement.RemoteSystemDatabase.*;

public class CustomCmdActivity extends Activity {
	private static final String CLASS_NAME = "CustomCmdActivity";
	
	final static int MSG_ID_SENT = 0;
	final static int MSG_ID_RECEIVED = 1;
	final static int MSG_ID_ERROR = 2;
	final static int MSG_ID_LOGOUT = 3;
	
	
	/*
	 * TextView Widgets
	 */
	TextView mLocationTextView;
	TextView mPhoneNumberTextView;
	TextView mDeviceTextView;
	TextView mCustomResultTextView;
	
	/* EditText
	 * 
	 */
	EditText mCustomCmdEditText;
	
	/*
	 * SMS variables
	 */
	BroadcastReceiver smsMsgSent = null;
	BroadcastReceiver smsMsgIncoming = null;
	
	SMSData smsData;

	/*
	 * Database classes and variables
	 */
	private RemoteSystemDatabaseHelper mDBHelper;
	private SQLiteDatabase mCmdHistoryDatabase;	
	private Cursor mCursor = null;
	private CommandHistoryDB mCommandHistoryDBClass;

	static private int mSavedRecordNumber=1;
	private boolean isAboveMax = false;
	/* Member Variables
	 * 
	 */
	String mCustomLocation;
	String mCustomPhoneNumber;
	String mCustomDevice;
	String mAuthorizedPassCode;
	
	String mCustomCommand;
	String mSender="";
	
	int msgType = 0;
	boolean isLogoutAckReceived;
	
	Handler mTimerHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_cmd);
		
		smsData = new SMSData();
		
		// Init Database
		mDBHelper = new RemoteSystemDatabaseHelper(this);
		mCmdHistoryDatabase = mDBHelper.getWritableDatabase();
		mCommandHistoryDBClass = new CommandHistoryDB();
		
		mCustomResultTextView = (TextView)findViewById(R.id.custom_result_view);
		mLocationTextView = (TextView)findViewById(R.id.custom_location);
		mPhoneNumberTextView = (TextView)findViewById(R.id.custom_phonenumber);
		mDeviceTextView = (TextView)findViewById(R.id.custom_device);
		
		Button customCmdButton = (Button)findViewById(R.id.custom_cmd_send_button);
		customCmdButton.setOnClickListener(CustomClickListener);
		
		Button customClearButton = (Button)findViewById(R.id.custom_cmd_clear_button);
		customClearButton.setOnClickListener(CustomClickListener);
		
		Button customLogoutButton = (Button)findViewById(R.id.custom_cmd_logout_button);
		customLogoutButton.setOnClickListener(CustomClickListener);
		
		ImageButton customHomeButton = (ImageButton)findViewById(R.id.custom_home_image_button);
		customHomeButton.setOnClickListener(CustomClickListener);
		
		mCustomCmdEditText = (EditText)findViewById(R.id.command_edit);


		// Init member variables
		initMemberVariables();

		getRemoteSiteInfo();
		showRemoteSiteInfo();		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
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
                    // Process received SMS message for further authentication process
                    
                    processReceivedCustomMessage(recvMessage);
        		}
        		else
        		{
        			Log.e(CLASS_NAME, "Received Data is null");
        		}        		
			}	// onReceive()			
		};	// BroadcastReceiver()
		
		registerReceiver(smsMsgIncoming, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));

	
		super.onResume();
	}



	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		try {
			if(smsMsgSent != null) {
				unregisterReceiver(smsMsgSent);
				smsMsgSent = null;
			}
		}catch(Exception e) {
			Log.e(CLASS_NAME, "Failed to unregister smsMsgSent");
		}
		
		
		try {
			if(smsMsgIncoming != null) {
				unregisterReceiver(smsMsgIncoming);
				smsMsgIncoming = null;
			}
		}catch(Exception e) {
			Log.e(CLASS_NAME, "Failed to unregister smsMsgIncoming");
		}

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		try {
			if(smsMsgSent != null) {
				unregisterReceiver(smsMsgSent);
				smsMsgSent = null;
				Log.i(CLASS_NAME, "unregisterReceiver(smsMsgSent");
			}
		}catch(Exception e) {
			Log.e(CLASS_NAME, "Failed to unregister smsMsgSent");
		}
		
		
		try {
			if(smsMsgIncoming != null) {
				unregisterReceiver(smsMsgIncoming);
				smsMsgIncoming = null;
				Log.i(CLASS_NAME, "unregisterReceiver(smsMsgIncoming");
			}
		}catch(Exception e) {
			Log.e(CLASS_NAME, "Failed to unregister smsMsgIncoming");
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
	 * Button OnClickListener
	 */
	Button.OnClickListener CustomClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId())
			{
			case R.id.custom_cmd_send_button:				
				msgType = SMSData.LOGIN_NORMAL_MSG;
				sendCustomCommand(msgType);
				break;
			case R.id.custom_cmd_clear_button:
				mCustomCmdEditText.setText("");
				break;
			case R.id.custom_cmd_logout_button:
				msgType = SMSData.LOGOUT;
				sendCustomCommand(msgType);
				break;
			case R.id.custom_home_image_button:
				startActivity(new Intent(CustomCmdActivity.this, Home.class));
				finish();
				break;
			}
			
		}
	};
	/*===========================================================================
     * Function Name: processReceivedCustomMessage
     * 
     * Description
     * 		process the received SMS from Remote Management System  
     *===========================================================================*/
	private void processReceivedCustomMessage(String msg)
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
			showResultMessage(MSG_ID_RECEIVED, msg);
		}
		else
		{
			showResultMessage(MSG_ID_ERROR, "Received an invalied message(No CRM field)");
		}
	}
	/*===========================================================================
     * Function Name: sendCustomCommand
     * 
     * Description
     * 		Send a SMS including a CRM header and a message body  
     *===========================================================================*/	
	private void sendCustomCommand(int type)
	{
		String customCmdMsg = ""; 
		int len = 0;
		String header = "";
		
		if(mCustomPhoneNumber.length() == 0)
		{
			showAlertErrorDialog("Error", "There is no Phone Number. Please check.");			
		}
		else
		{
			mCustomCommand = mCustomCmdEditText.getText().toString();
			len = mCustomCommand.length();
			Log.d(CLASS_NAME, "Cmd: " + mCustomCommand);
			
			if(type == SMSData.LOGIN_NORMAL_MSG && len > 0)
			{
				header = smsData.makeCommandHeader(mAuthorizedPassCode, type, len);
		
				customCmdMsg = header + mCustomCommand;				
				sendSMStoRemoteSystem(mCustomPhoneNumber, customCmdMsg);
				
				showResultMessage(MSG_ID_SENT, "Sent message(" + mCustomPhoneNumber + "): "
						 			+ customCmdMsg);
				
				insertCommandToHistory(mCustomCommand);
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
				showAlertErrorDialog("Error", "Invalid Custom command!");
			}
			
		}
		mCustomCmdEditText.setText("");
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
		
		sendSMStoRemoteSystem(mCustomPhoneNumber, logoutMsg);		
		showResultMessage(MSG_ID_SENT, "Sent a LOGOUT Message: " + logoutMsg);
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
                    Log.d(CLASS_NAME, "Successful transmission!!");
                    //showNotificationToast("Successful transmission!");
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Log.d(CLASS_NAME, "Nonspecific Failure!!");
                    showNotificationToast("Generic Failure!");
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Log.d(CLASS_NAME, "Radio is turned Off!!");
                    showNotificationToast("Radio is turned off!");
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Log.d(CLASS_NAME, "PDU Failure");
                    showNotificationToast("PDU Failure");
                    break;
				}

			}
		};
		registerReceiver(smsMsgSent, new IntentFilter("ACTION_MSG_SENT"));
		
		SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentIntent, receiptIntent);      
        
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
		//initMemberVariables();
		clearRemoteSiteInfo();		// Initialize local variables
		smsData.deletePassCode();
		smsData.setAuthenticationValue(false);		// check later: changsu
		smsData.resetRemoteSiteInformation();		// init remote site information
		
		showResultMessage(type, "LOGOUT: return to Login screen.");
		startActivity(new Intent(CustomCmdActivity.this, Login.class));
		finish();
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
    							String.valueOf((SMSData.LOGOUT_TIME - wait)/1000) + " seconds left.");
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
	/*=============================================================================
	 * Name: showNotificationToast
	 * 
	 * Description:
	 * 		Show an alert message into a Toast
	 *=============================================================================*/	
    private void showNotificationToast (String message) 
    {

        try {

            //Show the toast message
            Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show(); 

        }catch (Exception e) {
            Log.e(CLASS_NAME, e.toString());
        }
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
			mCustomResultTextView.append("# " + msg);
			Log.i(CLASS_NAME, "Sent Msg: " + msg);
		}
		else if(msgType == MSG_ID_RECEIVED)
		{
			mCustomResultTextView.append(">> " + msg);
			Log.i(CLASS_NAME, ">> " +msg);
		}
		else if(msgType == MSG_ID_LOGOUT)
		{
			mCustomResultTextView.append("# Logout message " + msg);
		}
		else
		{
			mCustomResultTextView.append("# Error " + msg);
			Log.e(CLASS_NAME, "Error: " + msg);
		}
		mCustomResultTextView.append("\n");
	}

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
		
		mCustomLocation = remoteSiteInfo[0];
		mCustomPhoneNumber = remoteSiteInfo[1];
		mCustomDevice = remoteSiteInfo[2];
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
		
		if(mCustomLocation.length() != 0)
		{
			
			mLocationTextView.setText(mCustomLocation);
		}
		else
		{
			Log.e(CLASS_NAME, "Remote Location is null.");
		}
		
		if(mCustomPhoneNumber.length() != 0)
		{
			mPhoneNumberTextView.setText(mCustomPhoneNumber);
		}
		else
		{
			Log.e(CLASS_NAME, "Remote Phone Number is null.");
		}
		
		if(mCustomDevice.length() != 0)
		{
			mDeviceTextView.setText(mCustomDevice);
		}
		else
		{
			Log.e(CLASS_NAME, "Remote Device is null.");
		}
		
		Log.i(CLASS_NAME, mCustomLocation + " " + mCustomPhoneNumber +" " + mCustomDevice);
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
	/*===========================================================================
     * Function Name: initMemberVariables
     * 
     * Description
     * 		Initialize member variables  
     *===========================================================================*/	
	private void initMemberVariables()
	{
		mCustomLocation = "";
		mCustomPhoneNumber = "";
		mCustomDevice = "";
		mAuthorizedPassCode = "";
		mCustomCommand = "";
		
		isLogoutAckReceived = false;


	}
	/*=============================================================================
	 * Name: showAlertErrorDialog
	 * 
	 * Description:
	 * 		Show an alert dialog when an error happens
	 *=============================================================================*/	
	private void showAlertErrorDialog(String title, String message)
	{
		AlertDialog.Builder alertDlg = new AlertDialog.Builder(CustomCmdActivity.this);
	
		alertDlg.setTitle(title);
		alertDlg.setMessage(message);
		alertDlg.setIcon(R.drawable.icon);
		alertDlg.setPositiveButton("Close", null);
		alertDlg.show();
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
		String phonenumber = mCustomPhoneNumber;

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
}
