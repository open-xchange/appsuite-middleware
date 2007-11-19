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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.upload.ManagedUploadFile;
import com.openexchange.session.Session;

/**
 * SessionImpl
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class SessionImpl implements Session {
	
	protected String loginName;
	
	protected String password;
	
    protected Context context;
    
    protected int userId;
    
    protected String sessionId;
    
    protected String secret;
    
    protected String randomToken;
    
    protected String localIp;
    
	private static final Log LOG = LogFactory.getLog(SessionImpl.class);
	
	private final transient Map<String, ManagedUploadFile> ajaxUploadFiles;

	private final Map<String, Object> parameters;

	public SessionImpl(int userId, String loginName, String password, Context context, String sessionId, String secret, String randomToken, String localIp) {
        this.userId = userId;
		this.loginName = loginName;
		this.password = password;
        this.context = context;
        this.sessionId = sessionId;
        this.secret = secret;
        this.randomToken = randomToken;
        this.localIp = localIp;
        
		parameters = new ConcurrentHashMap<String, Object>();
		ajaxUploadFiles = new ConcurrentHashMap<String, ManagedUploadFile>();
	}

    public Context getContext() {
        return context;
    }

    public Object getParameter(String name) {
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

    public ManagedUploadFile getUploadedFile(String id) {
		final ManagedUploadFile uploadFile = ajaxUploadFiles.get(id);
		if (null != uploadFile) {
			uploadFile.touch();
		}
		return uploadFile;
    }

    public int getUserID() {
        return userId;
    }

    public void putUploadedFile(String id, ManagedUploadFile uploadFile) {
		ajaxUploadFiles.put(id, uploadFile);
		uploadFile.startTimerTask(id, ajaxUploadFiles);
		if (LOG.isInfoEnabled()) {
			LOG.info(new StringBuilder(256).append("Upload file \"").append(uploadFile).append("\" with ID=")
					.append(id).append(" added to session and timer task started").toString());
		}
    }

    public ManagedUploadFile removeUploadedFile(String id) {
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

    public void removeUploadedFileOnly(String id) {
    }

    public void setParameter(String name, Object value) {
        parameters.put(name, value);
    }

    public boolean touchUploadedFile(String id) {
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

	public String getLocalIp() {
		return localIp;
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
	
	public String getPassword() {
		return password;
	}
}