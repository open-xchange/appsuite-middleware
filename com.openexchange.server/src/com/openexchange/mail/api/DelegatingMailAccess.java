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

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link DelegatingMailAccess}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public abstract class DelegatingMailAccess extends MailAccess<IMailFolderStorage,IMailMessageStorage> {

    private static final long serialVersionUID = -4906495158045777677L;

    private final MailAccess<IMailFolderStorage,IMailMessageStorage> delegate;

    /**
     * Initializes a new {@link DelegatingMailAccess} for the default mail account of the session-associated user.
     * <p>
     * THis is the same as calling {@link #DelegatingMailAccess(MailAccess, Session, int)} with <code>accountId</code> set to {@link com.openexchange.mailaccount.Account#DEFAULT_ID default identifier}.
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
    protected void startup() throws OXException {
        delegate.startup();
    }

    @Override
    protected void shutdown() throws OXException {
        delegate.shutdown();
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailAccess#getMailConfig()
     */
    @Override
    public MailConfig getMailConfig() throws OXException {

        //Workaround for Bug #51721: Overriding this non abstract method for proper delegation.

        //TODO/FIXME:
        //If this class does not overwrite all public methods of MailAccess,
        //calls to this methods result in calling the super implementation of MailAccess,
        //but not the delegate's overwritten method.
        //Create proper interface?

        return delegate.getMailConfig();
    }
}
