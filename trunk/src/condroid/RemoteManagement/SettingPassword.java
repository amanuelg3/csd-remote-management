package condroid.RemoteManagement;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class SettingPassword extends Activity{
	private static final int PASSWORD_LENGTH = 4;
	private static final String PREFERENCE_NAME = "RemoteMng";
	private static final String CLASS_NAME = "SettingPassword";
	/*
	 * EditText Widgets
	 */
	EditText mCurrentPasswordEditText;
	EditText mNewPasswordEditText;
	EditText mConfirmPasswordEditText;
	
	/*
	 * Button Widgets
	 */
	Button mChangeButton;
	Button mCancelButton;
	
	/*
	 * Member variables
	 */
	String mCurrentPassword ="";
	String mNewPassword = "";
	String mConfirmPassword="";
	
	String mSavedPassword ="";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_change_password);
		
		//EditText: limit input up to 4 digits
		mCurrentPasswordEditText = (EditText)findViewById(R.id.current_password);
		mCurrentPasswordEditText.setFilters(new InputFilter[] {
			new InputFilter.LengthFilter(PASSWORD_LENGTH)
		});
		
		mNewPasswordEditText = (EditText)findViewById(R.id.new_password);
		mNewPasswordEditText.setFilters(new InputFilter[] {
			new InputFilter.LengthFilter(PASSWORD_LENGTH)
		});
		
		mConfirmPasswordEditText = (EditText)findViewById(R.id.confirm_password);
		mConfirmPasswordEditText.setFilters(new InputFilter[] {
			new InputFilter.LengthFilter(PASSWORD_LENGTH)
		});
		
		// Buttons
		mChangeButton = (Button)findViewById(R.id.change_password_button);
		mChangeButton.setOnClickListener(mClickListener);
		
		mCancelButton = (Button)findViewById(R.id.back_password_button);
		mCancelButton.setOnClickListener(mClickListener);
		
		ImageButton homeButton = (ImageButton)findViewById(R.id.password_home_image_button);
		homeButton.setOnClickListener(mClickListener);
		
		//mCurrentPasswordEditText.addTextChangedListener(watcher)
		clearEditText();
		readSavedPassword();
	}

	Button.OnClickListener mClickListener = new View.OnClickListener() 
	{
		public void onClick(View v)
		{
			switch(v.getId())
			{
			case R.id.change_password_button:
				changePassword();
				break;
			case R.id.back_password_button:
				clearEditText();
				startActivity(new Intent(SettingPassword.this, Settings.class));
				finish();
				break;
			case R.id.password_home_image_button:
				startActivity(new Intent(SettingPassword.this, Home.class));
				finish();
				break;
			}
		}
	};
	
	/*=============================================================================
	 * Name: readSavedPassword
	 * 
	 * Description:
	 * 		Read the password from a xml file(RemoteMng.xml)
	 *=============================================================================*/	
	private void readSavedPassword()
	{
		SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
		mSavedPassword = pref.getString("Password", "1111");		
	}
	/*=============================================================================
	 * Name: writeNewPassword
	 * 
	 * Description:
	 * 		Write a new password to a xml file(RemoteMng.xml)
	 *=============================================================================*/		
	private void writeNewPassword(String password)
	{
		SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
    	SharedPreferences.Editor edit = pref.edit();	    	    	
    	edit.putString("Password", password);
    	
    	edit.commit();
    	Log.d(CLASS_NAME, "New password is saved. " + password);

	}
	/*=============================================================================
	 * Name: changePassword
	 * 
	 * Description:
	 * 		Check that user-input password is matched with the saved password
	 * 		If it is matched, then change password with the new password
	 *=============================================================================*/		
	private void changePassword()
	{
		mCurrentPassword = mCurrentPasswordEditText.getText().toString();		
		mNewPassword = mNewPasswordEditText.getText().toString();		
		mConfirmPassword = mConfirmPasswordEditText.getText().toString();
		
		if(mCurrentPassword.length() == 0 || mNewPassword.length() == 0 
			|| mConfirmPassword.length() == 0)
		{
			showMessageDialog("Error", "Please enter password again.");
		}
		else
		{
			if(mCurrentPassword.equals(mSavedPassword))
			{
				if(mNewPassword.equals(mConfirmPassword))
				{
					// Save user-input password to a xml file
					writeNewPassword(mNewPassword);
					showMessageDialog("Done", "Password was changed.");
					Log.i(CLASS_NAME, "New password: " + mNewPassword);
					clearEditText();
				}
				else
				{
					
					showMessageDialog("Error", "Confirm Password is not equal to New Password.");
					Log.i(CLASS_NAME, "Passwords are mismatched. " 
							+ mNewPassword + "/" + mConfirmPassword);
					
					mNewPasswordEditText.setText("");
					mConfirmPasswordEditText.setText("");
				}
			}
			else
			{
				showMessageDialog("Error", "Current Password is incorrect.");
				Log.i(CLASS_NAME, "Current Password is incorrect. " + mCurrentPassword);
	
				clearEditText();
			}
		}
		
	}
	/*=============================================================================
	 * Name: clearEditText
	 * 
	 * Description:
	 * 		Clear EditText text and member variables
	 *=============================================================================*/		
	private void clearEditText()
	{
		mCurrentPasswordEditText.setText("");
		mNewPasswordEditText.setText("");
		mConfirmPasswordEditText.setText("");
		
		mCurrentPassword ="";
		mNewPassword = "";
		mConfirmPassword="";		
		//mSavedPassword ="";

	}
	/*=============================================================================
	 * Name: showAlertErrorDialog
	 * 
	 * Description:
	 * 		Show an alert dialog when an error happens
	 *=============================================================================*/	
	private void showMessageDialog(String title, String message)
	{
		AlertDialog.Builder alertDlg = new AlertDialog.Builder(SettingPassword.this);
	
		alertDlg.setTitle(title);
		alertDlg.setMessage(message);
		alertDlg.setIcon(R.drawable.icon);
		alertDlg.setPositiveButton("Close", null);
		alertDlg.show();
	}

}
