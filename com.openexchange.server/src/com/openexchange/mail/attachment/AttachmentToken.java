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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.mail.attachment;

import static com.openexchange.java.util.UUIDs.getUnformattedString;
import static java.util.UUID.randomUUID;
import java.util.concurrent.atomic.AtomicLong;
import com.openexchange.session.Session;

/**
 * {@link AttachmentToken}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AttachmentToken implements AttachmentTokenConstants {

    private final String id;
    private final long ttlMillis;
    private final AtomicLong timeoutStamp;

    private int contextId;
    private int userId;
    private int accountId;
    private String mailId;
    private String attachmentId;
    private String folderPath;
    private String sessionId;
    private String clientIp;
    private String client;
    private String userAgent;
    private String jsessionId;
    private boolean oneTime;
    private boolean checkIp;

    /**
     * Initializes a new {@link AttachmentToken}.
     */
    public AttachmentToken(long ttlMillis) {
        super();
        if (ttlMillis <= 0) {
            throw new IllegalArgumentException("ttlMillis must be positive.");
        }
        this.id = new StringBuilder(75).append(getUnformattedString(randomUUID())).append('.').append(getUnformattedString(randomUUID())).toString();
        this.ttlMillis = ttlMillis;
        timeoutStamp = new AtomicLong(System.currentTimeMillis() + ttlMillis);
    }

    /**
     * Sets whether this token is a one-time token.
     *
     * @param oneTime <code>true</code> for one-time token; otherwise <code>false</code>
     * @return This attachment token with new behavior applied
     */
    public AttachmentToken setOneTime(boolean oneTime) {
        this.oneTime = oneTime;
        return this;
    }

    /**
     * Checks if this token is a one-time token.
     *
     * @return <code>true</code> for one-time token; otherwise <code>false</code>
     */
    public boolean isOneTime() {
        return oneTime;
    }

    /**
     * Gets the checkIp
     *
     * @return The checkIp
     */
    public boolean isCheckIp() {
        return checkIp;
    }

    /**
     * Sets the checkIp
     *
     * @param checkIp The checkIp to set
     * @return This attachment token with new behavior applied
     */
    public AttachmentToken setCheckIp(boolean checkIp) {
        this.checkIp = checkIp;
        return this;
    }

    /**
     * Gets the JSESSIONID.
     *
     * @return The JSESSIONID
     */
    public String getJSessionId() {
        return jsessionId;
    }

    /**
     * Sets the access information.
     *
     * @param accountId The account identifier
     * @param session The session
     * @return This token with access information applied
     */
    public AttachmentToken setAccessInfo(int accountId, Session session) {
        this.accountId = accountId;
        this.jsessionId = (String) session.getParameter("JSESSIONID");
        this.contextId = session.getContextId();
        this.userId = session.getUserId();
        this.sessionId = session.getSessionID();
        this.clientIp = session.getLocalIp();
        this.client = session.getClient();
        this.userAgent = (String) session.getParameter("user-agent");
        return this;
    }

    /**
     * Sets the attachment information.
     *
     * @param folderPath The folder path; e.g. <code>"default0/INBOX"</code>
     * @param mailId The mail identifier
     * @param attachmentId The attachment identifier
     * @return This token with arguments applied
     */
    public AttachmentToken setAttachmentInfo(String folderPath, String mailId, String attachmentId) {
        this.folderPath = folderPath;
        this.mailId = mailId;
        this.attachmentId = attachmentId;
        return this;
    }

    /**
     * Touches this token.
     *
     * @return This token with elapse timeout reseted
     */
    public AttachmentToken touch() {
        long cur;
        do {
            cur = timeoutStamp.get();
        } while (!timeoutStamp.compareAndSet(cur, System.currentTimeMillis() + ttlMillis));
        return this;
    }

    /**
     * Gets the token identifier.
     *
     * @return The token identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Checks if this token is expired.
     *
     * @return <code>true</code> if this token is expired; otherwise <code>false</code>
     */
    public boolean isExpired() {
        return System.currentTimeMillis() >= timeoutStamp.get();
    }

    /**
     * Gets the user agent identifier.
     *
     * @return The user agent identifier
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Gets the client IP address.
     *
     * @return The client IP address
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * Gets the client
     *
     * @return The client
     */
    public String getClient() {
        return client;
    }

    /**
     * Gets the session identifier
     *
     * @return The session identifier
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the contextId
     *
     * @return The contextId
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the userId
     *
     * @return The userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the accountId
     *
     * @return The accountId
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the folder path
     *
     * @return The folder path
     */
    public String getFolderPath() {
        return folderPath;
    }

    /**
     * Gets the mailId
     *
     * @return The mailId
     */
    public String getMailId() {
        return mailId;
    }

    /**
     * Gets the attachmentId
     *
     * @return The attachmentId
     */
    public String getAttachmentId() {
        return attachmentId;
    }

    /**
     * Sets the contextId
     *
     * @param contextId The contextId to set
     */
    public void setContextId(int contextId) {
        this.contextId = contextId;
    }

    /**
     * Sets the userId
     *
     * @param userId The userId to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Sets the accountId
     *
     * @param accountId The accountId to set
     */
    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    /**
     * Sets the mailId
     *
     * @param mailId The mailId to set
     */
    public void setMailId(String mailId) {
        this.mailId = mailId;
    }

    /**
     * Sets the attachmentId
     *
     * @param attachmentId The attachmentId to set
     */
    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    /**
     * Sets the folderPath
     *
     * @param folderPath The folderPath to set
     */
    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    /**
     * Sets the sessionId
     *
     * @param sessionId The sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Sets the clientIp
     *
     * @param clientIp The clientIp to set
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    /**
     * Sets the client
     *
     * @param client The client to set
     */
    public void setClient(String client) {
        this.client = client;
    }

    /**
     * Sets the userAgent
     *
     * @param userAgent The userAgent to set
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Sets the jsessionId
     *
     * @param jsessionId The jsessionId to set
     */
    public void setJsessionId(String jsessionId) {
        this.jsessionId = jsessionId;
    }

}
