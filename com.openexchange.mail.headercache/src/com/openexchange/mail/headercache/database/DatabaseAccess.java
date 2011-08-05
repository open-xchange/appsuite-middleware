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

import java.io.ByteArrayOutputStream;
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.headercache.Constants;
import com.openexchange.mail.headercache.services.HeaderCacheServiceRegistry;
import com.openexchange.mail.headercache.sync.SyncData;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

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
         * @throws OXException If a mail error occurs
         */
        int applyField(MailMessage mail, ResultSet rs, int pos) throws IOException, SQLException, OXException;

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

        @Override
        public int applyField(final MailMessage mail, final ResultSet rs, final int pos) throws IOException, SQLException, OXException {
            mail.setFolder(fullname);
            return pos;
        }

        @Override
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

            @Override
            public String getField() {
                return "h.flags, h.userFlags";
            }

            @Override
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

            @Override
            public String getField() {
                return "h.receivedDate";
            }

            @Override
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

            @Override
            public String getField() {
                return "h.rfc822Size";
            }

            @Override
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

            @Override
            public String getField() {
                return "h.headers";
            }

            @Override
            public int applyField(final MailMessage mail, final ResultSet rs, final int pos) throws IOException, SQLException, OXException {
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
                        final ThreadPoolService threadPoolService =
                            HeaderCacheServiceRegistry.getServiceRegistry().getService(ThreadPoolService.class);
                        if (null == threadPoolService) {
                            fillPipeDeflate(pipedOut, binaryStream);
                        } else {
                            final Callable<Object> c = new Callable<Object>() {

                                @Override
                                public Object call() throws IOException {
                                    try {
                                        fillPipeDeflate(pipedOut, binaryStream);
                                    } catch (final IOException e) {
                                        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(DatabaseAccess.class)).error(e.getMessage(), e);
                                        throw e;
                                    }
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

    private static final int CHUNK_SIZE = 5000;

    /**
     * Fills specified instances of {@link MailMessage} with available data.
     * <p>
     * Note that {@link MailMessage#getMailId()} is expected to return a non-<code>null</code> value for each {@link MailMessage} instance.
     *
     * @param mails The instances of {@link MailMessage} to fill
     * @param fields The fields to fill in instances of {@link MailMessage}
     * @throws OXException If filling the mails fails
     */
    public void fillMails(final MailMessage[] mails, final List<SetterApplier> setters) throws OXException {
        if ((null == mails) || (0 == mails.length)) {
            return;
        }

        // final long millis = System.currentTimeMillis();

        final List<MailMessage> strippedMails = stripNullValues(mails);
        final int size = strippedMails.size();
        final StringBuilder sb = new StringBuilder(size * 32);
        int fromIndex = 0;
        int toIndex;
        while (fromIndex < size) {
            final int a = fromIndex + CHUNK_SIZE;
            toIndex = (a <= size) ? a : size;
            fillMailChunk(strippedMails.subList(fromIndex, toIndex), setters, sb);
            fromIndex = toIndex;
        }

        // final long d = System.currentTimeMillis() - millis;
        // System.out.println(fullname + ": Filling " + size + " mails took " + d + "msec.");
    }

    /**
     * Fills specified instances of {@link MailMessage} with available data.
     * <p>
     * Note that {@link MailMessage#getMailId()} is expected to return a non-<code>null</code> value for each {@link MailMessage} instance.
     *
     * @param chunkedMails The instances of {@link MailMessage} to fill
     * @param fields The fields to fill in instances of {@link MailMessage}
     * @throws OXException If filling the mails fails
     */
    private void fillMailChunk(final List<MailMessage> chunkedMails, final List<SetterApplier> setters, final StringBuilder sb) throws OXException {
        if (chunkedMails.isEmpty()) {
            return;
        }
        final int len = chunkedMails.size();
        if (1 == len) {
            fillMail(chunkedMails.get(0), setters);
            return;
        }
        final DatabaseService databaseService = getDBService();
        final Connection readConnection;
        {
            readConnection = databaseService.getReadOnly(contextId);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            /*
             * Create and fire statement
             */
            {
                sb.setLength(0);
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
                sb.append(" FROM headersAsBlob AS h INNER JOIN (");
                sb.append("SELECT uuid FROM mailUUID INNER JOIN (");
                sb.append("SELECT ? AS id");
                for (int i = 1; i < len; i++) {
                    sb.append(" UNION ALL SELECT ?");
                }
                sb.append(") AS x ON mailUUID.id = x.id WHERE cid = ? AND user = ? AND account = ? AND fullname = ?");
                sb.append(") AS y ON h.uuid = y.uuid WHERE cid = ? AND user = ?");
                stmt = readConnection.prepareStatement(sb.toString());
                int pos = 1;
                for (final MailMessage mail : chunkedMails) {
                    stmt.setString(pos++, mail.getMailId());
                }
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, fullname);
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, user);
                rs = stmt.executeQuery();
            }
            /*
             * Apply to mails
             */
            int mailIndex = 0;
            int pos;
            while (rs.next()) {
                /*
                 * Fill appropriate mail
                 */
                final MailMessage mail = chunkedMails.get(mailIndex++);
                pos = 1;
                for (final SetterApplier setterApplier : setters) {
                    pos = setterApplier.applyField(mail, rs, pos);
                }
            }
        } catch (final SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
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
     * @throws OXException If filling the mail fails
     */
    public void fillMail(final MailMessage mail, final List<SetterApplier> setters) throws OXException {
        final DatabaseService databaseService = getDBService();
        final Connection readConnection;
        {
            readConnection = databaseService.getReadOnly(contextId);
        }
        try {
            fillMailInternal(mail, setters, readConnection);
        } catch (final Exception e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextId, readConnection);
        }
    }

    private void fillMailInternal(final MailMessage mail, final List<SetterApplier> setters, final Connection readConnection) throws OXException {
        final String id = mail.getMailId();
        if (null == id) {
            throw MailExceptionCode.MISSING_PARAM.create("id");
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
                throw MailExceptionCode.MAIL_NOT_FOUND.create(id, fullname);
            }
            /*
             * Apply to mail
             */
            int pos = 1;
            for (final SetterApplier setterApplier : setters) {
                pos = setterApplier.applyField(mail, rs, pos);
            }
        } catch (final SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
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

    private static final String SQL_LOAD_SYNC_DATA =
        "SELECT m.id, h.flags, h.userFlags FROM mailUUID AS m JOIN headersAsBlob As h ON" + " m.cid = ? AND h.cid = ? AND m.cid = h.cid AND m.user = h.user AND m.uuid = h.uuid" + " WHERE m.user = ? AND m.account = ? AND m.fullname = ?";

    /**
     * Loads sync data from database from specified folder in user's account.
     *
     * @return The sync data from database
     * @throws OXException If loading sync data fails
     */
    public Set<SyncData> loadSyncData() throws OXException {
        final DatabaseService databaseService = getDBService();
        final Connection rc;
        {
            rc = databaseService.getReadOnly(contextId);
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
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, rc);
        }
    } // End of loadSyncData() method

    private static final String SQL_DELETE =
        "DELETE mailUUID, headersAsBlob FROM mailUUID JOIN headersAsBlob" + " ON mailUUID.cid = ? AND headersAsBlob.cid = ? AND mailUUID.cid = headersAsBlob.cid" + " AND mailUUID.user = headersAsBlob.user AND mailUUID.uuid = headersAsBlob.uuid" + " WHERE mailUUID.user = ? AND mailUUID.account = ? AND mailUUID.fullname = ? AND mailUUID.uuid = UNHEX(REPLACE(?,'-',''))";

    /**
     * Deletes sync data from database associated with specified mail identifiers in folder denoted by given account's fullname.
     *
     * @param ids The mail identifiers to delete
     * @throws OXException If deletion fails
     */
    public void deleteSyncData(final Set<String> ids) throws OXException {
        if (null == ids || ids.isEmpty()) {
            return;
        }
        final DatabaseService databaseService = getDBService();
        final Connection wc;
        try {
            wc = databaseService.getWritable(contextId);
            wc.setAutoCommit(false); // BEGIN;
        } catch (final SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
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
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(null, stmt);
            DBUtils.autocommit(wc);
            databaseService.backWritable(contextId, wc);
        }
    } // End of deleteSyncData() method

    private static final String SQL_INSERT_UUID =
        "INSERT INTO mailUUID (cid, user, account, fullname, id, uuid)" + " VALUES (?, ?, ?, ?, ?, UNHEX(REPLACE(?,'-','')))";

    private static final String SQL_INSERT_DATA =
        "INSERT INTO headersAsBlob" + " (cid, user, uuid, flags, receivedDate, rfc822Size, userFlags, headers)" + " VALUES (?, ?, UNHEX(REPLACE(?,'-','')), ?, ?, ?, ?, ?)";

    /**
     * Inserts specified mail collection to sync data.
     *
     * @param mails The mail collection to insert
     * @throws OXException If insertion fails
     */
    public void insertSyncData(final List<MailMessage> mails) throws OXException {
        if (null == mails || mails.isEmpty()) {
            return;
        }
        final DatabaseService databaseService = getDBService();
        final Connection wc;
        {
            wc = databaseService.getWritable(contextId);
        }
        try {

            // final long s = System.currentTimeMillis();

            final int size = mails.size();
            final StringBuilder sb = new StringBuilder(size * 96);
            int fromIndex = 0;
            int toIndex;
            while (fromIndex < size) {
                final int a = fromIndex + CHUNK_SIZE;
                toIndex = (a <= size) ? a : size;
                insertSyncDataChunk(mails.subList(fromIndex, toIndex), false, sb, wc);
                fromIndex = toIndex;
            }

            // final long d = System.currentTimeMillis() - s;
            // System.out.println(fullname + ": INSERTION of " + mails.size() + " mails took " + d + "msec");

        } finally {
            databaseService.backWritable(contextId, wc);
        }
    }

    private static final String SQL_INSERT_UUID_PREFIX = "INSERT INTO mailUUID (cid, user, account, fullname, id, uuid) VALUES ";

    private static final String SQL_INSERT_UUID_VALUES = "(?, ?, ?, ?, ?, UNHEX(REPLACE(?,'-','')))";

    private static final String SQL_INSERT_DATA_PREFIX =
        "INSERT INTO headersAsBlob" + " (cid, user, uuid, flags, receivedDate, rfc822Size, userFlags, headers) VALUES ";

    private static final String SQL_INSERT_DATA_VALUES = "(?, ?, UNHEX(REPLACE(?,'-','')), ?, ?, ?, ?, ?)";

    /**
     * Inserts specified mail collection to sync data.
     *
     * @param mails The mail list to insert
     * @param wc A writable connection
     * @throws OXException If insertion fails
     */
    private void insertSyncDataChunk(final List<MailMessage> mails, final boolean batch, final StringBuilder sb, final Connection wc) throws OXException {
        if (mails.isEmpty()) {
            return;
        }
        /*
         * Begin transaction
         */
        try {
            wc.setAutoCommit(false); // BEGIN;
        } catch (final SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        PreparedStatement stmt = null;
        try {
            /*
             * Insert UUIDs
             */

            // long st = System.currentTimeMillis();

            final int size = mails.size();
            final Map<UUID, MailMessage> uuids = new HashMap<UUID, MailMessage>(size);
            if (batch) {
                stmt = wc.prepareStatement(SQL_INSERT_UUID);
                final int pos = 1;
                for (final MailMessage mail : mails) {
                    fillInsertUUIDStatement(stmt, uuids, pos, mail);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } else {
                /*
                 * Compose statement string
                 */
                sb.setLength(0);
                sb.append(SQL_INSERT_UUID_PREFIX);
                sb.append(SQL_INSERT_UUID_VALUES);
                for (int i = 1; i < size; i++) {
                    sb.append(", ").append(SQL_INSERT_UUID_VALUES);
                }
                stmt = wc.prepareStatement(sb.toString());
                /*
                 * Fill prepared statement
                 */
                int pos = 1;
                for (final MailMessage mail : mails) {
                    pos = fillInsertUUIDStatement(stmt, uuids, pos, mail);
                }
                stmt.executeUpdate();
            }

            // long d = System.currentTimeMillis() - st;
            // System.out.println((batch ? "Batch" : "Serial") + " inserting " + size + " UUIDs took " + d + "msec");

            DBUtils.closeSQLStuff(stmt);
            /*
             * Completion service
             */
            final Set<Entry<UUID, MailMessage>> entrySet = uuids.entrySet();
            final CompletionService<InputStream> completionService;
            {
                final ThreadPoolService tps = HeaderCacheServiceRegistry.getServiceRegistry().getService(ThreadPoolService.class, true);
                completionService = new ThreadPoolCompletionService<InputStream>(tps);
                for (final Entry<UUID, MailMessage> entry : entrySet) {
                    completionService.submit(new InputStreamCallable(entry.getValue()));
                }
            }
            /*
             * Insert data
             */

            // st = System.currentTimeMillis();

            if (batch) {
                stmt = wc.prepareStatement(SQL_INSERT_DATA);
                final int pos = 1;
                for (final Entry<UUID, MailMessage> entry : entrySet) {
                    fillInsertDataStatement(stmt, completionService, pos, entry);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } else {
                /*
                 * Compose statement string
                 */
                sb.setLength(0);
                sb.append(SQL_INSERT_DATA_PREFIX);
                sb.append(SQL_INSERT_DATA_VALUES);
                for (int i = 1; i < size; i++) {
                    sb.append(", ").append(SQL_INSERT_DATA_VALUES);
                }
                stmt = wc.prepareStatement(sb.toString());
                /*
                 * Fill prepared statement
                 */
                int pos = 1;
                for (final Entry<UUID, MailMessage> entry : entrySet) {
                    pos = fillInsertDataStatement(stmt, completionService, pos, entry);
                }
                stmt.executeUpdate();
            }

            // d = System.currentTimeMillis() - st;
            // System.out.println((batch ? "Batch" : "Serial") + " inserting " + size + " mail-data-sets took " + d + "msec");

            wc.commit(); // COMMIT
        } catch (final SQLException e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            DBUtils.autocommit(wc);
        }
    } // End of insertSyncData() method

    private int fillInsertUUIDStatement(final PreparedStatement stmt, final Map<UUID, MailMessage> uuids, final int position, final MailMessage mail) throws SQLException {
        int pos = position;
        stmt.setInt(pos++, contextId);
        stmt.setInt(pos++, user);
        stmt.setInt(pos++, accountId);
        stmt.setString(pos++, fullname);
        stmt.setString(pos++, mail.getMailId());
        final UUID uuid = UUID.randomUUID();
        stmt.setString(pos++, uuid.toString());
        uuids.put(uuid, mail);
        return pos;
    }

    private int fillInsertDataStatement(final PreparedStatement stmt, final CompletionService<InputStream> completionService, final int position, final Entry<UUID, MailMessage> entry) throws SQLException, InterruptedException, ExecutionException, IOException {
        int pos = position;
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
            final long msize = mail.getSize();
            stmt.setLong(pos++, msize < 0 ? 0 : msize); // rfc822Size
        }
        {
            final String[] userFlags = mail.getUserFlags();
            if (null == userFlags || userFlags.length <= 0) {
                stmt.setNull(pos++, Types.VARCHAR); // userFlags
            } else {
                final int len = userFlags.length;
                final StringBuilder usb = new StringBuilder(len * 8);
                usb.append(userFlags[0]);
                for (int i = 1; i < len; i++) {
                    usb.append(',').append(userFlags[i]);
                }
                if (usb.length() > MAX_USER_FLAGS) {
                    stmt.setString(pos++, usb.substring(0, 1024)); // userFlags
                } else {
                    stmt.setString(pos++, usb.toString()); // userFlags
                }
            }
        }
        {
            /*
             * Get input stream
             */
            final InputStream in = completionService.take().get();
            /*
             * Set binary as a InputStream
             */
            stmt.setBinaryStream(pos++, in, in.available()); // headers
        }
        return pos;
    }

    /**
     * Fills specified {@link PipedOutputStream} instance.
     *
     * @param pipedOutputStream The piped output stream
     * @param headers The headers' bytes
     * @throws IOException If an I/O error occurs
     */
    static void fillPipedInflate(final PipedOutputStream pipedOutputStream, final byte[] headers) throws IOException {
        final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(pipedOutputStream);
        try {
            gzipOutputStream.write(headers);
            gzipOutputStream.flush();
        } finally {
            closeQuietly(gzipOutputStream);
            closeQuietly(pipedOutputStream);
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
                com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(DatabaseAccess.class)).error(e.getMessage(), e);
            }
        }
    }

    /*-
     * TODO: Join?
     * UPDATE TABLE_1 LEFT JOIN TABLE_2 ON TABLE_1.COLUMN_1= TABLE_2.COLUMN_2
     * SET TABLE_1.COLUMN = EXPR WHERE TABLE_2.COLUMN2 IS NULL
     */

    private static final String SQL_UPDATE =
        "UPDATE headersAsBlob SET flags = ? WHERE cid = ? AND user = ? AND uuid =" + " (SELECT uuid FROM mailUUID WHERE cid = ? AND user = ? AND account = ? AND fullname = ? AND id = ?)";

    /**
     * Updates specified sync data.
     *
     * @param col The sync data to update
     * @param fullname The folder fullname
     * @param accountId The account identifier
     * @param user The user identifier
     * @param contextId The context identifier
     * @throws OXException If update fails
     */
    public void updateSyncData(final Collection<SyncData> col) throws OXException {
        if (null == col || col.isEmpty()) {
            return;
        }
        final DatabaseService databaseService = getDBService();
        final Connection wc;
        try {
            wc = databaseService.getWritable(contextId);
            wc.setAutoCommit(false); // BEGIN;
        } catch (final SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
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
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            DBUtils.autocommit(wc);
            databaseService.backWritable(contextId, wc);
        }
    } // End of updateSyncData() method

    private static DatabaseService getDBService() throws OXException {
        final DatabaseService databaseService;
        try {
            databaseService = HeaderCacheServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
        } catch (final OXException e) {
            throw new OXException(e);
        }
        return databaseService;
    }

    private static List<MailMessage> stripNullValues(final MailMessage[] mails) {
        final List<MailMessage> l = new ArrayList<MailMessage>(mails.length);
        for (int i = 0; i < mails.length; i++) {
            final MailMessage m = mails[i];
            if (null != m) {
                l.add(m);
            }
        }
        return l;
    }

    private static final int BUFSIZE = 0x800;

    private static final class InputStreamCallable implements Callable<InputStream> {

        final MailMessage mail;

        InputStreamCallable(final MailMessage mail) {
            super();
            this.mail = mail;
        }

        @Override
        public InputStream call() throws Exception {
            final ByteArrayOutputStream sink = new UnsynchronizedByteArrayOutputStream(BUFSIZE);
            {
                final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(sink);
                try {
                    gzipOutputStream.write(mail.getHeaders().toString().getBytes("US-ASCII"));
                    gzipOutputStream.flush();
                } finally {
                    closeQuietly(gzipOutputStream);
                }
            }
            return new UnsynchronizedByteArrayInputStream(sink.toByteArray());
        }

    }

}
