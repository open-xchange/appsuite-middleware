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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.security.manager.configurationReader;

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.security.manager.OXSecurityManager;
import com.openexchange.security.manager.impl.FolderPermission;

/**
 * {@link FileStorageReader} reads the list of filestores from configdb, any file types
 * found are added to the security allowed list for read and write
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.3
 */
public class FileStorageReader {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileStorageReader.class);

    private final String FILE_PARAMETER = "file:";

    private final DatabaseService dbService;
    private final OXSecurityManager securityManager;

    public FileStorageReader(DatabaseService dbService, OXSecurityManager securityManager) {
        this.dbService = dbService;
        this.securityManager = securityManager;
    }

    /**
     * Search the configdb filestore for stores with URI fitting a start pattern
     *
     * @param startPattern  Search of the database will be startPattern plus '%'
     * @return List of URIs that match the pattern
     */
    private List<String> getFileStorageURIList (String startPattern) {
        List<String> uri = null;
        {
            Connection con = null;
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                con = dbService.getReadOnly();
                stmt = con.prepareStatement("SELECT uri FROM filestore WHERE uri LIKE ?");
                stmt.setString(1, startPattern + '%');
                result = stmt.executeQuery();
                if (false == result.next()) {
                    return uri;
                }

                uri = new LinkedList<>();
                do {
                    uri.add(result.getString(1));
                } while (result.next());
            } catch (SQLException | OXException e) {
                LOG.error("Error reading filestore list from configdb", e);
            } finally {
                closeSQLStuff(result, stmt);
                if (null != con) {
                    dbService.backReadOnly(con);
                }
            }
        }
        return uri;
    }

    /**
     * Gets list of file type filestores from configdb, then adds read/write permission to security manager
     *
     * @throws OXException
     */
    public void updateSecurityForFileStore() throws OXException {
        List<String> URIs = getFileStorageURIList(FILE_PARAMETER);
        if (URIs != null && URIs.size() > 0) {
            ArrayList<FolderPermission> folderPermissions = new ArrayList<FolderPermission>(URIs.size());
            for (String uri : URIs) {
                // Cleanup URI, as it will appear like file:/var/spool
                String dir = uri.replace(FILE_PARAMETER, "");  // remove the file parameter
                FolderPermission folder =
                    new FolderPermission(dir,
                        dir,
                        FolderPermission.Decision.ALLOW,
                        FolderPermission.Allow.READ_WRITE,
                        FolderPermission.Type.RECURSIVE);
                folderPermissions.add(folder);
            }
            securityManager.insertFolderPolicy(folderPermissions);
        }
    }

}
