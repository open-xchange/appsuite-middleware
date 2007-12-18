package com.openexchange.groupware.ldap;

import java.util.Locale;

public class MockUser implements User {
	private static final int GROUP_ALL = 0;

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 2265710814522924009L;

    /**
     * Fore name.
     */
    private String givenName;

    /**
     * Sure name.
     */
    private String surname;

    /**
     * Unique identifier. This identifier must be only unique in a context.
     */
    private int id;

    /**
     * Unique identifier of the contact belonging to this user.
     */
    private int contactId;

    /**
     * E-Mail address.
     */
    private String mail;

    /**
     * E-Mail domain.
     */
    private String mailDomain;

    /**
     * IMAP server.
     */
    private String imapServer;

    /**
     * Login for the IMAP server.
     */
    private String imapLogin;

    /**
     * SMTP server.
     */
    private String smtpServer;

    /**
     * Timezone for this user.
     */
    private String timeZone;

    /**
     * Portal shows appointments for this number of days.
     */
    private int appointmentDays;

    /**
     * Portal shows tasks for this number of days.
     */
    private int taskDays;

    /**
     * The preferred language of this user. According to RFC 2798 and 2068 it
     * should be something like de-de, en-gb or en.
     */
    private String preferredLanguage;

    /**
     * Display name of the user.
     */
    private String displayName;

    /**
     * The hashed and base64 encoded password. The default value is
     * <code>"x"</code> to cause matches fail.
     */
    private String userPassword = "x";

    /**
     * Determines if the user is enabled or disabled.
     */
    private boolean mailEnabled = false;

    /**
     * Days since Jan 1, 1970 that password was last changed.
     */
    private int shadowLastChange = -1;

    /**
     * Groups this user is member of.
     */
    private int[] groups;


    /**
     * Password encryption mechanism
     */
    
    private String passwordMech;

	private String loginInfo;
    
	
	private Locale locale;
	
    /**
     * Getter for userPassword.
     * @return Password.
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * Setter for id.
     * @param id User identifier.
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Getter for uid.
     * @return User identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Setter for userPassword.
     * @param userPassword Password.
     */
    public void setUserPassword(final String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * Getter for mailEnabled.
     * @return <code>true</code> if user is enabled.
     */
    public boolean isMailEnabled() {
        return mailEnabled;
    }

    /**
     * Setter for mailEnabled.
     * @param mailEnabled <code>true</code> to enable user.
     */
    public void setMailEnabled(final boolean mailEnabled) {
        this.mailEnabled = mailEnabled;
    }

    /**
     * Getter for shadowLastChange.
     * @return Days since Jan 1, 1970 that password was last changed.
     */
    public int getShadowLastChange() {
        return shadowLastChange;
    }

    /**
     * Setter for shadowLastChange.
     * @param shadowLastChange Days since Jan 1, 1970 that password was last
     * changed.
     */
    public void setShadowLastChange(final int shadowLastChange) {
        this.shadowLastChange = shadowLastChange;
    }

    /**
     * Setter for imapServer.
     * @param imapServer IMAP server.
     */
    public void setImapServer(final String imapServer) {
        this.imapServer = imapServer;
    }

    /**
     * Getter for imapServer.
     * @return IMAP server.
     */
    public String getImapServer() {
        return imapServer;
    }

    /**
     * Setter for smtpServer.
     * @param smtpServer SMTP server.
     */
    public void setSmtpServer(final String smtpServer) {
        this.smtpServer = smtpServer;
    }

    /**
     * Getter for smtpServer.
     * @return SMTP server.
     */
    public String getSmtpServer() {
        return smtpServer;
    }

    /**
     * Setter for mailDomain.
     * @param mailDomain mail domain.
     */
    public void setMailDomain(final String mailDomain) {
        this.mailDomain = mailDomain;
    }

    /**
     * Getter for mailDomain.
     * @return mail domain.
     */
    public String getMailDomain() {
        return mailDomain;
    }

    /**
     * Setter for givenName.
     * @param givenName given name.
     */
    public void setGivenName(final String givenName) {
        this.givenName = givenName;
    }

    /**
     * Getter for givenName.
     * @return given name.
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Setter for sure name.
     * @param sureName sure name.
     */
    public void setSurname(final String sureName) {
        this.surname = sureName;
    }

    /**
     * Getter for sure name.
     * @return sure name.
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Setter for mail.
     * @param mail Mail address.
     */
    public void setMail(final String mail) {
        this.mail = mail;
    }

    /**
     * Getter for mail.
     * @return mail address.
     */
    public String getMail() {
        return mail;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getAliases() {
        return new String[0];
    }
    
    /**
     * Setter for displayName.
     * @param displayName Display name.
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Getter for displayName.
     * @return Display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Setter for timeZone.
     * @param timeZone Timezone.
     */
    public void setTimeZone(final String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Getter for timeZone.
     * @return Timezone.
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Setter for appointmentDays.
     * @param appointmentDays Portal show appointments for this number of days.
     */
    public void setAppointmentDays(final int appointmentDays) {
        this.appointmentDays = appointmentDays;
    }

    /**
     * Getter for appointmentDays.
     * @return Portal show appointments for this number of days.
     */
    public int getAppointmentDays() {
        return appointmentDays;
    }

    /**
     * Setter for taskDays.
     * @param taskDays Portal show tasks for this number of days.
     */
    public void setTaskDays(final int taskDays) {
        this.taskDays = taskDays;
    }

    /**
     * Getter for taskDays.
     * @return Portal show tasks for this number of days.
     */
    public int getTaskDays() {
        return taskDays;
    }

    /**
     * Setter for preferredLanguage.
     * @param preferredLanguage Preferred language.
     */
    public void setPreferredLanguage(final String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    /**
     * Getter for preferredLanguage. The preferred language of the user.
     * According to RFC 2798 and 2068 it should be something like de-de, en-gb
     * or en.
     * @return Preferred Language.
     */
    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    /**
     * Getter for groups.
     * @return the groups this user is member of.
     */
    public int[] getGroups() {
        return (int[]) groups.clone();
    }

    /**
     * Setter for groups.
     * @param groups the groups this user is member of.
     */
    public void setGroups(final int[] groups) {
        this.groups = addAllGroupsAndUsersGroup(groups);
    }

    /**
     * Adds the group identifier for all groups and users.
     * @param groups groups of the user.
     * @return groups of the user and 0 will be added if it is missing.
     */
    private static int[] addAllGroupsAndUsersGroup(final int[] groups) {
        boolean contained = false;
        for (int group : groups) {
            contained = group == GROUP_ALL;
        }
        if (contained) {
            return groups;
        } else {
            final int[] newgroups = new int[groups.length + 1];
            newgroups[0] = GROUP_ALL;
            System.arraycopy(groups, 0, newgroups, 1, groups.length);
            return newgroups;
        }
    }

    /**
     * @return the contactId
     */
    public int getContactId() {
        return contactId;
    }

    /**
     * @param contactId the contactId to set
     */
    public void setContactId(final int contactId) {
        this.contactId = contactId;
    }

    /**
     * @return the imapLogin
     */
    public String getImapLogin() {
        return imapLogin;
    }

	public String getPasswordMech() {
		return passwordMech;
	}

	public void setPasswordMech(String passwordMech) {
		this.passwordMech = passwordMech;
	}
	
	public void setLoginInfo(String loginInfo) {
		this.loginInfo = loginInfo;
	}

	public String getLoginInfo() {
		return loginInfo;
	}

	public Locale getLocale() {
        if(locale == null && preferredLanguage != null) {
            String[] lang = preferredLanguage.split("_");
            if(lang.length == 2) {
                locale = new Locale(lang[0], lang[1]);
            } else {
                locale = new Locale(lang[0]);
            }
        }
        return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}
}
