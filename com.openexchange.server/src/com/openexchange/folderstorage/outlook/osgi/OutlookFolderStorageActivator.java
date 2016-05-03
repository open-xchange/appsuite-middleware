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

package com.openexchange.folderstorage.outlook.osgi;

import java.sql.Connection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.osgi.framework.BundleActivator;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.folderstorage.FolderEventConstants;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.outlook.OutlookFolderStorage;
import com.openexchange.folderstorage.outlook.memory.MemoryTable;
import com.openexchange.folderstorage.outlook.sql.Update;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.mailaccount.MailAccountFacade;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.push.PushEventConstants;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link OutlookFolderStorageActivator} - {@link BundleActivator Activator} for MS outlook folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OutlookFolderStorageActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link OutlookFolderStorageActivator}.
     */
    public OutlookFolderStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            DatabaseService.class, MailAccountFacade.class, ThreadPoolService.class, MessagingServiceRegistry.class,
            UnifiedInboxManagement.class, ConfigurationService.class, FileStorageServiceRegistry.class, SessiondService.class,
            EventAdmin.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(OutlookFolderStorageActivator.class);
        try {
            Services.setServiceLookup(this);

            // Trackers
            rememberTracker(new ServiceTracker<FolderStorage,FolderStorage>(context, FolderStorage.class, new OutlookFolderStorageServiceTracker(context)));
            openTrackers();

            // Register services

            registerService(MailAccountDeleteListener.class, new MailAccountDeleteListener() {

                @Override
                public void onBeforeMailAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) throws OXException {
                    OutlookFolderStorage.clearTCM();
                }

                @Override
                public void onAfterMailAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) throws OXException {
                    // Nothing todo
                }
            }, null);

            {
                final Dictionary<String, String> dictionary = new Hashtable<String, String>(2);
                dictionary.put("tree", OutlookFolderStorage.OUTLOOK_TREE_ID);
                registerService(FolderStorage.class, OutlookFolderStorage.getInstance(), dictionary);
            }

            {
                final EventHandler pushMailEventHandler = new EventHandler() {

                    @Override
                    public void handleEvent(final Event event) {
                        // final Session session = ((Session) event.getProperty(PushEventConstants.PROPERTY_SESSION));
                        // final String folderId = (String) event.getProperty(PushEventConstants.PROPERTY_FOLDER);
                        // final Boolean contentRelated = (Boolean) event.getProperty(PushEventConstants.PROPERTY_CONTENT_RELATED);
                        OutlookFolderStorage.clearTCM();
                    }
                };
                final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
                dict.put(EventConstants.EVENT_TOPIC, PushEventConstants.getAllTopics());
                registerService(EventHandler.class, pushMailEventHandler, dict);
            }
            {
                final EventHandler folderEventHandler = new EventHandler() {

                    private final int tree = Integer.parseInt(OutlookFolderStorage.OUTLOOK_TREE_ID);

                    @Override
                    public void handleEvent(final Event event) {
                        OutlookFolderStorage.clearTCM();
                        if (FolderEventConstants.TOPIC_IDENTIFIERS.equals(event.getTopic())) {
                            final Session session = ((Session) event.getProperty(FolderEventConstants.PROPERTY_SESSION));
                            final String newId = (String) event.getProperty(FolderEventConstants.PROPERTY_NEW_IDENTIFIER);
                            final String oldId = (String) event.getProperty(FolderEventConstants.PROPERTY_OLD_IDENTIFIER);
                            final String delim = (String) event.getProperty(FolderEventConstants.PROPERTY_DELIMITER);

                            try {
                                Update.updateIds(session.getContextId(), tree, session.getUserId(), newId, oldId, delim);
                            } catch (final Exception e) {
                                logger.error("", e);
                            }

                            final MemoryTable memoryTable = MemoryTable.optMemoryTableFor(session);
                            if (null != memoryTable) {
                                try {
                                    memoryTable.initializeTree(tree, session.getUserId(), session.getContextId());
                                } catch (final Exception e) {
                                    logger.error("", e);
                                }
                            }
                        }
                    }
                };
                final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
                dict.put(EventConstants.EVENT_TOPIC, FolderEventConstants.getAllTopics());
                registerService(EventHandler.class.getName(), folderEventHandler, dict);
            }
            {
                final EventHandler sessionEventHandler = new EventHandler() {

                    @Override
                    public void handleEvent(final Event event) {
                        final String topic = event.getTopic();
                        if (SessiondEventConstants.TOPIC_LAST_SESSION.equals(topic)) {
                            Integer contextId = (Integer) event.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
                            if (null != contextId) {
                                Integer userId = (Integer) event.getProperty(SessiondEventConstants.PROP_USER_ID);
                                if (null != userId) {
                                    MemoryTable.dropMemoryTableFrom(userId.intValue(), contextId.intValue());
                                }
                            }
                        }
                    }
                };
                final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
                dict.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.TOPIC_LAST_SESSION);
                registerService(EventHandler.class.getName(), sessionEventHandler, dict);
            }
        } catch (final Exception e) {
            logger.error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
    }

}
