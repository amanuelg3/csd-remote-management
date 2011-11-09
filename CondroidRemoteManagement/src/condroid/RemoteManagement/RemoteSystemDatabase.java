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
	
	public static final class OpticalAMP{
		private OpticalAMP() {}
		
		// DEVICE_TABLE_NAME: table_device_name
		// +----+---------+-----------+
		// | id | command |   value   | 
		// +----+---------+-----------+
		// | INT|   TEXT  |  INTEGER  |
		// +----+---------+-----------+
		// Value: 	Optical Amplifier = 0001 (0x1)
		//			Wifi Repeater 	  = 0010 (0x2)
		//			Water sensor 	  = 0100 (0x4)
		//			etc				  = 1000 (0x8)

		public static final String TABLE_NAME = "table_optical_amp";
		public static final String ID = "_id";
		public static final String COMMAND = "command";
		
		public static final String DEFAULT_SORT_ORDER = "command ASC";
		
	

	}
	

}
