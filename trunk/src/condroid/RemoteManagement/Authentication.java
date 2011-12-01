package condroid.RemoteManagement;

import android.app.*;
import android.content.*;
import android.os.*;
import android.telephony.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class Authentication extends Activity{
	private static final String CLASS_NAME = "Authentication";
	/*
	 * Message ID
	 */
	final static int MSG_ID_SENT = 0;
	final static int MSG_ID_RECEIVED = 1; 
	final static int MSG_ID_ERROR = 2;

	final static int SUCCESS = 0;
	final static int ERROR = -1;

	final static int DEVICE_OPAMP_ID 		= 0x1;
	final static int DEVICE_WIFI_ID 		= 0x2;
	final static int DEVICE_WATER_SENSOR_ID = 0x4;
	final static int DEVICE_ETC_ID 			= 0x8;

	
	final static int REMOTE_SITE_SELECT = 1;

	final static int HEADER_SIZE =9;
	final static int CRM_LENGTH = 3;
	final static int PASSCODE_LENGTH = 3;

	/*
	 * The bit position of CRM header 
	 */
	final static int LAST_POS_CRM = 2;
	final static int LAST_POS_PASSCODE = 5;
	final static int POS_MULTI = 6;
	final static int POS_ORDER = 7;
	final static int POS_LOGIN = 8;
	final static int POS_DEVICE = 9;
	
	/*
	 * Index of Header fields
	 */
	static final int INDEX_CRM		= 0;
	static final int INDEX_PASSCODE = 1;
	static final int INDEX_MULTI 	= 2;
	static final int INDEX_ORDER 	= 3;
	static final int INDEX_LOGIN 	= 4;
	static final int INDEX_DEVICE 	= 5;
	static final int INDEX_BODY		= 6;
	static final int INDEX_MSG_END	= 7;
	/*
	 * The definition of login field 
	 */
	final static int LOGIN_REQUEST		= 0;
	final static int LOGIN_PASSCODE		= 1;
	final static int LOGIN_SUCCESS		= 2;
	final static int LOGIN_FAILED		= 3;
	final static int LOGIN_NORMAL_MSG 	= 4;	// Answer Required
	final static int LOGIN_NORMAL_NOREPLY = 5;	// No answer required
	final static int LOGIN_ADMIN_MSG	= 6;
	final static int LOGIN_CHANGE_PIN	= 7;
	final static int LOGOUT				= 8;


	/*
	 * SMS variables
	 */
	BroadcastReceiver smsMsgSent = null;
	BroadcastReceiver smsMsgIncoming = null;
	
	SMSData smsData;
	
	/*
	 * Authentication variables
	 */
	String mUserPassCode="";
	String mRandomNumber = "";
	String mAuthorizedPassCode = "";
	
	String mSender="";

	/*
	 * Remote Site Database variables
	 */
	String mRemoteSiteLocation = "";
	String mRemoteSitePhoneNumber = "";
	String mRemoteSiteDevice = "";
	
	/*
	 * Widget Variables
	 */
	// TextViews
	TextView mLocationTextView;
	TextView mPhoneNumTextView;
	TextView mDeviceTextView;
	TextView mResultTextView;
	
	EditText mEditPassCode;
	
	/*
	 * Buttons
	 */
	Button remoteSiteSelectButton;
	Button requestPasscodeButton;
	Button settingButton;		// Move to Setting menu
	Button moveToNextButton;	// Debugging code (changsu)
	
	String[] remoteSiteValues;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authentication);			
		
		smsData = new SMSData();
		
		// TextViews related to RemoteSiteDB
		mLocationTextView = (TextView)findViewById(R.id.location);
		mPhoneNumTextView = (TextView)findViewById(R.id.phonenumber);
		mDeviceTextView = (TextView)findViewById(R.id.device);
		mResultTextView = (TextView)findViewById(R.id.authentication_result);
		
				
		// Buttons
		remoteSiteSelectButton = (Button)findViewById(R.id.remotesite_button);
		remoteSiteSelectButton.setOnClickListener(mClickListener);
		
		// Disable Requst Passcode Button when it creates
		requestPasscodeButton = (Button)findViewById(R.id.passcode_request_button);
		requestPasscodeButton.setOnClickListener(mClickListener);
		requestPasscodeButton.setEnabled(false);
		
		// Home Image Button
		ImageButton imgHomeButton = (ImageButton)findViewById(R.id.home_image_button);
		imgHomeButton.setOnClickListener(mClickListener);
		
		
		// Debugging code: changsu(지울 것)
		moveToNextButton = (Button)findViewById(R.id.Next_Button);
		moveToNextButton.setOnClickListener(mClickListener);
		moveToNextButton.setEnabled(true);
		
		mEditPassCode = (EditText)findViewById(R.id.passcode);
		// Limit the input digits up to 3 numbers
		mEditPassCode.setFilters(new InputFilter[] {
				new InputFilter.LengthFilter(PASSCODE_LENGTH)
		});
		
		initMemberVariables();
		
		

	}	// onCreate()
	/*===============================================================================
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 *===============================================================================*/
	@Override
	protected void onPause() {
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

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
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

		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {		
		
		getRemoteSiteInfo();
		if(mRemoteSitePhoneNumber.equals("Phone Number"))
		{
			Log.i(CLASS_NAME, "Login button is disabled(onResume).");
			requestPasscodeButton.setEnabled(false);
		}
		else
		{
			Log.i(CLASS_NAME, "Login button is enabled(onResume).");
			requestPasscodeButton.setEnabled(true);
		}
		/*==================================================================
		 * SMS Receiver Function: smsMsgIncoming
		 * 
		 *==================================================================*/

		smsMsgIncoming = new BroadcastReceiver()
		{
			
			public void onReceive(Context context, Intent intent) 
			{
        		SmsMessage[] msgs = null;
                
        		String message = "";        		
        		
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
                        message += msgs[i].getMessageBody().toString();                              
                    }
  
                    // Process received SMS message for further authentication process
                    processAuthentication(message);
        		}
        		else
        		{
        			Log.e(CLASS_NAME, "Received data is empty");
        		}

			}	// onReceive()			
		};	// BroadcastReceiver()
		
		registerReceiver(smsMsgIncoming, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));

		
		super.onResume();
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
			Log.i(CLASS_NAME, "unregisterReceiver(smsMsgSent)");
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
                    showNotificationToast("Successful transmission!");
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
    /*===========================================================================*/
	Button.OnClickListener mClickListener = new View.OnClickListener()
	{
		
		@Override
		public void onClick(View v) {
			switch(v.getId())
			{
			case R.id.remotesite_button:
				// Show a new View for Database
				 Intent intent = new Intent(Authentication.this, RemoteSiteDBManagerActivity.class);
				startActivityForResult(intent, REMOTE_SITE_SELECT);
				break;
			case R.id.passcode_request_button:
				// Send SMS for requesting Random number				
				requestAuthentication();				
				break;
				
				// Test Code: 향후 지울 것(changsu)
			case R.id.Next_Button:
				// Test: Jump to Home
				smsData.setAuthenticationValue(true);
				startActivity(new Intent(Authentication.this, Home.class));
				finish();
				break;
			case R.id.home_image_button:
				startActivity(new Intent(Authentication.this, Home.class));
				finish();
				break;
			}
		}
		
	};	// mClickListener
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode)
		{
		case REMOTE_SITE_SELECT:
			if(resultCode == RESULT_OK)
			{
				remoteSiteValues = data.getStringArrayExtra("remote_site");
				
				// Location information
				String location = remoteSiteValues[0];
				mLocationTextView.setText(location);
				
				// Phone number
				String phonenumber = remoteSiteValues[1];
				mPhoneNumTextView.setText(phonenumber);
				
				// Device Type
				String device = remoteSiteValues[2];				
				mDeviceTextView.setText(device);
				
				// Save seleted item to SMSData class static variables
				smsData.saveRemoteSiteInfo(location, phonenumber, device);

				// Enalbe Authentication Button
				requestPasscodeButton.setEnabled(true);
			}
			break;	
				
		}
	}

	/*=============================================================================
	 * Name: requestAuthentication
	 * 
	 * Description:
	 * 		Request passcode to the Remote Management System (SMS Gateway)
	 *=============================================================================*/	
	public int requestAuthentication()
	{
		int result = SUCCESS;
		String message = "";
		
		mUserPassCode = mEditPassCode.getText().toString();				
		mRemoteSitePhoneNumber = mPhoneNumTextView.getText().toString();
		
		if(mRemoteSitePhoneNumber.length() > 0 && mUserPassCode.length() == PASSCODE_LENGTH)
		{	
			// Make a CRM Header fields
			message = smsData.makeCRMMessage(mUserPassCode, LOGIN_REQUEST);			
			showResultMessage(MSG_ID_SENT, "LOGIN REQUEST: " + message);	// Debugging
			//showResultMessage(MSG_ID_SENT, "LOGIN REQUEST");
			sendSMStoRemoteSystem(mRemoteSitePhoneNumber, message);
			
			result = SUCCESS;
		}
		else
		{
			if(mRemoteSitePhoneNumber.length() == 0)
			{
				message = "Phone number doesn't exist.";
				Log.e(CLASS_NAME, "PhoneNumber doesn't exist");
			}
			else if (mUserPassCode.length() != PASSCODE_LENGTH)
			{
				message = "Invalid PassCode (Input 3 numbers)";
				Log.e(CLASS_NAME, "mUserPassCode is invalid: " + mUserPassCode);
			}
			showAlertErrorDialog("Error", message);
			result = ERROR;
		}
		return result;
	}
	
	/*=============================================================================
	 * Name: processAuthentication
	 * 
	 * Description:
	 * 		Process all authentication handshake
	 *=============================================================================*/
	public void processAuthentication(String receivedMsg)
	{
		// Handshake messages between end-user and Remote Management System
		// End-user							Remote Management System 
		// 		LOGIN_REQUEST (0) --->
		//						<--	LOGIN_REQUEST(0) + random number
		//
		//		LOGIN_PASSCODE(1) ---->
		//						<--- LOGIN_SUCCESS(2)
		//
		//		LOGIN_NORMAL_MSG(4)  ---->
		//
		// 		LOGIN_CHANGE_CODE(6)  --->
		//						<--- LOGIN_REQUEST(0) + random number

			
		int loginValue = LOGIN_REQUEST;	
		int nextStep = LOGIN_REQUEST;
		
		String smsSendingMessage = "";
		String cryptoPassCode = "";
		String[] parsedMessage = new String[INDEX_MSG_END];
		
		showResultMessage(MSG_ID_RECEIVED, receivedMsg);	// Debugging
		
		parsedMessage = smsData.splitMessageField(receivedMsg);		
		
		if(parsedMessage == null)
		{
			Log.e(CLASS_NAME, "The received sms has an invalid format.");
			showResultMessage(MSG_ID_ERROR, "parsedMessage is null");
			return;
		}
	
		loginValue = Integer.parseInt(parsedMessage[INDEX_LOGIN]);
		
		switch(loginValue)
		{
		case LOGIN_REQUEST:
			mUserPassCode = mEditPassCode.getText().toString();
			mRandomNumber = parsedMessage[INDEX_BODY];
			
			showResultMessage(MSG_ID_RECEIVED,
								"LOGIN_REQUEST(random): " + mUserPassCode + ":" + mRandomNumber);
			cryptoPassCode = cryptoPassCode(mUserPassCode, mRandomNumber);
			
			nextStep = LOGIN_PASSCODE;
			smsSendingMessage = smsData.makeCRMMessage(cryptoPassCode, nextStep);
			break;
		case LOGIN_SUCCESS:
			// save passcode to SMSData class
			mAuthorizedPassCode = parsedMessage[INDEX_PASSCODE];
			smsData.saveAuthorizedPassCode(mAuthorizedPassCode);
			smsData.setAuthenticationValue(true);
			
			showResultMessage(MSG_ID_RECEIVED, "LOGIN_SUCCESS : " + receivedMsg);	// For debugging	
			
			nextStep = LOGIN_NORMAL_MSG;			
			showResultMessage(MSG_ID_SENT, "Authentication succeeded! : ");			
			break;
		case LOGIN_FAILED:
			// 화면 출력 Failed
			showResultMessage(MSG_ID_ERROR, "Login failed : " + receivedMsg);			
			showAlertErrorDialog("Error", "Login failed");
			smsData.setAuthenticationValue(false);
			nextStep = LOGIN_REQUEST;
			
			break;
		case LOGOUT:
			// 향후 기능 구현: 이전 Activity로 이동			
			showResultMessage(MSG_ID_RECEIVED, "Logged out by the server.");
			smsData.setAuthenticationValue(false);
			
			initMemberVariables();
			startActivity(new Intent(Authentication.this, Login.class));
			finish();
			break;
		}
		
		
		if(nextStep == LOGIN_PASSCODE)
		{
			// Send SMS
			showResultMessage(MSG_ID_SENT, "Sent Message: " + smsSendingMessage);
			sendSMStoRemoteSystem(mRemoteSitePhoneNumber, smsSendingMessage);
		}
		///////////////////////////////////////////////////////////////////
		if(nextStep == LOGIN_NORMAL_MSG)
		{
			Intent intent = new Intent(Authentication.this, Home.class);
			startActivity(intent);
			finish();
		}
		
	}
	/*=============================================================================
	 * Name: cryptoPassCode
	 * 
	 * Description:
	 * 		XOR operation with the received key from the remote management system
	 *=============================================================================*/		
	public String cryptoPassCode(String userKey, String remoteKey)
	{
		int userKeyInt = Integer.parseInt(userKey);
		int remoteKeyInt = Integer.parseInt(remoteKey);
		int resultCodeInt = 0;
		
		String result="";
		
		resultCodeInt = (userKeyInt ^ remoteKeyInt)%1000;
		
		if(resultCodeInt<10 && resultCodeInt>=0)
			result += "00";
		else if(resultCodeInt>=10 && resultCodeInt<100)
			result += "0";
		
		result += Integer.toString(resultCodeInt);
		
		return result;
	}

	/*=============================================================================
	 * Name: showAlertErrorDialog
	 * 
	 * Description:
	 * 		Show an alert dialog when an error happens
	 *=============================================================================*/	
	private void showAlertErrorDialog(String title, String message)
	{
		AlertDialog.Builder alertDlg = new AlertDialog.Builder(Authentication.this);
	
		alertDlg.setTitle(title);
		alertDlg.setMessage(message);
		alertDlg.setIcon(R.drawable.icon);
		alertDlg.setPositiveButton("Close", null);
		alertDlg.show();
	}

	private void initMemberVariables()
	{
		mRemoteSiteLocation = "";
		mRemoteSitePhoneNumber = "";
		mRemoteSiteDevice = "";
		
		mUserPassCode = "";
		mEditPassCode.setText("");
		
		mLocationTextView.setText("Location");		
		mPhoneNumTextView.setText("Phone Number");	
		mDeviceTextView.setText("Device");
		mResultTextView.setText("");
	}
	
	private void showResultMessage(int type, String msg)
	{
		if(type == MSG_ID_SENT)
		{
			mResultTextView.append("# " + msg);
			Log.i(CLASS_NAME, "Sent Msg: " + msg);
		}
		else if(type == MSG_ID_RECEIVED)
		{
			mResultTextView.append(">> " + msg);
			Log.i(CLASS_NAME, ">> " +msg);
		}
		else
		{
			mResultTextView.append("# ERROR: " + msg);
			Log.i(CLASS_NAME, "Error: " + msg);
		}
		mResultTextView.append("\n");
	}

	public int convertChartoInt(char ch)
	{
		int num = Character.digit(ch, 10);
		
		return num;
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

		mRemoteSiteLocation = remoteSiteInfo[0];
		mRemoteSitePhoneNumber = remoteSiteInfo[1];
		mRemoteSiteDevice = remoteSiteInfo[2];
		mAuthorizedPassCode = remoteSiteInfo[3];

		mLocationTextView.setText(mRemoteSiteLocation);		
		mPhoneNumTextView.setText(mRemoteSitePhoneNumber);	
		mDeviceTextView.setText(mRemoteSiteDevice);

		Log.i(CLASS_NAME, "Pass Code : " + mAuthorizedPassCode);

	}

}
