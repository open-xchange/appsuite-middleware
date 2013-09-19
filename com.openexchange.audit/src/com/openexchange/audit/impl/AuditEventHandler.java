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

package com.openexchange.audit.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.api2.FolderSQLInterface;
import com.openexchange.api2.RdbFolderSQLInterface;
import com.openexchange.audit.configuration.AuditConfiguration;
import com.openexchange.audit.logging.AuditFileHandler;
import com.openexchange.audit.logging.AuditFilter;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.publish.tools.PublicationSession;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 */
public class AuditEventHandler implements EventHandler {

    private static final Logger LOG = Logger.getLogger(AuditEventHandler.class.getName());

    private static final AuditEventHandler instance = new AuditEventHandler();

    private static final SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static AuditEventHandler getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link AuditEventHandler}.
     */
    private AuditEventHandler() {
        super();
        try {
            /*
             * Find out if the custom FileHandler should be used to log into a seperate logfile. If so, add a filter to the root logger to
             * avoid that the messages will also be written to the master logfile.
             */
            if (AuditConfiguration.getEnabled() == true) {
                try {
                    final Logger rootLogger = Logger.getLogger("");
                    final Handler[] handlers = rootLogger.getHandlers();
                    for (final Handler handler : handlers) {
                        handler.setFilter(new AuditFilter());
                    }
                    LOG.addHandler(new AuditFileHandler());
                } catch (final SecurityException e) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                } catch (final IOException e) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                }
                LOG.info("Using own Logging instance.");
            } else {
                LOG.info("Using global Logging instance.");
            }
        } catch (final OXException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public void handleEvent(final Event event) {
        if (!LOG.isLoggable(Level.INFO)) {
            // Not allowed to log
            return;
        }
        try {
            final StringBuilder log = new StringBuilder(2048);
            final String topic = event.getTopic();
            if (topic.startsWith("com/openexchange/groupware/infostore/")) {
                Session eventSession = (Session) event.getProperty("session");
                if (eventSession instanceof PublicationSession) {
                    log.append(eventSession.getLocalIp());
                } else {
                    if (topic.equals(FileStorageEventConstants.CREATE_TOPIC)) {
                        log.append("EVENT TYPE: INSERT; ");
                    } else if (topic.equals(FileStorageEventConstants.UPDATE_TOPIC)) {
                        log.append("EVENT TYPE: UPDATE; ");
                    } else if (topic.equals(FileStorageEventConstants.DELETE_TOPIC)) {
                        log.append("EVENT TYPE: DELETE; ");
                    } else if (topic.equals(FileStorageEventConstants.ACCESS_TOPIC)) {
                        log.append("EVENT TYPE: ACCESS; ");
                    }
                    synchronized (logDateFormat) {
                        log.append("EVENT TIME: ").append(logDateFormat.format(new Date())).append("; ");
                    }
                    log.append("OBJECT TYPE: FILE; ");
                    final Session session = (Session) event.getProperty(FileStorageEventConstants.SESSION);
                    appendUserInformation(session.getUserId(), session.getContextId(), log);
                    log.append("CONTEXT ID: ").append(session.getContextId()).append("; ");
                    log.append("OBJECT ID: ").append(event.getProperty(FileStorageEventConstants.OBJECT_ID)).append("; ");
                    {
                        final Object fileName = event.getProperty(FileStorageEventConstants.FILE_NAME);
                        if (null != fileName) {
                            log.append("FILE NAME: ").append(fileName).append("; ");
                        }
                    }
                    log.append("SERVICE ID: ").append(event.getProperty(FileStorageEventConstants.SERVICE)).append("; ");
                    log.append("ACCOUNT ID: ").append(event.getProperty(FileStorageEventConstants.ACCOUNT_ID)).append("; ");
                    {
                        final String folderId = (String) event.getProperty(FileStorageEventConstants.FOLDER_ID);
                        if (null != folderId) {
                            try {
                                final int iFolderId = Integer.parseInt(folderId);
                                log.append("FOLDER: ").append(getPathToRoot(iFolderId, session)).append(';');
                            } catch (NumberFormatException e) {
                                log.append("FOLDER: ").append(folderId).append(';');
                            }
                        }
                    }
                }
            } else if (topic.startsWith("com/openexchange/groupware/")) {
                final CommonEvent commonEvent = (CommonEvent) event.getProperty(CommonEvent.EVENT_KEY);
                if (null != commonEvent) {
                    final int contextId = commonEvent.getContextId();
                    final Context context = ContextStorage.getInstance().getContext(contextId);

                    ModuleSwitch:switch (commonEvent.getModule()) {
                    default:
                        break ModuleSwitch;
                    case Types.APPOINTMENT:
                        final Appointment appointment = (Appointment) commonEvent.getActionObj();

                        if (commonEvent.getAction() == CommonEvent.INSERT) {
                            log.append("EVENT TYPE: INSERT; ");
                        } else if (commonEvent.getAction() == CommonEvent.UPDATE) {
                            log.append("EVENT TYPE: UPDATE; ");
                        } else if (commonEvent.getAction() == CommonEvent.DELETE) {
                            log.append("EVENT TYPE: DELETE; ");
                        }

                        synchronized (logDateFormat) {
                            log.append("EVENT TIME: ").append(logDateFormat.format(new Date())).append("; ");
                        }
                        log.append("OBJECT TYPE: APPOINTMENT; ");
                        appendUserInformation(commonEvent.getUserId(), contextId, log);
                        log.append("CONTEXT ID: ").append(contextId).append("; ");
                        log.append("OBJECT ID: ").append(appointment.getObjectID()).append("; ");
                        log.append("CREATED BY: ").append(
                            UserStorage.getInstance().getUser(appointment.getCreatedBy(), context).getDisplayName()).append("; ");
                        log.append("MODIFIED BY: ").append(
                            UserStorage.getInstance().getUser(appointment.getModifiedBy(), context).getDisplayName()).append("; ");
                        log.append("TITLE: ").append(appointment.getTitle()).append("; ");
                        log.append("START DATE: ").append(appointment.getStartDate()).append("; ");
                        log.append("END DATE: ").append(appointment.getEndDate()).append("; ");
                        log.append("FOLDER: ").append(getPathToRoot(appointment.getParentFolderID(), commonEvent.getSession())).append(';');

                        break ModuleSwitch;
                    case Types.CONTACT:
                        /*
                         * get as much contact data as possible for log
                         */
                        Contact contact = null;
                        if (null != commonEvent.getActionObj() && Contact.class.isInstance(commonEvent.getActionObj())) {
                            contact = (Contact) commonEvent.getActionObj();
                        }
                        if (CommonEvent.DELETE != commonEvent.getAction() && (null == contact || false == contact.containsDisplayName() || false == contact.containsCreatedBy() || false == contact.containsModifiedBy() || false == contact.containsObjectID() || false == contact.containsParentFolderID())) {
                            contact = Contacts.getContactById(((Contact) commonEvent.getActionObj()).getObjectID(), commonEvent.getSession());
                        }

                        if (commonEvent.getAction() == CommonEvent.INSERT) {
                            log.append("EVENT TYPE: INSERT; ");
                        } else if (commonEvent.getAction() == CommonEvent.UPDATE) {
                            log.append("EVENT TYPE: UPDATE; ");
                        } else if (commonEvent.getAction() == CommonEvent.DELETE) {
                            log.append("EVENT TYPE: DELETE; ");
                        }

                        synchronized (logDateFormat) {
                            log.append("EVENT TIME: ").append(logDateFormat.format(new Date())).append("; ");
                        }
                        log.append("OBJECT TYPE: CONTACT; ");
                        appendUserInformation(commonEvent.getUserId(), contextId, log);
                        log.append("CONTEXT ID: ").append(contextId).append("; ");
                        if (null != contact) {
                            log.append("OBJECT ID: ").append(contact.getObjectID()).append("; ");
                            if (contact.containsCreatedBy()) {
                                log.append("CREATED BY: ").append(
                                    UserStorage.getInstance().getUser(contact.getCreatedBy(), context).getDisplayName()).append("; ");
                            }
                            if (contact.containsModifiedBy()) {
                                log.append("MODIFIED BY: ").append(
                                    UserStorage.getInstance().getUser(contact.getModifiedBy(), context).getDisplayName()).append("; ");
                            }
                            log.append("CONTACT FULLNAME: ").append(contact.getDisplayName()).append(';');
                            log.append("FOLDER: ").append(getPathToRoot(contact.getParentFolderID(), commonEvent.getSession())).append(';');
                        }
                        break ModuleSwitch;
                    case Types.TASK:
                        final Task task = (Task) commonEvent.getActionObj();

                        if (commonEvent.getAction() == CommonEvent.INSERT) {
                            log.append("EVENT TYPE: INSERT; ");
                        } else if (commonEvent.getAction() == CommonEvent.UPDATE) {
                            log.append("EVENT TYPE: UPDATE; ");
                        } else if (commonEvent.getAction() == CommonEvent.DELETE) {
                            log.append("EVENT TYPE: DELETE; ");
                        }

                        synchronized (logDateFormat) {
                            log.append("EVENT TIME: ").append(logDateFormat.format(new Date())).append("; ");
                        }
                        log.append("OBJECT TYPE: TASK; ");
                        appendUserInformation(commonEvent.getUserId(), contextId, log);
                        log.append("CONTEXT ID: ").append(contextId).append("; ");
                        log.append("OBJECT ID: ").append(task.getObjectID()).append("; ");
                        log.append("CREATED BY: ").append(UserStorage.getInstance().getUser(task.getCreatedBy(), context).getDisplayName()).append(
                            "; ");
                        log.append("MODIFIED BY: ").append(UserStorage.getInstance().getUser(task.getModifiedBy(), context).getDisplayName()).append(
                            "; ");
                        log.append("TITLE: ").append(task.getTitle()).append("; ");
                        log.append("FOLDER: ").append(getPathToRoot(task.getParentFolderID(), commonEvent.getSession())).append(';');

                        break ModuleSwitch;
                    case Types.INFOSTORE:
                        final DocumentMetadata document = (DocumentMetadata) commonEvent.getActionObj();

                        if (commonEvent.getAction() == CommonEvent.INSERT) {
                            log.append("EVENT TYPE: INSERT; ");
                        } else if (commonEvent.getAction() == CommonEvent.UPDATE) {
                            log.append("EVENT TYPE: UPDATE; ");
                        } else if (commonEvent.getAction() == CommonEvent.DELETE) {
                            log.append("EVENT TYPE: DELETE; ");
                        }

                        synchronized (logDateFormat) {
                            log.append("EVENT TIME: ").append(logDateFormat.format(new Date())).append("; ");
                        }
                        log.append("OBJECT TYPE: INFOSTORE; ");
                        appendUserInformation(commonEvent.getUserId(), contextId, log);
                        log.append("CONTEXT ID: ").append(contextId).append("; ");
                        log.append("OBJECT ID: ").append(document.getId()).append("; ");
                        log.append("CREATED BY: ").append(UserStorage.getInstance().getUser(document.getCreatedBy(), context).getDisplayName()).append(
                            "; ");
                        log.append("MODIFIED BY: ").append(
                            UserStorage.getInstance().getUser(document.getModifiedBy(), context).getDisplayName()).append("; ");
                        log.append("TITLE: ").append(document.getTitle()).append("; ");
                        log.append("TITLE: ").append(document.getFileName()).append("; ");
                        log.append("FOLDER: ").append(getPathToRoot((int) document.getFolderId(), commonEvent.getSession())).append(';');

                        break ModuleSwitch;
                    }
                }
            }

            final String infoMsg = log.toString();
            if (!isEmpty(infoMsg)) {
                LOG.log(Level.INFO, infoMsg);
            }
        } catch (final Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * This method will return the full folder path as String.
     *
     * @param folderId
     * @param sessionObj
     * @return String fullFolderPath
     */
    private String getPathToRoot(final int folderId, final Session sessionObj) {
        String retval = "";

        try {
            final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(ServerSessionAdapter.valueOf(sessionObj));
            final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getPathToRoot(folderId)).asQueue();
            final int size = q.size();
            final Iterator<FolderObject> iter = q.iterator();
            for (int i = 0; i < size; i++) {
                retval = iter.next().getFolderName() + "/" + retval;
            }
        } catch (final OXException e) {
            e.printStackTrace();
        }

        return retval;
    }

    private static void appendUserInformation(final int userId, final int contextId, final StringBuilder log) {
        String displayName;
        try {
            displayName = UserStorage.getInstance().getUser(userId, ContextStorage.getInstance().getContext(contextId)).getDisplayName();
        } catch (final Exception e) {
            // Ignore
            displayName = null;
        }
        log.append("USER: ");
        if (null == displayName) {
            log.append(userId);
        } else {
            log.append(displayName);
            log.append(" (").append(userId).append(')');
        }
        log.append("; ");
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
