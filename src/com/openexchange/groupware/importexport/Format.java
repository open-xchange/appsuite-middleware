package com.openexchange.groupware.importexport;

/**
 * This enumeration lists formats for import or export.
 * 
 * @author Tobias Prinz, mailto:tobias.prinz@open-xchange.com
 *
 */
public enum Format {
	CSV("Comma separated values","text/csv","csv"),
	ICAL("iCal","text/calendar","ics"),
	VCARD("vCard","text/x-vcard","vcf"),
	TNEF("Transport Neutral Encapsulation Format" , "application/ms-tnef", "tnef");
	
	private String mimetype, longName, extension;
	
	private Format(final String longName, final String mimetype, final String extension){
		this.longName = longName;
		this.mimetype = mimetype;
		this.extension = extension;
	}
	
	public String getFullName(){
		return this.longName;
	}
	
	public String getMimeType(){
		return this.mimetype;
	}
	
	public String getExtension(){
		return this.extension;
	}
}
