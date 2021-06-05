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

package com.openexchange.drive.client.windows.files;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;

/**
 * {@link UpdateFilesProvider} provides all setup files for the update
 *
 * Initially checks for existing setup files and provides them afterwards
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public interface UpdateFilesProvider {

    /**
     * Forget all knows configuration's and setup files and search them under the last used path.
     *
     * @throws OXException if branding's couldn't be reloaded
     */
    public void reload() throws OXException;

    /**
     * Forget all knows configuration's and setup files and search them under given path.
     *
     * @throws OXException if branding's couldn't be reloaded
     */
    void reload(String path) throws OXException;

    /**
     * Returns the given setup file as a stream
     *
     * @param branding The branding of the file.
     * @param name The filename
     * @return the given file as a stream
     * @throws OXException if it is unable to retrieve the file
     */
    public InputStream getFile(String branding, String name) throws OXException;

    /**
     * Tests if the provider knows the given file und the given brand.
     *
     * @param branding The branding identifier
     * @param name The name of the file
     * @return true if it knows the file, false otherwise
     * @throws OXException if it is unable to test
     */
    public boolean contains(String branding, String name) throws OXException;

    /**
     * Retrieves the size of the given file
     *
     * @param branding The branding identifier
     * @param name The name of the file
     * @return The size of the file
     * @throws OXException if branding isn't valid or if it is unable to retrieve the file size
     */
    public long getSize(String branding, String name) throws OXException;

    /**
     * Retrieves the first filename which matches the given regex expression.
     *
     * @param branding The branding identifier
     * @param regex A regex expression
     * @return the name or null
     * @throws OXException if branding isn't valid or if it is unable to retrieve the filename
     */
    public String getFileName(String branding, Pattern regex) throws OXException;

    /**
     * Retrieves the md5 checksum for the given file.
     *
     * @param branding The branding identifier
     * @param name The name of the file
     * @return The md5 checksum
     * @throws OXException if it is unable to calculate the md5
     */
    public String getMD5(String branding, String name) throws OXException;

    /**
     * Retrieves the icon for the given branding as a base64 String.
     *
     * @param branding The branding identifier
     * @return The icon as a base64 String
     * @throws OXException if is is unable to retrieve the icon
     */
    public String getIcon(String branding) throws OXException;

    /**
     * Retrieves the branding identifiers of all available branding's.
     *
     * @return A list of identifiers.
     */
    public List<String> getAvailableBrandings();

    /**
     * Tests if the Provider knows the given branding
     *
     * @param branding The branding identifier
     * @return true if the UpdateFilesProvider knows the branding, false otherwise.
     */
    public boolean contains(String branding);

}
