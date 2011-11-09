package condroid.RemoteManagement;

import android.app.*;
import android.content.*;
import android.os.*;
import android.telephony.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import condroid.RemoteManagement.SMSData;

public class CustomCmdActivity extends Activity {

	final static int MSG_ID_SENT = 0;
	final static int MSG_ID_RECEIVED = 1;
	final static int MSG_ID_ERROR = 2;
	/*
	 * TextView Widgets
	 */
	TextView mLocationTextView;
	TextView mPhoneNumberTextView;
	TextView mDeviceTextView;
	TextView mCustomResultTextView;
	
	/*
	 * Buttons
	 */
	Button customCmdButton;
	Button customClearButton;
	
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

	/* Member Variables
	 * 
	 */
	String mCustomLocation;
	String mCustomPhoneNumber;
	String mCustomDevice;
	String mAuthorizedPassCode;
	
	String mSender="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_cmd);
		
		smsData = new SMSData();
		Log.i("custom", "onCreate");
	
		mCustomLocation = "";
		mCustomPhoneNumber = "";
		mCustomDevice = "";
		mAuthorizedPassCode = "";
		
		mCustomResultTextView = (TextView)findViewById(R.id.custom_result_view);
		mLocationTextView = (TextView)findViewById(R.id.custom_location);
		mPhoneNumberTextView = (TextView)findViewById(R.id.custom_phonenumber);
		mDeviceTextView = (TextView)findViewById(R.id.custom_device);
		
		customCmdButton = (Button)findViewById(R.id.custom_cmd_send_button);
		customCmdButton.setOnClickListener(CustomClickListener);
		
		customClearButton = (Button)findViewById(R.id.custom_cmd_clear_button);
		customClearButton.setOnClickListener(CustomClickListener);

		mCustomCmdEditText = (EditText)findViewById(R.id.command_edit);

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
        			Log.i("sms", "Authentication.java: Bundle data received");
        			
                    // SMS uses a data format known as a PDU
                    Object pdus[] = (Object[]) data.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                                      
                    for (int i=0; i<msgs.length; i++)
                    {
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);      
                        mSender = msgs[i].getOriginatingAddress();
                        //message += msgs[i].getOriginatingAddress() + " ";                     
                        recvMessage += msgs[i].getMessageBody().toString();                              
                    }
  
                    Log.i("sms", recvMessage);
                    // Process received SMS message for further authentication process
                    
                    processReceivedCustomMessage(recvMessage);
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
	protected void onPause() {
		// TODO Auto-generated method stub
		try {
			if(smsMsgSent != null) {
				unregisterReceiver(smsMsgSent);
				smsMsgSent = null;
			}
		}catch(Exception e) {
			Log.e("sms", "Failed to unregister smsMsgSent");
		}
		
		
		try {
			if(smsMsgIncoming != null) {
				unregisterReceiver(smsMsgIncoming);
				smsMsgIncoming = null;
			}
		}catch(Exception e) {
			Log.e("sms", "Failed to unregister smsMsgIncoming");
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
				Log.i("sms", "unregisterReceiver(smsMsgSent");
			}
		}catch(Exception e) {
			Log.e("sms", "Failed to unregister smsMsgSent");
		}
		
		
		try {
			if(smsMsgIncoming != null) {
				unregisterReceiver(smsMsgIncoming);
				smsMsgIncoming = null;
				Log.i("sms", "unregisterReceiver(smsMsgIncoming");
			}
		}catch(Exception e) {
			Log.e("sms", "Failed to unregister smsMsgIncoming");
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
				sendCustomCommand();
				break;
			case R.id.custom_cmd_clear_button:
				mCustomCmdEditText.setText("");
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
				clearRemoteSiteInfo();		// Initialize local variables
				smsData.deletePassCode();	// Delete the authorized passcode
				
				showResultMessage(MSG_ID_RECEIVED, "LOGOUT: Go back to Login.");
				// Return to Login screen
				Intent intent = new Intent(CustomCmdActivity.this, Login.class);
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
     * Function Name: sendCustomCommand
     * 
     * Description
     * 		Send a SMS including a CRM header and a message body  
     *===========================================================================*/	
	private void sendCustomCommand()
	{
		String customCmdMsg = mCustomCmdEditText.getText().toString();
		int len = customCmdMsg.length();
		String header = ""; 
		
		//header = smsData.makeCommandHeader(mAuthorizedPassCode, SMSData.LOGIN_NORMAL_MSG, len);
		header = smsData.makeCommandHeader("123", SMSData.LOGIN_NORMAL_MSG, len);
		customCmdMsg = header + customCmdMsg;
		sendSMStoRemoteSystem(mCustomPhoneNumber, customCmdMsg);
		
		showResultMessage(MSG_ID_SENT, "Sending : " + customCmdMsg);
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
		}
		else if(msgType == MSG_ID_RECEIVED)
		{
			mCustomResultTextView.append(">> " + msg);
		}
		else
		{
			mCustomResultTextView.append("# Error " + msg);
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
		
		Log.i("custom", "Pass Code : " + mAuthorizedPassCode);
		/*
		mCustomLocation = SMSData.mRemoteLocation;
		Log.i("custom", "Location: " + mCustomLocation);
		
		mCustomPhoneNumber = SMSData.mRemotePhoneNumber;
		Log.i("custom", "Phone Number: " + mCustomPhoneNumber);
		
		mCustomDevice = SMSData.mRemoteDeviceType;
		Log.i("custom", "Device: " + mCustomDevice);
		
		mAuthorizedPassCode = smsData.getAuthorizedPasscode();
		Log.i("custom", "Device: " + mCustomDevice);
		*/
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
			Log.i("custom", mCustomLocation);
			mLocationTextView.setText(mCustomLocation);
		}
		else
		{
			Log.e("custom", "Remote Location is null.");
		}
		
		if(mCustomPhoneNumber.length() != 0)
		{
			Log.i("custom", mCustomPhoneNumber);
			mPhoneNumberTextView.setText(mCustomPhoneNumber);
		}
		else
		{
			Log.e("custom", "Remote Phone Number is null.");
		}
		
		if(mCustomDevice.length() != 0)
		{
			Log.i("custom", mCustomDevice);
			mDeviceTextView.setText(mCustomDevice);
		}
		else
		{
			Log.e("custom", "Remote Device is null.");
		}

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
