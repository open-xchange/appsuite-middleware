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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.contact.storage.rdb.mbean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import com.openexchange.contact.storage.rdb.internal.Deduplicator;
import com.openexchange.contact.storage.rdb.internal.RdbServiceLookup;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.util.UUIDs;

/**
 * {@link ContactStorageMBeanImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactStorageMBeanImpl extends StandardMBean implements ContactStorageMBean {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactStorageMBeanImpl.class);

    /**
     * Initializes a new {@link ContactStorageMBeanImpl}.
     */
    public ContactStorageMBeanImpl() throws NotCompliantMBeanException {
        super(ContactStorageMBean.class);
    }

    @Override
    public int[] deduplicateContacts(int contextID, int folderID, long limit, boolean dryRun) {
        Collection<Integer> objectIDs = null;
        try {
            objectIDs = Deduplicator.deduplicateContacts(contextID, folderID, limit, dryRun);
        } catch (OXException e) {
            LOG.error("Error de-duplicating contacts in folder {} of context {}{}: {}",
                folderID, contextID, dryRun ? " [dry-run]" : "", e.getMessage(), e);
        }
        return null != objectIDs ? Autoboxing.I2i(objectIDs) : null;
    }

    @Override
    public List<List<Integer>> checkUserAliases(int optContextId, boolean dryRun) throws MBeanException {
        try {
            DatabaseService databaseService = RdbServiceLookup.getService(DatabaseService.class);
            if (optContextId > 0) {
                return Collections.singletonList(toIntList(optContextId, checkUserAliasesForContext(optContextId, dryRun, databaseService)));
            }

            List<List<Integer>> list = new LinkedList<List<Integer>>();
            for (Integer contextIdentifier : RdbServiceLookup.getService(ContextService.class).getAllContextIds()) {
                int contextId = contextIdentifier.intValue();
                list.add(toIntList(contextId, checkUserAliasesForContext(contextId, dryRun, databaseService)));
            }
            return list;
        } catch (Exception e) {
            final String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    private List<Integer> toIntList(int contextId, List<UserAliasInfo> list) {
        int size = list.size();
        if (size <= 0) {
            return Collections.emptyList();
        }
        List<Integer> l = new ArrayList<Integer>(size + 1);
        l.add(Integer.valueOf(contextId));
        for (int i = size; i-- > 0;) {
            l.add(Integer.valueOf(list.get(i).userId));
        }
        return l;
    }

    private List<UserAliasInfo> checkUserAliasesForContext(int contextId, boolean dryRun, DatabaseService databaseService) throws OXException {
        Connection con = databaseService.getWritable(contextId);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        boolean rollback = false;
        boolean modified = false;
        try {
            stmt = con.prepareStatement("SELECT u.id, c.field65 FROM user AS u JOIN prg_contacts AS c ON u.cid=c.cid AND u.id=c.userid WHERE u.cid=? AND c.field65 NOT IN (SELECT user_attribute.value FROM user_attribute WHERE user_attribute.cid=u.cid AND user_attribute.id=u.id AND name='alias')");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();

            // Check result set
            if (false == rs.next()) {
                return Collections.emptyList();
            }

            // Iterate its results
            List<UserAliasInfo> list = new LinkedList<UserAliasInfo>();
            do {
                list.add(new UserAliasInfo(rs.getString(2), rs.getInt(1)));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;

            if (dryRun) {
                return list;
            }

            // Insert missing address
            Databases.startTransaction(con);
            rollback = true;
            stmt = con.prepareStatement("INSERT INTO user_attribute (cid, id, name, value, uuid) VALUES (?,?,?,?,UNHEX(?))");
            stmt.setInt(1, contextId);
            stmt.setString(3, "alias");
            for (UserAliasInfo uai : list) {
                stmt.setInt(2, uai.userId);
                stmt.setString(4, uai.email1);
                stmt.setString(5, UUIDs.getUnformattedString(UUID.randomUUID()));
                stmt.addBatch();
            }
            stmt.executeBatch();
            con.commit();
            rollback = false;

            return list;
        } catch (SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);

            if (rollback) {
                Databases.rollback(con);
            }

            Databases.autocommit(con);
            if (modified) {
                databaseService.backWritable(contextId, con);
            } else {
                databaseService.backWritableAfterReading(contextId, con);
            }
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------

    private static class UserAliasInfo {

        final int userId;
        final String email1;

        UserAliasInfo(String email1, int userId) {
            super();
            this.email1 = email1;
            this.userId = userId;
        }
    } // End of class UserAliasInfo

}
