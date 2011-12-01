package condroid.RemoteManagement;

import java.util.*;

import condroid.RemoteManagement.RemoteSystemDatabase.*;
import android.app.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.os.*;
import android.telephony.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class Administration extends Activity{

	private static final String CLASS_NAME = "Administration";
	
	final static int MSG_ID_SENT = 0;	
	final static int MSG_ID_RECEIVED = 1;
	final static int MSG_ID_ERROR = 2;
	final static int MSG_ID_LOGOUT = 3;	

	/*
	 * Database classes and variables
	 */
	private RemoteSystemDatabaseHelper mDBHelper;
	private SQLiteDatabase mCmdHistoryDatabase;	
	private Cursor mCursor = null;
	private CommandHistoryDB mCommandHistoryDBClass;

	static private int mSavedRecordNumber=1;
	private boolean isAboveMax = false;

	/*
	 * TextView Widgets
	 */
	TextView mLocationTextView;
	TextView mPhoneNumberTextView;
	TextView mDeviceTextView;
	TextView mResultTextView;
	
	/*
	 * ListView
	 */
	
	ListView mAdminListView;
	
	/* EditText
	 * 
	 */
	EditText mAdminCmdEditText;
	
	// Pincode dialog's edittext
	EditText mDlgPincodeEditText;
	/*
	 * RadioGroup
	 */
	RadioGroup mRadioAdminCmdType;
	
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
	private String mSender;
	/*
	 * Passcode
	 */
	private String mAuthorizedPassCode;

	/*
	 * Member variables
	 */
	String mAdminCommand;
	String mRadioCommand;
	String mNewPincode;
	
	boolean isLogoutAckReceived;
	boolean isPincodeChangeCommand;
	Handler mTimerHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.administration);
		
		// Init Database
		mDBHelper = new RemoteSystemDatabaseHelper(this);
		mCmdHistoryDatabase = mDBHelper.getWritableDatabase();
		mCommandHistoryDBClass = new CommandHistoryDB();

		smsData = new SMSData();
		
		// TextView
		mLocationTextView = (TextView)findViewById(R.id.admin_location);
		mPhoneNumberTextView = (TextView)findViewById(R.id.admin_phonenumber);
		mDeviceTextView = (TextView)findViewById(R.id.admin_device);
		
		mResultTextView = (TextView)findViewById(R.id.admin_result_text);
		mResultTextView.setText("");

		// EditText
		mAdminCmdEditText = (EditText)findViewById(R.id.admin_command_edit);
		
		// Button
		Button sendButton = (Button)findViewById(R.id.admin_cmd_send_button);
		sendButton.setOnClickListener(mAdminClickListener);
		
		Button changePincodeButton = (Button)findViewById(R.id.admin_change_pincode_button);
		changePincodeButton.setOnClickListener(mAdminClickListener);
				
		Button logoutButton = (Button)findViewById(R.id.admin_logout_button);
		logoutButton.setOnClickListener(mAdminClickListener);

		// Home Image Button
		ImageButton homeButton = (ImageButton)findViewById(R.id.admin_home_image_button);
		homeButton.setOnClickListener(mAdminClickListener);
		
		// Radio Group 
		mRadioAdminCmdType = (RadioGroup)findViewById(R.id.radio_group_admin_cmd);		
		mRadioAdminCmdType.setOnCheckedChangeListener(mCheckedChangeListener);

		
		initMemberVariables();
		getRemoteSiteInfo();
		showRemoteSiteInfo();

	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
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
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
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

	Button.OnClickListener mAdminClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) 
		{
			// TODO Auto-generated method stub
			switch(v.getId())
			{
			case R.id.admin_cmd_send_button:
				sendCommandToRemoteSystem(SMSData.LOGIN_ADMIN_MSG);
				break;
			case R.id.admin_change_pincode_button:								
				sendPincodeChangeCommand(SMSData.LOGIN_CHANGE_PIN);
				//showPincodeChangeDialog();
				break;
			case R.id.admin_logout_button:
				sendCommandToRemoteSystem(SMSData.LOGOUT);
				break;
			case R.id.admin_home_image_button:
				startActivity(new Intent(Administration.this, Home.class));
				finish();
				break;
			}
			
		}
	};
	
	RadioGroup.OnCheckedChangeListener mCheckedChangeListener = 
											new RadioGroup.OnCheckedChangeListener()
	{
		public void onCheckedChanged(RadioGroup group, int checkedId)
		{
			if(group.getId() == R.id.radio_group_admin_cmd)
			{
				switch(checkedId)
				{
				case R.id.radio_add_number:
					mRadioCommand = "ADD";
					break;
				case R.id.radio_remove_number:
					mRadioCommand = "REMOVE";
					break;
				case R.id.radio_change_admin:
					mRadioCommand = "CHANGE_ADMIN";
					break;
				}
			}
		}
	};
	
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
		smsData.resetRemoteSiteInformation();		// init remote site information
		
		showResultMessage(type, "LOGOUT: return to Login screen.");
		initMemberVariables();
		startActivity(new Intent(Administration.this, Login.class));
		finish();

	}
	/*===========================================================================
     * Function Name: sendCommandToRemoteSystem
     * 
     * Description
     * 		LOGIN_ADMIN_MSG(6)  
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
			mAdminCommand =  mAdminCmdEditText.getText().toString();
			len = mAdminCommand.length();					
		
			if((type == SMSData.LOGIN_ADMIN_MSG && len > 0))
			{
				header = smsData.makeCommandHeader(mAuthorizedPassCode, type, len);								
				
				command = header + mRadioCommand + " " + mAdminCommand;
				
				sendSMStoRemoteSystem(mPhoneNumber, command);
				
				// Write an administration command to a CommandHistory Database
				insertCommandToHistory(mRadioCommand +" " + mAdminCommand);
				
				showResultMessage(MSG_ID_SENT, "Sent message(" + mPhoneNumber + "): "
				 					+ command);
				
				mAdminCmdEditText.setText("");
				/* Later use
				showResultMessage(MSG_ID_SENT, "Sent message(" + mPhoneNumber + "): "
	 					+ mSelectedCommand);
	 			*/
			}
			else if(type == SMSData.LOGOUT)
			{
				isLogoutAckReceived = false;			
				
				sendLogoutMessage(type, len);	
				// Write an administration command to a CommandHistory Database
				insertCommandToHistory("Logout sent");

				waitingLogoutAckMessage();
			}
			else
			{
				showAlertErrorDialog("Error", "Invalid command(length is " + len + ")");
			}
		}
	}
	/*===========================================================================
     * Function Name: sendPincodeChangeCommand
     * 
     * Description
     * 		LOGIN_CHANGE_PIN(7)  
     *===========================================================================*/
	private void sendPincodeChangeCommand(int type)
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
			mNewPincode =  mAdminCmdEditText.getText().toString();
			len = mNewPincode.length();					
		
			if(type == SMSData.LOGIN_CHANGE_PIN && len == 3)
			{
				header = smsData.makeCommandHeader(mAuthorizedPassCode, type, len);								
				
				command = header + mNewPincode;
				
				sendSMStoRemoteSystem(mPhoneNumber, command);
				
				// Write an administration command to a CommandHistory Database
				insertCommandToHistory("Change Pincode " +  mNewPincode);

				showResultMessage(MSG_ID_SENT, "Pincode change (" + mPhoneNumber + "): "
				 					+ command);
				
				mAdminCmdEditText.setText("");
				mNewPincode = "";
			}
			else
			{	
				showAlertErrorDialog("Error", "Invalid Pincode number(Type 3 digits)!");
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
		mResultTextView.setText("");
		
		mRadioCommand = "ADD";
		isLogoutAckReceived = false;
		isPincodeChangeCommand = false;
		
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
	/*=============================================================================
	 * Name: showPincodeChangeDialog
	 * 
	 * Description:
	 * 		Create a custom dialog box and show a Pincode change dialog
	 *=============================================================================*/		
	private void showPincodeChangeDialog()
	{
		final LinearLayout linear = (LinearLayout)View.inflate(Administration.this, 
																R.layout.pincode_change, null);	
		
				
		AlertDialog.Builder pincodeDlg = new AlertDialog.Builder(Administration.this);
		pincodeDlg.setTitle("Change Pincode Dialog");
		pincodeDlg.setMessage("Please type a new pincode.");
		pincodeDlg.setIcon(R.drawable.pincode);
		pincodeDlg.setView(linear);
		pincodeDlg.setPositiveButton("Ok", mClickDialogButton);
		pincodeDlg.setNegativeButton("Cancel", mClickDialogButton);
		
		mDlgPincodeEditText = (EditText)linear.findViewById(R.id.pincode_edittext);
		// Limit pincode input up to 3 numbers		
		mDlgPincodeEditText.setFilters(new InputFilter[] {
				new InputFilter.LengthFilter(3)
		});
		
		mDlgPincodeEditText.setText("");
		
		pincodeDlg.show();		
	}
	
	DialogInterface.OnClickListener mClickDialogButton = new DialogInterface.OnClickListener() 
	{
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// OK Button
			if(which == DialogInterface.BUTTON1)
			{
				mNewPincode = mDlgPincodeEditText.getText().toString();
				Log.i(CLASS_NAME, "Ok button clicked : " + mNewPincode);
			}
			else
			{
				// Cancel Button
				Log.i(CLASS_NAME, "Cancel button clicked.");
				mNewPincode = "";
			}
		}
	};
	/*=============================================================================
	 * Name: showAlertErrorDialog
	 * 
	 * Description:
	 * 		Show an alert dialog when an error happens
	 *=============================================================================*/	
	private void showAlertErrorDialog(String title, String message)
	{
		AlertDialog.Builder alertDlg = new AlertDialog.Builder(Administration.this);
	
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
		long row = 0;
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
			Log.i(CLASS_NAME, "Insert row: " + row + " command: " + command);
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
