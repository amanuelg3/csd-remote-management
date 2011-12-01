package condroid.RemoteManagement;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class Home extends Activity{
	private static final String CLASS_NAME = "Home";
	
	SMSData smsData;
	boolean isAuth;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
	
		smsData = new SMSData();
		
		
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		isAuth = smsData.getAuthenticationValue();
		Log.i(CLASS_NAME, "onResume: isAuth= " + isAuth);
		
		// Image Button for Setting
		ImageButton imgAuthButton = (ImageButton)findViewById(R.id.home_authentication_button);
		imgAuthButton.setOnClickListener(mClickListener);
		imgAuthButton.setEnabled(true);
		
		ImageButton imgSettingButton = (ImageButton)findViewById(R.id.home_setting_button);
		imgSettingButton.setOnClickListener(mClickListener);
		imgSettingButton.setEnabled(true);
		
		ImageButton imgCustomButton = (ImageButton)findViewById(R.id.home_custom_button);
		imgCustomButton.setOnClickListener(mClickListener);
		imgCustomButton.setEnabled(isAuth);
		
		ImageButton imgGUIButton = (ImageButton)findViewById(R.id.home_gui_button);
		imgGUIButton.setOnClickListener(mClickListener);
		imgGUIButton.setEnabled(isAuth);
		
		ImageButton imgAdminButton = (ImageButton)findViewById(R.id.home_admin_button);
		imgAdminButton.setOnClickListener(mClickListener);
		imgAdminButton.setEnabled(isAuth);
		
		ImageButton imgAboutButton = (ImageButton)findViewById(R.id.home_about_button);
		imgAboutButton.setOnClickListener(mClickListener);
		imgAboutButton.setEnabled(true);

		ImageButton imgExitButton = (ImageButton)findViewById(R.id.home_exit_button);
		imgExitButton.setOnClickListener(mClickListener);
		imgExitButton.setEnabled(true);
	}
	
	Button.OnClickListener mClickListener = new View.OnClickListener()
	{
		public void onClick(View v) {
			switch(v.getId())
			{
			case R.id.home_authentication_button:
				startActivity(new Intent(Home.this, Authentication.class));
				finish();
				break;
			case R.id.home_setting_button:
				startActivity(new Intent(Home.this, Settings.class));
				finish();
				break;
			case R.id.home_custom_button:
				if(isAuth == true)
				{
					startActivity(new Intent(Home.this, CustomCmdActivity.class));
					finish();
				}
				else
					showAlertErrorDialog("Warning", "Athentication is needed.");				
				break;
			case R.id.home_gui_button:
				if(isAuth == true)
				{
					startActivity(new Intent(Home.this, GUICmdActivity.class));
					finish();
				}
				else
					showAlertErrorDialog("Warning", "Athentication is needed.");
				break;
			case R.id.home_admin_button:
				if(isAuth == true)
				{
					startActivity(new Intent(Home.this, Administration.class));
					finish();
				}
				else
					showAlertErrorDialog("Warning", "Athentication is needed.");				
				break;
			case R.id.home_about_button:
				startActivity(new Intent(Home.this, About.class));
				finish();
				break;
			case R.id.home_exit_button:
				System.exit(0);
				break;
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
		AlertDialog.Builder alertDlg = new AlertDialog.Builder(Home.this);
	
		alertDlg.setTitle(title);
		alertDlg.setMessage(message);
		alertDlg.setIcon(R.drawable.icon);
		alertDlg.setPositiveButton("Close", null);
		alertDlg.show();
	}

}
