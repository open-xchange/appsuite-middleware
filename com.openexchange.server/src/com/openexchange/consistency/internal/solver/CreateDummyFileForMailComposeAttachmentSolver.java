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
import java.sql.SQLException;
import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.consistency.Entity;
import com.openexchange.consistency.osgi.ConsistencyServiceLookup;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link CreateDummyFileForMailComposeAttachmentSolver}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class CreateDummyFileForMailComposeAttachmentSolver extends CreateDummyFileSolver implements ProblemSolver {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CreateDummyFileForMailComposeAttachmentSolver.class);
    }

    /**
     * Initializes a new {@link CreateDummyFileForMailComposeAttachmentSolver}.
     *
     * @param storage
     */
    public CreateDummyFileForMailComposeAttachmentSolver(FileStorage storage) {
        super(storage);
    }

    @Override
    public void solve(Entity entity, Set<String> problems) throws OXException {
        Connection con = null;
        try {
            con = Database.get(entity.getContext(), true);
            if (DBUtils.tableExists(con, "compositionSpaceAttachmentMeta")) {
                for (String referenceId : problems) {
                    String identifier = createDummyFile(storage);
                    try (PreparedStatement stmt = con.prepareStatement("UPDATE compositionSpaceAttachmentMeta SET refId = ?, size = ? WHERE refId = ?")) {
                        stmt.setString(1, identifier);
                        stmt.setLong(2, 0L);
                        stmt.setString(3, referenceId);
                        stmt.execute();
                    }
                }
            }
            if (DBUtils.tableExists(con, "compositionSpaceKeyStorage")) {
                for (String referenceId : problems) {
                    String identifier = createDummyFile(storage);
                    try (PreparedStatement stmt = con.prepareStatement("UPDATE compositionSpaceKeyStorage SET refId = ? WHERE refId = ?")) {
                        stmt.setString(1, obfuscate(identifier));
                        stmt.setString(2, obfuscate(referenceId));
                        stmt.execute();
                    }
                }
            }
        } catch (SQLException | OXException | RuntimeException e) {
            LoggerHolder.LOG.error("", e);
        } finally {
            Database.back(entity.getContext(), true, con);
        }
    }

    @Override
    public String description() {
        return "Create dummy file for Mail Compose Attachment.";
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
