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

package com.openexchange.mail.twitter;

import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mail.config.MailAccountProperties;
import com.openexchange.mail.twitter.config.TwitterConfig;
import com.openexchange.mail.twitter.services.TwitterServiceRegistry;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;
import com.openexchange.twitter.TwitterService;

/**
 * {@link TwitterAccess} - The twitter mail access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterAccess extends MailAccess<TwitterFolderStorage, TwitterMessageStorage> {

    private static final long serialVersionUID = -3267143544763657976L;

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(TwitterAccess.class));

    /*
     * Member section
     */

    private transient TwitterFolderStorage folderStorage;

    private transient TwitterMessageStorage messageStorage;

    private transient MailLogicTools logicTools;

    private com.openexchange.twitter.TwitterAccess twitterAccess;

    /**
     * Initializes a new {@link TwitterAccess}.
     *
     * @param session The session
     * @param accountId The account ID
     */
    public TwitterAccess(final Session session, final int accountId) {
        super(session, accountId);
    }

    private void reset() {
        super.resetFields();
        twitterAccess = null;
        folderStorage = null;
        messageStorage = null;
        logicTools = null;
    }

    /**
     * Gets the twitter configuration.
     *
     * @return The twitter configuration
     */
    public TwitterConfig getTwitterConfig() {
        try {
            return (TwitterConfig) getMailConfig();
        } catch (final OXException e) {
            /*
             * Cannot occur since already initialized
             */
            return null;
        }
    }

    @Override
    protected boolean checkMailServerPort() {
        return false;
    }

    @Override
    protected void closeInternal() {
        if (twitterAccess != null) {
            twitterAccess = null;
        }
        /*
         * Reset
         */
        reset();
    }

    @Override
    protected void connectInternal() throws OXException {
        if (null != twitterAccess) {
            // Already connected
            return;
        }
        try {
            final TwitterService twitterService = TwitterServiceRegistry.getServiceRegistry().getService(TwitterService.class, true);
            final TwitterConfig twitterConfig = getTwitterConfig();
            twitterAccess = twitterService.getTwitterAccess(twitterConfig.getLogin(), twitterConfig.getPassword());
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    @Override
    protected MailConfig createNewMailConfig() {
        return new TwitterConfig();
    }

    @Override
    protected IMailProperties createNewMailProperties() throws OXException {
        try {
            final MailAccountStorageService storageService =
                TwitterServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
            return new MailAccountProperties(storageService.getMailAccount(accountId, session.getUserId(), session.getContextId()));
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public TwitterFolderStorage getFolderStorage() throws OXException {
        if (null == twitterAccess) {
            throw MailExceptionCode.NOT_CONNECTED.create();
        }
        if (null == folderStorage) {
            folderStorage = new TwitterFolderStorage(session);
        }
        return folderStorage;
    }

    @Override
    public MailLogicTools getLogicTools() throws OXException {
        if (null == twitterAccess) {
            throw MailExceptionCode.NOT_CONNECTED.create();
        }
        if (null == logicTools) {
            logicTools = new MailLogicTools(session, accountId);
        }
        return logicTools;
    }

    @Override
    public TwitterMessageStorage getMessageStorage() throws OXException {
        if (null == twitterAccess) {
            throw MailExceptionCode.NOT_CONNECTED.create();
        }
        if (null == messageStorage) {
            messageStorage = new TwitterMessageStorage(twitterAccess, session, accountId);
        }
        return messageStorage;
    }

    @Override
    public boolean isConnected() {
        return null != twitterAccess;
    }

    @Override
    public boolean isConnectedUnsafe() {
        return null != twitterAccess;
    }

    @Override
    protected void releaseResources() {
        if (folderStorage != null) {
            try {
                folderStorage.releaseResources();
            } catch (final OXException e) {
                LOG.error(new StringBuilder("Error while closing POP3 folder storage: ").append(e.getMessage()).toString(), e);
            } finally {
                folderStorage = null;
            }
        }
        if (messageStorage != null) {
            try {
                messageStorage.releaseResources();
            } catch (final OXException e) {
                LOG.error(new StringBuilder("Error while closing POP3 message storage: ").append(e.getMessage()).toString(), e);
            } finally {
                messageStorage = null;

            }
        }
        if (logicTools != null) {
            logicTools = null;
        }
    }

    @Override
    protected void shutdown() throws OXException {
        // Nothing to do
    }

    @Override
    protected void startup() throws OXException {
        // Nothing to do
    }

}
