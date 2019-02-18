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

package com.openexchange.mail.compose.impl.attachment;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.AttachmentStorageType;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaces;
import com.openexchange.mail.compose.DataProvider;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link RdbAttachmentStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class RdbAttachmentStorage extends AbstractAttachmentStorage {

    /**
     * Initializes a new {@link RdbAttachmentStorage}.
     */
    public RdbAttachmentStorage(ServiceLookup services) {
        super(services);
    }

    @Override
    public AttachmentStorageType getStorageType() {
        return AttachmentStorageType.DATABASE;
    }

    @Override
    public List<String> neededCapabilities() {
        return Collections.emptyList();
    }

    @Override
    protected DataProvider getDataProviderFor(String storageIdentifier, Session session) throws OXException {
        return new RdbDataProvider(session, storageIdentifier, requireDatabaseService());
    }

    @Override
    protected String saveData(InputStream input, long size, Session session) throws OXException {
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

    private String saveData(InputStream input, Session session, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO compositionSpaceAttachmentBinary (uuid, cid, user, data) VALUES (?, ?, ?, ?)");
            UUID uuid = UUID.randomUUID();
            stmt.setBytes(1, UUIDs.toByteArray(uuid));
            stmt.setInt(2, session.getContextId());
            stmt.setInt(3, session.getUserId());
            stmt.setBinaryStream(4, input);
            stmt.executeUpdate();
            return UUIDs.getUnformattedString(uuid);
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    protected boolean deleteData(String storageIdentifier, Session session) throws OXException {
        if (Strings.isEmpty(storageIdentifier)) {
            throw CompositionSpaceErrorCode.ERROR.create("Attempted attachment deletion without an identifier");
        }

        DatabaseService databaseService = requireDatabaseService();
        Connection con = databaseService.getWritable(session.getContextId());
        try {
            return deleteData(storageIdentifier, session, con);
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
