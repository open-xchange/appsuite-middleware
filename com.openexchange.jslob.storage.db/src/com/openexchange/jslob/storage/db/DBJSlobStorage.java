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

package com.openexchange.jslob.storage.db;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.AsciiWriter;
import com.openexchange.java.Streams;
import com.openexchange.java.util.Pair;
import com.openexchange.jslob.DefaultJSlob;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobExceptionCodes;
import com.openexchange.jslob.JSlobId;
import com.openexchange.jslob.storage.JSlobStorage;
import com.openexchange.jslob.storage.db.cache.CachingJSlobStorage;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DBJSlobStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBJSlobStorage implements JSlobStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DBJSlobStorage.class);

    private static final String ID = "io.ox.wd.jslob.storage.db";

    private final ServiceLookup services;
    private volatile Boolean streamBasedJDBC;

    /**
     * Initializes a new {@link DBJSlobStorage}.
     */
    public DBJSlobStorage(final ServiceLookup services) {
        super();
        this.services = services;
    }

    private boolean streamBasedJDBC() {
        Boolean tmp = streamBasedJDBC;
        if (null == tmp) {
            synchronized (DBJSlobStorage.class) {
                tmp = streamBasedJDBC;
                if (null == tmp) {
                    final ConfigurationService service = services.getService(ConfigurationService.class);
                    tmp = Boolean.valueOf(null == service || service.getBoolProperty("com.openexchange.jslob.storage.db.streamBasedJDBC", true));
                    streamBasedJDBC = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    @Override
    public String getIdentifier() {
        return ID;
    }

    private static final String SQL_DELETE_ALL_USER = "DELETE FROM jsonStorage WHERE cid = ? AND user = ?";

    /**
     * Drops all JSlob entries associated with specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If deleting all user entries fails
     */
    public void dropAllUserJSlobs(final int userId, final int contextId) throws OXException {
        {
            final CachingJSlobStorage cachingJSlobStorage = CachingJSlobStorage.getInstance();
            if (null != cachingJSlobStorage) {
                cachingJSlobStorage.dropAllUserJSlobs(userId, contextId);
            }
        }
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getWritable(contextId);
        boolean committed = true;
        PreparedStatement stmt = null;
        try {
            /*
             * Now delete
             */
            con.setAutoCommit(false); // BEGIN
            committed = false;
            stmt = con.prepareStatement(SQL_DELETE_ALL_USER);
            stmt.setLong(1, contextId);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
            con.commit(); // COMMIT
            committed = true;
        } catch (final SQLException e) {
            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (!committed) {
                Databases.rollback(con);
            }
            Databases.closeSQLStuff(stmt);
            Databases.autocommit(con);
            databaseService.backWritable(contextId, con);
        }
    }

    @Override
    public JSlob load(JSlobId id) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        int contextId = id.getContext();
        Connection con = databaseService.getReadOnly(contextId);
        try {
            JSlob jslob = load(id, contextId, con);
            if (null == jslob) {
                throw JSlobExceptionCodes.NOT_FOUND_EXT.create(id.getServiceId(), Integer.valueOf(id.getUser()), Integer.valueOf(contextId));
            }
            return jslob;
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    @Override
    public void invalidate(JSlobId id) {
        // nothing to do
    }

    @Override
    public JSlob opt(JSlobId id) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        int contextId = id.getContext();
        Connection con = databaseService.getReadOnly(contextId);
        try {
            return load(id, contextId, con);
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    private JSlob load(JSlobId id, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT data FROM jsonStorage WHERE cid = ? AND user = ? AND serviceId = ? AND id = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, id.getUser());
            stmt.setString(3, id.getServiceId());
            stmt.setString(4, id.getId());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return new DefaultJSlob(new JSONObject(new AsciiReader(rs.getBinaryStream(1)))).setId(id);
            // return new JSlob(new JSONObject(new AsciiReader(rs.getBlob(1).getBinaryStream()))).setId(id);
        } catch (final SQLException e) {
            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw JSlobExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }

    }

    @Override
    public List<JSlob> list(List<JSlobId> ids) throws OXException {
        if (null == ids || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> failedOnes = new LinkedList<String>();
        List<JSlob> loaded = null;
        {
            DatabaseService databaseService = getDatabaseService();
            int contextId = ids.get(0).getContext();
            Connection con = databaseService.getReadOnly(contextId);
            try {
                loaded = loadThem(ids, contextId, con, failedOnes);
            } finally {
                databaseService.backReadOnly(contextId, con);
            }
        }

        // Check for failed parse attempts
        if (!failedOnes.isEmpty()) {
            final JSlobId jSlobId = ids.get(0);
            deleteCorruptBlobsSafe(failedOnes, jSlobId.getServiceId(), jSlobId.getUser(), jSlobId.getContext());
        }

        // Return loaded ones
        return loaded;
    }

    private List<JSlob> loadThem(List<JSlobId> ids, int contextId, Connection con, List<String> failedOnes) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final JSlobId id = ids.get(0);
            final String serviceId = id.getServiceId();
            final int user = id.getUser();
            stmt = con.prepareStatement("SELECT data, id FROM jsonStorage WHERE cid = ? AND user = ? AND serviceId = ? AND id IN " + getInString(ids));
            int pos = 1;
            stmt.setLong(pos++, contextId);
            stmt.setLong(pos++, user);
            stmt.setString(pos++, serviceId);

            int size = ids.size();
            for (int i = 0; i < size; i++) {
                stmt.setString(pos++, ids.get(i).getId());
            }

            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }

            Map<String, JSlob> map = new HashMap<String, JSlob>(size);
            do {
                String sId = rs.getString(2);
                try {
                    map.put(sId, new DefaultJSlob(new JSONObject(new AsciiReader(rs.getBinaryStream(1)))).setId(new JSlobId(serviceId, sId, user, contextId)));
                } catch (final JSONException e) {
                    // JSON garbage contained in BLOB - provide an empty JSlob
                    LOG.warn("Error deserializing stored JSlob data - falling back to empty JSlob", e);
                    if (null != failedOnes) {
                        failedOnes.add(sId);
                    }
                    map.put(sId, new DefaultJSlob(new JSONObject(1)).setId(new JSlobId(serviceId, sId, user, contextId)));
                }
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            // Generate list
            List<JSlob> list = new ArrayList<JSlob>(size);
            for (JSlobId jSlobId : ids) {
                list.add(map.get(jSlobId.getId()));
            }
            return list;
        } catch (final SQLException e) {
            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private String getInString(final List<JSlobId> ids) {
        final int size = ids.size();
        final StringBuilder sb = new StringBuilder(size << 2);
        sb.append('(').append('?');
        for (int i = 1; i < size; i++) {
            sb.append(',').append('?');
        }
        return sb.append(')').toString();
    }

    private void deleteCorruptBlobsSafe(final List<String> ids, final String serviceId, final int userId, final int contextId) {
        if (null == ids || ids.isEmpty()) {
            return;
        }

        DatabaseService databaseService = getDatabaseService();
        Connection con = null;
        boolean committed = true;
        PreparedStatement stmt = null;
        try {
            con = databaseService.getWritable(contextId);
            con.setAutoCommit(false);
            committed = false;
            stmt = con.prepareStatement("DELETE FROM jsonStorage WHERE cid = ? AND user = ? AND serviceId = ? AND id = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, userId);
            stmt.setString(3, serviceId);
            for (final String id : ids) {
                stmt.setString(4, id);
                stmt.addBatch();
            }
            stmt.executeBatch();
            con.commit();
            committed = true;
        } catch (final Exception e) {
            final Logger logger = org.slf4j.LoggerFactory.getLogger(DBJSlobStorage.class);
            logger.warn("Failed to delete corrupt JSlobs from service {} (user={}, context={}): {}", serviceId, userId, contextId, ids, e);
        } finally {
            if (!committed) {
                Databases.rollback(con);
            }
            Databases.closeSQLStuff(stmt);
            Databases.autocommit(con);
            if (null != con) {
                databaseService.backWritable(contextId, con);
            }
        }
    }

    @Override
    public Collection<JSlob> list(final JSlobId id) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        int contextId = id.getContext();
        Connection con = databaseService.getReadOnly(contextId);
        try {
            return loadAll(id, contextId, con);
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    private Collection<JSlob> loadAll(final JSlobId id, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT data, id FROM jsonStorage WHERE cid = ? AND user = ? AND serviceId = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, id.getUser());
            stmt.setString(3, id.getServiceId());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }

            List<JSlob> list = new LinkedList<JSlob>();
            do {
                list.add(new DefaultJSlob(new JSONObject(new AsciiReader(rs.getBinaryStream(1)))).setId(new JSlobId(id.getServiceId(), rs.getString(2), id.getUser(), contextId)));
            } while (rs.next());
            return list;
        } catch (final SQLException e) {
            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw JSlobExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Loads all identifiers.
     *
     * @param id The JSlob identifier
     * @return The identifiers
     * @throws OXException  f operation fails
     */
    public Collection<String> getIDs(final JSlobId id) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = id.getContext();
        final Connection con = databaseService.getReadOnly(contextId);
        try {
            return getIDs(id, contextId, con);
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    /**
     * Loads all identifiers.
     *
     * @param id The JSlob identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @return The identifiers
     * @throws OXException  f operation fails
     */
    public Collection<String> getIDs(final JSlobId id, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM jsonStorage WHERE cid = ? AND user = ? AND serviceId = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, id.getUser());
            stmt.setString(3, id.getServiceId());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            final List<String> list = new LinkedList<String>();
            do {
                list.add(rs.getString(1));
            } while (rs.next());
            return list;
        } catch (final SQLException e) {
            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public JSlob remove(final JSlobId id) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        int contextId = id.getContext();
        Connection con = databaseService.getWritable(contextId);
        boolean committed = true;
        PreparedStatement stmt = null;
        try {
            /*
             * Load
             */
            final JSlob toDelete = load(id, contextId, con);
            if (null == toDelete) {
                throw JSlobExceptionCodes.NOT_FOUND_EXT.create(
                    id.getServiceId(),
                    Integer.valueOf(id.getUser()),
                    Integer.valueOf(contextId));
            }
            /*
             * Now delete
             */
            con.setAutoCommit(false);
            committed = false;
            stmt = con.prepareStatement("DELETE FROM jsonStorage WHERE cid = ? AND user = ? AND serviceId = ? AND id = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, id.getUser());
            stmt.setString(3, id.getServiceId());
            stmt.setString(4, id.getId());
            final int rows = stmt.executeUpdate();
            if (rows < 1) {
                throw JSlobExceptionCodes.NOT_FOUND_EXT.create(
                    id.getServiceId(),
                    Integer.valueOf(id.getUser()),
                    Integer.valueOf(contextId));
            } else if (rows > 1) {
                throw JSlobExceptionCodes.CONFLICT.create(id.getServiceId(), Integer.valueOf(id.getUser()), Integer.valueOf(contextId));
            }
            /*
             * Return
             */
            con.commit();
            committed = true;
            return toDelete;
        } catch (final SQLException e) {
            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (!committed) {
                Databases.rollback(con);
            }
            Databases.closeSQLStuff(stmt);
            Databases.autocommit(con);
            databaseService.backWritable(contextId, con);
        }
    }

    /**
     * Checks if such an entry exists.
     *
     * @param id The identifier
     * @return <code>true</code> if exists; otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    public boolean exists(final JSlobId id) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        int contextId = id.getContext();
        Connection con = databaseService.getReadOnly(contextId);
        try {
            return exists(id, contextId, con);
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    private boolean exists(JSlobId id, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM jsonStorage WHERE cid = ? AND user = ? AND serviceId = ? AND id = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, id.getUser());
            stmt.setString(3, id.getServiceId());
            stmt.setString(4, id.getId());
            rs = stmt.executeQuery();
            return rs.next();
        } catch (final SQLException e) {
            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public boolean store(final JSlobId id, final JSlob jslob) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        int contextId = id.getContext();
        Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO jsonStorage (cid, user, serviceId, id, data) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE data=?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, id.getUser());
            stmt.setString(3, id.getServiceId());
            stmt.setString(4, id.getId());
            setBinaryStream(jslob.getJsonObject(), stmt, 5, 6);
            int updated = stmt.executeUpdate();
            return updated == 1;
        } catch (final DataTruncation e) {
            // A BLOB can be 65535 bytes maximum.
            // If you need more consider using a MEDIUMBLOB for 16777215 bytes or a LONGBLOB for 4294967295
            OXException x = JSlobExceptionCodes.JSLOB_TOO_BIG.create(
                e, id.getId(), Integer.valueOf(contextId), Integer.valueOf(id.getUser()));
            LOG.debug("The following JSlob is too big:\n{}", jslob.getJsonObject());
            throw x;
        } catch (final SQLException e) {
            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            databaseService.backWritable(contextId, con);
        }
    }

    /**
     * Stores multiple JSlobs to database.
     *
     * @param jslobs The JSlobs to store
     * @throws OXException If store operation fails
     */
    public void storeMultiple(final Map<JSlobId, JSlob> jslobs) throws OXException {
        if (null == jslobs || jslobs.isEmpty()) {
            return;
        }

        TIntObjectMap<List<Pair<JSlobId, JSlob>>> context2Pairs = new TIntObjectHashMap<>(8);
        {

            for (final Entry<JSlobId, JSlob> entry : jslobs.entrySet()) {
                final JSlobId jSlobId = entry.getKey();
                final int contextId = jSlobId.getContext();
                List<Pair<JSlobId, JSlob>> pairs = context2Pairs.get(contextId);
                if (null == pairs) {
                    pairs = new LinkedList<>();
                    context2Pairs.put(contextId, pairs);
                }
                pairs.add(new Pair<JSlobId, JSlob>(jSlobId, entry.getValue()));
            }
        }

        TIntObjectIterator<List<Pair<JSlobId, JSlob>>> iterator = context2Pairs.iterator();
        for (int i = context2Pairs.size(); i-- > 0;) {
            iterator.advance();
            storeMultiple(iterator.key(), iterator.value());
        }
    }

    /**
     * Stores multiple JSlobs to database that share the same context.
     */
    private void storeMultiple(int contextId, List<Pair<JSlobId, JSlob>> pairs) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection con = databaseService.getWritable(contextId);
        PreparedStatement insertStmt = null;
        try {
            /*
             * Iterate...
             */
            insertStmt = con.prepareStatement("INSERT INTO jsonStorage (cid, user, serviceId, id, data) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE data=?");
            for (Pair<JSlobId, JSlob> pair : pairs) {
                JSlobId id = pair.getFirst();
                insertStmt.setLong(1, contextId);
                insertStmt.setLong(2, id.getUser());
                insertStmt.setString(3, id.getServiceId());
                insertStmt.setString(4, id.getId());
                // Formerly: insertStmt.setBinaryStream(5, new JSONInputStream(jslob.getJsonObject(), "US-ASCII"));
                setBinaryStream(pair.getSecond().getJsonObject(), insertStmt, 5, 6);
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
        } catch (SQLException e) {
            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(insertStmt);
            databaseService.backWritable(contextId, con);
        }
    }

    private DatabaseService getDatabaseService() {
        return services.getService(DatabaseService.class);
    }

    private void setBinaryStream(JSONObject jObject, PreparedStatement stmt, int... positions) throws OXException {
        try {
            JSONObject json = null != jObject ? jObject : new JSONObject(0);
            ByteArrayOutputStream buf = Streams.newByteArrayOutputStream(65536);
            json.write(new AsciiWriter(buf), true);

            if (positions.length == 1) {
                stmt.setBinaryStream(positions[0], Streams.asInputStream(buf));
            } else {
                byte[] data = buf.toByteArray();
                buf = null; // might help GC
                for (int pos : positions) {
                    stmt.setBytes(pos, data);
                }
            }
        } catch (final SQLException e) {
            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw JSlobExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
