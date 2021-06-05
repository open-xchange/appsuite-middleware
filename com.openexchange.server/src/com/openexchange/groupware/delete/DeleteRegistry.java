/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.delete;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.Heartbeat;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.outlook.OutlookFolderDeleteListener;
import com.openexchange.group.internal.GroupDeleteListener;
import com.openexchange.groupware.attach.impl.AttachmentContextDelete;
import com.openexchange.groupware.attach.impl.AttachmentDelDelete;
import com.openexchange.groupware.contact.ContactDeleteListener;
import com.openexchange.groupware.delete.objectusagecount.ObjectUsageCountDeleteListener;
import com.openexchange.groupware.filestore.FileStorageRemover;
import com.openexchange.groupware.impl.id.SequenceContextDeleteListener;
import com.openexchange.groupware.infostore.InfostoreDelete;
import com.openexchange.groupware.ldap.UserContextDeleteListener;
import com.openexchange.groupware.tasks.TasksDelete;
import com.openexchange.groupware.userconfiguration.UserConfigurationDeleteListener;
import com.openexchange.java.Strings;
import com.openexchange.mail.usersetting.UserSettingMailDeleteListener;
import com.openexchange.mailaccount.internal.MailAccountDeleteListener;
import com.openexchange.preferences.UserSettingServerDeleteListener;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.sessiond.impl.SessionDeleteListener;
import com.openexchange.tools.file.QuotaUsageDelete;
import com.openexchange.tools.file.UserQuotaUsageDelete;
import com.openexchange.tools.oxfolder.OXFolderDeleteListener;
import com.openexchange.tools.oxfolder.deletelistener.ObjectPermissionDeleteListener;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link DeleteRegistry} - A registry for instances of {@link DeleteListener} whose
 * {@link DeleteListener#deletePerformed(DeleteEvent, Connection, Connection)} methods are executed in the order added to this registry.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DeleteRegistry {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DeleteRegistry.class);

    /**
     * Dummy value to associate with an Object in the concurrent map.
     */
    private static final Object PRESENT = new Object();

    /**
     * The singleton instance.
     */
    private static volatile DeleteRegistry instance;

    /**
     * Initializes singleton instance.
     */
    static void initInstance() {
        instance = new DeleteRegistry();
    }

    /**
     * Releases singleton instance.
     */
    static void releaseInstance() {
        instance.dispose();
        instance = null;
    }

    /**
     * Gets the singleton instance of {@link DeleteRegistry}.
     *
     * @return The singleton instance of {@link DeleteRegistry}.
     */
    public static DeleteRegistry getInstance() {
        if (null == instance) {
            initInstance();
        }
        return instance;
    }

    /*-
     * Member section
     */

    /**
     * The class-set to detect duplicate listeners.
     */
    private final ConcurrentMap<Class<? extends DeleteListener>, Object> classes;

    /**
     * The list of static listeners.
     */
    private final List<DeleteListener> staticListeners;

    /**
     * The listener queue for dynamically added listeners.
     */
    private final List<DeleteListener> listeners = new CopyOnWriteArrayList<DeleteListener>();

    /**
     * Initializes a new {@link DeleteRegistry}.
     */
    private DeleteRegistry() {
        super();
        classes = new ConcurrentHashMap<Class<? extends DeleteListener>, Object>();
        final DeleteListener[] tmpListeners = getStaticListeners();
        for (final DeleteListener deleteListener : tmpListeners) {
            classes.put(deleteListener.getClass(), PRESENT);
        }
        this.staticListeners = new CopyOnWriteArrayList<DeleteListener>(tmpListeners);
    }

    /**
     * Disposes this delete registry.
     */
    private void dispose() {
        staticListeners.clear();
        classes.clear();
        listeners.clear();
    }

    /**
     * Initializes this delete registry; meaning static {@link DeleteListener listeners} are added.
     */
    private DeleteListener[] getStaticListeners() {
        return new DeleteListener[] {
            new SessionDeleteListener(),
            /*
             * Insert module delete listener
             */
            new TasksDelete(),
            new InfostoreDelete(),
            new ContactDeleteListener(),
            new GroupDeleteListener(),
            /*
             * Delete user configuration & settings
             */
            new UserConfigurationDeleteListener(),
            new UserSettingMailDeleteListener(),
            new ObjectUsageCountDeleteListener(),
            new QuotaUsageDelete(),
            new UserQuotaUsageDelete(),
            new AttachmentContextDelete(),
            new AttachmentDelDelete(),
            new SessionClearerOnContextDelete(),
            new InvalidateUserCacheOnContextDelete(),
            new ObjectPermissionDeleteListener(),
            /*
             * Insert folder delete listener
             */
            new OXFolderDeleteListener(),
            /*
             * Remove FileStorage if context is deleted.
             */
            new FileStorageRemover(),
            /*
             * Remove other stuff
             */
            new OutlookFolderDeleteListener(),
            new UserSettingServerDeleteListener(),
            new POP3DeleteListener(),
            new MailAccountDeleteListener(),
            new UserContextDeleteListener(),
            new SequenceContextDeleteListener()
        };
    }

    /**
     * Registers an instance of <code>{@link DeleteListener}</code>.
     * <p>
     * <b>Note</b>: Only one instance of a certain <code>{@link DeleteListener}</code> implementation is added, meaning if you try to
     * register a certain implementation twice, the latter one is going to be discarded and <code>false</code> is returned.
     *
     * @param listener The listener to register
     * @return <code>true</code> if specified delete listener has been added to registry; otherwise <code>false</code>
     */
    public boolean registerDeleteListener(final DeleteListener listener) {
        if (null != classes.putIfAbsent(listener.getClass(), PRESENT)) {
            return false;
        }
        return listeners.add(listener);
    }

    /**
     * Removes given instance of <code>{@link DeleteListener}</code> from this registry's known listeners.
     *
     * @param listener The listener to remove
     */
    public void unregisterDeleteListener(final DeleteListener listener) {
        if (null == classes.remove(listener.getClass())) {
            return;
        }
        listeners.remove(listener);
    }

    private static final Heartbeat DUMMY_HEARTBEAT = new Heartbeat() {

        @Override
        public void stopHeartbeat() {
            // Nothing
        }

        @Override
        public boolean startHeartbeat() {
            return false;
        }
    };

    /**
     * Fires the delete event.
     *
     * @param deleteEvent The delete event
     * @param readCon A readable connection
     * @param writeCon A writable connection
     * @throws OXException If delete event could not be performed
     */
    public void fireDeleteEvent(final DeleteEvent deleteEvent, final Connection readCon, final Connection writeCon) throws OXException {
        Heartbeat heartbeat = writeCon instanceof Heartbeat ? (Heartbeat) writeCon : DUMMY_HEARTBEAT;
        boolean heartbeatStarted = heartbeat.startHeartbeat();
        try {
            boolean error = true;
            try {
                /*
                 * At first trigger dynamically added listeners
                 */
                for (DeleteListener listener : listeners) {
                    listener.deletePerformed(deleteEvent, readCon, writeCon);
                }
                /*
                 * Now trigger static listeners
                 */
                for (DeleteListener listener : staticListeners) {
                    try {
                        listener.deletePerformed(deleteEvent, readCon, writeCon);
                    } catch (OXException e) {
                        OXException opt = logFKFailureElseReturnException(e, listener, deleteEvent);
                        if (null != opt) {
                            throw opt;
                        }
                    } catch (RuntimeException e) {
                        RuntimeException opt = logFKFailureElseReturnException(e, listener, deleteEvent);
                        if (null != opt) {
                            throw opt;
                        }
                    }
                }
                /*
                 * Commit Global DB connection (if any)
                 */
                Connection globalDbConnection = deleteEvent.optGlobalDbConnection();
                if (globalDbConnection != null) {
                    globalDbConnection.commit();
                }
                error = false;
            } catch (SQLException e) {
                throw DeleteFailedExceptionCode.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Connection globalDbConnection = deleteEvent.optGlobalDbConnection();
                if (globalDbConnection != null) {
                    if (error) {
                        Databases.rollback(globalDbConnection);
                    }
                    Databases.autocommit(globalDbConnection);
                    DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
                    databaseService.backWritableForGlobal(deleteEvent.getContext().getContextId(), globalDbConnection);
                }
            }
        } finally {
            if (heartbeatStarted) {
                heartbeat.stopHeartbeat();
            }
        }
    }

    private <E extends Exception> E logFKFailureElseReturnException(E e, DeleteListener listener, DeleteEvent deleteEvent) {
        if (deleteEvent.getType() != DeleteEvent.TYPE_CONTEXT) {
            return e;
        }

        // Check for special SQL exception for a failed foreign key constraint
        SQLException sqlException = DBUtils.extractSqlException(e);
        if (null == sqlException) {
            return e;
        }

        String message = sqlException.getMessage();
        if (Strings.asciiLowerCase(message).indexOf("a foreign key constraint fails") < 0) {
            return e;
        }

        // Just DEBUG log it. Hard-deletion takes place anyway in OXContextMySQLStorage.deleteContextData()
        LOGGER.debug("Failed foreign key constraint while executing '{}'", listener.getClass().getName(), sqlException);
        return null;
    }

}
