package condroid.RemoteManagement;

import android.app.*;
import android.content.*;
import android.os.*;
import android.telephony.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class OpticalAmpActivity extends Activity{
	final static int MSG_ID_SENT = 0;
	final static int MSG_ID_RECEIVED = 1;
	final static int MSG_ID_ERROR = 2;

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
	String mSelectedCommand;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.opamp);
		
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
		
		getRemoteSiteInfo();
		showRemoteSiteInfo();
		
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

}
