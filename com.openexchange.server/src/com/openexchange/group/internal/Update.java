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
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.i18n.LocalizableArgument;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.oxfolder.OXFolderAdminHelper;
import com.openexchange.user.User;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * This class integrates all operations to be done if a group is updated.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
final class Update {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Update.class);

    private final Context ctx;
    private final User user;
    private final Group changed;
    private final boolean checkI18nNames;
    private final Date lastRead;
    private Group orig;
    private final TIntSet addedMembers = new TIntHashSet();
    private final TIntSet removedMembers = new TIntHashSet();

    /**
     * Default constructor.
     */
    Update(Context ctx, User user, Group group, Date lastRead, boolean checkI18nNames) {
        super();
        this.ctx = ctx;
        this.user = user;
        this.changed = group;
        this.lastRead = lastRead;
        this.checkI18nNames = checkI18nNames;
    }

    Group getOrig() throws OXException {
        if (null == orig) {
            orig = ServerServiceRegistry.getServize(GroupStorage.class, true).getGroup(changed.getIdentifier(), ctx);
        }
        return orig;
    }

    void perform() throws OXException {
        allowed();
        check();
        prepare();
        update();
        propagate();
        sentEvent();
    }

    private void allowed() throws OXException {
        if (!UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx).isEditGroup()) {
            throw GroupExceptionCodes.NO_MODIFY_PERMISSION.create();
        }
        if (changed.getIdentifier() == GroupStorage.GROUP_ZERO_IDENTIFIER) {
            try {
                throw GroupExceptionCodes.NO_GROUP_UPDATE.create(GroupTools.getGroupZero(ctx).getDisplayName());
            } catch (OXException e) {
                LOG.error("", e);
                throw GroupExceptionCodes.NO_GROUP_UPDATE.create(I(GroupStorage.GROUP_ZERO_IDENTIFIER));
            }
        }
    }

    private void check() throws OXException {
        if (null == changed) {
            throw GroupExceptionCodes.NULL.create();
        }
        if (GroupStorage.GROUP_ZERO_IDENTIFIER == changed.getIdentifier()) {
            throw GroupExceptionCodes.NO_GROUP_UPDATE.create(new LocalizableArgument(getOrig().getDisplayName()));
        }
        // Does the group exist? Are timestamps okay?
        if (getOrig().getLastModified().after(lastRead)) {
            throw GroupExceptionCodes.MODIFIED.create();
        }
        if (GroupStorage.GROUP_STANDARD_SIMPLE_NAME.equals(getOrig().getSimpleName())) {
            if (changed.isSimpleNameSet() && !changed.getSimpleName().equals(getOrig().getSimpleName())) {
                throw GroupExceptionCodes.NO_GROUP_UPDATE.create(new LocalizableArgument(getOrig().getDisplayName()));
            }
            if (changed.isDisplayNameSet() && !changed.getDisplayName().equals(getOrig().getDisplayName())) {
                throw GroupExceptionCodes.NO_GROUP_UPDATE.create(new LocalizableArgument(getOrig().getDisplayName()));
            }
        }
        Logic.checkMandatoryForUpdate(changed);
        Logic.validateSimpleName(changed);
        Logic.checkData(changed);
        Logic.checkForDuplicate(ServerServiceRegistry.getServize(GroupStorage.class, true), ctx, changed, checkI18nNames);
        Logic.doMembersExist(ctx, changed);
    }

    private void prepare() throws OXException {
        prepareFields();
        prepareMember();
    }

    private void prepareFields() throws OXException {
        if (!changed.isDisplayNameSet()) {
            changed.setDisplayName(getOrig().getDisplayName());
        }
        if (!changed.isSimpleNameSet()) {
            changed.setSimpleName(getOrig().getSimpleName());
        }
    }

    /**
     * Remember if the method prepareMember has already been executed.
     */
    private boolean memberPrepared = false;

    private void prepareMember() throws OXException {
        if (memberPrepared) {
            return;
        }
        if (changed.isMemberSet()) {
            for (final int member : changed.getMember()) {
                addedMembers.add(member);
            }
            for (final int member : getOrig().getMember()) {
                addedMembers.remove(member);
                removedMembers.add(member);
            }
            for (final int member : changed.getMember()) {
                removedMembers.remove(member);
            }
        } else {
            changed.setMember(getOrig().getMember());
        }
        memberPrepared = true;
    }

    /**
     * Updates all data for the group in the database.
     * @throws OXException
     */
    private void update() throws OXException {
        final Connection con = DBPool.pickupWriteable(ctx);
        try {
            con.setAutoCommit(false);
            update(con);
            con.commit();
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

    /**
     * This method calls the plain update methods.
     * @param con writable database connection in transaction or not.
     * @throws OXException if some problem occurs.
     */
    public void update(final Connection con) throws OXException {
        GroupStorage groupStorage = ServerServiceRegistry.getServize(GroupStorage.class, true);
        groupStorage.updateGroup(ctx, con, changed, lastRead);
        int[] tmp = new int[addedMembers.size()];
        TIntIterator iter = addedMembers.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            tmp[i] = iter.next();
        }
        groupStorage.insertMember(ctx, con, changed, tmp);
        tmp = new int[removedMembers.size()];
        iter = removedMembers.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            tmp[i] = iter.next();
        }
        groupStorage.deleteMember(ctx, con, changed, tmp);
    }

    /**
     * Inform the rest of the system about the changed group.
     * @throws OXException if something during propagate fails.
     */
    private void propagate() throws OXException {
        final int[] tmp = new int[addedMembers.size() + removedMembers.size()];
        TIntIterator iter = addedMembers.iterator();
        int i = 0;
        while (iter.hasNext()) {
            tmp[i++] = iter.next();
        }
        iter = removedMembers.iterator();
        while (iter.hasNext()) {
            tmp[i++] = iter.next();
        }
        final UserStorage storage = UserStorage.getInstance();
        storage.invalidateUser(ctx, tmp);
        // The time stamp of folder must be increased. The GUI the reloads the
        // folder. This must be done because through this change some folders
        // may get visible or invisible.
        final Connection con = DBPool.pickupWriteable(ctx);
        boolean writeConnectionUsed = false;
        try {
            con.setAutoCommit(false);
            writeConnectionUsed = propagate(con);
            con.commit();
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
            if (writeConnectionUsed){
                DBPool.closeWriterSilent(ctx, con);
            } else {
                DBPool.closeWriterAfterReading(ctx, con);
            }
        }
    }

    private boolean propagate(final Connection con) throws OXException {
        try {
            return OXFolderAdminHelper.propagateGroupModification(changed.getIdentifier(), con, con, ctx.getContextId());
        } catch (SQLException e) {
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private void sentEvent() {
        final EventAdmin eventAdmin = ServerServiceRegistry.getInstance().getService(EventAdmin.class);
        if (null != eventAdmin) {
            final Dictionary<String, Object> dict = new Hashtable<String, Object>(4);
            dict.put(GroupEventConstants.PROPERTY_CONTEXT_ID, Integer.valueOf(ctx.getContextId()));
            dict.put(GroupEventConstants.PROPERTY_USER_ID, Integer.valueOf(user.getId()));
            dict.put(GroupEventConstants.PROPERTY_GROUP_ID, Integer.valueOf(changed.getIdentifier()));
            eventAdmin.postEvent(new Event(GroupEventConstants.TOPIC_UPDATE, dict));
        }
    }
}
