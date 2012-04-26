package com.openexchange.logging.tracking;


public interface TrackingConfigurationMBean {

	public void setLogLevel(String className, String sessionId,
			String logLevel);

	public void setLogLevel(String className, int cid, int uid,
			String logLevel);

	public void setLogLevel(String className, int cid, String logLevel);

	public boolean setLogLevel(String className, int cid,
			String userName, String logLevel);
	
	public void clearTracking(int cid, int uid);
	public void clearTracking(String sessionId);
	public void clearTracking(int cid);
	public boolean clearTracking(int cid, String userName);

}