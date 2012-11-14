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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.management.MBeanException;
import org.apache.commons.logging.Log;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link LoginCounter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LoginCounter implements LoginCounterMBean {

    private final Log logger;

    private String wildcard;

    /**
     * Initializes a new {@link LoginCounter}.
     */
    public LoginCounter() {
        super();
        logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(ReportingMBean.class));
    }

    @Override
    public Date getLastLoginTimeStamp(final int userId, final int contextId, final String client) throws MBeanException {
        final DatabaseService service = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (null == service) {
            throw new MBeanException(null, "DatabaseService not available at the moment. Try again later.");
        }
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = service.getReadOnly(contextId);
            stmt = con.prepareStatement("SELECT value FROM user_attribute WHERE cid=? AND id=? AND name=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, "client:" + client);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new MBeanException(null, "No such entry found (user="+userId+", context="+contextId+", client=\""+client+"\").");
            }
            return new Date(Long.parseLong(rs.getString(1)));
        } catch (final MBeanException e) {
            throw e;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw new MBeanException(null, "Retrieving last login time stamp failed: " + e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            if (null != con) {
                service.backReadOnly(contextId, con);
            }
        }
    }

    @Override
    public int getNumberOfLogins(final Date startDate, final Date endDate) throws MBeanException {
        /*
         * Compile pattern
         */
        final Pattern pattern;
        if (wildcard != null) {
            try {
                pattern = Pattern.compile(wildcardToRegex("client:" + wildcard));
            } catch (final PatternSyntaxException e) {
                logger.error(e.getMessage(), e);
                throw new MBeanException(e, "Couldn't compile regex pattern.");
            }
        } else {
            pattern = null;
        }
        /*
         * Counter
         */
        int counter = 0;
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        Map<String, Integer> schemaMap = null;
        try {
            schemaMap = Tools.getAllSchemata(logger);
        } catch (OXException e) {
            logger.error(e.getMessage(), e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe,e.getMessage());
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe,e.getMessage());
        }

        /*
         * Get all logins in every schema
         */
        for (final String schema : schemaMap.keySet()) {
            final int readPool = schemaMap.get(schema).intValue();
            final Connection connection;
            try {
                connection = dbService.get(readPool, schema);
            } catch (final OXException e) {
                logger.error(e.getMessage(), e);
                throw new MBeanException(e, "Couldn't get connection to schema " + schema + " in pool " + readPool + ".");
            }
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = connection.prepareStatement("SELECT name, value FROM user_attribute WHERE name LIKE ?");
                stmt.setString(1, StringCollection.prepareForSearch("client:*", false, true));
                rs = stmt.executeQuery();
                final Date lastLogin = new Date();
                while (rs.next()) {
                    final String name = rs.getString(1);
                    if (pattern == null || (pattern.matcher(name).matches())) {
                        try {
                            lastLogin.setTime(Long.parseLong(rs.getString(2)));
                            if (lastLogin.after(startDate) && lastLogin.before(endDate)) {
                                counter++;
                            }
                        } catch (final NumberFormatException e) {
                            logger.warn("Client value is not a number.", e);
                        }
                    }
                }
            } catch (final SQLException e) {
                logger.error(e.getMessage(), e);
                throw new MBeanException(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
                dbService.back(readPool, connection);
            }
        }
        return counter;
    }

    @Override
    public void setDeviceWildcard(final String wildcard) {
        this.wildcard = wildcard;
    }

    @Override
    public String getDeviceWildcard() {
        return wildcard;
    }

    /**
     * Converts specified wildcard string to a regular expression
     *
     * @param wildcard The wildcard string to convert
     * @return An appropriate regular expression ready for being used in a {@link Pattern pattern}
     */
    private static String wildcardToRegex(final String wildcard) {
        final StringBuilder s = new StringBuilder(wildcard.length());
        s.append('^');
        final int len = wildcard.length();
        for (int i = 0; i < len; i++) {
            final char c = wildcard.charAt(i);
            if (c == '*') {
                s.append(".*");
            } else if (c == '?') {
                s.append('.');
            } else if (c == '(' || c == ')' || c == '[' || c == ']' || c == '$' || c == '^' || c == '.' || c == '{' || c == '}' || c == '|' || c == '\\') {
                s.append('\\');
                s.append(c);
            } else {
                s.append(c);
            }
        }
        s.append('$');
        return (s.toString());
    }

    /**
     * Closes the ResultSet.
     *
     * @param result <code>null</code> or a ResultSet to close.
     */
    private static void closeSQLStuff(final ResultSet result) {
        if (result != null) {
            try {
                result.close();
            } catch (final Exception e) {
            }
        }
    }

    /**
     * Closes the {@link Statement}.
     *
     * @param stmt <code>null</code> or a {@link Statement} to close.
     */
    private static void closeSQLStuff(final Statement stmt) {
        if (null != stmt) {
            try {
                stmt.close();
            } catch (final Exception e) {
            }
        }
    }

    /**
     * Closes the ResultSet and the Statement.
     *
     * @param result <code>null</code> or a ResultSet to close.
     * @param stmt <code>null</code> or a Statement to close.
     */
    private static void closeSQLStuff(final ResultSet result, final Statement stmt) {
        closeSQLStuff(result);
        closeSQLStuff(stmt);
    }

}
