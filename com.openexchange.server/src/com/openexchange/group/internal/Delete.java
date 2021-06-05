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

package com.openexchange.group.internal;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupEventConstants;
import com.openexchange.group.GroupExceptionCodes;
import com.openexchange.group.GroupStorage;
import com.openexchange.group.GroupStorage.StorageType;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFinishedListenerRegistry;
import com.openexchange.groupware.delete.DeleteRegistry;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.user.User;

/**
 * This class integrates all operations to be done for deleting a group.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Delete {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Delete.class);

    /**
     * Context.
     */
    private final Context ctx;

    /**
     * User for checking permissions.
     */
    private final User user;

    /**
     * Unique identifier of the group to delete.
     */
    private final int groupId;

    private final Date lastRead;

    /**
     * cache field for the group.
     */
    private transient Group orig;

    /**
     * Default constructor.
     * @param ctx Context.
     * @param user User for permission checks.
     * @param groupId unique identifier of the group to delete.
     */
    Delete(final Context ctx, final User user, final int groupId,
        final Date lastRead) {
        super();
        this.ctx = ctx;
        this.user = user;
        this.groupId = groupId;
        this.lastRead = lastRead;
    }

    Group getOrig() throws OXException {
        if (null == orig) {
            orig = ServerServiceRegistry.getServize(GroupStorage.class, true).getGroup(groupId, ctx);
        }
        return orig;
    }

    /**
     * This method integrates all several methods for the different operations
     * of deleting a group.
     * @throws OXException if something during delete fails.
     */
    void perform() throws OXException {
        allowed();
        check();
        delete();
        propagate();
        sentEvent();
    }

    private void allowed() throws OXException {
        if (!UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx).isEditGroup()) {
            throw GroupExceptionCodes.NO_DELETE_PERMISSION.create();
        }
        if (groupId == GroupStorage.GROUP_ZERO_IDENTIFIER) {
            try {
                throw GroupExceptionCodes.NO_GROUP_DELETE.create(GroupTools.getGroupZero(ctx).getDisplayName());
            } catch (OXException e) {
                LOG.error("", e);
                throw GroupExceptionCodes.NO_GROUP_DELETE.create(I(GroupStorage.GROUP_ZERO_IDENTIFIER));
            }
        }
    }

    private void check() throws OXException {
        // Does the group exist?
        getOrig();
        // Group 1 can not be deleted
        if (GroupStorage.GROUP_ZERO_IDENTIFIER == groupId || 1 == groupId) {
            throw GroupExceptionCodes.NO_GROUP_DELETE.create(getOrig().getDisplayName());
        }
    }

    /**
     * Deletes all data for the group in the database. This includes deleting
     * everything that references the group.
     * @throws OXException
     */
    private void delete() throws OXException {
        final Connection con = DBPool.pickupWriteable(ctx);
        try {
            con.setAutoCommit(false);
            propagateDelete(con);
            delete(con);
            con.commit();
            propagateDeleteFinished();
        } catch (SQLException e) {
            Databases.rollback(con);
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (OXException e) {
            Databases.rollback(con);
            throw e;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                LOG.error("Problem setting autocommit to true.", e);
            }
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    private void propagateDelete(final Connection con) throws OXException {
        // Delete all references to that group.
        final DeleteEvent event = DeleteEvent.createDeleteEventForGroupDeletion(getOrig(), groupId, ctx);
        DeleteRegistry.getInstance().fireDeleteEvent(event, con, con);
    }

    private void propagateDeleteFinished() {
        try {
            final DeleteEvent event = DeleteEvent.createDeleteEventForGroupDeletion(getOrig(), groupId, ctx);
            DeleteFinishedListenerRegistry.getInstance().fireDeleteEvent(event);
        } catch (OXException e) {
            LOG.warn("Failed to trigger delete finished listeners", e);
        }
    }

    private void delete(final Connection con) throws OXException {
        // Delete the group.
        GroupStorage storage = ServerServiceRegistry.getServize(GroupStorage.class, true);
        storage.deleteMember(ctx, con, getOrig(), getOrig().getMember());
        storage.deleteGroup(ctx, con, groupId, lastRead);
        // Remember as deleted group.
        final Group del = new Group();
        final Group orig = getOrig();
        del.setIdentifier(orig.getIdentifier());
        del.setDisplayName(orig.getDisplayName());
        del.setSimpleName(orig.getSimpleName());
        del.setLastModified(new Date());
        storage.insertGroup(ctx, con, del, StorageType.DELETED);
    }

    /**
     * Inform the rest of the system about the deleted group.
     * @throws OXException if something during propagate fails.
     */
    private void propagate() throws OXException {
        final UserStorage storage = UserStorage.getInstance();
        storage.invalidateUser(ctx, getOrig().getMember());
    }

    private void sentEvent() {
        final EventAdmin eventAdmin = ServerServiceRegistry.getInstance().getService(EventAdmin.class);
        if (null != eventAdmin) {
            final Dictionary<String, Object> dict = new Hashtable<String, Object>(4);
            dict.put(GroupEventConstants.PROPERTY_CONTEXT_ID, Integer.valueOf(ctx.getContextId()));
            dict.put(GroupEventConstants.PROPERTY_USER_ID, Integer.valueOf(user.getId()));
            dict.put(GroupEventConstants.PROPERTY_GROUP_ID, Integer.valueOf(groupId));
            eventAdmin.postEvent(new Event(GroupEventConstants.TOPIC_DELETE, dict));
        }
    }
}
