package condroid.RemoteManagement;

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

public class OpticalAmpActivity extends Activity{
	final static int MSG_ID_SENT = 0;
	final static int MSG_ID_RECEIVED = 1;
	final static int MSG_ID_ERROR = 2;

	/*
	 * Database classes
	 */
	private RemoteSystemDatabaseHelper mDBHelper;
	private SQLiteDatabase mCmdDatabase;	
	private Cursor mCursor = null;
	private DeviceCommandDB mDeviceCommandDB;

	/*
	 * SMS variables
	 */
	BroadcastReceiver smsMsgSent = null;
	BroadcastReceiver smsMsgIncoming = null;

	SMSData smsData;
	/*
	 * Remote Site Information 
	 */
	private String mOpAmpLocation;
	private String mOpAmpPhoneNumber;
	private String mOpAmpDevice;
	
	/*
	 * Passcode
	 */
	private String mAuthorizedPassCode;
	
	/*
	 * SMS Message
	 */
	private String mOpAmpCommand;	
	
	private String mSender="";
	/*
	 * TextView Widgets
	 */
	private TextView mLocationTextView;
	private TextView mPhoneNumberTextView;
	private TextView mDeviceTextView;
	private TextView mOpAmpResultTextView;
	/*
	 *	Buttons 
	 */
	private Button mOpAmpSendButton;
	private Button mOpAmpClearButton;
	private Button mOpAmpCmdEditButton;
	
	/*
	 * Spinner
	 */
	Spinner mOpAmpCommandSpinner;
	String mSelectedCommand;	// Command selected in Spinner widget
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.opamp);
		
		mDBHelper = new RemoteSystemDatabaseHelper(this);
		mCmdDatabase = mDBHelper.getReadableDatabase();

		mDeviceCommandDB = new DeviceCommandDB();

		smsData = new SMSData();
		initMemberVariables();
		
		/*
		 * TestView widgets
		 */
		mLocationTextView = (TextView)findViewById(R.id.opamp_location);
		mPhoneNumberTextView = (TextView)findViewById(R.id.opamp_phonenumber);
		mDeviceTextView = (TextView)findViewById(R.id.opamp_device);
		mOpAmpResultTextView = (TextView)findViewById(R.id.opamp_result);
		
		/*
		 * Buttons
		 */
		mOpAmpSendButton = (Button)findViewById(R.id.opamp_send_button);
		mOpAmpSendButton.setOnClickListener(mOpAmpClickListener);
		
		mOpAmpClearButton = (Button)findViewById(R.id.opamp_clear_button);
		mOpAmpClearButton.setOnClickListener(mOpAmpClickListener);
		
		mOpAmpCmdEditButton = (Button)findViewById(R.id.opamp_edit_button);
		mOpAmpCmdEditButton.setOnClickListener(mOpAmpClickListener);
		
		/*
		 * Spinner
		 */
		mOpAmpCommandSpinner = (Spinner)findViewById(R.id.opamp_cmd_spinner);
		mOpAmpCommandSpinner.setOnItemSelectedListener(mItemSelectedListener);
		// Display remote site information which was selected in Authentication process
		getRemoteSiteInfo();
		showRemoteSiteInfo();
		
		// Connect database item to Spinner widget
		Log.i("opamp", "showAllCommandListToSpinner");
		showAllCommandListToSpinner();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
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
        			Log.i("sms", "Authentication.java: Bundle data received");
        			
                    // SMS uses a data format known as a PDU
                    Object pdus[] = (Object[]) data.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                                      
                    for (int i=0; i<msgs.length; i++)
                    {
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);      
                        mSender = msgs[i].getOriginatingAddress();                                             
                        recvMessage += msgs[i].getMessageBody().toString();                              
                    }
  
                    Log.i("sms", recvMessage);
                    
                    processReceivedOpticalAmpMessage(recvMessage);
        		}
        		else
        		{
        			Log.e("sms", "Received Data is null");
        		}        		
			}	// onReceive()			
		};	// BroadcastReceiver()
		
		registerReceiver(smsMsgIncoming, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));


		super.onResume();
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
			Log.e("sms", "Failed to unregister smsMsgSent");
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
			Log.e("sms", "Failed to unregister smsMsgIncoming");
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
				Log.i("sms", "unregisterReceiver(smsMsgSent");
			}
		}
		catch(Exception e) 
		{
			Log.e("sms", "Failed to unregister smsMsgSent");
		}
		
		
		try 
		{
			if(smsMsgIncoming != null) 
			{
				unregisterReceiver(smsMsgIncoming);
				smsMsgIncoming = null;
				Log.i("sms", "unregisterReceiver(smsMsgIncoming");
			}
		}
		catch(Exception e) 
		{
			Log.e("sms", "Failed to unregister smsMsgIncoming");
		}

		if(mCmdDatabase != null)
		{
			mCmdDatabase.close();
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
	Button.OnClickListener mOpAmpClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId())
			{
			case R.id.opamp_send_button:
				// Send SMS to the Remote Management System
				//sendSMStoRemoteSystem(String phoneNumber, String message);
				break;
			case R.id.opamp_clear_button:
				// Clear
				break;
			case R.id.opamp_edit_button:
				// Move to OpticalAMPDBManagerActivity class
				Intent intent = new Intent(OpticalAmpActivity.this, OpticalAmpDBManagerActivity.class);
				startActivity(intent);
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
				mSelectedCommand = parent.getSelectedItem().toString(); 
	            Log.i("opamp", "mSelectedCommand: " + mSelectedCommand);   
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
		
		mOpAmpLocation = remoteSiteInfo[0];
		mOpAmpPhoneNumber = remoteSiteInfo[1];
		mOpAmpDevice = remoteSiteInfo[2];
		mAuthorizedPassCode = remoteSiteInfo[3];
		
		Log.i("custom", "Pass Code : " + mAuthorizedPassCode);

	}
	/*===========================================================================
     * Function Name: showRemoteSiteInfo
     * 
     * Description
     * 		Display Remote site information chosen by an end-user  
     *===========================================================================*/
	private void showRemoteSiteInfo()
	{
		
		if(mOpAmpLocation.length() != 0)
		{
			Log.i("custom", mOpAmpLocation);
			mLocationTextView.setText(mOpAmpLocation);
		}
		else
		{
			Log.e("custom", "Remote Location is null.");
		}
		
		if(mOpAmpPhoneNumber.length() != 0)
		{
			Log.i("custom", mOpAmpPhoneNumber);
			mPhoneNumberTextView.setText(mOpAmpPhoneNumber);
		}
		else
		{
			Log.e("custom", "Remote Phone Number is null.");
		}
		
		if(mOpAmpDevice.length() != 0)
		{
			Log.i("custom", mOpAmpDevice);
			mDeviceTextView.setText(mOpAmpDevice);
		}
		else
		{
			Log.e("custom", "Remote Device is null.");
		}

	}

	/*===========================================================================
     * Function Name: initMemberVariables
     * 
     * Description
     * 		Initialize member variables  
     *===========================================================================*/
	private void initMemberVariables()
	{
		mOpAmpLocation = "";
		mOpAmpPhoneNumber = "";
		mOpAmpDevice = "";

		mAuthorizedPassCode = "";
		mOpAmpCommand = "";
		
		mSelectedCommand = "";
	}
	/*===========================================================================
     * Function Name: processReceivedOpticalAmpMessage
     * 
     * Description
     * 		process the received SMS from Remote Management System  
     *===========================================================================*/
	private void processReceivedOpticalAmpMessage(String msg)
	{
        // *** Login field checking in any activities
		int login = 0;
		if(smsData.validationCheck(msg) == SMSData.SUCCESS)
		{
			login = smsData.checkLoginValue(msg);
			if(login == SMSData.LOGOUT)
			{
				initMemberVariables();
				smsData.deletePassCode();
				
				showResultMessage(MSG_ID_RECEIVED, "LOGOUT: Go back to Login.");
				// Return to Login screen
				Intent intent = new Intent(OpticalAmpActivity.this, Login.class);
				startActivity(intent);
			}
			showResultMessage(MSG_ID_RECEIVED, msg);
		}
		else
		{
			showResultMessage(MSG_ID_ERROR, "Received an invalied message(No CRM field)");
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
			mOpAmpResultTextView.append("# " + msg);
		}
		else if(msgType == MSG_ID_RECEIVED)
		{
			mOpAmpResultTextView.append(">> " + msg);
		}
		else
		{
			mOpAmpResultTextView.append("# Error " + msg);
		}
		mOpAmpResultTextView.append("\n");
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
			Log.i("sms_module", "unregisterReceiver(receiverMsgSent)");
			smsMsgSent = null;
		}
		// 
		smsMsgSent = new BroadcastReceiver() 
		{
			
			public void onReceive(Context context, Intent intent) 
			{
				int result = getResultCode();
				
				if(result != Activity.RESULT_OK) 
				{
					Log.e("sms_module", "SMS failed: code = " + result);
					Toast.makeText(getBaseContext(), "SMS failed = " + result, 
                            Toast.LENGTH_SHORT).show();
				}
				else 
				{
					Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
				}
			}
		};
		registerReceiver(smsMsgSent, new IntentFilter("ACTION_MSG_SENT"));
		
		SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentIntent, receiptIntent);
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
		if(mOpAmpDevice.length() == 0)
		{
			Log.e("opamp", "mOpAmpDevice is null");
		}
		else
		{
			Log.i("opamp", "mOpAmpDevice=" + mOpAmpDevice);
		}
		
		mCursor = queryCommandbyDevice(mOpAmpDevice);
		Log.i("opamp", "DeviceCommandDB: Count= " + mCursor.getCount());	
		
		if(mCursor != null)
		{
			startManagingCursor(mCursor);
			mCursor.moveToFirst();			
		
			String[] from = new String[] {DeviceCommandDB.COMMAND}; 
			int[] to = new int[] {android.R.id.text1};
			
			SimpleCursorAdapter Adapter = null;
			
			try {
				Adapter = new SimpleCursorAdapter(
											mOpAmpCommandSpinner.getContext(),
											android.R.layout.simple_spinner_item,
											mCursor,	//Item으로 사용할 DB의 Cursor
											from,	//DB 필드 이름
											to);	//DB필드에 대응되는 xml TextView의 id
			}
			catch(Exception e)
			{
				Log.e("opamp", e.getMessage());
			}
			
			mOpAmpCommandSpinner.setAdapter(Adapter);
		}
		else
		{
			Log.e("opamp", "mCursor is null.");
		}
		
		//db.close();
		
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
		Log.i("opamp", "queryAll: cursor= " + cursor.getCount());
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
	
		String[] columns = new String[] { DeviceCommandDB.ID,
										DeviceCommandDB.TYPE,
										DeviceCommandDB.COMMAND };
			
		try 
		{
		    
			Log.i("opamp", "devType= " + devType);
			cursor = mCmdDatabase.query(DeviceCommandDB.TABLE_NAME, columns, 
							DeviceCommandDB.TYPE + "=?", new String[]{devType},	null, null, null);
			if(cursor.getCount() == 0)
			{
				cursor = null;
			}
			else
			{
				while(cursor.moveToNext())
				{
					Log.e("opamp", "index: " + cursor.getString(0));
					Log.e("opamp", "type: " + cursor.getString(1));
					Log.e("opamp", "command: " + cursor.getString(2));				
				}
			}
		}
		catch(SQLiteException e)
		{
			cursor = null;
			Log.e("opamp", e.toString());	
		}
		return cursor;
	}
}
