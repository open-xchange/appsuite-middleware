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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.mail.MessagingException;
import javax.mail.Session;

import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.UserConfigurationException;
import com.openexchange.groupware.UserConfigurationStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.Credentials;
import com.openexchange.groupware.ldap.User;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.imap.IMAPUtils;
import com.openexchange.imap.UserSettingMail;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.Rights;

/**
 * SessionObject
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class SessionObject {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SessionObject.class);

	private final String sessionid;

	private String username;

	private String userlogin;

	private String loginName;

	private String password;

	private String language;

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

	private UserSettingMail userSettingMail;

	private IMAPProperties imapProperties;

	private Session mailSession;

	private final transient Map<String, Rights> imapCachedMyRights;

	private final transient Map<String, Boolean> imapCachedUserFlags;

	public SessionObject(final String sessionid) {
		this.sessionid = sessionid;
		imapCachedMyRights = new ConcurrentHashMap<String, Rights>();
		imapCachedUserFlags = new ConcurrentHashMap<String, Boolean>();
	}

	public final Rights getCachedRights(final IMAPFolder f, final boolean load) throws MessagingException {
		Rights r = imapCachedMyRights.get(f.getFullName());
		if (load && r == null) {
			r = f.myRights();
			imapCachedMyRights.put(f.getFullName(), r);
		}
		return r;

	}

	public final void removeCachedRights(final IMAPFolder f) {
		imapCachedMyRights.remove(f.getFullName());
	}

	public final void setCachedRights(final IMAPFolder f) throws MessagingException {
		imapCachedMyRights.put(f.getFullName(), f.myRights());
	}

	public final boolean containsCachedRights(final IMAPFolder f) {
		return imapCachedMyRights.containsKey(f.getFullName());
	}

	public final boolean getCachedUserFlags(final IMAPFolder f, final boolean load) throws MessagingException {
		Boolean b = imapCachedUserFlags.get(f.getFullName());
		if (load && b == null) {
			b = Boolean.valueOf(IMAPUtils.supportsUserDefinedFlags(f));
			imapCachedUserFlags.put(f.getFullName(), b);
		}
		return b.booleanValue();
	}

	public final void removeCachedUserFlags(final IMAPFolder f) {
		imapCachedUserFlags.remove(f.getFullName());
	}

	public final void setCachedUserFlags(final IMAPFolder f) throws MessagingException {
		imapCachedUserFlags.put(f.getFullName(), Boolean.valueOf(IMAPUtils.supportsUserDefinedFlags(f)));
	}

	public final boolean containsCachedUserFlags(final IMAPFolder f) {
		return imapCachedUserFlags.containsKey(f.getFullName());
	}

	public final void cleanIMAPCaches() {
		imapCachedMyRights.clear();
		imapCachedUserFlags.clear();
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

	public void setUserSettingMail(final UserSettingMail userSettingMail) {
		this.userSettingMail = userSettingMail;
	}

	public void setIMAPProperties(final IMAPProperties imapProperties) {
		this.imapProperties = imapProperties;
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

	/**
	 * Convenience method that just invokes {@link User#getLocale()}
	 * 
	 * @see com.openexchange.groupware.ldap.User#getLocale()
	 * 
	 * @return an instance of <code>java.util.Locale</code> or
	 *         <code>null</code> if none present
	 */
	public Locale getLocale() {
		return u == null ? null : u.getLocale();
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
		return timestamp;
	}

	public Date getCreationtime() {
		return creationtime;
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
	
	public UserSettingMail getUserSettingMail() {
		return userSettingMail;
	}

	public IMAPProperties getIMAPProperties() {
		return imapProperties;
	}

	public UserConfiguration getUserConfiguration() {
		try {
			return UserConfigurationStorage.getInstance().getUserConfiguration(u.getId(), u.getGroups(), context);
		} catch (final UserConfigurationException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return null;
		}
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

	public void closingOperations() {
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(final String secret) {
		this.secret = secret;
	}

}
