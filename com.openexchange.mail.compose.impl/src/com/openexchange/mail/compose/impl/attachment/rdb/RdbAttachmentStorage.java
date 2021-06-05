/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.compose.impl.attachment.rdb;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.AttachmentStorageIdentifier;
import com.openexchange.mail.compose.AttachmentStorageType;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaces;
import com.openexchange.mail.compose.DataProvider;
import com.openexchange.mail.compose.KnownAttachmentStorageType;
import com.openexchange.mail.compose.impl.attachment.AbstractNonCryptoAttachmentStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link RdbAttachmentStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class RdbAttachmentStorage extends AbstractNonCryptoAttachmentStorage {

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
        return new RdbDataProvider(session, storageIdentifier.getIdentifier(), requireDatabaseService());
    }

    @Override
    protected AttachmentStorageIdentifier saveData(InputStream input, long size, Session session) throws OXException {
        if (null == input) {
            throw CompositionSpaceErrorCode.ERROR.create("Attempted attachment storage without an input stream");
        }

        int contextId = session.getContextId();
        DatabaseService databaseService = requireDatabaseService();
        Connection con = databaseService.getWritable(contextId);
        try {
            return saveData(input, session, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    private AttachmentStorageIdentifier saveData(InputStream input, Session session, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO compositionSpaceAttachmentBinary (uuid, cid, user, data) VALUES (?, ?, ?, ?)");
            UUID uuid = UUID.randomUUID();
            stmt.setBytes(1, UUIDs.toByteArray(uuid));
            stmt.setInt(2, session.getContextId());
            stmt.setInt(3, session.getUserId());
            stmt.setBinaryStream(4, input);
            stmt.executeUpdate();
            return new AttachmentStorageIdentifier(UUIDs.getUnformattedString(uuid));
        } catch (SQLException e) {
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
        Connection con = databaseService.getWritable(session.getContextId());
        try {
            return deleteData(storageIdentifier.getIdentifier(), session, con);
        } finally {
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

    private static final class RdbDataProvider implements DataProvider {

        private final Session session;
        private final String storageIdentifier;
        private final DatabaseService databaseService;
        private final int contextId;

        /**
         * Initializes a new {@link DataProviderImplementation}.
         */
        RdbDataProvider(Session session, String storageIdentifier, DatabaseService databaseService) {
            super();
            this.session = session;
            this.storageIdentifier = storageIdentifier;
            this.databaseService = databaseService;
            this.contextId = session.getContextId();
        }

        @Override
        public InputStream getData() throws OXException {
            Connection con = databaseService.getReadOnly(contextId);
            boolean closeCon = true;
            try {
                InputStream data = doGetData(con);
                closeCon = false;
                return data;
            } finally {
                if (closeCon) {
                    databaseService.backReadOnly(contextId, con);
                }
            }
        }

        private InputStream doGetData(Connection con) throws OXException {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            boolean closeStuff = true;
            try {
                stmt = con.prepareStatement("SELECT data FROM compositionSpaceAttachmentBinary WHERE uuid=? AND cid=? AND user=?");
                stmt.setBytes(1, UUIDs.toByteArray(CompositionSpaces.parseAttachmentId(storageIdentifier)));
                stmt.setInt(2, contextId);
                stmt.setInt(3, session.getUserId());
                rs = stmt.executeQuery();
                if (false == rs.next()) {
                    throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_RESOURCE.create(storageIdentifier);
                }

                ResourceClosingStream data = new ResourceClosingStream(rs.getBinaryStream(1), contextId, rs, stmt, con, databaseService);
                closeStuff = false;
                return data;
            } catch (SQLException e) {
                throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
            } finally {
                if (closeStuff) {
                    Databases.closeSQLStuff(rs, stmt);
                }
            }
        }
    } // End of class RdbDataProvider

    private static class ResourceClosingStream extends FilterInputStream {

        private final int contextId;
        private final DatabaseService databaseService;
        private ResultSet rs;
        private PreparedStatement stmt;
        private Connection con;

        /**
         * Initializes a new {@link ResourceClosingStream}.
         */
        ResourceClosingStream(InputStream in, int contextId, ResultSet rs, PreparedStatement stmt, Connection con, DatabaseService databaseService) {
            super(in);
            this.contextId = contextId;
            this.rs = rs;
            this.stmt = stmt;
            this.con = con;
            this.databaseService = databaseService;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                if (null != stmt) {
                    Databases.closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;
                }

                if (null != con) {
                    databaseService.backReadOnly(contextId, con);
                    con = null;
                }
            }
        }

    } // End of class ResourceClosingStream

}
