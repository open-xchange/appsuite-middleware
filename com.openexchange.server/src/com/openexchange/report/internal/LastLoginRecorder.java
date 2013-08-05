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

package com.openexchange.report.internal;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.groupware.update.FullPrimaryKeySupportService;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.user.UserService;

/**
 * {@link LastLoginRecorder} records the last login of a user in its user attributes.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class LastLoginRecorder implements LoginHandlerService {

    private static volatile Integer maxClientCount;
    private static int maxClientCount() {
        Integer tmp = maxClientCount;
        if (null == tmp) {
            synchronized (LastLoginRecorder.class) {
                tmp = maxClientCount;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    tmp = Integer.valueOf(null == service ? -1 : service.getIntProperty("com.openexchange.user.maxClientCount", -1));
                    maxClientCount = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    /**
     * Initializes a new {@link LastLoginRecorder}.
     */
    public LastLoginRecorder() {
        super();
    }

    @Override
    public void handleLogin(final LoginResult login) throws OXException {
        final LoginRequest request = login.getRequest();
        // Determine client
        String client;
        if (null != request.getClient()) {
            client = request.getClient();
        } else if (null != request.getInterface()) {
            client = request.getInterface().toString();
        } else {
            return;
        }
        updateLastLogin(client, login.getUser(), login.getContext());
    }

    /**
     * Updates the last-accessed time stamp for given user's client.
     *
     * @param client The client identifier
     * @param origUser The associated user
     * @param context The context
     * @throws OXException If update fails for any reason
     */
    static void updateLastLogin(final String client, final User origUser, final Context context) throws OXException {
        // Set attribute
        final String key = "client:" + client;
        if (context.isReadOnly()) {
            return;
        }
        final int userId = origUser.getId();
        // Retrieve existing ones
        {
            final int maxClientCount = maxClientCount();
            if (maxClientCount > 0) {
                final Map<String, Set<String>> origAttributes = origUser.getAttributes();
                int count = 0;
                for (final String origKey : origAttributes.keySet()) {
                    if (origKey.startsWith("client:") && ++count > maxClientCount) {
                        throw UserExceptionCode.UPDATE_ATTRIBUTES_FAILED.create(Integer.valueOf(context.getContextId()), Integer.valueOf(userId));
                    }
                }
            }
        }

        // Add current time stamp
        updateTimeStamp(key, System.currentTimeMillis(), userId, context.getContextId());

        {
            UserService service = ServerServiceRegistry.getInstance().getService(UserService.class, true);
            service.invalidateUser(context, userId);
        }
    }

    private static void updateTimeStamp(final String key, final long stamp, final int userId, final int contextId) throws OXException {
        final DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class, true);
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            UUID uuid = null;

            {
                ResultSet rs = null;
                try {
                    stmt = con.prepareStatement("SELECT uuid FROM user_attribute WHERE cid=? AND id=? AND name=?");
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, userId);
                    stmt.setString(3, key);
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        uuid = UUIDs.toUUID(rs.getBytes(1));
                    }
                } finally {
                    Databases.closeSQLStuff(rs, stmt);
                    stmt = null;
                }
            }

            // INSERT or UPDATE
            if (null == uuid) {
                stmt = con.prepareStatement("INSERT INTO user_attribute (cid,id,name,value,uuid) VALUES (?,?,?,?,?)");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, key);
                stmt.setString(4, Long.toString(stamp));
                stmt.setBytes(5, UUIDs.toByteArray(UUID.randomUUID()));
                stmt.executeUpdate();
            } else {
                final FullPrimaryKeySupportService fullPrimaryKeySupportService = FullPrimaryKeySupportService.SERVICE_REFERENCE.get();
                if (null != fullPrimaryKeySupportService && fullPrimaryKeySupportService.isFullPrimaryKeySupported()) {
                    stmt = con.prepareStatement("UPDATE user_attribute SET value=? WHERE cid=? AND uuid=?");
                    stmt.setString(1, Long.toString(stamp));
                    stmt.setInt(2, contextId);
                    stmt.setBytes(3, UUIDs.toByteArray(uuid));
                    stmt.executeUpdate();
                } else {
                    stmt = con.prepareStatement("UPDATE user_attribute SET value=? WHERE cid=? AND id=? AND name=?");
                    stmt.setString(1, Long.toString(stamp));
                    stmt.setInt(2, contextId);
                    stmt.setInt(3, userId);
                    stmt.setString(4, key);
                    stmt.executeUpdate();
                }
            }

        } catch (final SQLException e) {
            throw UserExceptionCode.UPDATE_ATTRIBUTES_FAILED.create(e, I(contextId), I(userId));
        } finally {
            Databases.closeSQLStuff(stmt);
            databaseService.backWritable(contextId, con);
        }
    }

    @Override
    public void handleLogout(final LoginResult logout) {
        // Nothing to to.
    }
}
