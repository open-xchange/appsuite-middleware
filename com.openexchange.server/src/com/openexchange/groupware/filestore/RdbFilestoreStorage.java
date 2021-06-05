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

package com.openexchange.groupware.filestore;

import static com.openexchange.java.Autoboxing.I;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.impl.DBPool;

public class RdbFilestoreStorage extends FilestoreStorage {

    @Override
    public Filestore getFilestore(int id) throws OXException {
        Connection con = DBPool.pickup();
        try {
            return getFilestore(con, id);
        } finally {
            DBPool.closeReaderSilent(con);
        }
    }

    @Override
    public Filestore getFilestore(Connection con, int id) throws OXException {
        if (null == con) {
            return getFilestore(id);
        }

        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT uri, size, max_context FROM filestore WHERE id = ?");
            stmt.setInt(1,id);
            result = stmt.executeQuery();
            if (!result.next()) {
                throw FilestoreExceptionCodes.NO_SUCH_FILESTORE.create(I(id));
            }

            FilestoreImpl filestore = new FilestoreImpl();
            filestore.setId(id);
            setUriAsString(result.getString(1), filestore);
            filestore.setSize(result.getLong(2));
            filestore.setMaxContext(result.getLong(3));
            return filestore;
        } catch (SQLException e) {
            throw FilestoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(result, stmt);
        }
    }

    @Override
    public Filestore getFilestore(URI uri) throws OXException {
        Connection con = DBPool.pickup();
        try {
            return getFilestore(con, uri);
        } finally {
            DBPool.closeReaderSilent(con);
        }
    }

    /**
     * Gets the file store for given URI using specified connection.
     *
     * @param con The connection to use
     * @param uri The URI to resolve
     * @return The associated file store
     * @throws OXException If file store cannot be resolved
     */
    public Filestore getFilestore(Connection con, URI uri) throws OXException {
        if (null == con) {
            return getFilestore(uri);
        }

        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id, uri, size, max_context FROM filestore WHERE (? LIKE CONCAT(uri, '%'))");
            String sUriToLookUp = uri.toString();
            stmt.setString(1, sUriToLookUp);
            result = stmt.executeQuery();
            if (!result.next()) {
                throw FilestoreExceptionCodes.NO_SUCH_FILESTORE.create(sUriToLookUp);
            }

            sUriToLookUp = Strings.asciiLowerCase(sUriToLookUp);
            FilestoreImpl filestore = new FilestoreImpl();
            filestore.setId(result.getInt(1));
            String sUri = result.getString(2);
            setUriAsString(sUri, filestore);
            filestore.setSize(result.getLong(3));
            filestore.setMaxContext(result.getLong(4));

            // Check if there are further matches
            if (result.next() == false) {
                // No further matches
                return filestore;
            }

            // Collect all matches...
            Map<String, FilestoreImpl> filestores = new LinkedHashMap<>(4);
            filestores.put(sUri, filestore);
            do {
                filestore = new FilestoreImpl();
                filestore.setId(result.getInt(1));
                sUri = result.getString(2);
                setUriAsString(sUri, filestore);
                filestore.setSize(result.getLong(3));
                filestore.setMaxContext(result.getLong(4));
                filestores.put(sUri, filestore);
            } while (result.next());
            Databases.closeSQLStuff(result, stmt);
            result = null;
            stmt = null;

            return findMatchOrElse(sUriToLookUp, filestores);
        } catch (SQLException e) {
            throw FilestoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(result, stmt);
        }
    }

    /**
     * Finds the appropriate match for specified URI from given file storage mapping.
     *
     * @param uriToLookUp The URI (as string) to look-up
     * @param filestores The collection of file storages
     * @return The matching file storage
     * @throws OXException If no suitable file storage can be found
     */
    static Filestore findMatchOrElse(String uriToLookUp, Map<String, FilestoreImpl> filestores) throws OXException {
        for (Map.Entry<String, FilestoreImpl> fs : filestores.entrySet()) {
            String sUri = Strings.asciiLowerCase(fs.getKey());
            if (uriToLookUp.equals(sUri)) {
                // Exact match; e.g. "s3://mys3" == "s3://mys3"
                return fs.getValue();
            }
            if (sUri.endsWith("/")) {
                if (uriToLookUp.equals(sUri.substring(0, sUri.length() - 1))) {
                    // Exact match; e.g. "s3://mys3" == "s3://mys3/"
                    return fs.getValue();
                }
            } else {
                sUri = new StringBuilder(sUri.length() + 1).append(sUri).append('/').toString();
            }
            if (uriToLookUp.startsWith(sUri)) {
                // Match by path prefix; e.g. "s3://mys3/myprefix" starts with "s3://mys3/"
                return fs.getValue();
            }
        }

        throw FilestoreExceptionCodes.NO_SUCH_FILESTORE.create(uriToLookUp);
    }

    /**
     * Sets the given URI string to specified file store.
     *
     * @param uri The URI to set
     * @param filestore The file store to apply URI to
     * @throws OXException If given URI string cannot be parsed into a {@link java.net.URI}
     */
    static void setUriAsString(String uri, FilestoreImpl filestore) throws OXException {
        try {
            filestore.setUri(new URI(uri));
        } catch (URISyntaxException e) {
            throw FilestoreExceptionCodes.URI_CREATION_FAILED.create(e, uri);
        }
    }

}
