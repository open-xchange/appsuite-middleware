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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.caching.objects.CachedSession;
import com.openexchange.groupware.upload.ManagedUploadFile;
import com.openexchange.session.Session;

/**
 * {@link SessionImpl} - Implements interface {@link Session}
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionImpl implements Session {

	private static final transient Log LOG = LogFactory.getLog(SessionImpl.class);

	private final String loginName;

	private String password;

	private final int contextId;

	private final int userId;

	private final String sessionId;

	private final String secret;

	private final String login;

	private String randomToken;

	private String localIp;

	private final Map<String, ManagedUploadFile> managedUploadFiles;

	private final Map<String, Object> parameters;

	/**
	 * Initializes a new {@link SessionImpl}
	 * 
	 * @param userId
	 *            The user ID
	 * @param loginName
	 *            The login name
	 * @param password
	 *            The password
	 * @param contextId
	 *            The context ID
	 * @param sessionId
	 *            The session ID
	 * @param secret
	 *            The secret (cookie identifier)
	 * @param randomToken
	 *            The random token
	 * @param localIp
	 *            The local IP
	 */
	public SessionImpl(final int userId, final String loginName, final String password, final int contextId,
			final String sessionId, final String secret, final String randomToken, final String localIp,
			final String login) {
		this.userId = userId;
		this.loginName = loginName;
		this.password = password;
		this.sessionId = sessionId;
		this.secret = secret;
		this.randomToken = randomToken;
		this.localIp = localIp;
		this.contextId = contextId;
		this.login = login;
		parameters = new ConcurrentHashMap<String, Object>();
		managedUploadFiles = new ConcurrentHashMap<String, ManagedUploadFile>();
	}

	/**
	 * Initializes a new {@link SessionImpl} from specified cached session.
	 * 
	 * @param cachedSession
	 *            The cached session
	 * @param localIP
	 *            The host's local IP
	 */
	public SessionImpl(final CachedSession cachedSession, final String localIP) {
		super();
		this.userId = cachedSession.getUserId();
		this.contextId = cachedSession.getContextId();
		this.loginName = cachedSession.getLoginName();
		this.password = cachedSession.getPassword();
		this.sessionId = cachedSession.getSessionId();
		this.secret = cachedSession.getSecret();
		this.randomToken = cachedSession.getRandomToken();
		this.login = cachedSession.getLogin();
		this.localIp = localIP;
		final Map<String, Serializable> params = cachedSession.getParameters();
		parameters = new ConcurrentHashMap<String, Object>(params.size());
		for (final Iterator<Map.Entry<String, Serializable>> iter = params.entrySet().iterator(); iter.hasNext();) {
			final Map.Entry<String, Serializable> entry = iter.next();
			parameters.put(entry.getKey(), entry.getValue());
		}
		managedUploadFiles = new ConcurrentHashMap<String, ManagedUploadFile>();
	}

	/**
	 * Creates a new instance of {@link CachedSession} holding this session's
	 * state and informations ready for being put into session cache.
	 * 
	 * @return An appropriate instance of {@link CachedSession}
	 */
	public CachedSession createCachedSession() {
		return new CachedSession(userId, loginName, password, contextId, sessionId, secret, randomToken, localIp,
				login, parameters);
	}

	public int getContextId() {
		return contextId;
	}

	public Object getParameter(final String name) {
		return parameters.get(name);
	}

	public String getRandomToken() {
		return randomToken;
	}

	public String getSecret() {
		return secret;
	}

	public String getSessionID() {
		return sessionId;
	}

	public ManagedUploadFile getUploadedFile(final String id) {
		final ManagedUploadFile uploadFile = managedUploadFiles.get(id);
		if (null != uploadFile) {
			uploadFile.touch();
		}
		return uploadFile;
	}

	public int getUserID() {
		return userId;
	}

	public void putUploadedFile(final String id, final ManagedUploadFile uploadFile) {
		managedUploadFiles.put(id, uploadFile);
		uploadFile.startTimerTask(id, managedUploadFiles);
		if (LOG.isInfoEnabled()) {
			LOG.info(new StringBuilder(256).append("Upload file \"").append(uploadFile).append("\" with ID=")
					.append(id).append(" added to session and timer task started").toString());
		}
	}

	public ManagedUploadFile removeUploadedFile(final String id) {
		final ManagedUploadFile uploadFile = managedUploadFiles.remove(id);
		if (null != uploadFile) {
			/*
			 * Cancel timer task
			 */
			uploadFile.cancelTimerTask();
			if (!uploadFile.getFile().delete()) {
				LOG.warn(new StringBuilder(256).append("Temporary uploaded file \"").append(
						uploadFile.getFile().getName()).append("\" could not be deleted"));
			}
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(256).append("Upload file \"").append(uploadFile).append("\" with ID=")
						.append(id).append(" removed from session and timer task canceled").toString());
			}
		}
		return uploadFile;
	}

	public void removeUploadedFileOnly(final String id) {
	}

	public void setParameter(final String name, final Object value) {
		parameters.put(name, value);
	}

	public boolean touchUploadedFile(final String id) {
		final ManagedUploadFile uploadFile = managedUploadFiles.get(id);
		if (null != uploadFile) {
			uploadFile.touch();
			return true;
		}
		return false;
	}

	public void removeRandomToken() {
		randomToken = null;
	}

	public String getLocalIp() {
		return localIp;
	}

	/**
	 * Sets the local IP
	 * 
	 * @param localIp
	 *            The local IP to set
	 */
	void setLocalIp(final String localIp) {
		this.localIp = localIp;
	}

	public String getLoginName() {
		return loginName;
	}

	public int getUserId() {
		return userId;
	}

	public String getUserlogin() {
		return loginName;
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password
	 * 
	 * @param password
	 *            The password to set
	 */
	public void setPassword(final String password) {
		this.password = password;
	}
}