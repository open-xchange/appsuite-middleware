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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.io.Closeable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.session.Session;

/**
 * {@link AttachmentToken}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AttachmentToken implements AttachmentTokenConstants, Closeable {

    private final String id;
    private final long ttlMillis;
    private final AtomicLong timeoutStamp;

    private int contextId;
    private int userId;
    private int accountId;
    private String mailId;
    private String attachmentId;
    private String fullName;
    private MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess;
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
    public AttachmentToken(final long ttlMillis) {
        super();
        if (ttlMillis <= 0) {
            throw new IllegalArgumentException("ttlMillis must be positive.");
        }
        this.id =
            new com.openexchange.java.StringAllocator(75).append(UUIDs.getUnformattedString(UUID.randomUUID())).append('.').append(
                UUIDs.getUnformattedString(UUID.randomUUID())).toString();
        this.ttlMillis = ttlMillis;
        timeoutStamp = new AtomicLong(System.currentTimeMillis() + ttlMillis);
    }

    /**
     * Sets whether this token is a one-time token.
     *
     * @param oneTime <code>true</code> for one-time token; otherwise <code>false</code>
     * @return This attachment token with new behavior applied
     */
    public AttachmentToken setOneTime(final boolean oneTime) {
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
    public AttachmentToken setCheckIp(final boolean checkIp) {
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
    public AttachmentToken setAccessInfo(final int accountId, final Session session) {
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
     * @param mailId The mail identifier
     * @param attachmentId The attachment identifier
     * @return This token with access attachment applied
     */
    public AttachmentToken setAttachmentInfo(final String fullName, final String mailId, final String attachmentId) {
        this.fullName = fullName;
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
     * Gets the associated attachment.
     * <p>
     * <b>Note</b>: After calling this method {@link #close()} needs to be called!
     *
     * @return The associated attachment
     * @throws MailException
     * @see {@link #close()}
     */
    public MailPart getAttachment() throws OXException {
        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = MailAccess.getInstance(userId, contextId, accountId);
        this.mailAccess = mailAccess;
        mailAccess.connect();
        return mailAccess.getMessageStorage().getAttachment(
            MailFolderUtility.prepareMailFolderParam(fullName).getFullname(),
            mailId,
            attachmentId);
    }

    /**
     * Closes associated mail access (if opened)
     */
    @Override
    public void close() {
        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = this.mailAccess;
        if (null != mailAccess) {
            mailAccess.close(true);
            this.mailAccess = null;
        }
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

}
