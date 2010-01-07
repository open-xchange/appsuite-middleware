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

package com.openexchange.mail.headercache;

import java.util.Properties;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.session.Session;

/**
 * {@link HeaderCacheMailAccess} - The header cache mail access.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HeaderCacheMailAccess extends MailAccess<IMailFolderStorage, HeaderCacheMessageStorage> {

    private static final long serialVersionUID = -7759787601014517182L;

    /**
     * The mail access.
     */
    private final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess;

    /**
     * The session.
     */
    private final Session session;

    /**
     * The header cache message storage.
     */
    private transient HeaderCacheMessageStorage messageStorage;

    /**
     * Initializes a new {@link HeaderCacheMailAccess}.
     * 
     * @param session The session
     * @param accountId The account Id
     * @param mailAccess The mail access to delegate to
     */
    public HeaderCacheMailAccess(final Session session, final int accountId, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) {
        super(session, accountId);
        this.mailAccess = mailAccess;
        this.session = session;
    }

    @Override
    public boolean equals(final Object obj) {
        return mailAccess.equals(obj);
    }

    @Override
    public int getAccountId() {
        return mailAccess.getAccountId();
    }

    @Override
    public int getCacheIdleSeconds() {
        return mailAccess.getCacheIdleSeconds();
    }

    @Override
    public IMailFolderStorage getFolderStorage() throws MailException {
        return mailAccess.getFolderStorage();
    }

    @Override
    public MailLogicTools getLogicTools() throws MailException {
        return mailAccess.getLogicTools();
    }

    @Override
    public HeaderCacheMessageStorage getMessageStorage() throws MailException {
        if (null == messageStorage) {
            messageStorage = new HeaderCacheMessageStorage(session, mailAccess);
        }
        return messageStorage;
    }

    @Override
    public MailConfig getMailConfig() throws MailException {
        return mailAccess.getMailConfig();
    }

    @Override
    public Properties getMailProperties() {
        return mailAccess.getMailProperties();
    }

    @Override
    public void setMailProperties(final Properties mailProperties) {
        mailAccess.setMailProperties(mailProperties);
    }

    @Override
    public MailFolder getRootFolder() throws MailException {
        return mailAccess.getRootFolder();
    }

    @Override
    public int getUnreadMessagesCount(final String fullname) throws MailException {
        return mailAccess.getUnreadMessagesCount(fullname);
    }

    @Override
    public int hashCode() {
        return mailAccess.hashCode();
    }

    @Override
    public boolean isCacheable() {
        return mailAccess.isCacheable();
    }

    @Override
    public boolean isConnected() {
        return mailAccess.isConnected();
    }

    @Override
    public boolean isConnectedUnsafe() {
        return mailAccess.isConnectedUnsafe();
    }

    @Override
    public boolean ping() throws MailException {
        return mailAccess.ping();
    }

    @Override
    public void setCacheable(final boolean cacheable) {
        mailAccess.setCacheable(cacheable);
    }

    @Override
    public String toString() {
        return mailAccess.toString();
    }

    @Override
    public boolean delegateCheckMailServerPort() {
        return mailAccess.delegateCheckMailServerPort();
    }

    @Override
    public void delegateCloseInternal() {
        mailAccess.delegateCloseInternal();
    }

    @Override
    public void delegateConnectInternal() throws MailException {
        mailAccess.delegateConnectInternal();
    }

    @Override
    public MailConfig delegateCreateNewMailConfig() {
        return mailAccess.delegateCreateNewMailConfig();
    }

    @Override
    public IMailProperties delegateCreateNewMailProperties() throws MailException {
        return mailAccess.delegateCreateNewMailProperties();
    }

    @Override
    public void delegateReleaseResources() {
        mailAccess.delegateReleaseResources();
    }

    @Override
    protected boolean checkMailServerPort() {
        throw new UnsupportedOperationException("checkMailServerPort()");
    }

    @Override
    protected void closeInternal() {
        throw new UnsupportedOperationException("closeInternal()");
    }

    @Override
    protected void connectInternal() throws MailException {
        throw new UnsupportedOperationException("connectInternal()");
    }

    @Override
    protected MailConfig createNewMailConfig() {
        throw new UnsupportedOperationException("createNewMailConfig()");
    }

    @Override
    protected IMailProperties createNewMailProperties() throws MailException {
        throw new UnsupportedOperationException("createNewMailProperties()");
    }

    @Override
    protected void releaseResources() {
        throw new UnsupportedOperationException("releaseResources()");
    }

    @Override
    protected void shutdown() throws MailException {
        throw new UnsupportedOperationException("shutdown()");
    }

    @Override
    protected void startup() throws MailException {
        throw new UnsupportedOperationException("startup()");
    }

}
