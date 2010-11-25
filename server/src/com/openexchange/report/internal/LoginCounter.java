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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.management.MBeanException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DatabaseService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com>Steffen Templin</a>
 */
public class LoginCounter implements LoginCounterMBean {
    
    private static final Log LOG = LogFactory.getLog(ReportingMBean.class);
    
    private String regex = null;
    

    public int getNumberOfLogins(Date startDate, Date endDate) throws MBeanException { 
        final String SELECT_SCHEMAS = "SELECT read_db_pool_id, db_schema FROM context_server2db_pool GROUP BY db_schema";
        final String SELECT_SQL = "SELECT name, value FROM user_attribute";
        
        Pattern pattern = null;            
        if (regex != null)  {
            try {
                pattern = Pattern.compile("client:" + regex);
            } catch (PatternSyntaxException e) {
                LOG.error(e.getMessage(), e);
                throw new MBeanException(e, "Couldn't compile regex pattern.");
            }                
        }
        
        int counter = 0;
        
        
        DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class); 
        Connection readcon = null;        
        try {
            readcon = dbService.getReadOnly();            
        } catch (DBPoolingException e) {
            LOG.error(e.getMessage(), e);
            throw new MBeanException(e, "Couldn't get connection to configdb.");
        }
        
        
        // Get all schemas and put them into a map.
        Statement statement = null;
        ResultSet executeQuery;
        HashMap<String, Integer> schemaMap = new HashMap<String, Integer>(50);
        try {
            statement = readcon.createStatement();
            executeQuery = statement.executeQuery(SELECT_SCHEMAS); 
            
            while (executeQuery.next()) {
                int readPool = executeQuery.getInt("read_db_pool_id");
                String schema = executeQuery.getString("db_schema");
                schemaMap.put(schema, readPool);
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new MBeanException(e, e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            
            if (readcon != null) {
                dbService.backReadOnly(readcon);
            }
        }
        

        // Get all logins in every schema
        for (String schema : schemaMap.keySet()) {
            int readPool = schemaMap.get(schema);
            
            Connection connection = null;
            try {
                connection = dbService.get(readPool, schema);                
            } catch (DBPoolingException e) {
                LOG.error(e.getMessage(), e);
                throw new MBeanException(e, "Couldn't get connection to schema " + schema + " in pool " + readPool + ".");
            }
            
            PreparedStatement stmt = null;
            ResultSet rs;
            try {
                stmt = connection.prepareStatement(SELECT_SQL);
                rs = stmt.executeQuery();
                
                while (rs.next()) {
                    String name = rs.getString("name");
                    if (regex == null || (regex != null && pattern.matcher(name).matches())) {
                        boolean isDate = true;
                        String value = rs.getString("value");
                        Date lastLogin = null;
                        try {
                            long lastLoginLong = Long.parseLong(value);
                            lastLogin = new Date(lastLoginLong);
                        } catch (NumberFormatException e) {
                            isDate = false;
                        }
                        if (isDate) {
                            if (lastLogin.after(startDate) && lastLogin.before(endDate)) {
                                counter++;
                            }
                        }
                    }                   
                }
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
                throw new MBeanException(e, e.getMessage());
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                
                if (connection != null) {
                    dbService.back(readPool, connection);
                }
            }
        }
        
        return counter;
    }

    public void setDeviceRegex(String regex) {
        this.regex = regex;        
    }

    public String getDeviceRegex() {
        return regex;
    }

}
