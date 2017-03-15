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

package com.openexchange.mail.api;

import java.util.Collection;
import java.util.Properties;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.session.Session;

/**
 * {@link DelegatingMailAccess} - Wraps a {@link MailAccess} instance to delegate to.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public abstract class DelegatingMailAccess extends MailAccess<IMailFolderStorage, IMailMessageStorage> {

    private static final long serialVersionUID = -4906495158045777677L;

    private final MailAccess<IMailFolderStorage,IMailMessageStorage> delegate;

    /**
     * Initializes a new {@link DelegatingMailAccess} for the default mail account of the session-associated user.
     * <p>
     * This is the same as calling {@link #DelegatingMailAccess(MailAccess, Session, int)} with <code>accountId</code> set to {@link com.openexchange.mailaccount.Account#DEFAULT_ID default identifier}.
     *
     * @param delegate The mail access to delegate to
     * @param session The associated session
     */
    protected DelegatingMailAccess(final MailAccess<IMailFolderStorage,IMailMessageStorage> delegate, final Session session) {
        super(session);
        this.delegate = delegate;
    }

    /**
     * Initializes a new {@link DelegatingMailAccess} for denoted mail account of the session-associated user.
     *
     * @param delegate The mail access to delegate to
     * @param session The associated session
     * @param accountId The account identifier
     */
    protected DelegatingMailAccess(final MailAccess<IMailFolderStorage,IMailMessageStorage> delegate, final Session session, final int accountId) {
        super(session,accountId);
        this.delegate = delegate;
    }

    @Override
    public <T> T supports(Class<T> iface) throws OXException {
        return delegate.supports(iface);
    }

    @Override
    public Session getSession() {
        return delegate.getSession();
    }

    @Override
    public MailProvider getProvider() {
        return delegate.getProvider();
    }

    @Override
    protected MailAccess<IMailFolderStorage, IMailMessageStorage> setProvider(MailProvider provider) {
        delegate.setProvider(provider);
        return this;
    }

    @Override
    public void addWarnings(Collection<OXException> warnings) {
        delegate.addWarnings(warnings);
    }

    @Override
    public Collection<OXException> getWarnings() {
        return delegate.getWarnings();
    }

    @Override
    public Properties getMailProperties() {
        return delegate.getMailProperties();
    }

    @Override
    public void setMailProperties(Properties mailProperties) {
        delegate.setMailProperties(mailProperties);
    }

    @Override
    public boolean ping() throws OXException {
        return delegate.ping();
    }

    @Override
    public MailFolder getRootFolder() throws OXException {
        return delegate.getRootFolder();
    }

    @Override
    public int getUnreadMessagesCount(String fullname) throws OXException {
        return delegate.getUnreadMessagesCount(fullname);
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public void logTrace(StringBuilder sBuilder, Logger log) {
        delegate.logTrace(sBuilder, log);
    }

    @Override
    public MailConfig getMailConfig() throws OXException {
        return delegate.getMailConfig();
    }

    @Override
    public int getAccountId() {
        return delegate.getAccountId();
    }

    @Override
    public boolean isTrackable() {
        return delegate.isTrackable();
    }

    @Override
    public void setTrackable(boolean trackable) {
        delegate.setTrackable(trackable);
    }

    @Override
    public int getCacheIdleSeconds() {
        return delegate.getCacheIdleSeconds();
    }

    @Override
    public boolean isCacheable() {
        return delegate.isCacheable();
    }

    @Override
    public void setCacheable(boolean cacheable) {
        delegate.setCacheable(cacheable);
    }

    @Override
    public boolean isCached() {
        return delegate.isCached();
    }

    @Override
    public void setCached(boolean cached) {
        delegate.setCached(cached);
    }

    @Override
    public boolean isWaiting() {
        return delegate.isWaiting();
    }

    @Override
    public void setWaiting(boolean waiting) {
        delegate.setWaiting(waiting);
    }

    @Override
    public void invokeReleaseResources() {
        delegate.invokeReleaseResources();
    }

    @Override
    public IMailFolderStorage getFolderStorage() throws OXException {
        return delegate.getFolderStorage();
    }

    @Override
    public IMailMessageStorage getMessageStorage() throws OXException {
        return delegate.getMessageStorage();
    }

    @Override
    public MailLogicTools getLogicTools() throws OXException {
        return delegate.getLogicTools();
    }

    @Override
    public boolean isConnected() {
        return delegate.isConnected();
    }

    @Override
    public boolean isConnectedUnsafe() {
        return delegate.isConnectedUnsafe();
    }

    @Override
    protected void connectInternal() throws OXException {
        delegate.connectInternal();
    }

    @Override
    protected MailConfig createNewMailConfig() {
        return delegate.createNewMailConfig();
    }

    @Override
    protected IMailProperties createNewMailProperties() throws OXException {
        return delegate.createNewMailProperties();
    }

    @Override
    protected boolean checkMailServerPort() {
        return delegate.checkMailServerPort();
    }

    @Override
    protected void releaseResources() {
        delegate.releaseResources();
    }

    @Override
    protected void closeInternal() {
        delegate.closeInternal();
    }

    @Override
    protected void startup() throws OXException {
        delegate.startup();
    }

    @Override
    protected void shutdown() throws OXException {
        delegate.shutdown();
    }

    @Override
    protected boolean supports(AuthType authType) throws OXException {
        return delegate.supports(authType);
    }
}
