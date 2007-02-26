/*
 * UserTestImpl.java
 *
 * Created on 15. Februar 2007, 15:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.openexchange.ajax.user;

import com.openexchange.groupware.ldap.User;

/**
 *
 * @author Sebastian Kauss
 */
public class UserImpl4Test implements User {
	
	private int id = 0;
	
	private String userPassword;
	
	private String passwordMech = null;
	
	private boolean mailEnabled = false;
	
	private int shadowLastChange = -1;
	
	private String imapServer = null;
	
	private String imapLogin = null;
	
	private String smtpServer = null;
	
	private String mailDomain = null;
	
	private String givenName = null;
	
	private String surName = null;
	
	private String mail = null;
	
	private String displayName = null;
	
	private String timezone = null;
	
	private String preferedLanguage = null;
	
	public UserImpl4Test() {
		
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setMail(String mail1) {
		this.mail = mail;
	}
	
	public String getUserPassword() {
		return userPassword;
	}
	
	public String getPasswordMech() {
		return passwordMech;
	}
	
	public boolean isMailEnabled() {
		return mailEnabled;
	}
	
	public int getShadowLastChange() {
		return shadowLastChange;
	}
	
	public String getImapServer() {
		return imapServer;
	}
	
	public String getImapLogin() {
		return imapLogin;
	}
	
	public String getSmtpServer() {
		return smtpServer;
	}
	
	public String getMailDomain() {
		return mailDomain;
	}
	
	public String getGivenName() {
		return givenName;
	}
	
	public String getSurname() {
		return surName;
	}
	
	public String getMail() {
		return mail;
	}
	
	public String[] getAliases() {
		return null;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getTimeZone() {
		return timezone;
	}
	
	public String getPreferredLanguage() {
		return preferedLanguage;
	}
	
	public int[] getGroups() {
		return null;
	}
	
	public int getContactId() {
		return -1;
	}
}
