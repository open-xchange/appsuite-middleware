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

package com.openexchange.unifiedinbox;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.session.Session;
import com.openexchange.unifiedinbox.config.MailAccountUnifiedINBOXProperties;
import com.openexchange.unifiedinbox.config.UnifiedInboxConfig;
import com.openexchange.unifiedinbox.services.Services;

/**
 * {@link UnifiedInboxAccess} - Access to Unified Mail.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxAccess extends MailAccess<UnifiedInboxFolderStorage, UnifiedInboxMessageStorage> {

    private static final long serialVersionUID = 6666321725945931657L;

    /**
     * Full name of INBOX.
     */
    public static final String INBOX = UnifiedInboxManagement.INBOX;

    /**
     * Full name of Trash.
     */
    public static final String TRASH = UnifiedInboxManagement.TRASH;

    /**
     * Full name of Sent.
     */
    public static final String SENT = UnifiedInboxManagement.SENT;

    /**
     * Full name of Spam.
     */
    public static final String SPAM = UnifiedInboxManagement.SPAM;

    /**
     * Full name of Drafts.
     */
    public static final String DRAFTS = UnifiedInboxManagement.DRAFTS;

    /**
     * A set containing all known default folders for an Unified Mail account.
     */
    public static final Set<String> KNOWN_FOLDERS = UnifiedInboxManagement.KNOWN_FOLDERS;

    /*-
     * Members
     */

    private final TIntObjectMap<MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>> openedMailAccessed;
    private boolean connected;
    private transient UnifiedInboxFolderStorage folderStorage;
    private transient UnifiedInboxMessageStorage messageStorage;
    private transient UnifiedInboxLogicTools logicTools;

    /**
     * Initializes a new {@link UnifiedInboxAccess}.
     *
     * @param session The session providing needed user data
     */
    protected UnifiedInboxAccess(final Session session) {
        super(session);
        openedMailAccessed = new TIntObjectHashMap<MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage>>(8);
        cacheable = false;
    }

    /**
     * Initializes a new {@link UnifiedInboxAccess}.
     *
     * @param session The session providing needed user data
     * @param accountId The account ID
     */
    protected UnifiedInboxAccess(final Session session, final int accountId) {
        super(session, accountId);
        openedMailAccessed = new TIntObjectHashMap<MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage>>(8);
        cacheable = false;
    }

    /**
     * Gets the map holding opened {@link MailAccess} instances.
     *
     * @return The map
     */
    public TIntObjectMap<MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>> getOpenedMailAccessed() {
        return openedMailAccessed;
    }

    /**
     * Stores given {@code MailAccess} instance if no such instance is currently held in map.
     *
     * @param accountId The account identifier
     * @param mailAccess The opened {@code MailAccess} instance
     * @return <code>true</code> if put into map; otherwise <code>false</code> if such an instance is currently held in map
     */
    public boolean storeOpenedMailAccessIfAbsent(int accountId, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) {
        if (null != mailAccess) {
            if (!openedMailAccessed.containsKey(accountId)) {
                openedMailAccessed.put(accountId, mailAccess);
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the {@code MailAccess} instance associated with given account identifier.
     *
     * @param accountId The account identifier
     * @return The {@code MailAccess} instance or <code>null</code>
     */
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getOpenedMailAccess(int accountId) {
        return openedMailAccessed.get(accountId);
    }

    /**
     * Removes the {@code MailAccess} instance associated with given account identifier.
     *
     * @param accountId The account identifier
     * @return The removed {@code MailAccess} instance or <code>null</code>
     */
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> removeOpenedMailAccess(int accountId) {
        return openedMailAccessed.remove(accountId);
    }

    private void reset(boolean markClosed) {
        super.resetFields();

        UnifiedInboxFolderStorage folderStorage = this.folderStorage;
        if (null != folderStorage) {
            try {
                folderStorage.releaseResources();
            } catch (Exception e) {
                // Ignore
            }
            this.folderStorage = null;
        }

        UnifiedInboxMessageStorage messageStorage = this.messageStorage;
        if (null != messageStorage) {
            try {
                messageStorage.releaseResources();
            } catch (Exception e) {
                // Ignore
            }
            this.messageStorage = null;
        }

        logicTools = null;

        if (markClosed) {
            openedMailAccessed.forEachValue(CLOSER);
            openedMailAccessed.clear();
            connected = false;
        }
    }

    @Override
    protected boolean checkMailServerPort() {
        return false;
    }

    @Override
    protected void closeInternal() {
        /*
         * Reset
         */
        reset(true);
    }

    @Override
    protected void connectInternal() throws OXException {
        // Nothing to connect
        connected = true;
    }

    @Override
    protected MailConfig createNewMailConfig() {
        return new UnifiedInboxConfig();
    }

    @Override
    public UnifiedInboxFolderStorage getFolderStorage() throws OXException {
        if (!connected) {
            throw UnifiedInboxException.Code.NOT_CONNECTED.create();
        }
        if (null == folderStorage) {
            folderStorage = new UnifiedInboxFolderStorage(this, session);
        }
        return folderStorage;
    }

    @Override
    public MailLogicTools getLogicTools() throws OXException {
        if (!connected) {
            throw UnifiedInboxException.Code.NOT_CONNECTED.create();
        }
        if (null == logicTools) {
            logicTools = new UnifiedInboxLogicTools(session, accountId);
        }
        return logicTools;
    }

    @Override
    public UnifiedInboxMessageStorage getMessageStorage() throws OXException {
        if (!connected) {
            throw UnifiedInboxException.Code.NOT_CONNECTED.create();
        }
        if (null == messageStorage) {
            messageStorage = new UnifiedInboxMessageStorage(this, session);
        }
        return messageStorage;
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
    protected void releaseResources() {
        reset(false);
    }

    @Override
    protected void shutdown() throws OXException {
        // Nothing to shut-down
    }

    @Override
    protected void startup() throws OXException {
        // Nothing to start-up
    }

    @Override
    protected IMailProperties createNewMailProperties() throws OXException {
        try {
            final MailAccountStorageService storageService = Services.getService(MailAccountStorageService.class);
            return new MailAccountUnifiedINBOXProperties(storageService.getMailAccount(
                accountId,
                session.getUserId(),
                session.getContextId()));
        } catch (final OXException e) {
            throw e;
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------- //

    private static final TObjectProcedure<MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>> CLOSER = new TObjectProcedure<MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage>>() {

        @Override
        public boolean execute(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> openedMailAccess) {
            Streams.close(openedMailAccess);
            return true;
        }
    };

}
