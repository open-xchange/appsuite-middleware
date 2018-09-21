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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.storage.rdb.groupware;

import static com.openexchange.chronos.common.CalendarUtils.extractEMailAddress;
import static com.openexchange.groupware.tools.alias.UserAliasUtility.isAlias;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.AbstractUserServiceInterceptor;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link CalendarStorageInterceptor}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.1
 */
public class CalendarStorageInterceptor extends AbstractUserServiceInterceptor {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarStorageInterceptor.class);

    private final DBProvider dbProvider;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link CalendarStorageInterceptor}.
     * 
     * @param services A service lookup reference
     * @param dbProvider The database provider to use
     */
    public CalendarStorageInterceptor(ServiceLookup services, DBProvider dbProvider) {
        super();
        this.services = services;
        this.dbProvider = dbProvider;
    }

    @Override
    public void afterUpdate(Context context, User user, Contact contactData, Map<String, Object> properties) throws OXException {
        if (false == requiresAddressCheck(context, user)) {
            LOG.debug("Skipping address checks in context {} for user {}.", I(context.getContextId()), I(null == user ? -1 : user.getId()));
            return;
        }
        /*
         * get all attendee URIs referencing this user
         */
        Set<String> attendeeURIs;
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            attendeeURIs = selectAttendeeURIs(connection, context.getContextId(), user.getId());
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
        if (null == attendeeURIs || attendeeURIs.isEmpty()) {
            LOG.debug("No attendee references found in context {} for user {}, nothing to do.", I(context.getContextId()), I(user.getId()));
            return;
        }
        /*
         * check that each URI is either the user's unique resource identifier, or is covered by the user's aliases
         */
        List<String> attendeeURIsToReplace = new ArrayList<String>();
        String resourceId = ResourceId.forUser(context.getContextId(), user.getId());
        Set<String> possibleAliases = getAliases(user);
        for (String attendeeURI : attendeeURIs) {
            if (resourceId.equals(attendeeURI) || isAlias(extractEMailAddress(attendeeURI), possibleAliases)) {
                continue;
            }
            attendeeURIsToReplace.add(attendeeURI);
        }
        if (attendeeURIsToReplace.isEmpty()) {
            LOG.debug("No invalid attendee references found in context {} for user {}, nothing to do.", I(context.getContextId()), I(user.getId()));
            return;
        }
        /*
         * replace no longer valid attendee URIs with the user's default calendar address
         */
        String replacementUri = CalendarUtils.getURI(user.getMail());
        int updated = replaceCalendarUserAddresses(dbProvider, context, user.getId(), attendeeURIsToReplace, replacementUri);
        LOG.info("Successfully replaced {} references to no longer valid calendar user addresses {} with \"{}\" in context {} for user {}.", 
            I(updated), Arrays.toString(attendeeURIsToReplace.toArray()), replacementUri, I(context.getContextId()), I(user.getId()));
    }

    private boolean requiresAddressCheck(Context context, User user) throws OXException {
        if (null == user) {
            return false;
        }
        UserPermissionService userPermissionService = services.getOptionalService(UserPermissionService.class);
        if (null != userPermissionService) {
            UserPermissionBits permissionBits = userPermissionService.getUserPermissionBits(user.getId(), context);
            if (false == permissionBits.hasCalendar()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Replaces the URI property of a specific attendee.
     * 
     * @param dbProvider The database provider to use
     * @param context The context
     * @param userId The user identifier
     * @param urisToReplace The URI values to replace
     * @param replacementUri The replacement URI
     * @return The number of updated rows
     */
    private static int replaceCalendarUserAddresses(DBProvider dbProvider, Context context, int userId, Collection<String> urisToReplace, String replacementUri) throws OXException {
        Connection connection = null;
        boolean committed = false;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            connection.setAutoCommit(false);
            for (String uri : urisToReplace) {
                updated += replaceCalendarUserAddresses(connection, context.getContextId(), userId, uri, replacementUri);
            }
            connection.commit();
            committed = true;
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            if (false == committed) {
                Databases.rollback(connection);
                Databases.autocommit(connection);
                dbProvider.releaseWriteConnectionAfterReading(context, connection);
            } else {
                Databases.autocommit(connection);
                if (0 < updated) {
                    dbProvider.releaseWriteConnection(context, connection);
                } else {
                    dbProvider.releaseWriteConnectionAfterReading(context, connection);
                }
            }
        }
        return updated;
    }

    /**
     * Queries all calendar user addresses as URI string that are actually used for the attendee records of a specific entity.
     * 
     * @param connection The database connection to use
     * @param contextId The context identifier
     * @param entity The entity identifier
     * @return The calendar user addresses, or an empty set if there are none
     */
    private static Set<String> selectAttendeeURIs(Connection connection, int contextId, int entity) throws OXException {
        Set<String> uris = new HashSet<String>();
        String sql = "SELECT DISTINCT (uri) FROM calendar_attendee WHERE cid=? AND account=0 AND entity=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextId);
            stmt.setInt(parameterIndex++, entity);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    uris.add(resultSet.getString(1));
                }
            }
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
        return uris;
    }

    /**
     * Replaces the URI property of a specific attendee.
     * 
     * @param connection The connection to use
     * @param cid The context identifier
     * @param entity The user identifier
     * @param uri The URI value to replace
     * @param replacementUri The replacement URI
     * @return The number of updated rows
     */
    private static int replaceCalendarUserAddresses(Connection connection, int cid, int entity, String uri, String replacementUri) throws OXException {
        String sql = "UPDATE calendar_attendee SET uri=? WHERE cid=? AND account=0 AND entity=? AND uri=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setString(parameterIndex++, replacementUri);
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, entity);
            stmt.setString(parameterIndex++, uri);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    private static Set<String> getAliases(User user) {
        Set<String> possibleAliases = new HashSet<String>();
        possibleAliases.add(user.getMail());
        if (null != user.getAliases()) {
            possibleAliases.addAll(Arrays.asList(user.getAliases()));
        }
        return possibleAliases;
    }

}
