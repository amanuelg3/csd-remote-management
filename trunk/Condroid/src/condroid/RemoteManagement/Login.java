package condroid.RemoteManagement;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class Login extends Activity {
	private static final String CLASS_NAME = "Login";
	private static final String PREFERENCE_NAME = "RemoteMng";
	private static final int PASSWORD_LENGTH = 4;
	
	final static int SUCCESS = 0;
	final static int FAIL = -1;
	
	String mPassword = "";
	
	/*
	 * Variables to be saved in SharedPreferences file(RemoteMng.xml)
	 */
	String mValidPassWord = "";
	
	/*
	 * Widgets
	 */
	EditText passWordEditText;
	
	Button loginButton;
	Button cancelButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);		
		
		
		loginButton = (Button)findViewById(R.id.login_button);
		cancelButton = (Button)findViewById(R.id.login_cancel_button);
		
		passWordEditText = (EditText)findViewById(R.id.password);
		passWordEditText.setText("");
		
		loginButton.setOnClickListener(mClickListener);
		cancelButton.setOnClickListener(mClickListener);
	
		// Limit the input digits up to 4 digits
		passWordEditText.setFilters(new InputFilter[] {
				new InputFilter.LengthFilter(PASSWORD_LENGTH)
		});

				
	}
	
	Button.OnClickListener mClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId())
			{
			case R.id.login_button:
				if(checkAuthentication() == true)
				{
					//startActivity(new Intent(Login.this, Authentication.class));
					startActivity(new Intent(Login.this, Home.class));
					finish();

				}
				else
				{
					// Display DialogBox
					AlertDialog.Builder errDlg = new AlertDialog.Builder(Login.this);
					errDlg.setTitle("Error");
					errDlg.setMessage("Incorrect Password. Try again.");
					errDlg.setIcon(R.drawable.icon);
					errDlg.setNegativeButton("Close", null);
					errDlg.show();
				}
				break;
			case R.id.login_cancel_button:
				clearText();
				break;
			}
		}
		
	};
	
	/*=============================================================================
	 * Name: checkAuthentication
	 * 
	 * Description:
	 * 		Compare user input password with the saved password
	 *=============================================================================*/	
	protected boolean checkAuthentication()
	{
		boolean result = false;
	
		mPassword = passWordEditText.getText().toString();
		
		// Using SharedPreferences class for management of login information 
		SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
		String passWord = pref.getString("Password", "1111");

		if(mPassword.equals(passWord))
		{
			Log.i(CLASS_NAME, "Local Authentication succeed!");
			result = true;
		}
		else
		{
			Log.e(CLASS_NAME, "Mismatched(Valid Password:" + passWord + ")");
			passWordEditText.setText("");
			result = false;
		}
		
		return result;		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		clearText();
	}

	protected void clearText()
	{
		passWordEditText.setText("");		
		mPassword = "";

	}

	
}
