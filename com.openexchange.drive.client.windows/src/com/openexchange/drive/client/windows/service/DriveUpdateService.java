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

package com.openexchange.drive.client.windows.service;

import java.io.InputStream;
import java.util.Map;
import com.openexchange.drive.client.windows.files.UpdateFilesProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;
import com.openexchange.templating.OXTemplate;

/**
 * {@link DriveUpdateService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
@SingletonService
public interface DriveUpdateService {

    /**
     * This function serves the template data.
     *
     * @return The {@link OXTemplate}.
     * @throws TemplateException if it is unable to load the template
     */
    public OXTemplate getOxtenderSpecificTemplate() throws OXException;

    /**
     * Gets the URL to the installer file.
     *
     * @param hostData The host data
     * @param session The session providing user data
     * @return The URL to the installer file
     * @throws OXException If URL to the installer file cannot be returned
     */
    public String getInstallerDownloadUrl(HostData hostData, Session session) throws OXException;

    /**
     * Retrieves the values for the templates placeholder's.
     *
     * @param serverUrl The URL to the groupware server.
     * @param username The name of the current user.
     * @param branding The branding identifier.
     * @return A Map containing the values for the {@link OXTemplate}.
     * @throws OXException if branding isn't valid or if the values couldn't be retrieved
     */
    public Map<String, Object> getTemplateValues(String serverUrl, String username, String branding) throws OXException;

    /**
     * Gets the necessary permission that is needed to receive updates for the drive.
     * See {@link UserConfiguration}.
     *
     * @return String array of the necessary permission.
     */
    public String[] getNecessaryPermission();

    /**
     * Tests if this service is able to deliver the file identified by the given name.
     *
     * This method exists to support the old oxupdater and will eventually be removed in the future.
     *
     * @param fileName The file name. Must not be null.
     * @param branding The branding identifier.
     * @return <code>true</code> if this service is able to deliver that file.
     * @throws OXException if it is unable to test the responsibility
     */
    public boolean isResponsibleFor(String fileName, String branding) throws OXException;

    /**
     * Returns an {@link InputStream} to a file identified by the given file name and the given branding identifier.
     *
     * @param fileName The file name.
     * @param branding The branding identifier.
     * @return The {@link InputStream}.
     * @throws OXException If this service is not responsible for this file or if the file cannot be found.
     */
    public InputStream getFile(String fileName, String branding) throws OXException;

    /**
     * Returns the size of a file identified by the given file name and the given branding identifier.
     *
     * @param fileName The file name.
     * @param branding The branding identifier.
     * @return The file size.
     * @throws OXException If the file cannot be found.
     */
    public long getFileSize(String fileName, String branding) throws OXException;

    /**
     * Initialize the DriveUpdateService
     *
     * @param fileProvider The UpdateFileProvider
     * @throws OXException if fileProvider is null or if some configuration is missing
     */
    public void init(UpdateFilesProvider fileProvider) throws OXException;

    /**
     * Retrieves the filename of the executable file for the given branding
     *
     * @param branding The branding identifier
     * @return The name of the file.
     * @throws OXException if branding isn't valid or if it is unable to retrieve the filename
     */
    public String getExeFileName(String branding) throws OXException;

    /**
     * Retrieves the filename of the windows installer file for the given branding
     *
     * @param branding The branding identifier.
     * @return The name of the file.
     * @throws OXException if branding isn't valid or if it is unable to retrieve the filename
     */
    public String getMsiFileName(String branding) throws OXException;

    /**
     * Retrieves the system wide default branding identifier.
     * Default: "generic"
     *
     * @return The branding identifier.
     */
    public String getDefaultBranding();

}
