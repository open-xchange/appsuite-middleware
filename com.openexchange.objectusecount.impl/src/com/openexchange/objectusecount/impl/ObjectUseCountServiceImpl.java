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

package com.openexchange.objectusecount.impl;

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.contact.ContactService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.objectusecount.AbstractArguments;
import com.openexchange.objectusecount.BatchIncrementArguments;
import com.openexchange.objectusecount.BatchIncrementArguments.ObjectAndFolder;
import com.openexchange.objectusecount.IncrementArguments;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.objectusecount.SetArguments;
import com.openexchange.objectusecount.exception.ObjectUseCountExceptionCode;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.user.UserService;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * {@link ObjectUseCountServiceImpl}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ObjectUseCountServiceImpl implements ObjectUseCountService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ObjectUseCountServiceImpl.class);

    /** The service look-up */
    final ServiceLookup services;

    /**
     * Initializes a new {@link ObjectUseCountServiceImpl}.
     *
     * @param services The service look-up
     */
    public ObjectUseCountServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Checks if specified arguments allow to modify the use count asynchronously.
     *
     * @param arguments The arguments to check
     * @return <code>true</code> if asynchronous execution is possible; otherwise <code>false</code> for synchronous execution
     */
    private boolean doPerformAsynchronously(AbstractArguments arguments) {
        return null == arguments.getCon() && false == arguments.isThrowException();
    }

    @Override
    public int getObjectUseCount(Session session, int folderId, int objectId) throws OXException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        if (null == dbService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class);
        }

        Connection con = dbService.getReadOnly(session.getContextId());
        try {
            return getObjectUseCount(session, folderId, objectId, con);
        } finally {
            dbService.backReadOnly(session.getContextId(), con);
        }
    }

    @Override
    public int getObjectUseCount(Session session, int folderId, int objectId, Connection con) throws OXException {
        if (null == con) {
            return getObjectUseCount(session, folderId, objectId);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT value FROM object_use_count WHERE cid = ? AND user = ? AND folder = ? AND object = ?");
            stmt.setInt(1, session.getContextId());
            stmt.setInt(2, session.getUserId());
            stmt.setInt(3, folderId);
            stmt.setInt(4, objectId);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw ObjectUseCountExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs);
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void incrementObjectUseCount(final Session session, final IncrementArguments arguments) throws OXException {
        try {
            Task<Void> task = new AbstractTask<Void>() {

                @Override
                public Void call() throws OXException {
                    int userId = arguments.getUserId();
                    if (userId > 0) {
                        // By user identifier
                        UserService userService = services.getService(UserService.class);
                        if (null == userService) {
                            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class);
                        }
                        User user = userService.getUser(userId, session.getContextId());
                        TIntIntMap object2folder = new TIntIntHashMap(2);
                        object2folder.put(user.getContactId(), FolderObject.SYSTEM_LDAP_FOLDER_ID);
                        incrementObjectUseCount(object2folder, session.getUserId(), session.getContextId(), arguments.getCon());
                    }

                    Collection<String> mailAddresses = arguments.getMailAddresses();
                    if (null != mailAddresses && !mailAddresses.isEmpty()) {
                        // By mail address(es)
                        ContactService contactService = services.getService(ContactService.class);
                        if (null == contactService) {
                            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ContactService.class);
                        }

                        TIntIntMap object2folder = new TIntIntHashMap(mailAddresses.size());
                        for (String mail : mailAddresses) {
                            ContactSearchObject search = new ContactSearchObject();
                            search.setEmail1(mail);
                            SearchIterator<Contact> it = contactService.searchContacts(session, search);
                            try {
                                while (it.hasNext()) {
                                    Contact c = it.next();
                                    object2folder.put(c.getObjectID(), c.getParentFolderID());
                                }
                            } finally {
                                SearchIterators.close(it);
                            }
                        }
                        incrementObjectUseCount(object2folder, session.getUserId(), session.getContextId(), arguments.getCon());
                    }

                    if (arguments instanceof BatchIncrementArguments) {
                        BatchIncrementArguments batchArguments = (BatchIncrementArguments) arguments;
                        batchIncrementObjectUseCount(batchArguments.getCounts(), session.getUserId(), session.getContextId(), arguments.getCon());
                    } else {
                        int objectId = arguments.getObjectId();
                        int folderId = arguments.getFolderId();
                        if (objectId > 0 && folderId > 0) {
                            // By object/folder identifier
                            TIntIntMap object2folder = new TIntIntHashMap(2);
                            object2folder.put(objectId, folderId);
                            incrementObjectUseCount(object2folder, session.getUserId(), session.getContextId(), arguments.getCon());
                        }
                    }

                    return null;
                }
            };

            if (doPerformAsynchronously(arguments)) {
                // Execute asynchronously; as a new connection is supposed to be fetched and no error should be signaled; thus "fire & forget"
                ThreadPools.submitElseExecute(task);
            } else {
                task.call();
            }
        } catch (OXException e) {
            if (arguments.isThrowException()) {
                throw e;
            }

            LOG.debug("Failed to increment object use count", e);
        } catch (RuntimeException e) {
            if (arguments.isThrowException()) {
                throw ObjectUseCountExceptionCode.UNKNOWN.create(e, e.getMessage());
            }

            LOG.debug("Failed to increment object use count", e);
        } catch (Exception e) {
            if (arguments.isThrowException()) {
                throw ObjectUseCountExceptionCode.UNKNOWN.create(e, e.getMessage());
            }

            LOG.debug("Failed to increment object use count", e);
        }
    }

    private void batchIncrementObjectUseCount(Map<ObjectAndFolder, Integer> counts, int userId, int contextId) throws OXException {
        if (null == counts || counts.isEmpty()) {
            return;
        }

        DatabaseService dbService = services.getService(DatabaseService.class);
        if (null == dbService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class);
        }
        Connection con = dbService.getWritable(contextId);
        try {
            batchIncrementObjectUseCount(counts, userId, contextId, con);
        } finally {
            dbService.backWritable(contextId, con);
        }
    }

    void batchIncrementObjectUseCount(Map<ObjectAndFolder, Integer> counts, int userId, int contextId, Connection con) throws OXException {
        if (null == con) {
            batchIncrementObjectUseCount(counts, userId, contextId);
            return;
        }

        if (null == counts || counts.isEmpty()) {
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO object_use_count (cid, user, folder, object, value) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE value=value + ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);

            Iterator<Entry<ObjectAndFolder, Integer>> iterator = counts.entrySet().iterator();
            int size = counts.size();
            if (size > 1) {
                for (int i = size; i-- > 0;) {
                    Map.Entry<ObjectAndFolder, Integer> entry = iterator.next();
                    ObjectAndFolder key = entry.getKey();
                    int count = entry.getValue().intValue();
                    stmt.setInt(3, key.getFolderId());
                    stmt.setInt(4, key.getObjectId());
                    stmt.setInt(5, count);
                    stmt.setInt(6, count);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } else {
                Map.Entry<ObjectAndFolder, Integer> entry = iterator.next();
                ObjectAndFolder key = entry.getKey();
                int count = entry.getValue().intValue();
                stmt.setInt(3, key.getFolderId());
                stmt.setInt(4, key.getObjectId());
                stmt.setInt(5, count);
                stmt.setInt(6, count);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw ObjectUseCountExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void incrementObjectUseCount(TIntIntMap object2folder, int userId, int contextId) throws OXException {
        if (null == object2folder || object2folder.isEmpty()) {
            return;
        }

        DatabaseService dbService = services.getService(DatabaseService.class);
        if (null == dbService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class);
        }
        Connection con = dbService.getWritable(contextId);
        try {
            incrementObjectUseCount(object2folder, userId, contextId, con);
        } finally {
            dbService.backWritable(contextId, con);
        }
    }

    void incrementObjectUseCount(TIntIntMap contact2folder, int userId, int contextId, Connection con) throws OXException {
        if (null == con) {
            incrementObjectUseCount(contact2folder, userId, contextId);
            return;
        }

        if (null == contact2folder || contact2folder.isEmpty()) {
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO object_use_count (cid, user, folder, object, value) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE value=value+1");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);

            TIntIntIterator iterator = contact2folder.iterator();
            int size = contact2folder.size();
            if (size > 1) {
                for (int i = size; i-- > 0;) {
                    iterator.advance();
                    stmt.setInt(3, iterator.value());
                    stmt.setInt(4, iterator.key());
                    stmt.setInt(5, 1);
                    stmt.addBatch();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Incremented object use count for user {}, folder {}, object {} in context {}.", userId, iterator.value(), iterator.key(), contextId, new Throwable("use-count-trace"));
                    }
                }
                stmt.executeBatch();
            } else {
                iterator.advance();
                stmt.setInt(3, iterator.value());
                stmt.setInt(4, iterator.key());
                stmt.setInt(5, 1);
                stmt.executeUpdate();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Incremented object use count for user {}, folder {}, object {} in context {}.", userId, iterator.value(), iterator.key(), contextId, new Throwable("use-count-trace"));
                }
            }
        } catch (SQLException e) {
            throw ObjectUseCountExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void resetObjectUseCount(Session session, int folder, int objectId) throws OXException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        if (null == dbService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class);
        }

        Connection con = dbService.getWritable(session.getContextId());
        try {
            resetObjectUseCount(session, folder, objectId, con);
        } finally {
            dbService.backWritable(session.getContextId(), con);
        }
    }

    @Override
    public void resetObjectUseCount(Session session, int folder, int objectId, Connection con) throws OXException {
        if (null == con) {
            resetObjectUseCount(session, folder, objectId);
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE object_use_count SET value = 0 WHERE cid = ? AND user = ? AND folder = ? AND object = ?");
            stmt.setInt(1, session.getContextId());
            stmt.setInt(2, session.getUserId());
            stmt.setInt(3, folder);
            stmt.setInt(4, objectId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw ObjectUseCountExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void setObjectUseCount(final Session session, final SetArguments arguments) throws OXException {
        try {
            Task<Void> task = new AbstractTask<Void>() {

                @Override
                public Void call() throws Exception {
                    setObjectUseCount(arguments.getFolderId(), arguments.getObjectId(), arguments.getValue(), session.getUserId(), session.getContextId(), arguments.getCon());

                    return null;
                }
            };

            if (doPerformAsynchronously(arguments)) {
                // Execute asynchronously; as a new connection is supposed to be fetched and no error should be signaled; thus "fire & forget"
                ThreadPools.submitElseExecute(task);
            } else {
                task.call();
            }
        } catch (OXException e) {
            if (arguments.isThrowException()) {
                throw e;
            }

            LOG.debug("Failed to set object use count", e);
        } catch (RuntimeException e) {
            if (arguments.isThrowException()) {
                throw ObjectUseCountExceptionCode.UNKNOWN.create(e, e.getMessage());
            }

            LOG.debug("Failed to set object use count", e);
        } catch (Exception e) {
            if (arguments.isThrowException()) {
                throw ObjectUseCountExceptionCode.UNKNOWN.create(e, e.getMessage());
            }

            LOG.debug("Failed to set object use count", e);
        }
    }

    private void setObjectUseCount(int folderId, int objectId, int value, int userId, int contextId) throws OXException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        if (null == dbService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class);
        }
        Connection con = dbService.getWritable(contextId);
        try {
            setObjectUseCount(folderId, objectId, value, userId, contextId, con);
        } finally {
            dbService.backWritable(contextId, con);
        }
    }

    void setObjectUseCount(int folderId, int objectId, int value, int userId, int contextId, Connection con) throws OXException {
        if (null == con) {
            setObjectUseCount(folderId, objectId, value, userId, contextId);
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO object_use_count (cid, user, folder, object, value) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE value=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, folderId);
            stmt.setInt(4, objectId);
            stmt.setInt(5, value);
            stmt.setInt(6, value);
            stmt.executeUpdate();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Set object use count to {} for user {}, folder {}, object {} in context {}", value, userId, folderId, objectId, contextId, new Throwable("use-count-trace"));
            }
        } catch (SQLException e) {
            throw ObjectUseCountExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

}
