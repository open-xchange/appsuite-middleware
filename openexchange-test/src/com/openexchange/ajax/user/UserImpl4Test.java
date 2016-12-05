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
import java.util.Locale;
import java.util.Map;
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

    private int[] groups;

    private int guestCreatedBy = 0;

	public UserImpl4Test() {

	}

	@Override
    public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void setMail(final String mail) {
		this.mail = mail;
	}

	@Override
    public String getUserPassword() {
		return userPassword;
	}

	@Override
    public String getPasswordMech() {
		return passwordMech;
	}

	@Override
    public boolean isMailEnabled() {
		return mailEnabled;
	}

	@Override
    public int getShadowLastChange() {
		return shadowLastChange;
	}

	@Override
    public String getImapServer() {
		return imapServer;
	}

	@Override
    public String getImapLogin() {
		return imapLogin;
	}

	@Override
    public String getSmtpServer() {
		return smtpServer;
	}

	@Override
    public String getMailDomain() {
		return mailDomain;
	}

	@Override
    public String getGivenName() {
		return givenName;
	}

	@Override
    public String getSurname() {
		return surName;
	}

	@Override
    public String getMail() {
		return mail;
	}

	@Override
    public String[] getAliases() {
		return null;
	}

	@Override
    public Map<String, String> getAttributes() {
		return Collections.emptyMap();
	}

	@Override
    public String getDisplayName() {
		return displayName;
	}

	@Override
    public String getTimeZone() {
		return timezone;
	}

	@Override
    public String getPreferredLanguage() {
		return preferedLanguage;
	}

	@Override
    public int[] getGroups() {
		return groups;
	}


    public void setGroups(int[] groups) {
        this.groups = groups;
    }

	@Override
    public int getContactId() {
		return -1;
	}

	@Override
    public String getLoginInfo() {
		return loginInfo;
	}

	public void setLoginInfo(final String loginInfo) {
		this.loginInfo = loginInfo;
	}

	@Override
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

    @Override
    public int getCreatedBy() {
        return guestCreatedBy;
    }

    public void setGuestCreatedBy(int guestCreatedBy) {
        this.guestCreatedBy = guestCreatedBy;
    }

    @Override
    public boolean isGuest() {
        return guestCreatedBy > 0;
    }

    @Override
    public String[] getFileStorageAuth() {
        return new String[2];
    }

    @Override
    public long getFileStorageQuota() {
        return 0;
    }

    @Override
    public int getFilestoreId() {
        return -1;
    }

    @Override
    public String getFilestoreName() {
        return null;
    }

    @Override
    public int getFileStorageOwner() {
        return -1;
    }
}
