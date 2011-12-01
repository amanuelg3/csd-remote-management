package condroid.RemoteManagement;

import java.util.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;

public class Settings extends Activity{
	private static final String CLASS_NAME = "Settings";
	
	final static int INDEX_USER_INFO 			= 0;
	final static int INDEX_COMMAND_HISTORY		= 1;
	
	final static int INDEX_REMOTESITE_MANAGER 	= 2;
	final static int INDEX_GUI_COMMAND_MANAGER 	= 3;
	
	
	ListView mSettingList;
	ArrayList<SettingItem> arSettingItem;
	SettingItem mSettingItem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		ImageButton imgHomeButton = (ImageButton)findViewById(R.id.setting_home_button);
		imgHomeButton.setOnClickListener(mClickListener);
		
		arSettingItem = new ArrayList<SettingItem>();
		
		// Change Password
		SettingItem sItem;
		sItem = new SettingItem(R.drawable.save_key_icon, 
								"Change Password", 
								R.drawable.right_arrow);
		arSettingItem.add(sItem);
		
		// Command history
		sItem = new SettingItem(R.drawable.history,
								"Command History",
								R.drawable.right_arrow);
		arSettingItem.add(sItem);
		
		// Remote site Management
		sItem = new SettingItem(R.drawable.new_db, 
								"Remote Site Management", 
								R.drawable.right_arrow);
		arSettingItem.add(sItem);
		
		// GUI Command Management
		sItem = new SettingItem(R.drawable.new_db, 
								"GUI Command Management", 
								R.drawable.right_arrow);
		arSettingItem.add(sItem);
		
		
		MyListAdapter MyAdapter = new MyListAdapter(this, 
									R.layout.setting_icon_text, arSettingItem);
		
		mSettingList = (ListView)findViewById(R.id.setting_list);
		mSettingList.setAdapter(MyAdapter);
		
		mSettingList.setOnItemClickListener(mSettingItemClickListener);
		

	}
	/*=============================================================================
	 * Name: Button.OnClickListener
	 * 
	 * Description:
	 * 		- Handle Home button 		
	 *=============================================================================*/			
	Button.OnClickListener mClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId())
			{
			case R.id.setting_home_button:
				startActivity(new Intent(Settings.this, Home.class));
				finish();
				break;
			}
		}
	};
	/*=============================================================================
	 * Name: ListView.OnItemClickListener
	 * 
	 * Description:
	 * 		- Executes the related Activities when a user clicks a list view 		
	 *=============================================================================*/	
	AdapterView.OnItemClickListener mSettingItemClickListener = new AdapterView.OnItemClickListener() 
	{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,	long id) 
		{
			switch(position)
			{
			case INDEX_USER_INFO:
				Log.i(CLASS_NAME, "Selected User Info");
				startActivity(new Intent(Settings.this, SettingPassword.class));
				finish();
				break;
			case INDEX_REMOTESITE_MANAGER:
				Log.i(CLASS_NAME, "Selected Remote site database");
				startActivity(new Intent(Settings.this, SettingRemoteSiteManager.class));
				finish();
				break;
			case INDEX_GUI_COMMAND_MANAGER:
				Log.i(CLASS_NAME, "Selected Command database");
				startActivity(new Intent(Settings.this, SettingGUICmdManager.class));
				finish();
				break;
			case INDEX_COMMAND_HISTORY:
				Log.i(CLASS_NAME, "Selected Command History");
				startActivity(new Intent(Settings.this, SettingCommandHistory.class));
				finish();
				break;
			}
			
		}
		
	};
	

}

/*=============================================================================
 * Name: class SettingItem
 * 
 * Description:
 * 		- For the custom list items(icon + text + icon) 		
 *=============================================================================*/	
class SettingItem 
{
	int mIcon;
	String mItem;
	int mArrowIcon;
	
	SettingItem(int icon, String item, int arrow_icon)
	{
		mIcon = icon;
		mItem = item;
		mArrowIcon = arrow_icon;
	}
}

class MyListAdapter extends BaseAdapter
{
	Context mainContext;
	LayoutInflater Inflater;
	ArrayList<SettingItem> arItem;
	int mLayout;
	
	public MyListAdapter(Context context, int layout, ArrayList<SettingItem> src)
	{
		mainContext = context;
		Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		arItem = src;
		mLayout = layout;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		
		return arItem.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return arItem.get(position).mItem;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final int pos = position;
		if(convertView == null)
		{
			convertView = Inflater.inflate(mLayout, parent, false);
		}
		
		ImageView img = (ImageView)convertView.findViewById(R.id.setting_image);
		img.setImageResource(arItem.get(position).mIcon);
		
		TextView txt = (TextView)convertView.findViewById(R.id.setting_menu);
		txt.setText(arItem.get(position).mItem);
		
		ImageView img_arrow = (ImageView)convertView.findViewById(R.id.setting_arrow_image);
		img_arrow.setImageResource(arItem.get(position).mArrowIcon);
		
		return convertView;
	}
	
}