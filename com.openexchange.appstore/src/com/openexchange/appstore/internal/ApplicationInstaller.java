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

package com.openexchange.appstore.internal;

import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import com.openexchange.server.ServiceLookup;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.appstore.AppException;
import com.openexchange.appstore.Application;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.sql.grammar.Command;
import com.openexchange.sql.grammar.DELETE;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.sql.grammar.UPDATE;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ApplicationInstaller}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ApplicationInstaller {

    public static final String TABLE = "userApplications";

    private final User user;

    private final Context context;

    private final ServiceLookup serviceLookup;

    public ApplicationInstaller(Context context, User user, ServiceLookup serviceLookup) {
        this.context = context;
        this.user = user;
        this.serviceLookup = serviceLookup;
    }

    public void dropUserApplications() throws OXException {
        final DatabaseService databaseService = serviceLookup.getService(DatabaseService.class);
        final DELETE delete = new DELETE().FROM(TABLE).WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("userId", PLACEHOLDER)));

        final List<Object> values = new ArrayList<Object>(2);
        values.add(Integer.valueOf(context.getContextId()));
        values.add(user.getId());

        final Connection con = databaseService.getWritable(context);
        final StatementBuilder sb = new StatementBuilder();
        try {
            sb.executeStatement(con, delete, values);
        } catch (final SQLException e) {
            throw AppException.sqlException(e);
        } finally {
            databaseService.backWritable(context, con);
        }
    }

    public boolean install(final Application application) throws OXException {
        final Application.Status status = getStatus(application.getName());

        final Command command;
        if (status == Application.Status.none) {
            command =
                new INSERT().INTO(TABLE).SET("status", PLACEHOLDER).SET("cid", PLACEHOLDER).SET("userId", PLACEHOLDER).SET(
                    "application",
                    PLACEHOLDER);
        } else if (status == Application.Status.uninstalled) {
            command =
                new UPDATE(TABLE).SET("status", PLACEHOLDER).WHERE(
                    new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("userId", PLACEHOLDER).AND(new EQUALS("application", PLACEHOLDER))));
        } else {
            return false;
        }

        final DatabaseService databaseService = serviceLookup.getService(DatabaseService.class);
        final List<Object> values = new ArrayList<Object>(4);
        values.add(Application.Status.installed.getKeyword());
        values.add(Integer.valueOf(context.getContextId()));
        values.add(user.getId());
        values.add(application.getName());

        final Connection con = databaseService.getWritable(context);
        final StatementBuilder sb = new StatementBuilder();
        try {
            final int count = sb.executeStatement(con, command, values);
            return (count >= 1);
        } catch (final SQLException e) {
            throw AppException.sqlException(e);
        } finally {
            databaseService.backWritable(context, con);
        }
    }

    public boolean uninstall(final Application application) throws OXException {
        final DatabaseService databaseService = serviceLookup.getService(DatabaseService.class);
        final UPDATE update =
            new UPDATE(TABLE).SET("status", PLACEHOLDER).WHERE(
                new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("userId", PLACEHOLDER).AND(new EQUALS("application", PLACEHOLDER))));

        final List<Object> values = new ArrayList<Object>(4);
        values.add(Application.Status.uninstalled.getKeyword());
        values.add(Integer.valueOf(context.getContextId()));
        values.add(user.getId());
        values.add(application.getName());

        final Connection con = databaseService.getWritable(context);
        final StatementBuilder sb = new StatementBuilder();
        try {
            final int count = sb.executeStatement(con, update, values);
            return count > 0;
        } catch (final SQLException e) {
            throw AppException.sqlException(e);
        } finally {
            databaseService.backWritable(context, con);
        }
    }

    public List<Application> list() throws OXException {
        final List<Application> retval = new ArrayList<Application>();

        final DatabaseService databaseService = serviceLookup.getService(DatabaseService.class);
        final SELECT select =
            new SELECT("application", "status").FROM(TABLE).WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("userId", PLACEHOLDER)));

        final List<Object> values = new ArrayList<Object>(2);
        values.add(Integer.valueOf(context.getContextId()));
        values.add(user.getId());

        final Connection con = databaseService.getWritable(context);
        final StatementBuilder sb = new StatementBuilder();
        try {
            final ResultSet rs = sb.executeQuery(con, select, values);
            while (rs.next()) {
                final String app = rs.getString("application");
                final String s = rs.getString("status");
                Application.Status status = Application.Status.none;
                if (s.equalsIgnoreCase(Application.Status.installed.getKeyword())) {
                    status = Application.Status.installed;
                } else if (s.equalsIgnoreCase(Application.Status.uninstalled.getKeyword())) {
                    status = Application.Status.uninstalled;
                }

                final Application application = new Application();
                application.setName(app);
                application.setStatus(status);
                retval.add(application);
            }
            sb.closePreparedStatement(null, rs);
        } catch (final SQLException e) {
            throw AppException.sqlException(e);
        } finally {
            databaseService.backWritable(context, con);
        }

        return retval;
    }

    public Application.Status getStatus(final String name) throws OXException {
        final DatabaseService databaseService = serviceLookup.getService(DatabaseService.class);
        final SELECT select =
            new SELECT("status").FROM(TABLE).WHERE(
                new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("userId", PLACEHOLDER).AND(new EQUALS("application", PLACEHOLDER))));

        final List<Object> values = new ArrayList<Object>(4);
        values.add(Integer.valueOf(context.getContextId()));
        values.add(user.getId());
        values.add(name);

        final Connection con = databaseService.getReadOnly(context);
        final StatementBuilder sb = new StatementBuilder();
        try {
            final ResultSet rs = sb.executeQuery(con, select, values);
            if (rs.next()) {
                final String status = rs.getString("status");
                if (status.equalsIgnoreCase(Application.Status.installed.getKeyword())) {
                    return Application.Status.installed;
                }
                if (status.equalsIgnoreCase(Application.Status.uninstalled.getKeyword())) {
                    return Application.Status.uninstalled;
                }
            }
            sb.closePreparedStatement(null, rs);
        } catch (final SQLException e) {
            throw AppException.sqlException(e);
        } finally {
            databaseService.backReadOnly(context, con);
        }

        return Application.Status.none;
    }
}
