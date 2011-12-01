package condroid.RemoteManagement;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.util.*;
import android.widget.*;

public class RemoteManagementActivity extends TabActivity {
    /** Called when the activity is first created. */
	TabHost mTab;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        // Make tabs
        // Custom Command | Optical Amplifier | Configuration
        TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec("customcmd")
        		.setIndicator("Custom Command")
        		.setContent(new Intent(this, CustomCmdActivity.class)));
        
        tabHost.addTab(tabHost.newTabSpec("guicmd")
        		.setIndicator("GUI Command")
        		.setContent(new Intent(this, GUICmdActivity.class)));
        
        tabHost.addTab(tabHost.newTabSpec("admin")
        		.setIndicator("Administration Command")
        		.setContent(new Intent(this, Administration.class)));
        
        for(int tab=0; tab < tabHost.getTabWidget().getChildCount(); tab++)
        {
        	Log.i("config", "tab= " + tab + "height = " 
        			+ tabHost.getTabWidget().getChildAt(tab).getLayoutParams().height);
        	tabHost.getTabWidget().getChildAt(tab).getLayoutParams().height = 60;
        	
        }
    
    }
}
