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

package com.openexchange.consistency.internal.solver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.consistency.Entity;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
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
                        stmt.setString(1, identifier);
                        stmt.setString(2, referenceId);
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

}
