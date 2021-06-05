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

package com.openexchange.mail.compose.impl.attachment.filestore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.database.Databases;
import com.openexchange.groupware.filestore.AbstractFileLocationHandler;
import com.openexchange.mail.compose.KnownAttachmentStorageType;

/**
 * {@link FileStrorageAttachmentFileLocationHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class FileStrorageAttachmentFileLocationHandler extends AbstractFileLocationHandler {

    private final int fileStorageType;

    /**
     * Initializes a new {@link FileStrorageAttachmentFileLocationHandler}.
     */
    public FileStrorageAttachmentFileLocationHandler() {
        super();
        fileStorageType = KnownAttachmentStorageType.CONTEXT_ASSOCIATED_FILE_STORAGE.getType();
    }

    @Override
    public void updateFileLocations(Map<String, String> prevFileName2newFileName, int contextId, Connection con) throws SQLException {
        String selectStmt = "SELECT refId FROM compositionSpaceAttachmentMeta WHERE cid=? AND refType=" + fileStorageType + " AND refId IN ";
        String updateStmt = "UPDATE compositionSpaceAttachmentMeta SET refId = ? WHERE cid = ? AND refId = ?";
        updateFileLocationsUsing(prevFileName2newFileName, contextId, selectStmt, updateStmt, con);
    }

    @Override
    public Set<String> determineFileLocationsFor(int userId, int contextId, Connection con) throws SQLException {
        // Files for attachments are always stored in context-related storage
        return Collections.emptySet();
    }

    @Override
    public Set<String> determineFileLocationsFor(int contextId, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT refId FROM compositionSpaceAttachmentMeta WHERE cid=? AND refType=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, fileStorageType);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptySet();
            }

            Set<String> locations = new LinkedHashSet<String>();
            do {
                locations.add(rs.getString(1));
            } while (rs.next());
            return locations;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

}
