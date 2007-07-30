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

package com.openexchange.groupware.tasks;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.groupware.tasks.TaskParticipant.Type;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;

/**
 * Interface to different SQL implementations for storing task participants.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
abstract class ParticipantStorage {

    /**
     * Singleton attribute.
     */
    private static final ParticipantStorage SINGLETON =
        new RdbParticipantStorage();

    /**
     * Prevent instanciation.
     */
    protected ParticipantStorage() {
        super();
    }

    /**
     * @return the singleton implementation.
     */
    public static ParticipantStorage getInstance() {
        return SINGLETON;
    }

    /**
     * Reads the internal participants of a task.
     * @param ctx Context.
     * @param con readable database connection.
     * @param taskId unique identifier of the task.
     * @param type type of participant that should be selected.
     * @return a set of participants.
     * @throws TaskException if an error occurs.
     */
    final Set<InternalParticipant> selectInternal(final Context ctx,
        final Connection con, final int taskId, final StorageType type)
        throws TaskException {
        Set<InternalParticipant> parts = selectInternal(ctx, con,
            new int[] { taskId }, type).get(taskId);
        if (null == parts) {
            parts = Collections.emptySet();
        }
        return parts;
    }

    /**
     * Reads the internal participants of some tasks.
     * @param ctx Context.
     * @param con readable database connection.
     * @param tasks unique identifier of tasks.
     * @param type type of participant that should be selected.
     * @return a map with the task identifier as key and a set of internal
     * participants as value.
     * @throws TaskException if an error occurs.
     */
    protected abstract Map<Integer, Set<InternalParticipant>>
        selectInternal(Context ctx, Connection con, int[] tasks,
        StorageType type) throws TaskException;

    /**
     * Reads the internal participants of a task.
     * @param ctx Context.
     * @param taskId unique identifier of the task.
     * @param type type of participant that should be selected.
     * @return a set of participants.
     * @throws TaskException if an error occurs.
     */
    final Set<InternalParticipant> selectInternal(final Context ctx,
        final int taskId, final StorageType type) throws TaskException {
        final Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            return selectInternal(ctx, con, taskId, type);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * Updates a set of internal participants. This method only updates the
     * fields group, confirmation status and confirmation message. This method
     * does not insert new participants or removes participants.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task.
     * @param participants internal participants with updated values.
     * @param type storage type of participants that should be updated.
     * @throws TaskException if an error occurs.
     */
    abstract void updateInternal(Context ctx, Connection con, int taskId,
        Set<InternalParticipant> participants, StorageType type)
        throws TaskException;

    /**
     * Deletes internal participants of a task.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task.
     * @param users unique identifier of the internal participants.
     * @param type storage type of participants that should be deleted.
     * @param check <code>true</code> enables check if all given users are
     * deleted.
     * @throws TaskException if an exception occurs or less than the given users
     * are deleted and the check is enabled.
     */
    abstract void deleteInternal(Context ctx, Connection con, int taskId,
        int[] users, StorageType type, boolean check) throws TaskException;

    /**
     * Deletes an internal participant of a task.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task.
     * @param userId unique identifier of the internal participant.
     * @param type storage type of participant that should be deleted.
     * @param check <code>true</code> enables check if the entry is deleted.
     * @throws TaskException if an exception occurs or less than the given users
     * are deleted and the check is enabled.
     */
    final void deleteInternal(final Context ctx, final Connection con,
        final int taskId, final int userId, final StorageType type,
        final boolean check) throws TaskException {
        deleteInternal(ctx, con, taskId, new int[] { userId }, type, check);
    }

    /**
     * Deletes internal participants of a task.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task.
     * @param parts set of internal task participants.
     * @param type storage type of participants that should be deleted.
     * @param check <code>true</code> enables check if all given users are
     * deleted.
     * @throws TaskException if an exception occurs or less than the given users
     * are deleted and the check is enabled.
     */
    final void deleteInternal(final Context ctx, final Connection con,
        final int taskId, final Set<InternalParticipant> parts,
        final StorageType type, final boolean check) throws TaskException {
        final int[] partIds = new int[parts.size()];
        final Iterator<InternalParticipant> iter = parts.iterator();
        for (int i = 0; i < partIds.length; i++) {
            partIds[i] = iter.next().getIdentifier();
        }
        deleteInternal(ctx, con, taskId, partIds, type, check);
    }

    /**
     * Finds all tasks in a context with the group as participant.
     * @param ctx Context.
     * @param con readable database connection.
     * @param groupId unique identifier of the group.
     * @param type storage type of participant that should be searched.
     * @return a list of task identifier.
     * @throws TaskException if an error occurs.
     */
    abstract int[] findTasksWithGroup(Context ctx, Connection con,
        int groupId, StorageType type) throws TaskException;

    /**
     * Finds all tasks with the user as participant.
     * @param ctx Context.
     * @param con readable database connection.
     * @param userId unique identifier of the user.
     * @param type storage type of participant that should be searched.
     * @return an int array with all tasks identifier found.
     * @throws TaskException if an exception occurs.
     */
    abstract int[] findTasksWithParticipant(Context ctx, Connection con,
        int userId, StorageType type) throws TaskException;

    /**
     * Reads the internal participants of some tasks.
     * @param ctx Context.
     * @param con readable database connection.
     * @param tasks unique identifier of tasks.
     * @param type type of participant that should be selected.
     * @return a map with the task identifier as key and a set of internal
     * participants as value.
     * @throws TaskException if an error occurs.
     */
    protected abstract Map<Integer, Set<ExternalParticipant>>
        selectExternal(Context ctx, Connection con, int[] tasks,
            StorageType type) throws TaskException;

    /**
     * Reads the external participants of a task.
     * @param ctx Context.
     * @param con readable database connection.
     * @param taskId unique identifier of the task.
     * @param type type of participants that should be selected.
     * @return a set of participants.
     * @throws TaskException if an exception occurs.
     */
    final Set<ExternalParticipant> selectExternal(final Context ctx,
        final Connection con, final int taskId, final StorageType type)
        throws TaskException {
        Set<ExternalParticipant> parts = selectExternal(ctx, con,
            new int[] { taskId }, type).get(taskId);
        if (null == parts) {
            parts = Collections.emptySet();
        }
        return parts;
    }

    /**
     * Reads the external participants of a task.
     * @param ctx Context.
     * @param taskId unique identifier of the task.
     * @param type type of participants that should be selected.
     * @return a set of participants.
     * @throws TaskException if an exception occurs.
     */
    final Set<ExternalParticipant> selectExternal(final Context ctx,
        final int taskId, final StorageType type) throws TaskException {
        final Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            return selectExternal(ctx, con, taskId, type);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * Deletes external participants of a task.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task.
     * @param addresses email addresses of external participants.
     * @param type storage type of participants that should be deleted.
     * @param check <code>true</code> enables check if all given addresses are
     * deleted.
     * @throws TaskException if an exception occurs or less than the given
     * addresses are deleted and the check is enabled.
     */
    abstract void deleteExternal(Context ctx, Connection con, int taskId,
        String[] addresses, StorageType type, boolean check)
        throws TaskException;

    /**
     * Deletes participants.
     * @param ctx Context.
     * @param con writable connection.
     * @param taskId unique identifier of the task.
     * @param participants participants to delete.
     * @param type type of participants to delete.
     * @param sanityCheck if <code>true</code> it will be checked if all given
     * participants have been deleted.
     * @throws SQLException if a SQL error occurs.
     */
    void deleteParticipants(final Context ctx, final Connection con,
        final int taskId, final Set<TaskParticipant> participants,
        final StorageType type, final boolean sanityCheck)
        throws TaskException {
        if (null == participants || participants.size() == 0) {
            return;
        }
        deleteInternal(ctx, con, taskId, ParticipantStorage.extractInternal(participants),
            type, sanityCheck);
        deleteExternal(ctx, con, taskId, ParticipantStorage.extractExternal(participants),
            type, sanityCheck);
    }

    /**
     * Deletes external participants of a task.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task.
     * @param parts set of external participants.
     * @param type storage type of participants that should be deleted.
     * @param check <code>true</code> enables check if all given addresses are
     * deleted.
     * @throws TaskException if an exception occurs or less than the given
     * addresses are deleted and the check is enabled.
     */
    final void deleteExternal(final Context ctx, final Connection con,
        final int taskId, final Set<ExternalParticipant> parts,
        final StorageType type, final boolean check) throws TaskException {
        final String[] addresses = new String[parts.size()];
        final Iterator<ExternalParticipant> iter = parts.iterator();
        for (int i = 0; i < addresses.length; i++) {
            addresses[i] = iter.next().getMail();
        }
        deleteExternal(ctx, con, taskId, addresses, type, check);
    }

    /**
     * Reads the participants of a task.
     * @param ctx Context.
     * @param taskId unique identifier of the task.
     * @param type type of participant that should be selected.
     * @return a set of participants.
     * @throws TaskException if an exception occurs.
     */
    final Set<TaskParticipant> selectParticipants(final Context ctx,
        final int taskId, final StorageType type) throws TaskException {
        final Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        final Set<TaskParticipant> retval = new HashSet<TaskParticipant>();
        try {
            retval.addAll(selectInternal(ctx, con, taskId, type));
            retval.addAll(selectExternal(ctx, con, taskId, type));
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
        return retval;
    }

    /**
     * Reads the participants of several task.
     * @param ctx Context.
     * @param tasks unique identifier of the tasks.
     * @param type type of participant that should be selected.
     * @return a set of participants.
     * @throws TaskException if an exception occurs.
     */
    final Map<Integer, Set<TaskParticipant>> selectParticipants(
        final Context ctx, final int[] tasks, final StorageType type)
        throws TaskException {
        final Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        final Map<Integer, Set<InternalParticipant>> internals;
        final Map<Integer, Set<ExternalParticipant>> externals;
        try {
            internals = selectInternal(ctx, con, tasks, type);
            externals = selectExternal(ctx, con, tasks, type);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
        final Map<Integer, Set<TaskParticipant>> retval =
            new HashMap<Integer, Set<TaskParticipant>>();
        for (Entry<Integer, Set<InternalParticipant>> entry : internals
            .entrySet()) {
            Set<TaskParticipant> parts = retval.get(entry.getKey());
            if (null == parts) {
                parts = new HashSet<TaskParticipant>();
                retval.put(entry.getKey(), parts);
            }
            parts.addAll(entry.getValue());
        }
        for (Entry<Integer, Set<ExternalParticipant>> entry : externals
            .entrySet()) {
            Set<TaskParticipant> parts = retval.get(entry.getKey());
            if (null == parts) {
                parts = new HashSet<TaskParticipant>();
                retval.put(entry.getKey(), parts);
            }
            parts.addAll(entry.getValue());
        }
        return retval;
    }

    void insertParticipants(final Context ctx, final Connection con,
        final int taskId, final Set<TaskParticipant> participants,
        final StorageType type) throws TaskException {
        final Set<InternalParticipant> internals =
            ParticipantStorage.extractInternal(participants);
        final Set<ExternalParticipant> externals =
            ParticipantStorage.extractExternal(participants);
        insertInternals(ctx, con, taskId, internals, type);
        insertExternals(ctx, con, taskId, externals, type);
    }

    abstract void insertInternals(Context ctx, Connection con, int taskId,
        Set<InternalParticipant> participants, StorageType type)
        throws TaskException;

    abstract void insertExternals(Context ctx, Connection con, int taskId,
        Set<ExternalParticipant> participants, StorageType type)
        throws TaskException;

    static Set<ExternalParticipant> extractExternal(
        final Set<TaskParticipant> participants) {
        final Set<ExternalParticipant> retval =
            new HashSet<ExternalParticipant>();
        for (TaskParticipant participant : participants) {
            if (Type.EXTERNAL == participant.getType()) {
                retval.add((ExternalParticipant) participant);
            }
        }
        return retval;
    }

    static Set<InternalParticipant> extractInternal(
        final Set<TaskParticipant> participants) {
        final Set<InternalParticipant> retval =
            new HashSet<InternalParticipant>();
        for (TaskParticipant participant : participants) {
            if (Type.INTERNAL == participant.getType()) {
                retval.add((InternalParticipant) participant);
            }
        }
        return retval;
    }
}
