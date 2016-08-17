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

package com.openexchange.mail.attachment.impl.portable;

import java.io.IOException;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.mail.attachment.AttachmentToken;

/**
 * {@link PortableAttachmentToken}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class PortableAttachmentToken extends AbstractCustomPortable {

    public static final String PARAMETER_ID = "id";
    public static final String PARAMETER_CONTEXT_ID = "contextId";
    public static final String PARAMETER_USER_ID = "userId";
    public static final String PARAMETER_ACCOUNT_ID = "accountId";
    public static final String PARAMETER_MAIL_ID = "mailId";
    public static final String PARAMETER_ATTACHMENT_ID = "attachmentId";
    public static final String PARAMETER_FOLDER_PATH = "folderPath";
    public static final String PARAMETER_SESSION_ID = "sessionId";
    public static final String PARAMETER_CLIENT_IP = "clientIp";
    public static final String PARAMETER_CLIENT = "client";
    public static final String PARAMETER_USER_AGENT = "userAgent";
    public static final String PARAMETER_JSESSION_ID = "jsessionId";
    public static final String PARAMETER_ONE_TIME = "oneTime";
    public static final String PARAMETER_CHECK_IP = "checkIp";

    private String id;
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
     * Initializes a new {@link PortableAttachmentToken}.
     */
    public PortableAttachmentToken() {
        super();
    }

    /**
     * Initializes a new {@link PortableAttachmentToken}.
     */
    public PortableAttachmentToken(AttachmentToken token) {
        super();
        if (null == token) {
            id = "";
            contextId = 0;
            userId = 0;
            accountId = 0;
            mailId = "";
            attachmentId = "";
            folderPath = "";
            sessionId = "";
            clientIp = "";
            client = "";
            userAgent = "";
            jsessionId = "";
            oneTime = false;
            checkIp = false;
        } else {
            id = token.getId();
            contextId = token.getContextId();
            userId = token.getUserId();
            accountId = token.getAccountId();
            mailId = token.getMailId();
            attachmentId = token.getAttachmentId();
            folderPath = token.getFolderPath();
            sessionId = token.getSessionId();
            clientIp = token.getClientIp();
            client = token.getClient();
            userAgent = token.getUserAgent();
            jsessionId = token.getJSessionId();
            oneTime = token.isOneTime();
            checkIp = token.isCheckIp();
        }
    }

    @Override
    public int getFactoryId() {
        return FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return 108;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(PARAMETER_ID, id);

        writer.writeInt(PARAMETER_CONTEXT_ID, contextId);
        writer.writeInt(PARAMETER_USER_ID, userId);
        writer.writeInt(PARAMETER_ACCOUNT_ID, accountId);

        writer.writeUTF(PARAMETER_MAIL_ID, mailId);
        writer.writeUTF(PARAMETER_ATTACHMENT_ID, attachmentId);
        writer.writeUTF(PARAMETER_FOLDER_PATH, folderPath);
        writer.writeUTF(PARAMETER_SESSION_ID, sessionId);

        writer.writeUTF(PARAMETER_CLIENT_IP, clientIp);
        writer.writeUTF(PARAMETER_CLIENT, client);

        writer.writeUTF(PARAMETER_USER_AGENT, userAgent);
        writer.writeUTF(PARAMETER_JSESSION_ID, jsessionId);

        writer.writeBoolean(PARAMETER_ONE_TIME, oneTime);
        writer.writeBoolean(PARAMETER_CHECK_IP, checkIp);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        id = reader.readUTF(PARAMETER_ID);

        contextId = reader.readInt(PARAMETER_CONTEXT_ID);
        userId = reader.readInt(PARAMETER_USER_ID);
        accountId = reader.readInt(PARAMETER_ACCOUNT_ID);

        mailId = reader.readUTF(PARAMETER_MAIL_ID);
        attachmentId = reader.readUTF(PARAMETER_ATTACHMENT_ID);
        folderPath = reader.readUTF(PARAMETER_FOLDER_PATH);
        sessionId = reader.readUTF(PARAMETER_SESSION_ID);

        clientIp = reader.readUTF(PARAMETER_CLIENT_IP);
        client = reader.readUTF(PARAMETER_CLIENT);

        userAgent = reader.readUTF(PARAMETER_USER_AGENT);
        jsessionId = reader.readUTF(PARAMETER_JSESSION_ID);

        oneTime = reader.readBoolean(PARAMETER_ONE_TIME);
        checkIp = reader.readBoolean(PARAMETER_CHECK_IP);
    }

    /**
     * Checks validity
     *
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    public boolean isValid() {
        return null != id && id.length() > 0;
    }

    /**
     * Gets the id
     *
     * @return The id
     */
    public String getId() {
        return id;
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
     * Gets the folderPath
     *
     * @return The folderPath
     */
    public String getFolderPath() {
        return folderPath;
    }

    /**
     * Gets the sessionId
     *
     * @return The sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the clientIp
     *
     * @return The clientIp
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
     * Gets the userAgent
     *
     * @return The userAgent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Gets the jsessionId
     *
     * @return The jsessionId
     */
    public String getJsessionId() {
        return jsessionId;
    }

    /**
     * Gets the oneTime
     *
     * @return The oneTime
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
     * Sets the id
     *
     * @param id The id to set
     */
    public void setId(String id) {
        this.id = id;
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

    /**
     * Sets the oneTime
     *
     * @param oneTime The oneTime to set
     */
    public void setOneTime(boolean oneTime) {
        this.oneTime = oneTime;
    }

    /**
     * Sets the checkIp
     *
     * @param checkIp The checkIp to set
     */
    public void setCheckIp(boolean checkIp) {
        this.checkIp = checkIp;
    }

}
