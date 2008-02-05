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

package com.openexchange.sessiond.impl;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.mail.Session;

import com.openexchange.groupware.ldap.Credentials;
import com.openexchange.groupware.upload.ManagedUploadFile;

/**
 * SessionObject
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SessionObject implements com.openexchange.session.Session {

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

	private int contextId;

	private Credentials cred;

	private Session mailSession;

	private final transient Map<String, ManagedUploadFile> ajaxUploadFiles;

	private final Map<String, Object> parameters;

	public SessionObject(final String sessionid) {
		this.sessionid = sessionid;
		parameters = new ConcurrentHashMap<String, Object>();
		ajaxUploadFiles = new ConcurrentHashMap<String, ManagedUploadFile>();
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

	public void setContextId(final int contextId) {
		this.contextId = contextId;
	}

	public String getSessionID() {
		return sessionid;
	}

	public int getUserId() {
		return Integer.parseInt(username);
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

	public int getContextId() {
		return contextId;
	}

	public void setCredentials(final Credentials cred) {
		this.cred = cred;
	}

	public Credentials getCredentials() {
		return cred;
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

	public Object getParameter(final String name) {
		return parameters.get(name);
	}

	public ManagedUploadFile getUploadedFile(final String id) {
		final ManagedUploadFile uploadFile = ajaxUploadFiles.get(id);
		if (null != uploadFile) {
			uploadFile.touch();
		}
		return uploadFile;
	}

	public void putUploadedFile(final String id, final ManagedUploadFile uploadFile) {
		ajaxUploadFiles.put(id, uploadFile);
		uploadFile.startTimerTask(id, ajaxUploadFiles);
		if (LOG.isInfoEnabled()) {
			LOG.info(new StringBuilder(256).append("Upload file \"").append(uploadFile).append("\" with ID=")
					.append(id).append(" added to session and timer task started").toString());
		}
	}

	public ManagedUploadFile removeUploadedFile(final String id) {
		final ManagedUploadFile uploadFile = ajaxUploadFiles.remove(id);
		if (null != uploadFile) {
			/*
			 * Cancel timer task
			 */
			uploadFile.cancelTimerTask();
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(256).append("Upload file \"").append(uploadFile).append("\" with ID=")
						.append(id).append(" removed from session and timer task canceled").toString());
			}
		}
		return uploadFile;
	}

	public void removeUploadedFileOnly(final String id) {
		// TODO Auto-generated method stub
	}

	public void setParameter(final String name, final Object value) {
		parameters.put(name, value);
	}

	public boolean touchUploadedFile(final String id) {
		final ManagedUploadFile uploadFile = ajaxUploadFiles.get(id);
		if (null != uploadFile) {
			uploadFile.touch();
			return true;
		}
		return false;
	}

	public void removeRandomToken() {
		randomToken = null;
	}
}
