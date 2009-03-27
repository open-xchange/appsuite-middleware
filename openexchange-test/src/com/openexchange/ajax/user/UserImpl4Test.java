/*
 * UserTestImpl.java
 *
 * Created on 15. Februar 2007, 15:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.openexchange.ajax.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.openexchange.groupware.ldap.User;

/**
 *
 * @author Sebastian Kauss
 */
public class UserImpl4Test implements User {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5888772849310386010L;

	private int id = 0;
	
	private String userPassword;
	
	private final String passwordMech = null;
	
	private final boolean mailEnabled = false;
	
	private final int shadowLastChange = -1;
	
	private final String imapServer = null;
	
	private final String imapLogin = null;
	
	private final String smtpServer = null;
	
	private final String mailDomain = null;
	
	private String givenName = null;
	
	private String surName = null;
	
	private String mail = null;
	
	private String displayName = null;
	
	private final String timezone = null;
	
	private final String preferedLanguage = null;
	
	private String loginInfo = null;
	
	private Locale locale;
	
	public UserImpl4Test() {
		
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(final int id) {
		this.id = id;
	}
	
	public void setMail(final String mail) {
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

	public Map<String, Set<String>> getAttributes() {
		return Collections.unmodifiableMap(new HashMap<String, Set<String>>(0));
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

	public String getLoginInfo() {
		return loginInfo;
	}
	
	public void setLoginInfo(final String loginInfo) {
		this.loginInfo = loginInfo;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

    /**
     * @param string
     */
    public void setDisplayName(String displayName) {
       this.displayName = displayName;
    }

    /**
     * @param string
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    /**
     * @param string
     */
    public void setSurname(String surname) {
        this.surName = surname;
    }
}
