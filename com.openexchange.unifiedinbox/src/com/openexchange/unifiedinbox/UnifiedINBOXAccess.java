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

package com.openexchange.unifiedinbox;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.unifiedinbox.config.MailAccountUnifiedINBOXProperties;
import com.openexchange.unifiedinbox.config.UnifiedINBOXConfig;
import com.openexchange.unifiedinbox.services.UnifiedINBOXServiceRegistry;

/**
 * {@link UnifiedINBOXAccess} - Access to Unified INBOX.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedINBOXAccess extends MailAccess<UnifiedINBOXFolderStorage, UnifiedINBOXMessageStorage> {

    private static final long serialVersionUID = 6666321725945931657L;

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(UnifiedINBOXAccess.class);

    /**
     * Fullname of INBOX.
     */
    public static final String INBOX = "INBOX";

    /**
     * Fullname of Trash.
     */
    public static final String TRASH = "Trash";

    /**
     * Fullname of Sent.
     */
    public static final String SENT = "Sent";

    /**
     * Fullname of Spam.
     */
    public static final String SPAM = "Spam";

    /**
     * Fullname of Drafts.
     */
    public static final String DRAFTS = "Drafts";

    /**
     * A set containing all known default folders for an Unified INBOX account.
     */
    public static final Set<String> KNOWN_FOLDERS;

    static {
        final Set<String> tmp = new HashSet<String>(Arrays.asList(new String[] { INBOX, DRAFTS, SENT, SPAM, TRASH }));
        KNOWN_FOLDERS = Collections.unmodifiableSet(tmp);
    }

    /*-
     * Members
     */

    private boolean connected;

    private transient UnifiedINBOXFolderStorage folderStorage;

    private transient UnifiedINBOXMessageStorage messageStorage;

    private transient MailLogicTools logicTools;

    /**
     * Initializes a new {@link UnifiedINBOXAccess}.
     * 
     * @param session The session providing needed user data
     */
    protected UnifiedINBOXAccess(final Session session) {
        super(session);
    }

    /**
     * Initializes a new {@link UnifiedINBOXAccess}.
     * 
     * @param session The session providing needed user data
     * @param accountId The account ID
     */
    protected UnifiedINBOXAccess(final Session session, final int accountId) {
        super(session, accountId);
    }

    private void reset() {
        super.resetFields();
        folderStorage = null;
        messageStorage = null;
        logicTools = null;
        connected = false;
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
        reset();
    }

    @Override
    protected void connectInternal() throws MailException {
        // Nothing to connect
        connected = true;
    }

    @Override
    protected MailConfig createNewMailConfig() {
        return new UnifiedINBOXConfig();
    }

    @Override
    public UnifiedINBOXFolderStorage getFolderStorage() throws MailException {
        if (!connected) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.NOT_CONNECTED);
        }
        if (null == folderStorage) {
            folderStorage = new UnifiedINBOXFolderStorage(this, session);
        }
        return folderStorage;
    }

    @Override
    public MailLogicTools getLogicTools() throws MailException {
        if (!connected) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.NOT_CONNECTED);
        }
        if (null == logicTools) {
            logicTools = new MailLogicTools(session, accountId);
        }
        return logicTools;
    }

    @Override
    public UnifiedINBOXMessageStorage getMessageStorage() throws MailException {
        if (!connected) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.NOT_CONNECTED);
        }
        if (null == messageStorage) {
            messageStorage = new UnifiedINBOXMessageStorage(this, session);
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
        if (folderStorage != null) {
            try {
                folderStorage.releaseResources();
            } catch (final MailException e) {
                LOG.error(new StringBuilder("Error while closing Unified INBOX folder storage: ").append(e.getMessage()).toString(), e);
            } finally {
                folderStorage = null;
            }
        }
        if (messageStorage != null) {
            try {
                messageStorage.releaseResources();
            } catch (final MailException e) {
                LOG.error(new StringBuilder("Error while closing Unified INBOX message storage: ").append(e.getMessage()).toString(), e);
            } finally {
                messageStorage = null;

            }
        }
        if (logicTools != null) {
            logicTools = null;
        }
    }

    @Override
    protected void shutdown() throws MailException {
        // Nothing to shut-down
    }

    @Override
    protected void startup() throws MailException {
        // Nothing to start-up
    }

    @Override
    protected IMailProperties createNewMailProperties() throws MailException {
        try {
            final MailAccountStorageService storageService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(
                MailAccountStorageService.class,
                true);
            return new MailAccountUnifiedINBOXProperties(storageService.getMailAccount(
                accountId,
                session.getUserId(),
                session.getContextId()));
        } catch (final ServiceException e) {
            throw new MailException(e);
        } catch (final MailAccountException e) {
            throw new MailException(e);
        }
    }

}
