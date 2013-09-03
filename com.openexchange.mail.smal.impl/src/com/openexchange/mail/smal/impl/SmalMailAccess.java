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

package com.openexchange.mail.smal.impl;

import com.openexchange.exception.OXException;
import com.openexchange.mail.MailAccessWatcher;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link SmalMailAccess} - The SMAL mail access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SmalMailAccess extends MailAccess<SmalFolderStorage, SmalMessageStorage> {

    private static final long serialVersionUID = 3887048765113161340L;

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(SmalMailAccess.class));

    private final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> delegateMailAccess;

    private boolean connected;

    private MailLogicTools logicTools;

    private SmalMessageStorage messageStorage;

    private SmalFolderStorage folderStorage;

    /**
     * Initializes a new {@link SmalMailAccess}.
     *
     * @param session The session
     * @param accountId The account identifier
     * @throws OXException If initialization fails
     */
    public SmalMailAccess(final Session session, final int accountId) throws OXException {
        super(session, accountId);
        if (null == session) {
            delegateMailAccess = null;
        } else {
            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> dma =
                SmalMailAccessCache.getInstance().removeMailAccess(session, accountId);
            delegateMailAccess = null == dma ? newDelegate(session, accountId) : dma;
        }
    }

    private static MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> newDelegate(final Session session, final int accountId) throws OXException {
        return SmalMailProviderRegistry.getMailProviderBySession(session, accountId).createNewMailAccess(session, accountId);
    }

    /**
     * Gets an un-wrapped {@link MailAccess} instance for specified account and session.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param accountId The account identifier
     * @return An un-wrapped {@link MailAccess} instance either fetched from cache or newly created
     * @throws OXException If {@link MailAccess} instance cannot be returned
     */
    public static final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getUnwrappedInstance(final int userId, final int contextId, final int accountId) throws OXException {
        final SessiondService sessiondService = SmalServiceLookup.getServiceStatic(SessiondService.class);
        if (null != sessiondService) {
            final Session session = sessiondService.getAnyActiveSessionForUser(userId, contextId);
            if (null != session) {
                return getUnwrappedInstance(session, accountId);
            }
        }
        /*
         * No appropriate session found.
         */
        throw MailExceptionCode.UNEXPECTED_ERROR.create("No appropriate session found.");
    }

    /**
     * Gets an un-wrapped {@link MailAccess} instance for specified account and session.
     *
     * @param session The session
     * @param accountId The account identifier
     * @return An un-wrapped {@link MailAccess} instance either fetched from cache or newly created
     * @throws OXException If {@link MailAccess} instance cannot be returned
     */
    public static final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getUnwrappedInstance(final Session session, final int accountId) throws OXException {
        /*
         * Check MailAccessCache
         */
        final Object sLookup = session.getParameter("com.openexchange.mail.lookupMailAccessCache");
        if (null == sLookup || Boolean.parseBoolean(sLookup.toString())) {
            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess =
                SmalMailAccessCache.getInstance().removeMailAccess(session, accountId);
            if (mailAccess != null) {
                return mailAccess;
            }
        }
        /*
         * Return new MailAccess instance
         */
        final MailProvider mailProvider = SmalMailProviderRegistry.getMailProviderBySession(session, accountId);
        return mailProvider.createNewMailAccess(session, accountId);
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    public boolean isCacheable() {
        /*
         * Return false to let closeInternal() being called
         */
        return false;
    }

    @Override
    protected MailConfig createNewMailConfig() {
        /*
         * Invoked in getMailConfig(), but overridden here
         */
        return null;
    }

    @Override
    protected IMailProperties createNewMailProperties() throws OXException {
        /*
         * Invoked in getMailConfig(), but overridden here
         */
        return null;
    }

    @Override
    protected boolean checkMailServerPort() {
        return false;
    }

    @Override
    public void releaseResources() {
        delegateMailAccess.invokeReleaseResources();
    }

    @Override
    protected void connectInternal() throws OXException {
        connected = true;
        /*
         * In any case invoke delegate's connect() method to let it be properly initialized: using thread, adding to watcher, etc.
         */
        delegateMailAccess.connect(false);
    }

    @Override
    protected void closeInternal() {
        closeUnwrappedInstance(delegateMailAccess);
        connected = false;
    }

    /**
     * Closes specified unwrapped mail access.
     */
    public static void closeUnwrappedInstance(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) {
        if (null == mailAccess || !mailAccess.isConnectedUnsafe()) {
            return;
        }
        boolean put = true;
        try {
            /*
             * Release all used, non-cachable resources
             */
            mailAccess.invokeReleaseResources();
        } catch (final Throwable t) {
            /*
             * Dropping
             */
            LOG.error("Resources could not be properly released. Dropping mail connection for safety reasons", t);
            put = false;
        }
        try {
            /*
             * Cache connection if desired/possible anymore
             */
            if (put && mailAccess.isCacheable() && SmalMailAccessCache.getInstance().putMailAccess(mailAccess.getSession(), mailAccess.getAccountId(), mailAccess)) {
                /*
                 * Successfully cached: return
                 */
                MailAccessWatcher.removeMailAccess(mailAccess);
                return;
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
        /*
         * Couldn't be put into cache
         */
        mailAccess.close(false);
    }

    @Override
    public void setWaiting(boolean waiting) {
        delegateMailAccess.setWaiting(waiting);
    }

    @Override
    public boolean isWaiting() {
        return delegateMailAccess.isWaiting();
    }

    @Override
    public MailConfig getMailConfig() throws OXException {
        return delegateMailAccess.getMailConfig();
    }

    @Override
    protected void checkFieldsBeforeConnect(final MailConfig mailConfig) throws OXException {
        // Nothing to do
    }

    @Override
    public boolean ping() throws OXException {
        return delegateMailAccess.ping();
    }

    @Override
    public SmalFolderStorage getFolderStorage() throws OXException {
        if (!connected) {
            throw MailExceptionCode.NOT_CONNECTED.create();
        }
        if (null == folderStorage) {
            folderStorage = new SmalFolderStorage(session, accountId, delegateMailAccess);
        }
        return folderStorage;
    }

    @Override
    public SmalMessageStorage getMessageStorage() throws OXException {
        if (!connected) {
            throw MailExceptionCode.NOT_CONNECTED.create();
        }
        if (null == messageStorage) {
            messageStorage = new SmalMessageStorage(session, accountId, delegateMailAccess);
        }
        return messageStorage;
    }

    @Override
    public MailLogicTools getLogicTools() throws OXException {
        if (!connected) {
            throw MailExceptionCode.NOT_CONNECTED.create();
        }
        if (null == logicTools) {
            logicTools = new MailLogicTools(session, accountId);
        }
        return logicTools;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean isConnectedUnsafe() {
        return connected;
    }

    @Override
    protected void startup() throws OXException {
        // Start-up operations
    }

    @Override
    protected void shutdown() throws OXException {
        // Shut-down operations
        SmalMailAccessCache.releaseInstance();
    }

}
