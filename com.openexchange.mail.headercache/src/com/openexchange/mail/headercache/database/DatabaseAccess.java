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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail.headercache.database;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DatabaseService;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.headercache.Constants;
import com.openexchange.mail.headercache.services.HeaderCacheServiceRegistry;
import com.openexchange.mail.headercache.sync.SyncData;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.server.ServiceException;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link DatabaseAccess} - Database access for header cache.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DatabaseAccess {

    /**
     * Creates a new instance of {@link DatabaseAccess}.
     * 
     * @param fullname The folder fullname
     * @param accountId The account identifier
     * @param user The user identifier
     * @param contextId The context identifier
     * @return A new {@link DatabaseAccess} instance
     */
    public static DatabaseAccess newInstance(final String fullname, final int accountId, final int user, final int contextId) {
        return new DatabaseAccess(fullname, accountId, user, contextId);
    }

    private static final int MAX_USER_FLAGS = 1024;

    /*-
     * Member section
     */

    private final String fullname;

    private final int accountId;

    private final int user;

    private final int contextId;

    /**
     * Initializes a new {@link DatabaseAccess}.
     * 
     * @param fullname The folder fullname
     * @param accountId The account identifier
     * @param user The user identifier
     * @param contextId The context identifier
     */
    private DatabaseAccess(final String fullname, final int accountId, final int user, final int contextId) {
        super();
        this.accountId = accountId;
        this.contextId = contextId;
        this.fullname = fullname;
        this.user = user;
    }

    /**
     * Sets appropriate field name and applies value to given {@link MailMessage} instance.
     */
    public static interface SetterApplier {

        /**
         * Gets appropriate field name.
         * 
         * @return Appropriate field name
         */
        String getField();

        /**
         * Applies read value to given {@link MailMessage} instance.
         * 
         * @param mail The mail which shall be filled
         * @param rs The result set to read from
         * @param pos The current position to read
         * @return The next position to read
         * @throws IOException If an I/O error occurs
         * @throws SQLException If a SQL error occurs
         * @throws MailException If a mail error occurs
         */
        int applyField(MailMessage mail, ResultSet rs, int pos) throws IOException, SQLException, MailException;

    }

    /**
     * The {@link SetterApplier} implementation to apply folder fullname.
     */
    public static final class FolderSetterApplier implements SetterApplier {

        private final String fullname;

        /**
         * Initializes a new {@link FolderSetterApplier}.
         * 
         * @param fullname The folder fullname
         */
        public FolderSetterApplier(final String fullname) {
            super();
            this.fullname = fullname;
        }

        public int applyField(final MailMessage mail, final ResultSet rs, final int pos) throws IOException, SQLException, MailException {
            mail.setFolder(fullname);
            return pos;
        }

        public String getField() {
            return null;
        }

    }

    /**
     * The pattern to split a CSV.
     */
    static final Pattern SPLIT = Pattern.compile(" *, *");

    private static EnumMap<MailField, SetterApplier> MAP;

    static {
        MAP = new EnumMap<MailField, SetterApplier>(MailField.class);

        MAP.put(MailField.FLAGS, new SetterApplier() {

            public String getField() {
                return "h.flags, h.userFlags";
            }

            public int applyField(final MailMessage mail, final ResultSet rs, final int pos) throws SQLException {
                int p = pos;
                // System flags
                mail.setFlags(rs.getInt(p++));
                // User flags
                final String userFlagsStr = rs.getString(p++);
                if (null != userFlagsStr) {
                    mail.addUserFlags(SPLIT.split(userFlagsStr, 0));
                }
                return p;
            }
        });

        MAP.put(MailField.RECEIVED_DATE, new SetterApplier() {

            public String getField() {
                return "h.receivedDate";
            }

            public int applyField(final MailMessage mail, final ResultSet rs, final int pos) throws SQLException {
                int p = pos;
                final long recDate = rs.getLong(p++);
                if (!rs.wasNull()) {
                    mail.setReceivedDate(new Date(recDate));
                }
                return p;
            }
        });

        MAP.put(MailField.SIZE, new SetterApplier() {

            public String getField() {
                return "h.rfc822Size";
            }

            public int applyField(final MailMessage mail, final ResultSet rs, final int pos) throws SQLException {
                int p = pos;
                final long size = rs.getLong(p++);
                if (!rs.wasNull()) {
                    mail.setSize(size);
                }
                return p;
            }

        });

        MAP.put(MailField.HEADERS, new SetterApplier() {

            public String getField() {
                return "h.headers";
            }

            public int applyField(final MailMessage mail, final ResultSet rs, final int pos) throws IOException, SQLException,
                    MailException {
                int p = pos;
                final InputStream binaryStream = rs.getBinaryStream(p++);
                if (null != binaryStream) {
                    /*
                     * Create a pipe
                     */
                    final PipedOutputStream pipedOut = new PipedOutputStream();
                    final PipedInputStream pipedIn = new PipedInputStream(pipedOut);
                    /*
                     * read PipedInputStream depending on ThreadPoolService presence
                     */
                    try {
                        final ThreadPoolService threadPoolService = HeaderCacheServiceRegistry.getServiceRegistry().getService(
                                ThreadPoolService.class);
                        if (null == threadPoolService) {
                            fillPipeDeflate(pipedOut, binaryStream);
                        } else {
                            final Callable<Object> c = new Callable<Object>() {

                                public Object call() throws IOException {
                                    fillPipeDeflate(pipedOut, binaryStream);
                                    return null;
                                }
                            };
                            threadPoolService.submit(ThreadPools.task(c), CallerRunsBehavior.getInstance());
                        }
                        /*
                         * Set mail headers
                         */
                        mail.addHeaders(new HeaderCollection(pipedIn));
                    } finally {
                        closeQuietly(pipedOut);
                        closeQuietly(pipedIn);
                        closeQuietly(binaryStream);
                    }
                }
                return p;
            }
        });
    }

    private static final MailFields HEADER_MAIL_FIELDS = Constants.getHeaderMailFields();

    /**
     * Gets the {@link SetterApplier} list for specified mail fields.
     * 
     * @param fields The mail fields
     * @return The {@link SetterApplier} list for specified mail fields
     */
    public static List<SetterApplier> getSetterApplierList(final MailFields fields) {
        /*
         * Generate list
         */
        final List<SetterApplier> setters = new ArrayList<SetterApplier>(4);
        if (fields.contains(MailField.FLAGS)) {
            setters.add(MAP.get(MailField.FLAGS));
        }
        if (fields.contains(MailField.RECEIVED_DATE)) {
            setters.add(MAP.get(MailField.RECEIVED_DATE));
        }
        if (fields.contains(MailField.SIZE)) {
            setters.add(MAP.get(MailField.SIZE));
        }
        if (fields.containsAny(HEADER_MAIL_FIELDS)) {
            setters.add(MAP.get(MailField.HEADERS));
        }
        return setters;
    }

    // private static final String SQL_LOAD = "SELECT h.flags, h.userFlags, h.receivedDate, h.rfc822Size, h.headers FROM mailUUID AS m"
    // + " JOIN headersAsBlob As h ON m.cid = ? AND h.cid = ? AND m.cid = h.cid AND m.user = h.user AND m.uuid = h.uuid"
    // + " WHERE m.user = ? AND m.account = ? AND m.fullname = ? AND m.id = ?";

    /**
     * Fills specified instances of {@link MailMessage} with available data.
     * <p>
     * Note that {@link MailMessage#getMailId()} is expected to return a non-<code>null</code> value for each {@link MailMessage} instance.
     * 
     * @param mails The instances of {@link MailMessage} to fill
     * @param fields The fields to fill in instances of {@link MailMessage}
     * @throws MailException If filling the mails fails
     */
    public void fillMails(final MailMessage[] mails, final List<SetterApplier> setters) throws MailException {
        final DatabaseService databaseService = getDBService();
        final Connection readConnection;
        try {
            readConnection = databaseService.getReadOnly(contextId);
        } catch (final DBPoolingException e) {
            throw new MailException(e);
        }
        try {
            for (final MailMessage mail : mails) {
                if (null != mail) {
                    fillMailInternal(mail, setters, readConnection);
                }
            }
        } catch (final Exception e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextId, readConnection);
        }
    }

    /**
     * Fills specified instance of {@link MailMessage} with available data.
     * <p>
     * Note that {@link MailMessage#getMailId()} is expected to return a non-<code>null</code> value.
     * 
     * @param mail The instance of {@link MailMessage} to fill
     * @param fields The fields to fill in instance of {@link MailMessage}
     * @throws MailException If filling the mail fails
     */
    public void fillMail(final MailMessage mail, final List<SetterApplier> setters) throws MailException {
        final DatabaseService databaseService = getDBService();
        final Connection readConnection;
        try {
            readConnection = databaseService.getReadOnly(contextId);
        } catch (final DBPoolingException e) {
            throw new MailException(e);
        }
        try {
            fillMailInternal(mail, setters, readConnection);
        } catch (final Exception e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextId, readConnection);
        }
    }

    private void fillMailInternal(final MailMessage mail, final List<SetterApplier> setters, final Connection readConnection)
            throws MailException {
        final String id = mail.getMailId();
        if (null == id) {
            throw new MailException(MailException.Code.MISSING_PARAM, "id");
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            /*
             * Create and fire statement
             */
            {
                final StringBuilder sb = new StringBuilder(256);
                sb.append("SELECT ");
                {
                    final String name = setters.get(0).getField();
                    if (null != name) {
                        sb.append(name);
                    }
                }
                {
                    final int size = setters.size();
                    final String delim = ", ";
                    for (int i = 1; i < size; i++) {
                        final String name = setters.get(i).getField();
                        if (null != name) {
                            sb.append(delim).append(name);
                        }
                    }
                }
                sb.append(" FROM mailUUID AS m JOIN headersAsBlob As h ON m.cid = ? AND h.cid = ?");
                sb.append(" AND m.cid = h.cid AND m.user = h.user AND m.uuid = h.uuid");
                sb.append(" WHERE m.user = ? AND m.account = ? AND m.fullname = ? AND m.id = ?");
                stmt = readConnection.prepareStatement(sb.toString());
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, fullname);
                stmt.setString(pos++, id);
                rs = stmt.executeQuery();
            }
            if (!rs.next()) {
                throw new MailException(MailException.Code.MAIL_NOT_FOUND, id, fullname);
            }
            /*
             * Apply to mail
             */
            int pos = 1;
            for (final SetterApplier setterApplier : setters) {
                pos = setterApplier.applyField(mail, rs, pos);
            }
        } catch (final SQLException e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        } catch (final Exception e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Fill specified pipe with given input stream's data.
     * 
     * @param pipedOut The pipe to write to
     * @param in The input stream whose data shall be written
     * @throws IOException If an I/O error occurs
     */
    static void fillPipeDeflate(final PipedOutputStream pipedOut, final InputStream in) throws IOException {
        final GZIPInputStream gzipIn = new GZIPInputStream(in);
        try {
            final byte[] buf = new byte[2048];
            int read;
            while ((read = gzipIn.read(buf, 0, buf.length)) >= 0) {
                pipedOut.write(buf, 0, read);
            }
        } finally {
            closeQuietly(gzipIn);
            closeQuietly(pipedOut);
        }
    }

    private static final String SQL_LOAD_SYNC_DATA = "SELECT m.id, h.flags, h.userFlags FROM mailUUID AS m JOIN headersAsBlob As h ON"
            + " m.cid = ? AND h.cid = ? AND m.cid = h.cid AND m.user = h.user AND m.uuid = h.uuid"
            + " WHERE m.user = ? AND m.account = ? AND m.fullname = ?";

    /**
     * Loads sync data from database from specified folder in user's account.
     * 
     * @return The sync data from database
     * @throws MailException If loading sync data fails
     */
    public Set<SyncData> loadSyncData() throws MailException {
        final DatabaseService databaseService = getDBService();
        final Connection rc;
        try {
            rc = databaseService.getReadOnly(contextId);
        } catch (final DBPoolingException e) {
            throw new MailException(e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = rc.prepareStatement(SQL_LOAD_SYNC_DATA);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, fullname);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptySet();
            }
            final int posId = 1;
            final int posFlags = 2;
            final int posUserFlags = 3;
            final Set<SyncData> retval = new HashSet<SyncData>(128);
            do {
                final Collection<? extends CharSequence> userFlags;
                {
                    final String userFlagsStr = rs.getString(posUserFlags);
                    userFlags = (null == userFlagsStr) ? null : Arrays.asList(SPLIT.split(userFlagsStr, 0));
                }
                retval.add(SyncData.newInstance(rs.getString(posId), rs.getInt(posFlags), userFlags));
            } while (rs.next());
            return retval;
        } catch (final SQLException e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } catch (final Exception e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, rc);
        }
    } // End of loadSyncData() method

    private static final String SQL_DELETE = "DELETE mailUUID, headersAsBlob FROM mailUUID JOIN headersAsBlob"
            + " ON mailUUID.cid = ? AND headersAsBlob.cid = ? AND mailUUID.cid = headersAsBlob.cid"
            + " AND mailUUID.user = headersAsBlob.user AND mailUUID.uuid = headersAsBlob.uuid"
            + " WHERE mailUUID.user = ? AND mailUUID.account = ? AND mailUUID.fullname = ? AND mailUUID.uuid = UNHEX(REPLACE(?,'-',''))";

    /**
     * Deletes sync data from database associated with specified mail identifiers in folder denoted by given account's fullname.
     * 
     * @param ids The mail identifiers to delete
     * @throws MailException If deletion fails
     */
    public void deleteSyncData(final Set<String> ids) throws MailException {
        if (null == ids || ids.isEmpty()) {
            return;
        }
        final DatabaseService databaseService = getDBService();
        final Connection wc;
        try {
            wc = databaseService.getWritable(contextId);
            wc.setAutoCommit(false); // BEGIN;
        } catch (final DBPoolingException e) {
            throw new MailException(e);
        } catch (final SQLException e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        }
        PreparedStatement stmt = null;
        try {
            stmt = wc.prepareStatement(SQL_DELETE);
            int pos;
            for (final String id : ids) {
                pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, fullname);
                stmt.setString(pos++, id);
                stmt.addBatch();
            }
            stmt.executeBatch();
            wc.commit(); // COMMIT
        } catch (final SQLException e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } catch (final Exception e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(null, stmt);
            DBUtils.autocommit(wc);
            databaseService.backWritable(contextId, wc);
        }
    } // End of deleteSyncData() method

    private static final String SQL_INSERT_UUID = "INSERT INTO mailUUID (cid, user, account, fullname, id, uuid)"
            + " VALUES (?, ?, ?, ?, ?, UNHEX(REPLACE(?,'-','')))";

    private static final String SQL_INSERT_DATA = "INSERT INTO headersAsBlob"
            + " (cid, user, uuid, flags, receivedDate, rfc822Size, userFlags, headers)"
            + " VALUES (?, ?, UNHEX(REPLACE(?,'-','')), ?, ?, ?, ?, ?)";

    /**
     * Inserts specified mail collection to sync data.
     * 
     * @param mails The mail collection to insert
     * @throws MailException If insertion fails
     */
    public void insertSyncData(final Collection<MailMessage> mails) throws MailException {
        if (null == mails || mails.isEmpty()) {
            return;
        }
        final DatabaseService databaseService = getDBService();
        final Connection wc;
        try {
            wc = databaseService.getWritable(contextId);
            wc.setAutoCommit(false); // BEGIN;
        } catch (final DBPoolingException e) {
            throw new MailException(e);
        } catch (final SQLException e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        }
        PreparedStatement stmt = null;
        try {
            /*
             * Insert UUID
             */
            stmt = wc.prepareStatement(SQL_INSERT_UUID);
            final Map<UUID, MailMessage> uuids = new HashMap<UUID, MailMessage>(mails.size());
            int pos;
            for (final MailMessage mail : mails) {
                pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, fullname);
                stmt.setString(pos++, mail.getMailId());
                final UUID uuid = UUID.randomUUID();
                stmt.setString(pos++, uuid.toString());
                stmt.addBatch();
                uuids.put(uuid, mail);
            }
            stmt.executeBatch();
            DBUtils.closeSQLStuff(stmt);
            /*
             * Insert data
             */
            for (final Entry<UUID, MailMessage> entry : uuids.entrySet()) {
                stmt = wc.prepareStatement(SQL_INSERT_DATA);
                pos = 1;
                stmt.setInt(pos++, contextId); // cid
                stmt.setInt(pos++, user); // user
                stmt.setString(pos++, entry.getKey().toString()); // uuid
                final MailMessage mail = entry.getValue();
                stmt.setInt(pos++, mail.getFlags()); // flags
                {
                    final Date receivedDate = mail.getReceivedDate();
                    if (null == receivedDate) {
                        stmt.setNull(pos++, Types.BIGINT); // receivedDate
                    } else {
                        stmt.setLong(pos++, receivedDate.getTime()); // receivedDate
                    }
                }
                {
                    final long size = mail.getSize();
                    stmt.setLong(pos++, size < 0 ? 0 : size); // rfc822Size
                }
                {
                    final String[] userFlags = mail.getUserFlags();
                    if (null == userFlags || userFlags.length <= 0) {
                        stmt.setNull(pos++, Types.VARCHAR); // userFlags
                    } else {
                        final int len = userFlags.length;
                        final StringBuilder sb = new StringBuilder(len * 8);
                        sb.append(userFlags[0]);
                        for (int i = 1; i < len; i++) {
                            sb.append(',').append(userFlags[i]);
                        }
                        if (sb.length() > MAX_USER_FLAGS) {
                            stmt.setString(pos++, sb.substring(0, 1024)); // userFlags
                        } else {
                            stmt.setString(pos++, sb.toString()); // userFlags
                        }
                    }
                }
                {
                    final byte[] headers = mail.getHeaders().toString().getBytes("US-ASCII");
                    final int length = headers.length;
                    /*
                     * Create a pipe
                     */
                    final PipedOutputStream pipedOut = new PipedOutputStream();
                    final PipedInputStream pipedIn = new PipedInputStream(pipedOut);
                    try {
                        /*
                         * Fill PipedOutputStream depending on ThreadPoolService presence
                         */
                        final ThreadPoolService threadPoolService = HeaderCacheServiceRegistry.getServiceRegistry().getService(
                                ThreadPoolService.class);
                        if (null == threadPoolService) {
                            fillPipedInflate(pipedOut, headers, length);
                        } else {
                            final Callable<Object> c = new Callable<Object>() {

                                public Object call() throws IOException {
                                    fillPipedInflate(pipedOut, headers, length);
                                    return null;
                                }
                            };
                            threadPoolService.submit(ThreadPools.task(c), CallerRunsBehavior.getInstance());
                        }
                        /*
                         * Set binary as a PipedInputStream linked to previously created PipedOutputStream
                         */
                        stmt.setBinaryStream(pos++, pipedIn, length); // headers
                        /*
                         * Execute update prior to closing (stream) resources
                         */
                        stmt.executeUpdate();
                    } finally {
                        closeQuietly(pipedOut);
                        closeQuietly(pipedIn);
                    }
                }
                /*
                 * Close statement before next loop
                 */
                DBUtils.closeSQLStuff(stmt);
            }
            wc.commit(); // COMMIT
        } catch (final SQLException e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } catch (final IOException e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        } catch (final Exception e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            DBUtils.autocommit(wc);
            databaseService.backWritable(contextId, wc);
        }
    } // End of insertSyncData() method

    /**
     * Fills specified {@link PipedOutputStream} instance.
     * 
     * @param pipedOutputStream The piped output stream
     * @param headers The headers' bytes
     * @param length The bytes' length
     * @throws IOException If an I/O error occurs
     */
    static void fillPipedInflate(final PipedOutputStream pipedOutputStream, final byte[] headers, final int length) throws IOException {
        final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(pipedOutputStream, length);
        try {
            gzipOutputStream.write(headers, 0, length);
            gzipOutputStream.flush();
        } finally {
            closeQuietly(gzipOutputStream);
        }
    }

    /**
     * Closes specified {@link Closeable} instance quietly.
     * 
     * @param closeable The {@link Closeable} instance to close
     */
    static void closeQuietly(final Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (final IOException e) {
                org.apache.commons.logging.LogFactory.getLog(DatabaseAccess.class).error(e.getMessage(), e);
            }
        }
    }

    /*-
     * TODO: Join?
     * UPDATE TABLE_1 LEFT JOIN TABLE_2 ON TABLE_1.COLUMN_1= TABLE_2.COLUMN_2
     * SET TABLE_1.COLUMN = EXPR WHERE TABLE_2.COLUMN2 IS NULL
     */

    private static final String SQL_UPDATE = "UPDATE headersAsBlob SET flags = ? WHERE cid = ? AND user = ? AND uuid ="
            + " (SELECT uuid FROM mailUUID WHERE cid = ? AND user = ? AND account = ? AND fullname = ? AND id = ?)";

    /**
     * Updates specified sync data.
     * 
     * @param col The sync data to update
     * @param fullname The folder fullname
     * @param accountId The account identifier
     * @param user The user identifier
     * @param contextId The context identifier
     * @throws MailException If update fails
     */
    public void updateSyncData(final Collection<SyncData> col) throws MailException {
        if (null == col || col.isEmpty()) {
            return;
        }
        final DatabaseService databaseService = getDBService();
        final Connection wc;
        try {
            wc = databaseService.getWritable(contextId);
            wc.setAutoCommit(false); // BEGIN;
        } catch (final DBPoolingException e) {
            throw new MailException(e);
        } catch (final SQLException e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        }
        PreparedStatement stmt = null;
        try {
            stmt = wc.prepareStatement(SQL_UPDATE);
            int pos;
            for (final SyncData syncData : col) {
                pos = 1;
                stmt.setInt(pos++, syncData.getFlags());
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, fullname);
                stmt.setString(pos++, syncData.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
            wc.commit(); // COMMIT
        } catch (final SQLException e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } catch (final Exception e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            DBUtils.autocommit(wc);
            databaseService.backWritable(contextId, wc);
        }
    } // End of updateSyncData() method

    private static DatabaseService getDBService() throws MailException {
        final DatabaseService databaseService;
        try {
            databaseService = HeaderCacheServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
        } catch (final ServiceException e) {
            throw new MailException(e);
        }
        return databaseService;
    }

}
