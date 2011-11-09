package condroid.RemoteManagement;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class Login extends Activity {
	final static String ValidUserName = "admin";
	final static String ValidPassword = "1234";
	final static int SUCCESS = 0;
	final static int FAIL = -1;
	
	String mUserName = null;
	String mPassword = null;
	
	EditText userName;
	EditText passWord;
	
	Button loginButton;
	Button cancelButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);

		
		userName = (EditText)findViewById(R.id.username);
		passWord = (EditText)findViewById(R.id.password);
		loginButton = (Button)findViewById(R.id.login_button);
		cancelButton = (Button)findViewById(R.id.login_cancel_button);
		
		userName.setText("admin");
		passWord.setText("1234");
		
		loginButton.setOnClickListener(mClickListener);
		cancelButton.setOnClickListener(mClickListener);
		
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
					startActivity(new Intent(Login.this, Authentication.class));

				}
				else
				{
					// Display DialogBox
					AlertDialog.Builder errDlg = new AlertDialog.Builder(Login.this);
					errDlg.setTitle("Error");
					errDlg.setMessage("You put wrong ID or password. Try again.");
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
	
	protected boolean checkAuthentication()
	{
		boolean result = false;
		mUserName = userName.getText().toString();
		mPassword = passWord.getText().toString();
		
		Log.i("remo", "User: " + mUserName);
		Log.i("remo", "Password: " + mPassword);		
		
		if(mUserName.equals(ValidUserName) && mPassword.equals(ValidPassword))
		{
			Log.i("remo", "Local Authentication succeed!");
			result = true;
		}
		else
		{
			Log.e("remo", "Mismatched(UserName:  " + mUserName + " Password: " + mPassword);
			result = false;
		}
		
		return result;
		
	}

	protected void clearText()
	{
		userName.setText("");
		passWord.setText("");
		mUserName = "";
		mPassword = "";

	}
}
