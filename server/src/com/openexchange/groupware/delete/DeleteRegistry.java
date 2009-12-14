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

package com.openexchange.groupware.delete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.groupware.attach.impl.AttachmentContextDelete;
import com.openexchange.groupware.attach.impl.AttachmentDelDelete;
import com.openexchange.groupware.calendar.CalendarAdministrationService;
import com.openexchange.groupware.contact.ContactDeleteListener;
import com.openexchange.groupware.filestore.FileStorageRemover;
import com.openexchange.groupware.infostore.InfostoreDelete;
import com.openexchange.groupware.tasks.TasksDelete;
import com.openexchange.groupware.userconfiguration.UserConfigurationDeleteListener;
import com.openexchange.image.internal.ImageRegistryDeleteListener;
import com.openexchange.mail.usersetting.UserSettingMailDeleteListener;
import com.openexchange.mailaccount.internal.MailAccountDeleteListener;
import com.openexchange.preferences.UserSettingServerDeleteListener;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.file.QuotaUsageDelete;
import com.openexchange.tools.oxfolder.OXFolderDeleteListener;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link DeleteRegistry} - A registry for instances of {@link DeleteListener} whose
 * {@link DeleteListener#deletePerformed(DeleteEvent, Connection, Connection)} methods are executed in the order added to this registry.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DeleteRegistry {

    /**
     * Dummy value to associate with an Object in the concurrent map.
     */
    private static final Object PRESENT = new Object();

    /**
     * The singleton instance.
     */
    private static DeleteRegistry instance;

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
    private final Queue<DeleteListener> listeners;

    /**
     * Initializes a new {@link DeleteRegistry}.
     */
    private DeleteRegistry() {
        super();
        classes = new ConcurrentHashMap<Class<? extends DeleteListener>, Object>();
        final DeleteListener[] staticListeners = getStaticListeners();
        for (final DeleteListener deleteListener : staticListeners) {
            classes.put(deleteListener.getClass(), PRESENT);
        }
        this.staticListeners = new CopyOnWriteArrayList<DeleteListener>(staticListeners);
        listeners = new ConcurrentLinkedQueue<DeleteListener>();
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
            /*
             * Insert module delete listener
             */
            new TasksDelete(),
            new InfostoreDelete(),
            new ContactDeleteListener(),
            ServerServiceRegistry.getInstance().getService(CalendarAdministrationService.class),
            /*
             * Delete user configuration & settings
             */
            new UserConfigurationDeleteListener(),
            new UserSettingMailDeleteListener(),
            new QuotaUsageDelete(),
            new AttachmentContextDelete(),
            new AttachmentDelDelete(),
            /*
             * At last insert folder delete listener
             */
            new OXFolderDeleteListener(),
            /*
             * Remove FileStorage if context is deleted.
             */
            new FileStorageRemover(),
            /*
             * Remove stored images
             */
            new ImageRegistryDeleteListener(),
            new UserSettingServerDeleteListener(),
            new MailAccountDeleteListener(),
            /*
             * Remove POP3 data
             */
            new DeleteListener() {

                public void deletePerformed(final DeleteEvent deleteEvent, final Connection readCon, final Connection writeCon) throws DeleteFailedException {
                    if (DeleteEvent.TYPE_USER == deleteEvent.getType()) {
                        PreparedStatement stmt = null;
                        try {
                            final int contextId = deleteEvent.getContext().getContextId();
                            final int user = deleteEvent.getId();

                            stmt = writeCon.prepareStatement("DELETE FROM pop3_storage_deleted WHERE cid = ? AND user = ?");
                            int pos = 1;
                            stmt.setInt(pos++, contextId);
                            stmt.setInt(pos++, user);
                            stmt.executeUpdate();
                            DBUtils.closeSQLStuff(stmt);

                            stmt = writeCon.prepareStatement("DELETE FROM pop3_storage_ids WHERE cid = ? AND user = ?");
                            pos = 1;
                            stmt.setInt(pos++, contextId);
                            stmt.setInt(pos++, user);
                            stmt.executeUpdate();
                        } catch (final SQLException e) {
                            throw new DeleteFailedException(DeleteFailedException.Code.SQL_ERROR, e, e.getMessage());
                        } finally {
                            DBUtils.closeSQLStuff(stmt);
                        }
                    }
                }
            } };
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
        return listeners.offer(listener);
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

    /**
     * Fires the delete event.
     * 
     * @param deleteEvent The delete event
     * @param readCon A readable connection
     * @param writeCon A writable connection
     * @throws DeleteFailedException If delete event could not be performed
     */
    public void fireDeleteEvent(final DeleteEvent deleteEvent, final Connection readCon, final Connection writeCon) throws DeleteFailedException {
        /*
         * At first trigger dynamically added listeners
         */
        for (final Iterator<DeleteListener> iter = listeners.iterator(); iter.hasNext();) {
            final DeleteListener listener = iter.next();
            listener.deletePerformed(deleteEvent, readCon, writeCon);
        }
        /*
         * Now trigger static listeners
         */
        for (final Iterator<DeleteListener> iter = staticListeners.iterator(); iter.hasNext();) {
            final DeleteListener listener = iter.next();
            listener.deletePerformed(deleteEvent, readCon, writeCon);
        }
    }

}
