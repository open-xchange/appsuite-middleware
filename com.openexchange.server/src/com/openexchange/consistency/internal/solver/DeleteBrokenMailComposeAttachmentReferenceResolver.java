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

package com.openexchange.consistency.internal.solver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import com.openexchange.consistency.Entity;
import com.openexchange.consistency.osgi.ConsistencyServiceLookup;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link DeleteBrokenMailComposeAttachmentReferenceResolver}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class DeleteBrokenMailComposeAttachmentReferenceResolver implements ProblemSolver {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteBrokenMailComposeAttachmentReferenceResolver.class);
    }

    private static final String SELECT_UUID = "SELECT csid, uuid FROM compositionSpaceAttachmentMeta WHERE refId = ?;";
    private static final String DELETE_META = "DELETE FROM compositionSpaceAttachmentMeta WHERE refId = ?;";
    private static final String SELECT_SPACE = "SELECT attachments FROM compositionSpace WHERE uuid = ?;";
    private static final String UPDATE_SPACE = "UPDATE compositionSpace SET attachments = ? WHERE uuid = ?;";

    /**
     * Initializes a new {@link DeleteBrokenMailComposeAttachmentReferenceResolver}.
     */
    public DeleteBrokenMailComposeAttachmentReferenceResolver() {
        super();
    }

    @Override
    public void solve(Entity entity, Set<String> problems) throws OXException {
        Connection con = null;
        try {
            con = Database.get(entity.getContext(), true);

            if (DBUtils.tableExists(con, "compositionSpaceAttachmentMeta") && DBUtils.tableExists(con, "compositionSpace")) {
                NextReferenceId: for (String referenceId : problems) {
                    // Find corresponding Composition Space and attachment Id
                    UUID spaceId;
                    UUID attachmentId;
                    try (PreparedStatement stmt = con.prepareStatement(SELECT_UUID)) {
                        stmt.setString(1, referenceId);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (!rs.next()) {
                                continue NextReferenceId;
                            }

                            spaceId = UUIDs.toUUID(rs.getBytes("csid"));
                            attachmentId = UUIDs.toUUID(rs.getBytes("uuid"));
                        }
                    }

                    // Get attachments of the corresponding composition space
                    try (PreparedStatement stmt = con.prepareStatement(SELECT_SPACE)) {
                        stmt.setBytes(1, UUIDs.toByteArray(spaceId));
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                // Remove attachment from composition space
                                String attachments = rs.getString(1);
                                String newAttachments = removeAttachment(attachments, attachmentId);
                                try (PreparedStatement stmt2 = con.prepareStatement(UPDATE_SPACE)) {
                                    if (newAttachments == null) {
                                        stmt2.setNull(1, Types.VARCHAR);
                                    } else {
                                        stmt2.setString(1, newAttachments);
                                    }
                                    stmt2.setBytes(2, UUIDs.toByteArray(spaceId));
                                    stmt2.execute();
                                }
                            }
                        }
                    }

                    // Remove attachment meta data
                    try (PreparedStatement stmt = con.prepareStatement(DELETE_META)) {
                        stmt.setString(1, referenceId);
                        stmt.executeUpdate();
                    }
                }
            }

            if (DBUtils.tableExists(con, "compositionSpaceKeyStorage")) {
                for (String referenceId : problems) {
                    try (PreparedStatement stmt = con.prepareStatement("DELETE FROM compositionSpaceKeyStorage WHERE refId = ?")) {
                        stmt.setString(1, obfuscate(referenceId));
                        stmt.executeUpdate();
                    }
                }
            }

        } catch (SQLException | OXException | RuntimeException e) {
            LoggerHolder.LOG.error("{}", e.getMessage(), e);
        } finally {
            Database.back(entity.getContext(), true, con);
        }
    }

    @Override
    public String description() {
        return "Delete CompositionSpace Reference";
    }

    private String removeAttachment(String attachments, UUID toRemove) {
        try {
            JSONArray json = new JSONArray(attachments);
            String idToRemove = UUIDs.getUnformattedString(toRemove);
            for (Iterator<Object> iterator = json.iterator(); iterator.hasNext();) {
                String a = (String) iterator.next();
                if (a.equals(idToRemove)) {
                    iterator.remove();
                }
            }
            return json.isEmpty() ? null : json.toString();
        } catch (JSONException | ClassCastException e) {
            LoggerHolder.LOG.error("Unable to parse {} to a List of Attachments", attachments, e);
        }
        return null;
    }

    /**
     * Obfuscates given string.
     *
     * @param s The string
     * @return The obfuscated string
     * @throws OXException If service is missing
     */
    private String obfuscate(String s) throws OXException {
        ObfuscatorService obfuscatorService = ConsistencyServiceLookup.getOptionalService(ObfuscatorService.class);
        if (null == obfuscatorService) {
            throw ServiceExceptionCode.absentService(ObfuscatorService.class);
        }
        return obfuscatorService.obfuscate(s);
    }

}
