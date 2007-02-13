/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */



package com.openexchange.sessiond;

import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.mail.Session;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.imap.IMAPProperties;
import com.openexchange.groupware.ldap.Credentials;
import com.openexchange.groupware.ldap.User;

/**
 * SessionObject
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class SessionObject {
	
	private final String sessionid;
	private String username;
	private String userlogin;
	private String loginName;
	private String password;
	private String language;
	private Locale locale;
	private String localip;
	private String host;
	
	private long lifetime;
	private Date timestamp;
	private Date creationtime;
	
	private String secret;
	
	private String randomToken;
	
	private Context context;
	
	private Credentials cred;
	
	private Map hm;
	
	private User u;
	
	private IMAPProperties imapProperties;
	
	private UserConfiguration userConfig;
	
	private final Set<String> replyMsgSet;
	
	private final Set<String> forwardMsgSet;
	
	private Session mailSession;
	
	public SessionObject(final String sessionid) {
		this.sessionid = sessionid;
		this.replyMsgSet = new HashSet<String>();
		this.forwardMsgSet = new HashSet<String>();
	}
	
	public void setUsername(final String username) {
		this.username = username;
	}
	
	public void setUserlogin(final String userlogin) {
		this.userlogin = userlogin;
	}

	public void setPassword(final String password) {
		this.password = password;
	}
	
	public void setLanguage(final String language) {
		this.language = language;
		this.locale = createLocale(language);
	}
	
	public void setLocalIp(final String localip) {
		this.localip = localip;
	}
	
	public void setHost(final String host) {
		this.host = host;
	}
	
	public void setLifetime(final long lifetime) {
		this.lifetime = lifetime;
	}
	
	public void setTimestamp(final Date timestamp) {
		this.timestamp = (Date) timestamp.clone();
	}
	
	public void setCreationtime(final Date creationtime) {
		this.creationtime = (Date) creationtime.clone();
	}
	
	public void setContext(final Context context) {
		this.context = context;
	}
	
	public void setDynamicMap(final Map hm) {
		this.hm = hm;
	}
	
	public void setUserObject(final User u) {
		this.u = u;
	}
	
	public void setIMAPProperties(final IMAPProperties imapProperties) {
		this.imapProperties = imapProperties;
	}
	
	public void setUserConfiguration(final UserConfiguration userConfig) {
		this.userConfig = userConfig;
	}
	
	public String getSessionID() {
		return sessionid;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getUserlogin() {
		return userlogin;
	}

	public String getPassword() {
		return password;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	public String getLocalIp() {
		return localip;
	}
	
	public String getHost() {
		return host;
	}
	
	public long getLifetime() {
		return lifetime;
	}
	
	public Date getTimestamp() {
		return ((Date) timestamp);
	}
	
	public Date getCreationtime() {
		return ((Date) creationtime);
	}
	
	public Context getContext() {
		return context;
	}
	
	public void setCredentials(final Credentials cred) {
		this.cred = cred;
	}
	
	public Credentials getCredentials() {
		return cred;
	}
	
	public Map getDynamicMap() {
		return hm;
	}
	
	public User getUserObject() {
		return u;
	}
	
	public IMAPProperties getIMAPProperties() {
		return imapProperties;
	}
	
	public UserConfiguration getUserConfiguration() {
		return this.userConfig;
	}

	public void setLoginName(final String loginName) {
		this.loginName = loginName;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setRandomToken(final String randomToken) {
		this.randomToken = randomToken;
	}

	public String getRandomToken() {
		return randomToken;
	}
	
	public Session getMailSession() {
		return mailSession;
	}

	public void setMailSession(final Session mailSession) {
		this.mailSession = mailSession;
	}

	public void closingOperations() throws OXException {
		if (userConfig != null) {
			if (userConfig.getUserSettingMail() != null) {
				userConfig.getUserSettingMail().saveUserSettingMail(userConfig.getUserId(), userConfig.getContext());
			}
			UserConfiguration.saveUserConfiguration(userConfig);
		}
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(final String secret) {
		this.secret = secret;
	}
	
	/**
	 * Adds a unique identifier of a message on which a reply is going to be
	 * expected
	 */
	public void addExpectedReplyMsg(final String msgUID) {
		this.replyMsgSet.add(msgUID);
	}

	/**
	 * Removes a unique identifier of a message on which a reply is going to be
	 * expected
	 */
	public boolean removeExpectedReplyMsg(final String msgUID) {
		if (msgUID != null) {
			return this.replyMsgSet.remove(msgUID);
		}
		return false;
	}

	/**
	 * Checks if given message's unique identifier is in set of expected reply
	 * messages
	 */
	public boolean isExpectingReplyMsg(final String msgUID) {
		return msgUID == null ? false : this.replyMsgSet.contains(msgUID);
	}

	/**
	 * Clears the set of message unique identifiers
	 * 
	 */
	public void clearAllExpectedReplyMsgs() {
		this.replyMsgSet.clear();
	}
	
	/**
	 * Adds a unique identifier of a message on which a forward is going to be
	 * expected
	 */
	public void addExpectedForwardMsg(final String msgUID) {
		this.forwardMsgSet.add(msgUID);
	}

	/**
	 * Removes a unique identifier of a message on which a forward is going to be
	 * expected
	 */
	public boolean removeExpectedForwardMsg(final String msgUID) {
		if (msgUID != null) {
			return this.forwardMsgSet.remove(msgUID);
		}
		return false;
	}

	/**
	 * Checks if given message's unique identifier is in set of expected forward
	 * messages
	 */
	public boolean isExpectingForwardMsg(final String msgUID) {
		return msgUID == null ? false : this.forwardMsgSet.contains(msgUID);
	}

	/**
	 * Clears the set of message unique identifiers
	 * 
	 */
	public void clearAllExpectedForwardyMsgs() {
		this.forwardMsgSet.clear();
	}
	
	public static Locale createLocale(final String localeStr) {
		final String[] sa = localeStr.split("_");
		switch (sa.length) {
		case 1:
			return new Locale(sa[0]);
		case 2:
			return new Locale(sa[0],sa[1]);
		case 3:
			return new Locale(sa[0],sa[1],sa[2]);
		default:
			return null;
		}
	}
}





