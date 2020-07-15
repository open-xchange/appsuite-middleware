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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.compose.impl.attachment.rdb;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.slf4j.Logger;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.compose.AttachmentStorageIdentifier;
import com.openexchange.mail.compose.AttachmentStorageType;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaces;
import com.openexchange.mail.compose.DataProvider;
import com.openexchange.mail.compose.KnownAttachmentStorageType;
import com.openexchange.mail.compose.impl.attachment.AbstractNonCryptoAttachmentStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.uploaddir.UploadDirService;

/**
 * {@link RdbAttachmentStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class RdbAttachmentStorage extends AbstractNonCryptoAttachmentStorage {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbAttachmentStorage.class);
    }

    private static final int IN_MEMORY_THRESHOLD = 512000;

    /**
     * Initializes a new {@link RdbAttachmentStorage}.
     *
     * @param services The service look-up
     */
    public RdbAttachmentStorage(ServiceLookup services) {
        super(services);
    }

    @Override
    public AttachmentStorageType getStorageType() {
        return KnownAttachmentStorageType.DATABASE;
    }

    @Override
    public boolean isApplicableFor(CapabilitySet capabilities, Session session) throws OXException {
        return true;
    }

    @Override
    protected DataProvider getDataProviderFor(AttachmentStorageIdentifier storageIdentifier, Session session) throws OXException {
        return new RdbDataProvider(session, storageIdentifier.getIdentifier(), services);
    }

    @Override
    protected AttachmentStorageIdentifier saveData(InputStream input, long size, Session session) throws OXException {
        if (null == input) {
            throw CompositionSpaceErrorCode.ERROR.create("Attempted attachment storage without an input stream");
        }

        int contextId = session.getContextId();
        DatabaseService databaseService = requireDatabaseService();
        int rollback = 0;
        Connection con = databaseService.getWritable(contextId);
        try {
            Databases.startTransaction(con);
            rollback = 1;

            AttachmentStorageIdentifier retval = saveData(input, session, con);

            con.commit();
            rollback = 2;
            return retval;
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
            databaseService.backWritable(contextId, con);
        }
    }

    private AttachmentStorageIdentifier saveData(InputStream input, Session session, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            long maxAllowedPacketSize = Databases.getMaxAllowedPacketSize(con);
            if (maxAllowedPacketSize > 0) {
                // Keep a buffer for non-binary portion of the package
                maxAllowedPacketSize = (long) (maxAllowedPacketSize * 0.66);
            }

            // Generate identifier for the attachment
            UUID uuid = UUID.randomUUID();
            byte[] uuidBytes = UUIDs.toByteArray(uuid);

            // Insert data dependent on 'max_allowed_packet' setting
            if (maxAllowedPacketSize > 0) {
                int buflen = (int) Math.min(0xFFFF, maxAllowedPacketSize);
                byte[] buf = new byte[buflen];
                int inMemoryThreshold = IN_MEMORY_THRESHOLD;

                File tempFile = null;
                OutputStream out = null;
                long count = 0;
                try {
                    int chunkPos = 1;

                    ByteArrayOutputStream baos = Streams.newByteArrayOutputStream(0xFFFF);
                    out = baos;
                    long chunkSize = maxAllowedPacketSize;

                    for (int read; (read = input.read(buf, 0, buflen)) > 0;) {
                        chunkSize = chunkSize - read;
                        if (chunkSize < 0) {
                            // Flush current chunk
                            out = flushAndClose(out);
                            if (tempFile != null) {
                                insertChunkFromTempFile(tempFile, chunkPos, uuid, session, con);
                                tempFile = deleteFile(tempFile);
                            } else {
                                insertChunkFromBaos(baos, chunkPos, uuid, session, con);
                                baos = null;
                            }
                            chunkPos++;
                            baos = Streams.newByteArrayOutputStream(0xFFFF);
                            out = baos;
                            chunkSize = maxAllowedPacketSize;
                            count = 0;
                        }

                        count += read;
                        if ((null == tempFile) && (count > inMemoryThreshold) && baos != null) {
                            // Switch to file-backed output stream since in-memory threshold is exceeded
                            tempFile = newTempFile(false, services);
                            out = new FileOutputStream(tempFile);
                            baos.writeTo(out);
                            baos = null;
                        }
                        out.write(buf, 0, read);
                    }

                    if (count > 0) {
                        // Flush last chunk
                        out = flushAndClose(out);
                        if (tempFile != null) {
                            insertChunkFromTempFile(tempFile, chunkPos, uuid, session, con);
                            tempFile = deleteFile(tempFile);
                        } else {
                            insertChunkFromBaos(baos, chunkPos, uuid, session, con);
                            baos = null;
                        }
                    }
                } catch (IOException e) {
                    throw CompositionSpaceErrorCode.IO_ERROR.create(e, e.getMessage());
                } finally {
                    Streams.close(out);
                    tempFile = deleteFile(tempFile);
                }
            } else {
                stmt = con.prepareStatement("INSERT INTO compositionSpaceAttachmentBinaryChunk (uuid, cid, user, chunk, data) VALUES (?, ?, ?, 1, ?)");
                stmt.setBytes(1, uuidBytes);
                stmt.setInt(2, session.getContextId());
                stmt.setInt(3, session.getUserId());
                stmt.setBinaryStream(4, input);
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }

            stmt = con.prepareStatement("INSERT INTO compositionSpaceAttachmentBinary (uuid, cid, user) VALUES (?, ?, ?)");
            stmt.setBytes(1, uuidBytes);
            stmt.setInt(2, session.getContextId());
            stmt.setInt(3, session.getUserId());
            stmt.executeUpdate();

            return new AttachmentStorageIdentifier(UUIDs.getUnformattedString(uuid));
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static void insertChunkFromTempFile(File tempFile, int chunkPos, UUID uuid, Session session, Connection con) throws IOException, OXException {
        FileInputStream fis = null;
        PreparedStatement stmt = null;
        try {
            fis = new FileInputStream(tempFile);
            stmt = con.prepareStatement("INSERT INTO compositionSpaceAttachmentBinaryChunk (uuid, cid, user, chunk, data) VALUES (?, ?, ?, ?, ?)");
            stmt.setBytes(1, UUIDs.toByteArray(uuid));
            stmt.setInt(2, session.getContextId());
            stmt.setInt(3, session.getUserId());
            stmt.setInt(4, chunkPos);
            stmt.setBinaryStream(5, fis);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (Databases.isPacketTooBigException(e)) {
                throw OXException.general("Encountered \"package too big\" SQL error while trying to transfer " + tempFile.length() + " bytes of binary data.", e);
            }
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            Streams.close(fis);
        }
    }

    private static void insertChunkFromBaos(ByteArrayOutputStream baos, int chunkPos, UUID uuid, Session session, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO compositionSpaceAttachmentBinaryChunk (uuid, cid, user, chunk, data) VALUES (?, ?, ?, ?, ?)");
            stmt.setBytes(1, UUIDs.toByteArray(uuid));
            stmt.setInt(2, session.getContextId());
            stmt.setInt(3, session.getUserId());
            stmt.setInt(4, chunkPos);
            stmt.setBinaryStream(5, Streams.asInputStream(baos));
        } catch (SQLException e) {
            if (Databases.isPacketTooBigException(e)) {
                throw OXException.general("Encountered \"package too big\" SQL error while trying to transfer " + baos.size() + " bytes of binary data.", e);
            }
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    protected boolean deleteData(AttachmentStorageIdentifier storageIdentifier, Session session) throws OXException {
        if (storageIdentifier == null || Strings.isEmpty(storageIdentifier.getIdentifier())) {
            throw CompositionSpaceErrorCode.ERROR.create("Attempted attachment deletion without an identifier");
        }

        DatabaseService databaseService = requireDatabaseService();
        int rollback = 0;
        Connection con = databaseService.getWritable(session.getContextId());
        try {
            Databases.startTransaction(con);
            rollback = 1;

            boolean deleted = deleteData(storageIdentifier.getIdentifier(), session, con);

            con.commit();
            rollback = 2;
            return deleted;
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
            databaseService.backWritable(session.getContextId(), con);
        }
    }

    private boolean deleteData(String storageIdentifier, Session session, Connection con) throws OXException {
        UUID uuid = CompositionSpaces.parseAttachmentIdIfValid(storageIdentifier);
        if (null == uuid) {
            return false;
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM compositionSpaceAttachmentBinaryChunk WHERE uuid=? AND cid=? AND user=?");
            stmt.setBytes(1, UUIDs.toByteArray(uuid));
            stmt.setInt(2, session.getContextId());
            stmt.setInt(3, session.getUserId());
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            stmt = con.prepareStatement("DELETE FROM compositionSpaceAttachmentBinary WHERE uuid=? AND cid=? AND user=?");
            stmt.setBytes(1, UUIDs.toByteArray(uuid));
            stmt.setInt(2, session.getContextId());
            stmt.setInt(3, session.getUserId());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Creates a new empty file. If this method returns successfully then it is guaranteed that:
     * <ol>
     * <li>The file denoted by the returned abstract pathname did not exist before this method was invoked, and
     * <li>Neither this method nor any of its variants will return the same abstract pathname again in the current invocation of the virtual
     * machine.
     * </ol>
     *
     * @param autoManaged <code>true</code> to signal automatic management for the created file (deleted after processing thread terminates); otherwise <code>false</code> to let the caller control file's life-cycle
     * @param services The service look-up
     * @return An abstract pathname denoting a newly-created empty file
     * @throws OXException If a file could not be created
     */
    static File newTempFile(boolean autoManaged, ServiceLookup services) throws OXException {
        try {
            File uploadDir = services.getServiceSafe(UploadDirService.class).getUploadDir();

            File tmpFile = File.createTempFile("open-xchange-tmpfile-", ".tmp", uploadDir);
            tmpFile.deleteOnExit();
            if (autoManaged) {
                LogProperties.addTempFile(tmpFile);
            }
            return tmpFile;
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Deletes specified temporary file and returns <code>null</code>.
     *
     * @param tempFile The temporary file to delete
     * @return Always <code>null</code>
     */
    static File deleteFile(File tempFile) {
        if (tempFile != null && !tempFile.delete()) {
            LoggerHolder.LOG.error("Failed to delete temporary file: {}", tempFile);
        }
        return null;
    }

    /**
     * Flushes, then closes given output stream and returns <code>null</code>..
     *
     * @param out The output stream to close
     * @return Always <code>null</code>
     * @throws IOException If an I/O error occurs
     */
    static OutputStream flushAndClose(OutputStream out) throws IOException {
        out.flush();
        Streams.close(out);
        return null;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final class RdbDataProvider implements DataProvider {

        private final Session session;
        private final String storageIdentifier;
        private final ServiceLookup services;
        private final int contextId;

        /**
         * Initializes a new {@link DataProviderImplementation}.
         */
        RdbDataProvider(Session session, String storageIdentifier, ServiceLookup services) {
            super();
            this.session = session;
            this.storageIdentifier = storageIdentifier;
            this.services = services;
            this.contextId = session.getContextId();
        }

        @Override
        public InputStream getData() throws OXException {
            DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);

            Connection con = databaseService.getReadOnly(contextId);
            try {
                return doGetData(con, services);
            } finally {
                databaseService.backReadOnly(contextId, con);
            }
        }

        private InputStream doGetData(Connection con, ServiceLookup services) throws OXException {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                byte[] uuidBytes = UUIDs.toByteArray(CompositionSpaces.parseAttachmentId(storageIdentifier));

                stmt = con.prepareStatement("SELECT 1 FROM compositionSpaceAttachmentBinary WHERE uuid=? AND cid=? AND user=?");
                stmt.setBytes(1, uuidBytes);
                stmt.setInt(2, contextId);
                stmt.setInt(3, session.getUserId());
                rs = stmt.executeQuery();
                if (false == rs.next()) {
                    throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_RESOURCE.create(storageIdentifier);
                }

                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                stmt = con.prepareStatement("SELECT data FROM compositionSpaceAttachmentBinaryChunk WHERE uuid=? AND cid=? AND user=? ORDER BY chunk");
                stmt.setBytes(1, uuidBytes);
                stmt.setInt(2, contextId);
                stmt.setInt(3, session.getUserId());
                rs = stmt.executeQuery();

                if (!rs.next()) {
                    return Streams.EMPTY_INPUT_STREAM;
                }

                // At least one chunk available
                return new ResultSetConsumingStream(rs, services);
            } catch (SQLException e) {
                throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
            } catch (IOException e) {
                throw CompositionSpaceErrorCode.IO_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
        }
    } // End of class RdbDataProvider

    private static class ResultSetConsumingStream extends InputStream {

        private final InputStream in;
        private File tempFile;

        /**
         * Initializes a new {@link ResultSetConsumingStream}.
         */
        ResultSetConsumingStream(ResultSet preSelectedResultSet, ServiceLookup services) throws SQLException, IOException, OXException {
            super();
            File tempFile = null;
            OutputStream out = null;
            InputStream in = preSelectedResultSet.getBinaryStream(1);
            try {
                ByteArrayOutputStream baos = Streams.newByteArrayOutputStream(0xFFFF);
                out = baos;
                long count = 0;
                int inMemoryThreshold = IN_MEMORY_THRESHOLD;

                int buflen = 0xFFFF; // 64KB
                byte[] buffer = new byte[buflen];
                int read;
                do {
                    read = in.read(buffer, 0, buflen);
                    if (read <= 0 && preSelectedResultSet.next()) {
                        in = closeQuietly(in);
                        in = preSelectedResultSet.getBinaryStream(1);
                        read = in.read(buffer, 0, buflen);
                    }

                    if (read > 0) {
                        count += read;
                        if ((null == tempFile) && (count > inMemoryThreshold) && baos != null) {
                            // Switch to file-backed output stream since in-memory threshold is exceeded
                            tempFile = newTempFile(false, services);
                            out = new FileOutputStream(tempFile);
                            baos.writeTo(out);
                            baos = null;
                        }
                        out.write(buffer, 0, read);
                    }
                } while (read > 0);
                out = flushAndClose(out);

                if (tempFile == null) {
                    this.in = Streams.asInputStream(baos);
                } else {
                    this.in = new FileInputStream(tempFile);
                    this.tempFile = tempFile;
                    tempFile = null;
                }
            } finally {
                Streams.close(in, out);
                deleteFile(tempFile);
            }
        }

        private static InputStream closeQuietly(InputStream in) {
            Streams.close(in);
            return null;
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public int read(byte b[]) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {
            return in.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return in.skip(n);
        }

        @Override
        public int available() throws IOException {
            return in.available();
        }

        @Override
        public void close() throws IOException {
            try {
                in.close();
            } finally {
                if (null != tempFile) {
                    tempFile = deleteFile(tempFile);
                }
            }
        }

        @Override
        public synchronized void mark(int readlimit) {
            in.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            in.reset();
        }

        @Override
        public boolean markSupported() {
            return in.markSupported();
        }

    } // End of class ResultSetConsumingStream

}
