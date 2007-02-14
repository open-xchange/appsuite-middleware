package com.openexchange.groupware.importexport;

import com.openexchange.api2.OXException;

public class ImportResult {
	
	private String objectId;
	private String folder;
	private OXException exception;
	private long timestamp;


	/**
	 * Basic constructor
	 *
	 */
	public ImportResult(){
	}
	
	/**
	 * Constructor for correct result
	 * @param objectId
	 * @param type
	 * @param timestamp
	 */
	public ImportResult(String objectId, String folder, long timestamp){
		this(objectId, folder, timestamp, null);
	}
	
	/**
	 * Constructor for botched result
	 * @param objectId
	 * @param type
	 * @param timestamp
	 * @param exception
	 */
	public ImportResult(String objectId, String folder, long timestamp, OXException exception){
		this.objectId = objectId;
		this.folder = folder;
		this.timestamp = timestamp;
		this.exception = exception;
	}
	
	
	
	public boolean isCorrect(){
		return exception == null;
	}
	public boolean hasError(){
		return exception != null;
	}
	public OXException getException() {
		return exception;
	}
	public void setException(OXException error) {
		this.exception = error;
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getFolder() {
		return folder;
	}
	public void setFolder(String folder) {
		this.folder = folder;
	}
	
	
	
}
