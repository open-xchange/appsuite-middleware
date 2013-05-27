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

package com.openexchange.group.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.log.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupEventConstants;
import com.openexchange.group.GroupExceptionCodes;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;

/**
 * This class contains the glue code to join all several operations to be done
 * if a group is created.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Create {

    /**
     * Logger.
     */
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Create.class));

    /**
     * Context.
     */
    private final Context ctx;

    /**
     * User for permission checks.
     */
    private final User user;

    /**
     * Group to insert.
     */
    private final Group group;

    /**
     * Storage API for groups.
     */
    private static final GroupStorage storage = GroupStorage.getInstance();

    /**
     * Default constructor.
     * @param user
     */
    public Create(final Context ctx, final User user, final Group group) {
        super();
        this.ctx = ctx;
        this.user = user;
        this.group = group;
    }

    /**
     * This method glues all operations together. That includes all checks and
     * the operations to be done.
     * @throws OXException
     */
    void perform() throws OXException {
        allowed();
        check();
        insert();
        propagate();
        sentEvent();
    }

    /**
     * Check if the user is allowed to create groups.
     * @throws OXException if the user is not allowed to create groups.
     */
    private void allowed() throws OXException {
        if (!UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx).isEditGroup()) {
            throw GroupExceptionCodes.NO_CREATE_PERMISSION.create();
        }
    }

    /**
     * This method performs all necessary checks before creating a group.
     * @throws OXException if a problem was detected during checks.
     */
    private void check() throws OXException {
        if (null == group) {
            throw GroupExceptionCodes.NULL.create();
        }
        Logic.checkMandatoryForCreate(group);
        Logic.validateSimpleName(group);
        Logic.checkData(group);
        Logic.checkForDuplicate(storage, ctx, group);
        Logic.doMembersExist(ctx, group);
    }

    /**
     * Inserts all data for the group into the database.
     * @throws OXException
     */
    private void insert() throws OXException {
        final Connection con = DBPool.pickupWriteable(ctx);
        try {
            con.setAutoCommit(false);
            insert(con);
            con.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
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
     * This method calls the plain insert methods.
     * @param con writable database connection in transaction or not.
     * @throws OXException if some problem occurs.
     */
    public void insert(final Connection con) throws OXException {
        try {
            final int identifier = IDGenerator.getId(ctx.getContextId(),
                Types.PRINCIPAL, con);
            group.setIdentifier(identifier);
            storage.insertGroup(ctx, con, group);
            storage.insertMember(ctx, con, group, group.getMember());
        } catch (final SQLException e) {
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Inform the rest of the system about the new group.
     * @throws OXException
     */
    private void propagate() throws OXException {
        final UserStorage storage = UserStorage.getInstance();
        storage.invalidateUser(ctx, group.getMember());
    }

    /**
     * Sent event about created group.
     */
    private void sentEvent() {
        final EventAdmin eventAdmin = ServerServiceRegistry.getInstance().getService(EventAdmin.class);
        if (null != eventAdmin) {
            final Dictionary<String, Object> dict = new Hashtable<String, Object>(4);
            dict.put(GroupEventConstants.PROPERTY_CONTEXT_ID, Integer.valueOf(ctx.getContextId()));
            dict.put(GroupEventConstants.PROPERTY_USER_ID, Integer.valueOf(user.getId()));
            dict.put(GroupEventConstants.PROPERTY_GROUP_ID, Integer.valueOf(group.getIdentifier()));
            eventAdmin.postEvent(new Event(GroupEventConstants.TOPIC_CREATE, dict));
        }
    }
}
