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

package com.openexchange.group.internal;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.group.Group;
import com.openexchange.group.GroupException;
import com.openexchange.group.GroupStorage;
import com.openexchange.group.GroupException.Code;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.tools.oxfolder.OXFolderAdminHelper;
import com.openexchange.tools.sql.DBUtils;

/**
 * This class integrates all operations to be done if a group is updated.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
final class Update {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(Update.class);

    /**
     * Context.
     */
    private final Context ctx;
    
    /**
     * User.
     */
    private final User user;
    
    /**
     * Group object with changed information.
     */
    private final Group changed;

    private final Date lastRead;

    /**
     * Storage API for groups.
     */
    private static final GroupStorage storage = GroupStorage.getInstance();

    private Group orig;

    /**
     * Added members.
     */
    private final Set<Integer> addedMembers = new HashSet<Integer>();

    /**
     * Removed members.
     */
    private final Set<Integer> removedMembers = new HashSet<Integer>();

    /**
     * Default constructor.
     */
    Update(final Context ctx, final User user, final Group group,
        final Date lastRead) {
        super();
        this.ctx = ctx;
        this.user = user;
        this.changed = group;
        this.lastRead = lastRead;
    }

    Group getOrig() throws GroupException {
        if (null == orig) {
            try {
                orig = storage.getGroup(changed.getIdentifier(), ctx);
            } catch (final LdapException e) {
                throw new GroupException(e);
            }
        }
        return orig;
    }

    void perform() throws GroupException {
        allowed();
        check();
        prepare();
        update();
        propagate();
    }

    private void allowed() throws GroupException {
        try {
            if (!UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx).isEditGroup()) {
                throw new GroupException(Code.NO_MODIFY_PERMISSION);
            }
            if (changed.getIdentifier() == GroupTools.GROUP_ZERO.getIdentifier()) {
                try {
                    throw new GroupException(Code.NO_GROUP_UPDATE, GroupTools.getGroupZero(ctx).getDisplayName());
                } catch (final UserException e) {
                    LOG.error(e.getMessage(), e);
                    throw new GroupException(Code.NO_GROUP_UPDATE, I(GroupStorage.GROUP_ZERO_IDENTIFIER));
                } catch (final LdapException e) {
                    LOG.error(e.getMessage(), e);
                    throw new GroupException(Code.NO_GROUP_UPDATE, I(GroupStorage.GROUP_ZERO_IDENTIFIER));
                }
            }
        } catch (final UserConfigurationException e) {
            throw new GroupException(e);
        }
    }

    private void check() throws GroupException {
        if (null == changed) {
            throw new GroupException(Code.NULL);
        }
        if (GroupStorage.GROUP_ZERO_IDENTIFIER == changed.getIdentifier()) {
            throw new GroupException(Code.NO_GROUP_UPDATE, getOrig().getDisplayName());
        }
        // Does the group exist? Are timestamps okay?
        if (getOrig().getLastModified().after(lastRead)) {
            throw new GroupException(Code.MODIFIED);
        }
        Logic.checkMandatoryForUpdate(changed);
        Logic.validateSimpleName(changed);
        Logic.checkData(changed);
        Logic.checkForDuplicate(storage, ctx, changed);
        Logic.doMembersExist(ctx, changed);
    }

    private void prepare() throws GroupException {
        prepareFields();
        prepareMember();
    }

    private void prepareFields() throws GroupException {
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

    private void prepareMember() throws GroupException {
        if (memberPrepared) {
            return;
        }
        if (changed.isMemberSet()) {
            for (final int member : changed.getMember()) {
                addedMembers.add(Integer.valueOf(member));
            }
            for (final int member : getOrig().getMember()) {
                addedMembers.remove(Integer.valueOf(member));
                removedMembers.add(Integer.valueOf(member));
            }
            for (final int member : changed.getMember()) {
                removedMembers.remove(Integer.valueOf(member));
            }
        } else {
            changed.setMember(getOrig().getMember());
        }
        memberPrepared = true;
    }

    /**
     * Updates all data for the group in the database.
     * @throws GroupException
     */
    private void update() throws GroupException {
        final Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (final DBPoolingException e) {
            throw new GroupException(Code.NO_CONNECTION, e);
        }
        try {
            con.setAutoCommit(false);
            update(con);
            con.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw new GroupException(Code.SQL_ERROR, e, e.getMessage());
        } catch (final GroupException e) {
            DBUtils.rollback(con);
            throw e;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (final SQLException e) {
                LOG.error("Problem setting autocommit to true.", e);
            }
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    /**
     * This method calls the plain update methods.
     * @param con writable database connection in transaction or not.
     * @throws GroupException if some problem occurs.
     */
    public void update(final Connection con) throws GroupException {
        storage.updateGroup(ctx, con, changed, lastRead);
        int[] tmp = new int[addedMembers.size()];
        Iterator<Integer> iter = addedMembers.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            tmp[i] = iter.next().intValue();
        }
        storage.insertMember(ctx, con, changed, tmp);
        tmp = new int[removedMembers.size()];
        iter = removedMembers.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            tmp[i] = iter.next().intValue();
        }
        storage.deleteMember(ctx, con, changed, tmp);
    }

    /**
     * Inform the rest of the system about the changed group.
     * @throws GroupException if something during propagate fails.
     */
    private void propagate() throws GroupException {
        final int[] tmp = new int[addedMembers.size() + removedMembers.size()];
        Iterator<Integer> iter = addedMembers.iterator();
        int i = 0;
        while (iter.hasNext()) {
            tmp[i++] = iter.next().intValue();
        }
        iter = removedMembers.iterator();
        while (iter.hasNext()) {
            tmp[i++] = iter.next().intValue();
        }
        GroupTools.invalidateUser(ctx, tmp);
        // The time stamp of folder must be increased. The GUI the reloads the
        // folder. This must be done because through this change some folders
        // may get visible or invisible.
        final Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (final DBPoolingException e) {
            throw new GroupException(Code.NO_CONNECTION, e);
        }
        try {
            con.setAutoCommit(false);
            propagate(con);
            con.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw new GroupException(Code.SQL_ERROR, e, e.getMessage());
        } catch (final GroupException e) {
            DBUtils.rollback(con);
            throw e;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (final SQLException e) {
                LOG.error("Problem setting autocommit to true.", e);
            }
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    private void propagate(final Connection con) throws GroupException {
        try {
            OXFolderAdminHelper.propagateGroupModification(changed.getIdentifier(), con, con, ctx.getContextId());
        } catch (final SQLException e) {
            throw new GroupException(Code.SQL_ERROR, e, e.getMessage());
        }
    }
}
