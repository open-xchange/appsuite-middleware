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

package com.openexchange.groupware.tasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.ArrayIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;

/**
 * This class implements the methods needed by the tasks interface of the API version 2.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a> (findTask method)
 */
public class TasksSQLImpl implements TasksSQLInterface {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TasksSQLImpl.class);

    private final Session session;

    /**
     * Default constructor.
     * @param session Session.
     */
    public TasksSQLImpl(final Session session) {
        super();
        this.session = session;
    }

    /**
     * TODO eliminate duplicate columns
     */
    @Override
    public SearchIterator<Task> getTaskList(final int folderId, final int from,
        final int until, final int orderBy, final Order order,
        final int[] columns) throws OXException {
        boolean onlyOwn;
        final Context ctx;
        final int userId = session.getUserId();
        final User user;
        final UserPermissionBits permissionBits;
        final FolderObject folder;
        try {
            ctx = Tools.getContext(session.getContextId());
            user = Tools.getUser(ctx, userId);
            permissionBits = Tools.getUserPermissionBits(ctx, userId);
            folder = Tools.getFolder(ctx, folderId);
        } catch (final OXException e) {
            if (OXFolderExceptionCode.NOT_EXISTS.equals(e)) {
                return SearchIteratorAdapter.emptyIterator();
            }
            throw e;
        }
        try {
            onlyOwn = Permission.canReadInFolder(ctx, user, permissionBits, folder);
        } catch (final OXException e) {
            throw e;
        }
        final boolean noPrivate = Tools.isFolderShared(folder, user);
        try {
            return TaskStorage.getInstance().list(ctx, folderId, from, until,
                orderBy, order, columns, onlyOwn, userId, noPrivate);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public Task getTaskById(final int taskId, final int folderId) throws OXException {
        Context ctx = Tools.getContext(session.getContextId());
        int userId = session.getUserId();
        User user = Tools.getUser(ctx, userId);
        UserPermissionBits permissionBits = Tools.getUserPermissionBits(ctx, userId);
        try {
            GetTask get = new GetTask(ctx, user, permissionBits, folderId, taskId, StorageType.ACTIVE);
            return get.loadAndCheck();
        } catch (final OXException e) {
            if (OXFolderExceptionCode.NOT_EXISTS.equals(e)) {
                // Parent folder does no more exist
                try {
                    DeleteData deleteData = new DeleteData(ctx, user, permissionBits, null, taskId, null);
                    deleteData.doDeleteHard(session, folderId, StorageType.ACTIVE);
                } catch (Exception x) {
                    // Ignore
                }
            }
            throw e;
        }
    }

    private SearchIterator<Task> getModifiedTasksInFolder(final int folderId, final int[] columns, final Date since, final StorageType type) throws OXException {
        final Context ctx;
        final User user;
        final int userId = session.getUserId();
        final UserPermissionBits permissionBits;
        FolderObject folder;
        try {
            ctx = Tools.getContext(session.getContextId());
            user = Tools.getUser(ctx, userId);
            permissionBits = Tools.getUserPermissionBits(ctx, userId);
            folder = Tools.getFolder(ctx, folderId);
        } catch (final OXException e) {
            if (OXFolderExceptionCode.NOT_EXISTS.equals(e)) {
                return SearchIteratorAdapter.emptyIterator();
            }
            throw e;
        }
        boolean onlyOwn;
        try {
            onlyOwn = Permission.canReadInFolder(ctx, user, permissionBits, folder);
        } catch (final OXException e) {
            throw e;
        }
        final boolean noPrivate = Tools.isFolderShared(folder, user);
        try {
            return TaskSearch.getInstance().listModifiedTasks(ctx, folderId, type, columns, since, onlyOwn, userId, noPrivate);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public SearchIterator<Task> getModifiedTasksInFolder(final int folderId, final int[] columns, final Date since) throws OXException {
        return getModifiedTasksInFolder(folderId, columns, since, StorageType.ACTIVE);
    }

    @Override
    public SearchIterator<Task> getDeletedTasksInFolder(final int folderId, final int[] columns, final Date since) throws OXException {
        return getModifiedTasksInFolder(folderId, columns, since, StorageType.DELETED);
    }

    @Override
    public void insertTaskObject(Task task) throws OXException {
        final Context ctx = Tools.getContext(session.getContextId());
        final int userId = session.getUserId();
        final User user = Tools.getUser(ctx, userId);
        final UserPermissionBits permissionBits = Tools.getUserPermissionBits(ctx, userId);
        final int folderId = task.getParentFolderID();
        final FolderObject folder = Tools.getFolder(ctx, folderId);
        InsertData insert = new InsertData(ctx, user, permissionBits, folder, task);
        insert.prepare(session);
        insert.doInsert();
        insert.createReminder();
        insert.sentEvent(session);

        collectAddresses(task, false);
    }

    /**
     * Tries to add addresses of external participants to the ContactCollector.
     *
     * @param task The {@link Task} to collect addresses for
     * @param incrementUseCount Whether the use-count is supposed to be incremented
     */
    private void collectAddresses(Task task, boolean incrementUseCount) {
        if ((task == null) || (task.getParticipants() == null)) {
            LOG.debug("Provided Task object or containing participants null. Nothing to collect for the ContactCollector!");
            return;
        }

        ContactCollectorService contactCollectorService = ServerServiceRegistry.getInstance().getService(ContactCollectorService.class);
        if (contactCollectorService != null) {
            Participant[] participants = task.getParticipants();

            List<InternetAddress> addresses = new ArrayList<InternetAddress>(participants.length) ;
            for (Participant participant : participants) {
                String emailAddress = participant.getEmailAddress();
                try {
                    if (emailAddress != null) {
                        addresses.add(new InternetAddress(emailAddress));
                    }
                } catch (AddressException addressException) {
                    LOG.warn("Unable to add address " + emailAddress + " to ContactCollector.", addressException);
                }
            }

            contactCollectorService.memorizeAddresses(addresses, incrementUseCount, session);
        }
    }

    @Override
    public void updateTaskObject(Task task, int folderId, Date lastRead) throws OXException {
        final Context ctx;
        final int userId = session.getUserId();
        final User user;
        final UserPermissionBits permissionBits;
        final FolderObject folder;
        try {
            ctx = Tools.getContext(session.getContextId());
            user = Tools.getUser(ctx, userId);
            permissionBits = Tools.getUserPermissionBits(ctx, userId);
            folder = Tools.getFolder(ctx, folderId);
        } catch (final OXException e) {
            throw e;
        }
        final UpdateData update = new UpdateData(ctx, user, permissionBits, folder, task, lastRead);
        try {
            update.prepare();
            // TODO join doUpdate(), updateReminder() and makeNextRecurrence() in one transaction.
            update.doUpdate();
            update.sentEvent(session);
            update.updateReminder();
            update.makeNextRecurrence(session);

            collectAddresses(update, false);
        } catch (final OXException e) {
            throw e;
        }
    }

    /**
     * Collects addresses from the given {@link UpdateData}
     *
     * @param update The {@link UpdateData} to get addresses from
     * @param incrementUseCount Whether use-count is supposed to be incremented
     * @throws OXException
     */
    private void collectAddresses(UpdateData update, boolean incrementUseCount) throws OXException {
        if (update == null) {
            LOG.info("Provided UpdateData object is null. Nothing to collect for the ContactCollector!");
            return;
        }
        ContactCollectorService contactCollectorService = ServerServiceRegistry.getInstance().getService(ContactCollectorService.class);
        Set<TaskParticipant> updatedParticipants = update.getUpdatedParticipants();

        if ((contactCollectorService != null) && (!updatedParticipants.isEmpty())) {

            List<InternetAddress> addresses = new ArrayList<InternetAddress>(updatedParticipants.size());
            for (TaskParticipant participant : updatedParticipants) {
                if (participant.getType() == TaskParticipant.Type.EXTERNAL) {
                    ExternalParticipant external = (ExternalParticipant) participant;
                    String mail = external.getMail();
                    try {
                        if (mail != null) {
                            addresses.add(new InternetAddress(mail));
                        }
                    } catch (AddressException addressException) {
                        LOG.warn("Unable to add address " + mail + " to ContactCollector.", addressException);
                    }
                }
            }
            contactCollectorService.memorizeAddresses(addresses, incrementUseCount, session);
        }
    }

    @Override
    public void deleteTaskObject(int taskId, int folderId, Date lastModified) throws OXException {
        final FolderObject folder;
        final Context ctx;
        final int userId = session.getUserId();
        final User user;
        final UserPermissionBits permissionBits;
        try {
            ctx = Tools.getContext(session.getContextId());
            user = Tools.getUser(ctx, userId);
            permissionBits = Tools.getUserPermissionBits(ctx, userId);
            folder = Tools.getFolder(ctx, folderId);
        } catch (final OXException e) {
            throw e;
        }
        final DeleteData delete = new DeleteData(ctx, user, permissionBits, folder, taskId, lastModified);
        try {
            delete.prepare();
            delete.doDelete();
            delete.sentEvent(session);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public Date setUserConfirmation(final int taskId, final int userId, final int confirm, final String message) throws OXException {
        final Context ctx;
        try {
            ctx = Tools.getContext(session.getContextId());
        } catch (final OXException e) {
            throw e;
        }
        final Date lastModified;
        try {
            final ConfirmTask confirmT = new ConfirmTask(ctx, taskId, userId, confirm, message);
            confirmT.prepare();
            confirmT.doConfirmation();
            lastModified = confirmT.getLastModified();
            confirmT.sentEvent(session);
        } catch (final OXException e) {
            throw e;
        }
        return lastModified;
    }

    @Override
    public SearchIterator<Task> getObjectsById(final int[][] ids,
        final int[] columns) throws OXException {
        final Context ctx;
        final int userId = session.getUserId();
        final User user;
        final UserPermissionBits permissionBits;
        try {
            ctx = Tools.getContext(session.getContextId());
            user = Tools.getUser(ctx, userId);
            permissionBits = Tools.getUserPermissionBits(ctx, userId);
        } catch (final OXException e) {
            throw e;
        }
        // TODO Improve performance
        final List<Task> tasks = new ArrayList<Task>();
        for (final int[] objectAndFolderId : ids) {
            final GetTask get = new GetTask(ctx, user, permissionBits,
                objectAndFolderId[1], objectAndFolderId[0], StorageType.ACTIVE);
            try {
                tasks.add(get.loadAndCheck());
            } catch (final OXException e) {
                LOG.debug("", e);
            }
        }
        return new ArrayIterator<Task>(tasks.toArray(new Task[tasks.size()]));
    }

    @Override
    public SearchIterator<Task> getTasksByExtendedSearch(final TaskSearchObject searchData, final int orderBy, final Order order, final int[] columns) throws OXException {
        final Context ctx;
        final User user;
        final UserPermissionBits permissionBits;
        final int userId = session.getUserId();
        try {
            ctx = Tools.getContext(session.getContextId());
            user = Tools.getUser(ctx, userId);
            permissionBits = Tools.getUserPermissionBits(ctx, userId);
            final Search search = new Search(ctx, user, permissionBits, searchData, orderBy, order, columns);
            return search.perform();
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public int countTasks(FolderObject folder) throws OXException {
        final Context ctx = Tools.getContext(session.getContextId());
        final int userId = session.getUserId();
        final User user = Tools.getUser(ctx, userId);
        final UserPermissionBits permissionBits = Tools.getUserPermissionBits(ctx, userId);
        boolean onlyOwn = Permission.canReadInFolder(ctx, user, permissionBits, folder);
        boolean isShared = FolderObject.SHARED == folder.getType(userId);
        return TaskStorage.getInstance().countTasks(ctx, userId, folder.getObjectID(), onlyOwn, isShared);
    }

    /* (non-Javadoc)
     * @see com.openexchange.api2.TasksSQLInterface#findTask(com.openexchange.groupware.search.TaskSearchObject, int, com.openexchange.groupware.search.Order, int[])
     */
    @Override
    public SearchIterator<Task> findTask(TaskSearchObject searchObj, int orderBy, Order order, int[] cols) throws OXException {
        final Context ctx = Tools.getContext(session.getContextId());
        final int userID = session.getUserId();
        final User user = Tools.getUser(ctx, userID);
        final UserPermissionBits upb = Tools.getUserPermissionBits(ctx, userID);
        return new FindTask(ctx, user, upb, searchObj, orderBy, order, cols).perform();
    }
}
