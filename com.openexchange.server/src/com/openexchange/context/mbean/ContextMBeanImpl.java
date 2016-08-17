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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.context.mbean;

import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.slf4j.Logger;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link ContextMBeanImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ContextMBeanImpl extends StandardMBean implements ContextMBean {

    /**
     * Initializes a new {@link ContextMBeanImpl}.
     *
     * @throws NotCompliantMBeanException If the mbean interface does not follow JMX design patterns for Management Interfaces, or if this does not implement the specified interface.
     */
    public ContextMBeanImpl() throws NotCompliantMBeanException {
        super(ContextMBean.class);
    }

    @Override
    public void checkLogin2ContextMapping() throws MBeanException {
        final DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (null == databaseService) {
            final OXException oxe = ServiceExceptionCode.absentService(DatabaseService.class);
            throw new MBeanException(oxe, oxe.getMessage());
        }

        Connection con = null;
        try {
            con = databaseService.getWritable();
            // Get context identifiers
            final TIntList contextIds = new TIntLinkedList();
            {
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = con.prepareStatement("SELECT DISTINCT t1.cid FROM login2context AS t1 WHERE CONCAT('', t1.cid) NOT IN (SELECT t2.login_info FROM login2context AS t2 WHERE t2.cid = t1.cid)");
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        contextIds.add(rs.getInt(1));
                    }
                } catch (final SQLException e) {
                    throw new MBeanException(e, e.getMessage());
                } finally {
                    Databases.closeSQLStuff(rs, stmt);
                }
            }

            if (!contextIds.isEmpty()) {
                // Logger
                final Logger logger = org.slf4j.LoggerFactory.getLogger(ContextMBeanImpl.class);

                // Iterate context identifiers
                for (final int contextId : contextIds.toArray()) {
                    PreparedStatement stmt = null;
                    try {
                        stmt = con.prepareStatement("INSERT INTO login2context (cid, login_info) VALUES (?, ?)");
                        stmt.setInt(1, contextId);
                        stmt.setString(2, Integer.toString(contextId));
                        try {
                            stmt.executeUpdate();
                        } catch (final Exception e) {
                            logger.warn("Couldn't add context identifier to login2context mappings for context {}", Integer.valueOf(contextId), e);
                        }
                    } catch (final SQLException e) {
                        throw new MBeanException(e, e.getMessage());
                    } finally {
                        Databases.closeSQLStuff(stmt);
                    }
                }

                // Invalidate cache
                final ContextStorage cs = ContextStorage.getInstance();
                for (final int contextId : contextIds.toArray()) {
                    try {
                        cs.invalidateContext(contextId);
                    } catch (final Exception e) {
                        logger.warn("Error invalidating cached infos of context {} in context storage", contextId, e);
                    }
                }
            }
        } catch (final OXException e) {
            throw new MBeanException(e, e.getMessage());
        } finally {
            databaseService.backWritable(con);
        }
    }

    @Override
    public void checkLogin2ContextMapping(final int contextId) throws MBeanException {
        final DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (null == databaseService) {
            final OXException oxe = ServiceExceptionCode.absentService(DatabaseService.class);
            throw new MBeanException(oxe, oxe.getMessage());
        }

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = databaseService.getWritable();
            stmt = con.prepareStatement("INSERT INTO login2context (cid, login_info) VALUES (?, ?)");
            stmt.setInt(1, contextId);
            stmt.setString(2, Integer.toString(contextId));
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw new MBeanException(e, e.getMessage());
        } catch (final OXException e) {
            throw new MBeanException(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            databaseService.backWritable(con);
        }

        // Invalidate cache
        final ContextStorage cs = ContextStorage.getInstance();
        try {
            cs.invalidateContext(contextId);
        } catch (final Exception e) {
            final Logger logger = org.slf4j.LoggerFactory.getLogger(ContextMBeanImpl.class);
            logger.warn("Error invalidating cached infos of context {} in context storage", contextId, e);
        }
    }

}
