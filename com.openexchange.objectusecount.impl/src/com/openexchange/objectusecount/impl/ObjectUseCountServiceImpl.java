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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.objectusecount.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import javax.mail.internet.InternetAddress;
import com.openexchange.contact.ContactService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.objectusecount.exception.ObjectUseCountExceptionCode;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.user.UserService;

/**
 * {@link ObjectUseCountServiceImpl}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class ObjectUseCountServiceImpl implements ObjectUseCountService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ObjectUseCountServiceImpl.class);

    private ServiceLookup services;

    public ObjectUseCountServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public int getObjectUseCount(Session session, int folderId, int objectId) throws OXException {
        return getObjectUseCount(session, folderId, objectId, null);
    }

    @Override
    public int getObjectUseCount(Session session, int folderId, int objectId, Connection con) throws OXException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        boolean newConnection = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (null == con) {
                con = dbService.getReadOnly(session.getContextId());
                newConnection = true;
            }
            stmt = con.prepareStatement("SELECT value FROM object_use_count WHERE cid = ? AND user = ? AND folder = ? AND object = ?");
            stmt.setInt(1, session.getContextId());
            stmt.setInt(2, session.getUserId());
            stmt.setInt(3, folderId);
            stmt.setInt(4, objectId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw ObjectUseCountExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs);
            closeSQLStuff(stmt);
            if (newConnection) {
                dbService.backReadOnly(con);
            }
        }
    }

    @Override
    public void incrementObjectUseCount(Session session, int folder, int objectId) throws OXException {
        incrementObjectUseCount(session, folder, objectId, null);
    }

    @Override
    public void incrementObjectUseCount(Session session, int folder, int objectId, Connection con) throws OXException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        UserService userService = services.getService(UserService.class);
        if (null == userService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class);
        }
        User u = userService.getUser(objectId, session.getContextId());
        boolean newConnection = false;
        PreparedStatement stmt = null;
        try {
            if (null == con) {
                con = dbService.getWritable(session.getContextId());
                newConnection = true;
            }
            stmt = con.prepareStatement("INSERT INTO object_use_count (cid, user, folder, object, value) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE value=value+1");
            stmt.setInt(1, session.getContextId());
            stmt.setInt(2, session.getUserId());
            stmt.setInt(3, folder);
            stmt.setInt(4, u.getContactId());
            stmt.setInt(5, 1);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw ObjectUseCountExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            if (newConnection) {
                dbService.backWritable(con);
            }
        }
    }

    @Override
    public void incrementObjectUseCount(Session session, String mail) throws OXException {
        incrementObjectUseCount(session, mail, null);
    }

    @Override
    public void incrementObjectUseCount(Session session, String mail, Connection con) throws OXException {
        ContactService contactService = services.getService(ContactService.class);
        DatabaseService dbService = services.getService(DatabaseService.class);
        boolean newConnection = false;
        PreparedStatement stmt = null;
        ContactSearchObject search = new ContactSearchObject();
        search.setEmail1(mail);
        SearchIterator<Contact> it = contactService.searchContacts(session, search);
        try {
            if (null == con) {
                con = dbService.getWritable(session.getContextId());
                newConnection = true;
            }
            stmt = con.prepareStatement("INSERT INTO object_use_count (cid, user, folder, object, value) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE value=value+1");
            while (it.hasNext()) {
                Contact c = it.next();
                stmt.setInt(1, session.getContextId());
                stmt.setInt(2, session.getUserId());
                stmt.setInt(3, c.getParentFolderID());
                stmt.setInt(4, c.getObjectID());
                stmt.setInt(5, 1);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw ObjectUseCountExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            if (newConnection) {
                dbService.backWritable(con);
            }
        }
    }

    @Override
    public void incrementObjectUseCount(Session session, Set<InternetAddress> addresses, Connection con) throws OXException {
        for (InternetAddress address : addresses) {
            incrementObjectUseCount(session, address.getAddress(), con);
        }
    }

    @Override
    public void incrementObjectUseCount(Session session, Set<InternetAddress> addresses) throws OXException {
        for (InternetAddress address : addresses) {
            incrementObjectUseCount(session, address.getAddress(), null);
        }
    }

    @Override
    public void resetObjectUseCount(Session session, int folder, int objectId) throws OXException {
        resetObjectUseCount(session, folder, objectId, null);
    }

    @Override
    public void resetObjectUseCount(Session session, int folder, int objectId, Connection con) throws OXException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        boolean newConnection = false;
        PreparedStatement stmt = null;
        try {
            if (null == con) {
                con = dbService.getWritable(session.getContextId());
                newConnection = true;
            }
            stmt = con.prepareStatement("UPDATE object_use_count SET VALUE = 0 WHERE cid = ? AND user = ? AND folder = ? AND object = ?");
            stmt.setInt(1, session.getContextId());
            stmt.setInt(2, session.getUserId());
            stmt.setInt(3, folder);
            stmt.setInt(4, objectId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw ObjectUseCountExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            if (newConnection) {
                dbService.backWritable(con);
            }
        }
    }

    private void closeSQLStuff(final ResultSet result) {
        if (result != null) {
            try {
                result.close();
            } catch (final SQLException e) {
                LOG.error("", e);
            }
        }
    }

    private void closeSQLStuff(final Statement stmt) {
        if (null != stmt) {
            try {
                stmt.close();
            } catch (final SQLException e) {
                LOG.error("", e);
            }
        }
    }

}
