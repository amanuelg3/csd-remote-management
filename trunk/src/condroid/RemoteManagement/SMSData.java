package condroid.RemoteManagement;


import android.util.*;

public class SMSData {

	final static int SUCCESS = 0;
	final static int ERROR = -1;
	
	static final int HEADER_SIZE = 10;
	static final int CRM_LENGTH = 3;
	static final int PASSCODE_LENGTH = 3;
	static final int MAXIMUM_LENGTH = 140;
	
	static final long LOGOUT_TIME = (1000 * 5);	// 3 seconds
	
	static final int MAXIMUM_HISTORY_NUMBER = 100;	// 100
	/*
	 * Error number definition
	 */
	static final int CRM_MISSING_ERROR 	= 1;
	static final int PASSCODE_MISMATCH_ERROR 	= 2;
	static final int LOGIN_INFO_ERROR		= 3;
	
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
	 * The index of LOGIN field in a received SMS
	 */
	static final int INDEX_SMS_LOGIN = 8;
	/*
	 * Login field definition
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

	final static int DEVICE_OPAMP_ID 		= 0x1;
	final static int DEVICE_WIFI_ID 		= 0x2;
	final static int DEVICE_WATER_SENSOR_ID = 0x4;
	final static int DEVICE_ETC_ID 			= 0x8;

	/*
	 * Class variables (Shared variables)
	 */
	static String mRemoteLocation = "Location";		// Selected database columns
	static String mRemotePhoneNumber = "Phone Number";
	static String mRemoteDeviceType = "Device";
	
	static String mUserPassCode = "";
	static String mRandomNumber = "";
	static String mCryptoPassCode = "";
	static String mAuthorizedPassCode = "000";
	
	char[] mMessageHeader = new char[HEADER_SIZE];
	
	String[] mCRMHeader;

	static boolean isAuthenticated = false;
	// Constructor
	public SMSData()
	{
		/* CRM
		 * 	+-----+----------+-------+-------+-------+--------+------------------------+
		 *  | CRM | passcode | multi | order | login | device |         Body           | 
		 *  +-----+----------+-------+-------+-------+--------+------------------------+
		 *     3      3         1        1       1        1            130
		 */
		mCRMHeader = new String[] {"CRM", "000", "0", "0", "0", "0", "sms body"};
		
	}
	
	public String setMessageHeader(char[] crm, char[] passcode, char multi, 
									char order, char login, char device)
	{
		int index = 0;
		String strHeader = "";
		
		System.arraycopy(crm, 0, mMessageHeader, index, crm.length);
		index = crm.length;
		
		System.arraycopy(passcode, 0, mMessageHeader, index, passcode.length);
		index += passcode.length;
		
		mMessageHeader[INDEX_MULTI] = multi;
		mMessageHeader[INDEX_ORDER] = order;
		mMessageHeader[INDEX_LOGIN] = login;
		mMessageHeader[INDEX_DEVICE] = device;
		
		strHeader = mMessageHeader.toString();
		Log.i("sms", "mMessageHeader: " + strHeader);
		
		return strHeader;
	}
	


	
	public void saveUserPasscode(String code)
	{
		mUserPassCode = code;
	}
	public String getUserPasscode()
	{
		return mUserPassCode;
	}
	
	/*
	 * Save authorized passcode to SMSData class for the next usage in other classes
	 */
	public void saveAuthorizedPassCode(String passcode)
	{
		mAuthorizedPassCode = passcode;
		
	}
	
	public void setAuthenticationValue(boolean value)
	{
		isAuthenticated = value;
		Log.i("sms", "setAuthenticationValue = " + isAuthenticated);
	}
	
	public boolean getAuthenticationValue()
	{
		Log.i("sms", "getAuthenticationValue = " + isAuthenticated);
		return isAuthenticated;
	}
	
	public String getAuthorizedPasscode()
	{
		return mAuthorizedPassCode;
	}
	
	public void deletePassCode()
	{
		mAuthorizedPassCode = "000";
		Log.i("sms", "Authorized Passcode is deleted by LOGOUT message.");
	}
	
	/*=============================================================================
	 * Name: saveRemoteSiteInfo
	 * 
	 * Description:
	 * 		Save the selected item from database to SMSData class static variables
	 *=============================================================================*/	
	public void saveRemoteSiteInfo(String location, String phonenum, String device)
	{
		mRemoteLocation = location;
		mRemotePhoneNumber = phonenum;
		mRemoteDeviceType = device;
		Log.i("sms", "Location: " + mRemoteLocation);
		Log.i("sms", "Phone Number: " + mRemotePhoneNumber);
		Log.i("sms", "Device Type: " + mRemoteDeviceType);
	}

	/*=============================================================================
	 * Name: getRemoteSiteInformation
	 * 
	 * Description:
	 * 		Return remote site information which can be used in other classes
	 *=============================================================================*/		
	public String[] getRemoteSiteInformation()
	{
		String[] remoteSiteInfo = new String[4];
		
		remoteSiteInfo[0] = mRemoteLocation;
		remoteSiteInfo[1] = mRemotePhoneNumber;
		remoteSiteInfo[2] = mRemoteDeviceType;
		remoteSiteInfo[3] = mAuthorizedPassCode;
		
		return remoteSiteInfo;
	}
	/*=============================================================================
	 * Name: resetRemoteSiteInformation
	 * 
	 * Description:
	 * 		Initialize Remote site information with the default values
	 *=============================================================================*/			
	public void resetRemoteSiteInformation()
	{
		mRemoteLocation = "Location";		// Selected database columns
		mRemotePhoneNumber = "Phone Number";
		mRemoteDeviceType = "Device";

	}
	
	/*=============================================================================
	 * Name: getRemoteLocationInfo
	 * 
	 * Description:
	 * 		Return remote site location
	 *=============================================================================*/			
	public String getRemoteLocationInfo()
	{
		Log.i("sms", "[SMS] RemoteLocation: " + mRemoteLocation);
		return mRemoteLocation;
	}
	/*=============================================================================
	 * Name: getRemotePhoneNumber
	 * 
	 * Description:
	 * 		Return remote site phone number
	 *=============================================================================*/				
	public String getRemotePhoneNumber()
	{
		Log.i("sms", "[SMS] RemotePhoneNumber: " + mRemotePhoneNumber);
		return mRemotePhoneNumber;
	}
	/*=============================================================================
	 * Name: getRemoteDeviceType
	 * 
	 * Description:
	 * 		Return remote site device type
	 *=============================================================================*/
	public String getRemoteDeviceType()
	{
		Log.i("sms", "[SMS] RemoteDevice: " + mRemoteDeviceType);
		return mRemoteDeviceType;
	}
	
	/*=============================================================================
	 * Name: makeCRMMessage
	 * 
	 * Description:
	 * 		Make a header for authentication
	 *=============================================================================*/		
	public String makeCRMMessage(String code, int nextStep)
	{
		/*
		 * CRM Header Format (10 bytes)
		 * 	+-----+----------+-------+-------+-------+--------+
		 *  | CRM | passcode | multi | order | login | device |
		 *  +-----+----------+-------+-------+-------+--------+
		 *     3      3         1        1       1        1
		 *     
		 *  login field
		 *	- LOGIN_REQUEST		= 0;
		 *	- LOGIN_PASSCODE	= 1;
		 *	- LOGIN_SUCCESS		= 2;;
		 *	- LOGIN_FAILED		= 3;
		 *	- LOGIN_NORMAL_MSG 	= 4;
		 *	- LOGOUT			= 5;
		 *	- LOGIN_CHANGE_CODE	= 6;
		 */
		
		String header = "";
		
		String crm = "CRM";		
		String passcode = "";
		int multiFlag = 1;
		int orderField = 1;
		int loginField = 0;
		int deviceField = 0;

		passcode = code;
		loginField = nextStep;
		deviceField = getDeviceType();
		
		header = crm + passcode + Integer.toString(multiFlag)
				+ Integer.toString(orderField) + Integer.toString(loginField)
				+ Integer.toString(deviceField);
		
		return header;
	}
	/*=============================================================================
	 * Name: makeCommandHeader
	 * 
	 * Description:
	 * 		Make a header for custom and normal command
	 *=============================================================================*/			
	public String makeCommandHeader(String code, int nextStep, int length)
	{
		String cmdHeader = "";
		
		int multiField = 0;
		int orderField = 0;
		int loginField = 0;
		int deviceField = 0;
		
		String crm = "CRM";		
		
		deviceField = getDeviceType();
		loginField = nextStep;

		if(length + HEADER_SIZE > MAXIMUM_LENGTH)
		{
			multiField++; orderField++;
		}
		
		cmdHeader = crm + code + Integer.toString(multiField)
		+ Integer.toString(orderField) + Integer.toString(loginField)
		+ Integer.toString(deviceField);
		
		return cmdHeader;
	}
	/*=============================================================================
	 * Name: splitMessageField
	 * 
	 * Description:
	 * 		Split a SMS message to String array
	 *=============================================================================*/		
	public String[] splitMessageField(String msg)
	{
		String[] header = new String[INDEX_MSG_END];
		
		int eindex = CRM_LENGTH;
		int sindex = 0;
		
		//System.out.println("Header: " + msg);
		
		if(validationCheck(msg) == SUCCESS)
		{
			header[INDEX_CRM] = msg.substring(sindex, eindex);
			sindex += eindex; 
			eindex = sindex + PASSCODE_LENGTH; 
			
			header[INDEX_PASSCODE] = msg.substring(sindex, eindex);
			header[INDEX_MULTI] = msg.substring(eindex, ++eindex);
			header[INDEX_ORDER] = msg.substring(eindex, ++eindex);
			header[INDEX_LOGIN] = msg.substring(eindex, ++eindex);
			header[INDEX_DEVICE] = msg.substring(eindex, ++eindex);
			header[INDEX_BODY] = msg.substring(eindex);
			
			for(int i=0 ; i<=INDEX_BODY; i++)
				Log.i("sms", "[" + i + "]" + header[i]);
			
		}
		else
		{
			header = null;
		}
		return header;

	}
	/*=============================================================================
	 * Name: checkLoginValue
	 * 
	 * Description:
	 * 		Check Login field value for authentication and LOGOUT process
	 *=============================================================================*/			
	public int checkLoginValue(String msg)
	{
		int login = 0;
		String strLogin = "";
		
		strLogin = msg.substring(INDEX_SMS_LOGIN, INDEX_SMS_LOGIN+1);
		login = Integer.parseInt(strLogin);
		
		return login;
	}
	/*=============================================================================
	 * Name: validationCheck
	 * 
	 * Description:
	 * 		Compare the CRM field for message validation
	 * 		If the message contains "CRM" string, it is a valid message. 
	 *=============================================================================*/			
	public int validationCheck(String msg)
	{
		int result = SUCCESS;
		String token = msg.substring(0, CRM_LENGTH);
		
		if(!token.equals("CRM"))
		{
			Log.e("sms", "No CRM field: " + token);
			result = CRM_MISSING_ERROR;
		}
		
		return result;
	}
	/*=============================================================================
	 * Name: cryptoPassCode
	 * 
	 * Description:
	 * 		XOR operation with the received key from the remote management system
	 *=============================================================================*/		
	public String cryptoPassCode(String userKey, String remoteKey)
	{
		int arrayLength = PASSCODE_LENGTH;				

		byte userKeyChar[] = new byte[arrayLength];
		byte remoteKeyChar[] = new byte[arrayLength];			
		byte resultCodeChar[] = new byte[arrayLength];

		userKeyChar = userKey.getBytes();		// End-user input passcode
		remoteKeyChar = remoteKey.getBytes();	// the received key from the remote system
		
		if(userKey.getBytes().length != PASSCODE_LENGTH || 
			remoteKey.getBytes().length != PASSCODE_LENGTH)
		{
			Log.e("sms", "Invalid Remote Key: " + arrayLength);
			return null;
		}
	
		//XOR 연산
		for(int x=0; x< arrayLength; x++)
		{
			resultCodeChar[x] = (byte) (userKeyChar[x] ^ remoteKeyChar[x]);			
		}

		mUserPassCode = userKey;
		mRandomNumber = remoteKey;
		//mCryptoPassCode = new String(resultCodeChar);
		
		return new String(resultCodeChar);
	}

	/*=============================================================================
	 * Name: getDeviceType
	 * 
	 * Description:
	 * 		Get a device name which is attaced to the remote site
	 *=============================================================================*/		
	private int getDeviceType()
	{
		int type = 0;
		String devType = "";
		
		devType = mRemoteDeviceType;
		
		Log.i("remo", "Device Type: " + devType);
		
		if(devType.equals("opamp"))
		{
			type = DEVICE_OPAMP_ID;
		}
		else if(devType.equals("wifi"))
		{
			type = DEVICE_WIFI_ID;
		}
		else if(devType.equals("water_sensor"))
		{
			type = DEVICE_WATER_SENSOR_ID;
		}
		else
		{
			type = DEVICE_ETC_ID;
		}
		
		return type;
	}	
	
	public boolean isPasscodeChanged(String prevCode, String currentCode)
	{
		boolean isChanged = false;
		
		if(prevCode.equals(currentCode))
			isChanged = false;
		else
			isChanged = true;
		
		return isChanged;
	}
	
	/*=============================================================================
	 * Name: getLoginFieldValue
	 * 
	 * Description:
	 * 		Get the login field value for processing the next step
	 *=============================================================================*/
	public int getLoginFieldValue(char ch)
	{
		int ilogin=0;		
		
		ilogin = Character.digit(ch, 10);		
		
		return ilogin;
	}
}

