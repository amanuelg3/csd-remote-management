package condroid.RemoteManagement;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;

public class SplashWindow extends Activity 
{
	private static final String CLASS_NAME = "SplashWindow";
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		final int timeoutScreenDisplay = 1000;
		/** create a thread to show splash up to splash time */
		Thread timeoutThread = new Thread() 
		{

			int wait = 0;
	
			@Override
			public void run() 
			{
				try 
				{
					super.run();
					/**
					* use while to get the splash time. Use sleep() to increase
					* the wait variable for every 100L.
					*/
					while (wait < timeoutScreenDisplay) 
					{
						sleep(100);
						wait += 100;
					}
				} 
				catch (Exception e) 
				{
					Log.e(CLASS_NAME, "Exception=" + e);
				} 
				finally 
				{
					/**
					* Called after splash times up. Do some action after splash
					* times up. Here we moved to another main activity class
					*/
					startActivity(new Intent(SplashWindow.this, Login.class));
					finish();
				}
			}
		};
		timeoutThread.start();

	}
}

