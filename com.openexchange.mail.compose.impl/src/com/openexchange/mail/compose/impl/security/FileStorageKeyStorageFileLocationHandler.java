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

package com.openexchange.mail.compose.impl.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.filestore.AbstractFileLocationHandler;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.ObfuscatorService;

/**
 * {@link FileStorageKeyStorageFileLocationHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class FileStorageKeyStorageFileLocationHandler extends AbstractFileLocationHandler {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link FileStorageKeyStorageFileLocationHandler}.
     */
    public FileStorageKeyStorageFileLocationHandler(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void updateFileLocations(Map<String, String> prevFileName2newFileName, int contextId, Connection con) throws SQLException {
        updateFileLocationsUsing0(prevFileName2newFileName, contextId, con);
    }

    /**
     * Updates the file locations using given statements.
     *
     * @param prevFileName2newFileName The file name mappings
     * @param contextId The context identifier
     * @param updateStmt The UPDATE statement; e.g. <code>"UPDATE prg_attachment SET file_id = ? WHERE cid = ? AND file_id = ?"</code>
     * @param con The connection to use
     * @throws SQLException If an SQL error occurs
     */
    private void updateFileLocationsUsing0(Map<String, String> prevFileName2newFileName, int contextId, Connection con) throws SQLException {
        int size = prevFileName2newFileName.size();
        if (size <= 0) {
            return;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Iterator<String> allPrevFileNames = prevFileName2newFileName.keySet().iterator();
            for (int i = 0; i < size; i += IN_LIMIT) {
                int chunkSizeSize = i + IN_LIMIT > size ? size - i : IN_LIMIT;

                List<String> chunkPrevFileNames = new ArrayList<String>(chunkSizeSize);
                for (int j = chunkSizeSize; j-- > 0;) {
                    chunkPrevFileNames.add(allPrevFileNames.next());
                }

                stmt = con.prepareStatement("SELECT refId FROM compositionSpaceKeyStorage WHERE cid=? AND dedicatedFileStorageId=0 AND refId IN " + getSqlInStringFor(chunkSizeSize));
                // Fill values
                int pos = 0;
                stmt.setInt(++pos, contextId);
                {
                    Iterator<String> chunkIter = chunkPrevFileNames.iterator();
                    for (int j = chunkSizeSize; j-- > 0;) {
                        stmt.setString(++pos, obfuscate(chunkIter.next()));
                    }
                }

                // Query for existent file names (if any)
                rs = stmt.executeQuery();
                if (rs.next()) {
                    // Collect existent file names
                    List<String> existent = chunkPrevFileNames;
                    existent.clear();
                    chunkPrevFileNames = null;
                    do {
                        existent.add(unobfuscate(rs.getString(1)));
                    } while (rs.next());
                    Databases.closeSQLStuff(rs, stmt);
                    rs = null;

                    stmt = con.prepareStatement("UPDATE compositionSpaceKeyStorage SET refId = ? WHERE cid = ? AND refId = ?");
                    for (String prevFileName : existent) {
                        String newFileName = prevFileName2newFileName.get(prevFileName);
                        stmt.setString(1, obfuscate(newFileName));
                        stmt.setInt(2, contextId);
                        stmt.setString(3, obfuscate(prevFileName));
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
                Databases.closeSQLStuff(rs, stmt);
            }
        } catch (OXException e) {
            throw new SQLException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
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
            stmt = con.prepareStatement("SELECT refId FROM compositionSpaceKeyStorage WHERE cid=? AND dedicatedFileStorageId=0");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptySet();
            }

            Set<String> locations = new LinkedHashSet<String>();
            do {
                locations.add(unobfuscate(rs.getString(1)));
            } while (rs.next());
            return locations;
        } catch (OXException e) {
            throw new SQLException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Obfuscates given string.
     *
     * @param s The string
     * @return The obfuscated string
     * @throws OXException If service is missing
     */
    private String obfuscate(String s) throws OXException {
        ObfuscatorService obfuscatorService = services.getOptionalService(ObfuscatorService.class);
        if (null == obfuscatorService) {
            throw ServiceExceptionCode.absentService(ObfuscatorService.class);
        }
        return obfuscatorService.obfuscate(s);
    }

    /**
     * Un-Obfuscates given string.
     *
     * @param s The obfuscated string
     * @return The plain string
     * @throws OXException If service is missing
     */
    private String unobfuscate(String s) throws OXException {
        ObfuscatorService obfuscatorService = services.getOptionalService(ObfuscatorService.class);
        if (null == obfuscatorService) {
            throw ServiceExceptionCode.absentService(ObfuscatorService.class);
        }
        return obfuscatorService.unobfuscate(s);
    }

}
