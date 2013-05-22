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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanException;
import org.apache.commons.logging.Log;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link LoginCounter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LoginCounter implements LoginCounterMBean {

    private final Log logger;

    /**
     * Initializes a new {@link LoginCounter}.
     */
    public LoginCounter() {
        super();
        logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(ReportingMBean.class));
    }

    @Override
    public List<Object[]> getLastLoginTimeStamp(final int userId, final int contextId, final String client) throws MBeanException {
        final DatabaseService service = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (null == service) {
            throw new MBeanException(null, "DatabaseService not available at the moment. Try again later.");
        }
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = service.getReadOnly(contextId);
            if ("*".equals(client.trim())) {
                stmt = con.prepareStatement("SELECT value, name FROM user_attribute WHERE cid=? AND id=? AND name LIKE 'client:%'");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                rs = stmt.executeQuery();
                final List<Object[]> ret = new LinkedList<Object[]>();
                while (rs.next()) {
                    final String name = rs.getString(2);
                    ret.add(new Object[] { new Date(Long.parseLong(rs.getString(1))), name.substring(7) });
                }
                return ret;
            }
            // Query for single client identifier
            stmt = con.prepareStatement("SELECT value FROM user_attribute WHERE cid=? AND id=? AND name=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, "client:" + client);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new MBeanException(null, "No such entry found (user="+userId+", context="+contextId+", client=\""+client+"\").");
            }
            return Collections.singletonList(new Object[] { new Date(Long.parseLong(rs.getString(1))), client });
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
    public Map<String, Integer> getNumberOfLogins(final Date startDate, final Date endDate, boolean aggregate, String regex) throws MBeanException {
        if (startDate == null) {
            throw new MBeanException(new IllegalArgumentException("Parameter 'startDate' must not be null!"));
        }
        
        if (endDate == null) {
            throw new MBeanException(new IllegalArgumentException("Parameter 'endDate' must not be null!"));
        }
        
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
         * Get all logins of every schema
         */
        int sum = 0;
        final Map<String, Integer> results = new HashMap<String, Integer>();
        for (final String schema : schemaMap.keySet()) {
            final Set<UserContextId> countedUsers = new HashSet<UserContextId>();
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
                StringBuilder sb = new StringBuilder("SELECT cid, id, name, value FROM user_attribute WHERE name REGEXP ?");
                if (regex == null) {
                    regex = ".*";
                }
                stmt = connection.prepareStatement(sb.toString());
                stmt.setString(1, "client:(" + regex + ")");
                rs = stmt.executeQuery();
                while (rs.next()) {
                    final int contextId = rs.getInt(1);
                    final int userId = rs.getInt(2);
                    final String client = rs.getString(3);
                    try {
                        Date lastLogin = new Date(Long.parseLong(rs.getString(4)));
                        if (lastLogin.after(startDate) && lastLogin.before(endDate)) {
                            if (aggregate) {
                                UserContextId userContextId = new UserContextId(contextId, userId);
                                if (!countedUsers.contains(userContextId)) {
                                    countedUsers.add(userContextId);
                                    ++sum;
                                }
                            } else {
                                ++sum;
                            }
                            
                            Integer value = results.get(client);
                            if (value == null) {
                                results.put(client, 1);
                            } else {
                                results.put(client, value.intValue() + 1);
                            }
                        }
                    } catch (final NumberFormatException e) {
                        logger.warn("Client value is not a number.", e);
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

        results.put(SUM, sum);
        return results;
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
    
    private static final class UserContextId {
        
        private final int contextId;
        private final int userId;
        private final int hash;

        /**
         * Initializes a new {@link UserContextId}.
         * @param contextId
         * @param userId
         */
        public UserContextId(int contextId, int userId) {
            super();
            this.contextId = contextId;
            this.userId = userId;
            final int prime = 31;
            int result = 1;
            result = prime * result + contextId;
            result = prime * result + userId;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            UserContextId other = (UserContextId) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            return true;
        }
    }

}
