package condroid.RemoteManagement;



public final class RemoteSystemDatabase {
	private RemoteSystemDatabase() {}
	
	public static final class RemoteSiteDB {
		
		
		
		// REMOTESITE_TABLE_NAME: table_remote_site
		// +----+-----------+-----------+----------+
		// | id | Location  | Phone num | device id| 
		// +----+-----------+-----------+----------+
		// | INT|  TEXT     |  INTEGER  |  INTEGER |
		// +----+-----------+-----------+----------+
		
        public static final String TABLE_NAME = "table_remote_site";
        
        public static final String ID = "_id"; 
        public static final String LOCATION = "location";
        public static final String PHONE_NUMBER = "phone";
        public static final String DEVICE_ID = "device_id";      
        
        public static final String DEFAULT_SORT_ORDER = "location ASC";
        
        public String mLocation;
        public String mPhoneNumber;
        public String mDeviceId;
        
        public RemoteSiteDB() 
        {
        	mLocation = "";
        	mPhoneNumber = "";
        	mDeviceId = "";
        }
        
        public void setRemoteSiteItem(String location, String phonenumber, String deviceid)
        {
        	mLocation = location;
        	mPhoneNumber = phonenumber;
        	mDeviceId = deviceid;
        }
        
        public String[] getRemoteSiteItem()
        {
        	String[] item = {mLocation, mPhoneNumber, mDeviceId};
        	
        	return item;
        }
	}
	
	public static final class DeviceCommandDB
	{
		
		// DEVICE_TABLE_NAME: table_device_name
		// +----+-------+-----------+
		// | id | type  |  command  | 
		// +----+-------+-----------+
		// | INT| TEXT  |   TEXT    |
		// +----+-------+-----------+

		public static final String TABLE_NAME = "table_device_cmd";
		public static final String ID = "_id";
		public static final String TYPE = "type";
		public static final String COMMAND = "command";
		
		public static final String DEFAULT_SORT_ORDER = "command ASC";
		
        public String mDeviceType;
        public String mCommand;

		public DeviceCommandDB() 
		{
			mDeviceType = "";
			mCommand = "";
		}
		
        public void setDeviceCommandItem(String type, String command)
        {
        	mDeviceType = type;
        	mCommand = command;
        }
        
        public String[] getDeviceCommandItem()
        {
        	String[] item = {mDeviceType, mCommand};
        	
        	return item;
        }
	}
	
	public static final class CommandHistoryDB
	{
		// DEVICE_TABLE_NAME: table_device_name
		// +----+-------+-------+-------+----------+
		// | id | date  | time  | phone | command  | 
		// +----+-------+-------+-------+----------+
		// | INT| TEXT  | TEXT  | TEXT  |  TEXT    |
		// +----+-------+-------+-------+----------+

		public static final String TABLE_NAME = "table_command_history";
		public static final String ID = "_id";
		public static final String DATE = "date";
		public static final String TIME = "time";
		public static final String PHONE = "phone";
		public static final String COMMAND = "command";
		
		public static final String DEFAULT_SORT_ORDER = "date ASC";
		
		public String mCommandDate;
		public String mCommandTime;
		public String mCommandPhoneNumber;
		public String mCommandName;
		
		public CommandHistoryDB()
		{
			mCommandDate = "";
			mCommandTime = "";
			mCommandPhoneNumber = "";
			mCommandName = "";
		}
		
        public void setCommandHistoryItem(String date, String time, String phone, String command)
        {
        	mCommandDate = date;
        	mCommandTime = time;
        	mCommandPhoneNumber = phone;
        	mCommandName = command;
        }
        
        public String[] getCommandHistroyItem()
        {
        	String[] item = {mCommandDate, mCommandTime, mCommandPhoneNumber, mCommandName};
        	
        	return item;
        }

	}

}
